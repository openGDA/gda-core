/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

public class PathInfoCalculationException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -2484455579381036697L;

	public PathInfoCalculationException() {
		super();
	}

	public PathInfoCalculationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PathInfoCalculationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PathInfoCalculationException(String message) {
		super(message);
	}

	public PathInfoCalculationException(Throwable cause) {
		super(cause);
	}
}
