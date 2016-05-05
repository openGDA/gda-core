/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.msgbus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gda.configuration.properties.LocalProperties;

/**
 * Eagerly-initialised singleton (per-process but linked by JMS destination).
 *
 */
public enum MsgBus {
	INSTANCE;

	private final Logger logger = LoggerFactory.getLogger(MsgBus.class.getSimpleName()+"/"+ManagementFactory.getRuntimeMXBean().getName()); // static precluded by use in constructor (of enum)

	public static final String BROKER_REQUIRED_PROPERTY = "msgbus.broker.require";

	/**
	 * Use Guava [Async]EventBus to provide typed msg delivery WITHIN THIS PROCESS ONLY
	 * (and null-safe management of subscriptions)
	 */
	private final EventBus eventBus;
	private final SubscriberExceptionHandler subscriberExceptionHandler = new SubscriberExceptionHandler() {
		@Override public void handleException(Throwable exception, SubscriberExceptionContext context) {
			logger.error("exception thrown from @Subscribe method {}.{}({})",
					context.getSubscriber().getClass().getName(),
					context.getSubscriberMethod().getName(),
					context.getEvent().getClass().getName(),
					exception);
		}
	};

	// JMS
	private Connection connection;
	private final ExceptionListener connectionExceptionListener = new ExceptionListener() {
		@Override public synchronized void onException(JMSException e) {
			logger.error("connection problem", e);
		}
	};
	private Session session;
	private MessageConsumer consumer;
	private MessageProducer producer;

	private ExecutorService threadPool;

	/**
	 * Private constructor prevents sub-classing.
	 */
	private MsgBus() {

		threadPool = Executors.newCachedThreadPool();
		eventBus = new AsyncEventBus(threadPool, subscriberExceptionHandler);
//		eventBus = new EventBus(subscriberExceptionHandler);

		try {
			// Connection
			final String brokerUri = LocalProperties.getActiveMQBrokerURI();
			logger.debug("connecting to ActiveMQ broker {}", brokerUri);
			final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUri);
			connection = connectionFactory.createConnection();
//			connection.setClientID("TODO");
			connection.start();
			connection.setExceptionListener(connectionExceptionListener);

			// All published messages will be sent to all topic consumers,
			// i.e. MsgBus instances, including this
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			String topic = MsgBus.class.getName(); //FIXME something shorter like GDA:MsgBus
			Destination destination = session.createTopic(topic);

			// Sending
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Receiving and delivery
			consumer = session.createConsumer(destination); // receive
			consumer.setMessageListener(new PostsPublished()); // deliver
			logger.info("receiving published msgs on topic {}", topic); // explain

//			addShutdownHook(); // creates exceptions in JUnit tests
		}
		catch (JMSException e) {
			logger.error("problem instantiating MsgBus singleton", e);
			if (LocalProperties.check(BROKER_REQUIRED_PROPERTY, true)) {
				throw new ExceptionInInitializerError(e);
			}
		}
	}

	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				logger.warn("shutting down ...");
				INSTANCE._shutdown();
				logger.info("shut down");
			}
		});
	}

	//// public static API (and corresponding private Singleton implementation)

	/**
	 * Send an object (which need not implement Msg/Serializable)
	 * to reqistered subscribers WITHIN THIS PROCESS ONLY.
	 *
	 * Logs what is posted.
	 *
	 * @throws IllegalArgumentException if argument is null.
	 */
	public static void post(Object obj) {
		if (obj == null) throw new IllegalArgumentException("attempt to post null caught");
		INSTANCE.logger.debug("posting {}", obj);
		INSTANCE._post(obj);
	}

	/**
	 * In contrast to _publish this deliberate leaves logging
	 * the action of posting to the public static post method,
	 * so that postPublished can reuse it without double-logging
	 * published msgs that will...hang on:
	 * publish logs messages on the way out
	 * post logs then on the way in, but only if they were published
	 *
	 * @throws IllegalArgumentException if argument is null.
	 */
	private void _post(Object obj) {
		eventBus.post(obj);
	}

	/**
	 * Convenience class allowing import static ...MsgBus.*; of Serializable.
	 *
	 * Consistent usage should help others find and reuse your Msg sub-types.
	 */
	public static abstract class Msg implements Serializable {

		private static final long serialVersionUID = -4027759110290406614L;

		transient private final UUID id = UUID.randomUUID();

		/**
		 * Message ID, unique to inheriting instances.
		 */
		public final long mid = id.getMostSignificantBits();

		/**
		 * Correlation ID, for matching responses to requests.
		 */
		public final long cid = id.getLeastSignificantBits();

	}
//	/**
//	 * Convenience class allowing import static ...MsgBus.*; of Serializable.
//	 */
//	public static abstract class Msg extends uk.ac.diamond.daq.msgbus.Msg {}

	public static void publishAsJson(Serializable msg) { //TODO enforce Msg?
		if (msg == null) throw new IllegalArgumentException("attempt to publish null caught");
		INSTANCE._publishAsJson(msg);
	}

	private void _publishAsJson(Serializable msg) {
		//FIXME same problem and fix as described in _publish
		try {
			logger.debug("publishing {} as JSON:", msg);
			String json = gson.get().toJson(msg);
			logger.debug(json);
			final TextMessage message = session.createTextMessage(json);
			final String className = msg.getClass().getCanonicalName();
			logger.debug("setting string property = {}", className);
			message.setStringProperty("className", className);
			producer.send(message);
		} catch (JMSException e) {
			logger.error("JMSException while publishing {}", msg, e);
		}
	}

	private Supplier<Gson> gson =
//		Suppliers.memoize(
			new Supplier<Gson>() {
				@Override
				public Gson get() {
					return new GsonBuilder()
						.registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY)
						.setPrettyPrinting()
						.serializeSpecialFloatingPointValues() // handle Infinity
						.create();
				}
			}
//		)
	;

	/**
	 * Send a Msg object to all consumers of topic,
	 * i.e. each process's MsgBus singleton,
	 * which on receiving it will then post it to all registered
	 * subscribers with an @Subscribe annotated method accepting
	 * arguments of that msg type.
	 *
	 * @throws IllegalArgumentException if argument is null.
	 */
	public static void publish(Serializable msg) { //TODO enforce Msg?
		if (msg == null) throw new IllegalArgumentException("attempt to publish null caught");
		INSTANCE._publish(msg);
	}

	private void _publish(Serializable msg) {
		//FIXME publishing a msg that has just been posted on the same EventBus can cause an infinite loop!
		// A possible solution (which may have its own drawbacks) would be to cache the last few msgs
		// posted on this EventBus in a [Concurrent]HashSet and NOT publish this msg arg if the set contains it.
		try {
			logger.debug("publishing {}", msg);
			final ObjectMessage message = session.createObjectMessage(msg);
			producer.send(message);
		} catch (JMSException e) {
			logger.error("JMSException while publishing {}", msg, e);
		}
	}

	/** Published messages go out to and come in from ActiveMQ,
	 * whereupon we post them on our EventBus:
	 */
	//private final MessageListener postsPublished = new MessageListener() {
	private class PostsPublished implements MessageListener {
		@Override public void onMessage(Message message) {
			try {
				Object msg = null;
				if (message instanceof ObjectMessage) {
					msg = ((ObjectMessage) message).getObject();
					if (msg == null) {
						logger.warn("discarding object: null"); // almost definitely not published by MsgBus
						return;
					}
				}
				else if (message instanceof TextMessage) {
					final String text = ((TextMessage) message).getText();
					final String className = message.getStringProperty("className");
					if (className == null) {
						logger.info("missing string property 'className' for text message");
						logger.info("cannot recreate object from text without class information");
						logger.warn("discarding text:\n{}", text);
						return;
					}
					else{
						logger.info("trying to recreate {} object from text message", className);
						try {
							Class<?> clazz = Class.forName(className);
							logger.info("loaded Class {}", clazz.getCanonicalName());
							msg = gson.get().fromJson(text, clazz);
							logger.info("successfully recreated {}", msg);
						} catch (ClassNotFoundException e) {
							logger.error("failed to recreate object from text message");
							logger.warn("discarding text:\n{}", text);
							return;
						}
					}
				}
				logger.trace("posting published {}", msg);
				INSTANCE._post(msg);
			} catch (JMSException e) {
				logger.error("could not deserialize msg object from ActiveMQ message", e);
			}
		}
	}

	/**
	 * Register subscriber object to receive deserialized msg objects
	 * in zero or more of its @Subscribe annotated methods
	 * depending on msg type.
	 *
	 * @throws IllegalArgumentException if argument is null.
	 */
	public static void subscribe(Object subscriber) {
		if (subscriber == null) {
			throw new IllegalArgumentException("attempt to subscribe null caught");
		}
		INSTANCE._subscribe(subscriber);
	}

	private void _subscribe(Object subscriber) {
		logger.trace("subscribing {}", subscriber);
		eventBus.register(subscriber);
	}

	/**
	 * Deregister subscriber object from receiving msg objects.
	 *
	 * @throws IllegalArgumentException if argument is null.
	 */
	public static void unsubscribe(Object subscriber) {
		if (subscriber == null) {
			throw new IllegalArgumentException("attempt to unsubscribe null caught");
		}
		INSTANCE._unsubscribe(subscriber);
	}

	private void _unsubscribe(Object subscriber) {
		logger.trace("unsubscribing {}", subscriber);
		eventBus.unregister(subscriber);
	}

	/**
	 * Useful to discover which msg types are not handled by any subscribers
	 * WITHIN THIS PROCESS ONLY (so only really useful for testing). e.g.:
	 * <pre>
	 * // assuming import static ...MsgBus.*;
	 * subscribe(new LoggingDeadEventHandler(logger, Ig.class, Nored.class));
	 * </pre>
	 */
	public static class LoggingDeadEventHandler {
		final Logger logger;
		final Set<Class<?>> ignored;
		public LoggingDeadEventHandler(Logger logger, Class<?>... toIgnore) {
			this.logger = checkNotNull(logger);
			this.ignored = checkNotNull(newHashSet(toIgnore));
		}
		@Subscribe
		public void logWarning(DeadEvent d) {
			if (!ignored.contains(d.getEvent().getClass())) {
				logger.warn("unhandled event type {} posted by: {}", d.getEvent().getClass().getName(), d.getSource());
			}
		}
	}

	public void _shutdown() {
		logger.debug("stopping...");

		try {
//			consumer.setMessageListener(null);

//			if (consumer != null) consumer.close();
//			if (producer != null) producer.close();
//			if (session != null) session.close();
			if (connection != null) connection.close(); //((ActiveMQConnection) connection).close();
//			if (connection != null) connection.stop();

//			consumer = null;
//			postsPublished = null;
//			producer = null;
//			session = null;
		} catch (JMSException e) {
			logger.error("problem shutting down broker connection", e);
		}

//		try {
			if (threadPool != null) {
				threadPool.shutdown();
//				threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
//				threadPool.shutdownNow(); //TODO necessary?
			}
//			eventBus = null;
//		} catch (InterruptedException e) {
//			logger.error("problem shutting down bus threads", e);
//		}

		logger.debug("stopped");
	}

	public static void main(String[] args) {
		// empty main to enable Eclipse's Export to Runnable JAR for hands-on Jython tests
	}

}
