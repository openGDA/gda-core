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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ConsumerProxy;
import org.eclipse.scanning.event.EventTimingsHelper;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Test for controlling the consumer. The consumer can be
 * controlled directly if it is in the same VM by calling methods such as
 * {@link IConsumer#pause()} and {@link IConsumer#resume()} or from another
 * VM by sending {@link QueueCommandBean}s on the command topic.
 * {@link ConsumerProxyControlTest} overrides to
 * control the consumer using a {@link ConsumerProxy} which sends {@link QueueCommandBean}s
 * to the consumer.
 */
public class ConsumerControlTest extends AbstractNewConsumerTest {

	protected IConsumer<StatusBean> getConsumer() {
		return consumer;
	}

	protected void doPauseConsumer() throws Exception {
		getConsumer().pause();
	}

	protected void doResumeConsumer() throws Exception {
		getConsumer().resume();
	}

	protected void doStopConsumer() throws Exception {
		getConsumer().stop();
	}

	protected void doRestartConsumer() throws Exception {
		getConsumer().restart();
	}

	protected void doClearQueue(boolean clearCompleted) throws Exception {
		if (clearCompleted) {
			getConsumer().clearRunningAndCompleted();
		} else {
			getConsumer().clearQueue();
		}
	}

	private void verifyJobsBefore(InOrder inOrder, List<StatusBean> beans, List<IConsumerProcess<StatusBean>> processes,
			final int waitingProcessNum) throws EventException, InterruptedException {
		for (int i = 0; i < beans.size(); i++) {
			if (i <= waitingProcessNum) {
				inOrder.verify(runner).createProcess(beans.get(i), statusTopicPublisher);
				inOrder.verify(processes.get(i)).start();
			} else {
				verifyZeroInteractions(processes.get(i));
			}
		}
	}

	private void verifyJobsAfter(InOrder inOrder, List<StatusBean> beans,
			List<IConsumerProcess<StatusBean>> processes, final int waitingProcessNum) throws EventException, InterruptedException {
		for (int i = waitingProcessNum + 1; i < beans.size(); i++) {
			if (i <= waitingProcessNum) {
				verifyNoMoreInteractions(processes.get(i));
			} else {
				inOrder.verify(runner).createProcess(beans.get(i), statusTopicPublisher);
				inOrder.verify(processes.get(i)).start();
				verifyNoMoreInteractions(processes.get(i));
			}
		}
		verifyNoMoreInteractions(runner);
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
		doPauseConsumer(); // pause the consumer
		assertThat(consumer.isActive(), is(true)); // The consumer is still running the blocking method process.start, so it's not actually paused yet
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.PAUSED)); // but the awaitPause flag is set

		// allow the process to finish and wait for it to finish
		waitingAnswer.resume(); // resumes the current process, once finished the consumer should pause

		Thread.sleep(MOCK_PROCESS_TIME_MS * (processes.size() + 1 - waitingProcessNum));
		assertThat(consumer.isActive(), is(false));
		assertThat(consumer.getConsumerStatus(), is(ConsumerStatus.PAUSED));

		// verify that the first three jobs were run, but no more
		InOrder inOrder = inOrder(runner, processes, consumerStatusListener);
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.RUNNING);
		verifyJobsBefore(inOrder, beans, processes, waitingProcessNum);
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.PAUSED);
		verifyNoMoreInteractions(runner, consumerStatusListener);

		doResumeConsumer();

		boolean processesCompleted = latch.await(2, TimeUnit.SECONDS); // wait for the processes to finish
		assertThat(processesCompleted, is(true));
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.RUNNING);
		verifyJobsAfter(inOrder, beans, processes, waitingProcessNum);
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
		doStopConsumer();
		waitingAnswer.resume(); // allow the process to finish

		assertThat(consumer.isActive(), is(false));
		verify(processes.get(waitingProcessNum)).terminate();

		boolean processCompleted = latch.await(MOCK_PROCESS_TIME_MS * (processes.size() - waitingProcessNum), TimeUnit.MILLISECONDS);
		assertThat(processCompleted, is(false)); // we shouldn't have run the last couple of processes

		InOrder inOrder = inOrder(runner, processes, consumerStatusListener);
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.RUNNING);
		verifyJobsBefore(inOrder, beans, processes, waitingProcessNum);
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.STOPPED);
		verifyNoMoreInteractions(runner, consumerStatusListener);
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

		InOrder inOrder = inOrder(runner, processes, consumerStatusListener);
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.RUNNING);
		verifyJobsBefore(inOrder, beans, processes, waitingProcessNum);

		doRestartConsumer(); // restart the consumer
		inOrder.verify(processes.get(waitingProcessNum)).terminate();
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.STOPPED);
		Thread.sleep(500); // unfortunately, can't use Mockito.timeout as it doesn't work with InOrder
		inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.RUNNING);
		boolean processesCompleted = latch.await(MOCK_PROCESS_TIME_MS * 20, TimeUnit.MILLISECONDS); // wait for the processes to finish
		assertThat(processesCompleted, is(true));
		verifyJobsAfter(inOrder, beans, processes, waitingProcessNum);
		verifyNoMoreInteractions(runner, consumerStatusListener);
	}

	@Test
	public void testClearQueue() throws Exception {
		testClearQueue(false);
	}

	@Test
	public void testClearCompleted() throws Exception {
		testClearQueue(true);
	}

	private InOrder inOrder(Object... objs) {
		return Mockito.inOrder(Arrays.stream(objs).flatMap(
				obj -> (obj instanceof Collection ? ((Collection<?>) obj).stream() : Stream.of(obj))).toArray());
	}

	private void testClearQueue(boolean clearCompleted) throws Exception {
		final String queueName = clearCompleted ? consumer.getStatusSetName() : consumer.getSubmitQueueName();

		// Set up mocks for publishing the pause bean
		// TODO: mattd 2018-08-15: Note it's not necessary to send a pause bean when cleaning the status set,
		// but the existing implementation does so anyway. It's ignored by the consumer as the queue name is
		// not that of its submission queue. I intend to fix this in subsequent commit as part of bringing the
		// queue in memory
		IPublisher<QueueCommandBean> pauseBeanPublisher = mock(IPublisher.class);
		when(eventService.createPublisher(uri, EventConstants.CMD_TOPIC)).thenReturn(
				(IPublisher<Object>) (IPublisher<?>) pauseBeanPublisher); // TODO why do we need a double cast?

		startConsumer();

		// Set up mocks for creating the QueueBrowser
		QueueConnectionFactory connectionFactory = mock(QueueConnectionFactory.class);
		when(eventConnectorService.createConnectionFactory(uri)).thenReturn(connectionFactory);
		QueueConnection connection = mock(QueueConnection.class);
		when(connectionFactory.createQueueConnection()).thenReturn(connection);
		QueueSession session = mock(QueueSession.class);
		when(connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
		Queue queue = mock(Queue.class);
		when(session.createQueue(queueName)).thenReturn(queue);
		QueueBrowser queueBrowser = mock(QueueBrowser.class);
		when(session.createBrowser(queue)).thenReturn(queueBrowser);

		// set up the mocks for the enumeration of the messages and receiving them
		final int numElements = 5;
		final int receiveTimeout = EventTimingsHelper.getReceiveTimeout();
		List<Message> messages = new ArrayList<>(numElements);
		List<MessageConsumer> msgConsumers = new ArrayList<>(numElements);
		for (int i = 0; i < numElements; i++) {
			Message message = mock(Message.class);
			when(message.getJMSMessageID()).thenReturn("msg" + Integer.toString(i + 1));
			messages.add(message);
			MessageConsumer msgConsumer = mock(MessageConsumer.class);
			when(session.createConsumer(queue, "JMSMessageID = 'msg" + (i + 1) + "'")).thenReturn(msgConsumer);
			when(msgConsumer.receive(receiveTimeout)).thenReturn(message);
			msgConsumers.add(msgConsumer);
		}
		when(queueBrowser.getEnumeration()).thenReturn(new Vector<>(messages).elements());

		doClearQueue(clearCompleted);

		InOrder inOrder = inOrder(consumerStatusListener, connection, session, msgConsumers);

		// verify the pause bean was sent if we're clearing the submission queue
		if (!clearCompleted) {
			inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.PAUSED);
		}

		// verify the messages were consumed from the queue
		inOrder.verify(connection).start();
		for (int i = 0; i < numElements; i++) {
			inOrder.verify(session).createConsumer(queue, "JMSMessageID = 'msg" + (i + 1) + "'");
			inOrder.verify(msgConsumers.get(i)).receive(receiveTimeout);
		}

		if (!clearCompleted) {
			inOrder.verify(consumerStatusListener).consumerStatusChanged(ConsumerStatus.RUNNING);
		}
	}

}
