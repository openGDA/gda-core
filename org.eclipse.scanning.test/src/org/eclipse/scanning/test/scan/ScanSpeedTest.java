/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanPause;
import org.eclipse.scanning.api.annotation.scan.ScanResume;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.annotation.scan.WriteComplete;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CollatedStepModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockDetectorModel;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockScannable;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("DAQ-1484 This test is flakey and so is being ignored for now. It performs benchmarks which should probably not be run in general.")
public class ScanSpeedTest extends BrokerTest {

	private IRunnableDeviceService      dservice;
	private IScannableDeviceService     connector;
	private IPointGeneratorService      gservice;

	@Before
	public void start() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		dservice = ServiceTestHelper.getRunnableDeviceService();
		connector = ServiceTestHelper.getScannableDeviceService();
		gservice = ServiceTestHelper.getPointGeneratorService();
	}

	/**
	 * 2 scannables, 2 detectors each divided by into 10 levels.
	 *
	 * @throws Exception
	 */
	@Test
	public void checkTimes1000PointsNoAnnotations() throws Exception {

		checkNoAnnotations(1000,2,2,5);

	}

	/**
	 * 2 scannables, 2 detectors each divided by into 10 levels.
	 *
	 * @throws Exception
	 */
	@Test
	public void checkTimes10000PointsNoAnnotations() throws Exception {

		checkNoAnnotations(10000,2,2,5);

	}

	/**
	 * 100 scannables, 100 detectors each divided by into 10 levels.
	 *
	 * @throws Exception
	 */
	@Test
	public void checkTimes100PointsNoAnnotations() throws Exception {

		checkNoAnnotations(100,100,100,20);

	}

	private void checkNoAnnotations(	int pointCount, int scannableCount, int detectorCount, long pointTime) throws Exception {

		final List<IScannable<?>> scannables = new ArrayList<>();
		MockScannableConnector mc = (MockScannableConnector)connector;
		for (int i = 0; i < scannableCount; i++) {
			MockScannable ms = new MockScannable("specialScannable"+i, 0d);
			ms.setRequireSleep(false);
			ms.setLevel(i%10);
			mc.register(ms);
			scannables.add(ms);
		}

		final List<IRunnableDevice<?>> detectors = new ArrayList<>(detectorCount);
		for (int i = 0; i < detectorCount; i++) {
			MockDetectorModel mod = new MockDetectorModel();
			mod.setName("detector"+i);
			mod.setCreateImage(false);  // Would put our times off.
			mod.setExposureTime(0);

			IRunnableDevice<?> dev = dservice.createRunnableDevice(mod);
			dev.setLevel(i%10);
			detectors.add(dev);
		}

		long time = checkTimes(pointCount, scannables, detectors, "no annotations");
		assertTrue(time<pointTime);
	}

	@Test
	public void checkTimes100PointsWithAnnotations() throws Exception {

		int pointCount     = 99;   // Gives 100 points because it's a step model

		final List<IScannable<?>>     scannables = createAnnotatedScannables("annotatedScannable", 100, false);
		final List<IRunnableDevice<?>> detectors = createAnnotatedDetectors("annotatedDetector", 100, false);

		long time = checkTimes(pointCount, scannables, detectors, "all annotations");
		assertTrue("Time should be less than 30ms and is: "+time, time<60);

		for (IScannable<?> s : scannables) {
			AnnotatedMockScannable ams = (AnnotatedMockScannable)s;
			assertEquals(1,    ams.getCount(ScanStart.class));
			assertEquals(0,    ams.getCount(FileDeclared.class)); // No file!
			assertEquals(100,  ams.getCount(PointStart.class));
			assertEquals(100,  ams.getCount(PointEnd.class));
			assertEquals(100,  ams.getCount(WriteComplete.class));
			assertEquals(100,  ams.getCount(LevelStart.class));
			assertEquals(100,  ams.getCount(LevelEnd.class));
			assertEquals(1,    ams.getCount(ScanEnd.class));
		}

		for (IRunnableDevice<?> d : detectors) {
			AnnotatedMockWritableDetector ams = (AnnotatedMockWritableDetector)d;
			assertEquals(1,    ams.getCount(PreConfigure.class));
			assertEquals(1,    ams.getCount(PostConfigure.class));
			assertEquals(1,    ams.getCount(ScanStart.class));
			assertEquals(0,    ams.getCount(FileDeclared.class)); // No file!
			assertEquals(100,  ams.getCount(PointStart.class));
			assertEquals(100,  ams.getCount(PointEnd.class));
			assertEquals(100,  ams.getCount(WriteComplete.class));
			assertEquals(100,  ams.getCount(LevelStart.class)); // run is called, but write is not as no nexus file is configured
			assertEquals(100,  ams.getCount(LevelEnd.class));   // run is called, but write is not as no nexus file is configured
			assertEquals(1,    ams.getCount(ScanEnd.class));
		}

	}

	@Test
	public void abortTest() throws Exception {

		final List<IScannable<?>>      scannables = createAnnotatedScannables("annotatedSleepingScannable", 10, true);
		final List<IRunnableDevice<?>> detectors  = createAnnotatedDetectors("annotatedWritingDetector", 10, true);

		IRunnableDevice<?> device = createDevice(100, scannables, detectors);
		device.start(null);
		device.latch(500, TimeUnit.MILLISECONDS);
		device.abort();
		Thread.sleep(100);

		for (IScannable<?> s : scannables) {
			AnnotatedMockScannable ams = (AnnotatedMockScannable)s;
			assertEquals(1,  ams.getCount(ScanStart.class));
			assertEquals(0,  ams.getCount(FileDeclared.class)); // No file!
			assertEquals(1,  ams.getCount(ScanAbort.class));
			assertEquals(0,  ams.getCount(ScanEnd.class));
		}

		for (IRunnableDevice<?> d : detectors) {
			AnnotatedMockWritableDetector ams = (AnnotatedMockWritableDetector)d;
			assertEquals(1,  ams.getCount(ScanStart.class));
			assertEquals(0,  ams.getCount(FileDeclared.class)); // No file!
			assertEquals(1,  ams.getCount(ScanAbort.class));
			assertEquals(0,  ams.getCount(ScanEnd.class));
			assertEquals(1,  ams.getCount(ScanFinally.class));
		}


	}

	@Test
	public void pauseTest() throws Exception {

		final List<IScannable<?>>      scannables = createAnnotatedScannables("annotatedSleepingScannable", 10, true);
		final List<IRunnableDevice<?>> detectors  = createAnnotatedDetectors("annotatedWritingDetector", 10, true);

		IPausableDevice<?> device = (IPausableDevice<?>)createDevice(10, scannables, detectors);
		device.start(null);
		Thread.sleep(500);
		device.pause();
		Thread.sleep(1000); // sure enough time to reach checkShouldContinue for @ScanPause annotation to be sent
		device.resume();
		device.latch(10, TimeUnit.SECONDS); // Latches until scan done.

		for (IScannable<?> s : scannables) {
			AnnotatedMockScannable ams = (AnnotatedMockScannable)s;
			assertEquals(1,  ams.getCount(ScanStart.class));
			assertEquals(0,  ams.getCount(FileDeclared.class)); // No file!
			assertEquals(1,  ams.getCount(ScanPause.class));
			assertEquals(1,  ams.getCount(ScanResume.class));
			assertEquals(0,  ams.getCount(ScanAbort.class));
			assertEquals(1,  ams.getCount(ScanEnd.class));
			assertEquals(1,  ams.getCount(ScanFinally.class));
		}

		for (IRunnableDevice<?> d : detectors) {
			AnnotatedMockWritableDetector ams = (AnnotatedMockWritableDetector)d;
			assertEquals(1,  ams.getCount(ScanStart.class));
			assertEquals(0,  ams.getCount(FileDeclared.class)); // No file!
			assertEquals(1,  ams.getCount(ScanPause.class));
			assertEquals(1,  ams.getCount(ScanResume.class));
			assertEquals(0,  ams.getCount(ScanAbort.class));
			assertEquals(1,  ams.getCount(ScanEnd.class));
			assertEquals(1,  ams.getCount(ScanFinally.class));
		}


	}


	private List<IRunnableDevice<?>> createAnnotatedDetectors(String namefrag, int detectorCount, boolean createImage) throws ScanningException {

		final List<IRunnableDevice<?>> detectors = new ArrayList<>(detectorCount);
		for (int i = 0; i < detectorCount; i++) {
			MockDetectorModel mod = new AnnotatedMockDetectorModel();
			mod.setName(namefrag+i);
			mod.setCreateImage(createImage);  // Would put our times off.
			mod.setExposureTime(0);

			IRunnableDevice<?> dev = dservice.createRunnableDevice(mod);
			dev.setLevel(i%10);
			detectors.add(dev);
		}
		return detectors;
	}

	private List<IScannable<?>> createAnnotatedScannables(String namefrag, int scannableCount, boolean requireSleep) {
		final List<IScannable<?>> scannables = new ArrayList<>();
		MockScannableConnector mc = (MockScannableConnector)connector;
		for (int i = 0; i < scannableCount; i++) {
			MockScannable ms = new AnnotatedMockScannable(namefrag+i, 0d);
			ms.setRequireSleep(requireSleep);
			ms.setLevel(i%10);
			mc.register(ms);
			scannables.add(ms);
		}
		return scannables;
	}


	private long checkTimes(int pointCount, List<IScannable<?>> scannables, List<IRunnableDevice<?>> detectors, String msg) throws Exception {


		IRunnableDevice<?> device = createDevice(pointCount, scannables, detectors);

		long before = System.currentTimeMillis();
		device.run(null);
		long after = System.currentTimeMillis();
		double single = (after-before)/(double)pointCount;
		System.out.println("Time for one point was ("+msg+"): "+Math.round(single)+"ms");

		return Math.round(single);

	}

	private IRunnableDevice<?> createDevice(int pointCount, List<IScannable<?>> scannables, List<IRunnableDevice<?>> detectors) throws Exception {

		final String[] names = new String[scannables.size()];
		for (int i = 0; i < scannables.size(); i++) {
			names[i] = scannables.get(i).getName();
		}

		final StepModel onek = new CollatedStepModel(0,pointCount,1,names);
		Iterable<IPosition> gen = gservice.createGenerator(onek);

		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);

		// Create a file to scan into.
		smodel.setFilePath(null); // Intentionally no nexus writing

	smodel.setDetectors(detectors);

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);

		return scanner;
	}
}
