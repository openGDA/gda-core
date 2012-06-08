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

/**
 * A motor exception class
 */
final public class MotorException extends DeviceException {

	final public MotorStatus status;

	/**
	 * @param stat
	 * @param msg
	 */
	public MotorException(MotorStatus stat, String msg) {
		this(stat, msg, null);
	}

	/**
	 * @param stat
	 * @param msg
	 * @param cause
	 */
	public MotorException(MotorStatus stat, String msg, Throwable cause) {
		super(msg, cause);
		if (stat == null) {
			throw new IllegalArgumentException("MotorException: stat cannot be null");
		}
		status = stat;
	}

}
