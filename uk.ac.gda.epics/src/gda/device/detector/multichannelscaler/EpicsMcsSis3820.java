/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.multichannelscaler;

import gda.device.Detector;
import gda.device.DeviceException;

/**
 * EpicsMcsSis3820 Class
 */
public interface EpicsMcsSis3820 extends Detector {

	/**
	 * 
	 */
	public static final int MAX_NUMBER_MCA = EpicsDlsMcsSis3820Controller.MAXIMUM_NUMBER_OF_MCA;

	// public void erasestart() throws DeviceException;

	// public void start() throws DeviceException;
	// public void erase() throws DeviceException;
	// public void stop() throws DeviceException;
	/**
	 * read data from the specified channel.
	 * 
	 * @param channel
	 * @return data[channel]
	 * @throws DeviceException
	 */
	public abstract int[] getData(int channel) throws DeviceException;

	/**
	 * @return data
	 * @throws DeviceException
	 */
	public abstract int[][] getData() throws DeviceException;

	/**
	 * @return elapsed time
	 * @throws DeviceException
	 */
	public abstract double getElapsedTime() throws DeviceException;

	/**
	 * @return elapsed time from epics
	 * @throws DeviceException
	 */
	public abstract double getElapsedTimeFromEpics() throws DeviceException;

}