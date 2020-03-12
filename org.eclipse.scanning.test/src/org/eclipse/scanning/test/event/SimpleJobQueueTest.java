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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doAnswer;
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

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class SimpleJobQueueTest extends AbstractJobQueueTest {

	@Test
	public void testConsumerInitialized() {
		assertThat(jobQueue, is(notNullValue()));
		assertThat(jobQueue.getSubmitQueueName(), is(ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID));
		assertThat(jobQueue.getStatusTopicName(), is(EventConstants.STATUS_TOPIC));
		assertThat(jobQueue.getCommandTopicName(), is(EventConstants.CMD_TOPIC));
		assertThat(jobQueue.getQueueStatusTopicName(), is(EventConstants.QUEUE_STATUS_TOPIC));
		assertThat(jobQueue.getName(), is(equalTo("Test Consumer")));
		assertThat(jobQueue.getJobQueueId(), is(any(UUID.class)));
		assertThat(jobQueue.getQueueStatus(), is(QueueStatus.STOPPED));
		assertThat(jobQueue.isActive(), is(false));

		// verify the connections made in the connect() method
		verify(eventService).createPublisher(uri, EventConstants.STATUS_TOPIC);
		verify(eventService).createSubscriber(uri, EventConstants.CMD_TOPIC);
	}

	@Test
	public void testStart() throws Exception {
		startJobQueue();
		assertThat(jobQueue.isActive(), is(true));
		assertThat(jobQueue.getQueueStatus(), is(QueueStatus.RUNNING));
	}

	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void testStartPaused() throws Exception {
		// TODO: This test is ignored as it would required Powermock to mock the creation of the
		// queue reader. It should be reinstated in a subsequent commit, once the queue has been brought into memory
		// and a new persistence mechanism added

		// this mock called to reader the consumer and see if it is empty to check if the consumer should start paused
//		IQueueReader<StatusBean> submitQueueReader = mock(IQueueReader.class);
//		when(eventService.createQueueReader(uri, EventConstants.SUBMISSION_QUEUE)).thenReturn( // TODO remove
//				(IQueueReader<Object>) (IQueueReader<?>) submitQueueReader);
//		when(submitQueueReader.getQueue()).thenReturn(Arrays.asList(new StatusBean()));

		IPublisher<QueueCommandBean> commandPublisher = mock(IPublisher.class);
		Mockito.<IPublisher<QueueCommandBean>>when(eventService.createPublisher(uri, EventConstants.CMD_TOPIC)).thenReturn(
				commandPublisher);

		startJobQueue();

		Thread.sleep(500); // need to wait for the consumer thread to get to the pause (how to do this without a sleep?)

		assertThat(jobQueue.getQueueStatus(), is(QueueStatus.PAUSED));
		assertThat(jobQueue.isActive(), is(false));

		// check that a pause bean was broadcast
		verify(commandPublisher).broadcast(new QueueCommandBean(EventConstants.SUBMISSION_QUEUE, Command.PAUSE_QUEUE));
		verify(commandPublisher).close();
		verifyNoMoreInteractions(commandPublisher);
	}

	private IBeanProcess<StatusBean> submitBeanAndSetupMockProcess(StatusBean statusBean) throws Exception {
		IBeanProcess<StatusBean> process = mock(IBeanProcess.class);
		when(runner.createProcess(statusBean, statusTopicPublisher)).thenReturn(process);
		when(process.getBean()).thenReturn(statusBean);
		doAnswer(invocation -> { process.getBean().setStatus(Status.COMPLETE); return null; }).when(process).start();

		jobQueue.submit(statusBean);
		return process;
	}

	@Test
	public void testConsumingBean() throws Exception {
		StatusBean statusBean = new StatusBean("bean");
		IBeanProcess<StatusBean> process = submitBeanAndSetupMockProcess(statusBean);

		startJobQueue();

		// verify that the consumer created and started a process for the bean
		verify(runner, timeout(1000)).createProcess(statusBean, statusTopicPublisher);
		verify(process, timeout(1000)).start();

		// check that the bean is in the set of running and completed jobs
		assertThat(jobQueue.getRunningAndCompleted(), contains(statusBean));
	}

	@Test
	public void testConsumeBeanRequestTerminate() throws Exception {
		StatusBean statusBean = new StatusBean("requestTerminate");
		statusBean.setStatus(Status.REQUEST_TERMINATE);
		IBeanProcess<StatusBean> process = submitBeanAndSetupMockProcess(statusBean);

		startJobQueue();

		// verify that the bean's status was set to TERMINATED and it was broadcast on the status topic
		// and that there was no attempt to create or run a process for it
		verify(statusTopicPublisher, timeout(1000)).broadcast(statusBean);
		assertThat(statusBean.getStatus(), is(Status.TERMINATED));
		verifyZeroInteractions(runner, process);

		// check that the bean is in the set of running and completed jobs
		assertThat(jobQueue.getRunningAndCompleted(), contains(statusBean));
	}

	private void testConsumeBeanThrowException(Exception exceptionToThrow, Status expectedStatus) throws Exception {
		StatusBean statusBean = new StatusBean("bean");
		IBeanProcess<StatusBean> process = submitBeanAndSetupMockProcess(statusBean);
		doThrow(exceptionToThrow).when(process).start();

		startJobQueue();

		// verify that the consumer created and started a process for the bean
		verify(runner, timeout(1000)).createProcess(statusBean, statusTopicPublisher);
		verify(process, timeout(1000)).start();

		// verify that the bean's status was set to FAILED and was broadcast on the status topic
		verify(statusTopicPublisher, timeout(1000)).broadcast(statusBean);
		assertThat(statusBean.getStatus(), is(expectedStatus));
		assertThat(statusBean.getMessage(), is(exceptionToThrow.getMessage()));

		assertThat(jobQueue.getRunningAndCompleted(), contains(statusBean));
	}

	@Test
	public void testConsumeBeanFailed() throws Exception {
		testConsumeBeanThrowException(new EventException("Could not run bean"), Status.FAILED);
	}

	@Test
	public void testConsumeBeanAborted() throws Exception {
		testConsumeBeanThrowException(new InterruptedException("User abort"), Status.TERMINATED);
	}

	@Test
	public void testConsumingMultipleBeans() throws Exception {
		List<StatusBean> beans = createAndSubmitBeans();
		CountDownLatch latch = new CountDownLatch(beans.size());
		List<IBeanProcess<StatusBean>> processes = setupMockProcesses(beans, latch);

		startJobQueue();

		boolean reachedZero = latch.await(10, TimeUnit.SECONDS); // reached zero i.e.
		assertThat(reachedZero, is(true));

		beans.forEach(wrap(bean -> verify(runner).createProcess(bean, statusTopicPublisher)));
		processes.forEach(wrap(process -> verify(process).start()));
	}

	@Test
	public void testLimitingManyReturnedBeans() throws Exception {
		// Send 5 beans to the Queue
		List<StatusBean> beans = createAndSubmitBeans();
		CountDownLatch latch = new CountDownLatch(beans.size());
		setupMockProcesses(beans, latch);
		startJobQueue();
		latch.await(10, TimeUnit.SECONDS);
		List<StatusBean> returnedBeans = jobQueue.getRunningAndCompleted("3");
		// Get 3 latest beans
		assertEquals(3, returnedBeans.size());
		assertEquals("three", returnedBeans.get(0).getName());
		assertEquals("five", returnedBeans.get(2).getName());

		returnedBeans = jobQueue.getRunningAndCompleted("6");
		// Get all 5 beans that have been completed
		assertEquals(5, returnedBeans.size());

		returnedBeans = jobQueue.getRunningAndCompleted("-1");
		// Get all beans that have been completed
		assertEquals(5, returnedBeans.size());

		returnedBeans = jobQueue.getRunningAndCompleted("NaN");
		// Get all beans that have been completed
		assertEquals(5, returnedBeans.size());
	}

	@Test
	public void testLimitingManyReturnedBeansWhileBeansSubmitted() throws Exception {
		// Send 5 beans that 'will run' to the Queue
		List<StatusBean> beans = createAndSubmitBeans();
		CountDownLatch latch = new CountDownLatch(beans.size());
		setupMockProcesses(beans, latch);
		startJobQueue();
		latch.await(10, TimeUnit.SECONDS);
		// 5 completed scans + 2 submitted (but not running) scan
		jobQueue.pause();
		jobQueue.submit(new StatusBean("notRunning"));
		jobQueue.submit(new StatusBean("notRunning2"));
		List<StatusBean> returnedBeans = jobQueue.getRunningAndCompleted("3");
		// The 2 submitted beans would have been returned, so we only want 1 more to get 3 latest beans
		assertEquals(1, returnedBeans.size());
		assertEquals("five", returnedBeans.get(0).getName());

		returnedBeans = jobQueue.getRunningAndCompleted("6");
		// Get 4 beans to add the to the 2 that have been submitted to make 6
		assertEquals(4, returnedBeans.size());

		returnedBeans = jobQueue.getRunningAndCompleted("8");
		// 5 + 2 < 8 so return all 5
		assertEquals(5, returnedBeans.size());

		returnedBeans = jobQueue.getRunningAndCompleted("-1");
		// Get all beans that have been completed
		assertEquals(5, returnedBeans.size());

		returnedBeans = jobQueue.getRunningAndCompleted("NaN");
		// Get all beans that have been completed
		assertEquals(5, returnedBeans.size());
	}

	@Test
	public void testLimitingManyReturnedBeansWhileBeanRunning() throws Exception {
		// Send 5 beans that 'will run' to the Queue
		List<StatusBean> completeBeans = createAndSubmitBeans();
		List<StatusBean> nonCompleteBeans = createAndSubmitBeans("Running", "notRunning");
		CountDownLatch latch = new CountDownLatch(completeBeans.size() + 10 * nonCompleteBeans.size());
		// Default time (100ms)
		setupMockProcesses(completeBeans, latch);
		setMockProcessTime(10000);
		// (1s)
		setupMockProcesses(nonCompleteBeans, latch);

		startJobQueue();
		// Should be enough that all 100ms beans are done, but 1s beans not
		latch.await(2, TimeUnit.SECONDS);

		// 5 completed scans + 1 submitted (but not running) + 1 running scan
		List<StatusBean> submittedBeans = jobQueue.getSubmissionQueue();
		List<StatusBean> returnedBeans = jobQueue.getRunningAndCompleted("3");
		// (Submitted +) Running + Last Complete
		assertEquals(3, submittedBeans.size() + returnedBeans.size());
		assertEquals(2, returnedBeans.size());
		assertEquals("five", returnedBeans.get(0).getName());
		assertEquals("Running", returnedBeans.get(1).getName());
		assertEquals("notRunning", submittedBeans.get(0).getName());
		assertFalse(submittedBeans.get(0).getStatus().isActive());

		submittedBeans = jobQueue.getSubmissionQueue();
		returnedBeans = jobQueue.getRunningAndCompleted("6");
		// Get 5 beans to add the to the submitted one to make 6
		assertEquals(5, returnedBeans.size());

		submittedBeans = jobQueue.getSubmissionQueue();
		returnedBeans = jobQueue.getRunningAndCompleted("8");
		// 5 + 2 < 8 so return all 5
		assertEquals(6, returnedBeans.size());
	}

	@Test
	public void testWhenRunNotInOrderOfSubmission() throws Exception {
		List<StatusBean> completeBeans = Arrays.asList(new StatusBean("runSecond"), new StatusBean("runFirst"), new StatusBean("runThird"));
		completeBeans.get(0).setStartTime(2);
		completeBeans.get(1).setStartTime(1);
		completeBeans.get(2).setStartTime(3);

		for (StatusBean bean : completeBeans) {
			jobQueue.submit(bean);
		}

		CountDownLatch latch = new CountDownLatch(completeBeans.size());
		// Default time (100ms)
		setupMockProcesses(completeBeans, latch);
		// (1s)
		startJobQueue();
		// Should be enough that all beans are done
		latch.await(2, TimeUnit.SECONDS);

		List<StatusBean> returnedBeans = jobQueue.getRunningAndCompleted("2");
		assertEquals("runSecond", returnedBeans.get(0).getName());
		assertEquals("runThird", returnedBeans.get(1).getName());

	}

}
