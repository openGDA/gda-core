/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.function.Lookup;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltBallLookupTableHandler;

/**
 *
 */
public class TiltBallLookupTableHandler implements ITiltBallLookupTableHandler {

	private static final String MAX_Y = "maxY";
	private static final String MIN_Y = "minY";
	private static final String BALLOFFSET = "balloffset";
	private static final String MAX_X = "maxX";
	private static final String MIN_X = "minX";
	private Lookup moduleTable;

	public Lookup getModuleTable() {
		return moduleTable;
	}

	public void setModuleTable(Lookup moduleTable) {
		this.moduleTable = moduleTable;
	}

	@Override
	public double getTxOffset(Integer module) throws DeviceException {
		return moduleTable.lookupValue(module, BALLOFFSET);
	}

	@Override
	public int getMinY(Integer module) throws DeviceException {
		return (int) moduleTable.lookupValue(module, MIN_Y);
	}

	@Override
	public int getMaxY(Integer module) throws DeviceException {
		return (int) moduleTable.lookupValue(module, MAX_Y);
	}

	@Override
	public int getMaxX(Integer module) throws DeviceException {
		return (int) moduleTable.lookupValue(module, MAX_X);
	}

	@Override
	public int getMinX(Integer module) throws DeviceException {
		return (int) moduleTable.lookupValue(module, MIN_X);
	}

}
