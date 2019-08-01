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
import static org.junit.Assert.assertThat;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EventServiceTest extends BrokerTest {

	private IEventService eventService;
	private static final String QUEUE_NAME = "org.eclipse.scanning.test.event.queue";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		activemqConnectorService.setFilePathService(new MockFilePathService());
		eventService  = new EventServiceImpl(activemqConnectorService);
	}

	@After
	public void tearDown() throws Exception {
		eventService.disposeJobQueue();
	}

	@Test
	public void testTwoQueuesWithSameQueueName() throws EventException {
		exception.expect(EventException.class);
		exception.expectMessage("A job queue for the queue name '" + QUEUE_NAME + "' has already been created!");
		createTestJobQueue(); // create the first one
		createTestJobQueue(); // attempting to create a second job queue with the same queue name should throw
	}

	@Test
	public void createdQueueCanBeRetrievedLater() throws EventException {
		IJobQueue<ScanBean> originalJobQueue = createTestJobQueue();
		IJobQueue<? extends StatusBean> retrievedJobQueue = eventService.getJobQueue(QUEUE_NAME);
		assertThat(retrievedJobQueue, is(equalTo(originalJobQueue)));
	}

	@Test
	public void getJobQueueThrowsIfNoJobQueueFound() throws EventException {
		exception.expect(EventException.class);
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
		exception.expect(EventException.class);
		exception.expectMessage("No job queue exists for queue '" + QUEUE_NAME + "'");
		eventService.getJobQueue(QUEUE_NAME);
	}

	private IJobQueue<ScanBean> createTestJobQueue() throws EventException {
		return eventService.createJobQueue(uri, QUEUE_NAME, "dont care");
	}

}
