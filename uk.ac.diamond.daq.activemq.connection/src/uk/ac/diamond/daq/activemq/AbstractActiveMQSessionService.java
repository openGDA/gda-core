package uk.ac.diamond.daq.activemq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@code ISessionService}. Subclasses should implement getConnection/getQueueConnection,
 * making calls to createConnection/createQueueConnection with some form of management, and accept(ConnectionWrapper),
 * which is called when the wrapped Connection is closed.
 */
public abstract class AbstractActiveMQSessionService implements ISessionService, Consumer<ConnectionWrapper> {

	private static final String GDA_SERVER_HOST = "GDA/gda.activemq.broker.uri";
	private static final String GDA_SERVER_HOST_DEFAULT = "GDA/gda.server.host";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/*
	 * Management or control of Connections should be implemented in this method
	 */
	protected abstract ConnectionWrapper getConnection(String brokerUri);

	protected final ConnectionWrapper createConnection(String brokerUri) {
		try {
			final Connection connection = primedFactory(brokerUri).createConnection();
			return wrapConnection(new ConnectionWrapper(brokerUri, connection));
		} catch (JMSException e) {
			// NPE thrown by getSession if return null, will fail fast, catch exception for method reference
			logger.error("Error establishing connection for URI {}", brokerUri, e);
			return null;
		}
	}

	/*
	 * Management or control of QueueConnections should be implemented in this method
	 */
	protected abstract QueueConnectionWrapper getQueueConnection(String brokerUri);

	protected final QueueConnectionWrapper createQueueConnection(String brokerUri) {
		try {
			final QueueConnection connection = primedFactory(brokerUri).createQueueConnection();
			return wrapConnection(new QueueConnectionWrapper(brokerUri, connection));
		} catch (JMSException e) {
			// NPE thrown by getQueueSession if return null, will fail fast, catch exception for method reference
			logger.error("Error establishing queue connection for URI {}", brokerUri, e);
			return null;
		}
	}

	private ActiveMQConnectionFactory primedFactory(String brokerUri) {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUri);
		connectionFactory.setTrustAllPackages(true);
		return connectionFactory;
	}

	private <T extends ConnectionWrapper> T wrapConnection(T connection) throws JMSException {
		connection.setExceptionListener(
				(JMSException e) -> logger.error("{} Connection error!", connection.getConnectionUri(), e));
		connection.start();
		connection.setCloseListener(this);
		return connection;
	}

	@Override
	public final Session getSession() throws JMSException {
		// Duplicated logic from LocalProperties as cannot depend on it
		final String URI = System.getProperty(GDA_SERVER_HOST,
				String.format("failover:(tcp://%s:%d?daemon=true)?startupMaxReconnectAttempts=3",
						System.getProperty(GDA_SERVER_HOST_DEFAULT, "localhost"), 61616));
		return getSession(URI, false, Session.AUTO_ACKNOWLEDGE);
	}

	@Override
	public final Session getSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException {
		return getConnection(brokerUri).createSession(transacted, acknowledgeMode);
	}

	@Override
	public final QueueSession getQueueSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException {
		return getQueueConnection(brokerUri).createQueueSession(transacted, acknowledgeMode);
	}

	// Below moved from MsgBus
	// Using a java property here as the formatting may be easier to handle than for a Spring property
	private static final String KEY_GETSTATUS_ACTIVE_URI = "GDA/gda.activemq.broker.status.uri";
	private static final String KEY_GETSTATUS_ACTIVE_PROPERTY = "GDA/gda.activemq.broker.status.key.active";
	private static final String KEY_GETSTATUS_ACTIVE_DEFAULT = "activemq.apache.org";

	/** @return raw status response string for parsing */
	private String fetchActiveMQStatus() throws IOException {
		// Lazily get system property to ensure is set
		final String brokerUri = System.getProperty(KEY_GETSTATUS_ACTIVE_URI,
				String.format("http://%s:8161", System.getProperty(GDA_SERVER_HOST)));
		final URL url = new URL(brokerUri);
		URLConnection conn = url.openConnection();
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining());
		}
	}

	/** @return flag for ActiveMQ response indicating whether server is active, moved from MsgBus */
	@Override
	public final boolean defaultConnectionActive() {
		boolean response = false;
		try {
			String status = fetchActiveMQStatus();
			response = status.contains(System.getProperty(KEY_GETSTATUS_ACTIVE_PROPERTY, KEY_GETSTATUS_ACTIVE_DEFAULT));
		} catch (IOException e) {
			logger.error("FAIL to verify ActiveMQ status", e);
		}
		return response;
	}

}
