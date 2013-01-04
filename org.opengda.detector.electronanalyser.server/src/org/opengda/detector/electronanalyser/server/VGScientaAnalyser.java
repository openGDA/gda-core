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

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VGScientaAnalyser extends gda.device.detector.addetector.ADDetector {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2907729482321978030L;

	private static final Logger logger = LoggerFactory.getLogger(VGScientaAnalyser.class);

	private VGScientaController controller;
	private AnalyserCapabilties ac;
	private int[] fixedModeRegion;

	public AnalyserCapabilties getCapabilities() {
		return ac;
	}

	public void setCapabilities(AnalyserCapabilties ac) {
		this.ac = ac;
	}

	public VGScientaController getController() {
		return controller;
	}

	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	public int getNumberOfSweeptSteps() throws Exception {
		//FIXME this is unreliable if not wrong
		return (int) Math.round((controller.getEndEnergy() - controller.getStartEnergy()) / controller.getEnergyStep()); 
	}
	
	public double[] getEnergyAxis() throws Exception {
		double start, step;
		int length, startChannel = 0;
		if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
			int pass = controller.getPassEnergy().intValue();
			start = controller.getCentreEnergy() - (getCapabilities().getEnergyWidthForPass(pass) / 2);
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
			axis[j] = start + (j+startChannel) * step;
		}
		return axis;
	}

	public double[] getAngleAxis() throws Exception {
		return getCapabilities().getAngleAxis(controller.getLensMode(), getAdBase().getMinY_RBV(),
				getAdBase().getArraySizeY_RBV());
	}

	@Override
	protected void appendDataAxes(NXDetectorData data) throws Exception {
		if (firstReadoutInScan) {
			int i = 1;
			String aname = "energies";
			String aunit = "eV";
			double[] axis = getEnergyAxis();

			data.addAxis(getName(), aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit,
					false);

			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "angles";
				aunit = "degree";
			} else {
				aname = "location";
				aunit = "mm";
			}
			axis = getAngleAxis();

			data.addAxis(getName(), aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit,
					false);
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
	
	public void setLensMode(String value) throws Exception {
		controller.setLensMode(value);
	}
	
	public String getLensMode() throws Exception {
		return controller.getLensMode();
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
	
	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}
}