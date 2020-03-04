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

import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanFinished;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.remotedataset.test.utilities.mock.LoaderServiceMock;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritingMandlebrotModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("DAQ-1484 This test is flakey and so is being ignored for now. It will be investigated as part of DAQ-1488")
public class ScanFinishedTest {

	protected IRunnableDeviceService      dservice;
	protected IScannableDeviceService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	protected ILoaderService              lservice;

	@Before
	public void setup() {

		ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningTestClassRegistry()),
				Arrays.asList(new PointsModelMarshaller())
				));
		eservice  = new EventServiceImpl(activemqConnectorService);

		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE
		connector = new MockScannableConnector(null);
		dservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)dservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		impl._register(MandelbrotModel.class, MandelbrotDetector.class);

		gservice  = new PointGeneratorService();

		lservice = new LoaderServiceMock();

		// Provide lots of services that OSGi would normally.
		ServiceTestHelper.setupServices();
	}

	@Test
	public void testScanCompletedNormally() throws Exception {
		IRunnableDevice<ScanModel> scanner = createStepScan(8, 5);
		NXentry entry = getNexusEntry(scanner);
		assertScanNotFinished(entry);

		scanner.run(null);

		assertEquals(DeviceState.ARMED, scanner.getDeviceState());
		assertScanFinished(entry);
	}

	@Test
	public void testScanAborted() throws Exception {
		ScanModel smodel = createStepModel(8, 5);
		MandelbrotModel mmodel = new MandelbrotModel("neXusScannable1", "neXusScannable2");
		mmodel.setExposureTime(0.1);
		smodel.setDetectors(dservice.createRunnableDevice(mmodel));

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);

		NXentry entry = getNexusEntry(scanner);
		assertScanNotFinished(entry);

		((AbstractRunnableDevice<ScanModel>) scanner).start(null);
		scanner.latch(200, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.RUNNING, scanner.getDeviceState()); // should still be running if scan not finished
		scanner.abort();
		boolean aborted = scanner.latch(250, TimeUnit.MILLISECONDS);
		assertEquals(DeviceState.ABORTED, scanner.getDeviceState());
		assertTrue(aborted);

		assertScanFinished(entry);
	}

	static class IntWrapper<T> {
		int wrappedInt;
	}

	@Test
	public void testAbortingPausedScan() throws Exception {
		IScanDevice scanner = createStepScan(80, 5);
		NXentry entry = getNexusEntry(scanner);
		assertScanNotFinished(entry);

		final IntWrapper<Integer> stepCountWrapper = new IntWrapper<>();
		scanner.addPositionListener(new IPositionListener() {
			@Override
			public void positionPerformed(PositionEvent event) throws ScanningException {
				stepCountWrapper.wrappedInt = event.getPosition().getStepIndex();
			}
		});

		scanner.start(null);
		scanner.latch(100, TimeUnit.MILLISECONDS);
		scanner.pause();

		// check the scan is paused
		scanner.latch(100, TimeUnit.MILLISECONDS); // make sure current point is finished
		int stepCountOnPause = stepCountWrapper.wrappedInt;
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		// check the scan is still paused after 250ms, (explicity checking no more positions have been completed)
		boolean scanFinished = scanner.latch(250, TimeUnit.MILLISECONDS);
		assertFalse(scanFinished);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		assertEquals(stepCountOnPause, stepCountWrapper.wrappedInt);

		scanner.abort();
		boolean aborted = scanner.latch(100, TimeUnit.MILLISECONDS);
		assertTrue(aborted);
		assertEquals(DeviceState.ABORTED, scanner.getDeviceState());
		assertEquals(stepCountOnPause, stepCountWrapper.wrappedInt);
	}

	@Test
	public void testScanFinally() throws Exception {

		ScanModel smodel = createStepModel(2, 2);
		MandelbrotModel mmodel = new MandelbrotModel("neXusScannable1", "neXusScannable2");
		smodel.setDetectors(dservice.createRunnableDevice(mmodel));

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);
		scanner.run(null);

		assertEquals(DeviceState.ARMED, scanner.getDeviceState());

		@SuppressWarnings("rawtypes")
		IRunnableDevice device = dservice.getRunnableDevice(mmodel.getName());
		MandelbrotDetector detector = (MandelbrotDetector)device;
		assertTrue(detector._isScanFinallyCalled());
	}

	private NXentry getNexusEntry(IRunnableDevice<ScanModel> scanner) throws Exception {
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		try (NexusFile nf = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory().newNexusFile(filePath)) {
			nf.openToRead();

			TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			NXroot root = (NXroot) nexusTree.getGroupNode();
			return root.getEntry();
		}
	}

	private ScanModel createStepModel(int... size) throws Exception {

		CompoundModel cModel = new CompoundModel();

		// We add the outer scans, if any
		for (int dim = 0; dim < size.length; dim++) {
			if (size[dim]-1>0) {
				cModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,9.9d/(size[dim]-1)));
			} else {
				cModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,30)); // Will generate one value at 10
			}
		}

		IPointGenerator<CompoundModel> gen = gservice.createCompoundGenerator(cModel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPointGenerator(gen);

		// Create a file to scan into.
		File output = File.createTempFile("test_abort_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + smodel.getFilePath());

		return smodel;
	}
	private IScanDevice createStepScan(int... size) throws Exception {

		ScanModel smodel = createStepModel(size);

		// Create a scan and run it without publishing events
		IScanDevice scanner = (IScanDevice) dservice.createRunnableDevice(smodel, null);

		final IPointGenerator<?> fgen = smodel.getPointGenerator();
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException{
				System.out.println("Running acquisition scan of size "+fgen.size());
			}
		});

		return scanner;
	}

}
