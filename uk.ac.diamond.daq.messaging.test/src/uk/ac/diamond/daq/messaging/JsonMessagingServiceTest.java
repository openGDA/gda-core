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

package uk.ac.diamond.daq.messaging;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.api.messaging.Destination;
import uk.ac.diamond.daq.api.messaging.Message;
import uk.ac.diamond.daq.messaging.json.JsonMessagingService;

public class JsonMessagingServiceTest {

	private static final String TEST_TOPIC = "test.topic";

	private static final String TEST_TOPIC_ALTERNATIVE = "test.topic.alt";

	private JsonMessagingService jms;

	@BeforeClass
	public static void beforeClass() {
		LocalProperties.forceActiveMQEmbeddedBroker(); // Use in JVM broker for tests
	}

	@Before
	public void before() {
		jms = new JsonMessagingService();
		jms.activate();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSendingMessageWithoutDestinationThrows() {
		TestMessageWithoutDestination messageWithoutDestination = new TestMessageWithoutDestination(123);
		// Should throw
		jms.sendMessage(messageWithoutDestination);
	}

	@Test
	public void testSendingMessageWithoutDestinationWorksWhenDestinationIsSpecified() {
		TestMessageListener listener = setupListener(TEST_TOPIC);

		TestMessageWithoutDestination messageWithoutDestination = new TestMessageWithoutDestination(123);

		jms.sendMessage(messageWithoutDestination, TEST_TOPIC);

		// Wait for and get the message back
		String json = listener.getMessage();

		assertThat(json, is(equalTo("{\"id\":123}")));
	}

	@Test
	public void testSendingAndReceivingMessage() {
		TestMessageListener listener = setupListener(TEST_TOPIC);

		TestMessage message = new TestMessage(12345, "test message");

		jms.sendMessage(message);

		// Wait for and get the message back
		String json = listener.getMessage();

		assertThat(json, is(equalTo("{\"id\":12345,\"testMessage\":\"test message\"}")));
	}

	@Test
	public void testSendingAndReceivingMessageOverridingDestination() {
		TestMessageListener listener = setupListener(TEST_TOPIC_ALTERNATIVE);

		TestMessage message = new TestMessage(12345, "test message");

		jms.sendMessage(message, TEST_TOPIC_ALTERNATIVE);

		// Wait for and get the message back
		String json = listener.getMessage();

		assertThat(json, is(equalTo("{\"id\":12345,\"testMessage\":\"test message\"}")));
	}

	private TestMessageListener setupListener(String topic) {
		final String jmsBrokerUri = LocalProperties.getActiveMQBrokerURI();
		final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(jmsBrokerUri);
		try {
			Connection connection = factory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic jmsTopic = session.createTopic(topic);
			MessageConsumer consumer = session.createConsumer(jmsTopic);
			TestMessageListener listener = new TestMessageListener();
			consumer.setMessageListener(listener);
			connection.start();
			return listener;
		} catch (JMSException e) {
			throw new RuntimeException("Failed to connect to ActiveMQ, is it running?", e);
		}
	}

	private class TestMessageListener implements MessageListener {

		private final CountDownLatch latch = new CountDownLatch(1); // Used to allow getMessage() to block
		private String text;

		@Override
		public void onMessage(javax.jms.Message message) {
			try {
				// Assume it a text message
				text = ((TextMessage) message).getText();
			} catch (JMSException e) {
				throw new RuntimeException("Failed to get message text", e);
			}
			// release the latch
			latch.countDown();
		}

		public String getMessage() {
			try {
				// Wait for the message to be received should be fast <<1 sec so this is a very generous timeout.
				if(latch.await(30, SECONDS)) {
					return text;
				} else {
					throw new RuntimeException("Timed out waiting for message");
				}
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted waiting for message", e);
			}
		}
	}

	@Destination(TEST_TOPIC)
	private class TestMessage implements Message {

		private final int id;
		private final String testMessage;

		public TestMessage(int id, String testMessage) {
			this.id = id;
			this.testMessage = testMessage;
		}

		@SuppressWarnings("unused") // For JSON serialization
		public int getId() {
			return id;
		}

		@SuppressWarnings("unused") // For JSON serialization
		public String getTestMessage() {
			return testMessage;
		}

	}

	private class TestMessageWithoutDestination implements Message {
		private final int id;

		public TestMessageWithoutDestination(int id) {
			this.id = id;
		}

		@SuppressWarnings("unused") // For JSON serialization
		public int getId() {
			return id;
		}
	}
}
