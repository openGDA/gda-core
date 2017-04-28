/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.remoting.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.observable.IObservable;
import gda.observable.IObserver;

/**
 * An instance of {@link IObserver} that takes events dispatched by an instance of {@link IObservable} and dispatches
 * them through the GDA event system.
 */
public class ServerSideEventDispatcher implements InitializingBean, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ServerSideEventDispatcher.class);

	private String sourceName;

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	private IObservable object;

	/**
	 * Sets the object whose events will be watched.
	 */
	public void setObject(IObservable object) {
		this.object = object;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (object == null) {
			throw new IllegalStateException("You must set the 'object' property");
		}
		if (sourceName == null) {
			throw new IllegalStateException("You must set the 'sourceName' property");
		}

		// Set things up to dispatch events into the event system
		final EventService eventService = EventService.getInstance();
		eventDispatcher = eventService.getEventDispatcher();
		object.addIObserver(this);
		logger.debug("Now watching events from '{}'", sourceName);
	}

	private EventDispatcher eventDispatcher;

	/**
	 * Receives an event from the observed object, and dispatches it through the GDA event system.
	 */
	@Override
	public void update(Object source, Object event) {
	/*
	 *  cannot use toString method on source as if it a Scannable then getPosition is called which can cause a deadlock
	 *  The use of the conditional log format '{}' means that getPosition would be called within the doAppend of the logger which itself is a synchronized method
	 *
	 *  In one case the source getPosition method involves use of a lock that prevented other threads to access the single communication channel to the device
	 *  this meant that the current method could not complete the logging until the comms channel was free. However another thread that had got the
	 *  lock on the comms channel then called logger.debug which could not return until the logger lock has been released!
	 *
	 */
//		logger.debug("Dispatching event ({}) from '{}'...", event, source);
		eventDispatcher.publish(sourceName, event);
	}

}
