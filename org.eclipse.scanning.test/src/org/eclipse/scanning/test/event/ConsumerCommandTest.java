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

import static org.eclipse.scanning.test.util.LambdaUtils.wrap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerImpl;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerCommandTest {

	private static final long MOCK_PROCESS_TIME_MS = 100;

	private static URI uri;

	protected IConsumer<StatusBean>  consumer;

	@Mock
	private IEventConnectorService eventConnectorService;

	@Mock
	private IEventService eventService;

	@Mock
	private MessageConsumer messageConsumer;

	@Mock
	private IProcessCreator<StatusBean> runner;

	@Mock
	private IPublisher<StatusBean> statusTopicPublisher;

	@Mock
	private ISubmitter<StatusBean> statusSetSubmitter;

	@SuppressWarnings("unchecked")
	@Before
	public void startUp() throws Exception {
		uri = new URI("http://fakeUri"); // Not used as we mock the connection layer

		when(eventService.createSubmitter(uri, EventConstants.STATUS_SET)).thenReturn(statusSetSubmitter);

		when(eventService.createPublisher(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) statusTopicPublisher); // TODO why do we need the double cast?

		ISubscriber<IBeanListener<QueueCommandBean>> commandTopicSubscriber = mock(ISubscriber.class);
		when(eventService.createSubscriber(uri, EventConstants.CMD_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) commandTopicSubscriber);

		consumer = new ConsumerImpl<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC,
				/* EventConstants.HEARTBEAT_TOPIC */ null, // not worth testing and would need powermock to mock out the constructor for new HeartbeatBroadcaster
				EventConstants.CMD_TOPIC, eventConnectorService, eventService);
		consumer.setName("Test Consumer");
		consumer.setBeanClass(StatusBean.class);

		runner = mock(IProcessCreator.class);
		consumer.setRunner(runner);
		assertThat(consumer.getRunner(), is(runner));
	}

	@Test
	public void testConsumerInitialized() {
		assertThat(consumer, is(notNullValue()));
		assertThat(consumer.getSubmitQueueName(), is(EventConstants.SUBMISSION_QUEUE));
		assertThat(consumer.getStatusSetName(), is(EventConstants.STATUS_SET));
		assertThat(consumer.getStatusTopicName(), is(EventConstants.STATUS_TOPIC));
		assertThat(consumer.getCommandTopicName(), is(EventConstants.CMD_TOPIC));
		assertThat(consumer.getHeartbeatTopicName(), is(nullValue()));
		assertThat(consumer.getName(), is(equalTo("Test Consumer")));
		assertThat(consumer.getConsumerId(), is(any(UUID.class)));
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.RUNNING));
		assertThat(consumer.isActive(), is(false));
	}

	@SuppressWarnings("unchecked")
	private void startConsumer() throws Exception {
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

	@Test
	public void testStart() throws Exception {
		startConsumer();
		assertThat(consumer.isActive(), is(true));
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.RUNNING));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStartPaused() throws Exception {
		consumer.setPauseOnStart(true);

		// this mock called to reader the consumer and see if it is empty to check if the consumer should start paused
		IQueueReader<StatusBean> submitQueueReader = mock(IQueueReader.class);
		when(eventService.createQueueReader(uri, EventConstants.SUBMISSION_QUEUE)).thenReturn(
				(IQueueReader<Object>) (IQueueReader<?>) submitQueueReader);
		when(submitQueueReader.getQueue()).thenReturn(Arrays.asList(new StatusBean()));

		IPublisher<QueueCommandBean> commandPublisher = mock(IPublisher.class);
		Mockito.<IPublisher<QueueCommandBean>>when(eventService.createPublisher(uri, EventConstants.CMD_TOPIC)).thenReturn(
				commandPublisher);

		startConsumer();

		Thread.sleep(500); // need to wait for the consumer thread to get to the pause (how to do this without a sleep?)

		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.PAUSED));
		assertThat(consumer.isActive(), is(false));

		// check that a pause bean was broadcast
		verify(commandPublisher).setStatusSetName(EventConstants.CMD_SET);
		verify(commandPublisher).setStatusSetAddRequired(true);
		verify(commandPublisher).broadcast(new QueueCommandBean(EventConstants.SUBMISSION_QUEUE, Command.PAUSE));
		verify(commandPublisher).close();
		verifyNoMoreInteractions(commandPublisher);
	}

	private IConsumerProcess<StatusBean> setupMocksForConsumingBean(StatusBean statusBean) throws Exception {
		TextMessage message = mock(TextMessage.class);
		when(messageConsumer.receive(anyLong())).thenReturn(message).thenReturn(null);
		String jsonMessage = "jsonMessage";
		when(message.getText()).thenReturn(jsonMessage);
		when(eventConnectorService.unmarshal(jsonMessage, StatusBean.class)).thenReturn(statusBean);
		IConsumerProcess<StatusBean> process = mock(IConsumerProcess.class);
		when(runner.createProcess(statusBean, statusTopicPublisher)).thenReturn(process);
		return process;
	}

	@Test
	public void testConsumingBean() throws Exception {
		StatusBean statusBean = new StatusBean("bean");
		IConsumerProcess<StatusBean> process = setupMocksForConsumingBean(statusBean);

		startConsumer();

		verify(statusSetSubmitter, timeout(1000)).submit(statusBean);
		verify(runner).createProcess(statusBean, statusTopicPublisher);
		verify(process).start();
	}

	@Test
	public void testConsumeBeanRequestTerminate() throws Exception {
		StatusBean statusBean = new StatusBean("requestTerminate");
		statusBean.setStatus(Status.REQUEST_TERMINATE);
		IConsumerProcess<StatusBean> process = setupMocksForConsumingBean(statusBean);

		startConsumer();

		// verify that the bean's status was set to TERMINATED and it was broadcast on the status topic
		// and that there was no attempt to create or run a process for it
		verify(statusSetSubmitter, timeout(1000)).submit(statusBean);
		assertThat(statusBean.getStatus(), is(Status.TERMINATED));
		verify(statusTopicPublisher).broadcast(statusBean);
		verifyZeroInteractions(runner, process);
	}

	@Test
	public void testConsumeBeanFails() throws Exception {
		StatusBean statusBean = new StatusBean("bean");
		IConsumerProcess<StatusBean> process = setupMocksForConsumingBean(statusBean);
		EventException e = new EventException("Could not run bean");
		doThrow(e).when(process).start();

		startConsumer();

		// verify that the consumer created and started a process for the bean
		verify(statusSetSubmitter, timeout(1000)).submit(statusBean);
		verify(runner).createProcess(statusBean, statusTopicPublisher);
		verify(process).start();
		// verify that the bean's status was set to FAILED and was broadcast on the status topic
		assertThat(statusBean.getStatus(), is(Status.FAILED));
		assertThat(statusBean.getMessage(), is(e.getMessage()));
		verify(statusTopicPublisher).broadcast(statusBean);
	}

	private List<StatusBean> setupBeans() throws Exception {
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

	private List<IConsumerProcess<StatusBean>> setupMockProcesses(
			List<StatusBean> beans, CountDownLatch latch) throws Exception {
		return setupMockProcesses(beans, latch, -1, null);
	}

	private List<IConsumerProcess<StatusBean>> setupMockProcesses(
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

	@Test
	public void testConsumingMultipleBeans() throws Exception {
		List<StatusBean> beans = setupBeans();
		CountDownLatch latch = new CountDownLatch(beans.size());
		List<IConsumerProcess<StatusBean>> processes = setupMockProcesses(beans, latch);
		startConsumer();

		boolean reachedZero = latch.await(1, TimeUnit.SECONDS); // reached zero i.e.
		assertThat(reachedZero, is(true));

		beans.forEach(wrap(bean -> verify(runner).createProcess(bean, statusTopicPublisher)));
		processes.forEach(wrap(process -> verify(process).start()));
	}

	@Test
	public void testPauseResume() throws Exception {
		final int waitingProcessNum = 2;
		List<StatusBean> beans = setupBeans();

		CountDownLatch latch = new CountDownLatch(beans.size() - 1);
		WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>(null);
		List<IConsumerProcess<StatusBean>> processes = setupMockProcesses(beans, latch, waitingProcessNum, waitingAnswer);
		startConsumer();

		// the waiting answer of the third job will be blocked at this point, waiting to resume
		// this allows us to pause the consumer without a race condition
		waitingAnswer.waitUntilCalled();
		consumer.pause(); // pause the consumer
		assertThat(consumer.isActive(), is(true)); // The consumer is still running the blocking method process.start, so it's not actually paused yet
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.PAUSED)); // but the awaitPause flag is set

		// allow the process to finish and wait for it to finish
		waitingAnswer.resume(); // resumes the current process, once finished the consumer should pause

		// verify that the first three jobs were run, but no more
		Thread.sleep(MOCK_PROCESS_TIME_MS * (processes.size() + 1 - waitingProcessNum));
		assertThat(consumer.isActive(), is(false));
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.PAUSED));
		for (int i = 0; i < beans.size(); i++) {
			if (i <= waitingProcessNum) {
				verify(runner).createProcess(beans.get(i), statusTopicPublisher);
				verify(processes.get(i)).start();
			} else {
				verifyZeroInteractions(processes.get(i));
			}
		}
		verifyNoMoreInteractions(runner);

		consumer.resume();

		boolean processesCompleted = latch.await(MOCK_PROCESS_TIME_MS * 20, TimeUnit.MILLISECONDS); // wait for the processes to finish
		assertThat(processesCompleted, is(true));

		for (int i = waitingProcessNum + 1; i < beans.size(); i++) {
			verify(runner).createProcess(beans.get(i), statusTopicPublisher);
			verify(processes.get(i)).start();
		}
	}

	@Test
	public void testStop() throws Exception {
		final int waitingProcessNum = 2;
		List<StatusBean> beans = setupBeans();

		CountDownLatch latch = new CountDownLatch(beans.size() - 1);
		WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>(null);
		List<IConsumerProcess<StatusBean>> processes = setupMockProcesses(beans, latch, waitingProcessNum, waitingAnswer);
		startConsumer();

		// the waiting answer of the third job will be blocked at this point, waiting to resume
		// this allows us to pause the consumer without a race condition
		waitingAnswer.waitUntilCalled();
		consumer.stop();
		waitingAnswer.resume(); // allow the process to finish

		assertThat(consumer.isActive(), is(false));
		verify(processes.get(waitingProcessNum)).terminate();

		boolean processCompleted = latch.await(MOCK_PROCESS_TIME_MS * (processes.size() - waitingProcessNum), TimeUnit.MILLISECONDS);
		assertThat(processCompleted, is(false)); // we shouldn't have run the last couple of processes
		for (int i = waitingProcessNum + 1; i < processes.size(); i++) {
			verifyZeroInteractions(processes.get(i));
		}
	}

	@Test
	public void testRestart() throws Exception {
		final int waitingProcessNum = 2;
		List<StatusBean> beans = setupBeans();

		CountDownLatch latch = new CountDownLatch(beans.size() - 1);
		WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>(null);
		List<IConsumerProcess<StatusBean>> processes = setupMockProcesses(beans, latch, waitingProcessNum, waitingAnswer);
		startConsumer();

		// the waiting answer of the third job will be blocked at this point, waiting to resume
		// this allows us to pause the consumer without a race condition
		waitingAnswer.waitUntilCalled();
		((ConsumerImpl<StatusBean>) consumer).restart(); // pause the consumer

		// TODO: what to assert here?
		verify(processes.get(waitingProcessNum)).terminate();

		boolean processesCompleted = latch.await(MOCK_PROCESS_TIME_MS * 20, TimeUnit.MILLISECONDS); // wait for the processes to finish
		assertThat(processesCompleted, is(true));
		beans.forEach(wrap(bean -> verify(runner).createProcess(bean, statusTopicPublisher)));
		processes.forEach(wrap(process -> verify(process).start()));
	}

	// TODO: should we test for sending beans as well?
	// Test for receiving command beans


}
