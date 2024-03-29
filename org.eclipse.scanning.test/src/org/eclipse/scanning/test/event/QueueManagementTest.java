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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scanning.event.EventTimingsHelper.DEFAULT_MAXIMUM_COMPLETE_AGE;
import static org.eclipse.scanning.event.EventTimingsHelper.DEFAULT_MAXIMUM_RUNNING_AGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.AbstractLockingPausableProcess;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.eclipse.scanning.test.util.WaitingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class QueueManagementTest extends BrokerTest {

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
			return new DryRunProcess<StatusBean>(bean, publisher, true, 0, 1, 2, 10);
		}

		public void waitForInitialProcessToStart() throws InterruptedException {
			waitingRunnable.waitUntilRun();
		}

		public void releaseInitialProcess() {
			waitingRunnable.release();
		}

	}

	private IJobQueue<StatusBean> jobQueue;
	private IJobQueue<StatusBean> jobQueueProxy;

	private TestProcessCreator processFactory;

	private boolean useProxy;
	private boolean startConsumerThread;

	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ false, false },
			{ false, true },
			{ true, false },
			{ true, true }
		});
	}

	private void setUp(boolean useProxy, boolean startConsumerThread) throws Exception {
		this.useProxy = useProxy;
		this.startConsumerThread = startConsumerThread;

		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		jobQueue = eventService.createJobQueue(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID, EventConstants.STATUS_TOPIC, EventConstants.QUEUE_STATUS_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		jobQueue.setName("Test Queue");
		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();

		if (useProxy) {
			jobQueueProxy = eventService.createJobQueueProxy(uri, ScanningTestUtils.SUBMISSION_QUEUE_WITH_ID, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		}
		if (startConsumerThread) {
			startConsumerThread(true);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		if (processFactory != null) {
			processFactory.releaseInitialProcess();
		}

		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();

		if (startConsumerThread) {
			jobQueue.stop();
			jobQueue.awaitStop();
		}

		ServiceProvider.getService(IEventService.class).disposeAllJobQueues(); // calls jobQueue.dispose()
		jobQueue = null;
	}

	/**
	 * Starts the job queue's consumer thread and submits an initial task that waits, so that we add beans
	 * to the queue and rearrange them without them getting run
	 * @throws EventException
	 * @throws InterruptedException
	 */
	private void startConsumerThread(boolean submitInitialBean) throws EventException, InterruptedException {
		// starts the consumer thread  and sets it going with an initial task

		processFactory = new TestProcessCreator();
		jobQueue.setRunner(processFactory);

		// start the consumer and wait until its fully started
		jobQueue.start();
		jobQueue.awaitStart();

		if (submitInitialBean) {
			// create and submit the initial bean and wait for the process for it to start
			jobQueue.submit(createBean("initial"));
			processFactory.waitForInitialProcessToStart();
		}
	}

	private IJobQueue<StatusBean> getJobQueue() {
		if (useProxy) {
			return jobQueueProxy;
		}
		return jobQueue;
	}

	private List<StatusBean> createAndSubmitBeans() throws EventException {
		final List<String> beanNames = Arrays.asList(new String[] { "one", "two", "three", "four", "five" });
		final List<StatusBean> beans = beanNames.stream().map(this::createBean).collect(toList());
		submitBeans(beans);

		// check they've been submitted properly (check names for easier to read error message)
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));
		return beans;
	}

	private void submitBeans(final List<StatusBean> beans) throws EventException {
		for (StatusBean bean : beans) {
			jobQueue.submit(bean);
		}
	}

	private List<StatusBean> createSubmitAndRunBeans() throws Exception {
		final List<StatusBean> beans = createAndSubmitBeans();
		runBeans(beans);
		return beans;
	}

	private void runBeans(List<StatusBean> beans) throws Exception {
		if (startConsumerThread) {
			processFactory.releaseInitialProcess(); // the consumer is already started
		} else {
			startConsumerThread(false); // start the consumer thread just to consume the submitted beans
		}

		// wait for all beans to be set to COMPLETED
		// note, this normally takes around 100-200ms, but sometimes can take over 2 seconds
		final int numExpected = beans.size() + (startConsumerThread ? 1 : 0); // the extra bean is the initial bean
		boolean allCompleted = false;
		List<StatusBean> statusSet;
		final long startTime = System.currentTimeMillis();
		final long timeout = startTime + 5000;
		do {
			Thread.sleep(100);
			statusSet = jobQueue.getRunningAndCompleted();
			allCompleted = statusSet.size() == numExpected &&
					statusSet.stream().allMatch(bean -> bean.getStatus() == Status.COMPLETE);
		} while (!allCompleted && (System.currentTimeMillis() < timeout));

		assertThat(allCompleted, is(true));

		List<String> beanNames = getNames(jobQueue.getRunningAndCompleted());
		beanNames.removeIf(name -> name.equals("initial"));
		assertThat(beanNames, is(equalTo(getNames(beans))));

		if (!startConsumerThread) {
			jobQueue.stop(); // stop the consumer so that it is not running for the main test
			jobQueue.awaitStop();
		}
	}

	private StatusBean createBean(String name) {
		final StatusBean bean = new StatusBean();
		bean.setName(name);
		bean.setStatus(Status.SUBMITTED);
		return bean;
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testSubmit(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		List<StatusBean> beans = createAndSubmitBeans();

		StatusBean newBean = new StatusBean("new");
		getJobQueue().submit(newBean);

		List<StatusBean> submissionQueue = jobQueue.getSubmissionQueue();
		List<String> expectedNames = getNames(beans);
		expectedNames.add(newBean.getName());
		assertThat(getNames(submissionQueue), is(equalTo(expectedNames)));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testRemove(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();

		// Act: remove the third bean
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		getJobQueue().remove(beanThree);

		// Assert, check the bean has been remove from the submission queue
		assertThat(jobQueue.getSubmissionQueue(), is(equalTo(
				beans.stream().filter(bean -> !bean.getName().equals("three")).collect(toList()))));
	}

	private void doReplace(StatusBean bean) throws Exception {
		jobQueue.replace(bean);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testReplace(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();
		// get the bean to replace and change its name
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		beanThree.setName("foo");

		// Act: replace the bean on the queue with the updated bean
		doReplace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	public List<String> getNames(List<StatusBean> beans) {
		return beans.stream().map(StatusBean::getName).collect(toList());
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testMoveForward(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean towards the start of the queue
		getJobQueue().moveForward(beanThree);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(1, beanThree);
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testMoveBackward(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// Arrange: submit some beans and get the bean to reorder
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean towards the end of the queue
		getJobQueue().moveBackward(beanThree);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(3, beanThree);
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testMoveFowardTwice(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// A regression test for DAQ-1406 the submitter's MessageProducer was closed without being nullified

		// Arrange: submit some beans and get the beans to reorder
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean the first time
		getJobQueue().moveForward(beanThree);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(1, beanThree);
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));

		// Act: reorder the bean a second time
		getJobQueue().moveForward(beanThree);

		// Assert:
		beans.remove(beanThree);
		beans.add(0, beanThree);
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testReplaceTwiceWithSubmitter(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// A regression test for DAQ-1406, the submitter's MessageProducer was closed without being nullified
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();
		// get the bean to replace and change its name
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		beanThree.setName("foo");

		// Act: replace the bean on the queue with the updated bean
		doReplace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));

		// Arrange: change the name again
		beanThree.setName("bar");

		// Act: replace the bean on the queue with the updated bean
		doReplace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(jobQueue.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testClearQueue(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		createAndSubmitBeans();

		getJobQueue().clearQueue();

		assertThat(jobQueue.getSubmissionQueue(), is(empty()));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testGetQueue(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		List<StatusBean> beans = createAndSubmitBeans();

		// check that getting the queue works when using a consumer proxy
		assertThat(getJobQueue().getSubmissionQueue(), is(equalTo(beans)));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testGetRunningAndCompleted(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		List<StatusBean> beans = createSubmitAndRunBeans();

		List<StatusBean> completed = getJobQueue().getRunningAndCompleted();
		completed.removeIf(b -> b.getName().equals("initial"));

		assertThat(getNames(completed), is(equalTo(getNames(beans))));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testClearCompleted(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		createSubmitAndRunBeans();

		getJobQueue().clearRunningAndCompleted();

		assertThat(jobQueue.getRunningAndCompleted(), is(empty()));
	}

	private StatusBean createBean(String name, Status status, Duration age) {
		final StatusBean statusBean = new StatusBean(name);
		statusBean.setStatus(status);
		statusBean.setSubmissionTime(System.currentTimeMillis() - age.toMillis());
		return statusBean;
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testCleanUpCompleted(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		if (useProxy) return; // there's no command bean for cleaning up the queue, it only needs to be done on the server side
		// Arrange
		// These beans aren't the ones submitted, but are used to set the submitted beans after they've been run
		List<StatusBean> setupBeans = new ArrayList<>();
		setupBeans.add(createBean("failed", Status.FAILED, Duration.ofHours(2)));
		setupBeans.add(createBean("none", Status.NONE, Duration.ofHours(3)));
		setupBeans.add(createBean("newRunning", Status.RUNNING, Duration.ofMinutes(10)));
		setupBeans.add(createBean("oldRunning", Status.RUNNING, DEFAULT_MAXIMUM_RUNNING_AGE.plusSeconds(10)));
		setupBeans.add(createBean("newCompleted", Status.COMPLETE, Duration.ofHours(1)));
		setupBeans.add(createBean("oldCompleted", Status.COMPLETE, DEFAULT_MAXIMUM_COMPLETE_AGE.plusMinutes(10)));
		setupBeans.add(createBean("notStarted", Status.SUBMITTED, Duration.ofHours(5)));
		setupBeans.add(createBean("paused", Status.PAUSED, Duration.ofMinutes(20)));
		// the beans we submit are just copies with the same names as the status and submission time should not be set yet

		List<StatusBean> beansToSubmit = setupBeans.stream().map(bean -> createBean(bean.getName())).collect(toList());
		submitBeans(beansToSubmit);
		runBeans(beansToSubmit);

		// check they've been submitted properly (check names for easier to read error message)
		List<StatusBean> completedBeans = jobQueue.getRunningAndCompleted();
		completedBeans.removeIf(bean -> bean.getName().equals("initial"));
		final List<String> names = getNames(completedBeans);
		assertThat(names, containsInAnyOrder(getNames(setupBeans).toArray(new String[setupBeans.size()])));

		// now we can update the completed beans to be the same as the setup beans
		final Map<String, StatusBean> beansByName = setupBeans.stream().collect(toMap(b -> b.getName(), identity()));
		completedBeans.stream().forEach(b -> b.merge(beansByName.get(b.getName())));

		// Act - call the method under test
		jobQueue.cleanUpCompleted();
		final List<StatusBean> remainingBeans = jobQueue.getRunningAndCompleted();
		final Map<String, StatusBean> remainingBeansByName = remainingBeans.stream().collect(toMap(b -> b.getName(), identity()));
		assertThat(remainingBeansByName.keySet(), containsInAnyOrder("newRunning", "newCompleted", "notStarted", "paused"));

		// check that paused and not-started beans have had their status set to FAILED
		assertThat(remainingBeansByName.get("paused").getStatus(), is(Status.FAILED));
		assertThat(remainingBeansByName.get("notStarted").getStatus(), is(Status.FAILED));
	}

	@ParameterizedTest
	@MethodSource("data")
	public void testRemoveCompleted(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		// Arrange: submit some beans directly to the status set
		List<StatusBean> beans = createSubmitAndRunBeans();

		// Act: remove the third bean
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		getJobQueue().removeCompleted(beanThree);

		List<String> expectedNames = getNames(beans);
		expectedNames.remove("three");
		List<String> actualNames = getNames(jobQueue.getRunningAndCompleted());
		actualNames.remove("initial");

		// Assert: check the bean has been removed from the status set
		assertThat(actualNames, containsInAnyOrder(expectedNames.toArray()));
	}


	@ParameterizedTest
	@MethodSource("data")
	/*
	 * Submitted scans that have not yet been run should, when paused, be passed over, allowing
	 * scans submitted after them to run first. This allows for the situation where e.g.:
	 * The results of Scan A may determine which of scans A_1, A_2, A_3 is run
	 * But Scan B is run independent of A, and the results of Scan A can
	 * be inspected while Scan B is running:
	 * The queue is paused
	 * Scans A, A_1, A_2, A_3, B are queued.
	 * Scans A_1-A_3 are paused.
	 * The queue is unpaused and Scan A runs. Scan B starts.
	 * Results A are examined and Scan A_2 is unpaused, running when Scan B finishes (before e.g. Scan C)
	 * Scans A_1, A_3 are removed.
	 */
	public void pausedSubmittedScansAreSkipped(boolean useProxy, boolean startConsumerThread) throws Exception {
		setUp(useProxy, startConsumerThread);
		List<StatusBean> beans = createAndSubmitBeans();
		StatusBean firstBean = beans.get(0);
		getJobQueue().pauseJob(firstBean);
		StatusBean thirdBean = beans.get(2);
		getJobQueue().pauseJob(thirdBean);
		beans.remove(2);
		beans.remove(0);
		// Scans 2, 4, 5 should have been run
		runBeans(beans);
		getJobQueue().resumeJob(firstBean);
		getJobQueue().resumeJob(thirdBean);
		beans.add(firstBean);
		beans.add(thirdBean);
		// Scans 2, 4, 5, 1, 3 should have run.
		runBeans(beans);
	}

}
