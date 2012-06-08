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
import gda.device.scannable.component.UnitsComponent;
import gda.factory.Finder;
import gda.jscience.physics.units.NonSIext;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replcaes the old SinceDriveAngularDOF.
 * <p>
 * This operates a linear motor which is attached to an arm, creating an angular motion.
 */
// TODO: SineDriveAngularScannable should extend ScannableMotionUnitsBase if it really wants to use units.
public class SineDriveAngularScannable extends ScannableMotionBase {
	
	private static final Logger logger = LoggerFactory.getLogger(SineDriveAngularScannable.class);

	// the motor
	private String motorName;
	private ScannableMotor theMotor;

	// conversion between the user values and the motor
	private double angleOffset = 0.0;
	private double armLength = 0.0;

	// handles user-unit to motor-unit conversion
	UnitsComponent unitsComponent = new UnitsComponent();

	/**
	 * Constructor.
	 */
	public SineDriveAngularScannable() {

	}

	@Override
	public void configure() {
		theMotor = (ScannableMotor) Finder.getInstance().find(motorName);
		this.inputNames = new String[] { theMotor.getName() };
		try {
			unitsComponent.setHardwareUnitString("Deg");
		} catch (DeviceException e) {
			logger.error("Unexpected exception setting hardware unit of SineDriveAngularScannable " + getName(), e);
		}
	}

	/**
	 * @return Returns the armLength expressed in the same units that the underlying motor uses
	 */
	public double getArmLength() {
		return armLength;
	}

	/**
	 * @param armLength
	 *            the armLength to set expressed in the same units that the underlying motor uses.
	 */
	public void setArmLength(double armLength) {
		this.armLength = armLength;
	}

	/**
	 * @return Returns the angleOffset in degrees
	 */
	public double getAngleOffset() {
		return Quantity.valueOf(angleOffset, SI.RADIAN).to(NonSIext.DEG_ANGLE).doubleValue();
	}

	/**
	 * @param angleOffset
	 *            The angleOffset to set expressed in degrees
	 */
	public void setAngleOffset(double angleOffset) {
		this.angleOffset = Quantity.valueOf(angleOffset, NonSIext.DEG_ANGLE).to(SI.RADIAN).doubleValue();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// check motor status. Throw an error if it is not idle
		if (this.isBusy()) {
			throw new DeviceException(getName() + " cannot perform the requested move as the motor is not ready");
		}

		Double targetAngle = this.unitsComponent.convertObjectToHardwareUnitsAssumeUserUnits(position);

		Double motorTargetPosition = Math.sin(targetAngle - this.angleOffset) * this.armLength;

		this.theMotor.moveTo(motorTargetPosition);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {

		// get linear motor position
		double[] position = ScannableUtils.getCurrentPositionArray(this.theMotor);

		Double angle = Math.asin(position[0] / armLength);

		Double currentPosition = angle + this.angleOffset;

		return this.unitsComponent.convertObjectToUserUnitsAssumeHardwareUnits(currentPosition);

	}

	@Override
	public boolean rawIsBusy() throws DeviceException {
		return this.theMotor.isBusy();
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
}
