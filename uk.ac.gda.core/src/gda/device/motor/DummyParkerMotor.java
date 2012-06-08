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

package gda.device.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;

/**
 * A Dummy Parker motor class
 */
public class DummyParkerMotor extends DummyMotor {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyParkerMotor.class);
	
	// private boolean homeable;
	private String storedPosLocation;

	private String homingStatusLocation;

	private String homeCommandString;

	private double encoderScalingFactor;

	private String parker6kControllerName;

	private boolean stepper;

	private int axisNo;

	private double maxSpeed;

	private double minSpeed;

	/**
	 * Constructor
	 */
	public DummyParkerMotor() {
	}

	@Override
	public void setPosition(double newPosition) {
		try {
			double difference = newPosition - getPosition();
			setPosition(newPosition);
			setSoftLimits((minPosition + difference), (maxPosition + difference));
		} catch (MotorException me) {
		}
	}

	@Override
	public boolean isHomeable() {
		return true;
	}

	@Override
	public void home() throws MotorException {
		moveTo(0);
	}

	@Override
	public void setSoftLimits(double min, double max) throws MotorException {
		logger.debug("setting software limits");
		minPosition = min;
		maxPosition = max;
		logger.debug("the new limits are " + minPosition + "and " + maxPosition);
	}

	/**
	 * Sets the minimum speed (steps/sec?).
	 * 
	 * @param minSpeed
	 *            the minimum speed
	 */
	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	/**
	 * Get the minimum speed
	 * 
	 * @return the minimum speed
	 */
	public double getMinSpeed() {
		return minSpeed;
	}

	/**
	 * Sets the maximum speed (steps/sec?).
	 * 
	 * @param maxSpeed
	 *            the maximum speed
	 */
	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/**
	 * Get the maximum speed
	 * 
	 * @return the maximum speed
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Set the axis number in the motor. Minimum value is 1 and the maximum depends on the maximum number of motors for
	 * this unit.
	 * 
	 * @see Parker6kController#getMaxNoOfMotors
	 * @param axisNo
	 *            the axis number
	 */
	public void setAxisNo(int axisNo) {
		this.axisNo = axisNo;
	}

	/**
	 * Get the axis number
	 * 
	 * @return the axis number
	 */
	public int getAxisNo() {
		return axisNo;
	}

	/**
	 * Sets the minimum position.
	 * 
	 * @param minPosition
	 *            the minimum position
	 */
	public void setMinPosition(double minPosition) {
		this.minPosition = minPosition;
	}

	/**
	 * Get the minimum position
	 * 
	 * @return the minimum position
	 */
	@Override
	public double getMinPosition() {
		return minPosition;
	}

	/**
	 * Sets the maximum position.
	 * 
	 * @param maxPosition
	 *            the maximum position
	 */
	public void setMaxPosition(double maxPosition) {
		this.maxPosition = maxPosition;
	}

	/**
	 * Get the minimum position
	 * 
	 * @return the minimum position
	 */
	@Override
	public double getMaxPosition() {
		return maxPosition;
	}

	/**
	 * Set the motor type
	 * 
	 * @param stepper
	 *            true = stepper motor, false = servo motor
	 */
	public void setStepper(boolean stepper) {
		this.stepper = stepper;
	}

	/**
	 * Get the motor type
	 * 
	 * @return true = stepper motor, false = servo motor
	 */
	public boolean isStepper() {
		return stepper;
	}

	/**
	 * Set the controller name associated with the motor.
	 * 
	 * @param parker6kControllerName
	 *            the contorller name for the motor.
	 */
	public void setParker6kControllerName(String parker6kControllerName) {
		this.parker6kControllerName = parker6kControllerName;
	}

	/**
	 * Get the controller name associated with the motor.
	 * 
	 * @return the controller name.
	 */
	public String getParker6kControllerName() {
		return parker6kControllerName;
	}

	/**
	 * Get the encoderScalingFactor.
	 * 
	 * @return Returns the encoderScalingFactor.
	 */
	public double getEncoderScalingFactor() {
		return encoderScalingFactor;
	}

	/**
	 * Set the encoderScalingFactor.
	 * 
	 * @param encoderScalingFactor
	 *            The encoderScalingFactor to set.
	 */
	public void setEncoderScalingFactor(double encoderScalingFactor) {
		this.encoderScalingFactor = encoderScalingFactor;
	}

	/**
	 * Get the home command string.
	 * 
	 * @return Returns the homeCommandString.
	 */
	public String getHomeCommandString() {
		return homeCommandString;
	}

	/**
	 * Set the home command string.
	 * 
	 * @param homeCommandString
	 *            The homeCommandString to set.
	 */
	public void setHomeCommandString(String homeCommandString) {
		this.homeCommandString = homeCommandString;
	}

	/**
	 * Get the homing status location.
	 * 
	 * @return Returns the homingStatusLocation.
	 */
	public String getHomingStatusLocation() {
		return homingStatusLocation;
	}

	/**
	 * Set hte homing status location.
	 * 
	 * @param homingStatusLocation
	 *            The homingStatusLocation to set.
	 */
	public void setHomingStatusLocation(String homingStatusLocation) {
		this.homingStatusLocation = homingStatusLocation;
	}

	/**
	 * Get the stored position location.
	 * 
	 * @return Returns the storedPosLocation.
	 */
	public String getStoredPosLocation() {
		return storedPosLocation;
	}

	/**
	 * Set the stored position location.
	 * 
	 * @param storedPosLocation
	 *            The storedPosLocation to set.
	 */
	public void setStoredPosLocation(String storedPosLocation) {
		this.storedPosLocation = storedPosLocation;
	}

	/**
	 * Set a flag to indicate whether the motor is homeable.
	 * 
	 * @param homeable
	 *            The homeable to set.
	 */
	@Override
	public void setHomeable(boolean homeable) {
	}
}
