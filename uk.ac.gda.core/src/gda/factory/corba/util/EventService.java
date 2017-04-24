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

import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.events.jms.JmsEventDispatcher;
import gda.events.jms.JmsEventReceiver;
import gda.factory.FactoryException;

/**
 * An event service class to allow dispatching of events to remote receivers using the event service.
 */
public class EventService {
	private static final Logger logger = LoggerFactory.getLogger(EventService.class);

	public static final String USE_JMS_EVENTS = "gda.events.useJMS";
	public final boolean usingJMS = LocalProperties.check(USE_JMS_EVENTS);

	private org.omg.CORBA.ORB orb;

	private NamingContextExt nc;

	private EventChannel eventChannel = null;

	private String eventChannelName = "eventchannel.example";

	private static EventService instance = null;

	private EventDispatcher eventDispatcher = null;

	private EventReceiver eventReceiver = null;

	private boolean configured;

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

	private EventService() throws FactoryException, NotFound, CannotProceed, InvalidName {
		if (usingJMS) {
			logger.info("Configured for JMS events");
		}
		else { // Using Corba events
			String property;
			if ((property = NameFilter.getEventChannelName()) != null) {
				eventChannelName = property;
			}
			NetService netService = NetService.getInstance();
			orb = netService.getOrb();
			nc = netService.getNamingContextExt();
			logger.info("Resolving EventService using name '{}'", eventChannelName);
			eventChannel = EventChannelHelper.narrow(nc.resolve(nc.to_name(eventChannelName)));
			logger.info("Successfully configured EventService using name '{}'", eventChannelName);
		}
		configured = true;
	}

	public boolean isConfigured() {
		return configured;
	}

	/**
	 * Associate a dispatcher with the event channel
	 *
	 * @return the event dispatcher
	 */
	public EventDispatcher getEventDispatcher() {
		if (eventDispatcher == null && configured) {
			if (usingJMS) {
				eventDispatcher = new JmsEventDispatcher();
			}
			else { // Using Corba
				//To prevent threads calling publish being blocked as the client responses one can opt to
				//put the events onto a separate thread in a threadpool.
				//To opt for this set gda.factory.corba.util.CorbaEventDispatcher.threadPoolSize to rather than 0, e.g 10
				eventDispatcher = new CorbaEventDispatcher(eventChannel, orb, LocalProperties.check("gda.factory.corba.util.CorbaEventDispatcher.allowBatchMode", true));
			}
		}
		return eventDispatcher;
	}

	/**
	 * Associate an event subscriber with a receiver for an event channel
	 *
	 * @param eventSubscriber
	 *            the event subscriber
	 * @param filter
	 *            the event filter
	 */
	public void subscribe(EventSubscriber eventSubscriber, Filter filter) {
		if (!configured) {
			logger.error("Not configured");
			return; // Not configured
		}

		if (eventReceiver == null) {
			if(usingJMS) {
				eventReceiver = new JmsEventReceiver();
			}
			else { // using Corba
				eventReceiver = new CorbaEventReceiver(eventChannel, orb);
			}
		}
		eventReceiver.subscribe(eventSubscriber, filter);
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
