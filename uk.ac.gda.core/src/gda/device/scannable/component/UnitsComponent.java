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

import static gda.jscience.physics.units.NonSIext.ANG;
import static gda.jscience.physics.units.NonSIext.ANGSTROM;
import static gda.jscience.physics.units.NonSIext.CENTIGRADE;
import static gda.jscience.physics.units.NonSIext.COUNT;
import static gda.jscience.physics.units.NonSIext.DEGREES_ANGLE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.GIGAELECTRONVOLT;
import static gda.jscience.physics.units.NonSIext.KILOCOUNT;
import static gda.jscience.physics.units.NonSIext.KILOELECTRONVOLT;
import static gda.jscience.physics.units.NonSIext.MICROAMPERE;
import static gda.jscience.physics.units.NonSIext.MICRON;
import static gda.jscience.physics.units.NonSIext.MICRONS;
import static gda.jscience.physics.units.NonSIext.MICRON_UM;
import static gda.jscience.physics.units.NonSIext.mDEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.mDEG_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.mRADIAN_ANGLE;
import static gda.jscience.physics.units.NonSIext.mRADIAN_ANGLE_LC;
import static gda.jscience.physics.units.NonSIext.uAMPERE;
import static gda.jscience.physics.units.NonSIext.uDEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.uRADIAN_ANGLE;
import static gda.jscience.physics.units.NonSIext.uRADIAN_ANGLE_LC;
import static org.jscience.physics.units.NonSI.ELECTRON_VOLT;
import static org.jscience.physics.units.NonSI.LITER;
import static org.jscience.physics.units.SI.AMPERE;
import static org.jscience.physics.units.SI.CUBIC_METER;
import static org.jscience.physics.units.SI.KELVIN;
import static org.jscience.physics.units.SI.KILOGRAM;
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MICRO;
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.NANO;
import static org.jscience.physics.units.SI.NEWTON;
import static org.jscience.physics.units.SI.RADIAN;
import static org.jscience.physics.units.SI.SECOND;
import static org.jscience.physics.units.SI.VOLT;
import static org.jscience.physics.units.Unit.ONE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Dimensionless;
import org.jscience.physics.quantities.Duration;
import org.jscience.physics.quantities.ElectricCurrent;
import org.jscience.physics.quantities.ElectricPotential;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Force;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.quantities.Temperature;
import org.jscience.physics.quantities.Volume;
import org.jscience.physics.quantities.VolumetricDensity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.jscience.physics.quantities.Count;
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
	private Unit<? extends Quantity> hardwareUnit;

	// user units
	protected Unit<? extends Quantity> userUnit = null;
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
		return getUserUnit().toString();
	}

	public boolean unitHasBeenSet() {
		return (userUnitHasBeenExplicitlySet || hardwareUnitHasBeenExplicitlySet);
	}

	/**
	 * @return the Unit object that is the current user unit
	 */
	public Unit<? extends Quantity> getUserUnit() {
		return userUnit;
	}

	/**
	 * @return the Unit object that is the current user unit
	 */
	public Unit<? extends Quantity> getHardwareUnit() {
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

	@SuppressWarnings("unchecked")
	protected void actuallySetUserUnits(String userUnitsString) throws DeviceException {
		// Check the user unit is acceptable (and create the unit)
		if (acceptableUnits.contains(userUnitsString)) {
			userUnit = Unit.valueOf(userUnitsString);
		} else {
			userUnitStringIsNotAcceptable(userUnitsString);
		}
	}

	private void userUnitStringIsNotAcceptable(String userUnitsString) throws DeviceException {
		final String acceptableUnitsString = Arrays.deepToString(getAcceptableUnits());
		if (acceptableUnits.get(0).equals(ONE.toString())) {
			throw new DeviceException(
					"User unit "
							+ userUnitsString
							+ " is not acceptable. The current hardware unit is at its dimensionless default of ONE (settable with an empty string ''). Make sure that a sensible hardware unit has been set.");
		}
		throw new DeviceException("User unit " + userUnitsString + " is not acceptable. Try one of "
				+ acceptableUnitsString);

	}

	/**
	 * @return Returns the hardwareUnitString.
	 */
	public String getHardwareUnitString() {
		return hardwareUnit.toString();
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
		if (userUnitHasBeenExplicitlySet) {
			// Check the new hardware unit is compatible
			@SuppressWarnings("unchecked")
			final Unit<? extends Quantity> unit = Unit.valueOf(hardwareUnitString);
			final List<String> compatibleUnits = generateCompatibleUnits(unit);
			if (!compatibleUnits.contains(userUnit.toString())) {
				throw new DeviceException("The hardware unit could not be set to '" + hardwareUnitString
						+ "' because this is incompatible" + " with the explicitely set user unit '"
						+ userUnit.toString() + "'");
			}
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
		final Unit<? extends Quantity> newUnit = QuantityFactory.createUnitFromString(hardwareUnitString);

		if (newUnit.getBaseUnits() == this.hardwareUnit.getBaseUnits() && !this.acceptableUnits.contains(hardwareUnitString)) {
			this.acceptableUnits.add(hardwareUnitString);
		}
	}

	private List<String> generateCompatibleUnits(Unit<? extends Quantity> unit) {
		final List<String> unitList = new ArrayList<>();
		final Quantity hardwareUnitQuantity = Quantity.valueOf(1.0, unit);

		if (hardwareUnitQuantity instanceof Length) {
			// these first two lines here so that they are not overwritten
			// unless we recognise the dimensions of the hardware units
			unitList.add(METER.toString());
			unitList.add(NANO(METER).toString());
			unitList.add(MILLI(METER).toString());
			unitList.add(MICRO(METER).toString());
			unitList.add(MICRON.toString());
			unitList.add(MICRON_UM.toString());
			unitList.add(MICRONS.toString());
			unitList.add(ANG.toString());
			unitList.add(ANGSTROM.toString());
		}
		// angular motions
		else if (hardwareUnitQuantity instanceof Angle) {
			unitList.add(RADIAN.toString());
			unitList.add(DEG_ANGLE.toString());
			unitList.add(DEGREES_ANGLE.toString());
			unitList.add(mDEG_ANGLE.toString());
			unitList.add(DEG_ANGLE_LOWERCASE.toString());
			unitList.add(mDEG_ANGLE_LOWERCASE.toString());
			unitList.add(mRADIAN_ANGLE.toString());
			unitList.add(mRADIAN_ANGLE_LC.toString());
			unitList.add(uDEG_ANGLE.toString());
			unitList.add(uRADIAN_ANGLE.toString());
			unitList.add(uRADIAN_ANGLE_LC.toString());
		}

		// // temperature
		else if (hardwareUnitQuantity instanceof Temperature) {
			unitList.add(CENTIGRADE.toString());
			unitList.add(KELVIN.toString());
		}

		// Force
		else if (hardwareUnitQuantity instanceof Force) {
			unitList.add(NEWTON.toString());
		}
		// Voltage
		else if (hardwareUnitQuantity instanceof ElectricPotential) {
			unitList.add(VOLT.toString());
		}

		// Count
		else if (hardwareUnitQuantity instanceof Count) {
			unitList.add(COUNT.toString());
			unitList.add(KILOCOUNT.toString());
		}

		// also want energy here
		else if (hardwareUnitQuantity instanceof Energy) {
			unitList.add(KILOELECTRONVOLT.toString());
			unitList.add(ELECTRON_VOLT.toString());
			unitList.add(GIGAELECTRONVOLT.toString());  //for the machine
		}

		else if (hardwareUnitQuantity instanceof Dimensionless) {
			unitList.add(ONE.toString());
		}

		else if (hardwareUnitQuantity instanceof ElectricCurrent) {
			unitList.add(AMPERE.toString());
			unitList.add(MICROAMPERE.toString());
			unitList.add(uAMPERE.toString());
			unitList.add(MILLI(AMPERE).toString());
		}

		else if (hardwareUnitQuantity instanceof Duration) {
			unitList.add(SECOND.toString());
			unitList.add(MILLI(SECOND).toString());
		}

		else if (hardwareUnitQuantity instanceof Volume) {
			unitList.add(LITER.toString());
			unitList.add(CUBIC_METER.toString());
		}

		else if (hardwareUnitQuantity instanceof VolumetricDensity) {
			// mg/mL
			unitList.add(MICRO(KILOGRAM).divide(MILLI(LITER)).toString());
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
		final Quantity[] internalQuantityArray = PositionConvertorFunctions.toQuantityArray(internalObjectArray, hardwareUnit);

		// Convert to external (user) units
		final Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(internalQuantityArray, userUnit);

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
		final Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalObjectArray, userUnit);

		// Convert to internal (hardware) units
		final Quantity[] internalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalQuantityArray, hardwareUnit);

		// Retrieve the amounts
		final Double[] internalAmountArray = PositionConvertorFunctions.toAmountArray(internalQuantityArray);

		return PositionConvertorFunctions.toParticularContainer(internalAmountArray, externalPosition);
	}

	public Object toExternalAmount(Object externalPosition) {
		final Object[] externalObjectArray = PositionConvertorFunctions.toObjectArray(externalPosition);

		// Amounts by definition are in external (user) units, so make this explicit
		final Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalObjectArray, userUnit);

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
