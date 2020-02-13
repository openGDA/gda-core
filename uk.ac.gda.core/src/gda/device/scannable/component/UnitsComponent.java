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

package gda.device.scannable.component;

import static gda.jscience.physics.units.NonSIext.ANGSTROM_STRING;
import static gda.jscience.physics.units.NonSIext.ANGSTROM_SYMBOL;
import static gda.jscience.physics.units.NonSIext.ANGSTROM_SYMBOL_ALTERNATIVE;
import static gda.jscience.physics.units.NonSIext.ANG_STRING;
import static gda.jscience.physics.units.NonSIext.CENTIGRADE_STRING;
import static gda.jscience.physics.units.NonSIext.COUNTS;
import static gda.jscience.physics.units.NonSIext.COUNTS_STRING;
import static gda.jscience.physics.units.NonSIext.COUNT_STRING;
import static gda.jscience.physics.units.NonSIext.CUBIC_METRE_STRING;
import static gda.jscience.physics.units.NonSIext.DEGREES_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_SYMBOL;
import static gda.jscience.physics.units.NonSIext.GIGAELECTRONVOLT_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNTS_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNTS_UC_STRING;
import static gda.jscience.physics.units.NonSIext.KILOCOUNT_STRING;
import static gda.jscience.physics.units.NonSIext.KILOELECTRONVOLT_STRING;
import static gda.jscience.physics.units.NonSIext.LITRE_U_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE_U_STRING;
import static gda.jscience.physics.units.NonSIext.MICROLITRE_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICROLITRE_STRING;
import static gda.jscience.physics.units.NonSIext.MICROLITRE_U_STRING;
import static gda.jscience.physics.units.NonSIext.MICRONS_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_MU_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_STRING;
import static gda.jscience.physics.units.NonSIext.MICRON_UM_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_MU_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_DEG_U_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_MU_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MICRO_RADIAN_U_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLIGRAMS_PER_MILLILITRE;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_DEG_ANGLE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE_LOWERCASE_STRING;
import static gda.jscience.physics.units.NonSIext.MILLI_RADIAN_ANGLE_STRING;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.AbstractUnit.ONE;
import static tec.units.indriya.unit.MetricPrefix.KILO;
import static tec.units.indriya.unit.MetricPrefix.MEGA;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.AMPERE;
import static tec.units.indriya.unit.Units.CUBIC_METRE;
import static tec.units.indriya.unit.Units.JOULE;
import static tec.units.indriya.unit.Units.KELVIN;
import static tec.units.indriya.unit.Units.LITRE;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.NEWTON;
import static tec.units.indriya.unit.Units.PASCAL;
import static tec.units.indriya.unit.Units.RADIAN;
import static tec.units.indriya.unit.Units.SECOND;
import static tec.units.indriya.unit.Units.VOLT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.jscience.physics.units.NonSIext;
import gda.util.QuantityFactory;

/**
 * This class provides Device classes with the functionality to convert between user-units and a lower-level hardware
 * unit.
 * <p>
 * It performs the user to hardware unit conversion, so that only numbers be exchanged between Device classes and lower
 * and higher level objects.
 * <p>
 * It has a list of acceptable units which the user-unit can dynamically switch between, although the hardware unit is
 * fixed.
 * <p>
 * It is expected that this class will be used as a component of a concrete device class.
 * <p>
 * This should not be used inside classes whose acceptable units are incompatible with each other e.g. a mono which
 * accepts angle, energy and wavelength as its position.
 */
public class UnitsComponent implements PositionConvertor {

	private static final Logger logger = LoggerFactory.getLogger(UnitsComponent.class);

	// the hardware's units
	private Unit<? extends Quantity<?>> hardwareUnit;

	// user units
	protected Unit<? extends Quantity<?>> userUnit = null;
	private List<String> acceptableUnits;

	private boolean userUnitHasBeenExplicitlySet;

	private boolean hardwareUnitHasBeenExplicitlySet;

	/**
	 * Constructor. Sets the hardware and user unit to the dimensionless ONE.
	 */
	public UnitsComponent() {
		try {
			setHardwareUnitString("");
			setUserUnits("");
		} catch (DeviceException e) {
			logger.error("Code logic error:", e);
		}
		userUnitHasBeenExplicitlySet = false;
		hardwareUnitHasBeenExplicitlySet = false;
	}

	/**
	 * Returns a string representation of the current reporting units
	 *
	 * @return Returns the reportingUnitsString.
	 */
	public String getUserUnitString() {
		return NonSIext.getUnitString(userUnit);
	}

	public boolean unitHasBeenSet() {
		return (userUnitHasBeenExplicitlySet || hardwareUnitHasBeenExplicitlySet);
	}

	/**
	 * @return the Unit object that is the current user unit
	 */
	public Unit<? extends Quantity<?>> getUserUnit() {
		return userUnit;
	}

	/**
	 * @return the Unit object that is the current user unit
	 */
	public Unit<? extends Quantity<?>> getHardwareUnit() {
		return hardwareUnit;
	}

	/**
	 * Sets the user unit to userUnit. If a hardware unit has not been explicitly set, then the hardware unit is also
	 * set to userUnit. If a hardware unit has been set, and the userUnit is not compatible with this, then a
	 * DeviceException is thrown.
	 *
	 * @param userUnitsString
	 *            The string representation of the unit.
	 * @throws DeviceException
	 *             if string not found in list of acceptable units
	 */
	public void setUserUnits(String userUnitsString) throws DeviceException {
		// Make hardware unit compatible if it has not been explicitly set
		if (!hardwareUnitHasBeenExplicitlySet) {
			hardwareUnit = QuantityFactory.createUnitFromString(userUnitsString);
			setCompatibleUnits();
		}
		actuallySetUserUnits(userUnitsString);
		userUnitHasBeenExplicitlySet = true;
	}

	protected void actuallySetUserUnits(String userUnitsString) throws DeviceException {
		// Check the user unit is acceptable (and create the unit)
		if (acceptableUnits.contains(userUnitsString)) {
			userUnit = QuantityFactory.createUnitFromString(userUnitsString);
		} else {
			userUnitStringIsNotAcceptable(userUnitsString);
		}
	}

	private void userUnitStringIsNotAcceptable(String userUnitsString) throws DeviceException {
		final String acceptableUnitsString = Arrays.deepToString(getAcceptableUnits());
		if (acceptableUnits.get(0).isEmpty()) {
			throw new DeviceException("User unit " + userUnitsString
					+ " is not acceptable. The current hardware unit is at its dimensionless default of ONE (settable with an empty string ''). Make sure that a sensible hardware unit has been set.");
		}
		throw new DeviceException(
				"User unit " + userUnitsString + " is not acceptable. Try one of " + acceptableUnitsString);
	}

	/**
	 * @return Returns the hardwareUnitString.
	 */
	public String getHardwareUnitString() {
		return NonSIext.getUnitString(hardwareUnit);
	}

	/**
	 * Sets the hardware unit to hardwareUnitString. If a user unit has not been explicitly set then the user unit is
	 * also set to hardwareUnitString. If the user unit has been explicitly set and the new hardware unit would
	 * invalidate this then a DeviceException is thrown.
	 * <p>
	 * Based on this string, this method will build a list of acceptable user-units. Afterwards, the acceptable units
	 * list can be added to via the addAcceptableUnits method if the defaults do not cover enough.
	 * <p>
	 *
	 * @param hardwareUnitString
	 *            The hardwareUnitString to use.
	 * @throws DeviceException
	 */
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		// If user unit has been set, check the new hardware unit is compatible
		if (userUnitHasBeenExplicitlySet && !acceptableUnits.contains(hardwareUnitString)) {
			throw new DeviceException(
					"The hardware unit could not be set to '" + hardwareUnitString + "' because this is incompatible"
							+ " with the explicitly set user unit '" + userUnit.toString() + "'");
		}

		actuallySetHardwareUnitString(hardwareUnitString);
		hardwareUnitHasBeenExplicitlySet = true;
		if (!userUnitHasBeenExplicitlySet) {
			try {
				actuallySetUserUnits(hardwareUnitString);
			} catch (DeviceException e) {
				throw new DeviceException("Error in setHardwareUnitString for " + hardwareUnitString, e);
			}
		}
	}

	private void actuallySetHardwareUnitString(String hardwareUnitString) {
		this.hardwareUnit = QuantityFactory.createUnitFromString(hardwareUnitString);
		setCompatibleUnits();
	}

	/**
	 * Should only be called once the hardware units have been set.
	 *
	 * @return Returns the acceptableUnitStrings.
	 */
	public String[] getAcceptableUnits() {
		return acceptableUnits.stream().toArray(String[]::new);
	}

	/**
	 * Adds a unit type to the list of acceptable units.
	 * <p>
	 * This should only be called after the setHardwareUnitString method as the list of acceptable units is rebuilt
	 * during that method.
	 *
	 * @param hardwareUnitString
	 */
	public void addAcceptableUnit(String hardwareUnitString) {
		final Unit<? extends Quantity<?>> newUnit = QuantityFactory.createUnitFromString(hardwareUnitString);

		if (newUnit.getSystemUnit() == this.hardwareUnit.getSystemUnit() && !this.acceptableUnits.contains(hardwareUnitString)) {
			this.acceptableUnits.add(hardwareUnitString);
		}
	}

	private List<String> generateCompatibleUnits(Unit<? extends Quantity<?>> unit) {
		final List<String> unitList = new ArrayList<>();

		if (unit.equals(ONE)) {
			unitList.add(ONE.toString());
			unitList.add("");
		}
		else if (unit.isCompatible(METRE)) {
			// these first two lines here so that they are not overwritten
			// unless we recognise the dimensions of the hardware units
			unitList.add(METRE.toString());
			unitList.add(NANO(METRE).toString());
			unitList.add(MILLI(METRE).toString());
			unitList.add(MICRO(METRE).toString());
			unitList.add(MICRON_STRING);
			unitList.add(MICRON_UM_STRING);
			unitList.add(MICRON_MU_STRING);
			unitList.add(MICRONS_STRING);
			unitList.add(ANG_STRING);
			unitList.add(ANGSTROM_STRING);
			unitList.add(ANGSTROM_SYMBOL);
			unitList.add(ANGSTROM_SYMBOL_ALTERNATIVE);
		}
		// angular motions
		else if (unit.isCompatible(RADIAN)) {
			unitList.add(RADIAN.toString());
			unitList.add(DEG_ANGLE_STRING);
			unitList.add(DEGREES_ANGLE_STRING);
			unitList.add(DEG_ANGLE_SYMBOL);
			unitList.add(MILLI_DEG_ANGLE_STRING);
			unitList.add(DEG_ANGLE_LOWERCASE_STRING);
			unitList.add(MILLI_DEG_ANGLE_LOWERCASE_STRING);
			unitList.add(MILLI_RADIAN_ANGLE_STRING);
			unitList.add(MILLI_RADIAN_ANGLE_LOWERCASE_STRING);
			unitList.add(MICRO_DEG_ANGLE_STRING);
			unitList.add(MICRO_DEG_MU_ANGLE_STRING);
			unitList.add(MICRO_DEG_U_ANGLE_STRING);
			unitList.add(MICRO_RADIAN_ANGLE_STRING);
			unitList.add(MICRO_RADIAN_U_ANGLE_STRING);
			unitList.add(MICRO_RADIAN_MU_ANGLE_STRING);
			unitList.add(MICRO_RADIAN_U_ANGLE_LOWERCASE_STRING);
			unitList.add(MICRO_RADIAN_ANGLE_LOWERCASE_STRING);
			unitList.add(MICRO_RADIAN_MU_ANGLE_LOWERCASE_STRING);
		}

		// // temperature
		else if (unit.isCompatible(KELVIN)) {
			unitList.add(CENTIGRADE_STRING);
			unitList.add(KELVIN.toString());
		}

		// Force
		else if (unit.isCompatible(NEWTON)) {
			unitList.add(NEWTON.toString());
		}
		// Voltage
		else if (unit.isCompatible(VOLT)) {
			unitList.add(VOLT.toString());
		}

		// Count
		else if (unit.isCompatible(COUNTS)) {
			unitList.add(COUNT_STRING);
			unitList.add(COUNTS_STRING);
			unitList.add(KILOCOUNT_STRING);
			unitList.add(KILOCOUNTS_STRING);
			unitList.add(KILOCOUNTS_UC_STRING);
		}

		// also want energy here
		else if (unit.isCompatible(JOULE)) {
			unitList.add(JOULE.toString());
			unitList.add(KILOELECTRONVOLT_STRING);
			unitList.add(ELECTRON_VOLT.toString());
			unitList.add(GIGAELECTRONVOLT_STRING);  //for the machine
		}

		else if (unit.isCompatible(AMPERE)) {
			unitList.add(AMPERE.toString());
			unitList.add(MICROAMPERE_STRING);
			unitList.add(MICROAMPERE_MU_STRING);
			unitList.add(MICROAMPERE_U_STRING);
			unitList.add(MILLI(AMPERE).toString());
		}

		else if (unit.isCompatible(SECOND)) {
			unitList.add(SECOND.toString());
			unitList.add(MILLI(SECOND).toString());
		}

		else if (unit.isCompatible(CUBIC_METRE)) {
			unitList.add(LITRE.toString());
			unitList.add(CUBIC_METRE.toString());
			unitList.add(LITRE_U_STRING);
			unitList.add(MICROLITRE_STRING);
			unitList.add(MICROLITRE_U_STRING);
			unitList.add(MICROLITRE_MU_STRING);
			unitList.add(CUBIC_METRE_STRING);
		}

		else if (unit.isCompatible(MILLIGRAMS_PER_MILLILITRE)) {
			// mg/mL
			unitList.add(MILLIGRAMS_PER_MILLILITRE.toString());
		}

		else if (unit.isCompatible(PASCAL)) {
			unitList.add(PASCAL.toString());
			unitList.add(MILLI(PASCAL).toString());
			unitList.add(KILO(PASCAL).toString());
			unitList.add(MEGA(PASCAL).toString());
		}

		else {
			throw new IllegalArgumentException("Hardware unit " + unit + " not supported.");
		}
		return unitList;
	}

	/**
	 * Creates a vector of units which match the given unit. The if statement should be extended as more types of units
	 * are used in the GDA
	 */
	private void setCompatibleUnits() {
		acceptableUnits = generateCompatibleUnits(this.hardwareUnit);
	}

	/*
	 * The position passed to asynchronousMoveTo and returned by getPosition will be in the external (user)
	 * representation. getPosition will return the amount of the the Scannable's position Quantity in external (user)
	 * units as a Double (or array of Doubles). getPositionQuantity will return the position as a quantity in external
	 * (user) units. asynchronousMoveTo asynchronousMoveTo will accept a Quantity that is compatible with the internal
	 * (hardware) unit. A String will be parsed into quantity. Otherwise it will take will take anything that {@link
	 * ScannableMotionBase}'s will take (except an arbitrary String).
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**
	 * Converts a position array to its representation in external (user) units, and returns the amount of each element
	 * as a Double. Assumes the input, if not a quantity, or string parseable to a quantity, to be parseable to a Double
	 * and in internal (hardware) units. If neither external (user) unit or internal (hardware) unit has been set then
	 * the object is not altered.
	 *
	 * @param internalPosition
	 * @return externalPosition
	 */
	@Override
	public Object internalTowardExternal(final Object internalPosition) {
		if (internalPosition == null) {
			return null;
		}

		if (!unitHasBeenSet()) {
			return internalPosition;
		}

		final Object[] internalObjectArray = PositionConvertorFunctions.toObjectArray(internalPosition);

		// Amounts by definition are in internal (hardware) units, so make this explicit
		final Quantity[] internalQuantityArray = PositionConvertorFunctions.toQuantityArray(internalObjectArray, (Unit) hardwareUnit);

		// Convert to external (user) units
		final Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(internalQuantityArray, (Unit) userUnit);

		// Retrieve the amounts
		final Double[] externalAmountArray = PositionConvertorFunctions.toAmountArray(externalQuantityArray);

		return PositionConvertorFunctions.toParticularContainer(externalAmountArray, internalPosition);
	}

	/**
	 * Converts a position array to its representation in internal (hardware) units, and returns the amount of each
	 * element as a Double. Assumes the input, if not a quantity, or string parseable to a quantity, to be parseable to
	 * a Double and in external (user) units. If neither external (user) unit or internal (hardware) unit has been set then
	 * the object is not altered.
	 *
	 * @param externalPosition
	 * @return internalPosition
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object externalTowardInternal(Object externalPosition) {

		if (externalPosition == null) {
			return null;
		}

		if (!unitHasBeenSet()) {
			return externalPosition;
		}

		final Object[] externalObjectArray = PositionConvertorFunctions.toObjectArray(externalPosition);

		// Amounts by definition are in external (user) units, so make this explicit
		final Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalObjectArray, (Unit) userUnit);

		// Convert to internal (hardware) units
		final Quantity[] internalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalQuantityArray, (Unit) hardwareUnit);

		// Retrieve the amounts
		final Double[] internalAmountArray = PositionConvertorFunctions.toAmountArray(internalQuantityArray);

		return PositionConvertorFunctions.toParticularContainer(internalAmountArray, externalPosition);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object toExternalAmount(Object externalPosition) {
		final Object[] externalObjectArray = PositionConvertorFunctions.toObjectArray(externalPosition);

		// Amounts by definition are in external (user) units, so make this explicit
		final Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalObjectArray, (Unit) userUnit);

		// Retrieve the amounts
		final Double[] externalAmountArray = PositionConvertorFunctions.toAmountArray(externalQuantityArray);

		return PositionConvertorFunctions.toParticularContainer(externalAmountArray, externalPosition);
	}

	@Override
	public String toString() {
		return "UnitsComponent [hardwareUnit=" + hardwareUnit + ", userUnit=" + userUnit + ", acceptableUnits="
				+ acceptableUnits + ", userUnitHasBeenExplicitlySet=" + userUnitHasBeenExplicitlySet
				+ ", hardwareUnitHasBeenExplicitlySet=" + hardwareUnitHasBeenExplicitlySet + "]";
	}
}
