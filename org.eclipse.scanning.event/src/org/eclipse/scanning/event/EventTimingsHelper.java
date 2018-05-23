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

import org.eclipse.scanning.api.event.core.IConsumer;

/**
 * A helper class for getting interval and timeout preferences for the queueing and events system.
 */
public final class EventTimingsHelper {

	private EventTimingsHelper() {
		// private constructor to prevent instantiation
	}

	/**
	 * The interval in ms at which HeartbeatBeans are send by an {@link IConsumer} in ms, default 2000.
	 * Set org.eclipse.scanning.event.heartbeat.interval system property to change this time.
	 */
	private static long notificationInterval = 2000;

	public static long getNotificationInterval() {
		return Long.getLong("org.eclipse.scanning.event.heartbeat.interval", notificationInterval);
	}
	public static void setNotificationInterval(long interval) {
		notificationInterval = interval;
		System.setProperty("org.eclipse.scanning.event.heartbeat.interval", String.valueOf(interval));
	}

	/**
	 * The timeout to use for consumer.receive() in ms. A higher rate might be better for some applications.
	 * @return timeout
	 */
	public static final int getReceiveTimeout() {
		return Integer.getInteger("org.eclipse.scanning.receive.timeout", 500);
	}

	public static void setReceiveTimeout(int timeout) {
		System.setProperty("org.eclipse.scanning.event.receive.timeout", String.valueOf(timeout));
	}

}
