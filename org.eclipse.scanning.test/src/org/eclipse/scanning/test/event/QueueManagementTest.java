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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.dry.DryRunProcessCreator;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

//@RunWith(value = Parameterized.class)
public class QueueManagementTest extends BrokerTest {

	private IEventService eservice;
	private ISubmitter<StatusBean> submitter;
	private IConsumer<StatusBean> consumer;

	private final boolean startConsumer = false;

	@Parameters(name="startConsumer={0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { false }, { true } });
	}

	// TODO: this test is flakey when startConsumer = true, so for the moment it is
	// only tested with this as false. Fix as part of DAQ-1465
//	public QueueManagementTest(boolean startConsumer) {
//		this.startConsumer = startConsumer;
//	}

	@Before
	public void setUp() throws Exception {
		ServiceTestHelper.setupServices();

		eservice = ServiceTestHelper.getEventService();

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		submitter  = eservice.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		consumer   = eservice.createConsumer(uri, EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC, EventConstants.HEARTBEAT_TOPIC, EventConstants.CMD_TOPIC);
		consumer.setName("Test Consumer");
		consumer.clearQueue(EventConstants.SUBMISSION_QUEUE);
		consumer.clearQueue(EventConstants.STATUS_SET);

		if (startConsumer) {
			startConsumer();
		}
	}

	private void startConsumer() throws EventException, InterruptedException {
		// starts the consumer and sets it going with an initial task
		consumer.setRunner(new DryRunProcessCreator<>(0, 100, 1, 100l, true));
		consumer.start();
		consumer.awaitStart();
		submitter.submit(createBean("initial"));
	}

	@After
	public void dispose() throws Exception {
		submitter.disconnect();
		consumer.clearQueue(EventConstants.SUBMISSION_QUEUE);
		consumer.clearQueue(EventConstants.STATUS_SET);
		consumer.disconnect();
	}

	private List<StatusBean> createAndSubmitBeans() throws EventException {
		List<String> beanNames = Arrays.asList(new String[] { "one", "two", "three", "four", "five" });
		List<StatusBean> beans = beanNames.stream().map(this::createBean).collect(toList());
		for (StatusBean bean : beans) {
			submitter.submit(bean);
		}
		// check they've been submitted properly (check names for easier to read error message)
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
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
		submitter.remove(beanThree);

		// Assert, check the bean has been remove from the submission queue
		assertThat(consumer.getSubmissionQueue(), is(equalTo(
				beans.stream().filter(bean -> !bean.getName().equals("three")).collect(toList()))));
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
		submitter.replace(beanThree);

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
		submitter.reorder(beanThree, 1);

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
		submitter.reorder(beanThree, -1);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(3, beanThree);
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@Test
	public void testReorderTwiceWithSubmitter() throws Exception {
		// A regression test for DAQ-1406 the submitter's MessageProducer was closed without being nullified

		// Arrange: submit some beans and get the beans to reorder
		List<StatusBean> beans = createAndSubmitBeans();

		// get the bean to reorder
		StatusBean beanThree = beans.get(2);
		assertThat(beanThree.getName(), is(equalTo("three")));

		// Act: reorder the bean the first time
		submitter.reorder(beanThree, 1);

		// Assert: first update the beans list so we can use it as the expected answer
		beans.remove(beanThree);
		beans.add(1, beanThree);
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));

		// Act: reorder the bean a second time
		submitter.reorder(beanThree, 1);

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
		submitter.replace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));

		// Arrange: change the name again
		beanThree.setName("bar");

		// Act: replace the bean on the queue with the updated bean
		submitter.replace(beanThree);

		// Assert: check that the bean has bean updated in the submission queue (as it has in the beans list)
		assertThat(getNames(consumer.getSubmissionQueue()), is(equalTo(getNames(beans))));
	}

	@Test
	public void testClearQueue() throws Exception {
		createAndSubmitBeans();

		submitter.clearQueue(submitter.getSubmitQueueName());

		assertThat(submitter.getQueue(), is(empty()));
	}

}
