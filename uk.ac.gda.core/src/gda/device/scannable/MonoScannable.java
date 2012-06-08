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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.factory.Finder;
import gda.jscience.physics.quantities.BraggAngle;
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.quantities.Wavelength;
import gda.jscience.physics.units.NonSIext;
import gda.util.QuantityFactory;

import org.apache.commons.lang.ArrayUtils;
import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Operates an angular motor controlling a mono
 * <p>
 * This can accept and return the mono position as angle, wavelength or energy.
 */
public class MonoScannable extends ScannableMotionUnitsBase implements ScannableMotionUnits {
	
	private static final Logger logger = LoggerFactory.getLogger(MonoScannable.class);

	// attributes describing the crystal in use
	private Length twoDee;
	private double[] twoDValues = { 3.275, 3.840, 6.2695, 2.0903 };
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

	/**
	 * Constructor.
	 */
	public MonoScannable() {

	}

	@Override
	public void configure() {
		theMotor = (ScannableMotor) Finder.getInstance().find(motorName);
//		this.inputNames = new String[] { theMotor.getName() };

		try {
			// if (theMotor instanceof EpicsMotor) {
			// // try to work out the units the motor works in
			// motorUnit = QuantityFactory.createUnitFromString(theMotor
			// .getAttribute("unit").toString());
			// }
			// // else help it is defined from setMotorUnitString()
			// else {
			motorUnit = QuantityFactory.createUnitFromString(this.motorUnitString);
			// }
			if (initialUserUnits == null) {
				userUnits = motorUnit;
			} else {
				userUnits = QuantityFactory.createUnitFromString(initialUserUnits);
			}

			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, NonSIext.mDEG_ANGLE);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, NonSIext.DEG_ANGLE);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, NonSI.ANGSTROM);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, SI.NANO(SI.METER));
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, NonSI.ELECTRON_VOLT);
			acceptableUnits = (Unit[]) ArrayUtils.add(acceptableUnits, SI.KILO(NonSI.ELECTRON_VOLT));

			this.configured = true;
		} catch (Exception e) {
			// do not throw an error as this would stop ObjectFactory from
			// completing its initialisation
			logger.error("Exception during configure of " + getName() + " (motor=" + StringUtils.quote(motorName) + ")", e);
		}

	}

	/**
	 * @param twoD
	 *            2d expressed in Angstroms
	 */
	public void setTwoD(double twoD) {
		twoDee = Quantity.valueOf(twoD, NonSI.ANGSTROM);
	}

	/**
	 * @param crystalType
	 */
	public void setCrystalType(String crystalType) {
		this.crystalType = crystalType;
		twoDee = Quantity.valueOf(twoDFromCrystalType(crystalType), NonSI.ANGSTROM);
	}

	/**
	 * @return the current twoD value
	 */
	public double getTwoD() {
		return (twoDee != null) ? twoDee.to(NonSI.ANGSTROM).getAmount() : 0.0;
	}

	/**
	 * @return string of the current crystal type
	 */
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
		if (configured) {
			if (ArrayUtils.contains(this.acceptableUnits, newUnits)) {
				this.userUnits = newUnits;
			}
		} else {
			this.initialUserUnits = userUnitsString;
		}
	}

	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		Quantity newUnitClass = QuantityFactory.createFromTwoStrings("1.0", newUnit);
		if (newUnitClass instanceof Angle || newUnitClass instanceof Energy || newUnitClass instanceof Length) {
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

	/**
	 * @return Returns the motorName.
	 */
	public String getMotorName() {
		return motorName;
	}

	/**
	 * @param motorName
	 *            The motorName to set.
	 */
	public void setMotorName(String motorName) {
		this.motorName = motorName;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		Quantity newQuantity = QuantityFactory.createFromObject(position, userUnits);

		// for each of the allowed type of unit calculate the
		// equivalent Angle, if the unit type is not allowed then
		// null will be returned
		Angle angle = null;
		if (newQuantity instanceof Angle) {
			angle = (Angle) newQuantity;
		} else if (newQuantity instanceof Length) {
			Angle braggangle = BraggAngle.braggAngleOf((Length) newQuantity, twoDee);
			angle = (Angle) braggangle.to(QuantityFactory.createUnitFromString(getHardwareUnitString()));
		} else if (newQuantity instanceof Energy) {
			Angle braggangle = BraggAngle.braggAngleOf((Energy) newQuantity, twoDee);
			angle = (Angle) braggangle.to(QuantityFactory.createUnitFromString(getHardwareUnitString()));
		}

		if (angle != null) {
			// move motor to the angle
			this.theMotor.moveTo(angle.getAmount());
		} else {
			throw new DeviceException("MonoScannable.rawAsynchronousMoveTo(): Null position/quantity specified.");
		}

	}

	@Override
	public Object getPosition() throws DeviceException {
		double[] angle;
		angle = ScannableUtils.getCurrentPositionArray(this.theMotor);

		Quantity currentPosition = QuantityFactory.createFromObject(angle[0], this.motorUnit);
		Quantity toReturn = Quantity.valueOf(1.0, userUnits);
		if (toReturn instanceof Angle) {
			toReturn = currentPosition.to(userUnits);
		} else if (toReturn instanceof Length) {
			Quantity wavelength = Wavelength.wavelengthOf((Angle) currentPosition, twoDee);
			toReturn = wavelength.to(userUnits);
		} else if (toReturn instanceof Energy) {
			Quantity energy = PhotonEnergy.photonEnergyOf((Angle) currentPosition, twoDee);
			toReturn = energy.to(userUnits);
		}
		return toReturn.getAmount();
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
			attribute = twoDee.to(NonSI.ANGSTROM).getAmount();
		} else {
			attribute = super.getAttribute(name);
		}
		return attribute;
	}
}
