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

import java.time.Duration;

import org.eclipse.scanning.api.event.core.IConsumer;

/**
 * A helper class for getting interval and timeout preferences for the queueing and events system.
 */
public final class EventTimingsHelper {

	public static final Duration DEFAULT_MAXIMUM_RUNNING_AGE = Duration.ofDays(2);
	public static final Duration DEFAULT_MAXIMUM_COMPLETE_AGE = Duration.ofDays(7);

	private EventTimingsHelper() {
		// private constructor to prevent instantiation
	}

	/**
	 * The interval at which HeartbeatBeans are send by an {@link IConsumer} in ms, default 2 seconds (i.e. 2000ms)
	 * Set org.eclipse.scanning.event.heartbeat.interval system property to change this time.
	 */
	private static long notificationInterval = Duration.ofSeconds(2).toMillis();

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

	/**
	 * Defines the time in ms that a job may be in the running state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old running jobs older than
	 * this age.
	 *
	 * @return
	 */
	public static long getMaximumRunningAgeMs() {
		if (System.getProperty("org.eclipse.scanning.event.consumer.maximumRunningAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.consumer.maximumRunningAge"));
		}
		return DEFAULT_MAXIMUM_RUNNING_AGE.toMillis();
	}

	/**
	 * Defines the time in ms that a job may be in the complete (or other final) state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old complete jobs older than
	 * this age.
	 *
	 * @return
	 */
	public static long getMaximumCompleteAgeMs() {
		if (System.getProperty("org.eclipse.scanning.event.consumer.maximumCompleteAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.consumer.maximumCompleteAge"));
		}
		return DEFAULT_MAXIMUM_COMPLETE_AGE.toMillis();
	}

}
