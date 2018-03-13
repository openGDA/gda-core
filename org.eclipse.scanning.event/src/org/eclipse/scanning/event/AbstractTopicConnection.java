/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event;

import java.net.URI;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.ITopicConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass for objects that publish or subscribe to a topic.
 */
public abstract class AbstractTopicConnection extends AbstractConnection implements ITopicConnection {

	private static final Logger logger = LoggerFactory.getLogger(AbstractTopicConnection.class);

	protected Session session;

	AbstractTopicConnection(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
	}

	AbstractTopicConnection(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			String commandTopicName, IEventConnectorService service) {
		super(uri, submitQueueName, statusQueueName, statusTopicName, commandTopicName, service);
	}

	/**
	 * Creates and returns a topic of the given name
	 * @param topicName
	 * @return topic
	 * @throws JMSException
	 */
	protected Topic createTopic(String topicName) throws JMSException {
		if (connection==null) createConnection();
		if (session == null)  createSession();
		return session.createTopic(topicName);
	}

	protected void createSession() throws JMSException {
		this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	@Override
	public void disconnect() throws EventException {
		super.disconnect();

		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				logger.error("Could not close session", e);
			}
			session = null;
		}
	}

}
