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

package gda.device.gpib;

import gda.device.DeviceException;

/**
 * A simulator for an IEEE488/GPIB device or controller.
 */
public class DummyGpib extends GpibBase {
	String command = "";

	String deviceName = "";

	@Override
	public void configure(){
		// no configuration required
	}	
	
	@Override
	public int findDevice(String deviceName) throws DeviceException {
		this.deviceName = deviceName;
		return 1;
	}

	@Override
	public int getSerialPollByte(String deviceName) throws DeviceException {
		return 0;
	}

	@Override
	public void sendDeviceClear(String deviceName) throws DeviceException {
	}

	@Override
	public void sendInterfaceClear(String interFaceName) throws DeviceException {
	}

	@Override
	public void setTimeOut(String deviceName, int timeout) throws DeviceException {
	}

	@Override
	public int getTimeOut(String deviceName) throws DeviceException {
		return 0;
	}

	@Override
	public void setTerminator(String deviceName, char term) throws DeviceException {
	}

	@Override
	public char getTerminator(String deviceName) throws DeviceException {
		return 0;
	}

	@Override
	public void setReadTermination(String deviceName, boolean terminate) throws DeviceException {
	}

	@Override
	public void setWriteTermination(String deviceName, boolean terminate) throws DeviceException {
	}

	@Override
	public boolean getReadTermination(String deviceName) throws DeviceException {
		return false;
	}

	@Override
	public boolean getWriteTermination(String deviceName) throws DeviceException {
		return false;
	}

	@Override
	public String read(String deviceName) throws DeviceException {
		if (!this.deviceName.equals(deviceName))
			throw new DeviceException("device not found");

		return command;
	}

	@Override
	public String read(String deviceName, int strLength) throws DeviceException {
		return null;
	}

	@Override
	public void write(String deviceName, String buffer) throws DeviceException {
		if (this.deviceName.equals(deviceName))
			command = buffer;
	}
}
