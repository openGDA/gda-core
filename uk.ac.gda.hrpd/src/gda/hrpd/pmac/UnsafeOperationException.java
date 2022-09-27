/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.hrpd.pmac;

public class UnsafeOperationException extends RuntimeException {
	/**
	 * Create a unsafe operation exception.
	 * 
	 * @param message
	 *            the exception message to be passed to the receiver
	 */
	public UnsafeOperationException(Object current, Object must, String message) {
		super("\n" + message + " Its safe position is " + must.toString() + "; current position at " +current.toString());
	}

	/**
	 * Create a unsafe operation exception with another Throwable as the cause.
	 * 
	 * @param message
	 *            the message for this Exception
	 * @param cause
	 *            the cause (will become the detail message).
	 */
	public UnsafeOperationException(Object current, Object must, String message, Throwable cause) {
		super("\n" + message + " Its safe position is " + must.toString() + "; current position at " +current.toString(), cause);
	}

}
