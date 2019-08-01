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
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.jms.MessageConsumer;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IJobQueue.IQueueStatusListener;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.queue.QueueStatusBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.JobQueueImpl;
import org.eclipse.scanning.event.ScanningEventsClassRegistry;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * Abstract superclass for new mockito-based unit tests for {@link JobQueueImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractJobQueueTest {

	protected static final long DEFAULT_MOCK_PROCESS_TIME_MS = 100;

	protected static URI uri;

	protected IJobQueue<StatusBean> jobQueue;

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
	protected IPublisher<QueueStatusBean> queueStatusTopicPublisher;

	@Mock
	protected ISubscriber<IBeanListener<StatusBean>> statusTopicSubscriber;

	@Mock
	protected ISubmitter<StatusBean> statusSetSubmitter;

	@Mock
	protected ISubscriber<IBeanListener<QueueCommandBean>> commandTopicSubscriber;

	@Mock
	protected IQueueStatusListener queueStatusListener;

	@Mock
	protected IPublisher<QueueCommandBean> commandAckTopicPublisher;

	private IMarshallerService marshallerService;

	private IBeanListener<StatusBean> statusTopicListener;

	private long mockProcessTime = DEFAULT_MOCK_PROCESS_TIME_MS;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		uri = new URI("http://fakeUri"); // Not used as we mock the connection layer

		when(eventService.createPublisher(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) statusTopicPublisher);
		when(eventService.createPublisher(uri, EventConstants.QUEUE_STATUS_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) queueStatusTopicPublisher);
		when(eventService.createSubscriber(uri, EventConstants.CMD_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) commandTopicSubscriber);
		when(eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) statusTopicSubscriber);
		when(eventService.createPublisher(uri, EventConstants.ACK_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) commandAckTopicPublisher);

		Path file = Files.createTempDirectory("temp");
		file.toFile().deleteOnExit();
		when(eventConnectorService.getPersistenceDir()).thenReturn(file.toString());

		// pass calls to eventService.marshal/unmarshall to a real marshaller service instance
		marshallerService = new MarshallerService(new ScanningAPIClassRegistry(),
				new ScanningEventsClassRegistry());
		when(eventConnectorService.marshal(any(Object.class))).thenAnswer(
				invocation -> marshallerService.marshal(invocation.getArgumentAt(0, Object.class)));
		when(eventConnectorService.unmarshal(any(String.class), any(Class.class))).thenAnswer(
				invocation -> marshallerService.unmarshal(invocation.getArgumentAt(0, String.class),
						invocation.getArgumentAt(1, Class.class)));

		createJobQueue();
	}

	protected void createJobQueue() throws EventException {
		jobQueue = new JobQueueImpl<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.STATUS_TOPIC, EventConstants.QUEUE_STATUS_TOPIC,
				EventConstants.CMD_TOPIC,
				EventConstants.ACK_TOPIC, eventConnectorService, eventService);
		jobQueue.setName("Test Consumer");
		jobQueue.setBeanClass(StatusBean.class);
		jobQueue.addQueueStatusListener(queueStatusListener);
		jobQueue.setRunner(runner);
		assertThat(jobQueue.getRunner(), is(runner));

		if (statusTopicListener == null) {
			// only capture the status listener of the first consumer
			ArgumentCaptor<IBeanListener<StatusBean>> statusTopicListenerCaptor =
					(ArgumentCaptor<IBeanListener<StatusBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
			verify(statusTopicSubscriber).addListener(statusTopicListenerCaptor.capture());
			statusTopicListener = statusTopicListenerCaptor.getValue();
			assertThat(statusTopicListener, Matchers.is(notNullValue()));
		}
	}

	@After
	public void tearDown() throws Exception {
		if (jobQueue.isActive()) {
			jobQueue.stop();
		}

		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();

		jobQueue.close();

		jobQueue = null;
	}

	@SuppressWarnings("unchecked")
	protected void startJobQueue() throws Exception {
		// configure the event service to create the status topic subscriber, this is only done when the consumer starts
		when(eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) statusTopicSubscriber);

		assertThat(jobQueue.isActive(), is(false));
		assertThat(jobQueue.getQueueStatus(), is(QueueStatus.STOPPED));
		jobQueue.start();
		jobQueue.awaitStart();
	}

	protected List<StatusBean> createAndSubmitBeans() throws Exception {
		return createAndSubmitBeans("one", "two", "three", "four", "five");
	}

	protected List<StatusBean> createAndSubmitBeans(String... names) throws Exception {
		final List<StatusBean> beans = Arrays.stream(names).map(StatusBean::new).collect(toList());
		for (StatusBean bean : beans) {
			jobQueue.submit(bean);
		}

		return beans;
	}

	protected List<IBeanProcess<StatusBean>> setupMockProcesses(
			List<StatusBean> beans, CountDownLatch latch) throws Exception {
		return setupMockProcesses(beans, latch, -1, null);
	}

	protected class DummyRunProcessAnswer implements Answer<Void> {

		private final StatusBean bean;
		private final CountDownLatch latch;
		private final long processTime;

		public DummyRunProcessAnswer(StatusBean bean, CountDownLatch latch, long processTime) {
			this.bean = bean;
			this.latch = latch;
			this.processTime = processTime;
		}

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			updateBeanStatus(bean, Status.RUNNING);
			Thread.sleep(processTime);
			updateBeanStatus(bean, Status.COMPLETE);
			latch.countDown();
			return null;
		}

	}

	private void updateBeanStatus(StatusBean bean, Status newStatus) {
		bean.setStatus(newStatus);
		statusTopicListener.beanChangePerformed(new BeanEvent<>(bean));
	}

	protected long getMockProcessTime() {
		return mockProcessTime;
	}

	protected void setMockProcessTime(long mockProcessTime) {
		this.mockProcessTime = mockProcessTime;
	}

	protected List<IBeanProcess<StatusBean>> setupMockProcesses(
			List<StatusBean> beans, CountDownLatch latch, int waitingProcessNum,
				WaitingAnswer<Void> waitingAnswer) throws Exception {
		final List<IBeanProcess<StatusBean>> mockProcesses = new ArrayList<>(beans.size());
		for (int i = 0; i < beans.size(); i++) {
			StatusBean bean = beans.get(i);
			@SuppressWarnings("unchecked")
			IBeanProcess<StatusBean> process = mock(IBeanProcess.class);
			when(process.getBean()).thenReturn(bean);
			mockProcesses.add(process);
			when(runner.createProcess(bean, statusTopicPublisher)).thenReturn(process);

			Answer<Void> answer = i == waitingProcessNum && waitingAnswer != null ?
					waitingAnswer : new DummyRunProcessAnswer(bean, latch, mockProcessTime);
			doAnswer(answer).when(process).start();
		}

		return mockProcesses;
	}

}
