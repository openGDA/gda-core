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
package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.test.BrokerDelegate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * This class always runs the same nexus scan but puts in various parts of the
 * scanning to see what effect they make.
 *
 * @author Matthew Gerring
 *
 */
public class NexusStepScanSpeedTest extends NexusTest {

	private static EventServiceImpl eservice;
	private static BrokerDelegate delegate;
	private IPointGenerator<AxialStepModel> gen;

	@BeforeClass
    public static void createEventService() throws Exception {

		delegate = new BrokerDelegate();
		delegate.start();

		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new ScanningAPIClassRegistry()));
		eservice = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!

		// We publish an event to make sure all these libraries are loaded
		IPublisher<ScanBean> publisher = eservice.createPublisher(delegate.getUri(), EventConstants.SCAN_TOPIC);
		publisher.broadcast(new ScanBean());

		// We write a nexus file to ensure that the library is loaded
		File file = File.createTempFile("test_nexus", ".nxs");
		file.deleteOnExit();
		IPointGenerator<AxialStepModel> gen = pointGenService.createGenerator(new AxialStepModel("xNex", 0, 3, 1));
		final IRunnableDevice<ScanModel> scan = runnableDeviceService.createRunnableDevice(new ScanModel(gen, file));
		scan.run(null);

	}
	@AfterClass
    public static void stop() throws Exception {
		delegate.stop();
	}


	@Before
	public void before() throws GeneratorException, IOException {
		this.gen = pointGenService.createGenerator(new AxialStepModel("xNex", 0, 1000, 1));
	}

	@Test
	public void testBareNexusStepScanSpeedNoNexus() throws Exception {

		// We create a step scan
		final IRunnableDevice<ScanModel> scan = runnableDeviceService.createRunnableDevice(new ScanModel(gen));
		runAndCheck("No NeXus scan", scan, 5, 1, 100L);
	}


	@Test
	public void testBareNexusStepNoSetSlice() throws Exception {

		IScannable<?> scannable = connector.getScannable("xNex");
		MockNeXusScannable xNex = (MockNeXusScannable)scannable;
		try {
			xNex.setWritingOn(false);
			// We create a step scan
			final IRunnableDevice<ScanModel> scan = runnableDeviceService.createRunnableDevice(new ScanModel(gen, output));
			runAndCheck("Scan no 'setSlice'", scan, 10, 3072, 2000L);
		} finally {
			xNex.setWritingOn(true);
		}
	}

	@Test
	public void testBareNexusStepScanSpeed() throws Exception {

		// We create a step scan
		final IRunnableDevice<ScanModel> scan = runnableDeviceService.createRunnableDevice(new ScanModel(gen, output));
		runAndCheck("Normal NeXus Scan", scan, 10, 3072, 2000L);
	}

	@Test
	public void testPublishedNexusStepScanSpeed() throws Exception {

		// We create a step scan
		IPublisher<ScanBean> publisher = eservice.createPublisher(delegate.getUri(), EventConstants.SCAN_TOPIC);
		final IRunnableDevice<ScanModel> scan = runnableDeviceService.createRunnableDevice(new ScanModel(gen, output), publisher);
		runAndCheck("NeXus with Publish", scan, 10, 3072, 2000L);
	}


	private void runAndCheck(String name, final IRunnableDevice<ScanModel> scan, int pointTime, int fileSizeKB, long treeTime) throws Exception {

		long before = System.currentTimeMillis();
		scan.run(null);
		long after = System.currentTimeMillis();

		long time = (after-before);

		AbstractRunnableDevice<ScanModel> ascan = (AbstractRunnableDevice<ScanModel>)scan;
		System.out.println("\n------------------------------");
		System.out.println(name);
		System.out.println("------------------------------");
		System.out.println("Configure time was "+ascan.getConfigureTime()+" ms");
		System.out.println("Ran in "+time+"ms not including tree write time");
		System.out.println("Ran "+gen.size()+" points at "+(time/gen.size())+"ms/pnt");
		System.out.println("File size is "+output.length()/1024+"kB");
		System.out.println();

		assertTrue("The configure time must be less than "+treeTime+"ms", ascan.getConfigureTime()<treeTime);
		assertTrue("The time must be less than "+pointTime+"ms", (time/gen.size())<pointTime);
		long sizeKB = (output.length()/1024);
		assertTrue("The size must be less than "+fileSizeKB+"kB. It is "+sizeKB+"kB", sizeKB<fileSizeKB);
	}
}