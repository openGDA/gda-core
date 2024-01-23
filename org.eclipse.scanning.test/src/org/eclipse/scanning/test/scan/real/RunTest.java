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

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class RunTest extends BrokerTest{

	private IEventService            eservice;
	private IPublisher<TestScanBean> publisher;

	@BeforeEach
	public void before() {
		ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		activemqConnectorService.setSessionService(ServiceProvider.getService(ISessionService.class));
		eservice  = new EventServiceImpl(activemqConnectorService);
		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		publisher = eservice.createPublisher(uri, "org.eclipse.scanning.test.scan.real.test");

	}

	@AfterEach
	public void after() throws EventException {
		publisher.disconnect();
	}

	@Test
	public void testSendScan() throws Exception {
		TestScanBean info = new TestScanBean();
		info.setName("fred");
		publisher.broadcast(info);
	}
}
