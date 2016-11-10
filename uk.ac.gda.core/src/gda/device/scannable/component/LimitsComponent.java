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

package gda.device.scannable.component;

import gda.device.DeviceException;

/**
 * All setters, getters and messages work with external positions.
 */
public interface LimitsComponent extends PositionValidator {

	/**
	 * Get lower limits in the their external representation.
	 * @return null if no limits set. Any value within array may also be null.
	 */
	Double[] getInternalLower() throws DeviceException;

	/**
	 * Get upper limits in the their external representation.
	 * @return null if no limits set. Any value within array may also be null.
	 */
	Double[] getInternalUpper() throws DeviceException;

	/**
	 * Set lower limits in the their internal representation.
	 * Use null to clear all limits. Any value within array may also be null.
	 */
	void setInternalLower(Double[] internalLowerLim) throws DeviceException;

	/**
	 * Set upper limits in the their internal representation.
	 * Use null to clear all limits. Any value within array may also be null.
	 */
	void setInternalUpper(Double[] internalUpperLim) throws DeviceException;

}