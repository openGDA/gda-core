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

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.mockito.ArgumentCaptor;

/**
 * Tests controlling a consumer using {@link QueueCommandBean}s.
 */
public class ConsumerControlCommandBeanTest extends AbstractConsumerControlTest {

	private IBeanListener<QueueCommandBean> commandTopicListener;

	@SuppressWarnings("unchecked")
	@Override
	public void setUp() throws Exception {
		super.setUp();

		ArgumentCaptor<IBeanListener<QueueCommandBean>> commandTopicSubscriberCaptor =
				(ArgumentCaptor<IBeanListener<QueueCommandBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
		verify(commandTopicSubscriber).addListener(commandTopicSubscriberCaptor.capture());
		commandTopicListener = commandTopicSubscriberCaptor.getValue();
	}

	private void sendCommandBean(Command command) throws EventException {
		final QueueCommandBean commandBean = new QueueCommandBean(consumer.getSubmitQueueName(), command);
		commandTopicListener.beanChangePerformed(new BeanEvent<>(commandBean));
		verify(commandAckTopicPublisher, timeout(250)).broadcast(commandBean);
	}

	@Override
	protected void doPauseConsumer() throws Exception {
		sendCommandBean(Command.PAUSE);
	}

	@Override
	protected void doResumeConsumer() throws Exception {
		sendCommandBean(Command.RESUME);
	}

	@Override
	protected void doStopConsumer() throws Exception {
		sendCommandBean(Command.STOP);
	}

	@Override
	protected void doRestartConsumer() throws Exception {
		sendCommandBean(Command.RESTART);
	}

	@Override
	protected void doClearQueue(boolean clearCompleted) throws Exception {
		Command command = clearCompleted ? Command.CLEAR_COMPLETED : Command.CLEAR;
		sendCommandBean(command);
	}

}
