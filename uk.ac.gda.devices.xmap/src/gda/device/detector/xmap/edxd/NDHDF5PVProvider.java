/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.xmap.edxd;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.factory.FactoryException;

import java.io.IOException;

/**
 * Provides access to PVs in the NDHDF5 extension to the HDF5 Area Detector plugin
 */
public class NDHDF5PVProvider {

	private static final String NumExtraDims_SUFFIX = ":NumExtraDims";
	private static final String NumExtraDims_RBV_SUFFIX = ":NumExtraDims_RBV";
	private static final String ExtraDimSizeN_SUFFIX = ":ExtraDimSizeN";
	private static final String ExtraDimSizeN_RBV_SUFFIX = ":ExtraDimSizeN_RBV";
	private static final String ExtraDimSizeX_SUFFIX = ":ExtraDimSizeX";
	private static final String ExtraDimSizeX_RBV_SUFFIX = ":ExtraDimSizeX_RBV";
	private static final String ExtraDimSizeY_SUFFIX = ":ExtraDimSizeY";
	private static final String ExtraDimSizeY_RBV_SUFFIX = ":ExtraDimSizeY_RBV";

	private String epicsTemplate;
	private PV<Integer> pvNumExtraDims;
	private PV<Integer> pvExtraDimSizeN;
	private PV<Integer> pvNumExtraDims_rbv;
	private PV<Integer> pvExtraDimSizeN_rbv;
	private PV<Integer> pvExtraDimSizeX;
	private PV<Integer> pvExtraDimSizeX_rbv;
	private PV<Integer> pvExtraDimSizeY;
	private PV<Integer> pvExtraDimSizeY_rbv;

	public NDHDF5PVProvider(String epicsTemplate) throws FactoryException {
		if (epicsTemplate == null || epicsTemplate.isEmpty()) {
			throw new FactoryException("Epics template has not been set!");
		}
		this.epicsTemplate = epicsTemplate;
		createPVs();
	}

	private String generatePVName(String suffix) {
		return epicsTemplate + suffix;
	}

	private void createPVs() {
		pvNumExtraDims = LazyPVFactory.newIntegerPV(generatePVName(NumExtraDims_SUFFIX));
		pvNumExtraDims_rbv = LazyPVFactory.newIntegerPV(generatePVName(NumExtraDims_RBV_SUFFIX));
		pvExtraDimSizeN = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeN_SUFFIX));
		pvExtraDimSizeN_rbv = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeN_RBV_SUFFIX));
		pvExtraDimSizeX = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeX_SUFFIX));
		pvExtraDimSizeX_rbv = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeX_RBV_SUFFIX));
		pvExtraDimSizeY = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeY_SUFFIX));
		pvExtraDimSizeY_rbv = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeY_RBV_SUFFIX));
	}

	public int getNumExtraDims() throws IOException {
		return pvNumExtraDims_rbv.get();
	}

	public void setNumExtraDims(int dims) throws IOException {
		pvNumExtraDims.putWait(dims);
	}

	public int getExtraDimSizeN() throws IOException {
		return pvExtraDimSizeN_rbv.get();
	}

	public void setExtraDimSizeN(int dims) throws IOException {
		pvExtraDimSizeN.putWait(dims);
	}

	public int getExtraDimSizeX() throws IOException {
		return pvExtraDimSizeX_rbv.get();
	}

	public void setExtraDimSizeX(int dims) throws IOException {
		pvExtraDimSizeX.putWait(dims);
	}

	public int getExtraDimSizeY() throws IOException {
		return pvExtraDimSizeY_rbv.get();
	}

	public void setExtraDimSizeY(int dims) throws IOException {
		pvExtraDimSizeY.putWait(dims);
	}
}
