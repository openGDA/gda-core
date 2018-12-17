/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.factory.corba.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.events.jms.JmsEventDispatcher;
import gda.events.jms.JmsEventReceiver;

/**
 * An event service class to allow dispatching of events to remote receivers using the event service.
 */
public class EventService {
	private static final Logger logger = LoggerFactory.getLogger(EventService.class);

	public static final String USE_JMS_EVENTS = "gda.events.useJMS";

	/** If JMS events is specifically requested or if Corba is disabled */
	private final boolean usingJMS = LocalProperties.check(USE_JMS_EVENTS) || LocalProperties.isCorbaDisabled();

	private EventDispatcher eventDispatcher = null;

	private EventReceiver eventReceiver = null;

	private static EventService instance = null;

	/**
	 * Access method for the EventService singleton
	 *
	 * @return the EventService
	 */
	public static synchronized EventService getInstance() {
		if( instance == null){
			try {
				instance = new EventService();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	private EventService() {
		if (usingJMS) {
			logger.info("Configured for JMS events");
		}
	}


	/**
	 * Associate a dispatcher with the event channel
	 *
	 * @return the event dispatcher
	 */
	public synchronized EventDispatcher getEventDispatcher() {
		if (eventDispatcher == null) {
			if (usingJMS) {
				eventDispatcher = new JmsEventDispatcher();
			}
		}
		return eventDispatcher;
	}

	/**
	 * Associate an event subscriber with a receiver for an event channel
	 *
	 * @param eventSubscriber
	 *            the event subscriber
	 * @param objectName
	 *            the name of the object to receive events from
	 */
	public void subscribe(EventSubscriber eventSubscriber, String objectName) {
		if (eventReceiver == null) {
			if(usingJMS) {
				eventReceiver = new JmsEventReceiver();
			}
		}
		eventReceiver.subscribe(eventSubscriber, objectName);
	}

	/**
	 * Disconnect an event subscriber from an event channel
	 */
	public void unsubscribe() {
		if (eventReceiver != null) {
			eventReceiver.disconnect();
			eventReceiver = null;
		}
	}
}
