/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.eventbus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.io.Serializable;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.eventbus.api.IGDAEventBus;

import com.google.common.base.Objects;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * A Java-friendly route to a publish-subscribe message-oriented architecture
 * changes foreshadowed by Eclipse 4. For general EventBus rationale please read:
 * https://code.google.com/p/guava-libraries/wiki/EventBusExplained
 *
 * For intra-process event delivery GDAEventBus delegates to a Guava EventBus, as this
 * offers type-guarding and coercion (so code is navigable), allows flexible method naming,
 * and provides a mechanism for discovery of unhandled events.
 *
 * For inter-process communication
 * (e.g. between GDA clients and the server, Global Phasing's ASTRA project and GDA, or GDA and RESTful webservices for ISPyB/VMXi),
 * multiple GDAEventBus instances send AND consume JMS messages on an ActiveMQ topic "GDA",
 * sending each posted event out only and forwarding received messages to subscribers.
 * Having multiple GDAEventBus instances publishing on same topic means that subscribers to one
 * are subscribed to all.
 *
 * JavaDoc: http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/eventbus/EventBus.html
 *
 * AsyncEventBus: http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/eventbus/AsyncEventBus.html
 */
public class GDAEventBus extends EventBus implements IGDAEventBus {

	public static final Logger logger = LoggerFactory.getLogger(GDAEventBus.class);

	private static GDAEventBus INSTANCE;

	/**
	 * @return singleton shared by objects of the same classloader
	 */
	public static GDAEventBus getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GDAEventBus();
		}
		return INSTANCE;
	}

	private String identifier;
	private final EventBus delegate;
//	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
//	private String destinationName;
	private MessageConsumer consumer;
	private MessageProducer producer;

	public GDAEventBus(String identifier) {
		this.identifier = identifier;

		delegate = new EventBus(); //TODO use AsyncEventBus instead?
	}

	public GDAEventBus() {
		this("default");
	}

	public GDAEventBus(ConnectionFactory connectionFactory) {
		this("default", connectionFactory);
	}

	public GDAEventBus(ConnectionFactory connectionFactory, String destinationName) {
		this("default", connectionFactory, destinationName);
	}

	public GDAEventBus(ConnectionFactory connectionFactory, String destinationName, boolean isDestinationTopicElseQueue) {
		this("default", connectionFactory, destinationName, isDestinationTopicElseQueue);
	}

	public GDAEventBus(String identifier, ConnectionFactory connectionFactory) {
		this(identifier, connectionFactory, "GDA");
	}

	public GDAEventBus(String identifier, ConnectionFactory connectionFactory, String destinationName) {
		this(identifier, connectionFactory, destinationName, true);
	}

	public GDAEventBus(String identifier, ConnectionFactory connectionFactory, String destinationName, boolean isDestinationTopicElseQueue) {
		this(identifier);

		// create JMS components for forwarding GDAEventBus events to ActiveMQ
		// adapted from HelloWorld{Producer,Consumer} here: http://activemq.apache.org/hello-world.html
		try {
			// Create a Connection
			connection = connectionFactory.createConnection();
			connection.start();

			connection.setExceptionListener(new ExceptionListener() {
				@Override
				public synchronized void onException(JMSException e) {
					logger.error("JMS Exception occured.", e);
				}
			});

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = isDestinationTopicElseQueue ? session.createTopic(destinationName) : session.createQueue(destinationName);

			// Create a MessageProducer from the Session to the Topic or Queue
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a MessageConsumer from the Session to the Topic or Queue
			consumer = session.createConsumer(destination);

			// Forward messages to EventBus delegate
			consumer.setMessageListener(new ConsumerMessageListener());

			// Clean up
			// cleanUp(); //TODO when?
		} catch (JMSException e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Receives published messages and posts them to subscribers
	 */
	class ConsumerMessageListener implements MessageListener {
		@Override
		public void onMessage(Message message) {
			logger.debug("dequeued message");
			try {
				// publish via delegate
				ObjectMessage objectMessage = (ObjectMessage) message;
				Serializable event = objectMessage.getObject();
				post(event);
			} catch (JMSException e) {
				logger.error("could not deserialize message from ActiveMQ: {}", e);
			}
		}
	}

	@Override
	public void post(Object event) {
		logger.debug("posting event: {}", event);
		delegate.post(event);
	}

	/**
	 * Forwards messages to ActiveMQ
	 */
	@Override
	public void publish(Serializable event) {
		logger.debug("publishing event: {}", event);
		try {
			ObjectMessage message = session.createObjectMessage(event);
			producer.send(message);
			logger.debug("enqueued message");
		} catch (JMSException e) {
			logger.error("could not serialize or send message to ActiveMQ: {}", event, e);
		}
	}

	@Override
	public String identifier() {
		return this.identifier;
//		return delegate.identifier(); // only available in Guava > 16
	}

	@Override
	public void register(Object handler) {
		delegate.register(handler);
	}

	@Override
	public void unregister(Object handler) {
		delegate.unregister(handler);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.addValue(identifier)
			.toString();
//		return delegate.toString(); // does the same but with MoreObjects in Guava > 16
	}

	/**
	 * Uncover any event types without (registered) subscribers that
	 * are posted (or published) on a GDAEventBus, by (optionally)
	 * registering an instance of DefaultDeadEventHandler
	 * (or any object subscribing to DeadEvent).
	 */
	public static class DefaultDeadEventHandler {
		final Logger logger;
		final Set<Class<?>> ignored;
		public DefaultDeadEventHandler(Logger logger, Class<?>... ignored) {
			this.logger = checkNotNull(logger);
			this.ignored = checkNotNull(newHashSet(ignored));
		}
		@Subscribe public void logWarning(DeadEvent d) {
			if (!ignored.contains(d.getEvent().getClass()))
				logger.warn("unhandled event type {} posted by: {}", d.getEvent().getClass().getName(), d.getSource());
		}
	}

	public void cleanUp/*closeConnection*/() throws JMSException {
		if (producer != null) producer.close();
		if (consumer != null) consumer.close();
		if (session != null) session.close();
		if (connection != null) connection.close();
	}

	@Override
	public void setName(String name) {
		this.identifier = name;
	}

	@Override
	public String getName() {
		return this.identifier;
	}

}
