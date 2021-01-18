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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Session;

/**
 * Implementation of {@code ISessionService} which tracks Connections and QueueConnections in a static map, minimising
 * the number of {@code Connection}s which are created.
 */
public class StaticMapManagedSessionService extends AbstractActiveMQSessionService {

	/**
	 * Map of brokerUri to Connections, wrapped to allow callbacks from {@link Session#close()} method from
	 * SessionWrappers created by the ConnectionWrapper to allow Connections to be automatically closed.
	 */
	private static final Map<String, ConnectionWrapper> connections = new ConcurrentHashMap<>();
	private static final Map<String, QueueConnectionWrapper> queueConnections = new ConcurrentHashMap<>();

	/**
	 * If a Connection of the brokerUri exists, should return a Session on that Connection, otherwise create a new
	 * Connection, store a reference to it and return a Session on this new Connection
	 */
	@Override
	protected ConnectionWrapper getConnection(String brokerUri) {
		return connections.computeIfAbsent(brokerUri, this::createConnection);
	}

	/**
	 * If a QueueConnection of the brokerUri exists, should return a QueueSession on that QueueConnection, otherwise
	 * create a new QueueConnection, store a reference to it and return a QueueSession on this new QueueConnection
	 */
	@Override
	protected QueueConnectionWrapper getQueueConnection(String brokerUri) {
		return queueConnections.computeIfAbsent(brokerUri, this::createQueueConnection);
	}

	/**
	 * Removes a Connection when it is closed, so that we do not return new Sessions on a closed Connection
	 */
	@Override
	public void accept(ConnectionWrapper t) {
		if (t instanceof QueueConnectionWrapper) {
			if (queueConnections.containsValue(t))
				queueConnections.remove(t.getConnectionUri());
			else
				logger.error("QueueConnection {} requested close but not managed by this service!", t);
		} else if (connections.containsValue(t))
			connections.remove(t.getConnectionUri());
		else
			logger.error("Connection {} requested close but not managed by this service!", t);
	}

}
