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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EventListener;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IJobQueue.IQueueStatusListener;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queue.IQueueStatusBeanListener;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueStatusBean;
import org.eclipse.scanning.api.event.queue.QueueStatusBeanEvent;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.JobQueueProxy;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests controlling a queue using {@link JobQueueProxy}.
 */
public class ConsumerProxyControlTest extends QueueControlTest {

	private IJobQueue<StatusBean> jobQueueProxy;

	private IBeanListener<QueueCommandBean> commandTopicListener;

	@Mock
	private IRequester<QueueCommandBean> commandRequester;

	@Override
	protected IJobQueue<StatusBean> getConsumer() {
		return jobQueueProxy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setUp() throws Exception {
		// set up the mock queue status topic subscriber used by the consumer proxy
		ISubscriber<IQueueStatusBeanListener> queueStatusTopicSubscriber = mock(ISubscriber.class);
		when(eventService.createSubscriber(uri, EventConstants.QUEUE_STATUS_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) queueStatusTopicSubscriber);

		// setup the mock command requester used by the job queue proxy to invoke the consumers topic listener directly
		// with the bean passed to it (this would normally happen over JMS, which this test mocks out)
		when(eventService.createRequestor(uri, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC)).thenReturn(
				(IRequester<IdBean>) (IRequester<?>) commandRequester);
		when(commandRequester.post(any())).thenAnswer(new Answer<QueueCommandBean>() {
			@Override
			public QueueCommandBean answer(InvocationOnMock invocation) throws Throwable {
				final QueueCommandBean commandBean = invocation.getArgumentAt(0, QueueCommandBean.class);
				commandTopicListener.beanChangePerformed(new BeanEvent<>(commandBean));
				return commandBean;
			}
		});

		// create the job queue proxy
		jobQueueProxy = new JobQueueProxy<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC, eventConnectorService, eventService);

		// capture the job queue proxy's listener to queue status events
		verify(eventService).createSubscriber(uri, EventConstants.QUEUE_STATUS_TOPIC);
		ArgumentCaptor<IQueueStatusBeanListener> queueStatusBeanListenerCaptor =
				(ArgumentCaptor<IQueueStatusBeanListener>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IQueueStatusBeanListener.class);
		verify(queueStatusTopicSubscriber).addListener(queueStatusBeanListenerCaptor.capture());
		IQueueStatusBeanListener heartbeatListener = queueStatusBeanListenerCaptor.getValue();

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				QueueStatusBean bean = invocation.getArgumentAt(0, QueueStatusBean.class);
				heartbeatListener.queueStatusChanged(new QueueStatusBeanEvent(bean));
				return null;
			}
		}).when(queueStatusTopicPublisher).broadcast(any(QueueStatusBean.class));

		super.setUp();

		// Overwrite the status listener in the superclass with one added to the job queue proxy
		// so that we can verify it is called at the appropriate times
		queueStatusListener = mock(IQueueStatusListener.class);
		jobQueueProxy.addQueueStatusListener(queueStatusListener);

		// capture the consumer's listener to the command topic
		ArgumentCaptor<IBeanListener<QueueCommandBean>> commandTopicSubscriberCaptor =
				(ArgumentCaptor<IBeanListener<QueueCommandBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
		verify(commandTopicSubscriber).addListener(commandTopicSubscriberCaptor.capture());
		commandTopicListener = commandTopicSubscriberCaptor.getValue();
	}

}
