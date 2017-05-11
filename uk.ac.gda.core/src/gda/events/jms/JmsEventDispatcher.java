/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.events.jms;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.corba.util.EventDispatcher;
import gda.util.Serializer;

/**
 * An {@link EventDispatcher} that uses JMS to dispatch events. This class was significantly rewritten as part of DAQ-515
 */
public class JmsEventDispatcher extends JmsClient implements EventDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(JmsEventDispatcher.class);

	private final ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, MessageProducer> publisherMap = new ConcurrentHashMap<>();

	/**
	 * Creates a JMS event dispatcher.
	 */
	public JmsEventDispatcher() {
		// If the super constructor succeeds then we know the session was created
		logger.info("Created new session: {}", session);
	}

	@Override
	public void publish(String sourceName, Object message) {
		// Check if message is not Serializable and fail.
		if (!(message instanceof Serializable)) {
			logger.error("Message is not Serializable. Message class is {}", message.getClass().getName());
			throw new IllegalArgumentException(new NotSerializableException(message.getClass().getName()));
		}

		try {
			// Get the topic and the publisher
			final Topic topic = getTopic(sourceName);
			final MessageProducer publisher = getPublisher(sourceName);

			// Serialize the message - here we used the GDA Serializer as it can see the required classes
			// ActiveMQ could serialize for us but then ActiveMQ needs to be able to see all the classes
			// we might want to deserialize into.
			final byte[] serializedObject = Serializer.toByte(message);
			// Make a object message containing the serialized message object
			final ObjectMessage objectMessage = session.createObjectMessage(serializedObject);
			// Add a sending timestamp to message
			objectMessage.setJMSTimestamp(System.currentTimeMillis());

			// Send the message
			publisher.send(topic, objectMessage);

		// Catch RuntimeException here as it used to wrap JMSException
		} catch (RuntimeException e) {
			// Check if the RuntimeException is wrapping a JMSException. If it is log the JMSException.
			if (e.getCause() instanceof JMSException ){
				logger.error("Unable to dispatch message from sourceName: {}", sourceName, e.getCause());
			}
			else {
				// It's a RuntimeExceptions that's NOT wrapping a JMSException so just rethrow it.
				throw e;
			}
		} catch (JMSException e) {
			logger.error("Unable to dispatch message from sourceName: {}", sourceName, e);
		}
	}

	private Topic getTopic(final String sourceName) {
		// If we have already created a Topic for this source just return it, else create one
		return topicMap.computeIfAbsent(sourceName, key -> {
			logger.trace("Creating topic for sourceName: {}", key);
			try {
				return session.createTopic(TOPIC_PREFIX + key);
			} catch (JMSException e) {
				// Wrap with RuntimeException will be unwrapped and handled later
				throw new RuntimeException("Error creating topic for sourceName: " + key, e);
			}
		});
	}

	private MessageProducer getPublisher(final String sourceName) {
		// If we have already created a publisher for this topic just return it, else create one
		return publisherMap.computeIfAbsent(sourceName, key -> {
			logger.trace("Creating publisher for topic: {}", key);
			try {
				// Not got a publisher for this topic so make one
				MessageProducer publisher = session.createProducer(getTopic(key));

				// Guarantee message delivery (as much as possible)
				// Maybe NON_PERSISTENT would be ok here, and would improve throughput but be cautious for now.
				publisher.setDeliveryMode(DeliveryMode.PERSISTENT);

				return publisher;
			} catch (JMSException e) {
				// Wrap with RuntimeException will be unwrapped and handled later
				throw new RuntimeException("Error creating publisher for topic: " + key, e);
			}
		});
	}

}
