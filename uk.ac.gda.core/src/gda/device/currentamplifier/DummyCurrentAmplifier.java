/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.currentamplifier;

import gda.device.DeviceException;

/**
 * Dummy class for the CurrentAmplifier interface
 */
public class DummyCurrentAmplifier extends CurrentAmplifierBase{
	private volatile String gain = "";
	private volatile String gainUnit = "";
	
	@Override
	public void setName(String name){
		super.setName(name);
		this.inputNames = new String[]{name};
		
	}

	@Override
	public double getCurrent() throws DeviceException {
		return 0;
	}

	@Override
	public String getGain() throws DeviceException {
		return gain;
	}

	@Override
	public String getGainUnit() throws DeviceException {
		return gainUnit;
	}

	@Override
	public String getMode() throws DeviceException {
		return null;
	}

	@Override
	public Status getStatus() throws DeviceException {
		return null;
	}

	@Override
	public void setGain(String position) throws DeviceException {
		this.gain = position;
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		this.gainUnit = unit;
	}

	@Override
	public void setMode(String mode) throws DeviceException {
	}

	@Override
	public void listGains() throws DeviceException {
	}

}
