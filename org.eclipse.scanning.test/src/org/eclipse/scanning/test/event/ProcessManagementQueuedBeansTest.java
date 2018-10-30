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
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.dry.DryRunProcess;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.util.WaitingRunnable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A test for changing the status of a queued bean, and testing that its updated in the queue.
 *
 * TODO: Note that this is a temporary test. Ideally these tests would be additional test methods in the
 * class {@link ProcessManagementTest}. However, that test mocks out activemq which would have
 * meant writing a lot of tricky code to setup the Mockito mocks. Once the queue has been brought
 * into memory, this change can be made.
 */
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
		public IConsumerProcess<StatusBean> createProcess(StatusBean bean, IPublisher<StatusBean> publisher)
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

	private IEventService eservice;
	private ISubmitter<StatusBean> submitter;
	private IConsumer<StatusBean> consumer;
	private IPublisher<StatusBean> publisher;

	private TestProcessCreator processFactory;

	@Before
	public void setUp() throws Exception {
		ServiceTestHelper.setupServices();

		eservice = ServiceTestHelper.getEventService();
		submitter = eservice.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		publisher = eservice.createPublisher(uri, EventConstants.STATUS_TOPIC);

		consumer = eservice.createConsumer(uri, EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC, EventConstants.HEARTBEAT_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		consumer.setName("Test Consumer");
		consumer.clearQueue();
		consumer.clearRunningAndCompleted();

		startConsumer();
	}

	@After
	public void tearDown() throws Exception {
		processFactory.releaseInitialProcess();

		consumer.clearQueue();
		consumer.clearRunningAndCompleted();

		submitter.disconnect();
		consumer.disconnect();
	}

	/**
	 * Starts the consumer and submits an initial task that waits, so that we add beans
	 * to the queue and rearrange them without them getting run
	 * @throws EventException
	 * @throws InterruptedException
	 */
	private void startConsumer() throws EventException, InterruptedException {
		// starts the consumer and sets it going with an initial task

		processFactory = new TestProcessCreator();
		consumer.setRunner(processFactory);

		// start the consumer and wait until its fully started
		consumer.start();
		consumer.awaitStart();

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

		List<StatusBean> queue = consumer.getSubmissionQueue();
		assertThat(queue, hasSize(3));

		StatusBean beanTwo = queue.get(1);
		beanTwo.setStatus(newStatus);
		publisher.broadcast(beanTwo);

		Thread.sleep(200); // allow some time for the update to be processed
		queue = consumer.getSubmissionQueue();
		assertThat(queue.get(1), is(equalTo(beanTwo)));
	}

	private void testUpdateQueuedBeanStatus(Status newStatus) throws Exception {
		createAndSubmitBeans();

		List<StatusBean> queue = consumer.getSubmissionQueue();
		assertThat(queue, hasSize(3));

		StatusBean beanTwo = queue.get(1);
		beanTwo.setStatus(newStatus);
		publisher.broadcast(beanTwo);

		Thread.sleep(200); // allow some time for the update to be processed
		queue = consumer.getSubmissionQueue();
		assertThat(queue.get(1), is(equalTo(beanTwo)));
	}

}
