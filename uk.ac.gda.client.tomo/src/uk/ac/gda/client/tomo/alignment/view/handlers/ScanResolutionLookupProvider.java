/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import gda.device.DeviceException;
import gda.function.Lookup;
import gda.function.lookup.LookupTable;
import uk.ac.gda.client.tomo.IScanResolutionLookupProvider;

public class ScanResolutionLookupProvider implements IScanResolutionLookupProvider {

	private static final String X_BIN = "XBin";
	private static final String Y_BIN = "YBin";
	private static final String STEPSIZE = "Stepsize";
	private static final String NUM_PROJECTIONS = "NumProjections";
	private Lookup scanLookupTable;

	public void setScanLookupTable(Lookup scanLookupTable) {
		this.scanLookupTable = scanLookupTable;
	}

	@Override
	public int getNumberOfProjections(int resolution) throws DeviceException {
		return (int) scanLookupTable.lookupValue(resolution, NUM_PROJECTIONS);
	}

	@Override
	public double getStepSize(int resolution) throws Exception {
		return scanLookupTable.lookupValue(resolution, STEPSIZE);
	}

	@Override
	public int getBinX(int resolution) throws Exception {
		return (int) scanLookupTable.lookupValue(resolution, X_BIN);
	}

	@Override
	public int getBinY(int resolution) throws Exception {
		return (int) scanLookupTable.lookupValue(resolution, Y_BIN);
	}

}
