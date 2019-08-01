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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.JobQueueProxy;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Test for controlling the consumer. The consumer can be
 * controlled directly if it is in the same VM by calling methods such as
 * {@link IJobQueue#pause()} and {@link IJobQueue#resume()} or from another
 * VM by sending {@link QueueCommandBean}s on the command topic.
 * {@link ConsumerProxyControlTest} overrides to
 * control the consumer using a {@link JobQueueProxy} which sends {@link QueueCommandBean}s
 * to the consumer.
 */
public class QueueControlTest extends AbstractJobQueueTest {

	protected IJobQueue<StatusBean> getConsumer() {
		return jobQueue;
	}

	protected void doPauseQueue() throws Exception {
		getConsumer().pause();
	}

	protected void doResumeQueue() throws Exception {
		getConsumer().resume();
	}

	protected void doStopConsumer() throws Exception {
		getConsumer().stop();
	}

	private void verifyJobsBefore(InOrder inOrder, List<StatusBean> beans, List<IBeanProcess<StatusBean>> processes,
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
			List<IBeanProcess<StatusBean>> processes, final int waitingProcessNum) throws EventException, InterruptedException {
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
		List<StatusBean> beans = createAndSubmitBeans();

		CountDownLatch latch = new CountDownLatch(beans.size() - 1);
		WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>(null);
		List<IBeanProcess<StatusBean>> processes = setupMockProcesses(beans, latch, waitingProcessNum, waitingAnswer);
		startJobQueue();

		// the waiting answer of the third job will be blocked at this point, waiting to resume
		// this allows us to pause the queue without a race condition
		waitingAnswer.waitUntilCalled();
		doPauseQueue(); // pause the job queue's consumer thread
		assertThat(jobQueue.isActive(), is(true)); // The consumer thread is still running the blocking method process.start, so it's not actually paused yet
		assertThat(jobQueue.getQueueStatus(), is(QueueStatus.PAUSED)); // but the awaitPause flag is set

		// allow the process to finish and wait for it to finish
		waitingAnswer.resume(); // resumes the current process, once finished the consumer thread should pause

		Thread.sleep(getMockProcessTime() * (processes.size() + 1 - waitingProcessNum));
		assertThat(jobQueue.isActive(), is(false));
		assertThat(jobQueue.getQueueStatus(), is(QueueStatus.PAUSED));

		// verify that the first three jobs were run, but no more
		InOrder inOrder = inOrder(runner, processes, queueStatusListener);
		inOrder.verify(queueStatusListener).queueStatusChanged(QueueStatus.RUNNING);
		verifyJobsBefore(inOrder, beans, processes, waitingProcessNum);
		inOrder.verify(queueStatusListener).queueStatusChanged(QueueStatus.PAUSED);
		verifyNoMoreInteractions(runner, queueStatusListener);

		doResumeQueue();

		boolean processesCompleted = latch.await(2, TimeUnit.SECONDS); // wait for the processes to finish
		assertThat(processesCompleted, is(true));
		inOrder.verify(queueStatusListener).queueStatusChanged(QueueStatus.RUNNING);
		verifyJobsAfter(inOrder, beans, processes, waitingProcessNum);
	}

	@Test
	public void testStop() throws Exception {
		final int waitingProcessNum = 2;
		List<StatusBean> beans = createAndSubmitBeans();

		CountDownLatch latch = new CountDownLatch(beans.size() - 1);
		WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>(null);
		List<IBeanProcess<StatusBean>> processes = setupMockProcesses(beans, latch, waitingProcessNum, waitingAnswer);
		startJobQueue();

		// the waiting answer of the third job will be blocked at this point, waiting to resume
		// this allows us to pause the consumer without a race condition
		waitingAnswer.waitUntilCalled();
		doStopConsumer();
		waitingAnswer.resume(); // allow the process to finish

		assertThat(jobQueue.isActive(), is(false));
		verify(processes.get(waitingProcessNum)).terminate();

		boolean processCompleted = latch.await(getMockProcessTime() * (processes.size() - waitingProcessNum), TimeUnit.MILLISECONDS);
		assertThat(processCompleted, is(false)); // we shouldn't have run the last couple of processes

		InOrder inOrder = inOrder(runner, processes, queueStatusListener);
		inOrder.verify(queueStatusListener).queueStatusChanged(QueueStatus.RUNNING);
		verifyJobsBefore(inOrder, beans, processes, waitingProcessNum);
		inOrder.verify(queueStatusListener).queueStatusChanged(QueueStatus.STOPPED);
		verifyNoMoreInteractions(runner, queueStatusListener);
	}

	private InOrder inOrder(Object... objs) {
		return Mockito.inOrder(Arrays.stream(objs).flatMap(
				obj -> (obj instanceof Collection ? ((Collection<?>) obj).stream() : Stream.of(obj))).toArray());
	}

}
