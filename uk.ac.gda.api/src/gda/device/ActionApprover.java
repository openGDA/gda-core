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

package gda.device;

import java.io.Serializable;

/**
 * Interface for classes which perform tests on the current status/position of equipment before operations are made
 * which could cause damage or effect the flow of the experiment
 */
public interface ActionApprover extends Serializable {
	/**
	 * Performs the test that the object encapsulates and returns the result of that test.
	 * 
	 * @return true if operation would be OK to perform
	 * @throws DeviceException -
	 *             thrown if an error occurred while inquiring into device objects' status
	 */
	public boolean actionApproved() throws DeviceException;

	/**
	 * Returns an explanation of why the last call to actionApproved returned false.
	 * 
	 * @return string
	 */
	public String getDenialReason();
}
