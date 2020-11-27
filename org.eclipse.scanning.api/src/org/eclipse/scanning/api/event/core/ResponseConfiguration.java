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
package org.eclipse.scanning.api.event.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;

public class ResponseConfiguration {

	/**
	 * Interface to a class that determines, when an initial timeout has been reached, whether the system should
	 * continue to wait.
	 */
	public interface ResponseWaiter {
		/**
		 * @return <code>true</code> if the system should continue to wait, of <code>false</code> if it should not
		 */
		boolean waitAgain();

		/**
		 * An implementation of {@link ResponseWaiter} that will never continue to wait
		 */
		public static class Dont implements ResponseWaiter {
			@Override
			public boolean waitAgain() {
				return false;
			}
		}
	}

	public enum ResponseType {
		/**
		 * One response is required, after that comes in, return. If the timeout is reached throw an exception.
		 * <p>
		 * This is more efficient because the timeout is not waited for, it can return as soon as the first message is
		 * encountered.
		 */
		ONE,

		/**
		 * Multiple responses are required, wait for the response timeout and collate all responses. If no response
		 * comes in, throw an exception.
		 * <p>
		 * Less efficient but caters for multiple sources of the response if there may be some.
		 */
		ONE_OR_MORE;
	}

	public static final long DEFAULT_TIMEOUT = 100;
	public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

	private ResponseType responseType;
	private long timeout;
	private TimeUnit timeUnit;
	private CountDownLatch latch;
	private boolean somethingFound;

	public ResponseConfiguration() {
		this(ResponseType.ONE, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
	}

	public ResponseConfiguration(ResponseType responseType, long timeout, TimeUnit timeUnit) {
		this.responseType = responseType;
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}

	public void latch(ResponseWaiter waiter) throws EventException, InterruptedException {
		if (waiter == null) {
			// Default to waiting just one timeout period
			waiter = new ResponseWaiter.Dont();
		}

		if (responseType == ResponseType.ONE) {
			// Wait for the first response, subject to timing out
			latch = new CountDownLatch(1);
			boolean ok = latch.await(timeout, timeUnit);
			while (!ok && waiter.waitAgain()) {
				ok = latch.await(timeout, timeUnit);
			}
			// waitAgain() could be false leaving ok as false, so we recheck it
			ok = latch.await(timeout, timeUnit);
			if (!ok) {
				throw new EventException("The timeout of " + timeout + " " + timeUnit + " was reached and no response occurred!");
			}
		} else if (responseType == ResponseType.ONE_OR_MORE) {
			// Wait for the specified time, regardless of how many responses are received
			somethingFound = false;

			Thread.sleep(timeUnit.toMillis(timeout));
			while (waiter.waitAgain()) {
				Thread.sleep(timeUnit.toMillis(timeout));
			}
			if (!somethingFound) {
				throw new EventException("The timeout of " + timeout + " " + timeUnit + " was reached and no response occurred!");
			}
		}
	}

	public void countDown() {
		somethingFound = true;
		if (latch != null) {
			latch.countDown();
		}
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setTimeout(long time, TimeUnit unit) {
		setTimeout(time);
		setTimeUnit(unit);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((responseType == null) ? 0 : responseType.hashCode());
		result = prime * result + ((timeUnit == null) ? 0 : timeUnit.hashCode());
		result = prime * result + (int) (timeout ^ (timeout >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResponseConfiguration other = (ResponseConfiguration) obj;
		if (responseType != other.responseType)
			return false;
		if (timeUnit != other.timeUnit)
			return false;
		if (timeout != other.timeout)
			return false;
		return true;
	}
}
