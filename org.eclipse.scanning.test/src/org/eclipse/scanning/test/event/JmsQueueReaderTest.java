/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.command.ActiveMQQueue;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.JmsQueueReader;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JmsQueueReaderTest extends BrokerTest {

	private IEventService eventService;
	private ISubmitter<StatusBean> submitter;
	private JmsQueueReader<StatusBean> jmsQueueReader;

	@Mock
	private IConsumer<StatusBean> mockConsumer;

	@Before
	public void setUp() throws Exception {
		initMocks(this);

		ServiceTestHelper.setupServices();
		eventService = ServiceTestHelper.getEventService();
		submitter = eventService.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		IEventService eventServiceSpy = spy(eventService);
		doReturn(mockConsumer).when(eventServiceSpy).getConsumer(EventConstants.SUBMISSION_QUEUE);
		jmsQueueReader = new JmsQueueReader<>(uri, eventServiceSpy, EventConstants.SUBMISSION_QUEUE);
		jmsQueueReader.start();
	}

	@After
	public void tearDown() throws Exception {
		if (jmsQueueReader.isRunning()) {
			jmsQueueReader.stop();
		}
		jmsQueueReader.close();
		jmsQueueReader = null;

		clearJmsQueue();
	}

	private void clearJmsQueue() throws JMSException {
		// the easiest way to clear the JMS queue is to destroy it using ActiveMq specific API
		QueueConnectionFactory connectionFactory =
				(QueueConnectionFactory) eventService.getEventConnectorService().createConnectionFactory(uri);
		QueueConnection connection = connectionFactory.createQueueConnection();
		((ActiveMQConnection) connection).destroyDestination(new ActiveMQQueue(EventConstants.SUBMISSION_QUEUE));
	}

	@Test
	public void testSubmitBean() throws Exception {
		StatusBean statusBean = new StatusBean("fred");
		submitter.submit(statusBean);

		verify(mockConsumer, timeout(100)).submit(statusBean);
		verifyNoMoreInteractions(mockConsumer);
	}

	@Test
	public void testSubmitMultiple() throws Exception {
		final int numBeans = 5;
		final List<StatusBean> beans = new ArrayList<>();
		for (int i = 1; i <= numBeans; i++) {
			StatusBean bean = new StatusBean("bean" + i);
			beans.add(bean);
			submitter.submit(bean);
		}

		for (StatusBean bean : beans) {
			verify(mockConsumer, timeout(100)).submit(bean);
		}
		verifyNoMoreInteractions(mockConsumer);
	}

	@Test
	public void testStop() throws Exception {
		StatusBean statusBean = new StatusBean("fred");
		submitter.submit(statusBean);

		verify(mockConsumer, timeout(100)).submit(statusBean);

		jmsQueueReader.stop();
		Thread.sleep(500);

		StatusBean never = new StatusBean("never");
		submitter.submit(never);

		verify(mockConsumer, after(100).never()).submit(never);
	}

}
