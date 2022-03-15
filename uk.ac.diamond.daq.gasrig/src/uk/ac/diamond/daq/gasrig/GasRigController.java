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

package uk.ac.diamond.daq.gasrig;

import gda.device.DeviceException;

public class GasRigController extends BaseGasRigController implements IGasRigController {

	public GasRigController(String basePvName) {
		super(basePvName);
	}

	@Override
	public String getGasName(int gasId) throws DeviceException {
		return getStringValue(constructGasNamePvSuffix(gasId), "gas name");
	}

	@Override
	public double getMaximumMassFlow(int gasId) throws DeviceException {
		return getDoubleValue(constructMaximumMassFlowPvSuffix(gasId), "maximum mass flow");
	}
}