/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.accesscontrol;

import gda.device.DeviceException;

import java.io.Serializable;

/**
 * Exception thrown by an gda.jython.accesscontrol.Interceptor class when a method call is rejected as the device being
 * operated has a greater protection level than the authorisation level of the user making that method call.
 * <p>
 * This class extends DeviceException so that it will be caught by the try-catch blocks used around method calls to
 * Devices
 */
public class AccessDeniedException extends DeviceException implements Serializable {

	/**
	 * Error message to use when a method call has been denied due to lack of baton
	 */
	public static final String NOBATON_EXCEPTION_MESSAGE = "You do not hold the baton, so cannot perform this operation!";
	

	/**
	 * Create an exception.
	 * 
	 * @param reason
	 */
	public AccessDeniedException(String reason) {
		super(reason);
	}
}