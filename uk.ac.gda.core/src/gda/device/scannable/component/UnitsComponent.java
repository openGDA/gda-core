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
import static gda.jscience.physics.units.NonSIext.KILOCOUNT;
import static gda.jscience.physics.units.NonSIext.DEGREES_ANGLE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.DEG_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.KILOELECTRONVOLT;
import static gda.jscience.physics.units.NonSIext.MICRON;
import static gda.jscience.physics.units.NonSIext.MICRONS;
import static gda.jscience.physics.units.NonSIext.MICRON_UM;
import static gda.jscience.physics.units.NonSIext.mDEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.mDEG_ANGLE_LOWERCASE;
import static gda.jscience.physics.units.NonSIext.mRADIAN_ANGLE;
import static gda.jscience.physics.units.NonSIext.mRADIAN_ANGLE_LC;
import static gda.jscience.physics.units.NonSIext.uDEG_ANGLE;
import static gda.jscience.physics.units.NonSIext.uRADIAN_ANGLE;
import static gda.jscience.physics.units.NonSIext.uRADIAN_ANGLE_LC;
import static org.jscience.physics.units.SI.KELVIN;
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MICRO;
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.NANO;
import static org.jscience.physics.units.SI.NEWTON;
import static org.jscience.physics.units.SI.RADIAN;
import static org.jscience.physics.units.SI.VOLT;
import static org.jscience.physics.units.Unit.ONE;
import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.jscience.physics.quantities.Count;
import gda.jscience.physics.units.NonSIext;
import gda.util.QuantityFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.jacorb.orb.Any;
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
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private List<Unit<? extends Quantity>> acceptableUnits;

	private boolean userUnitHasBeenExplicitelySet;

	private boolean hardwareUnitHasBeenExplicitelySet;

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
		userUnitHasBeenExplicitelySet = false;
		hardwareUnitHasBeenExplicitelySet = false;
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
		return (userUnitHasBeenExplicitelySet | hardwareUnitHasBeenExplicitelySet);
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
		if (!hardwareUnitHasBeenExplicitelySet) {
			hardwareUnit = QuantityFactory.createUnitFromString(userUnitsString);
			setCompatibleUnits();
		}
		actuallySetUserUnits(userUnitsString);
		userUnitHasBeenExplicitelySet = true;
	}

	protected void actuallySetUserUnits(String userUnitsString) throws DeviceException {

		// Check the user unit is acceptable (and create the unit)
		Unit<? extends Quantity> unitToSet = null;
		for (Unit<? extends Quantity> unit : this.acceptableUnits) {
			if (unit.toString().equals(userUnitsString)) {
				unitToSet = unit;
			}
		}

		// Exception if user unit not compatable with hardware unit
		if (unitToSet == null) {
			userUnitStringIsNotAcceptable(userUnitsString);
		}

		// Set the user unit
		userUnit = unitToSet;
	}

	private void userUnitStringIsNotAcceptable(String userUnitsString) throws DeviceException {
		String acceptableUnitsString = Arrays.deepToString(getAcceptableUnits());
		if (acceptableUnits.get(0) == ONE) {
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
		if (userUnitHasBeenExplicitelySet) {
			// Check the new hardware unit is compatible
			if (!generateCompatibleUnits(hardwareUnitString).contains(userUnit)) {
				throw new DeviceException("The hardware unit could not be set to '" + hardwareUnitString
						+ "' because this is incompatible" + " with the explicitely set user unit '"
						+ userUnit.toString() + "'");
			}
		}

		actuallySetHardwareUnitString(hardwareUnitString);
		hardwareUnitHasBeenExplicitelySet = true;
		if (!userUnitHasBeenExplicitelySet) {
			try {
				actuallySetUserUnits(hardwareUnitString);
			} catch (DeviceException e) {
				throw new Error("Error in setHardwareUnitString for " + hardwareUnitString, e);
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
		String[] output = new String[0];
		for (Unit<? extends Quantity> unit : this.acceptableUnits) {
			output = (String[]) ArrayUtils.add(output, unit.toString());
		}
		return output;
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
		Unit<?> newUnit = QuantityFactory.createUnitFromString(hardwareUnitString);

		if (newUnit.getBaseUnits() == this.hardwareUnit.getBaseUnits() && !this.acceptableUnits.contains(newUnit)) {
			this.acceptableUnits.add(newUnit);
		}
	}

	/**
	 * Converts whatever the supplied object is into a Double in hardware units, if the supplied object is compatible.
	 * Unless the object is a string containing a unit symbol or a quantity, the value is assumed to be in user units.
	 * <p>
	 * This can take strings, numbers or Jython native numbers.
	 * <p>
	 * Acceptable strings would be of the form: "10" or "10 mm" or "10mm".
	 * 
	 * @param position
	 * @return a Double in hardware units of the given position
	 */
	@Deprecated
	public Double convertObjectToHardwareUnitsAssumeUserUnits(Object position) {
		Quantity targetPosition = convertObjectToQuantityInUserUnits(position);

		// convert the quantity into a quantity that is of the units that
		if (targetPosition != null) {
			return targetPosition.to(this.hardwareUnit).getAmount();
		}

		return null;
	}

	/**
	 * Converts whatever the supplied object is into a Double in hardware units, if the supplied object is compatible.
	 * Unless the object is a string containing a unit symbol or a quantity, the value is assumed to be in hardware
	 * units.
	 * <p>
	 * This can take strings, numbers or Jython native numbers.
	 * <p>
	 * Acceptable strings would be of the form: "10" or "10 mm" or "10mm".
	 * 
	 * @param position
	 * @return a Double in hardware units of the given position
	 */
	@Deprecated 
	public Double convertObjectToHardwareUnitsAssumeHardwareUnits(Object position) {
		Quantity targetPosition = convertObjectToQuantityInHardwareUnits(position);

		// convert the quantity into a quantity that is of the units that
		if (targetPosition != null) {
			return targetPosition.to(this.hardwareUnit).getAmount();
		}

		return null;
	}

	/**
	 * Converts whatever the supplied is into a Double in the current user units, if the supplied object is
	 * compatible.Unless the object is a string containing a unit symbol or a quantity, the value is assumed to be in
	 * hardware units.
	 * <p>
	 * This can take strings, numbers or Jython native numbers.
	 * <p>
	 * Acceptable strings would be of the form: "10" or "10 mm" or "10mm".
	 * 
	 * @param position
	 * @return a Double in user units of the given position
	 */
	@Deprecated 
	public Double convertObjectToUserUnitsAssumeHardwareUnits(Object position) {
		Quantity targetPosition = convertObjectToQuantityInHardwareUnits(position);

		// convert the quantity into a quantity that is of the units that
		if (targetPosition != null) {
			return targetPosition.to(this.userUnit).getAmount();
		}
		return null;
	}

	/**
	 * Array version of convertObjectToUserUnitsAssumeHardwareUnits
	 * 
	 * @param position
	 * @return Double[]
	 */
	@Deprecated 
	public Double[] convertObjectToUserUnitsAssumeHardwareUnits(Object[] position) {

		Double[] result = new Double[position.length];

		for (int i = 0; i < position.length; i++) {
			Quantity targetPosition = convertObjectToQuantityInHardwareUnits(position[i]);
			// convert the quantity into a quantity that is of the units that
			if (targetPosition != null) {
				result[i] = targetPosition.to(this.userUnit).getAmount();
			}
		}
		return result;
	}

	/**
	 * Converts whatever the supplied is into a Double in the current user units, if the supplied object is
	 * compatible.Unless the object is a string containing a unit symbol or a quantity, the value is assumed to be in
	 * user units.
	 * <p>
	 * This can take strings, numbers or Jython native numbers.
	 * <p>
	 * Acceptable strings would be of the form: "10" or "10 mm" or "10mm".
	 * 
	 * @param position
	 * @return a Double in user units of the given position
	 */
	@Deprecated 
	public Double convertObjectToUserUnitsAssumeUserUnits(Object position) {
		Quantity targetPosition = convertObjectToQuantityInUserUnits(position);

		// convert the quantity into a quantity that is of the units that
		if (targetPosition != null) {
			return targetPosition.to(this.userUnit).getAmount();
		}
		return null;
	}

	/**
	 * This converts a variety of objects into a Quantity in user units. Returns null if the object was unacceptable.
	 * 
	 * @param position
	 * @return a Quantity which if not implied from the given object will be in user units of the given position
	 */
	@Deprecated 
	public Quantity convertObjectToQuantityInUserUnits(Object position) {
		Quantity targetPosition = convertObjectToQuantity(position, userUnit);
		return targetPosition;

	}

	/**
	 * This converts a variety of objects into a Quantity in hardware units. Returns null if the object was
	 * unacceptable.
	 * 
	 * @param position
	 * @return a Quantity which if not implied from the given object will be in hardware units of the given position
	 */
	@Deprecated 
	public Quantity convertObjectToQuantityInHardwareUnits(Object position) {
		Quantity targetPosition = convertObjectToQuantity(position, hardwareUnit);
		return targetPosition;
	}

	/**
	 * Converts whatever position is into a Quantity of units targetUnit
	 * 
	 * @param position
	 * @param targetUnit
	 * @return Quantity
	 */
	@Deprecated 
	public Quantity convertObjectToQuantity(final Object position, final Unit<?> targetUnit) {
		Quantity quantity = null;
		if (position instanceof String) {
			// if the string has no units in its then this will return a dimensionless quantity
			quantity = QuantityFactory.createFromString((String) position);
			if (position instanceof org.jscience.physics.quantities.Dimensionless) {
				quantity = QuantityFactory.createFromObject(((org.jscience.physics.quantities.Dimensionless) position)
						.getAmount(), targetUnit);
			}
		} else if (position instanceof PyString) {
			quantity = QuantityFactory.createFromString(((PyString) position).toString());
			if (quantity instanceof org.jscience.physics.quantities.Dimensionless) {
				return QuantityFactory.createFromObject(((org.jscience.physics.quantities.Dimensionless) position)
						.getAmount(), targetUnit);
			}
		} else if (position instanceof Double) {
			quantity = Quantity.valueOf((Double) position, targetUnit);
		} else if (position instanceof Any) {
			quantity = Quantity.valueOf(Double.valueOf(position.toString()), targetUnit);
		} else if (position instanceof Integer) {
			quantity = Quantity.valueOf((Integer) position, targetUnit);
		} else if (position instanceof PyFloat) {
			quantity = Quantity.valueOf(((PyFloat) position).getValue(), targetUnit);
		} else if (position instanceof PyInteger) {
			quantity = Quantity.valueOf(Double.valueOf(((PyInteger) position).getValue()), targetUnit);
		} else if (position instanceof org.jscience.physics.quantities.Dimensionless) {
			// convert dimensionless to the target
			quantity = QuantityFactory.createFromObject(((org.jscience.physics.quantities.Dimensionless) position)
					.getAmount(), targetUnit);
		} else if (position instanceof Quantity) {
			quantity = ((Quantity) position).to(targetUnit);
		} else {
			return null;
		}
		return quantity;
	}

	private ArrayList<Unit<? extends Quantity>> generateCompatibleUnits(String hardwareUnitString) {
		ArrayList<Unit<? extends Quantity>> unitList = new ArrayList<Unit<? extends Quantity>>();
		Quantity hardwareUnitQuantity = QuantityFactory.createFromTwoStrings("1.0", hardwareUnitString);

		if (hardwareUnitQuantity == null) {
			throw new IllegalArgumentException("Hardware unit string " + hardwareUnitString + " not supportd.");
		}
		if (hardwareUnitQuantity instanceof Length) {
			// these first two lines here so theat they are not overwritten
			// unless we recognise the dimensions of the hardware units
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(METER);
			unitList.add(NANO((METER)));
			unitList.add(MILLI((METER)));
			unitList.add(MICRO((METER)));
			unitList.add(MICRON);
			unitList.add(MICRON_UM);
			unitList.add(ANG);
			unitList.add(ANGSTROM);
			unitList.add(MICRON);
			unitList.add(MICRONS);
			unitList.add(METER);
		}
		// angular motions
		else if (hardwareUnitQuantity instanceof Angle) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(RADIAN);
			unitList.add(DEG_ANGLE);
			unitList.add(DEGREES_ANGLE);
			unitList.add(mDEG_ANGLE);
			unitList.add(DEG_ANGLE_LOWERCASE);
			unitList.add(mDEG_ANGLE_LOWERCASE);
			unitList.add(mRADIAN_ANGLE);
			unitList.add(mRADIAN_ANGLE_LC);
			unitList.add(uDEG_ANGLE);
			unitList.add(uRADIAN_ANGLE);
			unitList.add(uRADIAN_ANGLE_LC);
		}

		// // temperature
		else if (hardwareUnitQuantity instanceof Temperature) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(CENTIGRADE);
			unitList.add(KELVIN);
		}
		
		// Force
		else if (hardwareUnitQuantity instanceof Force) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(NEWTON);
		}
		// Voltage
		else if (hardwareUnitQuantity instanceof ElectricPotential) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(VOLT);
		}
		
		// Count
		else if (hardwareUnitQuantity instanceof Count) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(COUNT);
			unitList.add(KILOCOUNT);
		}

		// also want energy here
		else if (hardwareUnitQuantity instanceof Energy) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(KILOELECTRONVOLT);
			unitList.add(NonSI.ELECTRON_VOLT);
			unitList.add(NonSIext.GIGAELECTRONVOLT);  //for the machine
		}

		else if (hardwareUnitQuantity instanceof Dimensionless) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(ONE);
		}

		else if (hardwareUnitQuantity instanceof ElectricCurrent) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(SI.AMPERE);
			unitList.add(NonSIext.MICROAMPERE);
			unitList.add(NonSIext.uAMPERE);
			unitList.add(MILLI((SI.AMPERE)));
		}

		else if (hardwareUnitQuantity instanceof Duration) {
			unitList = new ArrayList<Unit<? extends Quantity>>();
			unitList.add(SI.SECOND);
			unitList.add(MILLI((SI.SECOND)));
		}
		
		else {
			throw new IllegalArgumentException("Hardware unit string " + hardwareUnitString + " not supported.");
		}
		return unitList;
	}

	/**
	 * Creates a vector of units which match the given unit. The if statement should be extended as more types of units
	 * are used in the GDA
	 */
	private void setCompatibleUnits() {
		acceptableUnits = generateCompatibleUnits(this.hardwareUnit.toString());
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
		
		Object[] internalObjectArray = PositionConvertorFunctions.toObjectArray(internalPosition);
		
		// Amounts by definition are in internal (hardware) units, so make this explicit
		Quantity[] internalQuantityArray = PositionConvertorFunctions.toQuantityArray(internalObjectArray, hardwareUnit);

		// Convert to external (user) units
		Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(internalQuantityArray, userUnit);
		
		// Retrieve the amounts
		Double[] externalAmountArray = PositionConvertorFunctions.toAmountArray(externalQuantityArray);
		
		// 
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
		
		Object[] externalObjectArray = PositionConvertorFunctions.toObjectArray(externalPosition);
		
		// Amounts by definition are in external (user) units, so make this explicit
		Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalObjectArray, userUnit);

		// Convert to internal (hardware) units
		Quantity[] internalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalQuantityArray, hardwareUnit);
		
		// Retrieve the amounts
		Double[] internalAmountArray = PositionConvertorFunctions.toAmountArray(internalQuantityArray);
		
		return PositionConvertorFunctions.toParticularContainer(internalAmountArray, externalPosition);
	}
	
	public Object toExternalAmount(Object externalPosition) {
		Object[] externalObjectArray = PositionConvertorFunctions.toObjectArray(externalPosition);
		
		// Amounts by definition are in external (user) units, so make this explicit
		Quantity[] externalQuantityArray = PositionConvertorFunctions.toQuantityArray(externalObjectArray, userUnit);
	
		// Retrieve the amounts
		Double[] externalAmountArray = PositionConvertorFunctions.toAmountArray(externalQuantityArray);
		
		return PositionConvertorFunctions.toParticularContainer(externalAmountArray, externalPosition);
	}
}
