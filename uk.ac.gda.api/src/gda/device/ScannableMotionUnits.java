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

import org.jscience.physics.quantities.Quantity;

/**
 * An interface for ScannableMotion classes which provide a unit conversion from lower level hardware to an 'user unit'
 * from a list of 'acceptable units'.
 * <p>
 * This provides a set of standard methods to use to enable this conversion.
 * <p>
 * Classes implementing this interface are recommended to use the UnitsComponent component to make the conversion
 * between 'user units' and 'hardware units' easier.
 * <p>
 * Strings are used rather than Unit objects to enable use of these classes in the Jython environment.
 * 
 */
public interface ScannableMotionUnits extends ScannableMotion {

	/**
	 * The attribute to ask for to get the user units
	 */
	public static final String USERUNITS = "userunits";

	/**
	 * The attribute to ask for to get the hardware units
	 */
	public static final String HARDWAREUNITS = "hardwareunits";

	/**
	 * Returns a string representation of the current reporting units
	 * 
	 * @return Returns the reportingUnitsString.
	 */
	public String getUserUnits();

	/**
	 * Sets the user unit to userUnitString. If a hardware unit has not been explicitly set, then the hardware unit is also
	 * set to userUnitString. If a hardware unit has been set, and the userUnit is not compatible with this, then a DeviceException
	 * is thrown.
	 * 
	 * @param userUnitsString
	 *            The reportingUnitsString to set.
	 * @throws DeviceException
	 *             if string not found in list of acceptable units
	 */
	public void setUserUnits(String userUnitsString) throws DeviceException;

	/**
	 * @return Returns the motorUnitString.
	 */
	public String getHardwareUnitString();

	/**
	 * 
	 * Sets the hardware unit to hardwareUnitString. If a user unit has not been explicitly set then the
	 * user unit is also set to hardwareUnitString. If the user unit has been explicitly set and the new
	 * hardware unit would invalidate this then a DeviceException is thrown.
	 * <p>
	 * Based on this string, this method will build a list of acceptable user-units. Afterwards, the acceptable units
	 * list can be added to via the addAcceptableUnits method if the defaults do not cover enough.
	 * <p>
	 * @param hardwareUnitString
	 *            The motorUnitString to set.
	 * @throws DeviceException 
	 */
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException;

	/**
	 * @return Returns the acceptableUnitStrings.
	 */
	public String[] getAcceptableUnits();

	/**
	 * Adds a new unit to the list of acceptable units based on the supllied string
	 * 
	 * @param newUnit -
	 *            string representation of the new acceptable unit
	 * @throws DeviceException
	 */
	public void addAcceptableUnit(String newUnit) throws DeviceException;
	
	public Quantity[] getPositionAsQuantityArray() throws DeviceException;
	
	/**
	 * Set offset(s) in amounts of external quantities as {@link ScannableMotion#setOffset(Double...)}, but allows any
	 * position container containing objects that may be quantities. 
	 * 
	 * {@link ScannableMotion#setOffset(Double...)}
	 * @param offsetPositionInExternalUnits
	 */
	// TODO make javadoc more specific.
	void setOffset(Object offsetPositionInExternalUnits);
}
