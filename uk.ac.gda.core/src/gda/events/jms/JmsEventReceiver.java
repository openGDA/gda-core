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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.corba.util.EventReceiver;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.Filter;
import gda.factory.corba.util.NameFilter;
import gda.util.Serializer;

/**
 * An {@link EventReceiver} that receives messages using JMS. This class was significantly rewritten as part of DAQ-515
 */
public class JmsEventReceiver extends JmsClient implements EventReceiver {

	private static final Logger logger = LoggerFactory.getLogger(JmsEventReceiver.class);

	/**
	 * Messages received older than this will generate a warning.
	 */
	private static final long MESSAGE_AGE_WARNING_MILLS = 1000;

	/**
	 * Store a list of consumers so they can be disconnected
	 */
	private final List<MessageConsumer> consumers = new CopyOnWriteArrayList<>();

	/**
	 * Creates a JMS event receiver.
	 */
	public JmsEventReceiver() {
		logger.info("Created new session: '{}'", session);
		try {
			connection.start();
		} catch (JMSException e) {
			throw new RuntimeException("Unable to create JMS event receiver", e);
		}
	}

	@Override
	public void subscribe(final EventSubscriber eventSubscriber, final Filter filter) {
		try {
			// TODO The only Filter type we have is NameFilter so this cast is definitely safe at the moment but maybe
			// we should consider redesigning or removing this Filter system.
			final NameFilter nf = (NameFilter) filter;
			final String name = nf.getName();
			final String topicName = TOPIC_PREFIX + name;
			final Topic topic = session.createTopic(topicName);

			final MessageConsumer consumer = session.createConsumer(topic);

			// Add the listener that handles received messages
			consumer.setMessageListener(new MessageDispatcher(eventSubscriber));

			// Store the consumer for disconnecting
			consumers.add(consumer);

			logger.debug("Subscribed to events on topic: '{}' with consumer: '{}'", topicName, consumer);
		} catch (JMSException e) {
			throw new RuntimeException("Could not subscribe to topic: '{}'", e);
		}
	}

	/**
	 * This handles messages received from JMS. It checks the message is valid, deserializes it and pushes it out to
	 * subscribers.
	 */
	private class MessageDispatcher implements MessageListener {

		private final EventSubscriber eventSubscriber;

		public MessageDispatcher(EventSubscriber eventSubscriber) {
			this.eventSubscriber = eventSubscriber;
		}

		@Override
		public void onMessage(final Message message) {
			// If its not a ObjectMessage fail
			if (!(message instanceof ObjectMessage)) {
				logger.error("Received unexpected message: '{}'", message);
				return; // We can't handle this message
			}
			// It is a ObjectMessage so cast it
			final ObjectMessage objectMessage = (ObjectMessage) message;

			// Get the data out of the message
			final Object serializedObject;
			final long sendingTimestamp;
			try {
				// Get the serialized object back out of the message
				serializedObject = objectMessage.getObject();

				// Get the sending timestamp
				sendingTimestamp = objectMessage.getJMSTimestamp();
			} catch (JMSException e) {
				logger.error("Error handling received message: '{}'", message, e);
				return; // We can't continue
			}

			if (!(serializedObject instanceof byte[])) {
				logger.error("Received a message that was not a serialized object");
				return; // We can't handle this case
			}
			// Deserialize back to the actual message object - Using the GDA serializer which can see the classes
			final Object messageObject = Serializer.toObject((byte[]) serializedObject);

			// Warn about receiving old messages. This suggests a communication issue or very high message rate.
			final long messageAgeMillis = System.currentTimeMillis() - sendingTimestamp;
			if (messageAgeMillis > MESSAGE_AGE_WARNING_MILLS) {
				logger.warn("Message received is older than {} ms. Age is: {} ms", MESSAGE_AGE_WARNING_MILLS, messageAgeMillis);
			}

			// Inform subscribers
			eventSubscriber.inform(messageObject);
		}
	}

	@Override
	public void disconnect() {
		logger.debug("Disconnecting...");
		for (MessageConsumer consumer : consumers) {
			try {
				logger.trace("Closing consumer: '{}'...", consumer);
				consumer.close();
			} catch (JMSException e) {
				logger.error("Error closing connection", e);
			}
		}
	}

}
