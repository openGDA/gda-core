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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.Constants;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubmitterTest extends BrokerTest {

	private IEventService eventService;
	private ISubmitter<StatusBean> submitter;
	private ISubscriber<IBeanListener<StatusBean>> subscriber;

	@Before
	public void start() throws Exception {
		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(createNonOSGIActivemqMarshaller(
				FredStatusBean.class, BillStatusBean.class));
		eventService = new EventServiceImpl(activemqConnectorService); // Do not copy this get the service from OSGi!

		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally
		submitter = eventService.createSubmitter(uri, EventConstants.SUBMISSION_QUEUE);
		submitter.setStatusTopicName(null);
		submitter.clearQueue(EventConstants.SUBMISSION_QUEUE);

		subscriber = eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC);

		Constants.setReceiveFrequency(100);
		Constants.setNotificationFrequency(200); // Normally 2000
	}

	@After
	public void stop() throws Exception {
		Constants.setNotificationFrequency(2000); // Normally 2000
		submitter.disconnect();
		submitter.clearQueue(EventConstants.SUBMISSION_QUEUE);
		submitter.disconnect();

		subscriber.disconnect();
	}

	private StatusBean createStatusBean() {
		StatusBean bean = new StatusBean();
		bean.setName("simple");
		bean.setMessage("A simple test bean");
		return bean;
	}

	@Test
	public void testSimpleSubmission() throws Exception {
		final StatusBean bean = createStatusBean();
		submitter.submit(bean);
		Thread.sleep(100);

		List<StatusBean> beans = submitter.getQueue(); // uses utility method in AbstractQueueConnection
		assertThat(beans, hasSize(1));
		assertThat(beans.get(0), is(equalTo(bean)));
	}

	@Test
	public void testBeanPublishedToStatusTopicOff() throws Exception {
		testBeanPublishedToStatusTopic(false);
	}

	@Test
	public void testBeanPublishedToStatusTopicOn() throws Exception {
		testBeanPublishedToStatusTopic(true);
	}

	private void testBeanPublishedToStatusTopic(boolean publishToStatusTopic) throws Exception {
		submitter.setStatusTopicName(publishToStatusTopic ? EventConstants.STATUS_TOPIC : null);
		final TestBeanListener listener = new TestBeanListener(1);
		subscriber.addListener(listener);

		final StatusBean bean = createStatusBean();
		submitter.submit(bean);
		Thread.sleep(100);

		List<StatusBean> beans = submitter.getQueue(); // uses utility method in AbstractQueueConnection
		assertThat(beans, hasSize(1));
		assertThat(beans.get(0), is(equalTo(bean)));

		listener.awaitBeans();
		beans = listener.getBeansReceived();

		if (publishToStatusTopic) {
			assertThat(beans, hasSize(1));
			assertThat(beans.get(0), is(equalTo(bean)));
		} else {
			assertThat(beans, is(empty()));
		}
	}

}
