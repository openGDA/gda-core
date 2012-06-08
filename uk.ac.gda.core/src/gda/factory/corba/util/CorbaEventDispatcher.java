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

import gda.configuration.properties.LocalProperties;
import gda.factory.corba.EventHeader;
import gda.factory.corba.StructuredEvent;
import gda.factory.corba.StructuredEventHelper;
import gda.util.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosEventComm.PushSupplierPOA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event dispatcher class
 */
public class CorbaEventDispatcher extends PushSupplierPOA implements EventDispatcher, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CorbaEventDispatcher.class);

	private SupplierAdmin supplierAdmin;

	private ProxyPushConsumer consumer;

	boolean batchMode;

	private ExecutorCompletionService<Void> execCompletionService;
	
	private Object lock = new Object();
	
	private Map<String, EventCollection> sourceEventsMap;
	
	private boolean killed = false;
	
	private Thread thread = null;

	private int eventsPublishedWithinTime = 0;
	
	private Object previousEvent;
	
	class TimedStructuredEvent {

		public StructuredEvent event;

		public long timeReceivedMs;

		public TimedStructuredEvent(StructuredEvent event, long timeReceivedMS) {
			this.event = event;
			this.timeReceivedMs = timeReceivedMS;
		}

		@Override
		public String toString() {
			return String.format("TimedStructuredEvent(time=%d, source=%s, type=%s)", timeReceivedMs,
					event.eventHeader.eventName, event.eventHeader.typeName);
		}
	}
	
	/**
	 * Create an event dispatcher for the specified channel.
	 * 
	 * @param channel
	 *            the event channel
	 * @param orb
	 *            the orb
	 */
	public CorbaEventDispatcher(EventChannel channel, ORB orb, boolean batchMode) {
		this.batchMode = batchMode;

		if(batchMode){
			int threadPoolSize;
			threadPoolSize = LocalProperties.getAsInt("gda.factory.corba.util.CorbaEventDispatcher.threadPoolSize", 5);
			execCompletionService = new ExecutorCompletionService<Void>(Executors.newFixedThreadPool(threadPoolSize));
		}
		
		supplierAdmin = channel.for_suppliers();
		consumer = supplierAdmin.obtain_push_consumer();
		try {
			consumer.connect_push_supplier(_this(orb));
		} catch (AlreadyConnected ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(1);
		}
	}

	@Override
	public void disconnect_push_supplier() {
		try {
			consumer.disconnect_push_consumer();
		} catch (org.omg.CORBA.TRANSIENT ex) {
			logger.error("EventDispatcher.disconnect_push_supplier() ", ex);
		} finally {
			consumer = null;
		}
	}

	/**
	 * Push the event to all event channels
	 * 
	 * @param timeEvent
	 *            the event.
	 */
	public void publish(TimedStructuredEvent timeEvent) {
		Any any = ORB.init().create_any();
		StructuredEventHelper.insert(any, timeEvent.event);

		try {
			if (consumer != null) {
				consumer.push(any);
				if (logger.isDebugEnabled()) {
					long timeAfterDispatch = System.currentTimeMillis();
					long timeToDispatch = timeAfterDispatch - timeEvent.timeReceivedMs;
					if (timeToDispatch > 100) {
						logger.debug(String.format("Event took %dms to publish %s", timeToDispatch, timeEvent.toString()));
					}
				}
			}
		} catch (Disconnected ex) {
			logger.error("EventDispatcher.publish()", ex);
		} catch (org.omg.CORBA.COMM_FAILURE ex) {
			logger.error("EventDispatcher.publish()", ex);
			disconnect_push_supplier();
		} catch (org.omg.CORBA.TRANSIENT ex) {
			logger.error("EventDispatcher.publish()", ex);
			disconnect_push_supplier();
		} catch (org.omg.CORBA.UNKNOWN ex) {
			// do nothing here see bugzilla bug #
		}
	}

	@Override
	public void publish(String sourceName, Object message) {
		try {
			/**
			 * Enter batchMode if allowed AND either sourceEventsMap != null OR sequential messages are to
			 * the same source
			 */
			if (batchMode) {
				synchronized (lock) {
					if (sourceEventsMap == null)
						sourceEventsMap = new HashMap<String,EventCollection>();
					EventCollection eventList = sourceEventsMap.get(sourceName);
					if (eventList == null) {
						eventList = new EventCollection();
						eventList.add(message);
						sourceEventsMap.put(sourceName, eventList);
					} else {
						boolean added = false;
						if( message instanceof Double ){
							Object lastElement = eventList.lastElement();
							if( lastElement instanceof Double ){
								//replace
								eventList.set(eventList.size()-1, message);
								added =true;
							}
						} 
						if(!added)
							eventList.add(message);
					}
					if (thread == null) {
						thread = new Thread(this, "CorbaEventDispatcher");
						thread.start();
					}
					lock.notifyAll();

				}
			} else {
				String id = NameFilter.MakeEventChannelName(sourceName);
				String argClass = (message == null) ? "null" : message.getClass().toString();
				EventHeader eventHeader = new EventHeader(argClass, id);
				StructuredEvent event = new StructuredEvent(eventHeader, Serializer.toByte(message));
				final TimedStructuredEvent timeEvent = new TimedStructuredEvent(event,
						logger.isDebugEnabled() ? System.currentTimeMillis() : 0);
				publish(timeEvent);
			}

		} catch (Exception e) {
			logger.error("Could not publish event", e);
		}
	}

	public Callable<Void> createPublishCallable(final TimedStructuredEvent timeEvent) {
		return new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				long timeOfDispatch = System.currentTimeMillis();
				if (logger.isDebugEnabled()) {
					long timeBeforeDispatching = timeOfDispatch - timeEvent.timeReceivedMs;
					if (timeBeforeDispatching > 100) {
						logger.debug(String
								.format("Event took %dms until publish %s. %d events published since last warning. Previous event %s",
										timeBeforeDispatching, timeEvent.toString(), eventsPublishedWithinTime,
										previousEvent != null ? previousEvent.toString() : "none"));
						eventsPublishedWithinTime = 0;
					} else {
						eventsPublishedWithinTime++;
					}
					timeEvent.timeReceivedMs = timeOfDispatch;
				}
				publish(timeEvent);
				previousEvent = timeEvent;
				if (logger.isDebugEnabled()) {
					long timeAfterPublish = System.currentTimeMillis();
					long timeToPublish = timeAfterPublish - timeOfDispatch;
					if (timeToPublish >= 500) { 
						logger.debug(String.format("[%s] Event took %dms to publish %s. ", Thread.currentThread()
								.getName(), timeToPublish, timeEvent.toString()));
					}
				}
				return null;
			}
		};
	}

	public void dispose() {
		killed = true;
	}

	@Override
	public void run() {
		while (!killed) {
			Map<String,EventCollection> sourceEventsMapToHandle;
			synchronized (lock) {
				if (!killed && sourceEventsMap == null || sourceEventsMap.isEmpty()) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				sourceEventsMapToHandle = sourceEventsMap;
				sourceEventsMap = null;
			}
			int numberCallablesSubmitted=0;
			if (sourceEventsMapToHandle != null) {
				for (Entry<String,EventCollection> mapEntry : sourceEventsMapToHandle.entrySet()) {
					try{
						String id = NameFilter.MakeEventChannelName(mapEntry.getKey());
						Object objectForSource;
						{
							Vector<Object> objectsForSource = mapEntry.getValue();
							if(objectsForSource.size()==1){
								objectForSource = objectsForSource.get(0);
							} else {
								objectForSource = objectsForSource;
							}
						}
						String argClass = (objectForSource == null) ? "null" : objectForSource.getClass().toString();
						EventHeader eventHeader = new EventHeader(argClass, id);
						byte[] byte1 = Serializer.toByte(objectForSource);
						if( byte1 != null){
							StructuredEvent event = new StructuredEvent(eventHeader, byte1);
							final TimedStructuredEvent timeEvent = new TimedStructuredEvent(event,
									logger.isDebugEnabled() ? System.currentTimeMillis() : 0);

							Callable<Void> publishCallable = createPublishCallable(timeEvent);
							execCompletionService.submit(publishCallable);
							numberCallablesSubmitted++;
						} else {
							logger.error("Error dispatch event:" + mapEntry.getKey() + " serializer returned null");
						}
						
					}
					catch (Throwable th){
						logger.error("Error dispatch event:" + mapEntry.getKey(),th);
					}
				}
			}
			try {
				while(numberCallablesSubmitted > 0){
					if( execCompletionService.poll(10,TimeUnit.SECONDS) == null){
						logger.error("An event has taken more than 10s to send to event server");
					}
					//always decrease so eventually we will return
					numberCallablesSubmitted--;
				}
			} catch (InterruptedException e) {
				logger.error("execCompletionService.poll interrupted",e);
			}
		}
	}
}