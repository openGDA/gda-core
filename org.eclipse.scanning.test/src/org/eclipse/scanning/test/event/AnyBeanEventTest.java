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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.connector.jms.JmsConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class AnyBeanEventTest extends BrokerTest {

	private static IEventService eventService;
	private IPublisher<AnyBean> publisher;
	private ISubscriber<IBeanListener<AnyBean>> subscriber;

	@BeforeAll
	public static void setUpServices() {
		JmsConnectorService activemqConnectorService = new JmsConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new AnyBeanClassRegistry()));
		activemqConnectorService.setSessionService(ServiceProvider.getService(ISessionService.class));
		eventService = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!
	}

	@BeforeEach
	public void setUp() {
		publisher  = eventService.createPublisher(uri,  "my.custom.topic");
		subscriber = eventService.createSubscriber(uri, "my.custom.topic");
	}

	@AfterEach
	public void dispose() throws EventException {
		publisher.disconnect();
		subscriber.disconnect();
	}


	@Test
	public void blindBroadcastTest() throws Exception {

		final AnyBean bean = new AnyBean();
		bean.setName("fred");
		bean.setAddress("My home");
		bean.setDob(-1);
		bean.setTelephoneNumber("+44 666");
		publisher.broadcast(bean);
	}

	@Test
	public void checkedBroadcastTest() throws Exception {

		final AnyBean bean = new AnyBean();
		bean.setName("fred");
		bean.setAddress("My home");
		bean.setDob(-1);
		bean.setTelephoneNumber("+44 666");

		final List<AnyBean> gotBack = new ArrayList<AnyBean>(3);
		subscriber.addListener(new IBeanListener<AnyBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<AnyBean> evt) {
				gotBack.add(evt.getBean());
			}
		});

		publisher.broadcast(bean);

		Thread.sleep(500); // The bean should go back and forth in ms anyway

		if (!bean.equals(gotBack.get(0))) throw new Exception("Bean did not come back!");
	}


}
