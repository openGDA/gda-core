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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.io.File;

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
import org.eclipse.scanning.connector.jms.JmsConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.test.BrokerDelegate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 *
 * This class always runs the same nexus scan but puts in various parts of the
 * scanning to see what effect they make.
 *
 * @author Matthew Gerring
 *
 */
class NexusStepScanSpeedTest extends NexusTest {

	private static EventServiceImpl eservice;
	private static BrokerDelegate delegate;
	private IPointGenerator<AxialStepModel> gen;

	@BeforeAll
    static void createEventService() throws Exception {
		delegate = new BrokerDelegate();
		delegate.start();

		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		final JmsConnectorService activemqConnectorService = new JmsConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new ScanningAPIClassRegistry()));
		activemqConnectorService.setSessionService(ServiceProvider.getService(ISessionService.class));
		eservice = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!

		// We publish an event to make sure all these libraries are loaded
		final IPublisher<ScanBean> publisher = eservice.createPublisher(delegate.uri, EventConstants.SCAN_TOPIC);
		publisher.broadcast(new ScanBean());

		// We write a nexus file to ensure that the library is loaded
		final File file = File.createTempFile("test_nexus", ".nxs");
		file.deleteOnExit();
		final IPointGenerator<AxialStepModel> gen = pointGenService.createGenerator(new AxialStepModel("xNex", 0, 3, 1));
		final IRunnableDevice<ScanModel> scan = scanService.createScanDevice(new ScanModel(gen, file));
		scan.run(null);
	}

	@AfterAll
    static void stop() throws Exception {
		delegate.stop();
	}

	@BeforeEach
	void before() throws GeneratorException {
		this.gen = pointGenService.createGenerator(new AxialStepModel("xNex", 0, 1000, 1));
	}

	@Test
	void testBareNexusStepScanSpeedNoNexus() throws Exception {
		// We create a step scan
		final IRunnableDevice<ScanModel> scan = scanService.createScanDevice(new ScanModel(gen));
		runAndCheck("No NeXus scan", scan, 5, 1, 100L);
	}


	@Test
	void testBareNexusStepNoSetSlice() throws Exception {
		final IScannable<?> scannable = scannableDeviceService.getScannable("xNex");
		final MockNeXusScannable xNex = (MockNeXusScannable) scannable;
		try {
			xNex.setWritingOn(false);
			// We create a step scan
			final IRunnableDevice<ScanModel> scan = scanService.createScanDevice(new ScanModel(gen, output));
			runAndCheck("Scan no 'setSlice'", scan, 10, 3072, 2000L);
		} finally {
			xNex.setWritingOn(true);
		}
	}

	@Test
	void testBareNexusStepScanSpeed() throws Exception {
		// We create a step scan
		final IRunnableDevice<ScanModel> scan = scanService.createScanDevice(new ScanModel(gen, output));
		runAndCheck("Normal NeXus Scan", scan, 10, 3072, 2000L);
	}

	@Test
	void testPublishedNexusStepScanSpeed() throws Exception {
		// We create a step scan
		IPublisher<ScanBean> publisher = eservice.createPublisher(delegate.uri, EventConstants.SCAN_TOPIC);
		final IRunnableDevice<ScanModel> scan = scanService.createScanDevice(new ScanModel(gen, output), publisher);
		runAndCheck("NeXus with Publish", scan, 10, 3072, 2000L);
	}


	private void runAndCheck(String name, final IRunnableDevice<ScanModel> scan, int pointTime, int fileSizeKB, long treeTime) throws Exception {
		final long before = System.currentTimeMillis();
		scan.run(null);
		final long after = System.currentTimeMillis();
		final long time = (after-before);

		final AbstractRunnableDevice<ScanModel> ascan = (AbstractRunnableDevice<ScanModel>)scan;
		System.out.println("\n------------------------------");
		System.out.println(name);
		System.out.println("------------------------------");
		System.out.println("Configure time was "+ascan.getConfigureTime()+" ms");
		System.out.println("Ran in "+time+"ms not including tree write time");
		System.out.println("Ran "+gen.size()+" points at "+(time/gen.size())+"ms/pnt");
		System.out.println("File size is "+output.length()/1024+"kB");
		System.out.println();

		assertThat("The configure time must be less than "+treeTime+"ms", ascan.getConfigureTime(), is(lessThan(treeTime)));
		assertThat("The time must be less than "+pointTime+"ms", (time/gen.size()), is(lessThan((long) pointTime)));
		final long sizeKB = (output.length()/1024);
		assertThat("The size must be less than "+fileSizeKB+"kB. It is "+sizeKB+"kB", sizeKB, is(lessThan((long) fileSizeKB)));
	}
}
