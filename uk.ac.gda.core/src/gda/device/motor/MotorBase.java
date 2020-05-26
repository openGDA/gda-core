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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;

/**
 * A base implementation of a generic Motor for all real motor types, which are therefore subclasses of this class.
 */
public abstract class MotorBase extends DeviceBase implements Motor, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private static final long POLL_TIME_MILLIS = LocalProperties.getAsInt(LocalProperties.GDA_SCANNABLEBASE_POLLTIME, 100);

	private double backlashSteps = 0.0;

	// README: protected as some subclasses need access to it
	protected boolean correctBacklash = false;

	private double[] speedLevel = new double[Motor.SPEED_LEVELS];

	private boolean[] speedPossible = new boolean[Motor.SPEED_LEVELS];

	private boolean limitsSettable = false;

	private double motorResolution = Double.NaN;

	protected double minPosition = Double.NaN;

	protected double maxPosition = Double.NaN;

	protected volatile boolean isInitialised = false;

	/**
	 * Changes the maximum velocity fast speed setting.
	 *
	 * @param fastSpeed
	 *            velocity in motor hardware units
	 */
	public void setFastSpeed(double fastSpeed) {
		speedLevel[Motor.FAST] = fastSpeed;
		speedPossible[Motor.FAST] = true;
	}

	/**
	 * @return fast speed velocity in motor hardware units
	 */
	public double getFastSpeed() {
		return speedLevel[Motor.FAST];
	}

	/**
	 * Changes the maximum velocity medium speed setting.
	 *
	 * @param mediumSpeed
	 *            velocity in motor hardware units
	 */
	public void setMediumSpeed(double mediumSpeed) {
		speedLevel[Motor.MEDIUM] = mediumSpeed;
		speedPossible[Motor.MEDIUM] = true;
	}

	/**
	 * @return medium speed velocity in motor hardware units
	 */
	public double getMediumSpeed() {
		return speedLevel[Motor.MEDIUM];
	}

	/**
	 * Changes the maximum velocity slow speed setting.
	 *
	 * @param slowSpeed
	 *            velocity in motor hardware units
	 */
	public void setSlowSpeed(double slowSpeed) {
		speedLevel[Motor.SLOW] = slowSpeed;
		speedPossible[Motor.SLOW] = true;
	}

	/**
	 * @return slow speed velocity in motor hardware units
	 */
	public double getSlowSpeed() {
		return speedLevel[Motor.SLOW];
	}

	/**
	 * Changes the current backlash correction to the specified hardware units.
	 *
	 * @param backlashSteps
	 *            backlash correction drive in motor hardware units
	 */
	public void setBacklashSteps(double backlashSteps) {
		this.backlashSteps = backlashSteps;
	}

	/**
	 * @return current backlash correction in hardware units
	 */
	public double getBacklashSteps() {
		return backlashSteps;
	}

	/**
	 * Checks whether this motor is allowed to change its hardware soft limits. Motor implementations should overide
	 * this method if required.
	 *
	 * @return a default of false here, but would return true where limits are settable.
	 */
	@Override
	public boolean isLimitsSettable() {
		return limitsSettable;
	}

	/**
	 * Sets whether this motor should change the hardware's soft limits.
	 *
	 * @param limitsSettable
	 *            flag true if limits are settable
	 */
	public void setLimitsSettable(boolean limitsSettable) {
		this.limitsSettable = limitsSettable;
	}

	@Override
	public double getTimeToVelocity() throws MotorException {
		throw new UnsupportedOperationException("Getting this motor's time to velocity is not supported");
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
		throw new UnsupportedOperationException("Setting this motor's time to velocity is not supported");
	}

	@Override
	public void setSpeedLevel(int speed) throws MotorException {
		if (speedPossible[speed])
			setSpeed(speedLevel[speed]);
		else {
			/*
			 * FIXME: Perhaps this should throw a MotorException. throw new MotorException(MotorStatus.FAULT, "Cannot
			 * set speed level"); I've not implemented this yet. I'm not sure this is an Exception or that everyone will
			 * want to have to set speeds up. The code is in place to deal with exceptions (so far as OEMove is
			 * concerned at least).
			 */
			logger.debug("Warning speed " + speed + " cannot be set.");
		}
	}

	/**
	 * The calculation used to determine the size of the first part of a backlash move and to set whether backlash is
	 * required.
	 *
	 * @param increment
	 *            requested size of move in hardware units
	 * @return size of move adjusted for backlash correction if needed
	 */
	public double addInBacklash(double increment) {
		if ((getBacklashSteps() > 0 && increment < 0.0) || (getBacklashSteps() < 0 && increment > 0.0)) {
			correctBacklash = true;
			increment -= getBacklashSteps();
		} else {
			correctBacklash = false;
		}
		return increment;
	}

	/**
	 * If backlash is required, this method instigates the final backlash move. This method works for motors which use
	 * incremental moves and should be overridden by motors which use absolute encoder positions.
	 *
	 * @throws MotorException
	 */
	@Override
	public void correctBacklash() throws MotorException {
		if (correctBacklash) {
			logger.debug("MotorBase correctBacklash about to move by " + getBacklashSteps() + " steps");
			moveBy(getBacklashSteps());
		}
	}

	@Override
	public MotorStatus waitWhileStatusBusy() throws InterruptedException, DeviceException {
		while (getStatus() == MotorStatus.BUSY) {
			Thread.sleep(POLL_TIME_MILLIS);
		}
		return getStatus();
	}

	/**
	 * Sets the software limits. Some motors/motor controllers are capable of setting up softlimits in them so that the
	 * so that the hard limit switch is never hit (at least theoretically) Such motor implementations should overide
	 * this method if required.
	 *
	 * @param minPosition
	 *            the minimum softlimit
	 * @param maxPosition
	 *            the maximum softlimit
	 * @throws MotorException
	 */
	@Override
	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException {
		// README: delibrately do nothing
	}

	@Override
	public double getMinPosition() throws MotorException {
		return minPosition;
	}

	@Override
	public void setMinPosition(double minPosition) throws MotorException {
		this.minPosition = minPosition;
	}

	@Override
	public double getMaxPosition() throws MotorException {
		return maxPosition;
	}

	@Override
	public void setMaxPosition(double maxPosition) throws MotorException {
		this.maxPosition = maxPosition;
	}

	@Override
	public double getRetryDeadband() throws MotorException {
		logger.warn("Retry deadband or position tolerance is not implmented for {}", getName());
		return Double.NaN;
	}

	/**
	 * Checks if the motor is homeable or not. Motor implementations should overide this method if required.
	 *
	 * @return if the motor is homeable. Returns false!
	 */
	@Override
	public boolean isHomeable() {
		return false;
	}

	/**
	 * Checks if the motor is homed or not. Motor implementations should overide this method if required.
	 *
	 * @return if the motor is homed. Returns false!
	 * @throws MotorException
	 */
	@Override
	public boolean isHomed() throws MotorException {
		return false;
	}

	/**
	 * Moves the motor to a repeatable starting location. Motor implementations should overide this method if required.
	 *
	 * @throws MotorException
	 */
	@Override
	public void home() throws MotorException {
	}

	@Override
	public boolean isInitialised() {
		return isInitialised;
	}

	/**
	 * @param initialised
	 */
	public void setInitialised(boolean initialised) {
		this.isInitialised = initialised;
	}

	public void setMotorResolution(double motorResolution) throws MotorException {
		this.motorResolution = motorResolution;
	}

	@Override
	public double getMotorResolution() throws MotorException {
		return motorResolution;
	}

	@Override
	public double getUserOffset() throws MotorException {
		return Double.NaN;
	}

	@Override
	public String getUnitString() throws MotorException {
		return "";
	}
}
