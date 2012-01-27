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

package gda.device.peem;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.PEEM;

/**
 * Dummy PhotoEmission Electron Microscope (PEEM) end-station
 */
public class DummyPeem extends DeviceBase implements PEEM {

	// the last target set
	double latestValue;

	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * Return the value of latestValue
	 * 
	 * @return double latestValue
	 */
	public double getValue() {
		// TODO Auto-generated method stub
		return latestValue;
	}

	/**
	 * Set the value of latestValue
	 * 
	 * @param target
	 *            double value to set latestValue to
	 */
	public void setValue(double target) {
		// TODO Auto-generated method stub
		latestValue = target;
	}

	@Override
	public boolean connect() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double[] getMicrometerValue() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getModuleIndex(String moduleName) throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getModuleNumber() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPSName(int index) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getPSValue(int index) throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPreset() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVacuumGaugeLabel() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getVacuumGaugeValue() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isInitDone() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String modules() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setPSValue(int index, double value) throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int setPhi(double angle) throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
