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

package uk.ac.diamond.daq.bluesky.api;

import uk.ac.diamond.daq.bluesky.event.WorkerEvent;

public class BlueskyException extends Exception {

	public BlueskyException() {
		super();
	}

	public BlueskyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BlueskyException(String message, Throwable cause) {
		super(message, cause);
	}

	public BlueskyException(String message) {
		super(message);
	}

	public BlueskyException(Throwable cause) {
		super(cause);
	}

	public BlueskyException(WorkerEvent cause) {
		super(cause.toString());
	}
}
