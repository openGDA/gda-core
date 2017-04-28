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

package uk.ac.gda.remoting.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.Filter;
import gda.factory.corba.util.NameFilter;
import gda.observable.ObservableComponent;

/**
 * Object that registers with the GDA event system to receive events relating to a specific remote object. The events
 * are then injected into the client-side proxy for that remote object, and are then pushed out to the proxy's
 * observers.
 */
public class ClientSideEventReceiver implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ClientSideEventReceiver.class);

	private String objectName;

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	private Object proxy;

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	private ObservableComponent observableComponent;

	public void setObservableComponent(ObservableComponent observableComponent) {
		this.observableComponent = observableComponent;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (objectName == null) {
			throw new IllegalStateException("The 'objectName' property is required");
		}

		if (proxy == null) {
			throw new IllegalStateException("The 'proxy' property is required");
		}

		if (observableComponent == null) {
			throw new IllegalStateException("The 'observableComponent' property is required");
		}

		// This is the EventSubscriber implementation that most existing CORBA adapters use
		EventSubscriber eventSubscriber = new SimpleEventSubscriber(proxy, observableComponent);

		// Register to receive events from the event system
		final EventService eventService = EventService.getInstance();
		Filter filter = new NameFilter(objectName, observableComponent);
		eventService.subscribe(eventSubscriber, filter);
		logger.debug("Now subscribed to events from '{}'", objectName);
	}

}
