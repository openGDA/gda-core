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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerProxy;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests controlling a consumer using {@link ConsumerProxy}.
 */
public class ConsumerProxyControlTest extends AbstractConsumerControlTest {

	private IConsumer<StatusBean> consumerProxy;

	private IBeanListener<QueueCommandBean> commandTopicListener;

	@Mock
	private IRequester<QueueCommandBean> commandRequester;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		ArgumentCaptor<IBeanListener<QueueCommandBean>> commandTopicSubscriberCaptor =
				(ArgumentCaptor<IBeanListener<QueueCommandBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
		verify(commandTopicSubscriber).addListener(commandTopicSubscriberCaptor.capture());
		commandTopicListener = commandTopicSubscriberCaptor.getValue();

		consumerProxy = new ConsumerProxy<>(uri, EventConstants.SUBMISSION_QUEUE,
				EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC, eventConnectorService, eventService);

		// mock out the requester and
		when(eventService.createRequestor(uri, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC)).thenReturn(
				(IRequester<IdBean>) (IRequester<?>) commandRequester);
		when(commandRequester.post(Mockito.any())).thenAnswer(new Answer<QueueCommandBean>() {
			@Override
			public QueueCommandBean answer(InvocationOnMock invocation) throws Throwable {
				final QueueCommandBean commandBean = invocation.getArgumentAt(0, QueueCommandBean.class);
				commandTopicListener.beanChangePerformed(new BeanEvent<>(commandBean));
				return commandBean;
			}
		});
	}

	@Override
	protected void doPauseConsumer() throws Exception {
		consumerProxy.pause();
	}

	@Override
	protected void doResumeConsumer() throws Exception {
		consumerProxy.resume();
	}

	@Override
	protected void doStopConsumer() throws Exception {
		consumerProxy.stop();
	}

	@Override
	protected void doRestartConsumer() throws Exception {
		consumerProxy.restart();
	}

	@Override
	protected void doClearQueue(boolean completed) throws Exception {
		if (completed) {
			consumer.clearRunningAndCompleted();
		} else {
			consumer.clearQueue();
		}
	}

}
