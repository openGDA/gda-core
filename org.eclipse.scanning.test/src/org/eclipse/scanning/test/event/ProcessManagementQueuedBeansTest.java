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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.AbstractLockingPausableProcess;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.eclipse.scanning.test.util.WaitingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * A test for changing the status of a queued bean, and testing that its updated in the queue.
 *
 * TODO: Note that this is a temporary test. Ideally these tests would be additional test methods in the
 * class {@link ProcessManagementTest}. However, that test mocks out activemq which would have
 * meant writing a lot of tricky code to setup the Mockito mocks. Once the queue has been brought
 * into memory, this change can be made.
 */
@Disabled("DAQ-2088 These tests time out and fail")
public class ProcessManagementQueuedBeansTest extends BrokerTest {

	private class InitialProcess extends AbstractLockingPausableProcess<StatusBean> {

		private final WaitingRunnable waitingRunnable;

		protected InitialProcess(WaitingRunnable waitingRunnable, StatusBean bean, IPublisher<StatusBean> publisher) {
			super(bean, publisher);
			this.waitingRunnable = waitingRunnable;
		}

		@Override
		public void execute() throws EventException, InterruptedException {
			// notifies any thread waiting on waitUntilRun and waits until its own release method is called
			waitingRunnable.run();

			getBean().setStatus(Status.COMPLETE);
			getPublisher().broadcast(getBean());
		}
	}

	private class TestProcessCreator implements IProcessCreator<StatusBean> {

		private final WaitingRunnable waitingRunnable = new WaitingRunnable();

		@Override
		public IBeanProcess<StatusBean> createProcess(StatusBean bean, IPublisher<StatusBean> publisher)
				throws EventException {
			if (bean.getName().equals("initial")) {
				// return the initial process that waits to be told to resume
				return new InitialProcess(waitingRunnable, bean, publisher);
			}

			// return a short job that runs in just 10ms
			return new DryRunProcess<StatusBean>(bean, publisher, true, 0, 1, 1, 10);
		}

		public void waitForInitialProcessToStart() throws InterruptedException {
			waitingRunnable.waitUntilRun();
		}

		public void releaseInitialProcess() {
			waitingRunnable.release();
		}

	}

	private ISubmitter<StatusBean> submitter;
	private IJobQueue<StatusBean> jobQueue;
	private IPublisher<StatusBean> publisher;

	private TestProcessCreator processFactory;

	@BeforeEach
	public void setUp() throws Exception {
		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		submitter = eventService.createSubmitter(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID);
		publisher = eventService.createPublisher(uri, EventConstants.STATUS_TOPIC);

		jobQueue = eventService.createJobQueue(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID, EventConstants.STATUS_TOPIC, EventConstants.QUEUE_STATUS_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		jobQueue.setName("Test Queue");
		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();

		startJobQueue();
	}

	@AfterEach
	public void tearDown() throws Exception {
		processFactory.releaseInitialProcess();

		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();

		submitter.disconnect();
		jobQueue.disconnect();
	}

	/**
	 * Starts the consumer and submits an initial task that waits, so that we add beans
	 * to the queue and rearrange them without them getting run
	 * @throws EventException
	 * @throws InterruptedException
	 */
	private void startJobQueue() throws EventException, InterruptedException {
		// starts the consumer and sets it going with an initial task

		processFactory = new TestProcessCreator();
		jobQueue.setRunner(processFactory);

		// start the consumer and wait until its fully started
		jobQueue.start();
		jobQueue.awaitStart();

		// create and submit the initial bean and wait for the process for it to start
		submitter.submit(createBean("initial"));
		processFactory.waitForInitialProcessToStart();
	}

	private StatusBean createBean(String name) {
		final StatusBean bean = new StatusBean();
		bean.setName(name);
		bean.setStatus(Status.SUBMITTED);
		return bean;
	}

	private List<StatusBean> createAndSubmitBeans() throws EventException {
		List<String> beanNames = Arrays.asList(new String[] { "one", "two", "three" });
		List<StatusBean> beans = beanNames.stream().map(this::createBean).collect(toList());
		for (StatusBean bean : beans) {
			submitter.submit(bean);
		}
		return beans;
	}

	@Test
	public void testPauseQueuedOld() throws Exception {
		testUpdateQueuedBeanStatusOld(Status.REQUEST_PAUSE);
	}

	@Test
	public void testResumeQueuedOld() throws Exception {
		testUpdateQueuedBeanStatusOld(Status.REQUEST_RESUME);
	}

	@Test
	public void testTerminatePausedOld() throws Exception {
		testUpdateQueuedBeanStatusOld(Status.REQUEST_TERMINATE);
	}

	@Test
	public void testPauseQueued() throws Exception {
		testUpdateQueuedBeanStatus(Status.REQUEST_PAUSE);
	}

	@Test
	public void testResumeQueued() throws Exception {
		testUpdateQueuedBeanStatus(Status.REQUEST_RESUME);
	}

	@Test
	public void testTerminatePaused() throws Exception {
		testUpdateQueuedBeanStatus(Status.REQUEST_TERMINATE);
	}

	private void testUpdateQueuedBeanStatusOld(Status newStatus) throws Exception {
		// TODO remove this old test - it uses the status topic to broadcast the event
		createAndSubmitBeans();

		List<StatusBean> queue = jobQueue.getSubmissionQueue();
		assertThat(queue, hasSize(3));

		StatusBean beanTwo = queue.get(1);
		beanTwo.setStatus(newStatus);
		publisher.broadcast(beanTwo);

		Thread.sleep(200); // allow some time for the update to be processed
		queue = jobQueue.getSubmissionQueue();
		assertThat(queue.get(1), is(equalTo(beanTwo)));
	}

	private void testUpdateQueuedBeanStatus(Status newStatus) throws Exception {
		createAndSubmitBeans();

		List<StatusBean> queue = jobQueue.getSubmissionQueue();
		assertThat(queue, hasSize(3));

		StatusBean beanTwo = queue.get(1);
		beanTwo.setStatus(newStatus);
		publisher.broadcast(beanTwo);

		Thread.sleep(200); // allow some time for the update to be processed
		queue = jobQueue.getSubmissionQueue();
		assertThat(queue.get(1), is(equalTo(beanTwo)));
	}

}
