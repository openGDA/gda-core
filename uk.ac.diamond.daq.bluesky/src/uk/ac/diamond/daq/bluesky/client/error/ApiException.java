/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.client.error;

import uk.ac.diamond.daq.bluesky.api.BlueskyException;

/** General exception class for anything that can go wrong in requests to BlueAPI */
public abstract class ApiException extends BlueskyException {
	protected ApiException(String message) {
		super(message);
	}
	protected ApiException(String message, Exception cause) {
		super(message, cause);
	}
}
