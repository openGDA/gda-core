/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.file.MockFilePathService;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.common.activemq.test.TestSessionService;

public class EventServiceTest extends BrokerTest {

	private IEventService eventService;
	private static final String QUEUE_NAME = "org.eclipse.scanning.test.event.queue".concat(ScanningTestUtils.JVM_UNIQUE_ID);

	@Before
	public void setUp() {
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		activemqConnectorService.setFilePathService(new MockFilePathService());
		activemqConnectorService.setSessionService(new TestSessionService());
		eventService  = new EventServiceImpl(activemqConnectorService);
	}

	@After
	public void tearDown() throws Exception {
		eventService.disposeJobQueue();
	}

	@Test
	public void testTwoQueuesWithSameQueueName() throws EventException {
		createTestJobQueue(); // create the first one
		var e = assertThrows(EventException.class, () -> createTestJobQueue()); // attempting to create a second job
																				// queue with the same queue name should
																				// throw
		assertThat(e.getMessage(), is("A job queue for the queue name '" + QUEUE_NAME + "' has already been created!"));
	}

	@Test
	public void createdQueueCanBeRetrievedLater() throws EventException {
		IJobQueue<ScanBean> originalJobQueue = createTestJobQueue();
		IJobQueue<? extends StatusBean> retrievedJobQueue = eventService.getJobQueue(QUEUE_NAME);
		assertThat(retrievedJobQueue, is(equalTo(originalJobQueue)));
	}

	@Test(expected = EventException.class)
	public void getJobQueueThrowsIfNoJobQueueFound() throws EventException {
		eventService.getJobQueue(QUEUE_NAME);
	}

	@Test
	public void disposeJobQueueDisconnectsThem() throws EventException {
		IJobQueue<ScanBean> consumer = createTestJobQueue();
		assertThat(consumer.isConnected(), is(true));
		eventService.disposeJobQueue();
		assertThat(consumer.isConnected(), is(false));
	}

	@Test
	public void disposeJobQueusUnregistersThem() throws Exception {
		createTestJobQueue();
		eventService.disposeJobQueue();

		// This should throw since job queue has been unregistered
		var e = assertThrows("No job queue exists for queue '" + QUEUE_NAME + "'", EventException.class,
				() -> eventService.getJobQueue(QUEUE_NAME));
		assertThat(e.getMessage(), is("No job queue exists for queue '" + QUEUE_NAME + "'"));
	}

	private IJobQueue<ScanBean> createTestJobQueue() throws EventException {
		return eventService.createJobQueue(uri, QUEUE_NAME, "dont care");

	}

}
