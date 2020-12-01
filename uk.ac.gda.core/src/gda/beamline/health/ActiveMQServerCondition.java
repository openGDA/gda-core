/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.beamline.health;

import com.google.common.util.concurrent.RateLimiter;

import gda.data.ServiceHolder;
import gda.factory.FactoryException;

/**
 * A beamline health condition that checks whether ActiveMQ is active
 */
public class ActiveMQServerCondition extends ServerCondition {

	/**
	 * The minimum time in seconds between actually checking the connection.<br>
	 * This can be set to a higher number if the check appears to be slowing things down.
	 */
	private double minCheckTime = 10.0;

	private boolean running;

	private RateLimiter rateLimiter;

	@Override
	public void configure() throws FactoryException {
		rateLimiter = RateLimiter.create(1.0 / minCheckTime);
		setConfigured(true);
	}

	@Override
	protected synchronized boolean isRunning() {
		if (rateLimiter.tryAcquire()) {
			running = ServiceHolder.getSessionService().defaultConnectionActive();
		}
		return running;
	}

	public void setMinCheckTime(double minCheckTime) {
		this.minCheckTime = minCheckTime;
	}

	@Override
	public String toString() {
		return "ActiveMQServerCondition [minCheckTime=" + minCheckTime + ", running=" + running + ", rateLimiter="
				+ rateLimiter + "]";
	}
}
