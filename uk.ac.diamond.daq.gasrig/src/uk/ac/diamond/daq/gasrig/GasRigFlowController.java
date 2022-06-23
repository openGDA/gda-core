/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig;

import java.util.Map;

import gda.device.DeviceException;

public class GasRigFlowController extends BaseGasRigController {

	private int gasId;
	private Map<Integer, Integer> lineValveNumbers;

	public GasRigFlowController(String basePvName, int gasId, Map<Integer, Integer> lineValveNumbers) {
		super(basePvName);
		this.gasId = gasId;
		this.lineValveNumbers = lineValveNumbers;
	}

	public int getGasId() {
		return gasId;
	}

	public String getGasName() throws DeviceException {
		return getStringValue(constructGasNamePvSuffix(gasId), "gas name");
	}

	public double getMaximumMassFlow() throws DeviceException {
		return getDoubleValue(constructMaximumMassFlowPvSuffix(gasId), "maximum mass flow");
	}

	public void setMassFlow(double massFlow) throws DeviceException {
		setDoubleValue(constructMassFlowSetPointPv(gasId), massFlow, "mass flow");
	}

	public void closeLineValves() throws DeviceException {
		for (var valveNumber : lineValveNumbers.values()) {
			closeValve(valveNumber);
		}
	}

	public void closeValve(int valveNumber) throws DeviceException {
		setStringValue(constructValveControlPv(valveNumber), VALVE_CLOSE, "valve control");
	}

	public void openValve(int valveNumber) throws DeviceException {
		setStringValue(constructValveControlPv(valveNumber), VALVE_OPEN, "valve control");
	}

	public boolean isValveOpen(int valveNumber) throws DeviceException {
		String valveStatus = getStringValue(constructValveStatusPv(valveNumber), "valve status");
		return valveStatus.equals(VALVE_OPEN);
	}

	public int getValveNumber(int lineNumber) {
		return lineValveNumbers.get(lineNumber);
	}
}
