/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

import gda.device.DeviceException;

public interface EnumValueMapper<T> {

	/**
	 *
	 * @param internalValue value for which external value is to be found
	 * @return External representation of internalValue
	 * @throws IllegalArgumentException - if there is no external representation for internalValue
	 */
	String getExternalValue(T internalValue) throws IllegalArgumentException;

	/**
	 *
	 * @param externalValue value for which internal value is to be found
	 * @return Internal representation of externalValue
	 * @throws IllegalArgumentException - if there is no internal representation for externalValue
	 */
	T getInternalValue(String externalValue) throws IllegalArgumentException;

	/**
	 *
	 * @param externalValueToCheck
	 * @return true if valid, false if not
	 * @throws IllegalArgumentException - if not valid
	 */
	Boolean isExternalValueValid(String externalValueToCheck);

	String[] getExternalValues() throws DeviceException;

}
