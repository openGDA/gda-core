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
package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MappingScanTest extends BrokerTest{

	private IEventService eservice;
	private IPublisher<ScanBean> publisher;
	private ISubscriber<IScanListener> subscriber;
	private IPointGeneratorService gservice;

	@Before
	public void createServices() {
		ServiceTestHelper.setupServices();

		gservice = ServiceTestHelper.getPointGeneratorService();
		eservice = ServiceTestHelper.getEventService();

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		publisher = eservice.createPublisher(uri, EventConstants.SCAN_TOPIC); // Do not copy this leave as null!
		subscriber = eservice.createSubscriber(uri, EventConstants.SCAN_TOPIC); // Do not copy this leave as null!
	}

	@After
	public void disconnect() throws Exception {
		publisher.disconnect();
		subscriber.disconnect();
	}

	/**
	 * This test mimics a scan being run
	 *
	 * Eventually we will need a test running the sequencing system.
	 *
	 * @throws Exception
	 */
	@Test
	public void testSimpleMappingScan() throws Exception {

		// Listen to events sent
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});

		// Simulate queueing it
		final ScanBean bean = new ScanBean();
		bean.setName("Test Mapping Scan");
		bean.setBeamline("I05-1");
		bean.setUserName("Joe Bloggs");
		bean.setDeviceState(DeviceState.READY);
		bean.setPreviousStatus(Status.SUBMITTED);
		bean.setStatus(Status.PREPARING);
		bean.setFilePath("/dls/tmp/fred.h5");
		bean.setDatasetPath("/entry/data");
		publisher.broadcast(bean);

		// Tell them we started it.
		bean.setPreviousStatus(Status.PREPARING);
		bean.setStatus(Status.RUNNING);
		publisher.broadcast(bean);

		bean.setSize(10);
		int ipoint = 0;

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(10);
		box.setyAxisStart(10);
		box.setxAxisLength(5);
		box.setyAxisLength(2);

		final TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(2);
		model.setxAxisPoints(5);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = gservice.createGenerator(model);

		// Outer loop temperature, will be scan command driven when sequencer exists.
		bean.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean);

		int index = -1;
		for (double temp = 273; temp < 283; temp++) {
			bean.setPoint(ipoint);
			bean.putPosition("temperature", ++index, temp);
			testDeviceScan(bean, gen);
			Thread.sleep(10); // Moving to the new temp takes non-zero time so I've heard.
			++ipoint;
		}

		bean.setPreviousStatus(Status.RUNNING);
		bean.setStatus(Status.COMPLETE);
		publisher.broadcast(bean);

		Thread.sleep(100); // Just to make sure all the message events come in

		assertTrue(gotBack.size() > 10);
		assertTrue(gotBack.get(1).scanStart());
		assertTrue(gotBack.get(gotBack.size() - 1).scanEnd());
	}

	private void testDeviceScan(ScanBean bean, IPointGenerator<TwoAxisGridPointsModel> gen) throws Exception {


		bean.setDeviceState(DeviceState.RUNNING);
		publisher.broadcast(bean);
		int size = 0;
		for (IPosition pnt : gen) {
			bean.putPosition("zebra_x", pnt.getIndex("X"), pnt.get("X"));
			bean.putPosition("zebra_y", pnt.getIndex("Y"), pnt.get("Y"));
			publisher.broadcast(bean);
			++size;
		}
		//System.out.println("Did hardware scan of size " + size);
		assertTrue("Did hardware scan of size " + size, size == gen.size());

		bean.setDeviceState(DeviceState.ARMED);
		publisher.broadcast(bean);

	}

}