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

package gda.device.detector.odccd;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.ODCCD;
import gda.device.detector.DetectorBase;
import gda.factory.Configurable;
import gda.factory.Findable;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 */
public class ODCCDDummy extends DetectorBase implements Configurable, Serializable, Findable, Detector, ODCCD {

	@Override
	public String closeShutter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void collectData() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect(String host) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDataName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String openShutter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ODCCDImage readDataFromISDataBase(String pathname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void runScript(String command) {
		// TODO Auto-generated method stub

	}

	@Override
	public String shutter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double temperature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double waterTemperature() {
		// TODO Auto-generated method stub
		return 0;
	}

}
