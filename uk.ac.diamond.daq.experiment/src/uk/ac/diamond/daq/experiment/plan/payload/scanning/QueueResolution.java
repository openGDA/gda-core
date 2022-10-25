/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan.payload.scanning;

/**
 * Policy for when a scan is already running at the time of submission
 */
public enum QueueResolution {

	/**
	 * A scan is already running, do not add this one to the queue
	 */
	DROP,

	/**
	 * This is an important scan, so stop anything else that's running to make room for this one
	 */
	STOP_PREVIOUS_SCANS;

}
