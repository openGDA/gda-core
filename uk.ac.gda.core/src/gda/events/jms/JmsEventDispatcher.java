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

import gda.factory.corba.util.EventDispatcher;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

/**
 * An {@link EventDispatcher} that uses JMS to dispatch events.
 */
public class JmsEventDispatcher extends JmsClient implements EventDispatcher {
	
	/**
	 * Creates a JMS event dispatcher.
	 */
	public JmsEventDispatcher() {
		try {
			createSession();
		} catch (JMSException e) {
			throw new RuntimeException("Unable to create JMS event dispatcher", e);
		}
	}

	@Override
	public void publish(String sourceName, Object message) {
		if (message instanceof Serializable) {
			Serializable obj = (Serializable) message;
			try {
				ObjectMessage msg = session.createObjectMessage(obj);
				Topic topic = session.createTopic(TOPIC_PREFIX + sourceName);
				MessageProducer publisher = session.createProducer(topic);
				publisher.setDeliveryMode(DeliveryMode.PERSISTENT);
				publisher.send(topic, msg);
			} catch (JMSException e) {
				throw new RuntimeException("Unable to dispatch message", e);
			}
		} else {
			throw new RuntimeException("Unable to dispatch message: it is not Serializable");
		}
	}

}
