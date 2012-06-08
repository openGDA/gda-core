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

package gda.util;

/**
 * Class passed by Poller to PollerListener. The contents are not very useful but specifying the Poller and
 * PollerListener in this way makes the mechanism analagous to the ActionListener mechanism.
 */

public class PollerEvent {
	private Poller poller;

	private long pollTime;

	/**
	 * Constructs a PollerEvent with the specified Poller and pollTime.
	 * 
	 * @param poller
	 *            the Poller
	 * @param pollTime
	 *            the polling time (mS)
	 */
	public PollerEvent(Poller poller, long pollTime) {
		this.poller = poller;
		this.pollTime = pollTime;
	}

	/**
	 * Gets the Poller
	 * 
	 * @return the Poller
	 */
	public Poller getPoller() {
		return poller;
	}

	/**
	 * Gets the polling time
	 * 
	 * @return the polling time (mS)
	 */
	public long getPollTime() {
		return pollTime;
	}
}
