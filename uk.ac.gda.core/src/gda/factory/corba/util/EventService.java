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

import gda.events.jms.JmsEventDispatcher;
import gda.events.jms.JmsEventReceiver;

/**
 * An event service class to allow dispatching of events to remote receivers using the event service.
 */
public enum EventService {
	INSTANCE;

	private final EventDispatcher eventDispatcher = new JmsEventDispatcher();

	private final EventReceiver eventReceiver = new JmsEventReceiver();

	/**
	 * Access method for the EventService singleton
	 *
	 * @return the EventService
	 */
	public static synchronized EventService getInstance() {
		return INSTANCE;
	}

	/**
	 * Associate a dispatcher with the event channel
	 *
	 * @return the event dispatcher
	 */
	public synchronized EventDispatcher getEventDispatcher() {
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
		eventReceiver.subscribe(eventSubscriber, objectName);
	}

}
