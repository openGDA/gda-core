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

import gda.factory.corba.util.EventReceiver;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.Filter;
import gda.factory.corba.util.NameFilter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

/**
 * An {@link EventReceiver} that receives messages using JMS.
 */
public class JmsEventReceiver extends JmsClient implements EventReceiver {
	
	/**
	 * Creates a JMS event receiver.
	 */
	public JmsEventReceiver() {
		try {
			createSession();
			connection.start();
		} catch (JMSException e) {
			throw new RuntimeException("Unable to create JMS event receiver", e);
		}
	}
	
	@SuppressWarnings("unused")
	@Override
	public void subscribe(EventSubscriber eventSubscriber, Filter filter) {
		try {
			NameFilter nf = (NameFilter) filter;
			String name = nf.getName();
			String channelName = TOPIC_PREFIX + name;
			Topic topic = session.createTopic(channelName);
			new EventTopicListener(topic, eventSubscriber);
		} catch (JMSException e) {
			throw new RuntimeException("Could not subscribe", e);
		}
	}

	class EventTopicListener implements MessageListener {

		private EventSubscriber eventSubscriber;
		
		public EventTopicListener(Topic topic, EventSubscriber eventSubscriber) throws JMSException {
			this.eventSubscriber = eventSubscriber;
			MessageConsumer consumer = session.createConsumer(topic);
			consumer.setMessageListener(this);
		}
		
		@Override
		public void onMessage(Message message) {
			if (message instanceof ObjectMessage) {
				try {
					ObjectMessage objectMessage = (ObjectMessage) message;
					Object o = objectMessage.getObject();
					eventSubscriber.inform(o);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	public void disconnect() {
	}

}
