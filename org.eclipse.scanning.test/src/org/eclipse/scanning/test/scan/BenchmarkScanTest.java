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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

@Disabled("DAQ-1484 This test is flakey and so is being ignored for now. It performs benchmarks which should probably not be run in general.")
public class BenchmarkScanTest extends BrokerTest {


	private static long nexusSmall, nexusMedium, nexusSmallEvents, nexusMediumEvents;
	@BeforeAll
	public static void ensureLambdasLoaded() {
		// This is required so that we don't benchmark lambda loading.
		Arrays.asList(1,2,3).stream().map(x -> x+1).collect(Collectors.toList());
	}

	@AfterAll
	public static void checkTimes() throws Exception {
		assertTrue(nexusSmallEvents<(nexusSmall+20));
		assertTrue(nexusMediumEvents<(nexusMedium+20));
	}

	@BeforeEach
	public void start() throws Exception {
		ServiceTestHelper.registerTestDevices();
	}

	/**
	 * Required unless we use a benchmarking framework. However
	 * the test measures each increase in size and uses multiples
	 * plus the fudge. This avoids some of the benchmarking issues.
	 */
	private final static long fudge = 1200;

	@Test
	public void testLinearScanNoNexus() throws Exception {

		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCreateImage(false);  // Would put our times off.
		dmodel.setExposureTime(0.001); // Sleep 1ms on the mock detector.
		dmodel.setName("detector");
		IRunnableDevice<MockDetectorModel> detector = TestDetectorHelpers.createAndConfigureMockDetector(dmodel);

		benchmarkStep(new BenchmarkBean(256, 2000l, 1, true, detector)); // set things up

		// Benchmark things. A good idea to do nothing much else on your machine for this...
		long point1     = benchmarkStep(new BenchmarkBean(1,     200, 1, detector)); // should take not more than 2ms sleep + scan time

		// should take not more than 64*point1 + scan time
		long point10    = benchmarkStep(new BenchmarkBean(10,    (10*point1)+fudge,  10, detector));

		// should take not more than 4*point64 sleep + scan time
		long point100   = benchmarkStep(new BenchmarkBean(100,   (10*point10)+fudge, 12L,   10, detector));

//		// should take not more than 4*point64 sleep + scan time
//		long point1000  = benchmarkStep(new BenchmarkBean(1000,  (10*point100)+fudge, 10L, 10, detector));
//
//		// should take not more than 4*point64 sleep + scan time
//		long point10000 = benchmarkStep(new BenchmarkBean(10000, (10*point1000)+fudge, 10L, 10, detector));
	}

	@Test
	public void testLinearScanNexusSmall() throws Exception {
		System.out.println(">> testLinearScanNexusSmall");
	    nexusSmall = benchmarkNexus(64, 25L);
		System.out.println(">> done");
	}

	@Test
	public void testLinearScanNexusMedium() throws Exception {
		System.out.println(">> testLinearScanNexusMedium");
	    nexusMedium = benchmarkNexus(256, 50L);
		System.out.println(">> done");
	}

	private long benchmarkNexus(int imageSize, long max)  throws Exception {

		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setRealAxisName("xNex");
		model.setImaginaryAxisName("yNex");
		model.setColumns(imageSize);
		model.setRows(imageSize);
		model.setMaxIterations(1);
		model.setExposureTime(0.0);

		IRunnableDevice<MandelbrotModel> detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);

		final BenchmarkBean bean = new BenchmarkBean(256, 5000l, 1, true, detector);
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
		output.deleteOnExit();
		bean.setFilePath(output.getAbsolutePath());

		try {
			benchmarkStep(bean); // set things up

			// Benchmark things. A good idea to do nothing much else on your machine for this...
			long point1     = benchmarkStep(new BenchmarkBean(1, 100, 1, detector, output)); // should take not more than 2ms sleep + scan time

			// should take not more than 64*point1 + scan time
			long point10    = benchmarkStep(new BenchmarkBean(10,    (10*point1)+fudge, max,   10, detector, output));

			// should take not more than 4*point64 sleep + scan time
			long point100   = benchmarkStep(new BenchmarkBean(100,   (10*point10)+fudge, max,  10, detector, output));

			return point100/100;

		} finally {
		    output.delete();
		}
	}

	@Test
	public void testLinearScanNexusSmallWithEvents() throws Exception {
		System.out.println(">> testLinearScanNexusSmallWithEvents");
		nexusSmallEvents = benchmarkNexusWithEvents(64, 50L);
		System.out.println(">> done");
	}

	@Test
	public void testLinearScanNexusMediumWithEvents() throws Exception {
		System.out.println(">> testLinearScanNexusMediumWithEvents");
		nexusMediumEvents = benchmarkNexusWithEvents(256, 50L);
		System.out.println(">> done");
	}

	private long benchmarkNexusWithEvents(int imageSize, long max)  throws Exception {
		// We create a publisher and subscriber for the scan.
		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		final IPublisher<ScanBean> publisher = eventService.createPublisher(uri, EventConstants.STATUS_TOPIC);

		final ISubscriber<IScanListener> subscriber = eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
		final Set<Status> states = new HashSet<>(5);
		subscriber.addListener(new IScanListener() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				states.add(evt.getBean().getStatus());
			}
			@Override
			public void scanEventPerformed(ScanEvent evt) {

			}
		});

		File output = null;
		try {
			MandelbrotModel model = new MandelbrotModel();
			model.setName("mandelbrot");
			model.setRealAxisName("xNex");
			model.setImaginaryAxisName("yNex");
			model.setColumns(imageSize);
			model.setRows(imageSize);
			model.setMaxIterations(1);
			model.setExposureTime(0.0);

			IRunnableDevice<MandelbrotModel> detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);

			final BenchmarkBean bean = new BenchmarkBean(256, 5000l, 1, true, detector);
			output = File.createTempFile("test_mandel_nexus", ".nxs");
			output.deleteOnExit();
			bean.setFilePath(output.getAbsolutePath());
			bean.setPublisher(publisher);

			benchmarkStep(bean); // set things up

			// Benchmark things. A good idea to do nothing much else on your machine for this...
			long point1     = benchmarkStep(new BenchmarkBean(1, 200, 1, detector, output)); // should take not more than 2ms sleep + scan time

			// should take not more than 64*point1 + scan time
			long point10    = benchmarkStep(new BenchmarkBean(10,    (10*point1)+fudge, max,   10, detector, output, publisher));

			// should take not more than 4*point64 sleep + scan time
			long point100   = benchmarkStep(new BenchmarkBean(100,   (10*point10)+fudge, max,   10, detector, output, publisher));

			assertTrue(states.size()==3); // CONFIGURING, READY, RUNNING
			return point100/100;

		} finally {
			if (publisher!=null) publisher.disconnect();
		    if (output!=null) output.delete();
		}
	}


	/**
	 *
	 * @param size
	 * @param reqTime
	 * @param tries - we try several times to get the time because sometimes the gc will run.
	 * @param silent
	 * @return
	 * @throws Exception
	 */
	private long benchmarkStep(final BenchmarkBean bean) throws Exception {

		if (!bean.isSilent()) System.out.println("\nChecking that "+bean.getSize()+" points take "+bean.getReqTime()+"ms or less to run. Using "+bean.getTries()+" tries.");

		// Before, run, after, check time.
		final AxialStepModel smodel = new AxialStepModel(bean.getScannableName(), 0, bean.getSize(), 1);
		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("xNex");
		gmodel.setyAxisName("yNex");
		gmodel.setxAxisPoints(1);
		gmodel.setyAxisPoints(1);

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(2);
		box.setyAxisStart(2);
		box.setxAxisLength(5);
		box.setyAxisLength(5);
		gmodel.setBoundingBox(box);

		ScanModel scanModel = createTestScanner(bean.getDetector(), smodel, gmodel);
		if (bean.getFilePath()!=null) {
			scanModel.setFilePath(bean.getFilePath());
			System.out.println("File writing to "+bean.getFilePath());
		}

		// Create configured device.
		IRunnableDevice<ScanModel> scanner = ServiceProvider.getService(IScanService.class)
				.createScanDevice(scanModel, bean.getPublisher());

		long time = 0l;
		for (int i = 0; i < bean.getTries(); i++) {
			long before = System.currentTimeMillis();
			if (scanModel.getFilePath()!=null) {
				System.out.println("Scanning to "+(new File(scanModel.getFilePath())).getName());
			}
			scanner.run(null);
			long after = System.currentTimeMillis();

			time = (after-before);

			if (time>bean.getReqTime()) {
				continue;
			}

			break; // We got it low enough
		}

		final IDetectorModel dmodel = bean.getDetector().getModel();
		if (!bean.isSilent()) {
			System.out.println(bean.getSize()+" point(s) took "+time+"ms with detector exposure set to "+dmodel.getExposureTime()+"s");

			long pointTime = (time/bean.getSize());
			System.out.println("That's "+pointTime+"ms per point");
			assertTrue("It should not take longer than "+bean.getReqTime()+"ms to scan "+bean.getSize()+" points with mock devices set to 1 ms exposure.",
				    time<bean.getReqTime());

			assertTrue("The average scan point time is over "+bean.getMaxPointTime()+" it's "+pointTime, pointTime<=bean.getMaxPointTime());
		}

		if (scanModel.getFilePath()!=null) {
			final IDataHolder dh = ServiceProvider.getService(ILoaderService.class).getData(scanModel.getFilePath(), null);
			final ILazyDataset lz = dh.getLazyDataset("/entry/instrument/mandelbrot/data");
			System.out.println("Wrote dataset of shape: "+Arrays.toString(lz.getShape()));
		}

		// Attempt to make the VM roughly do the same thing each run.
		System.gc();
		System.runFinalization();
		Thread.sleep(100); // Hopefully something happens, but probably not unless we intentionally fill the heap.
		                   // We just need to avoid a gc during the benchmarking phase.

		return time;
	}

	private ScanModel createTestScanner(IRunnableDevice<? extends IDetectorModel> detector, IScanPointGeneratorModel... models) throws Exception {
		IPointGenerator<CompoundModel> gen = ServiceProvider.getService(IPointGeneratorService.class)
				.createCompoundGenerator(new CompoundModel(models));

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPointGenerator(gen);
		smodel.setDetector(detector);

		// Create a scan and run it without publishing events
		return smodel;
	}

}
