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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.PushConsumerPOA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.corba.EventHeader;
import gda.util.LoggingConstants;

/**
 * An event receiver class for the event service.
 */
public class CorbaEventReceiver extends PushConsumerPOA implements EventReceiver {
	private static final Logger logger = LoggerFactory.getLogger(CorbaEventReceiver.class);

	private final Set<Subscription> subscriptions = new CopyOnWriteArraySet<>();

	private ConsumerAdmin consumerAdmin;

	private ProxyPushSupplier supplier;

	private static PushEventQueue pushEventQueue;

	/**
	 * Create an event receiver instance for incoming events
	 *
	 * @param channel
	 * @param orb
	 */
	CorbaEventReceiver(EventChannel channel, ORB orb) {
		logger.debug(LoggingConstants.FINEST, "Constructing EventReceiver");
		consumerAdmin = channel.for_consumers();
		supplier = consumerAdmin.obtain_push_supplier();
		pushEventQueue = new PushEventQueue(this);

		try {
			supplier.connect_push_consumer(_this(orb));
			logger.debug("EventReceiver: Consumer connected");
		} catch (TypeError | AlreadyConnected ex) {
			logger.error(ex.getMessage());
			System.exit(1);
		}
	}

	@Override
	public void disconnect() {
		disconnect_push_consumer();
	}

	@Override
	public void disconnect_push_consumer() {
		supplier = null;
	}

	/**
	 * Register on the event channel using the filter to get the required events
	 *
	 * @param subscriber
	 *            the subscriber
	 * @param filter
	 *            the filter used to select the appropriate events
	 */
	@Override
	public void subscribe(EventSubscriber subscriber, Filter filter) {
		Subscription subscription = new Subscription(subscriber, filter);

		subscriptions.add(subscription);
	}

	/**
	 * Unsubscribe from the event channel
	 *
	 * @param subscriber
	 *            the subscriber
	 * @param filter
	 *            the filter which was used to subscribe.
	 */
	public void unsubscribe(EventSubscriber subscriber, Filter filter) {
		Subscription subscription = new Subscription(subscriber, filter);
		// FIXME don't think this will actually work remove uses equals which should never be true because you have just made a new Subscription
		subscriptions.remove(subscription);
	}

	@Override
	public void push(Any any) {
		// never allow exceptions to go through this function as it causes the
		// same event to be aborted to other consumers
		try {
			pushEventQueue.addEvent(any);
		} catch (Exception e) {
			logger.error("Error pushing event: " + e.getMessage());
		}

	}

	void pushNow(TimedStructuredEvent event) {
		for (Subscription subscription : subscriptions) {
			try {
				if (subscription.accept(event) == Filter.ACCEPTANCE.EXCLUSIVE)
					break; // The event has been handled by this subscription so stop trying others
			} catch (Exception e) {
				EventHeader header = event.getHeader();
				logger.error("Error pushing event (source={}, type={})", header.eventName, header.typeName, e);
			}
		}
	}
}


/**
 * Queue for handling Corba events
 */
class PushEventQueue implements Runnable {

	public static final String GDA_EVENTRECEIVER_QUEUE_LENGTH_CHECK = "gda.eventreceiver.queue.length.check";

	private static final Logger logger = LoggerFactory.getLogger(PushEventQueue.class);

	private final List<TimedAny> items = new ArrayList<>();
	private volatile boolean killed = false;
	private final Thread thread;
	private final int chkLengthLimit = LocalProperties.getInt(GDA_EVENTRECEIVER_QUEUE_LENGTH_CHECK, 100);

	CorbaEventReceiver receiver;
	PushEventQueue(CorbaEventReceiver receiver){
		this.receiver = receiver;
		// Setup the receiver thread
		thread = uk.ac.gda.util.ThreadManager.getThread(this, "EventReceiver:PushEventQueue");
		thread.start();
	}

	public void addEvent(Any any){
		synchronized (items) {
			items.add(new TimedAny(any, System.currentTimeMillis()));
			items.notifyAll();
		}
	}

	public void dispose() {
		killed = true;
	}
	@Override
	public void run() {
		List<TimedStructuredEvent> lastItemsHandled = null;
		while (!killed) {
			try {
				List<TimedAny> itemsToBeHandled = null;
				synchronized (items) {
					if (!killed && items.isEmpty()) {
						items.wait();
					}
					// Copy the items list into the itemsToBeHandled list
					itemsToBeHandled = new ArrayList<>(items);
					// Empty the items list
					items.clear();
				}

				List<TimedStructuredEvent> newEvents = new ArrayList<>();
				for (TimedAny e : itemsToBeHandled) {
					if (e.isStructuredEvent()) {
						try {
							newEvents.add(new TimedStructuredEvent(e));
						} catch (Throwable t) {
							logger.error("Couldn't extract StructuredEvent from received object", t);
						}
					} else {
						logger.warn("Received an object that is not a StructuredEvent");
					}
				}

				if (!newEvents.isEmpty()) {
					int numItems = newEvents.size();
					if( chkLengthLimit > 0 && numItems > chkLengthLimit && lastItemsHandled != null){
						logger.warn("EventReceiver queue length of {} has exceeded check threshold of {}",  numItems, chkLengthLimit);
						if(logger.isDebugEnabled()){
							for(TimedStructuredEvent timeEvent : lastItemsHandled){
								logger.debug(String.format("Previously pushed event (source=%s, type=%s)", timeEvent.getHeader().eventName, timeEvent.getHeader().typeName));
							}
						}
					}
					for (TimedStructuredEvent event : newEvents) {
						long timeOfDispatch = System.currentTimeMillis();
						long timeBeforeDispatching = timeOfDispatch - event.getTimeReceivedMs();
						if( timeBeforeDispatching > 1000){
							logger.warn("Event took {}ms until dispatch (source={}, type={})", timeBeforeDispatching, event.getHeader().eventName, event.getHeader().typeName);
						}

						receiver.pushNow(event);

						long timeAfterDispatch = System.currentTimeMillis();
						long timeToDispatch = timeAfterDispatch-timeOfDispatch;
						if(timeToDispatch > 1000){
							logger.warn("Event took {}ms to dispatch (source={}, type={})", timeToDispatch, event.getHeader().eventName, event.getHeader().typeName);
						}
					}
				}
				lastItemsHandled = newEvents;
			} catch (Throwable th) {
				logger.error("EventReceiver.run exception ", th);
			}
		}
	}

}