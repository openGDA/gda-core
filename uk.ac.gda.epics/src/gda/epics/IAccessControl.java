/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.epics;

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public interface IAccessControl {
	/**
	 * Status enum, containing ENABLED and DISABLED
	 */
	public enum Status {

		DISABLED,

		ENABLED
	}

	/**
	 * Queries the objects and returns its current status
	 */
	public Status getAccessControlState() throws TimeoutException, CAException, InterruptedException;

	/**
	 * Returns the most recently updated status
	 */
	public Status getStatus();
}
