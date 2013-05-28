/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import java.io.IOException;


public interface NDPlugin {

	/**
	 * Get Asyn port name for NDArray driver that will make callbacks to this plugin. This port can be changed at run
	 * time, connecting the plugin to a different NDArray driver.
	 * 
	 * @return NDArrayPort.
	 */
	String getInputNDArrayPort();

	/**
	 * Set Asyn port name for NDArray driver that will make callbacks to this plugin. This port can be changed at run
	 * time, connecting the plugin to a different NDArray driver.
	 * 
	 * @param nDArrayPort
	 */
	void setInputNDArrayPort(String nDArrayPort);

	/**
	 * Asyn port name of this plugin.
	 * 
	 * @return PortName
	 * @throws IOException 
	 */
	String getPortName() throws IOException;

}
