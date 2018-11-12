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
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumer.IConsumerStatusListener;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerProxy;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests controlling a consumer using {@link ConsumerProxy}.
 */
public class ConsumerProxyControlTest extends ConsumerControlTest {

	private IConsumer<StatusBean> consumerProxy;

	private IBeanListener<QueueCommandBean> commandTopicListener;

	@Mock
	private IRequester<QueueCommandBean> commandRequester;

	@Override
	protected IConsumer<StatusBean> getConsumer() {
		return consumerProxy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setUp() throws Exception {
		// set up the mock heartbeat subscriber used by the consumer proxy
		ISubscriber<IHeartbeatListener> heartbeatSubscriber = mock(ISubscriber.class);
		when(eventService.createSubscriber(uri, EventConstants.HEARTBEAT_TOPIC)).thenReturn(
				(ISubscriber<EventListener>) (ISubscriber<?>) heartbeatSubscriber);

		// setup the mock command requester used by the consumer proxy to invoke the consumers topic listener directly
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

		// create the consumer proxy
		consumerProxy = new ConsumerProxy<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC, eventConnectorService, eventService);

		// capture the consumer proxy's listener to consumer status events
		verify(eventService).createSubscriber(uri, EventConstants.HEARTBEAT_TOPIC);
		ArgumentCaptor<IHeartbeatListener> heartbeatListenerCaptor =
				(ArgumentCaptor<IHeartbeatListener>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IHeartbeatListener.class);
		verify(heartbeatSubscriber).addListener(heartbeatListenerCaptor.capture());
		IHeartbeatListener heartbeatListener = heartbeatListenerCaptor.getValue();

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				HeartbeatBean bean = invocation.getArgumentAt(0, HeartbeatBean.class);
				heartbeatListener.heartbeatPerformed(new HeartbeatEvent(bean));
				return null;
			}
		}).when(heartbeatTopicPublisher).broadcast(any(HeartbeatBean.class));

		super.setUp();

		// Overwrite the status listener in the superclass with one added to the consumer proxy
		// so that we can verify it is called at the appropriate times
		consumerStatusListener = mock(IConsumerStatusListener.class);
		consumerProxy.addConsumerStatusListener(consumerStatusListener);

		// capture the consumer's listener to the command topic
		ArgumentCaptor<IBeanListener<QueueCommandBean>> commandTopicSubscriberCaptor =
				(ArgumentCaptor<IBeanListener<QueueCommandBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
		verify(commandTopicSubscriber).addListener(commandTopicSubscriberCaptor.capture());
		commandTopicListener = commandTopicSubscriberCaptor.getValue();
	}

}
