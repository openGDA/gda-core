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
import javax.jms.Topic;

import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.ITopicConnection;

/**
 * Abstract superclass for objects that publish or subscribe to a topic.
 */
public abstract class AbstractTopicConnection extends AbstractConnection implements ITopicConnection {

	private String topicName;

	AbstractTopicConnection(URI uri, String topicName, IEventConnectorService service) {
		super(uri, service);
		this.topicName = topicName;
	}

	/**
	 * Creates and returns a topic of the given name
	 * @param topicName
	 * @return topic
	 * @throws JMSException
	 */
	protected Topic createTopic(String topicName) throws JMSException {
		return getSession().createTopic(topicName);
	}

	@Override
	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topic) {
		this.topicName = topic;
	}

}
