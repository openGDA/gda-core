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

package gda.device;

import gda.jython.accesscontrol.MethodAccessProtected;

/**
 * Interface to control motors of all types. Operations which need amounts, for example moveTo, are specified in terms
 * of steps (which can be non-integral for some types of motor).
 */
public interface Motor extends Device {
	/**
	 * MotorBase creates an array of SPEED_LEVELS values to use as preset speeds. The fields SLOW, MEDIUM and FAST are
	 * used to identify the relevant value.
	 */
	public static final int SPEED_LEVELS = 3;

	/**
	 * @see #SPEED_LEVELS
	 */
	public static final int SLOW = 0;

	/**
	 * @see #SPEED_LEVELS
	 */
	public static final int MEDIUM = 1;

	/**
	 * @see #SPEED_LEVELS
	 */
	public static final int FAST = 2;

	/**
	 * Moves the motor by the specified number of steps
	 * 
	 * @param steps
	 *            the number of steps to move by
	 * @throws MotorException
	 */
	@MethodAccessProtected(isProtected = true)
	public void moveBy(double steps) throws MotorException;

	/**
	 * Moves the motor to the specified position in steps
	 * 
	 * @param steps
	 *            the number of steps to move to
	 * @throws MotorException
	 */
	@MethodAccessProtected(isProtected = true)
	public void moveTo(double steps) throws MotorException;

	/**
	 * Moves the motor in a continuous mode
	 * 
	 * @param direction
	 *            direction to move in
	 * @throws MotorException
	 */
	@MethodAccessProtected(isProtected = true)
	public void moveContinuously(int direction) throws MotorException;

	/**
	 * Sets the current position of the motor
	 * 
	 * @param steps
	 *            the position to be set as current
	 * @throws MotorException
	 */
	@MethodAccessProtected(isProtected = true)
	public void setPosition(double steps) throws MotorException;

	/**
	 * Gets the current position of the motor
	 * 
	 * @return the current position
	 * @throws MotorException
	 */
	public double getPosition() throws MotorException;

	/**
	 * Sets the speed of the motor
	 * 
	 * @param speed
	 *            the speed
	 * @throws MotorException
	 */
	public void setSpeed(double speed) throws MotorException;

	/**
	 * Sets the speed level of the motor
	 * 
	 * @param level
	 *            one of a range of possible levels eg slow, medium, fast
	 * @throws MotorException
	 */
	public void setSpeedLevel(int level) throws MotorException;

	/**
	 * Gets the current speed setting of the motor
	 * 
	 * @return the speed in steps per second
	 * @throws MotorException
	 */
	public double getSpeed() throws MotorException;

	/**
	 * Returns this motor's time to velocity.
	 */
	public double getTimeToVelocity() throws MotorException;

	/**
	 * Sets this motor's time to velocity.
	 */
	public void setTimeToVelocity(double timeToVelocity) throws MotorException;

	/**
	 * Brings the motor to a controlled stop if possible
	 * 
	 * @throws MotorException
	 */
	public void stop() throws MotorException;

	/**
	 * Brings the motor to an uncontrolled stop if possible
	 * 
	 * @throws MotorException
	 */
	public void panicStop() throws MotorException;

	/**
	 * Gets the state of the motor
	 * 
	 * @return a value from the MotorStatus enum
	 * @throws MotorException
	 */
	public MotorStatus getStatus() throws MotorException;

	/**
	 * Do backlash correction
	 * 
	 * @throws MotorException
	 */
	public void correctBacklash() throws MotorException;

	/**
	 * Returns whether or not motor is actually moving
	 * 
	 * @return true if moving
	 * @throws MotorException
	 */
	public boolean isMoving() throws MotorException;

	/**
	 * Returns whether or not motor can home
	 * 
	 * @return true if homeable
	 * @throws MotorException
	 */
	public boolean isHomeable() throws MotorException;

	/**
	 * Returns whether or not motor is already homed
	 * 
	 * @return true if homed
	 * @throws MotorException
	 */
	public boolean isHomed() throws MotorException;

	/**
	 * Moves the motor to a repeatable starting location
	 * 
	 * @throws MotorException
	 */
	public void home() throws MotorException;

	/**
	 * Sets the soft limits of the motor itself (i.e. NOT limits in our software)
	 * 
	 * @param minPosition
	 *            minimum software limit
	 * @param maxPosition
	 *            maximum software limit
	 * @throws MotorException
	 */
	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException;

	/**
	 * Gets the minimum or lower soft limits of the motor itself (i.e. NOT limits cached in GDA software). By convention
	 * return NaN if not set.
	 * 
	 * @return the lower soft limit of the motor
	 * @throws MotorException
	 */
	public double getMinPosition() throws MotorException;

	/**
	 * Gets the maximum or upper soft limits of the motor itself (i.e. NOT limits cached in GDA software). By convention
	 * return NaN if not set.
	 * 
	 * @return the upper soft limit of the motor
	 * @throws MotorException
	 */
	public double getMaxPosition() throws MotorException;

	/**
	 * Returns whether or not limits are settable (in the motor itself).
	 * 
	 * @return true if the limits are settable.
	 * @throws MotorException
	 */
	public boolean isLimitsSettable() throws MotorException;

	/**
	 * Gets initialisition state of the motor with respect to its connection to low level device service e.g. EPICS
	 * server. This value does not imply that the motors initial values are set (or not set).
	 * 
	 * @return return true if motor is already initialised.
	 * @throws MotorException
	 */
	public boolean isInitialised() throws MotorException;

	/**
	 * return the position tolerance or accuracy
	 * 
	 * @return positioning tolerance
	 * @throws MotorException
	 */
	public double getRetryDeadband() throws MotorException;

	double getMotorResolution() throws MotorException;

	/**
	 * @return the user offset set on the motor
	 * @throws MotorException
	 */
	public double getUserOffset() throws MotorException;
}
