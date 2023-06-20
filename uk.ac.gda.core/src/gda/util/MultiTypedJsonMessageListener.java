/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * This class is similar to {@link JsonMessageListener} but maintains a list of types
 * to attempt to deserialize to.
 */
public class MultiTypedJsonMessageListener {
	private static final Logger logger = LoggerFactory.getLogger(MultiTypedJsonMessageListener.class);

	/** Topic being listened to */
	private String topic;
	/** Listener connected to the server */
	private MessageConsumer consumer;

	private final List<TypeConsumer<?>> handlers = new ArrayList<>();

	/** Cached converter instance used for deserialization */
	private static final JsonMessageConverter converter = new JsonMessageConverter();

	/** Create a consumer to listen for activeMQ messages. If a consumer already exists, close existing connection */
	public void configure() throws JMSException {
		if (consumer != null) {
			shutdown();
		}
		if (topic == null) {
			throw new IllegalStateException("Cannot listen to null topic");
		}

		try {
			Destination destination = getTopic(topic);
			consumer = ServiceProvider.getService(ISessionService.class).getSession().createConsumer(destination);
			consumer.setMessageListener(this::handleMessage);
		} catch (IOException | TimeoutException e) {
			throw new IllegalStateException("Unable to listen to topic {} ");
		}
	}

	/** Close the connection and stop processing messages */
	public void shutdown() throws JMSException {
		if (consumer != null) {
			consumer.close();
		}
	}

	/** Handle messages from the bus by delegating to specific methods for different message types */
	private void handleMessage(Message message) {
		for (var item : handlers) {
			try {
				if (item.attemptDeserialzeAndConsume(message)) {
					break;
				}
			} catch (Exception e) {
				logger.error("Error handling message: {}", message, e);
			}
		}
	}

	/** Get the currently configure activeMQ topic */
	public String getTopic() {
		return topic;
	}

	/**
	 * Set the topic this listener listens to.
	 *
	 * If this listener is already configured, shutdown existing consumer and reconfigure with new topic.
	 *
	 * @throws JMSException
	 *             if either shutdown or configuration fail
	 */
	public void setTopic(String topic) throws JMSException {
		if (topic == null) {
			shutdown();
		} else if (!topic.equals(this.topic)) {
			this.topic = topic;
			configure();
		}
	}

	protected Destination getTopic(String topicName) throws IOException, TimeoutException {
		return ((Topic) () -> topicName);
	}

	public <T> void setHandler(Class<T> cls, Consumer<T> handler) {
		// TODO ensure only one handler per class?
		handlers.add(new TypeConsumer<>(cls, handler));
	}

	private record TypeConsumer<T>(Class<T> cls, Consumer<T> handler) {

		private T attemptDeserialize(Message message) {
			try {
				return converter.fromMessage(message, cls);
			} catch (JMSException e) {
				logger.error("Couldn't read message", e);
			} catch (JsonMessageConversionException e) {
				logger.trace("Couldn't parse JSON message into {} ({})", cls.getName(), message);
			}
			return null;
		}

		private boolean attemptDeserialzeAndConsume(Message message) {
			T deserialized = attemptDeserialize(message);
			if (deserialized == null) {
				return false;
			}
			handler.accept(deserialized);
			return true;
		}
	}
}
