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

package uk.ac.diamond.daq.activemq;

import static gda.configuration.properties.LocalProperties.GDA_SERVER_HOST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

public class ActiveMQSessionService implements ISessionService, Consumer<ConnectionWrapper> {

	private static final Logger logger = LoggerFactory.getLogger(ActiveMQSessionService.class);
	/**
	 * Map of brokerUri to Connections, wrapped to allow callbacks from {@link Session#close()} method from Sessions
	 * created by it to allow Connections to be automatically closed.
	 */
	private static final Map<String, ConnectionWrapper> connections = new ConcurrentHashMap<>();


	/**
	 * If a Connection of the brokerUri exists, returns a new Session on that connection, otherwise creates a new Connection and
	 * returns a Session on that Connection.
	 * @see Connection#createSession(boolean transacted, int acknowledgeMode)
	 */
	@Override
	public Session getSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException {
		ConnectionWrapper connection = connections.computeIfAbsent(brokerUri, this::createConnection);
		return connection.createSession(transacted, acknowledgeMode);
	}

	private ConnectionWrapper createConnection(String brokerUri) {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUri);
			connectionFactory.setTrustAllPackages(true);
			Connection connection = connectionFactory.createConnection();
			connection.setExceptionListener((JMSException e) -> logger.error("{} Connection error!", brokerUri, e));
			connection.start();
			return new ConnectionWrapper(brokerUri, connection);
		} catch (JMSException e) {
			// NPE thrown by getSession if return null, will fail fast
			logger.error("Error establishing connection for URI {}", brokerUri, e);
			return null;
		}
	}

	@Override
	public Session getSession() throws JMSException {
		return getSession(LocalProperties.getActiveMQBrokerURI(), false, Session.AUTO_ACKNOWLEDGE);
	}

	// Below moved from MsgBus
	// Using a java property here as the formatting may be easier to handle than for a Spring property
	public static final String KEY_GETSTATUS_ACTIVE_URI = "gda.activemq.broker.status.uri";
	public static final String GETSTATUS_ACTIVE_URI_DEFAULT = String.join("", "http://", LocalProperties.get(GDA_SERVER_HOST), ":8161");
	public static final String KEY_GETSTATUS_ACTIVE_PROPERTY = "gda.activemq.broker.status.key.active";
	public static final String KEY_GETSTATUS_ACTIVE_DEFAULT = "activemq.apache.org";
	public static final String KEY_GETSTATUS_ACTIVE = LocalProperties.get(KEY_GETSTATUS_ACTIVE_PROPERTY, KEY_GETSTATUS_ACTIVE_DEFAULT);

	/** @return raw status response string for parsing */
	public static String fetchActiveMQStatus() throws IOException {
		final String brokerUri = LocalProperties.get(KEY_GETSTATUS_ACTIVE_URI, GETSTATUS_ACTIVE_URI_DEFAULT);
		final URL url = new URL(brokerUri);
		URLConnection  conn = url.openConnection();
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining());
		}
	}

	/** @return flag for ActiveMQ response indicating whether server is active, moved from MsgBus */
	@Override
	public boolean defaultConnectionActive() {
		boolean response = false;
		try {
			String status = fetchActiveMQStatus();
			response = status.contains(KEY_GETSTATUS_ACTIVE);
		} catch (IOException e) {
			logger.error("FAIL to verify ActiveMQ status", e);
		}
		return response;
	}

	/**
	 * Removes a Connection when it is closed, so that we do not return new Sessions on a closed Connection
	 */
	@Override
	public void accept(ConnectionWrapper t) {
		if (connections.containsValue(t)) connections.remove(t.getConnectionUri());
		else logger.error("Connection {} requested close but not managed by this service!", t);
	}

}
