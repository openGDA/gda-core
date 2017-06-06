/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence.jythonshelf;

/**
 *
 */
public class ObjectShelfException extends Exception {
	/**
	 * Thrown when problems at the shelf object layer are caused either by internal logic problems, or incorrect usage.
	 */
	public ObjectShelfException() {
	}

	/**
	 * @param msg
	 */
	public ObjectShelfException(String msg) {
		super(msg);
	}

	/**
	 * Create a shelf exception with another Throwable as the cause.
	 *
	 * @param message
	 *            the message for this Exception
	 * @param cause
	 *            the cause (will become the detail message).
	 */
	public ObjectShelfException(String message, Throwable cause) {
		super(message, cause);
	}
}