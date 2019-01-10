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
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;
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
	public void setUpServices() {
		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eventService  = new EventServiceImpl(activemqConnectorService);
	}

	@Test
	public void enforceOneConsumerPerQueue() throws EventException {
		exception.expect(EventException.class);
		exception.expectMessage("A consumer for queue '" + QUEUE_NAME + "' has already been created!");
		createTestConsumer(); // create the first one
		createTestConsumer(); // attempting to create a second consumer for the same queue should throw
	}

	@Test
	public void createdConsumerCanBeRetrievedLater() throws EventException {
		IConsumer<ScanBean> originalConsumer = createTestConsumer();
		IConsumer<? extends StatusBean> retrievedConsumer = eventService.getConsumer(QUEUE_NAME);
		assertThat(retrievedConsumer, is(equalTo(originalConsumer)));
	}

	@Test
	public void getConsumerThrowsIfNoConsumerFound() throws EventException {
		exception.expect(EventException.class);
		eventService.getConsumer(QUEUE_NAME);
	}

	@Test
	public void disposeConsumersDisconnectsThem() throws EventException {
		IConsumer<ScanBean> consumer = createTestConsumer();
		assertThat(consumer.isConnected(), is(true));
		eventService.disposeConsumers();
		assertThat(consumer.isConnected(), is(false));
	}

	@Test
	public void disposeConsumersUnregistersThem() throws Exception {
		createTestConsumer();
		eventService.disposeConsumers();

		// This should throw since consumer has been unregistered
		exception.expect(EventException.class);
		exception.expectMessage("No consumer exists for queue '" + QUEUE_NAME + "'");
		eventService.getConsumer(QUEUE_NAME);
	}

	private IConsumer<ScanBean> createTestConsumer() throws EventException {
		return eventService.createConsumer(uri, QUEUE_NAME, "dont care", "dont care");
	}

}
