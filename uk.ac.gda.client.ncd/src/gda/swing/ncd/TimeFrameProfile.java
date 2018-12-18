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

package gda.swing.ncd;

import gda.device.DeviceException;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import javax.swing.JPanel;

/**
 * A class to implement a time frame profile
 */
public abstract class TimeFrameProfile extends JPanel {
	/**
	 * Set the total number of frames in this profile
	 */
	abstract public void displayTotalFrames();

	/**
	 * @return a copy of the time frame profile
	 */
	abstract public TimeFrameProfile copy();

	/**
	 * Configure the hardware
	 * @throws DeviceException 
	 */
	abstract public void configureHardware() throws DeviceException;
	
	/**
	 * Save the time frame profile to file as xml
	 * 
	 * @param writer
	 */
	abstract public void save(BufferedWriter writer);

	/**
	 * Load a previously saved time frame profile
	 * 
	 * @param reader
	 */
	abstract public void load(BufferedReader reader);
}
