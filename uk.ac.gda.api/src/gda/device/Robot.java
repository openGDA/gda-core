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

import gda.device.robot.SampleState;

/**
 * Robot interface
 */
public interface Robot extends Scannable {

	/**
	 * starts the robot control
	 *
	 * @throws DeviceException
	 */
	void start() throws DeviceException;

	/**
	 * fetches the next sample from the carousel and put it on sample stage.
	 *
	 * @throws DeviceException
	 */
	void nextSample() throws DeviceException;

	/**
	 * fetches the n'th sample from the carousel and put it on sample stage.
	 *
	 * @param n
	 * @throws DeviceException
	 */
	void nextSample(double n) throws DeviceException;

	/**
	 * clear the sample from the sample stage and put it back onto carousel.
	 *
	 * @throws DeviceException
	 */
	void clearSample() throws DeviceException;

	/**
	 * release the hold to enable "start" command again
	 *
	 * @throws DeviceException
	 */
	void finish() throws DeviceException;

	/**
	 * gets the error code for engineer
	 *
	 * @return error code
	 * @throws DeviceException
	 */
	String getError() throws DeviceException;

	/**
	 * reset robot following interruption
	 *
	 * @throws DeviceException
	 */
	void recover() throws DeviceException;

	/**
	 * gets the actual sample position number in Robot
	 *
	 * @return the actual sample position number in Robot
	 * @throws DeviceException
	 */
	double getSamplePosition() throws DeviceException;

	/**
	 * gets robot's sample state i.e. where is the sample in relation to the robot.
	 *
	 * @return robot's sample state ie where is the sample in relation to the robot.
	 * @throws DeviceException
	 */
	SampleState getSampleState() throws DeviceException;
}