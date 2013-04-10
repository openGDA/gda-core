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

public class VGScientaAnalyser extends ADDetector implements IVGScientaAnalyser {

	private static final long serialVersionUID = -2907729482321978030L;

	private static final Logger logger = LoggerFactory
			.getLogger(VGScientaAnalyser.class);

	private VGScientaController controller;
	private AnalyserCapabilities capabilites;
	private int[] fixedModeRegion;

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCapabilities()
	 */
	@Override
	public AnalyserCapabilities getCapabilities() {
		return capabilites;
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCapabilities(org.opengda.detector.electronanalyser.server.AnalyserCapabilities)
	 */
	@Override
	public void setCapabilities(AnalyserCapabilities ac) {
		this.capabilites = ac;
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getController()
	 */
	@Override
	public VGScientaController getController() {
		return controller;
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setController(org.opengda.detector.electronanalyser.server.VGScientaController)
	 */
	@Override
	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getNumberOfSweeptSteps()
	 */
	@Override
	public int getNumberOfSweeptSteps() throws Exception {
		return controller.getTotalSteps();
		// FIXME this is unreliable if not wrong
		// return (int) Math.round((controller.getEndEnergy() -
		// controller.getStartEnergy()) / controller.getEnergyStep());
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getEnergyAxis()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getAngleAxis()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#prepareFixedMode()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getFixedModeRegion()
	 */
	@Override
	public int[] getFixedModeRegion() {
		return fixedModeRegion;
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setFixedModeRegion(int[])
	 */
	@Override
	public void setFixedModeRegion(int[] fixedModeRegion) {
		this.fixedModeRegion = fixedModeRegion;
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCollectionTime()
	 */
	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return getAdBase().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("error getting collection time", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCollectionTime(double)
	 */
	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try {
			getAdBase().setAcquireTime(collectionTime);
		} catch (Exception e) {
			throw new DeviceException("error setting collection time", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setNumberInterations(int)
	 */
	@Override
	public void setNumberInterations(int value) throws Exception {
		getAdBase().setNumExposures(value);
	}
	
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getNumberIterations()
	 */
	@Override
	public Integer getNumberIterations() throws Exception {
		return getAdBase().getNumExposures_RBV();
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCameraMinX(int)
	 */
	@Override
	public void setCameraMinX(int value) throws Exception {
		getAdBase().setMinX(value);
	}
	
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCameraMinX()
	 */
	@Override
	public int getCameraMinX() throws Exception {
		return getAdBase().getMinX_RBV();
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCameraMinY(int)
	 */
	@Override
	public void setCameraMinY(int value) throws Exception {
		getAdBase().setMinY(value);
	}
	
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCameraMinY()
	 */
	@Override
	public int getCameraMinY() throws Exception {
		return getAdBase().getMinY_RBV();
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCameraSizeX(int)
	 */
	@Override
	public void setCameraSizeX(int value) throws Exception {
		getAdBase().setSizeX(value);
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCameraSizeX()
	 */
	@Override
	public int getCameraSizeX() throws Exception {
		return getAdBase().getSizeX_RBV();
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCameraSizeY(int)
	 */
	@Override
	public void setCameraSizeY(int value) throws Exception {
		getAdBase().setSizeY(value);
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setImageMode(gda.device.detector.areadetector.v17.ADBase.ImageMode)
	 */
	@Override
	public void setImageMode(ImageMode imagemode) throws Exception {
		getAdBase().setImageMode(imagemode);
	}
	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCameraSizeY()
	 */
	@Override
	public int getCameraSizeY() throws Exception {
		return getAdBase().getSizeY_RBV();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setLensMode(java.lang.String)
	 */
	@Override
	public void setLensMode(String value) throws Exception {
		controller.setLensMode(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getLensMode()
	 */
	@Override
	public String getLensMode() throws Exception {
		return controller.getLensMode();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setAcquisitionMode(java.lang.String)
	 */
	@Override
	public void setAcquisitionMode(String value) throws Exception {
		controller.setAcquisitionMode(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getAcquisitionMode()
	 */
	@Override
	public String getAcquisitionMode() throws Exception {
		return controller.getAcquisitionMode();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setEnergysMode(java.lang.String)
	 */
	@Override
	public void setEnergysMode(String value) throws Exception {
		controller.setEnergyMode(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getEnergysMode()
	 */
	@Override
	public String getEnergysMode() throws Exception {
		return controller.getEnergyMode();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setDetectorMode(java.lang.String)
	 */
	@Override
	public void setDetectorMode(String value) throws Exception {
		controller.setDetectorMode(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getDetectorMode()
	 */
	@Override
	public String getDetectorMode() throws Exception {
		return controller.getDetectorMode();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setElement(java.lang.String)
	 */
	@Override
	public void setElement(String value) throws Exception {
		controller.setElement(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getElement()
	 */
	@Override
	public String getElement() throws Exception {
		return controller.getElement();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setPassEnergy(java.lang.Integer)
	 */
	@Override
	public void setPassEnergy(Integer value) throws Exception {
		controller.setPassEnergy(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getPassEnergy()
	 */
	@Override
	public Integer getPassEnergy() throws Exception {
		return controller.getPassEnergy();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setStartEnergy(java.lang.Double)
	 */
	@Override
	public void setStartEnergy(Double value) throws Exception {
		controller.setStartEnergy(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getStartEnergy()
	 */
	@Override
	public Double getStartEnergy() throws Exception {
		return getStartEnergy();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setCentreEnergy(java.lang.Double)
	 */
	@Override
	public void setCentreEnergy(Double value) throws Exception {
		controller.setCentreEnergy(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getCentreEnergy()
	 */
	@Override
	public Double getCentreEnergy() throws Exception {
		return controller.getCentreEnergy();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setEndEnergy(java.lang.Double)
	 */
	@Override
	public void setEndEnergy(Double value) throws Exception {
		controller.setEndEnergy(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getEndEnergy()
	 */
	@Override
	public Double getEndEnergy() throws Exception {
		return controller.getEndEnergy();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setEnergyStep(java.lang.Double)
	 */
	@Override
	public void setEnergyStep(Double value) throws Exception {
		controller.setEnergyStep(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getEnergyStep()
	 */
	@Override
	public Double getEnergyStep() throws Exception {
		return controller.getEnergyStep();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setFrames(java.lang.Integer)
	 */
	@Override
	public void setFrames(Integer value) throws Exception {
		controller.setFrames(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getFrames()
	 */
	@Override
	public Integer getFrames() throws Exception {
		return controller.getFrames();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setStepTime(double)
	 */
	@Override
	public void setStepTime(double value) throws Exception {
		controller.setStepTime(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#setSlices(int)
	 */
	@Override
	public void setSlices(int value) throws Exception {
		controller.setSlice(value);
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getSlices()
	 */
	@Override
	public int getSlices() throws Exception {
		return controller.getSlice();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#getTotalSteps()
	 */
	@Override
	public Integer getTotalSteps() throws Exception {
		return controller.getTotalSteps();
	}

	/* (non-Javadoc)
	 * @see org.opengda.detector.electronanalyser.server.IVGScientaAnalyser#zeroSupplies()
	 */
	@Override
	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}
}