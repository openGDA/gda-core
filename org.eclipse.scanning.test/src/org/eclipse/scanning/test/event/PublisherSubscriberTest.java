/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.test.event;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PublisherSubscriberTest extends BrokerTest {

	private IPublisher<StatusBean> publisher;
	private ISubscriber<IBeanListener<StatusBean>> subscriber;

	@Before
	public void setUp() throws Exception {
		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(createNonOSGIActivemqMarshaller());
		IEventService eventService = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!

		publisher = eventService.createPublisher(uri, "test");
		subscriber = eventService.createSubscriber(uri, "test");
	}

	@After
	public void dispose() throws Exception {
		publisher.disconnect();
		subscriber.disconnect();
	}

	@Test
	public void testBroadcastSubscribe() throws Exception {
		final StatusBean bean = new StatusBean();
		bean.setName("fred");

		final TestBeanListener listener = new TestBeanListener(1);
		subscriber.addListener(listener);

		publisher.broadcast(bean);

		listener.awaitBeans();
		final List<StatusBean> beansReceived = listener.getBeansReceived();
		assertThat(beansReceived, hasSize(1));
		assertEquals(beansReceived.get(0), bean);
	}

	@Test
	public void testSynchronousSubscriber() throws Exception {
		testSubscriber(true);
	}

	@Test
	public void testAsynchronousSubscriber() throws Exception {
		testSubscriber(false);
	}

	public void testSubscriber(boolean synchronous) throws Exception {
		final List<StatusBean> beansToSend = Stream.of("one", "two", "three")
				.map(name -> new StatusBean(name))
				.collect(toList());

		final TestBeanListener listener = new TestBeanListener(3);
		subscriber.setSynchronous(synchronous);
		subscriber.addListener(listener);

		for (StatusBean beanToSend : beansToSend) {
			publisher.broadcast(beanToSend);
		}

		listener.awaitBeans();
		final List<StatusBean> beansReceived = listener.getBeansReceived();
		if (synchronous) {
			assertThat(beansReceived, is(equalTo(beansToSend)));
		} else {
			assertThat(beansReceived, containsInAnyOrder(beansToSend.toArray()));
		}

		final List<String> startFinishEvents = listener.getListenerStartEndEvents();
		if (synchronous) {
			final List<String> expectedStartFinishEvents = Arrays.asList(
					"one start", "one end", "two start", "two end", "three start", "three end");
			assertEquals(expectedStartFinishEvents, startFinishEvents);
		} else {
			int numBeans = beansToSend.size();
			assertThat(startFinishEvents.subList(0, numBeans),
					containsInAnyOrder("one start", "two start", "three start"));
			assertThat(startFinishEvents.subList(numBeans, startFinishEvents.size()),
					containsInAnyOrder("one end", "two end", "three end"));
		}
	}

	/**
	 * Check that no beans are processed after disconnection.
	 * @throws Exception
	 */
	@Test
	public void testDisconnect() throws Exception {
		final List<StatusBean> beansToSend = Stream.of("one", "two", "three", "four")
				.map(name -> new StatusBean(name))
				.collect(toList());

		final TestBeanListener listener = new TestBeanListener(2);
		listener.setSleepTime(1000);
		subscriber.addListener(listener);
		subscriber.setSynchronous(true);

		for (StatusBean beanToSend : beansToSend) {
			publisher.broadcast(beanToSend);
		}

		listener.awaitBeans();
		subscriber.disconnect();

		// run for longer to check the remaining beans aren't processed
		Thread.sleep(listener.sleepTime * 3);

		// test that only the first two beans are received
		final List<StatusBean> beansReceived = listener.getBeansReceived();
		assertThat(beansReceived, is(equalTo(beansToSend.subList(0, 2))));
	}

}
