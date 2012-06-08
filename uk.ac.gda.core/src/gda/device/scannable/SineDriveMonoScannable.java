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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SineDriveMonoScannable extends ScannableMotionBase {

	private static final Logger logger = LoggerFactory.getLogger(SineDriveMonoScannable.class);

	
	// convert between user values and motor motion
	private double sineArmLength;
	private double gratingDensity;
	private double correctionFactor = 1.0;
	private double factor;

	// the motor
	private String motorName;
	private ScannableMotor theMotor;

	// handles user-unit to motor-unit conversion
	UnitsComponent unitsComponent = new UnitsComponent();

	/**
	 * Constructor.
	 */
	public SineDriveMonoScannable() {

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

		// calculate the factor to always use
		factor = (sineArmLength * gratingDensity) / (2 * Math.cos(Math.toRadians(45))) * correctionFactor;
	}

	/**
	 * @return Returns the sineArmLength.
	 */
	public double getSineArmLength() {
		return sineArmLength;
	}

	/**
	 * @param sineArmLength
	 *            The sineArmLength to set.
	 */
	public void setSineArmLength(double sineArmLength) {
		this.sineArmLength = sineArmLength;
	}

	/**
	 * @return Returns the gratingDensity.
	 */
	public double getGratingDensity() {
		return gratingDensity;
	}

	/**
	 * @param gratingDensity
	 *            The gratingDensity to set.
	 */
	public void setGratingDensity(double gratingDensity) {
		this.gratingDensity = gratingDensity;
	}

	/**
	 * @return Returns the correctionFactor.
	 */
	public double getCorrectionFactor() {
		return correctionFactor;
	}

	/**
	 * @param correctionFactor
	 *            The correctionFactor to set.
	 */
	public void setCorrectionFactor(double correctionFactor) {
		this.correctionFactor = correctionFactor;
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
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// check motor status. Throw an error if it is not idle
		if (this.isBusy()) {
			throw new DeviceException(getName() + " cannot perform the requested move as the motor is not ready");
		}

		Double target = this.unitsComponent.convertObjectToHardwareUnitsAssumeUserUnits(position);

		Double motorTargetPosition = target * this.factor;

		this.theMotor.moveTo(motorTargetPosition);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {

		// get motor position
		double[] position = ScannableUtils.getCurrentPositionArray(this.theMotor);
		Double currentPosition = position[0] / this.factor;
		return this.unitsComponent.convertObjectToUserUnitsAssumeHardwareUnits(currentPosition);

	}

	@Override
	public boolean rawIsBusy() throws DeviceException {
		return this.theMotor.isBusy();
	}
}
