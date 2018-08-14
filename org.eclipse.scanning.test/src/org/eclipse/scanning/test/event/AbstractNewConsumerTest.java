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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerImpl;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * Abstract superclass for new mockito-based unit tests for {@link ConsumerImpl}.
 * This class can be renamed to AbstractConsumerTest if/when we delete the plug-in tests,
 * including {@link ConsumerPluginTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractNewConsumerTest {

	protected static final long MOCK_PROCESS_TIME_MS = 100;

	protected static URI uri;

	protected IConsumer<StatusBean>  consumer;

	@Mock
	protected IEventConnectorService eventConnectorService;

	@Mock
	protected IEventService eventService;

	@Mock
	protected MessageConsumer messageConsumer;

	@Mock
	protected IProcessCreator<StatusBean> runner;

	@Mock
	protected IPublisher<StatusBean> statusTopicPublisher;

	@Mock
	protected ISubmitter<StatusBean> statusSetSubmitter;

	@Mock
	protected ISubscriber<IBeanListener<QueueCommandBean>> commandTopicSubscriber;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		uri = new URI("http://fakeUri"); // Not used as we mock the connection layer

		when(eventService.createSubmitter(uri, EventConstants.STATUS_SET)).thenReturn(statusSetSubmitter);

		when(eventService.createPublisher(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) statusTopicPublisher); // TODO why do we need the double cast?

		when(eventService.createSubscriber(uri, EventConstants.CMD_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) commandTopicSubscriber);

		consumer = new ConsumerImpl<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC,
				/* EventConstants.HEARTBEAT_TOPIC */ null, // not worth testing and would need powermock to mock out the constructor for new HeartbeatBroadcaster
				EventConstants.CMD_TOPIC, eventConnectorService, eventService);
		consumer.setName("Test Consumer");
		consumer.setBeanClass(StatusBean.class);

		// verify the methods

		runner = mock(IProcessCreator.class);
		consumer.setRunner(runner);
		assertThat(consumer.getRunner(), is(runner));
	}

	@SuppressWarnings("unchecked")
	protected void startConsumer() throws Exception {
		// configure the event service to create the status topic subscriber, this is only done when the consumer starts
		ISubscriber<IBeanListener<StatusBean>> statusTopicSubscriber = mock(ISubscriber.class);
		when(eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) statusTopicSubscriber);

		// set up the mocks required to create a MessageConsumer, this is enough to get the main thread executing the main event loop without throwing
		QueueConnectionFactory queueConnectionFactory = mock(QueueConnectionFactory.class);
		when(eventConnectorService.createConnectionFactory(uri)).thenReturn(queueConnectionFactory);
		QueueConnection connection = mock(QueueConnection.class);
		when(queueConnectionFactory.createQueueConnection()).thenReturn(connection);
		QueueSession session = mock(QueueSession.class); // TODO: createMessageConsumer creates two sessions, one by calling getSession and one in createQueue, fix this
		when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
		QueueSession queueSession = mock(QueueSession.class);
		when(connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(queueSession);
		Queue queue = mock(Queue.class);
		when(queueSession.createQueue(EventConstants.SUBMISSION_QUEUE)).thenReturn(queue);
		when(session.createConsumer(queue)).thenReturn(messageConsumer);

		assertThat(consumer.isActive(), is(false));
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.RUNNING)); // TODO should not be running yet, see DAQ-1615
		consumer.start();
		consumer.awaitStart();
	}

	protected List<StatusBean> setupBeans() throws Exception {
		List<String> names = Arrays.asList("one", "two", "three", "four", "five");
		List<StatusBean> beans = names.stream().map(StatusBean::new).collect(Collectors.toList());
		TextMessage message = mock(TextMessage.class);
		when(messageConsumer.receive(anyLong())).thenReturn(message);
		String jsonMessage = "jsonMessage";
		when(message.getText()).thenReturn(jsonMessage);
		when(eventConnectorService.unmarshal(jsonMessage, StatusBean.class)).thenReturn(
				beans.get(0), beans.subList(1, beans.size()).toArray(new StatusBean[beans.size() - 1]));
		return beans;
	}

	protected List<IConsumerProcess<StatusBean>> setupMockProcesses(
			List<StatusBean> beans, CountDownLatch latch) throws Exception {
		return setupMockProcesses(beans, latch, -1, null);
	}

	protected List<IConsumerProcess<StatusBean>> setupMockProcesses(
			List<StatusBean> beans, CountDownLatch latch, int waitingProcessNum,
				WaitingAnswer<Void> waitingAnswer) throws Exception {
		final Answer<Void> processStartAnswer = new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Thread.sleep(MOCK_PROCESS_TIME_MS); // each process takes 100ms
				latch.countDown();
				return null;
			}
		};

		final List<IConsumerProcess<StatusBean>> mockProcesses = new ArrayList<>(beans.size());
		for (int i = 0; i < beans.size(); i++) {
			StatusBean bean = beans.get(i);
			IConsumerProcess<StatusBean> process = mock(IConsumerProcess.class);
			mockProcesses.add(process);
			when(runner.createProcess(bean, statusTopicPublisher)).thenReturn(process);

			Answer<Void> answer = i == waitingProcessNum && waitingAnswer != null ?
					waitingAnswer : processStartAnswer;
			doAnswer(answer).when(process).start();
		}

		return mockProcesses;
	}



}
