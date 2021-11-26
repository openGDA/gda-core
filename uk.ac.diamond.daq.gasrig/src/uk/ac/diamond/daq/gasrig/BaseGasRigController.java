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

import gda.device.BaseEpicsDeviceController;

/**
 * As there is no simulator for the gas rig, this is hard to test in dummy mode. Therefore this base class
 * contains all the methods require for building live PVs, so that both the live and dummy controller classes
 * can inherit from it and the dummy controller can log the PVs which would be used in live mode, to aid
 * in debugging.
 *
 * @author Tom Richardson (too27251)
 */
public abstract class BaseGasRigController extends BaseEpicsDeviceController {

	private String basePvName;
	private static final String GAS_NAME = "%sMFC-%02d:GAS:STR:RD";

	protected BaseGasRigController(String basePvName) {
		this.basePvName = basePvName;
	}

	protected String constructGasNamePV(int gasId) {
		return String.format(GAS_NAME, basePvName, gasId);
	}
}
