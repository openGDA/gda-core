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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.junit.Test;
import org.mockito.Mockito;

public class SimpleConsumerTest extends AbstractNewConsumerTest {

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

		// verify the connections made in the connect() method
		verify(eventService).createSubmitter(uri, EventConstants.STATUS_SET);
		verify(eventService).createPublisher(uri, EventConstants.STATUS_TOPIC);
		verify(eventService).createSubscriber(uri, EventConstants.CMD_TOPIC);
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
		verify(statusTopicPublisher, timeout(1000)).broadcast(statusBean);
		assertThat(statusBean.getStatus(), is(Status.FAILED));
		assertThat(statusBean.getMessage(), is(e.getMessage()));
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

}
