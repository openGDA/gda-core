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

import org.eclipse.scanning.api.event.core.IJobQueue;

/**
 * A helper class for getting interval and timeout preferences for the queueing and events system.
 */
public final class EventTimingsHelper {

	public static final Duration DEFAULT_MAXIMUM_RUNNING_AGE = Duration.ofDays(2);
	public static final Duration DEFAULT_MAXIMUM_COMPLETE_AGE = Duration.ofDays(7);
	public static final Duration DEFAULT_MAXIMUM_WAIT_TIME = Duration.ofDays(1);

	private EventTimingsHelper() {
		// private constructor to prevent instantiation
	}

	/**
	 * The time to wait after an error reading from the JMS queue before trying again.
	 */
	private static long connectionRetryInterval = Duration.ofSeconds(2).toMillis();

	public static long getConnectionRetryInterval() {
		return Long.getLong("org.eclipse.scanning.event.heartbeat.interval", connectionRetryInterval);
	}

	public static void setConnectionRetryInterval(long interval) {
		connectionRetryInterval = interval;
		System.setProperty("org.eclipse.scanning.event.heartbeat.interval", String.valueOf(interval));
	}

	/**
	 * The timeout to use for jobQueue.receive() in ms. A higher rate might be better for some applications.
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
	 * before the {@link IJobQueue} might consider it for deletion. If an {@link IJobQueue}
	 * is restarted it will normally delete old running jobs older than
	 * this age.
	 *
	 * @return
	 */
	public static long getMaximumRunningAgeMs() {
		if (System.getProperty("org.eclipse.scanning.event.queue.maximumRunningAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.queue.maximumRunningAge"));
		}
		return DEFAULT_MAXIMUM_RUNNING_AGE.toMillis();
	}

	/**
	 * Defines the time in ms that a job may be in the complete (or other final) state
	 * before the queue might consider it for deletion. If a queue
	 * is restarted it will normally delete old complete jobs older than
	 * this age.
	 *
	 * @return
	 */
	public static long getMaximumCompleteAgeMs() {
		if (System.getProperty("org.eclipse.scanning.event.queue.maximumCompleteAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.queue.maximumCompleteAge"));
		}
		return DEFAULT_MAXIMUM_COMPLETE_AGE.toMillis();
	}

}
