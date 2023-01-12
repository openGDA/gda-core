/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.tenma;

import gda.device.BaseEpicsDeviceController;
import gda.device.DeviceException;
import uk.ac.gda.devices.tenma.api.IPsu722931Controller;

public class Psu722931EpicsController extends BaseEpicsDeviceController implements IPsu722931Controller {

	private static final String CURRENT = "SET_CURRENT";
	private static final String CURRENT_RBV = "CURRENT";
	private static final String VOLTAGE = "SET_VOLTAGE";
	private static final String VOLTAGE_RBV = "VOLTAGE";
	private static final String OUTPUT_ON = "OUTPUT_ON.PROC";
	private static final String OUTPUT_OFF = "OUTPUT_OFF.PROC";
	private static final String OUTPUT_STATUS_RBV = "STATUS_RBV.B6";

	public Psu722931EpicsController(String basePvName) {
		this.setBasePvName(basePvName);
	}

	@Override
	public double getCurrent() throws DeviceException {
		return getDoubleValue(CURRENT_RBV, "current");
	}

	@Override
	public void setCurrent(double current) throws DeviceException {
		setDoubleValue(CURRENT, current, "current");
	}

	@Override
	public double getVoltage() throws DeviceException {
		return getDoubleValue(VOLTAGE_RBV, "voltage");
	}

	@Override
	public void setVoltage(double voltage) throws DeviceException {
		setDoubleValue(VOLTAGE, voltage, "voltage");
	}

	@Override
	public void outputOn() throws DeviceException {
		setIntegerValue(OUTPUT_ON, 1, "output on");
	}

	@Override
	public void outputOff() throws DeviceException {
		setIntegerValue(OUTPUT_OFF, 1, "output on");
	}

	@Override
	public boolean outputIsOn() throws DeviceException {
		return getIntegerValue(OUTPUT_STATUS_RBV, "output status") == 1;
	}
}
