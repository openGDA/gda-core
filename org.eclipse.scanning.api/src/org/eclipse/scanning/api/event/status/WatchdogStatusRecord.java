/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.event.status;

import org.eclipse.scanning.api.event.status.WatchdogStatusRecord.WatchdogState;

public record WatchdogStatusRecord(

		/**
		 * Name of the watchdog under which it is rergistered in IDeviceWatchdogService
		 */
		String watchdogName,

		/**
		 * State that indicates if the watchdog is pausing or resuming a scan
		 */
		WatchdogState state,

		/**
		 * Whether the watchdog has been enabled to have the ability to pause and resume scans
		 */
		boolean enabled) {

	public enum WatchdogState {PAUSING, RESUMING}

}
