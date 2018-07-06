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
package org.eclipse.scanning.test.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.server.servlet.PositionerServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RemoteRunnableServiceTest extends BrokerTest {

	private static IRunnableDeviceService dservice;
	private static IEventService eservice;
	private static AbstractResponderServlet<?> dservlet, pservlet;

	@BeforeClass
	public static void createServices() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		RemoteServiceFactory.setTimeout(1, TimeUnit.MINUTES); // Make test easier to debug.

		eservice = ServiceTestHelper.getEventService();
		dservice = ServiceTestHelper.getRunnableDeviceService();

		// Setup Servlets
		dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.setRequestTopic(EventConstants.DEVICE_REQUEST_TOPIC);
		dservlet.setResponseTopic(EventConstants.DEVICE_RESPONSE_TOPIC);
		dservlet.connect();

		pservlet = new PositionerServlet();
		pservlet.setBroker(uri.toString());
		pservlet.setRequestTopic(EventConstants.POSITIONER_REQUEST_TOPIC);
		pservlet.setResponseTopic(EventConstants.POSITIONER_RESPONSE_TOPIC);
		pservlet.connect();
	}

	@AfterClass
	public static void cleanup() throws EventException {
		dservlet.disconnect();
		pservlet.disconnect();
	}

	private         IRunnableDeviceService    rservice;

	@Before
	public void createService() throws EventException {
		rservice = eservice.createRemoteService(uri, IRunnableDeviceService.class);
	}

	@After
	public void disposeService() throws EventException {
		((IConnection)rservice).disconnect();
	}

	@Test
	public void checkNotNull() throws Exception {
		assertNotNull(rservice);
	}

	@Test
	public void testDrivePositioner() throws Exception {

		IPositioner pos1 = dservice.createPositioner("test");
		IPositioner pos2 = rservice.createPositioner("test");

		// Set them up the same.
		pos1.setPosition(new MapPosition("test", 0, 0));
		pos2.setPosition(new MapPosition("test", 0, 0));

		pos1.setPosition(new MapPosition("test", 0, Math.PI));
		assertTrue(pos2.getPosition().getValue("test")==Math.PI);
	}

	@Test
	public void testThatAbortDoesNotChangeHardware() throws Exception {

		IPositioner pos1 = dservice.createPositioner("test");
		if (rservice==null) rservice = eservice.createRemoteService(uri, IRunnableDeviceService.class);
		IPositioner pos2 = rservice.createPositioner("test");
		pos1.setPosition(new MapPosition("x", 0, 0));
		pos2.setPosition(new MapPosition("x", 0, 0));

		pos1.setPosition(new MapPosition("x", 0, 10)); // Should take 1 seconds
		pos2.abort();

		assertTrue(pos2.getPosition().getValue("x")==10); // Should reach 10 despite abort because setPosition is blocking.

	}

	// TODO Why does this pass locally an not on travis?
	//@Test
	public void testDeviceNames() throws Exception {

		Collection<String> names1 = dservice.getRunnableDeviceNames();
		Collection<String> names2 = rservice.getRunnableDeviceNames();
		assertTrue(names1!=null);
		assertTrue(names2!=null);
		assertTrue(names1.containsAll(names2));
		assertTrue(names2.containsAll(names1));
	}

	@Test
	public void testGetRunnableDevice() throws Exception {

		IRunnableDevice<?> dev1 = dservice.getRunnableDevice("mandelbrot");
		IRunnableDevice<?> dev2 = rservice.getRunnableDevice("mandelbrot");
		assertTrue(dev1!=null);
		assertTrue(dev2!=null);

		assertTrue(dev1.getLevel()==(dev2.getLevel()));
		assertTrue(dev1.getDeviceState().equals(dev2.getDeviceState()));

		final Object mod1 = dev1.getModel();
		final Object mod2 = dev2.getModel();
		assertTrue(mod1!=null);
		assertTrue(mod2!=null);
		assertTrue(mod1.equals(mod2));
	}


}
