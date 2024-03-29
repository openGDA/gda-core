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
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class ScanTest extends BrokerTest {

	private static IScannableDeviceService scannableDeviceService;

	@BeforeAll
	public static void setUpServices() {
		scannableDeviceService = ServiceProvider.getService(IScannableDeviceService.class);
	}

	@BeforeEach
	public void setup() throws Exception {
		ServiceTestHelper.registerTestDevices();
	}

	@Test
	public void testSetSimplePosition() throws Exception {
		IPositioner pos = ServiceProvider.getService(IScanService.class).createPositioner("test");
		pos.setPosition(new MapPosition("x:0:1, y:0:2"));

		IScannable<Number> x = scannableDeviceService.getScannable("x");
		IScannable<Number> y = scannableDeviceService.getScannable("y");
		Number xpos = x.getPosition();
		Number ypos = y.getPosition();

		assertTrue(Math.round(xpos.doubleValue()) == 1d);
		assertTrue(Math.round(ypos.doubleValue()) == 2d);
	}

	@Test
	public void testNames() throws ScanningException {
		List<String> names = scannableDeviceService.getScannableNames();
		assertTrue(names.contains("x"));
		assertTrue(names.contains("xNex"));
		assertTrue(names.contains("yNex"));
	}

	@Test
	public void testLevels() throws Exception {

		IPositioner pos = ServiceProvider.getService(IScanService.class).createPositioner("test");

		final List<String> scannablesMoved = new ArrayList<>(6);
		pos.addPositionListener(new IPositionListener() {
			@Override
			public void levelPerformed(PositionEvent evt) {
				for (INameable s : evt.getLevelObjects()) scannablesMoved.add(s.getName());
			}
		});

		pos.setPosition(new MapPosition("a:0:10, b:0:10, p:0:10, q:0:10, x:0:10, y:0:10"));

		assertTrue(scannablesMoved.get(0).equals("a") || scannablesMoved.get(0).equals("b"));
		assertTrue(scannablesMoved.get(1).equals("a") || scannablesMoved.get(1).equals("b"));
		assertTrue(scannablesMoved.get(2).equals("p") || scannablesMoved.get(2).equals("q"));
		assertTrue(scannablesMoved.get(3).equals("p") || scannablesMoved.get(3).equals("q"));
		assertTrue(scannablesMoved.get(4).equals("x") || scannablesMoved.get(4).equals("y"));
		assertTrue(scannablesMoved.get(5).equals("x") || scannablesMoved.get(5).equals("y"));

		for (String name : pos.getPosition().getNames()) {
			assertTrue(scannableDeviceService.getScannable(name).getPosition().equals(10d));
		}
	}

	@Test
	public void testMassiveMove() throws Exception {

		MapPosition pos = new MapPosition();
		for (int ilevel = 0; ilevel < 100; ilevel++) {
			for (int iscannable = 0; iscannable < 1000; iscannable++) {
				String name = "pos"+ilevel+"_"+iscannable;

				// We set the level in this loop, normally this comes
				// in via spring.
				IScannable<?> motor = scannableDeviceService.getScannable(name);
				motor.setLevel(ilevel);
				if (motor instanceof MockScannable) ((MockScannable)motor).setRequireSleep(false);

				// We set the position required
				pos.put(name, ilevel+iscannable);
			}
		}

		IPositioner positioner = ServiceProvider.getService(IScanService.class).createPositioner("test");

		final List<String> levelsMoved = new ArrayList<>(6);
		positioner.addPositionListener(new IPositionListener() {
			@Override
			public void levelPerformed(PositionEvent evt) {
				for (ILevel s : evt.getLevelObjects()) {
					levelsMoved.add(String.valueOf(s.getLevel()));
				}
			}
		});

		long start = System.currentTimeMillis();
		positioner.setPosition(pos);
		long end   = System.currentTimeMillis();

		// Check the size
		assertTrue(levelsMoved.size()==100000);

		// Check that the level order was right
		final List<String> sorted = new ArrayList<String>(levelsMoved.size());
	    sorted.addAll(levelsMoved);
	    Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.parseInt(o1)-Integer.parseInt(o2);
			}
		});

	    for (int i = 0; i < levelsMoved.size(); i++) {
		    assertEquals("The wrong level was encountered sorted='"+sorted.get(i)+"' moved='"+levelsMoved.get(i)+"'", levelsMoved.get(i), sorted.get(i));
		}

		System.out.println("Positioning 100,000 test motor with 100 levels took "+(end-start)+" ms");
	}

	@Test
	public void testSimpleScan() throws Exception {

		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null, null);
		scanner.run(null);
		checkRun(scanner);
	}

	@Disabled("DAQ-2088 This now regularly fails on  Jenkins with 'Cannot to do readout' from MockWritableDetector#run")
	@Test
	public void testAbortSimpleScan() throws Exception {
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setExposureTime(0.01);
		dmodel.setName("detector");
		IRunnableDevice<MockDetectorModel> detector = TestDetectorHelpers.createAndConfigureMockDetector(dmodel);

		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null, detector);
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			try {
				scanner.abort();
			} catch (ScanningException | InterruptedException e) {
				e.printStackTrace();
			}
		}, 100, TimeUnit.MILLISECONDS);
		scanner.start(null);
		try {
			scanner.latch(10, TimeUnit.SECONDS); // latch calls createException to rethrow any exceptions thrown by run()
			fail("InterruptedException expected");
		} catch (InterruptedException e) {
			assertEquals("User requested abort", e.getMessage());
		}
		assertEquals(DeviceState.ABORTED, scanner.getDeviceState());
	}

	@Test
	public void testThreadCount() throws Exception {

		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null, null);
		int before = Thread.activeCount();
		scanner.run(null);
		if (scannableDeviceService instanceof IConnection) ((IConnection)scannableDeviceService).disconnect();
		Thread.sleep(25); // Just allows any threads no longer required to exit.
		int after = Thread.activeCount();
		System.out.println("Before = "+before);
		System.out.println("After  = "+after);
		int tolerance = 3;
		System.out.println("Tolerance  = "+tolerance);
		if (after>before+tolerance) throw new Exception("too many extra threads after scan! Expected not more than "+(before+tolerance)+" got "+after);
	}


	@Test
	public void testStepScan() throws Exception {

		AxialStepModel model = new AxialStepModel();
		model.setStart(0);
		model.setStop(100);
		model.setStep(1);
		model.setName("myScannable");

		IRunnableDevice<ScanModel> scanner = createTestScanner(model, null, null, null, null, null);
		scanner.run(null);
		checkRun(scanner);
	}

	@Test
	public void testInvalidStepScan() throws Exception {

		AxialStepModel model = new AxialStepModel();
		model.setStart(0);
		model.setStop(10);
		model.setStep(-1);
		model.setName("myScannable");

		try {
			IRunnableDevice<ScanModel> scanner = createTestScanner(model, null, null, null, null, null);

			// Cast to AbstractRunnableDevice gives us non-blocking .start() method.
			((AbstractRunnableDevice<ScanModel>) scanner).start(null);

			Thread.sleep(5000);  // testStepScan (the valid one) takes ~2 seconds total.

		} catch (Exception ex) {
			assertEquals(GeneratorException.class, ex.getClass());
			assertEquals(InvocationTargetException.class, ex.getCause().getClass());
			assertEquals(ModelValidationException.class, ex.getCause().getCause().getClass());
			assertTrue(ex.getCause().getCause().getMessage().toLowerCase().indexOf("wrong direction")>0);
			return;
		}

		throw new Exception("Scanner failed to throw an exception.");
	}

	@Test
	public void testZeroStepStepScan() throws Exception {

		AxialStepModel model = new AxialStepModel();
		model.setStart(0);
		model.setStop(10);
		model.setStep(0);
		model.setName("myScannable");

		try {
			IRunnableDevice<ScanModel> scanner = createTestScanner(model, null, null, null, null, null);

			// Cast to AbstractRunnableDevice gives us non-blocking .start() method.
			((AbstractRunnableDevice<ScanModel>) scanner).start(null);

			Thread.sleep(5000);  // testStepScan (the valid one) takes ~2 seconds total.

		} catch (Exception ex) {
			assertEquals(GeneratorException.class, ex.getClass());
			assertEquals(InvocationTargetException.class, ex.getCause().getClass());
			assertEquals(ModelValidationException.class, ex.getCause().getCause().getClass());
			assertEquals("Model step size must be nonzero for nonzero length!", ex.getCause().getCause().getMessage());
			return;
		}

		throw new Exception("Scanner failed to throw an exception.");
	}


	@Test
	public void testScanError() throws Exception {

		// 1. Set the model to make the detector throw an exception
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setExposureTime(0.001);
		dmodel.setAbortCount(3); // Aborts on the third write call by throwing an exception
		IWritableDetector<MockDetectorModel> detector = TestDetectorHelpers.createAndConfigureMockDetector(dmodel);

		// 2. Check run fails and check exception is that which the detector provided
		// Not some horrible reflection one.
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null, detector);
		boolean ok=false;
		try {
		    scanner.run(null);
		} catch (ScanningException expected) {
			if (!expected.getMessage().equals("The detector had a problem running! This exception should stop the scan running!")) {
				if (expected.getCause()!=null) expected.getCause().printStackTrace();
				throw new Exception("Expected the precise message from the mock detector not to be lost but it was! It was '"+expected.getMessage()+"'");
			}
			ok=true;
		}
		if (!ok) throw new Exception("The exception was not thrown by the scan as expected!");

		// 3. Check that it died after 3 and it is FAULT
		assertEquals(3, dmodel.getRan());
		assertTrue(scanner.getDeviceState()==DeviceState.FAULT);

		// 4. Check that running it again fails
		ok=false;
		try {
			scanner.run(null);
		} catch (ScanningException expected) {
			ok=true;
		}
		if (!ok) throw new Exception("The exception was not thrown by the scan as expected!");
	}


	@Test
	public void testSimpleScanWithStatus() throws Exception {


		final ScanBean bean = new ScanBean();
		bean.setName("Fred");
		bean.setUniqueId("fred");

		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		final IPublisher<ScanBean> publisher = eventService.createPublisher(uri, EventConstants.STATUS_TOPIC);

		final ISubscriber<IScanListener> subscriber = eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
		final List<ScanBean>    events = new ArrayList<ScanBean>(11);
		final List<Status> states = new ArrayList<Status>(11);
		subscriber.addListener(new IScanListener() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				states.add(evt.getBean().getStatus());
			}
			@Override
			public void scanEventPerformed(ScanEvent evt) {
				events.add(evt.getBean());
//				System.out.println("Percent complete : "+evt.getBean().getPercentComplete());
//				System.out.println(evt.getBean().getPosition());
			}
		});

		try {

			// Create a scan and run it without publishing events
			IRunnableDevice<ScanModel> scanner = createTestScanner(null, bean, publisher, null, null, null);
			scanner.run(null);

			Thread.sleep(100); // Wait for all events to make it over from ActiveMQ

			checkRun(scanner);

			// Bit of a hack to get the generator from the model - should this be easier?
			IPointGenerator<?> gen = ((ScanModel)((AbstractRunnableDevice)scanner).getModel()).getPointGenerator();
			assertEquals(Arrays.asList(Status.SUBMITTED, Status.PREPARING, Status.RUNNING), states);
			/*
			 * DAQ-1967 removed deviceStatus from ScanBean, changing final Scan Complete message,
			 * (which doesn't change the state from running as ScanProcess may have to perform e.g. after scan)
			 * The total number of message (25+4) sent before is maintained, but more events and less state changes.
			 * Only non test implementation of IScanListener (SubmitterImpl) reads all State and Event messages
			 */
			assertEquals(gen.size() + 1, events.size());

			for (ScanBean b : events) assertEquals("fred", b.getUniqueId());

		} finally {
			publisher.disconnect();
		}
	}

	@Test
	public void testSimpleScanSetPositionCalls() throws Exception {

		IScannable<Number> p = scannableDeviceService.getScannable("p");
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null, null, new String[]{"p","q"});

		scanner.run(null);

		checkRun(scanner);

		// NOTE Did with Mockito but caused dependency issues.
		MockScannable ms = (MockScannable)p;
		ms.verify(0, new Point("p",0,0, "q",0,0));
		ms.verify(0, new Point("p",0,0, "q",2,1.5));
		ms.verify(1.5, new Point("p",2,1.5, "q",0,0));
		ms.verify(1.5, new Point("p",2,1.5, "q",2,1.5));
	}

	@Test
	public void testSimpleScanWithMonitor() throws Exception {

		IScannable<Number> monitor = scannableDeviceService.getScannable("monitor");
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, monitor, null, null);

		scanner.run(null);

		checkRun(scanner);

		// NOTE Did with Mockito but caused dependency issues.
		MockScannable ms = (MockScannable)monitor;
		ms.verify(null, new Point(0,0,0,0));
		ms.verify(null, new Point(0,0,2,1.5));
		ms.verify(null, new Point(2,1.5,0,0));
		ms.verify(null, new Point(2,1.5,2,1.5));
	}

	private void checkRun(IRunnableDevice<ScanModel> scanner) throws Exception {
		// Bit of a hack to get the generator from the model - should this be easier?
		// Do not copy this code
		ScanModel smodel = (ScanModel)((AbstractRunnableDevice)scanner).getModel();
		IPointGenerator<?> gen = smodel.getPointGenerator();
		MockDetectorModel dmodel = (MockDetectorModel)((AbstractRunnableDevice)smodel.getDetectors().get(0)).getModel();
		assertEquals(gen.size(), dmodel.getRan());
		assertEquals(0, dmodel.getWritten()); // write() not called as no nexus file set
	}

	private IRunnableDevice<ScanModel> createTestScanner(AbstractPointsModel pmodel,
			final ScanBean bean,
			final IPublisher<ScanBean> publisher,
			IScannable<?> monitorsPerPoint,
			IScannable<?> monitorsPerScan,
			IRunnableDevice<MockDetectorModel> detector) throws Exception {
		return createTestScanner(pmodel, bean, publisher, monitorsPerPoint, monitorsPerScan, detector, null);
	}

	private IRunnableDevice<ScanModel> createTestScanner(AbstractPointsModel gridPointsModel,
														final ScanBean bean,
														final IPublisher<ScanBean> publisher,
														IScannable<?> monitorsPerPoint,
														IScannable<?> monitorsPerScan,
														IRunnableDevice<MockDetectorModel> detector,
														String[] axes) throws Exception {

		// Configure a detector with a collection time.
		if (detector == null) {
			final MockDetectorModel detModel = new MockDetectorModel();
			detModel.setExposureTime(0.001);
			detModel.setName("detector");
			detector = TestDetectorHelpers.createAndConfigureMockDetector(detModel);
		}

		// If none passed, create scan points for a grid.
		if (gridPointsModel == null) {
			gridPointsModel = new TwoAxisGridPointsModel("x", "y");
			((TwoAxisGridPointsModel) gridPointsModel).setyAxisPoints(5);
			((TwoAxisGridPointsModel) gridPointsModel).setxAxisPoints(5);
			((TwoAxisGridPointsModel) gridPointsModel).setBoundingBox(new BoundingBox(0,0,3,3));
		}

		if (axes!=null && gridPointsModel instanceof IBoundingBoxModel) {
			IBoundingBoxModel boundingBoxModel = (IBoundingBoxModel) gridPointsModel;
			boundingBoxModel.setxAxisName(axes[0]);
			boundingBoxModel.setyAxisName(axes[1]);
		}

		IPointGenerator<? extends IScanPointGeneratorModel> gen =
				ServiceProvider.getService(IPointGeneratorService.class).createGenerator(gridPointsModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(gen);
		scanModel.setScanPathModel(gridPointsModel);
		scanModel.setDetector(detector);
		scanModel.setBean(bean);
		if (monitorsPerPoint!=null) scanModel.setMonitorsPerPoint(monitorsPerPoint);
		if (monitorsPerScan!=null) scanModel.setMonitorsPerScan(monitorsPerScan);

		// Create a scan and run it without publishing events
		return ServiceProvider.getService(IScanService.class).createScanDevice(scanModel, publisher);
	}

}
