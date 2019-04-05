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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.jms.MessageConsumer;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.consumer.ConsumerStatus;
import org.eclipse.scanning.api.event.consumer.ConsumerStatusBean;
import org.eclipse.scanning.api.event.consumer.QueueCommandBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumer.IConsumerStatusListener;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerImpl;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.After;
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
	protected IPublisher<ConsumerStatusBean> consumerStatusTopicPublisher;

	@Mock
	protected ISubscriber<IBeanListener<StatusBean>> statusTopicSubscriber;

	@Mock
	protected ISubmitter<StatusBean> statusSetSubmitter;

	@Mock
	protected ISubscriber<IBeanListener<QueueCommandBean>> commandTopicSubscriber;

	@Mock
	protected IConsumerStatusListener consumerStatusListener;

	@Mock
	protected IPublisher<QueueCommandBean> commandAckTopicPublisher;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		uri = new URI("http://fakeUri"); // Not used as we mock the connection layer

		when(eventService.createSubmitter(uri, EventConstants.STATUS_SET)).thenReturn(statusSetSubmitter);
		when(eventService.createPublisher(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) statusTopicPublisher);
		when(eventService.createPublisher(uri, EventConstants.CONSUMER_STATUS_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) consumerStatusTopicPublisher);
		when(eventService.createSubscriber(uri, EventConstants.CMD_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) commandTopicSubscriber);
		when(eventService.createPublisher(uri, EventConstants.ACK_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) commandAckTopicPublisher);

		consumer = new ConsumerImpl<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC,
				EventConstants.CONSUMER_STATUS_TOPIC,
				EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC, eventConnectorService, eventService);
		consumer.setName("Test Consumer");
		consumer.setBeanClass(StatusBean.class);
		consumer.addConsumerStatusListener(consumerStatusListener);

		// verify the methods
		runner = mock(IProcessCreator.class);
		consumer.setRunner(runner);
		assertThat(consumer.getRunner(), is(runner));
	}

	@After
	public void tearDown() throws Exception {
		if (consumer.isActive()) {
			consumer.stop();
		}
		consumer = null;
	}

	@SuppressWarnings("unchecked")
	protected void startConsumer() throws Exception {
		// configure the event service to create the status topic subscriber, this is only done when the consumer starts
		when(eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) statusTopicSubscriber);

		assertThat(consumer.isActive(), is(false));
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.STOPPED));
		consumer.start();
		consumer.awaitStart();
	}

	protected List<StatusBean> createAndSubmitBeans() throws Exception {
		return createAndSubmitBeans("one", "two", "three", "four", "five");
	}

	protected List<StatusBean> createAndSubmitBeans(String... names) throws Exception {
		final List<StatusBean> beans = Arrays.stream(names).map(StatusBean::new).collect(toList());
		for (StatusBean bean : beans) {
			consumer.submit(bean);
		}

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
			@SuppressWarnings("unchecked")
			IConsumerProcess<StatusBean> process = mock(IConsumerProcess.class);
			when(process.getBean()).thenReturn(bean);
			mockProcesses.add(process);
			when(runner.createProcess(bean, statusTopicPublisher)).thenReturn(process);

			Answer<Void> answer = i == waitingProcessNum && waitingAnswer != null ?
					waitingAnswer : processStartAnswer;
			doAnswer(answer).when(process).start();
		}

		return mockProcesses;
	}

}
