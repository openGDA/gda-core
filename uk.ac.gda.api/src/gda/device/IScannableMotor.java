/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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


/**
 *
 */
public interface IScannableMotor extends Scannable {

	/**
	 * Sets the motor used by this scannable motor.
	 *
	 * @param motor
	 *            the motor
	 */
	void setMotor(Motor motor);

	/**
	 * COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS Method required by scripts which need to access the real motor at times. Before the script could get the motor
	 * name but now that the motor may be set by spring, scripts cannot get the underlying motor.
	 *
	 * @return Motor
	 */
	Motor getMotor();

	/**
	 * @param motorName
	 *            The motorName to set.
	 */
	void setMotorName(String motorName);

	/**
	 * @return Returns the motorName.
	 */
	String getMotorName();

	/**
	 * @return speed of the motor
	 * @throws DeviceException
	 */
	double getSpeed() throws DeviceException;

	/**
	 * @return the resolution of the motor
	 * @throws DeviceException
	 */
	double getMotorResolution() throws DeviceException;

	/**
	 * @return the deadband retry of an EPICS motor for eg.
	 */
	double getDemandPositionTolerance();

	/**
	 * @return the user offset on the motor
	 * @throws DeviceException
	 */
	double getUserOffset() throws DeviceException;

	/**
	 * Returns this motor's time to velocity.
	 */
	double getTimeToVelocity() throws DeviceException;

	/**
	 * Sets this motor's time to velocity.
	 */
	void setTimeToVelocity(double timeToVelocity) throws DeviceException;
	/**
	 * Set the speed of the underlying motor
	 *
	 * @param requiredSpeed
	 *            in the motor's units
	 * @throws DeviceException
	 */
	void setSpeed(double requiredSpeed) throws DeviceException;

	/**
	 * Set the position
	 *
	 * @throws DeviceException
	 */
	void setPosition(Object position) throws DeviceException;

	/**
	 * Returns the lower motor limit in its external representation. Null if there is no lower Motor limit.
	 *
	 * @return limit in external representation
	 * @throws DeviceException
	 */
	Double getLowerMotorLimit() throws DeviceException;

	/**
	 * Returns the upper motor limit in its external representation. Null if there is no upper Motor limit.
	 *
	 * @return limit in external representation
	 * @throws DeviceException
	 */
	Double getUpperMotorLimit() throws DeviceException;

	/**
	 * Returns the innermost (i.e. the most limiting) of the lower Scannable and Motor limits.
	 *
	 * @return the highest minimum limit, or null if neither are set.
	 * @throws DeviceException
	 */
	Double getLowerInnerLimit() throws DeviceException;

	/**
	 * Returns the innermost (i.e. the most limiting) of the upper Scannable and Motor limits.
	 *
	 * @return the lowest maximum limit, or null if neither are set.
	 * @throws DeviceException
	 */
	Double getUpperInnerLimit() throws DeviceException;
}
