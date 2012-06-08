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

package gda.device;

import java.io.Serializable;

/**
 * An Exception class specifically for {@link Device}s.
 */
public class DeviceException extends Exception implements Serializable {
	/**
	 * Constructs a device exception.
	 * 
	 * @param message
	 *            the exception message to be passed to the receiver
	 */
	public DeviceException(String message) {
		super(message);
	}

	/**
	 * Constructs a device exception with another Throwable as the cause.
	 * 
	 * @param message
	 *            the message for this Exception
	 * @param cause
	 *            the cause (will become the detail message).
	 */
	public DeviceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new DeviceException with the specified cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the class and detail message of
	 * <tt>cause</tt>). This constructor is useful for exceptions that are little more than wrappers for other
	 * throwables.
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
	 *            value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public DeviceException(Throwable cause) {
		super(cause);
	}
}
