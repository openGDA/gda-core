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

package uk.ac.diamond.daq.messaging.json;

import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.api.messaging.Destination;
import uk.ac.diamond.daq.api.messaging.Message;
import uk.ac.diamond.daq.api.messaging.MessagingService;
import uk.ac.diamond.daq.concurrent.Threads;
import uk.ac.diamond.daq.services.PropertyService;

/**
 * Implementation of the {@link MessagingService}. It uses ActiveMQ as the broker and Jackson for the JSON
 * serialisation.
 * <p>
 * This class is using DS annotations to automatically generate the component.xml files in /OSGI-INF. Please don't edit
 * the files manually if you want to change something edit the annotations.
 *
 * @author James Mudd
 * @since GDA 9.12
 */
@Component(name = "JsonMessagingServiceImpl")
public class JsonMessagingService implements MessagingService {
	private static final Logger logger = LoggerFactory.getLogger(JsonMessagingService.class);

	private static final long MESSAGE_TIME_TO_LIVE_MS = 5 * 60 * 1000L; // 5 mins

	private final ConcurrentMap<Class<?>, String> typeToDestination = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, MessageProducer> topicToProducer = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final ExecutorService executorService = Executors.newSingleThreadExecutor(Threads.daemon().named(JsonMessagingService.class.getCanonicalName()).factory());

	private Session session;

	@Activate
	public void activate() {
		final String jmsBrokerUri = LocalProperties.getActiveMQBrokerURI();
		final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(jmsBrokerUri);
		try {
			Connection connection = factory.createConnection();
			session = connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			throw new RuntimeException("Failed to connect to ActiveMQ, is it running?", e);
		}
	}

	@Override
	public void sendMessage(Message message) {
		// Figure out the destination - There is a map to cache this by type to avoid getting the annotation every time.
		String destination = typeToDestination.computeIfAbsent(message.getClass(), this::getDestination);
		sendMessage(message, destination);
	}

	private String getDestination(Class<?> type) {
		// Lookup the destination annotation
		Destination[] annotationsByType = type.getAnnotationsByType(Destination.class);
		if (annotationsByType.length == 0) {
			throw new IllegalArgumentException("Trying to send an instance of '" + type.getCanonicalName()
					+ "' which does not have an @Destination annotation");
		}
		return annotationsByType[0].value();
	}

	@Override
	public void sendMessage(Message message, String destination) {
		// Submit the message to be sent and return without blocking
		executorService.execute(new SendMessageRunnable(message, destination));
	}

	private MessageProducer createProducer(String topic) {
		try {
			Topic jmsTopic = session.createTopic(topic);
			MessageProducer producer = session.createProducer(jmsTopic);
			producer.setDeliveryMode(NON_PERSISTENT);
			producer.setTimeToLive(MESSAGE_TIME_TO_LIVE_MS);
			return producer;
		} catch (JMSException e) {
			logger.error("Could not create topic '{}'", topic, e);
			throw new RuntimeException("Failed to create topic: " + topic, e);
		}
	}

	@Reference(cardinality = MANDATORY)
	public synchronized void setFactoryService(PropertyService propertyService) {
		logger.debug("Set Property Service to {}", propertyService);
		// We don't actually need this but requiring it means it will be initalized before we call LocalProperties.
	}

	private class SendMessageRunnable implements Runnable {
		private final Message message;
		private final String destination;

		public SendMessageRunnable(Message message, String destination) {
			this.message = message;
			this.destination = destination;
		}

		@Override
		public void run() {
			// Get the producer, there is a cache to avoid creating the producer every time.
			MessageProducer producer = topicToProducer.computeIfAbsent(destination, JsonMessagingService.this::createProducer);
			try {
				// Serialise to JSON
				String json = objectMapper.writeValueAsString(message);
				// Make JMS test message
				javax.jms.Message jsonMessage = session.createTextMessage(json);
				// Send
				producer.send(jsonMessage);

			} catch (JsonProcessingException e) {
				logger.error("Failed converting '{}' to JSON", message, e);
			} catch (JMSException e) {
				logger.error("Failed to send message '{}'", message, e);
			}
		}
	}

}
