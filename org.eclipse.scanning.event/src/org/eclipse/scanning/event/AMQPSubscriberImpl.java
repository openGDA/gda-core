/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.io.IOException;
import java.net.URI;
import java.util.EventListener;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.IEventConnectorService;

public class AMQPSubscriberImpl<T extends EventListener> extends SubscriberImpl<T> {

	public AMQPSubscriberImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
	}

	@Override
	protected Topic createTopic(String topicName) throws JMSException {
		try {
			return (Topic) service.getSessionService().declareAMQPTopic(topicName);
		} catch (IOException | TimeoutException e) {
			throw new JMSException("");
		}
	}

}
