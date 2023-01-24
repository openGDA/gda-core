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

import java.io.IOException;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;

/**
 * Provides access to PVs in the NDHDF5 extension to the HDF5 Area Detector plugin
 */
public class NDHDF5PVProvider {

	private static final String NumExtraDims_SUFFIX = ":HDF:NumExtraDims";
	private static final String NumExtraDims_RBV_SUFFIX = ":HDF:NumExtraDims_RBV";
	private static final String ExtraDimSizeN_SUFFIX = ":HDF:ExtraDimSizeN";
	private static final String ExtraDimSizeN_RBV_SUFFIX = ":HDF:ExtraDimSizeN_RBV";
	private static final String ExtraDimSizeX_SUFFIX = ":HDF:ExtraDimSizeX";
	private static final String ExtraDimSizeX_RBV_SUFFIX = ":HDF:ExtraDimSizeX_RBV";
	private static final String ExtraDimSizeY_SUFFIX = ":HDF:ExtraDimSizeY";
	private static final String ExtraDimSizeY_RBV_SUFFIX = ":HDF:ExtraDimSizeY_RBV";
	private static final String FilePath_SUFFIX = ":HDF:FilePath";
	private static final String FilePath_RBV_SUFFIX = ":HDF:FilePath_RBV";
	private static final String NumCapture_SUFFIX = ":HDF:NumCapture";
	private static final String NumCapture_RBV_SUFFIX = ":HDF:NumCapture_RBV";
	private static final String FileNumber_SUFFIX = ":HDF:FileNumber";
	private static final String FileNumber_RBV_SUFFIX = ":HDF:FileNumber_RBV";

	private String epicsTemplate;
	private PV<Integer> pvNumExtraDims;
	private PV<Integer> pvExtraDimSizeN;
	private ReadOnlyPV<Integer> pvNumExtraDims_rbv;
	private ReadOnlyPV<Integer> pvExtraDimSizeN_rbv;
	private PV<Integer> pvExtraDimSizeX;
	private ReadOnlyPV<Integer> pvExtraDimSizeX_rbv;
	private PV<Integer> pvExtraDimSizeY;
	private ReadOnlyPV<Integer> pvExtraDimSizeY_rbv;
	private PV<String> pvFilePath;
	private ReadOnlyPV<String> pvFilePath_rbv;
	private PV<Integer> pvNumCapture;
	private ReadOnlyPV<Integer> pvNumCapture_rbv;
	private PV<Integer> pvFileNumber;
	private ReadOnlyPV<Integer> pvFileNumber_rbv;

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
		pvNumExtraDims_rbv = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(NumExtraDims_RBV_SUFFIX));
		pvExtraDimSizeN = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeN_SUFFIX));
		pvExtraDimSizeN_rbv = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(ExtraDimSizeN_RBV_SUFFIX));
		pvExtraDimSizeX = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeX_SUFFIX));
		pvExtraDimSizeX_rbv = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(ExtraDimSizeX_RBV_SUFFIX));
		pvExtraDimSizeY = LazyPVFactory.newIntegerPV(generatePVName(ExtraDimSizeY_SUFFIX));
		pvExtraDimSizeY_rbv = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(ExtraDimSizeY_RBV_SUFFIX));
		pvFilePath = LazyPVFactory.newStringFromWaveformPV(generatePVName(FilePath_SUFFIX));
		pvFilePath_rbv = LazyPVFactory.newReadOnlyStringPV(generatePVName(FilePath_RBV_SUFFIX));
		pvNumCapture = LazyPVFactory.newIntegerPV(generatePVName(NumCapture_SUFFIX));
		pvNumCapture_rbv = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(NumCapture_RBV_SUFFIX));
		pvFileNumber = LazyPVFactory.newIntegerPV(generatePVName(FileNumber_SUFFIX));
		pvFileNumber_rbv = LazyPVFactory.newReadOnlyIntegerPV(generatePVName(FileNumber_RBV_SUFFIX));
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

	public int getNumberOfPixels() throws IOException {
		return pvNumCapture_rbv.get();
	}

	public void setNumberOfPixels(int i) throws IOException {
		pvNumCapture.putWait(i);
	}

	public String getFilePath() throws IOException {
		return pvFilePath_rbv.get();
	}

	public void setFilePath(String dataDir) throws IOException {
		pvFilePath.putNoWait(dataDir);
	}

	public int getFileNumber() throws IOException {
		return pvFileNumber_rbv.get();
	}

	public void setFileNumber(int fileNumber) throws IOException {
		pvFileNumber.putWait(fileNumber);
	}
}
