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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Wrapper for a Connection to allow it to handle callbacks from its Sessions, allowing it to be closed once all
 * Sessions are closed.
 */
public class ConnectionWrapper implements Connection, Consumer<SessionWrapper> {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionWrapper.class);

	protected final Connection connection;
	private final String connectionUri;
	private final List<SessionWrapper> sessions = new ArrayList<>();
	private Consumer<ConnectionWrapper> closeListener;

	ConnectionWrapper(String connectionUri, Connection connection){
		this.connection = connection;
		this.connectionUri = connectionUri;
	}

	public String getConnectionUri() {
		return connectionUri;
	}

	@Override
	public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
		SessionWrapper session = new SessionWrapper(connection.createSession(transacted, acknowledgeMode));
		session.setCloseListener(this);
		sessions.add(session);
		return session;
	}

	@Override
	public String getClientID() throws JMSException {
		return connection.getClientID();
	}

	@Override
	public void setClientID(String clientID) throws JMSException {
		connection.setClientID(clientID);
	}

	@Override
	public ConnectionMetaData getMetaData() throws JMSException {
		return connection.getMetaData();
	}

	@Override
	public ExceptionListener getExceptionListener() throws JMSException {
		return connection.getExceptionListener();
	}

	@Override
	public void setExceptionListener(ExceptionListener listener) throws JMSException {
		connection.setExceptionListener(listener);
	}

	@Override
	public void start() throws JMSException {
		connection.start();
	}

	@Override
	public void stop() throws JMSException {
		connection.stop();
	}

	@Override
	public void close() throws JMSException {
		connection.close();
		closeListener.accept(this);
	}

	@Override
	public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		return connection.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
	}

	@Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName,
			String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		return connection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool,
				maxMessages);
	}

	/**
	 * Track Sessions that are active on this Connection, and close the Connection if none are any more.
	 */
	@Override
	public void accept(SessionWrapper t) {
		if (sessions.contains(t)) sessions.remove(t);
		else logger.error("Connection {} tried to remove Session {} but is not assosciated with it!", connection, t);
		if (sessions.isEmpty()) try {
			this.close();
		} catch (JMSException e) {
			logger.error("Unable to close connection on URI: {}", connectionUri, e);
		}
	}

	public void setCloseListener(Consumer<ConnectionWrapper> closeListener) {
		this.closeListener = closeListener;
	}

}
