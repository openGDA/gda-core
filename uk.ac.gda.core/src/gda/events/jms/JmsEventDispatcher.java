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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

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

	/**
	 * If an event being dispatched is older than this value a warning will be logged
	 */
	private static final int QUEUE_TIME_WARNING_MS = 1000; // In ms

	// Maps to cache Topic and MessageProducer for performance
	private final ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, MessageProducer> publisherMap = new ConcurrentHashMap<>();

	/**
	 * Queue to hold messages waiting for dispatch - prevents publish blocking
	 */
	private final BlockingQueue<OutgoingEvent> outgoingMessageQueue = new LinkedBlockingQueue<>();

	/**
	 * The ThreadFactory used to create threads to handle the event dispatching task
	 */
	private final ThreadFactory threadFactory = runnable -> {
		final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setName("JmsEventDispatcher Thread");
		thread.setDaemon(true); // Make daemon so it won't block JVM shutdown
		// If any exceptions happen log them and then restart the dispatch thread.
		thread.setUncaughtExceptionHandler((t, e) -> {
			logger.error("THIS IS A FAKE MESSAGE!"); // This is needed as the first call to the logger here doesn't work! This message will not be logged.
			logger.error("Unhandled exception in dispatcher thread", e); // This one will be logged due to the fake one.
			logger.info("Restarting JmsEventDispatcher Thread");
			startDispatcherThread();
		});
		return thread;
	};

	/**
	 * The ExecutorService executing the dispatch task
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

	/**
	 * This is the task which actually dispatches the events.
	 */
	private final Runnable dispatcher = () -> {
		// While not interrupted i.e. run forever
		while(!Thread.currentThread().isInterrupted()) {
			try {
				// This blocks here waiting for stuff on the queue
				final OutgoingEvent event = outgoingMessageQueue.take();
				sendEvent(event);
			} catch (InterruptedException e) {
				// Re-interrupt to allow thread to end.
				Thread.currentThread().interrupt();
			}
		}
	};

	/**
	 * Creates a JMS event dispatcher and starts the dispatch thread.
	 */
	public JmsEventDispatcher() {
		// If the super constructor succeeds then we know the session was created
		logger.info("Created new session: {}", session);

		// Start the tread doing the dispatching.
		startDispatcherThread();
	}

	private void startDispatcherThread() {
		executorService.execute(dispatcher);
		logger.debug("Started event dispatch thread");
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This actually queues the message to be sent and will return immediately.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *             if the sourceName is null, or the message object is not {@link Serializable}
	 */
	@Override
	public void publish(final String sourceName, final Object message) {
		if (sourceName == null) {
			throw new IllegalArgumentException("sourceName cannot be null");
		}
		// If message is not null check if message is not Serializable and fail.
		if (message != null && !(message instanceof Serializable)) {
			throw new IllegalArgumentException(new NotSerializableException(message.getClass().getName()));
		}
		// Queue the message for dispatch
		outgoingMessageQueue.add(new OutgoingEvent(sourceName, message));
	}

	/**
	 * This actually sends the event. It serializes the event message object using the {@link Serializer}. This is then
	 * placed into a JMS {@link ObjectMessage} and sent. The topic used is determined from the sourceName in the event.
	 *
	 * @param event
	 *            The event to send
	 */
	private void sendEvent(final OutgoingEvent event) {
		// Unwrap the OutgoingEvent
		final String sourceName = event.getSourceName();
		final Object message = event.getMessage();
		final long timestamp = event.getTimestamp();

		// Check how long the event was in the queue and warn if longer than QUEUE_TIME_WARNING
		if (System.currentTimeMillis() - timestamp > QUEUE_TIME_WARNING_MS) {
			logger.warn("Event '{}' has waited longer than {} ms in the dispatch queue", event, QUEUE_TIME_WARNING_MS);
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
			// Add a sending timestamp to message - approximately when the event happened
			objectMessage.setJMSTimestamp(timestamp);

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

	/**
	 * Class for wrapping together a sourceName and message so it can be queued for dispatch.
	 * <p>
	 * It adds a timestamp in the constructor intended to be the time the event happened.
	 */
	private final class OutgoingEvent {
		private final String sourceName;
		private final Object message;
		private final long timestamp;

		public OutgoingEvent(String sourceName, Object message) {
			this.sourceName = sourceName;
			this.message = message;
			this.timestamp = System.currentTimeMillis();
		}

		public String getSourceName() {
			return sourceName;
		}

		public Object getMessage() {
			return message;
		}
		public long getTimestamp() {
			return timestamp;
		}

		@Override
		public String toString() {
			return "OutgoingEvent [sourceName=" + sourceName + ", message=" + message + ", timestamp=" + timestamp
					+ "]";
		}
	}

}
