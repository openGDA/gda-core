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

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.eventbus.api.IGDAEventBus;
import ch.qos.logback.classic.Level;

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
		
		// publish(Serializable event) { producer.send(ObjectMessage message) }
		
		// ConsumerMessageListener.onMessage(Message message) { post(((ObjectMessage) message).getObject()) }
		
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

	/* (non-Javadoc)
	 * @see uk.ac.gda.eventbus.IGDAEventBus#post(java.lang.Object)
	 */
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
			ObjectMessage message = session.createObjectMessage((Serializable) event);
			producer.send(message);
			logger.debug("enqueued message");
		} catch (JMSException e) {
			logger.error("could not serialize or send message to ActiveMQ: {}", event, e);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.gda.eventbus.IGDAEventBus#identifier()
	 */
	@Override
	public String identifier() {
		return this.identifier;
//		return delegate.identifier(); // only available in Guava > 16
	}

	/* (non-Javadoc)
	 * @see uk.ac.gda.eventbus.IGDAEventBus#register(java.lang.Object)
	 */
	@Override
	public void register(Object handler) {
		delegate.register(handler);
	}

	/* (non-Javadoc)
	 * @see uk.ac.gda.eventbus.IGDAEventBus#unregister(java.lang.Object)
	 */
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

	@SuppressWarnings("unused")
	private static void example() {
		GDAEventBus bus = new GDAEventBus(); // identifier "default"
		
		// register a good old anonymous inner class for a simple handler
		bus.register(new Object() {
			@Subscribe
			public void openingGambit(String s) {
				logger.info("read " + (s.trim().split("\\s").length == 1 ? "single " : "multi-") + "word String: \"{}\"", s);
			}
		});
		
		// handlers can subscribe to multiple event types/subtypes
		class NumericEventHandler<T> { // parameterised for demonstration purposes
			@Subscribe
			public void lessThanZero(Integer i) {
				logger.info("determined Integer to be " + (i < 0 ? "negative" : "positive") + ": {}", i);
			}
			@Subscribe
			public void lessThanZero(Double d) {
				logger.info("determined Double to be " + (d < 0 ? "negative" : "positive") + ": {}", d);
			}
			@Subscribe
			public void whichClass(T n) { // https://github.com/google/guava/issues/1549 ?
				logger.debug("passed object of type " + n.getClass().getName() + ": {}", n);
			}
		}
		
		// subscribe to Object to receive all events
		class AnyEventHandler {
			@Subscribe
			public void anyAndAll(Object o) {
				logger.debug("saw {}: {}", o.getClass().getName(), o.toString());
			}
		}
		// but keep a reference in case you need to unregister later
		final AnyEventHandler anyEventHandler = new AnyEventHandler();
		bus.register(anyEventHandler);
		
		// handle DeadEvent to discover all events without a receiver
		class DeadEventHandler {
			@Subscribe
			public void wentInWater(DeadEvent d) {
				logger.warn("discovered unhandled event of type {} posted by: {}", d.getEvent().getClass().getName(), d.getSource());
			}
		}
		
		// can register some handlers dynamically as needed
		bus.register(new DeadEventHandler());
		// having left parameterising generics until it is unavoidable
		bus.register(new NumericEventHandler<Number>());
		
		// and don't forget to unregister unnecessary handlers
		bus.unregister(anyEventHandler);
		
		bus.post(1);
		bus.post(-2.0);
		bus.post("Three little birds");
	}

	public static void main(String[] args) throws Exception {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.activemq.transport")).setLevel(Level.INFO);
		((ch.qos.logback.classic.Logger) logger).setLevel(Level.INFO);
//		example();
		testActiveMq();
	}

	private static void testActiveMq() throws InterruptedException, JMSException {
		
		// create
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		GDAEventBus eventBus1 = new GDAEventBus("eventBus1", connectionFactory);
		GDAEventBus eventBus2 = new GDAEventBus("eventBus2", connectionFactory);
//		eventBus2 = eventBus1; // can be the same!
		
		// ready
		Object handler = new Object() {
			@Subscribe
			public void print(Object event) {
				logger.debug("received event: {}", event);
				System.out.println(event);
			}
		};
		
		// set
		eventBus1.register(handler);
		eventBus2.register(handler);
		
		// go
		eventBus1.post("String from eventBus1");
		eventBus2.publish("String from eventBus2");
		eventBus1.publish(1);
		eventBus2.post(2);
		
		// wait expectantly
		Thread.sleep(3000);
		
		eventBus1.cleanUp();
		eventBus2.cleanUp();
	}
		
	private void cleanUp() throws JMSException {
		producer.close();
		consumer.close();
		session.close();
		connection.close();
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
