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

import java.io.Closeable;

import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.ServiceHolder;

/**
 * Base class implemented by classes that send and receive messages using JMS.
 */
public abstract class JmsClient implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(JmsClient.class);

	protected static final String TOPIC_PREFIX = "gda.event.";

	protected final Session session;

	protected JmsClient() {
		try {
			session = ServiceHolder.getSessionService().getSession();
		} catch (JMSException e) {
			logger.error("Failed to connect to ActiveMQ", e);
			throw new RuntimeException("Failed to connect to ActiveMQ, is it running?", e);
		}
		logger.info("Created new session: {}", session);
	}

	@Override
	public void close() {
		try {
			if (session != null) session.close();
		} catch (JMSException e) {
			throw new RuntimeException("Unable to close Session on ActiveMQConnection ", e);
		}
	}

}
