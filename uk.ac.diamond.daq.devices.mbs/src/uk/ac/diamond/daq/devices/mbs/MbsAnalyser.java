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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.MotorStatus;
import gda.device.detector.NXDetector;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.devices.mbs.api.IMbsAnalyser;
import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;
import uk.ac.diamond.daq.pes.api.DetectorConfiguration;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IElectronAnalyser.class)
public class MbsAnalyser extends NXDetector implements IMbsAnalyser {

	private static final Logger logger = LoggerFactory.getLogger(MbsAnalyser.class);

	private MbsAnalyserController controller;
	private MbsAnalyserCollectionStrategy mbsCollectionStrategy;
	private AnalyserEnergyRangeConfiguration energyRange;
	private DetectorConfiguration fixedModeConfiguration;
	private DetectorConfiguration sweptModeConfiguration;

	private double energyStepPerPixel = 0.000855;

	// MBS doesn't have PSU modes but the interface requires it so this string will be used
	// where it's requested
	private String defaultPsuModeName = "N/A";

	private double maxKE = 200.0;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}

		try {
			validateRegions();
		} catch (DeviceException exception) {
			throw new FactoryException("Unable to validate region configuration", exception);
		}

		try {
			energyStepPerPixel = controller.getEnergyStepPerPixel();
		} catch (DeviceException exception) {
			logger.warn("Unable to update energy step per pixel from EPICS. Using default value of {}", energyStepPerPixel);
		}

		setConfigured(true);
	}

	private void validateRegions() throws DeviceException {
		int sensorSizeX;
		int sensorSizeY;

		try {
			sensorSizeX = controller.getSensorSizeX();
			sensorSizeY = controller.getSensorSizeY();
		} catch (DeviceException exception) {
			logger.error("Unable to get sensor size - problem with EPICS communication");
			throw exception;
		}


		try {
			// See if the swept mode configuration can be set
			controller.setDetectorConfiguration(sweptModeConfiguration);
			logger.debug("Swept region is ok");
		} catch (Exception e3) {
			logger.info("Swept mode detector configuration is invalid:", e3);
			// If not, set the region size to the same as the sensor size
			sweptModeConfiguration.setStartX(1);
			sweptModeConfiguration.setStartY(1);
			sweptModeConfiguration.setSizeX(sensorSizeX);
			sweptModeConfiguration.setSizeY(sensorSizeY);
			sweptModeConfiguration.setSlices(sensorSizeY);
			logger.warn("Swept mode region size changed to {} x {}, starting at 1, 1. Slices {}", sensorSizeX, sensorSizeY, sensorSizeY);
		}
		try {
			// See if the fixed mode configuration can be set leave it in that state
			controller.setDetectorConfiguration(fixedModeConfiguration);
			logger.debug("Fixed region is ok");
		} catch (Exception e2) {
			logger.info("Fixed mode detector configuration is invalid:", e2);
			// If not, set the region size to the same as the sensor size
			fixedModeConfiguration.setStartX(1);
			fixedModeConfiguration.setStartY(1);
			fixedModeConfiguration.setSizeX(sensorSizeX);
			fixedModeConfiguration.setSizeY(sensorSizeY);
			fixedModeConfiguration.setSlices(sensorSizeY);
			logger.warn("Fixed mode region size changed to {} x {}, starting at 1, 1. Slices {}", sensorSizeX, sensorSizeY, sensorSizeY);
		}
	}

	public MbsAnalyser(MbsAnalyserController controller) {
		this.controller = controller;
		controller.addIObserver(this::notifyStatusChange);
	}

	private void notifyStatusChange(Object source, Object arg) {
		if (arg instanceof MotorStatus) {
			notifyIObservers(this, arg);
		}
	}

	public String getDefaultPsuModeName() {
		return defaultPsuModeName;
	}

	public void setDefaultPsuModeName(String defaultPsuModeName) {
		this.defaultPsuModeName = defaultPsuModeName;
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
	public Integer getPassEnergy() throws DeviceException {
		return parsePassEnergyValue(controller.getPassEnergy());
	}

	@Override
	public void setPassEnergy(Integer passEnergy) throws DeviceException {
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
	public Double getCentreEnergy() throws DeviceException {
		return controller.getCentreEnergy();
	}

	@Override
	public void setCentreEnergy(Double centreEnergy) throws DeviceException {
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

	@Override
	public double[][] get2DImageArray() throws DeviceException {
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
		completedRegion.setNumberOfSlices(getSlices());
		completedRegion.setNumberfSteps(getNumberOfSteps());
		completedRegion.setNumberOfDitherSteps(getNumberOfDitherSteps());
		completedRegion.setSpinOffset(getSpinOffset());
		completedRegion.setStepSize(getStepSize());
		completedRegion.setImage(get2DImageArray());
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

	private String convertPassEnergyToAnalyserString(Integer passEnergy) {
		String passEnergyString = passEnergy.toString();
		while (passEnergyString.length() < 3) {
			passEnergyString = "0".concat(passEnergyString);
		}
		return "PE".concat(passEnergyString);
	}

	@Override
	public AnalyserEnergyRangeConfiguration getEnergyRange() {
		if (energyRange == null) {
			logger.error("No energy range configured");
		}

		return energyRange;
	}

	public void setEnergyRange(AnalyserEnergyRangeConfiguration energyRange) {
		this.energyRange = energyRange;
	}

	@Override
	public double getEnergyStepPerPixel() {
		return energyStepPerPixel;
	}

	@Override
	public double getMaxKE() {
		return maxKE;
	}

	@Override
	public int getFixedModeEnergyChannels() {
		return fixedModeConfiguration.getSizeX();
	}

	@Override
	public int getSweptModeEnergyChannels() {
		return sweptModeConfiguration.getSizeY();
	}

	@Override
	public String getPsuMode() throws Exception {
		// MBS Analyser doesn't have PSU modes but this method is required to satisfy the interface
		return getDefaultPsuModeName();
	}

	@Override
	public List<String> getPsuModes() {
		return Stream.of(getDefaultPsuModeName()).collect(Collectors.toList());
	}

	@Override
	public void setPsuMode(String psuMode) throws Exception {
		// MBS Analyser doesn't have PSU modes but this method is required to satisfy the interface
	}

	@Override
	public void changeRequestedIterations(int newScheduledIterations) {
		throw new UnsupportedOperationException("Can not change iterations on this implementation");
	}

	@Override
	public double[] getEnergyAxis() throws Exception {
		return controller.getEnergyAxis();
	}

	@Override
	public double[] getAngleAxis() throws Exception {
		return controller.getAngleAxis();
	}

	@Override
	public void startContinuous() throws Exception {
		logger.info("Starting continuous acquisition");
		setFixedMode(true);
		controller.setContinuousImageMode();
		controller.setIterations(1);
		controller.startAcquiring();
	}

	@Override
	public void zeroSupplies() throws Exception {
		stopAcquiring();
	}

	@Override
	public void stopAfterCurrentIteration() throws Exception {
		logger.warn("Stop after current iteration is not currently implemented.");
	}

	@Override
	public int getCompletedIterations() throws Exception {
		return controller.getCompletedIterations();
	}

	@Override
	public int getCurrentIteration() throws Exception {
		return controller.getCurrentIteration();
	}

	@Override
	public double[] getSpectrum() throws Exception {
		logger.warn("This implementation of the MBS analyser does not provide a spectrum");
		return new double[0];
	}

	@Override
	public int getFrames() throws Exception {
		logger.warn("getFrames() is not implemented for MBS.");
		return 0;
	}

	@Override
	public double getExcitationEnergy() throws Exception {
		logger.warn("This implementation of the MBS analyser does not implement getExcitationEnergy as it is deprecated on the interface.");
		return 0;
	}

	@Override
	public double[] getExtIO(int length) throws Exception {
		logger.warn("No external I/O.");
		return new double[0];
	}

	@Override
	public void start() throws Exception {
		mbsCollectionStrategy.collectData();
	}

	@Override
	public double[] getImage() throws Exception {
		return controller.getImageData();
	}

	@Override
	public short getDetectorState() throws DeviceException {
		return controller.getDetectorState();
	}

	@Override
	public void setSingleImageMode() throws DeviceException {
		controller.setSingleImageMode();
	}

	public DetectorConfiguration getFixedModeConfiguration() {
		return fixedModeConfiguration;
	}

	public void setFixedModeConfiguration(DetectorConfiguration fixedModeConfiguration) {
		this.fixedModeConfiguration = fixedModeConfiguration;
	}

	public DetectorConfiguration getSweptModeConfiguration() {
		return sweptModeConfiguration;
	}

	public void setSweptModeConfiguration(DetectorConfiguration sweptModeConfiguration) {
		this.sweptModeConfiguration = sweptModeConfiguration;
	}

	public void setFixedMode(boolean fixed) throws Exception {
		if (fixed) {
			setupFixedMode();
		} else {
			setupSweptMode();
		}
	}

	private void setupFixedMode() throws Exception {
		// If already fixed, set the slices in the configuration to the value from EPICS. If not,
		// reset it to the region Y size.
		if (getAcquisitionMode().equals("Fixed")) {
			fixedModeConfiguration.setSlices(getSlices());
		} else {
			fixedModeConfiguration.setSlices(fixedModeConfiguration.getSizeY());
		}

		setAcquisitionMode("Fixed");
		controller.setDetectorConfiguration(fixedModeConfiguration);
		controller.setSingleImageMode();
		controller.setInternalTriggerMode();
	}

	private void setupSweptMode() throws Exception {
		setAcquisitionMode("Swept");
		controller.setDetectorConfiguration(sweptModeConfiguration);
		controller.setSingleImageMode();
		controller.setInternalTriggerMode();
	}
}
