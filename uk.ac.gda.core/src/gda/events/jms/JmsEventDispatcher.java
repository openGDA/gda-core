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

import static javax.jms.DeliveryMode.NON_PERSISTENT;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.events.EventDispatcher;
import gda.util.Serializer;
import uk.ac.diamond.daq.concurrent.ExecutorFactory;

/**
 * An {@link EventDispatcher} that uses JMS to dispatch events. This class was significantly rewritten as part of DAQ-515
 *
 * @author James Mudd
 */
public class JmsEventDispatcher extends JmsClient implements EventDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(JmsEventDispatcher.class);

	/** If the serialized object being sent is larger than this size a warning will be logged. Default is 10 MB */
	private static final int SERIALIZED_OBJECT_SIZE_WARNING_BYTES = LocalProperties.getAsInt("gda.events.jms.eventSizeWarning", 10_000_000);

	/** If an event being dispatched is older than this value a warning will be logged */
	private static final long QUEUE_TIME_WARNING_MS = 1000L; // 1 sec

	/** The time after which undelivered events will be discarded */
	private static final long MESSAGE_EXPIRATION_TIME_MS = 15 * 60 * 1000L; // 15 mins

	/** Map to cache MessageProducers for performance */
	private final ConcurrentMap<String, MessageProducer> sourceToPublisherMap = new ConcurrentHashMap<>();

	/** The ExecutorService executing the dispatch task */
	private final ExecutorService executorService = ExecutorFactory.singleThread(JmsEventDispatcher.class.getSimpleName());

	/**
	 * Creates a JMS event dispatcher
	 */
	public JmsEventDispatcher() {
		// If the super constructor succeeds then we know the session was created
		logger.info("Created new session: {}", session);
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

		// Build the event object
		final OutgoingEvent event = new OutgoingEvent(sourceName, (Serializable) message);

		// Queue the message for dispatch. Use execute not submit to allow uncaught exception handling to work.
		executorService.execute(new SendMessageRunnable(event));
	}

	/**
	 * Class for wrapping together a sourceName and message so it can be queued for dispatch.
	 * <p>
	 * It adds a timestamp in the constructor intended to be the time the event happened.
	 */
	private final class OutgoingEvent {
		private final String sourceName;
		private final Serializable message;
		private final long timestamp;

		public OutgoingEvent(String sourceName, Serializable message) {
			this.sourceName = sourceName;
			this.message = message;
			this.timestamp = System.currentTimeMillis();
		}

		public String getSourceName() {
			return sourceName;
		}

		public Serializable getMessage() {
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

	/**
	 * This is the task which actually dispatches the events.
	 */
	private class SendMessageRunnable implements Runnable {
		private final OutgoingEvent event;

		public SendMessageRunnable(OutgoingEvent event) {
			this.event = event;
		}

		/**
		 * This actually sends the event. It serializes the event message object using the {@link Serializer}. This is then
		 * placed into a JMS {@link ObjectMessage} and sent. The topic used is determined from the sourceName in the event.
		 */
		@Override
		public void run() {
			// Unwrap the OutgoingEvent
			final String sourceName = event.getSourceName();
			final Serializable message = event.getMessage();
			final long timestamp = event.getTimestamp();

			// Check how long the event was in the queue and warn if longer than QUEUE_TIME_WARNING_MS
			final long delay = System.currentTimeMillis() - timestamp;
			if (delay > QUEUE_TIME_WARNING_MS) {
				logger.warn("Event '{}' has waited {}ms in the dispatch queue (above {}ms warning threadshold)",
						event,
						delay,
						QUEUE_TIME_WARNING_MS);
			}

			try {
				// Get the publisher
				final MessageProducer publisher = getPublisher(sourceName);

				// Serialize the message - here we used the GDA Serializer as it can see the required classes
				// ActiveMQ could serialize for us but then ActiveMQ needs to be able to see all the classes
				// we might want to deserialize into.
				final byte[] serializedObject = Serializer.toByte(message);
				// Check the size of the object to be sent is reasonable
				if (serializedObject.length > SERIALIZED_OBJECT_SIZE_WARNING_BYTES) {
					logger.warn("Sending large object. '{}' is {} bytes.", message, serializedObject.length);
				}
				// Make a object message containing the serialized message object
				final ObjectMessage objectMessage = session.createObjectMessage(serializedObject);
				// Add a sending timestamp to message - approximately when the event happened
				objectMessage.setJMSTimestamp(timestamp);

				// Send the message
				publisher.send(objectMessage);

			// Catch RuntimeException here as it used to wrap JMSException
			} catch (RuntimeException e) {
				// Check if the RuntimeException is wrapping a JMSException. If it is log the JMSException.
				if (e.getCause() instanceof JMSException ){
					logger.error("Unable to dispatch message from sourceName: {}", sourceName, e.getCause());
				} else {
					// It's a RuntimeException that's NOT wrapping a JMSException so just rethrow it.
					throw e;
				}
			} catch (JMSException | IOException e) {
				logger.error("Unable to dispatch message from sourceName: {}", sourceName, e);
			}
		}

		private MessageProducer getPublisher(final String sourceName) {
			// If we have already created a publisher for this topic just return it, else create one
			return sourceToPublisherMap.computeIfAbsent(sourceName, key -> {
				logger.trace("Creating publisher for topic: {}", key);
				try {
					final Topic topic = session.createTopic(TOPIC_PREFIX + key);
					final MessageProducer publisher = session.createProducer(topic);

					// Use non-persistent we don't want events to survive a ActiveMQ broker restart
					publisher.setDeliveryMode(NON_PERSISTENT);

					// We want to ensure events don't last too long
					publisher.setTimeToLive(MESSAGE_EXPIRATION_TIME_MS);

					return publisher;
				} catch (JMSException e) {
					// Wrap with RuntimeException will be unwrapped and handled later
					throw new RuntimeException("Error creating publisher for topic: " + key, e);
				}
			});
		}

		@Override
		public String toString() {
			return "SendMessageRunnable [event.sourceName=" + event.sourceName + ", event.message=" + event.message + "]";
		}

	}

}
