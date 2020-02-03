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

import static javax.measure.unit.NonSI.ANGSTROM;
import static javax.measure.unit.NonSI.DEGREE_ANGLE;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.NANO;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.apache.commons.lang.ArrayUtils;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jscience.physics.quantities.BraggAngle;
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.quantities.Wavelength;
import gda.util.QuantityFactory;

/**
 * Operates an angular motor controlling a mono
 * <p>
 * This can accept and return the mono position as angle, wavelength or energy.
 */
public class MonoScannable extends ScannableMotionUnitsBase {

	private static final Logger logger = LoggerFactory.getLogger(MonoScannable.class);

	// attributes describing the crystal in use
	private Amount<Length> twoDee;
	private double[] twoDValues = { 3.275, 3.840, 6.271, 2.0903 };
	private String crystalType = "unknown";
	private String[] knownCrystalTypes = { "Si311", "Si220", "Si111", "Si333" };

	// the motor
	private String motorName;
	private ScannableMotor theMotor;

	// handles user-unit to motor-unit conversion
	private String motorUnitString;
	private Unit<?> motorUnit;
	private Unit<?>[] acceptableUnits = new Unit<?>[0];
	private Unit<?> userUnits = QuantityFactory.createUnitFromString("nm");
	private String initialUserUnits = null;

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
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, NANO(METER));
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
		twoDee = Amount.valueOf(twoD, NonSI.ANGSTROM);
	}

	public void setCrystalType(String crystalType) {
		this.crystalType = crystalType;
		twoDee = Amount.valueOf(twoDFromCrystalType(crystalType), NonSI.ANGSTROM);
	}

	public double getTwoD() {
		return (twoDee != null) ? twoDee.to(NonSI.ANGSTROM).getEstimatedValue() : 0.0;
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

		Unit<?> newUnits = QuantityFactory.createUnitFromString(userUnitsString);
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
		Amount<? extends Quantity> newUnitClass = QuantityFactory.createFromTwoStrings("1.0", newUnit);
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

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		final Amount<? extends Quantity> newQuantity = QuantityFactory.createFromObject(position, userUnits);

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned
		Amount<Angle> angle = null;
		final Unit<? extends Quantity> hardwareUnit = QuantityFactory.createUnitFromString(getHardwareUnitString());
		if (newQuantity.getUnit().isCompatible(Angle.UNIT)) {
			angle = newQuantity.to(Angle.UNIT);
		} else if (newQuantity.getUnit().isCompatible(Length.UNIT)) {
			final Amount<Angle> braggangle = BraggAngle.braggAngleFromWavelength(newQuantity.to(Length.UNIT), twoDee);
			angle = braggangle.to(hardwareUnit).to(Angle.UNIT);
		} else if (newQuantity.getUnit().isCompatible(Energy.UNIT)) {
			final Amount<Angle> braggangle = BraggAngle.braggAngleFromEnergy(newQuantity.to(Energy.UNIT), twoDee);
			angle = braggangle.to(hardwareUnit).to(Angle.UNIT);
		}

		if (angle != null) {
			// move motor to the angle
			this.theMotor.moveTo(angle.doubleValue(Angle.UNIT));
		} else {
			throw new DeviceException("MonoScannable.rawAsynchronousMoveTo(): Null position/quantity specified.");
		}

	}

	@Override
	public Object getPosition() throws DeviceException {
		double[] angle;
		angle = ScannableUtils.getCurrentPositionArray(this.theMotor);

		Amount<? extends Quantity> currentPosition = QuantityFactory.createFromObject(angle[0], this.motorUnit);
		Amount<? extends Quantity> toReturn = Amount.valueOf(1.0, userUnits);
		if (toReturn.getUnit() instanceof Angle) {
			toReturn = currentPosition.to(userUnits);
		} else if (toReturn.getUnit() instanceof Length) {
			@SuppressWarnings("unchecked")
			Amount<Length> wavelength = Wavelength.wavelengthOf((Amount<Angle>)currentPosition, twoDee);
			toReturn = wavelength.to(userUnits);
		} else if (toReturn.getUnit() instanceof Energy) {
			@SuppressWarnings("unchecked")
			Amount<Energy> energy = PhotonEnergy.photonEnergyFromBraggAngle((Amount<Angle>) currentPosition, twoDee);
			toReturn = energy.to(userUnits);
		}
		return toReturn.getEstimatedValue();
	}

	@SuppressWarnings("unchecked")
	public void setPosition(Object position) throws DeviceException {
		final Amount<? extends Quantity> newQuantity = QuantityFactory.createFromObject(position, userUnits);

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned
		Amount<Angle> angle = null;
		final Unit<? extends Quantity> hardwareUnit = QuantityFactory.createUnitFromString(getHardwareUnitString());
		if (newQuantity.getUnit() instanceof Angle) {
			angle = (Amount<Angle>) newQuantity;
		} else if (newQuantity.getUnit() instanceof Length) {
			Amount<Angle> braggangle = BraggAngle.braggAngleFromWavelength((Amount<Length>) newQuantity, twoDee);
			angle = (Amount<Angle>) braggangle.to(hardwareUnit);
		} else if (newQuantity.getUnit() instanceof Energy) {
			Amount<Angle> braggangle = BraggAngle.braggAngleFromEnergy((Amount<Energy>) newQuantity, twoDee);
			angle = (Amount<Angle>) braggangle.to(hardwareUnit);
		}

		if (angle != null) {
			// move motor to the angle
			this.theMotor.setPosition(angle.getEstimatedValue());
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
			attribute = twoDee.to(NonSI.ANGSTROM).getEstimatedValue();
		} else {
			attribute = super.getAttribute(name);
		}
		return attribute;
	}
}
