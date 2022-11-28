/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.function.Consumer;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.ServiceHolder;

/**
 * <h1>Utility to listen for activeMQ messages</h1>
 *
 * Messages are expected to be JSON strings contained either as UTF-8 in a {@link BytesMessage} or
 * as text in a {@link TextMessage}. JSON is converted to the configured class and passed to
 * a configurable handler.
 * <p>
 * This allows the handling of the received message to unaware of its source and not have to
 * manage activeMQ connections.
 *
 * <h2>Example configuration</h2>
 * Configuration requires
 * <ul>
 * <li>a FQCN as a constructor argument. This is the class that messages are
 * expected to be.</li>
 * <li>The topic is the activeMQ topic that is used to broadcast the messaged.</li>
 * <li>The handler should be an instance of {@link Consumer} that accepts instances of
 * the class specified above.</li>
 * </ul>
 * <pre>
 * {@code
 * <bean id="processingListener" class="gda.util.MessageListener"
 *         c:type="org.opengda.lde.ReductionResponse">
 *     <property name="topic" value="gda.messages.calibration.xrd2" />
 *     <property name="handler" ref="processingHandler" />
 * </bean>
 * }
 * </pre>
 * @param <T> The class of objects that the this listener expects.
 */
public class JsonMessageListener<T> {
	private static final Logger logger = LoggerFactory.getLogger(JsonMessageListener.class);

	/** Topic being listened to */
	private String topic;
	/** ActiveMQ listener connected to the server */
	private MessageConsumer consumer;
	/** Class of objects expected */
	private Class<T> messageClass;
	/** Consumer of objects received by this listener */
	private Consumer<T> handler;

	/** Cached converter instance used for deserialization */
	private final JsonMessageConverter converter;

	public JsonMessageListener(Class<T> type) {
		messageClass = type;
		converter = new JsonMessageConverter();
	}

	/** Create a consumer to listen for activeMQ messages. If a consumer already exists, close existing connection */
	public void configure() throws JMSException {
		if (consumer != null) {
			shutdown();
		}
		if (topic == null) {
			throw new IllegalStateException("Cannot listen to null topic");
		}
		consumer = ServiceHolder.getSessionService().getSession().createConsumer(new ActiveMQTopic(topic));
		consumer.setMessageListener(this::handleMessage);
	}

	/** Close the activeMQ connection and stop processing messages */
	public void shutdown() throws JMSException {
		if (consumer != null) {
			consumer.close();
		}
	}

	/** Handle messaged from activeMQ by delegating to specific methods for different message types */
	private void handleMessage(Message message) {
		if (handler == null) {
			logger.warn("No handler set for message listener");
			return;
		}
		try {
			var deserialized = converter.fromMessage(message, messageClass);
			handler.accept(deserialized);
		} catch (JMSException e) {
			logger.error("Couldn't read message", e);
		} catch (JsonMessageConversionException e) {
			logger.error("Couldn't parse JSON message into {} ({})", messageClass.getName(), message, e);
		} catch (Exception e) {
			logger.error("Error handling message: {}", message, e);
		}
	}

	/** Get the currently configure activeMQ topic */
	public String getTopic() {
		return topic;
	}

	/**
	 * Set the topic this listener listens to.
	 *
	 * If this listener is already configured, shutdown existing consumer and reconfigure with
	 * new topic.
	 * @throws JMSException if either shutdown or configuration fail
	 */
	public void setTopic(String topic) throws JMSException {
		if (topic == null) {
			shutdown();
		} else if (!topic.equals(this.topic)) {
			this.topic = topic;
			configure();
		}
	}

	/**
	 * Set the handler for received messages.
	 * @param handler to accept instances of this listener's message type
	 */
	public void setHandler(Consumer<T> handler) {
		this.handler = handler;
	}

	/** Get the handler currently being used to handle messages */
	public Consumer<T> getHandler() {
		return handler;
	}

}
