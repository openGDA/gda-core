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

import java.util.concurrent.ScheduledExecutorService;

/**
 * Classes which want to use a Poller must implement this interface.
 *
 * @deprecated These classes replicate {@link ScheduledExecutorService} behaviour and should be replaced if possible.
 *     see DAQ-1197
 */
@Deprecated
public interface PollerListener {
	/**
	 * Method to be called automatically by Poller at regular, preset intervals
	 *
	 * @param pe
	 *            PollerEvent,
	 * @see PollerEvent
	 * @see Poller
	 */
	public void pollDone(PollerEvent pe);
}
