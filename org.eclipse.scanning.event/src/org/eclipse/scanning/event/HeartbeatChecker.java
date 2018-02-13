/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.event;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.ISubscriber;

/**
 * Checks for the heartbeat of a named consumer.
 *
 * @author Matthew Gerring
 *
 */
class HeartbeatChecker {

	private URI uri;
	private String consumerName;
	private long listenTime;
	private IEventService eventService;

	public HeartbeatChecker(IEventService eventService, URI uri, String consumerName, long listenTime) {
		this.eventService = eventService;
		this.uri = uri;
		this.consumerName = consumerName;
		this.listenTime = listenTime;
	}

	public void checkPulse() throws EventException, InterruptedException {
		ISubscriber<IHeartbeatListener> subscriber = eventService.createSubscriber(uri, EventConstants.HEARTBEAT_TOPIC);
		final AtomicBoolean heartBeatReceived = new AtomicBoolean(false);

		try {
			subscriber.addListener(event -> {
				if (consumerName.equals(event.getBean().getConsumerName())) {
					heartBeatReceived.compareAndSet(false, true);
				}
			});
			Thread.sleep(listenTime);

			if (!heartBeatReceived.get())
				throw new EventException(consumerName
						+ " Consumer heartbeat absent.\nIt is either stopped or unresponsive.\nPlease contact your support representative.");
		} finally {
			subscriber.disconnect();
		}
	}

}
