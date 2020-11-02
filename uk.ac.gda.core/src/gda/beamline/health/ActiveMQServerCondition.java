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

import java.util.Date;

import gda.data.ServiceHolder;

/**
 * A beamline health condition that checks whether ActiveMQ is active
 */
public class ActiveMQServerCondition extends ServerCondition {

	/**
	 * The minimum time in ms between actually checking the connection.<br>
	 * This can be set to a higher number if the check appears to be slowing things down.
	 */
	private long minCheckTime = 10000;

	private boolean running;

	private long lastUpdate = 0;

	@Override
	protected synchronized boolean isRunning() {
		final long currentTime = new Date().getTime();
		if (currentTime - lastUpdate > minCheckTime) {
			running = ServiceHolder.getSessionService().defaultConnectionActive();
			lastUpdate = currentTime;
		}
		return running;
	}

	public void setMinCheckTime(long minCheckTime) {
		this.minCheckTime = minCheckTime;
	}

	@Override
	public String toString() {
		return "ActiveMQServerCondition [minCheckTime=" + minCheckTime + ", running=" + running + ", lastUpdate="
				+ lastUpdate + "]";
	}
}
