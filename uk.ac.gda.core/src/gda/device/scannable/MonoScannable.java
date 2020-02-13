/*-
 * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
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

package gda.device.scannable;

import static si.uom.NonSI.ANGSTROM;
import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.KILO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.JOULE;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.RADIAN;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jscience.physics.quantities.QuantityConverters;
import gda.util.QuantityFactory;
import tec.units.indriya.quantity.Quantities;

/**
 * Operates an angular motor controlling a mono
 * <p>
 * This can accept and return the mono position as angle, wavelength or energy.
 */
public class MonoScannable extends ScannableMotionUnitsBase {

	private static final Logger logger = LoggerFactory.getLogger(MonoScannable.class);

	// attributes describing the crystal in use
	private Quantity<Length> twoDee;
	private double[] twoDValues = { 3.275, 3.840, 6.271, 2.0903 };
	private String crystalType = "unknown";
	private String[] knownCrystalTypes = { "Si311", "Si220", "Si111", "Si333" };

	// the motor
	private String motorName;
	private ScannableMotor theMotor;

	// handles user-unit to motor-unit conversion
	private String motorUnitString;
	private Unit<? extends Quantity<?>> motorUnit;
	private Unit<? extends Quantity<?>>[] acceptableUnits = new Unit<?>[0];
	private Unit<? extends Quantity<?>> userUnits = QuantityFactory.createUnitFromString("nm");
	private String initialUserUnits = null;

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		theMotor = Finder.getInstance().find(motorName);

		if (this.inputNames.length == 1 && this.inputNames[0].equals("value")) {
			this.inputNames = new String[] { getName() };
		}

		try {
			motorUnit = QuantityFactory.createUnitFromString(this.motorUnitString);
			if (initialUserUnits == null) {
				userUnits = motorUnit;
			} else {
				userUnits = QuantityFactory.createUnitFromString(initialUserUnits);
			}

			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, MILLI(DEGREE_ANGLE));
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, DEGREE_ANGLE);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, ANGSTROM);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, NANO(METRE));
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, ELECTRON_VOLT);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, KILO(ELECTRON_VOLT));

			setConfigured(true);
		} catch (Exception e) {
			throw new FactoryException("Exception during configure of " + getName() + " (motor=" + StringUtils.quote(motorName) + ")", e);
		}
	}

	/**
	 * @param twoD
	 *            2d expressed in Angstroms
	 */
	public void setTwoD(double twoD) {
		twoDee = Quantities.getQuantity(twoD, ANGSTROM);
	}

	public void setCrystalType(String crystalType) {
		this.crystalType = crystalType;
		twoDee = Quantities.getQuantity(twoDFromCrystalType(crystalType), ANGSTROM);
	}

	public double getTwoD() {
		return (twoDee != null) ? twoDee.to(ANGSTROM).getValue().doubleValue() : 0.0;
	}

	public String getCrystalType() {
		return crystalType;
	}

	@Override
	public String getUserUnits() {
		return this.userUnits.toString();
	}

	@Override
	public void setUserUnits(String userUnitsString) throws DeviceException {

		Unit<? extends Quantity<?>> newUnits = QuantityFactory.createUnitFromString(userUnitsString);
		if (isConfigured()) {
			if (ArrayUtils.contains(this.acceptableUnits, newUnits)) {
				this.userUnits = newUnits;
			}
		} else {
			this.initialUserUnits = userUnitsString;
		}
	}

	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		Quantity<? extends Quantity<?>> newUnitClass = QuantityFactory.createFromTwoStrings("1.0", newUnit);
		if (newUnitClass.getUnit() instanceof Angle || newUnitClass.getUnit() instanceof Energy || newUnitClass.getUnit() instanceof Length) {
			this.acceptableUnits = (Unit<?>[]) ArrayUtils.add(this.acceptableUnits, newUnitClass.getUnit());
		}
	}

	@Override
	public String[] getAcceptableUnits() {
		String[] output = new String[this.acceptableUnits.length];

		for (int i = 0; i < this.acceptableUnits.length; i++) {
			output[i] = this.acceptableUnits[i].toString();
		}
		return output;
	}

	@Override
	public String getHardwareUnitString() {
		return this.motorUnitString;
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) {
		this.motorUnitString = hardwareUnitString;
	}

	public String getMotorName() {
		return motorName;
	}

	public void setMotorName(String motorName) {
		this.motorName = motorName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		final Quantity<? extends Quantity<?>> newQuantity = QuantityFactory.createFromObjectUnknownUnit(position, userUnits);

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned
		Quantity<Angle> angle = null;
		final Unit<? extends Quantity<?>> hardwareUnit = QuantityFactory.createUnitFromString(getHardwareUnitString());
		if (newQuantity.getUnit().isCompatible(RADIAN)) {
			angle = newQuantity.asType(Angle.class).to(RADIAN);
		} else if (newQuantity.getUnit().isCompatible(METRE)) {
			final Quantity<Angle> braggangle = QuantityConverters.braggAngleFromWavelength(newQuantity.asType(Length.class).to(METRE), twoDee);
			angle = ((Quantity) braggangle).to(hardwareUnit).to(RADIAN);
		} else if (newQuantity.getUnit().isCompatible(JOULE)) {
			final Quantity<Angle> braggangle = QuantityConverters.braggAngleFromEnergy(((Quantity<Energy>) newQuantity).to(JOULE), twoDee);
			angle = ((Quantity) braggangle).to(hardwareUnit).to(RADIAN);
		}

		if (angle != null) {
			// move motor to the angle
			this.theMotor.moveTo(angle.to(RADIAN).getValue().doubleValue());
		} else {
			throw new DeviceException("MonoScannable.rawAsynchronousMoveTo(): Null position/quantity specified.");
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getPosition() throws DeviceException {
		double[] angle;
		angle = ScannableUtils.getCurrentPositionArray(this.theMotor);

		Quantity<? extends Quantity<?>> currentPosition = QuantityFactory.createFromObjectUnknownUnit(angle[0], this.motorUnit);
		Quantity<? extends Quantity<?>> toReturn = QuantityFactory.createFromObjectUnknownUnit(1.0, userUnits);
		if (toReturn.getUnit() instanceof Angle) {
			toReturn = ((Quantity) currentPosition).to(userUnits);
		} else if (toReturn.getUnit() instanceof Length) {
			Quantity<Length> wavelength = QuantityConverters.wavelengthOf((Quantity<Angle>)currentPosition, twoDee);
			toReturn = ((Quantity) wavelength).to(userUnits);
		} else if (toReturn.getUnit() instanceof Energy) {
			Quantity<Energy> energy = QuantityConverters.photonEnergyFromBraggAngle((Quantity<Angle>) currentPosition, twoDee);
			toReturn = ((Quantity) energy).to(userUnits);
		}
		return toReturn.getValue().doubleValue();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setPosition(Object position) throws DeviceException {
		final Quantity<? extends Quantity<?>> newQuantity = QuantityFactory.createFromObjectUnknownUnit(position, userUnits);

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned
		Quantity<Angle> angle = null;
		final Unit<? extends Quantity<?>> hardwareUnit = QuantityFactory.createUnitFromString(getHardwareUnitString());
		if (newQuantity.getUnit() instanceof Angle) {
			angle = (Quantity<Angle>) newQuantity;
		} else if (newQuantity.getUnit() instanceof Length) {
			Quantity braggangle = QuantityConverters.braggAngleFromWavelength((Quantity<Length>) newQuantity, twoDee);
			angle = braggangle.to(hardwareUnit);
		} else if (newQuantity.getUnit() instanceof Energy) {
			Quantity braggangle = QuantityConverters.braggAngleFromEnergy((Quantity<Energy>) newQuantity, twoDee);
			angle = braggangle.to(hardwareUnit);
		}

		if (angle != null) {
			// move motor to the angle
			this.theMotor.setPosition(angle.getValue().doubleValue());
		} else {
			throw new DeviceException("MonoScannable.rawAsynchronousMoveTo(): Null position/quantity specified.");
		}

	}

	@Override
	public boolean isBusy() throws DeviceException {
		return this.theMotor.isBusy();
	}

	private double twoDFromCrystalType(String crystalType) {
		double twoD = 0.0;

		for (int i = 0; i < knownCrystalTypes.length; i++) {
			String s = knownCrystalTypes[i];
			if (s.equals(crystalType)) {
				twoD = twoDValues[i];
				break;
			}
		}
		return twoD;
	}

	@Override
	public Object getAttribute(String name) throws DeviceException {
		Object attribute = null;
		if (name.equals("twoD")) {
			// Quantities seem not to be serializable so pass the double
			// value
			attribute = twoDee.to(ANGSTROM).getValue().doubleValue();
		} else {
			attribute = super.getAttribute(name);
		}
		return attribute;
	}
}
