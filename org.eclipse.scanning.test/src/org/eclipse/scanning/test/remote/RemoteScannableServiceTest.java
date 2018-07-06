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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
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

public class RemoteScannableServiceTest extends BrokerTest {

	private static IScannableDeviceService      cservice;
	private        IScannableDeviceService      rservice;
	private static IEventService                eservice;
	private static AbstractResponderServlet<?>  dservlet, pservlet;

	@BeforeClass
	public static void createServices() throws Exception {
		ServiceTestHelper.setupServices();
		eservice = ServiceTestHelper.getEventService();
		cservice = ServiceTestHelper.getScannableDeviceService();

		RemoteServiceFactory.setTimeout(1, TimeUnit.MINUTES); // Make test easier to debug.

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
		System.out.println("Made Servlets");
	}

	@Before
	public void createRemoteScannableDeviceService() throws EventException {
		rservice = eservice.createRemoteService(uri, IScannableDeviceService.class);
	}

	@After
	public void disposeRemoteScannableDeviceService() throws EventException {
		((IConnection)rservice).disconnect();
		rservice = null;
	}


	@AfterClass
	public static void cleanup() throws EventException {
		dservlet.disconnect();
		pservlet.disconnect();
	}

	@Test
	public void checkNotNull() throws Exception {
		assertNotNull(rservice);
	}

	@Test
	public void testScannableNames() throws Exception {

		Collection<String> names1 = cservice.getScannableNames();
		Collection<String> names2 = rservice.getScannableNames();
		assertTrue(names1!=null);
		assertTrue(names2!=null);
		assertTrue(names1.containsAll(names2));
		assertTrue(names2.containsAll(names1));
	}

	@Test
	public void testGetScannable() throws Exception {

		IScannable<Double> xNex1 = cservice.getScannable("xNex");
		IScannable<Double> xNex2 = rservice.getScannable("xNex");
		assertTrue(xNex1!=null);
		assertTrue(xNex2!=null);
	}

	@Test
	public void testScannablePositionLocal() throws Exception {

		IScannable<Double> xNex1 = cservice.getScannable("xNex");
		IScannable<Double> xNex2 = rservice.getScannable("xNex");
		scannableValues(xNex1, xNex2);
	}

	@Test
	public void testScannablePositionRemote() throws Exception {

		IScannable<Double> xNex1 = cservice.getScannable("xNex");
		IScannable<Double> xNex2 = rservice.getScannable("xNex");
		scannableValues(xNex2, xNex1);
	}

	@Test
	public void testDisconnect() throws Exception {
		// regression test for DAQ-1479
		IConnection connection = (IConnection) rservice;
		assertTrue(connection.isConnected());
		connection.disconnect();
		assertFalse(connection.isConnected());
	}

	private void scannableValues(IScannable<Double> setter, IScannable<Double> getter) throws Exception {
		assertTrue(setter!=null);
		assertTrue(getter!=null);

		for (int i = 0; i < 10; i++) {
			setter.setPosition(i*10d);
//			System.out.println("Set "+setter.getName()+" to value "+(i*10d)+" It's value is "+setter.getPosition());
			assertTrue(getter.getPosition()==(i*10d));
//			System.out.println("The value of "+setter.getName()+" was also "+getter.getPosition());
		}
	}

	@Test
	public void addFive() throws Exception {
		checkTemperature(5);
	}

	@Test
	public void subtractFive() throws Exception {
		checkTemperature(-5);
	}

	private void checkTemperature(double delta) throws Exception {
		System.out.println("rservice = " + rservice);
		IScannable<Double> temp = rservice.getScannable("T");

		CountDownLatch latch = new CountDownLatch(10); // MockScannable.doRealisticMove() always fires 10 events during the move
		((IPositionListenable)temp).addPositionListener(new IPositionListener() {
			@Override
			public void positionChanged(PositionEvent evt) throws ScanningException {
				double val = (Double)evt.getPosition().get("T");
				System.out.println("The value of T was at "+val);
				latch.countDown();
			}
		});

		double currPosition = temp.getPosition();
		double newPosition = currPosition + delta;

		System.out.println("Moving to "+ newPosition+" from "+currPosition);
		temp.setPosition(newPosition);

		// Ensure 10 events were received before the timeout
		assertTrue("Latch broke before 10 events were received", latch.await(60, TimeUnit.SECONDS)); //TODO decrease time
		// Ensure T is now at the new position
		assertEquals(newPosition, temp.getPosition(), 1e-15);
	}

}
