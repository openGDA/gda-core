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
package org.eclipse.scanning.test.scan.real;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * This class is an object which can be started by spring on the GDA server.
 *
 * It receives commands and runs a simple test scan.
 *
 * @author fri44821
 *
 */
@Disabled("DAQ-2088 Invalid constructor for JUnit")
public class ScanExecutionTest extends BrokerTest {

	private static IEventService     eventService;
	private static IPointGeneratorService generatorService;
	private static IScanService  scanService;
	private static IScannableDeviceService connector;


	public static IScannableDeviceService getConnector() {
		return connector;
	}

	public static void setConnector(IScannableDeviceService connector) {
		ScanExecutionTest.connector = connector;
	}

	public ScanExecutionTest() {

	}

	/**
	 *
	 * @param uri - for activemq, for instance BrokerTest.uri
	 * @throws URISyntaxException
	 * @throws EventException
	 */
	public ScanExecutionTest(String uri) throws URISyntaxException, EventException {
		this();
		ISubscriber<IBeanListener<TestScanBean>> sub = eventService.createSubscriber(new URI(uri), "org.eclipse.scanning.test.scan.real.test");
		sub.addListener(new IBeanListener<TestScanBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<TestScanBean> evt) {
				try {
					executeTestScan();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	protected void executeTestScan() throws Exception {

		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setExposureTime(0.1);
		dmodel.setName("swmr");
		IWritableDetector<MockDetectorModel> detector = TestDetectorHelpers.createAndConfigureMockDetector(dmodel);
		assertNotNull(detector);

		detector.addRunListener(IRunListener.createRunPerformedListener(
				event -> System.out.println("Ran detector @ "+event.getPosition())));

		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 8, 5); // Outer scan of another scannable, for instance temp.
		scanner.run(null);
		System.out.println("done");
	}

	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<? extends IDetectorModel> detector, int... size) throws Exception {

		// Create scan points for a grid and make a generator
		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("smx");
		gmodel.setxAxisPoints(size[size.length-2]);
		gmodel.setyAxisName("smy");
		gmodel.setyAxisPoints(size[size.length-1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,2,2));

		CompoundModel cModel = new CompoundModel();
		for (int dim = 0; dim < size.length - 2; dim++) {
			cModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,
					size[dim] > 1 ? 9.9d/(size[dim]-1) : 30)); // Either N many points or 1 point at 10
		}
		cModel.addModel(gmodel);

		IPointGenerator<CompoundModel> gen = generatorService.createCompoundGenerator(cModel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPointGenerator(gen);
		smodel.setDetector(detector);

		// Create a file to scan into.
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(smodel);

		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ScanExecutionTest.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public static void setGeneratorService(IPointGeneratorService generatorService) {
		ScanExecutionTest.generatorService = generatorService;
	}

	public static IScanService getScanService() {
		return scanService;
	}

	public static void setScanService(IScanService scanService) {
		ScanExecutionTest.scanService = scanService;
	}

	/**
	 * This class is designed to be run as a spring object.
	 * It can also be run as a junit plugin test to check OSGi services are injected.
	 */
	@Test
	public void checkServices() throws Exception {
		assertNotNull(eventService);
		assertNotNull(generatorService);
		assertNotNull(scanService);
		assertNotNull(connector);
	}
}
