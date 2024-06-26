/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;


/**
 * This is just for testing out messages with the server.
 */
public class JavaMessagingTests extends BrokerTest {

	@Test
	public void testGetDeviceNames() throws ScanningException {

		Collection<String> deviceNames = ServiceProvider.getService(IRunnableDeviceService.class).getRunnableDeviceNames();

		System.out.println(deviceNames.toString());

	}

	@Test
	public void testAvailableQueuesAndTopics() throws ScanningException, JMSException {

		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(uri);

		// Create a Connection
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();

		// Important point that was missed in the above answer
		connection.start();

		DestinationSource ds = connection.getDestinationSource();
		Set<ActiveMQQueue> queues = ds.getQueues();

	System.out.println("List of queues:");

		for (ActiveMQQueue queue : queues) {
			try {
				System.out.println(queue.getQueueName());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		System.out.println("\nNow for topics:");

		Set<ActiveMQTopic> topics = ds.getTopics();

		for (ActiveMQTopic topic : topics) {
			try {
				System.out.println(topic.getTopicName());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}


	@Test
	public void testProducedMessage() throws Exception {

		MarshallerService marshaller = new MarshallerService(new PointsModelMarshaller());
		DeviceRequest req = new DeviceRequest();
		Collection<DeviceInformation<?>> devices = new ArrayList<DeviceInformation<?>>();

		System.out.println("First, no collection:");
		System.out.println(marshaller.marshal(req));

		System.out.println("\nZero items in collection");
		req.setDevices(devices);
		System.out.println(marshaller.marshal(req));

		System.out.println("\nNow, with one DeviceInformation");
		devices.add(new DeviceInformation<Object>("This is the first device name."));
		System.out.println(marshaller.marshal(req));

		System.out.println("\nNow, with two DeviceInformation's");
		devices.add(new DeviceInformation<Object>("This is the second device's name."));
		System.out.println(marshaller.marshal(req));
	}

	@Test
	public void simpleSerialize() throws Exception {
		Collection<DeviceInformation<?>> devices = new HashSet<DeviceInformation<?>>();
		devices.add(new DeviceInformation<Object>("This is the first device name."));
		devices.add(new DeviceInformation<Object>("This is the second device's name."));
		DeviceRequest in = new DeviceRequest();
		in.setDevices(devices);
		IEventService eventService = ServiceProvider.getService(IEventService.class);
        String json = eventService.getEventConnectorService().marshal(in);
        DeviceRequest back = eventService.getEventConnectorService().unmarshal(json, DeviceRequest.class);
        System.out.println(json);
	}

}