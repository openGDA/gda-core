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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class QueueManagementTest extends BrokerTest {

	private class InitialProcess extends AbstractLockingPausableProcess<StatusBean> {

		protected InitialProcess(StatusBean bean, IPublisher<StatusBean> publisher) {
			super(bean, publisher);
		}

		@Override
		public void execute() throws EventException, InterruptedException {
			// Decrement the countdown latch, this releases the main thread which should be waiting on the latch
			initialProcessStarted.countDown();

			releaseInitialProcess.await(); // this process waits until the test is finished
			getBean().setStatus(Status.COMPLETE);
			getPublisher().broadcast(getBean());
		}
	}

	private class TestProcessCreator implements IProcessCreator<StatusBean> {

		private IPublisher<StatusBean> publisher;

		@Override
		public IConsumerProcess<StatusBean> createProcess(StatusBean bean, IPublisher<StatusBean> publisher)
				throws EventException {
			if (this.publisher == null) {
				this.publisher = publisher;
			}
			if (bean.getName().equals("initial")) {
				// return the initial process that waits to be told to resume
				return new InitialProcess(bean, publisher);
			}

			// return a short job that runs in just 10ms
			return new DryRunProcess<StatusBean>(bean, publisher, true, 0, 1, 1, 10);
		}

	}

	private IEventService eservice;
	private ISubmitter<StatusBean> submitter;
	private IConsumer<StatusBean> consumer;
	private IConsumer<StatusBean> consumerProxy;

	private CountDownLatch initialProcessStarted;
	private CountDownLatch releaseInitialProcess;

	private final boolean useProxy;
	private final boolean startConsumer;

	@Parameters(name="useProxy = {0}, startConsumer = {1}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ false, false },
			{ false, true },
			{ true, false },
			{ true, true }
		});
	}

	public QueueManagementTest(boolean useProxy, boolean startConsumer) {
		this.useProxy = useProxy;
		this.startConsumer = startConsumer;
	}

	@Before
	public void setUp() throws Exception {
		ServiceTestHelper.setupServices();

		eservice = ServiceTestHelper.getEventService();

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		submitter = eservice.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		consumer = eservice.createConsumer(uri, EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC, EventConstants.HEARTBEAT_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		consumer.setName("Test Consumer");
		consumer.clearQueue();
		consumer.clearRunningAndCompleted();

		if (useProxy) {
			consumerProxy = eservice.createConsumerProxy(uri, EventConstants.SUBMISSION_QUEUE, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		}
		if (startConsumer) {
			startConsumer();
		}
	}

	@After
	public void tearDown() throws Exception {
		consumer.clearQueue();
		consumer.clearRunningAndCompleted();

		submitter.disconnect();
		consumer.disconnect();

		if (releaseInitialProcess != null) {
			releaseInitialProcess.countDown();
		}
	}

	/**
	 * Starts the consumer and submits an initial task that waits, so that we add beans
	 * to the queue and rearrange them without them getting run
	 * @throws EventException
	 * @throws InterruptedException
	 */
	private void startConsumer() throws EventException, InterruptedException {
		// starts the consumer and sets it going with an initial task

		// Create the latch for releasing the initial process
		releaseInitialProcess = new CountDownLatch(1);

		// Create the latch for the initial process starting
		initialProcessStarted = new CountDownLatch(1);
		consumer.setRunner(new TestProcessCreator());

		// start the consumer and wait until its fully started
		consumer.start();
		consumer.awaitStart();

		// create and submit the inital bean and wait for the process for it to start
		submitter.submit(createBean("initial"));
		boolean success = initialProcessStarted.await(1, TimeUnit.SECONDS);
		assertThat(success, is(true)); // check the above didn't time out
	}

	private IConsumer<StatusBean> getConsumer() {
		if (useProxy) {
			return consumerProxy;
		}
		return consumer;
	}

	private List<StatusBean> createAndSubmitBeans() throws EventException {
		final List<StatusBean> beans = createAndSubmitBeans(submitter);
		// check they've been submitted properly (check names for easier to read error message)
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
		return beans;
	}

	private List<StatusBean> createAndSubmitBeansToStatusSet() throws EventException {
		try (ISubmitter<StatusBean> submitter = eservice.createSubmitter(uri, EventConstants.STATUS_SET)) {
			final List<StatusBean> beans = createAndSubmitBeans(submitter);
			// check they've been submitted properly (check names for easier to read error message)
			final List<String> names = getNames(consumer.getRunningAndCompleted());
			names.remove("initial");
			assertThat(names, containsInAnyOrder(getNames(beans).toArray(new String[beans.size()])));
			return beans;
		}
	}

	private List<StatusBean> createAndSubmitBeans(ISubmitter<StatusBean> submitter) throws EventException {
		List<String> beanNames = Arrays.asList(new String[] { "one", "two", "three", "four", "five" });
		List<StatusBean> beans = beanNames.stream().map(this::createBean).collect(toList());
		for (StatusBean bean : beans) {
			submitter.submit(bean);
		}
		return beans;
	}

	private StatusBean createBean(String name) {
		final StatusBean bean = new StatusBean();
		bean.setName(name);
		bean.setStatus(Status.SUBMITTED);
		return bean;
	}

	@Test
	public void testRemove() throws Exception {
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();

		// Act: remove the third bean
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		getConsumer().remove(beanThree);

		// Assert, check the bean has been remove from the submission queue
		assertThat(consumer.getSubmissionQueue(), is(equalTo(
				beans.stream().filter(bean -> !bean.getName().equals("three")).collect(toList()))));
	}

	private void doReplace(StatusBean bean) throws Exception {
		consumer.replace(bean);
	}

	@Test
	public void testReplace() throws Exception {
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();
		// get the bean to replace and change its name
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		beanThree.setName("foo");

		// Act: replace the bean on the queue with the updated bean
		doReplace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	public List<String> getNames(List<StatusBean> beans) {
		return beans.stream().map(StatusBean::getName).collect(toList());
	}

	@Test
	public void testReorderUp() throws Exception {
		// Arrange: submit some beans
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean towards the start of the queue
		getConsumer().reorder(beanThree, 1);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(1, beanThree);
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@Test
	public void testReorderDown() throws Exception {
		// Arrange: submit some beans and get the bean to reorder
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean towards the start of the queue
		getConsumer().reorder(beanThree, -1);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(3, beanThree);
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@Test
	public void testReorderDownTwice() throws Exception {
		// A regression test for DAQ-1406 the submitter's MessageProducer was closed without being nullified

		// Arrange: submit some beans and get the beans to reorder
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean the first time
		getConsumer().reorder(beanThree, 1);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(1, beanThree);
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));

		// Act: reorder the bean a second time
		getConsumer().reorder(beanThree, 1);

		// Assert:
		beans.remove(beanThree);
		beans.add(0, beanThree);
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@Test
	public void testReplaceTwiceWithSubmitter() throws Exception {
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
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));

		// Arrange: change the name again
		beanThree.setName("bar");

		// Act: replace the bean on the queue with the updated bean
		doReplace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@Test
	public void testClearQueue() throws Exception {
		createAndSubmitBeans();

		getConsumer().clearQueue();

		assertThat(consumer.getQueue(), is(empty()));
	}

	@Test
	public void testClearCompleted() throws Exception {
		createAndSubmitBeansToStatusSet();

		getConsumer().clearRunningAndCompleted();

		assertThat(consumer.getRunningAndCompleted(), is(empty()));
	}

	private StatusBean createBean(String name, Status status, Duration age) {
		final StatusBean statusBean = new StatusBean(name);
		statusBean.setStatus(status);
		statusBean.setSubmissionTime(System.currentTimeMillis() - age.toMillis());
		return statusBean;
	}

	@Test
	public void testCleanUpCompleted() throws Exception {
		if (useProxy) return; // there's no command bean for cleaning up the queue, it only needs to be done on the server side

		List<StatusBean> beans = new ArrayList<>();
		beans.add(createBean("failed", Status.FAILED, Duration.ofHours(2)));
		beans.add(createBean("none", Status.NONE, Duration.ofHours(3)));
		beans.add(createBean("newRunning", Status.RUNNING, Duration.ofMinutes(10)));
		beans.add(createBean("oldRunning", Status.RUNNING, DEFAULT_MAXIMUM_RUNNING_AGE.plusSeconds(10)));
		beans.add(createBean("newCompleted", Status.COMPLETE, Duration.ofHours(1)));
		beans.add(createBean("oldCompleted", Status.COMPLETE, DEFAULT_MAXIMUM_COMPLETE_AGE.plusMinutes(10)));
		beans.add(createBean("notStarted", Status.SUBMITTED, Duration.ofHours(5)));
		beans.add(createBean("paused", Status.PAUSED, Duration.ofMinutes(20)));

		try (ISubmitter<StatusBean> submitter = eservice.createSubmitter(uri, EventConstants.STATUS_SET)) {
			for (StatusBean bean : beans) {
				submitter.submit(bean);
			}

			// check they've been submitted properly (check names for easier to read error message)
			final List<String> names = getNames(consumer.getRunningAndCompleted());
			names.remove("initial");
			assertThat(names, containsInAnyOrder(getNames(beans).toArray(new String[beans.size()])));
		}

		consumer.cleanUpCompleted();

		final Map<String, StatusBean> beanMap = consumer.getRunningAndCompleted().stream().collect(
				toMap(StatusBean::getName, identity()));
		beanMap.remove("initial");

		assertThat(beanMap.keySet(), containsInAnyOrder("newRunning", "newCompleted", "notStarted", "paused"));
		// check that paused and not-started beans have had their status set to FAILED
		assertThat(beanMap.get("paused").getStatus(), is(Status.FAILED));
		assertThat(beanMap.get("notStarted").getStatus(), is(Status.FAILED));
	}

	@Test
	public void testRemoveCompleted() throws Exception {
		// Arrange: submit some beans directly to the status set
		List<StatusBean> beans = createAndSubmitBeansToStatusSet();

		// Act: remove the third bean
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));
		getConsumer().removeCompleted(beanThree);

		List<String> expectedNames = getNames(beans);
		expectedNames.remove("three");
		List<String> actualNames = getNames(consumer.getRunningAndCompleted());
		actualNames.remove("initial");

		// Assert: check the bean has been removed from the status set
		assertThat(expectedNames, containsInAnyOrder(expectedNames.toArray()));
	}

}
