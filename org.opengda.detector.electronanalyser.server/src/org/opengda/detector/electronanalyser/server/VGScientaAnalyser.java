/**
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

package org.opengda.detector.electronanalyser.server;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VGScientaAnalyser extends ADDetector {

	private static final long serialVersionUID = -2907729482321978030L;

	private static final Logger logger = LoggerFactory
			.getLogger(VGScientaAnalyser.class);

	private VGScientaController controller;
	private AnalyserCapabilities capabilites;
	private int[] fixedModeRegion;

	public AnalyserCapabilities getCapabilities() {
		return capabilites;
	}

	public void setCapabilities(AnalyserCapabilities ac) {
		this.capabilites = ac;
	}

	public VGScientaController getController() {
		return controller;
	}

	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	public int getNumberOfSweeptSteps() throws Exception {
		return controller.getTotalSteps();
		// FIXME this is unreliable if not wrong
		// return (int) Math.round((controller.getEndEnergy() -
		// controller.getStartEnergy()) / controller.getEnergyStep());
	}

	public double[] getEnergyAxis() throws Exception {
		double start, step;
		int length, startChannel = 0;
		if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
			int pass = controller.getPassEnergy().intValue();
			start = controller.getCentreEnergy()
					- (getCapabilities().getEnergyWidthForPass(pass) / 2);
			// TODO the following does not suitable for I09?
			step = getCapabilities().getEnergyStepForPass(pass);
			length = getAdBase().getSizeX();
			startChannel = getAdBase().getMinX();
		} else {
			start = controller.getStartEnergy();
			step = controller.getEnergyStep();
			length = getNumberOfSweeptSteps();
		}

		double[] axis = new double[length];
		for (int j = 0; j < length; j++) {
			axis[j] = start + (j + startChannel) * step;
		}
		return axis;
	}

	public double[] getAngleAxis() throws Exception {
		return getCapabilities().getAngleAxis(controller.getLensMode(),
				getAdBase().getMinY_RBV(), getAdBase().getArraySizeY_RBV());
	}

	@Override
	protected void appendDataAxes(NXDetectorData data) throws Exception {
		if (firstReadoutInScan) {
			int i = 1;
			String aname = "energies";
			String aunit = "eV";
			double[] axis = getEnergyAxis();

			data.addAxis(getName(), aname, new int[] { axis.length },
					NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit, false);

			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "angles";
				aunit = "degree";
			} else {
				aname = "location";
				aunit = "mm";
			}
			axis = getAngleAxis();

			data.addAxis(getName(), aname, new int[] { axis.length },
					NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit, false);
		}
	}

	public void prepareFixedMode() throws Exception {
		controller.setAcquisitionMode("Fixed");
		getAdBase().setMinX(fixedModeRegion[0]);
		getAdBase().setMinY(fixedModeRegion[1]);
		getAdBase().setSizeX(fixedModeRegion[2]);
		getAdBase().setSizeY(fixedModeRegion[3]);
		getAdBase().setImageMode(0);
		getAdBase().setTriggerMode(0);
		controller.setSlice(fixedModeRegion[3]);
	}

	public int[] getFixedModeRegion() {
		return fixedModeRegion;
	}

	public void setFixedModeRegion(int[] fixedModeRegion) {
		this.fixedModeRegion = fixedModeRegion;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return getAdBase().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("error getting collection time", e);
		}
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try {
			getAdBase().setAcquireTime(collectionTime);
		} catch (Exception e) {
			throw new DeviceException("error setting collection time", e);
		}
	}
	
	public void setNumberInterations(int value) throws Exception {
		getAdBase().setNumExposures(value);
	}
	
	public Integer getNumberIterations() throws Exception {
		return getAdBase().getNumExposures_RBV();
	}
	public void setCameraMinX(int value) throws Exception {
		getAdBase().setMinX(value);
	}
	
	public int getCameraMinX() throws Exception {
		return getAdBase().getMinX_RBV();
	}
	public void setCameraMinY(int value) throws Exception {
		getAdBase().setMinY(value);
	}
	
	public int getCameraMinY() throws Exception {
		return getAdBase().getMinY_RBV();
	}
	public void setCameraSizeX(int value) throws Exception {
		getAdBase().setSizeX(value);
	}
	public int getCameraSizeX() throws Exception {
		return getAdBase().getSizeX_RBV();
	}
	public void setCameraSizeY(int value) throws Exception {
		getAdBase().setSizeY(value);
	}
	public void setImageMode(ImageMode imagemode) throws Exception {
		getAdBase().setImageMode(imagemode);
	}
	public int getCameraSizeY() throws Exception {
		return getAdBase().getSizeY_RBV();
	}

	public void setLensMode(String value) throws Exception {
		controller.setLensMode(value);
	}

	public String getLensMode() throws Exception {
		return controller.getLensMode();
	}

	public void setAcquisitionMode(String value) throws Exception {
		controller.setAcquisitionMode(value);
	}

	public String getAcquisitionMode() throws Exception {
		return controller.getAcquisitionMode();
	}

	public void setEnergysMode(String value) throws Exception {
		controller.setEnergyMode(value);
	}

	public String getEnergysMode() throws Exception {
		return controller.getEnergyMode();
	}

	public void setDetectorMode(String value) throws Exception {
		controller.setDetectorMode(value);
	}

	public String getDetectorMode() throws Exception {
		return controller.getDetectorMode();
	}

	public void setElement(String value) throws Exception {
		controller.setElement(value);
	}

	public String getElement() throws Exception {
		return controller.getElement();
	}

	public void setPassEnergy(Integer value) throws Exception {
		controller.setPassEnergy(value);
	}

	public Integer getPassEnergy() throws Exception {
		return controller.getPassEnergy();
	}

	public void setStartEnergy(Double value) throws Exception {
		controller.setStartEnergy(value);
	}

	public Double getStartEnergy() throws Exception {
		return getStartEnergy();
	}

	public void setCentreEnergy(Double value) throws Exception {
		controller.setCentreEnergy(value);
	}

	public Double getCentreEnergy() throws Exception {
		return controller.getCentreEnergy();
	}

	public void setEndEnergy(Double value) throws Exception {
		controller.setEndEnergy(value);
	}

	public Double getEndEnergy() throws Exception {
		return controller.getEndEnergy();
	}

	public void setEnergyStep(Double value) throws Exception {
		controller.setEnergyStep(value);
	}

	public Double getEnergyStep() throws Exception {
		return controller.getEnergyStep();
	}

	public void setFrames(Integer value) throws Exception {
		controller.setFrames(value);
	}

	public Integer getFrames() throws Exception {
		return controller.getFrames();
	}

	public void setStepTime(double value) throws Exception {
		controller.setStepTime(value);
	}

	public void setSlices(int value) throws Exception {
		controller.setSlice(value);
	}

	public int getSlices() throws Exception {
		return controller.getSlice();
	}

	public Integer getTotalSteps() throws Exception {
		return controller.getTotalSteps();
	}

	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}
}