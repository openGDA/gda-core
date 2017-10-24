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

import com.google.common.base.Optional;

import gda.configuration.properties.LocalProperties;
import gda.factory.corba.EventHeader;
import gda.factory.corba.StructuredEvent;
import gda.factory.corba.StructuredEventHelper;
import gda.util.Serializer;

public class CorbaEventDispatcher extends PushSupplierPOA implements EventDispatcher, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CorbaEventDispatcher.class);

	private SupplierAdmin supplierAdmin;

	private ProxyPushConsumer consumer;

	private boolean batchMode;

	private ExecutorCompletionService<Void> execCompletionService;

	private Object lock = new Object();

	private Map<String, EventCollection> sourceEventsMap;

	private boolean killed = false;

	private Thread thread = null;

	private int eventsPublishedWithinTime = 0;

	private Object previousEvent;

	public CorbaEventDispatcher(EventChannel eventChannel, ORB orb, boolean batchMode) {
		this.batchMode = batchMode;

		if(batchMode){
			final int threadPoolSize = LocalProperties.getAsInt("gda.factory.corba.util.CorbaEventDispatcher.threadPoolSize", 5);
			execCompletionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(threadPoolSize, r -> {
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			}));
		}

		supplierAdmin = eventChannel.for_suppliers();
		consumer = supplierAdmin.obtain_push_consumer();
		try {
			consumer.connect_push_supplier(_this(orb));
		} catch (AlreadyConnected ex) {
			logger.error("Channel already connected", ex);
			System.exit(1);
		}
	}

	@Override
	public void publish(String sourceName, Object message) {
		try {
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
						thread.setDaemon(true);
						thread.start();
					}
					lock.notifyAll();

				}
			} else {
				final Optional<OutgoingTimedStructuredEvent> timeEvent = makeTimedStructuredEvent(sourceName, message);
				if (timeEvent.isPresent()) {
					publish(timeEvent.get());
				}
			}

		} catch (Exception e) {
			logger.error("Could not publish event", e);
		}
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
						// ignore
					}
				}
				sourceEventsMapToHandle = sourceEventsMap;
				sourceEventsMap = null;
			}
			int numberCallablesSubmitted=0;
			if (sourceEventsMapToHandle != null) {
				for (Entry<String,EventCollection> mapEntry : sourceEventsMapToHandle.entrySet()) {
					try{
						Object objectForSource;
						{
							Vector<Object> objectsForSource = mapEntry.getValue();
							if(objectsForSource.size()==1){
								objectForSource = objectsForSource.get(0);
							} else {
								objectForSource = objectsForSource;
							}
						}
						final String sourceName = mapEntry.getKey();
						final Optional<OutgoingTimedStructuredEvent> timeEvent = makeTimedStructuredEvent(sourceName, objectForSource);
						if (timeEvent.isPresent()) {
							Callable<Void> publishCallable = createPublishCallable(timeEvent.get());
							execCompletionService.submit(publishCallable);
							numberCallablesSubmitted++;
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

	private static Optional<OutgoingTimedStructuredEvent> makeTimedStructuredEvent(String sourceName, Object message) {

		final byte[] byteData = Serializer.toByte(message);
		if (byteData == null) {
			logger.error("Error dispatch event:" + sourceName + " serializer returned null");
			return Optional.absent();
		}

		final String argClass = (message == null) ? "null" : message.getClass().toString();
		final String id = NameFilter.MakeEventChannelName(sourceName);
		final EventHeader eventHeader = new EventHeader(argClass, id);

		final StructuredEvent event = new StructuredEvent(eventHeader, byteData);

		final long timeReceived = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
		final OutgoingTimedStructuredEvent timeEvent = new OutgoingTimedStructuredEvent(event, timeReceived);
		return Optional.of(timeEvent);
	}

	private Callable<Void> createPublishCallable(final OutgoingTimedStructuredEvent timeEvent) {
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

	private void publish(OutgoingTimedStructuredEvent timeEvent) {
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
			// do nothing
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
}
