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

import java.util.List;

import gda.device.DeviceException;

/**
 * This class serves to simulate the responses from EPICS, and also logs the PVs which
 * will be used in the live environment for debugging purposes, as there is no gas rig simulator.
 *
 * It extends {@link BaseGasRigController} so that it can make use of the methods which construct
 * the live PVs
 *
 * @author Tom Richardson (too27251)
 */
public class DummyGasRigController extends BaseGasRigController implements IGasRigController {

	private List<Gas> gases;

	public DummyGasRigController(String basePvName, List<Gas> gases) {
		super(basePvName);
		this.gases = gases;
	}

	@Override
	public String getGasName(int gasId) throws DeviceException {
		String gasNamePv = constructGasNamePV(gasId);
		logger.info("Gas name requested for gas {}. Live PV would be {}", gasId, gasNamePv);

		return (gases.stream().filter(g -> g.getId() == gasId).findFirst()
				.orElseThrow(() -> new DeviceException("No gas found for id " + gasId)))
				.getName();
	}
}
