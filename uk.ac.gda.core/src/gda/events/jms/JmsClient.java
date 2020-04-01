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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Base class implemented by classes that send and receive messages using JMS.
 */
public abstract class JmsClient {

	private static final Logger logger = LoggerFactory.getLogger(JmsClient.class);

	protected static final String TOPIC_PREFIX = "gda.event.";

	protected final Connection connection;

	protected final Session session;

	protected JmsClient() {
		final String jmsBrokerUri = LocalProperties.getActiveMQBrokerURI();
		final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(jmsBrokerUri);
		try {
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error("Failed to connect to ActiveMQ", e);
			throw new RuntimeException("Failed to connect to ActiveMQ, is it running?", e);
		}
	}

}
