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
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class RemoveReplaceReorderTest extends BrokerTest {

	private IEventService eservice;
	private ISubmitter<StatusBean> submitter;
	private IConsumer<StatusBean> consumer;

	private final boolean startConsumer;

	@Parameters(name="startConsumer={0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { false }, { true } });
	}

	public RemoveReplaceReorderTest(boolean startConsumer) {
		this.startConsumer = startConsumer;
	}

	@Before
	public void setUp() throws Exception {
		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(createNonOSGIActivemqMarshaller());
		eservice = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!

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
		// check they've been submitted properly
		assertThat(consumer.getSubmissionQueue(), is(equalTo(beans)));
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
		assertThat(consumer.getSubmissionQueue(), is(equalTo(beans)));
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
		assertThat(consumer.getSubmissionQueue(), is(equalTo(beans)));
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
		assertThat(consumer.getSubmissionQueue(), is(equalTo(beans)));
	}

}
