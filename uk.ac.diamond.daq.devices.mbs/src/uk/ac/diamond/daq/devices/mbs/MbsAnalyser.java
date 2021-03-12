/**
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.NXDetector;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import uk.ac.diamond.daq.devices.mbs.api.IMbsAnalyser;

public class MbsAnalyser extends NXDetector implements IMbsAnalyser {

	private static final Logger logger = LoggerFactory.getLogger(MbsAnalyser.class);

	private MbsAnalyserController controller;
	private MbsAnalyserCollectionStrategy mbsCollectionStrategy;

	public MbsAnalyser(MbsAnalyserController controller) {
		this.controller = controller;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return controller.getCollectionTime();
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		controller.setCollectionTime(collectionTime);
	}

	@Override
	public double getAcquirePeriod() throws DeviceException {
		return controller.getAcquirePeriod();
	}

	@Override
	public void setAcquirePeriod(double acquirePeriod) throws DeviceException {
		controller.setAcquirePeriod(acquirePeriod);
	}

	@Override
	public int getIterations() throws DeviceException {
		return controller.getIterations();
	}

	@Override
	public void setIterations(int iterations) throws DeviceException {
		controller.setIterations(iterations);
	}

	@Override
	public List<String> getPassEnergies() {
		return controller.getPassEnergies();
	}

	@Override
	public int getPassEnergy() throws DeviceException {
		return parsePassEnergyValue(controller.getPassEnergy());
	}

	@Override
	public void setPassEnergy(int passEnergy) throws DeviceException {
		controller.setPassEnergy(convertPassEnergyToAnalyserString(passEnergy));
	}

	@Override
	public List<String> getLensModes() {
		return controller.getLensModes();
	}

	@Override
	public String getLensMode() throws DeviceException {
		return controller.getLensMode();
	}

	@Override
	public void setLensMode(String lensMode) throws DeviceException {
		controller.setLensMode(lensMode);
	}

	@Override
	public List<String> getAcquisitionModes() {
		return controller.getAcquisitionModes();
	}

	@Override
	public String getAcquisitionMode() throws DeviceException {
		return controller.getAcquisitionMode();
	}

	@Override
	public void setAcquisitionMode(String acquisitionMode) throws DeviceException {
		controller.setAcquisitionMode(acquisitionMode);
	}

	@Override
	public double getStartEnergy() throws DeviceException {
		return controller.getStartEnergy();
	}

	@Override
	public void setStartEnergy(double startEnergy) throws DeviceException {
		controller.setStartEnergy(startEnergy);
	}

	@Override
	public double getEndEnergy() throws DeviceException {
		return controller.getEndEnergy();
	}

	@Override
	public void setEndEnergy(double endEnergy) throws DeviceException {
		controller.setEndEnergy(endEnergy);
	}

	@Override
	public double getCentreEnergy() throws DeviceException {
		return controller.getCentreEnergy();
	}

	@Override
	public void setCentreEnergy(double centreEnergy) throws DeviceException {
		controller.setCentreEnergy(centreEnergy);
	}

	@Override
	public double getEnergyWidth() throws DeviceException {
		return controller.getEnergyWidth();
	}

	@Override
	public double getDeflectorX() throws DeviceException {
		return controller.getDeflectorX();
	}

	@Override
	public void setDeflectorX(double deflectorX) throws DeviceException {
		controller.setDeflectorX(deflectorX);
	}

	@Override
	public double getDeflectorY() throws DeviceException {
		return controller.getDeflectorY();
	}

	@Override
	public void setDeflectorY(double deflectorY) throws DeviceException {
		controller.setDeflectorY(deflectorY);
	}

	@Override
	public int getNumberOfScans() throws DeviceException {
		return controller.getNumberOfScans();
	}

	@Override
	public void setNumberOfScans(int numberOfScans) throws DeviceException {
		controller.setNumberOfScans(numberOfScans);
	}

	@Override
	public int getSlices() throws DeviceException {
		return controller.getNumberOfSlices();
	}

	@Override
	public void setSlices(int slices) throws DeviceException {
		controller.setNumberOfSlices(slices);
	}

	@Override
	public int getNumberOfSteps() throws DeviceException {
		return controller.getNumberOfSteps();
	}

	@Override
	public void setNumberOfSteps(int steps) throws DeviceException {
		controller.setNumberOfSteps(steps);
	}

	@Override
	public int getNumberOfDitherSteps() throws DeviceException {
		return controller.getNumberOfDitherSteps();
	}

	@Override
	public void setNumberOfDitherSteps(int ditherSteps) throws DeviceException {
		controller.setNumberOfDitherSteps(ditherSteps);
	}

	@Override
	public double getSpinOffset() throws DeviceException {
		return controller.getSpinOffset();
	}

	@Override
	public void setSpinOffset(double spinOffset) throws DeviceException {
		controller.setSpinOffset(spinOffset);
	}

	@Override
	public double getStepSize() throws DeviceException {
		return controller.getStepSize();
	}

	@Override
	public void setStepSize(double stepSize) throws DeviceException {
		controller.setStepSize(stepSize);
	}

	@Override
	public MbsAnalyserCollectionStrategy getCollectionStrategy() {
		return this.mbsCollectionStrategy;
	}

	@Override
	public void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
		if (!(collectionStrategy instanceof MbsAnalyserCollectionStrategy)) {
			throw new IllegalArgumentException("An instance of MbsAnalyserCollectionStrategy is required.");
		}

		super.setCollectionStrategy(collectionStrategy);
		mbsCollectionStrategy = (MbsAnalyserCollectionStrategy)collectionStrategy;
		getCollectionStrategy().setAnalyser(this);
	}

	public MbsAnalyserStatus getAnalyserStatus() throws DeviceException {
		return controller.getAnalyserStatus();
	}

	public String getStatusMessage() throws DeviceException {
		return controller.getStatusMessage();
	}

	public void startAcquiringWait() throws DeviceException {
		controller.setSingleImageMode();
		controller.startAcquiring();
		try {
			controller.waitWhileStatusBusy();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt(); // Re-interrupt the thread.
			throw new DeviceException("Interrupted while waiting for acquisition to complete", exception);
		}
	}

	public void stopAcquiring() throws DeviceException {
		controller.stopAcquiring();
	}

	public double[][] getImage() throws DeviceException {
		int imageHeight = controller.getImageDataHeight();
		int imageWidth = controller.getImageDataWidth();
		double[] imageData = controller.getImageData(imageHeight * imageWidth);

		double[][] reshapedImageData = new double[imageHeight][imageWidth];

		for (int i = 0; i < imageHeight; i++) {
			System.arraycopy(imageData, (i * imageWidth), reshapedImageData[i], 0, imageWidth);
		}

		return reshapedImageData;
	}

	public MbsAnalyserCompletedRegion getCompletedRegion() throws DeviceException {
		MbsAnalyserCompletedRegion completedRegion = new MbsAnalyserCompletedRegion();

		completedRegion.setCollectionTime(getCollectionTime());
		completedRegion.setAcquirePeriod(getAcquirePeriod());
		completedRegion.setIterations(getIterations());
		completedRegion.setPassEnergy(getPassEnergy());
		completedRegion.setLensMode(getLensMode());
		completedRegion.setAcquisitionMode(getAcquisitionMode());
		completedRegion.setStartEnergy(getStartEnergy());
		completedRegion.setEndEnergy(getEndEnergy());
		completedRegion.setCentreEnergy(getCentreEnergy());
		completedRegion.setEnergyWidth(getEnergyWidth());
		completedRegion.setDeflectorX(getDeflectorX());
		completedRegion.setDeflectorY(getDeflectorY());
		completedRegion.setNumberOfScans(getNumberOfScans());
		completedRegion.setNumberOfSlices(getSlices());
		completedRegion.setNumberfSteps(getNumberOfSteps());
		completedRegion.setNumberOfDitherSteps(getNumberOfDitherSteps());
		completedRegion.setSpinOffset(getSpinOffset());
		completedRegion.setStepSize(getStepSize());
		completedRegion.setImage(getImage());
		completedRegion.setRegionStartX(getRegionStartX());
		completedRegion.setRegionStartY(getRegionStartY());
		completedRegion.setRegionSizeX(getRegionSizeX());
		completedRegion.setRegionSizeY(getRegionSizeY());
		completedRegion.setSensorSizeX(getSensorSizeX());
		completedRegion.setSensorSizeY(getSensorSizeY());

		return completedRegion;
	}

	public int getRegionStartX() throws DeviceException {
		return controller.getRegionStartX();
	}

	public int getRegionStartY() throws DeviceException {
		return controller.getRegionStartY();
	}

	public int getRegionSizeX() throws DeviceException {
		return controller.getRegionSizeX();
	}

	public int getRegionSizeY() throws DeviceException {
		return controller.getRegionSizeY();
	}

	public int getSensorSizeX() throws DeviceException {
		return controller.getSensorSizeX();
	}

	public int getSensorSizeY() throws DeviceException {
		return controller.getSensorSizeY();
	}

	private int parsePassEnergyValue(String passEnergy) {
		// Remove "PE" from the beginning
		String trimmedPassenergy = passEnergy.substring(2);
		return Integer.parseInt(trimmedPassenergy);
	}

	private String convertPassEnergyToAnalyserString(int passEnergy) {
		String passEnergyString = Integer.toString(passEnergy);
		while (passEnergyString.length() < 3) {
			passEnergyString = "0".concat(passEnergyString);
		}
		return "PE".concat(passEnergyString);
	}
}
