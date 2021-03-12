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

package uk.ac.diamond.daq.devices.mbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDArray;
import gda.epics.connection.EpicsController;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class MbsAnalyserController extends ConfigurableBase {

	private static final Logger logger = LoggerFactory.getLogger(MbsAnalyserController.class);

	private ADBase adBase;
	private NDArray ndArray;
	private EpicsController epicsController = EpicsController.getInstance();
	private String basePvName;
	private final Map<String, Channel> channels = new HashMap<>();

	private static final String EPICS_GET_ERROR_MESSAGE_TEMPLATE = "Unable to get %s from EPICS";
	private static final String EPICS_SET_ERROR_MESSAGE_TEMPLATE = "Unable to set %s to %s via EPICS";

	private static final String ITERATIONS = "CAM:NumExposures";
	private static final String ITERATIONS_RBV = "CAM:NumExposures_RBV";
	private static final String PASS_ENERGY = "CAM:PassEnergy";
	private static final String LENS_MODE = "CAM:LensMode";
	private static final String ACQUISITION_MODE = "CAM:AcqMode";
	private static final String START_ENERGY = "CAM:StartKE";
	private static final String START_ENERGY_RBV = "CAM:StartKE_RBV";
	private static final String END_ENERGY = "CAM:EndKE";
	private static final String END_ENERGY_RBV = "CAM:EndKE_RBV";
	private static final String CENTRE_ENERGY = "CAM:CentreKE";
	private static final String CENTRE_ENERGY_RBV = "CAM:CentreKE_RBV";
	private static final String ENERGY_WIDTH_RBV = "CAM:Width_RBV";
	private static final String DEFLECTOR_X = "CAM:DeflX";
	private static final String DEFLECTOR_X_RBV = "CAM:DeflX_RBV";
	private static final String DEFLECTOR_Y = "CAM:DeflY";
	private static final String DEFLECTOR_Y_RBV = "CAM:DeflY_RBV";
	private static final String NUMBER_OF_SCANS = "CAM:NumScans";
	private static final String NUMBER_OF_SCANS_RBV = "CAM:NumScans_RBV";
	private static final String NUMBER_OF_SLICES = "CAM:NumSlice";
	private static final String NUMBER_OF_SLICES_RBV = "CAM:NumSlice_RBV";
	private static final String NUMBER_OF_STEPS = "CAM:NumSteps";
	private static final String NUMBER_OF_STEPS_RBV = "CAM:NumSteps_RBV";
	private static final String NUMBER_OF_DITHER_STEPS = "CAM:DithSteps";
	private static final String NUMBER_OF_DITHER_STEPS_RBV = "CAM:DithSteps_RBV";
	private static final String SPIN_OFFSET = "CAM:SpinOffs";
	private static final String SPIN_OFFSET_RBV = "CAM:SpinOffs_RBV";
	private static final String STEP_SIZE = "CAM:StepSize";
	private static final String STEP_SIZE_RBV = "CAM:StepSize_RBV";
	private static final String IMAGE_DATA_WIDTH = "ARR:ArraySize0_RBV";
	private static final String IMAGE_DATA_HEIGHT = "ARR:ArraySize1_RBV";

	private final List<String> passEnergies = new ArrayList<>();
	private final List<String> lensModes = new ArrayList<>();
	private final List<String> acquisitionModes = new ArrayList<>();

	public MbsAnalyserController(ADBase adBase, NDArray ndArray, String basePvName) {
		this.adBase = adBase;
		this.ndArray = ndArray;
		this.basePvName = basePvName;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (basePvName == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new FactoryException("Configure called with no basePVName. Check spring configuration!");
		}
		logger.info("Configuring analyser with base PV: {}", basePvName);

		try {
			initialiseEnumChannel(PASS_ENERGY, passEnergies);
			logger.debug("Available pass energies: {}", passEnergies);
			initialiseEnumChannel(LENS_MODE, lensModes);
			logger.debug("Available lens modes: {}", lensModes);
			initialiseEnumChannel(ACQUISITION_MODE, acquisitionModes);
			logger.debug("Available acquisition modes: {}", acquisitionModes);
		} catch (Exception e) {
			throw new FactoryException("Configuring the analyser failed", e);
		}

		logger.info("Finished configuring analyser");
		setConfigured(true);
	}

	/**
	 * Gets the collection time
	 *
	 * @return The collection time
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	public double getCollectionTime() throws DeviceException {
		try {
			return adBase.getAcquireTime();
		} catch (Exception exception) {
			throw new DeviceException("Error getting acquire time", exception);
		}
	}

	/**
	 * Sets the collection time
	 *
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try {
			adBase.setAcquireTime(collectionTime);
		} catch (Exception exception) {
			throw new DeviceException("Error setting acquire time", exception);
		}
	}

	/**
	 * Gets the acquisition period
	 *
	 * @return The acquisition period
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	public double getAcquirePeriod() throws DeviceException {
		try {
			return adBase.getAcquirePeriod();
		} catch (Exception exception) {
			throw new DeviceException("Error getting acquire period", exception);
		}
	}

	/**
	 * Sets the acquisition period
	 *
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	public void setAcquirePeriod(double acquirePeriod) throws DeviceException {
		try {
			adBase.setAcquirePeriod(acquirePeriod);
		} catch (Exception exception) {
			throw new DeviceException("Error setting acquire period", exception);
		}
	}

	/**
	 * Gets the number of iterations (exposures per image)
	 *
	 * @return The number of iterations
	 * @throws DeviceException If there is a problem with the EPICS communication
	 */
	public int getIterations() throws DeviceException {
		return getIntegerValue(ITERATIONS_RBV, "iterations");
	}

	/**
	 * Sets the number of iterations (exposures per image)
	 *
	 * @param iterations The number of iterations
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setIterations(int iterations) throws DeviceException {
		setIntegerValue(ITERATIONS, iterations, "iterations");
	}

	/**
	 * Gets a list of all available pass energies.
	 * <p>
	 * This does <b>not</b> check if the pass energies are usable with the current PSU mode.
	 *
	 * @return List of the available pass energies
	 */
	public List<String> getPassEnergies() {
		return new ArrayList<>(passEnergies);
	}

	/**
	 * Gets the current pass energy
	 *
	 * @return The pass energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public String getPassEnergy() throws DeviceException {
		return getStringValue(PASS_ENERGY, "pass energy");
	}

	/**
	 * Set the pass energy
	 *
	 * @param passEnergy The pass energy to be set
	 * @throws DeviceException
	 */
	public void setPassEnergy(String passEnergy) throws DeviceException {
		if (!passEnergies.contains(passEnergy)) {
			throw new DeviceException("The specified pass energy is not valid.");
		}
		setStringValue(PASS_ENERGY, passEnergy, "pass energy");
	}

	/**
	 * Gets the list of available lens modes
	 *
	 * @return The list of available lens modes
	 */
	public List<String> getLensModes() {
		return new ArrayList<>(lensModes);
	}

	/**
	 * Gets the current lens mode
	 *
	 * @return The current lens mode
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public String getLensMode() throws DeviceException {
		return getStringValue(LENS_MODE, "lens mode");
	}

	/**
	 * Sets the current lens mode
	 *
	 * @param lensMode The requested lens mode
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setLensMode(String lensMode) throws DeviceException {
		if (!lensModes.contains(lensMode)) {
			throw new DeviceException("The specified lens mode is not valid");
		}
		setStringValue(LENS_MODE, lensMode, "lens mode");
	}

	/**
	 * Gets the list of available acquisition modes
	 *
	 * @return the list of available acquisition modes
	 */
	public List<String> getAcquisitionModes() {
		return new ArrayList<>(acquisitionModes);
	}

	/**
	 * Gets the current acquisition mode
	 *
	 * @return The current acquisition mode
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public String getAcquisitionMode() throws DeviceException {
		return getStringValue(ACQUISITION_MODE, "acquisition mode");
	}

	/**
	 * Sets the current acquisition mode
	 *
	 * @param acquisitionMode The requested acquisition mode
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setAcquisitionMode(String acquisitionMode) throws DeviceException {
		if (!acquisitionModes.contains(acquisitionMode)) {
			throw new DeviceException("The specified acquisition mode is not valid");
		}
		setStringValue(ACQUISITION_MODE, acquisitionMode, "acquisition mode");
	}

	/**
	 * Gets the start energy
	 *
	 * @return The start energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getStartEnergy() throws DeviceException {
		return getDoubleValue(START_ENERGY_RBV, "start energy");
	}

	/**
	 * Sets the start energy
	 *
	 * @param startEnergy The start energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setStartEnergy(double startEnergy) throws DeviceException {
		setDoubleValue(START_ENERGY, startEnergy, "start energy");
	}

	/**
	 * Gets the end energy
	 *
	 * @return The end energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getEndEnergy() throws DeviceException {
		return getDoubleValue(END_ENERGY_RBV, "end energy");
	}

	/**
	 * Sets the end energy
	 *
	 * @param endEnergy The end energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setEndEnergy(double endEnergy) throws DeviceException {
		setDoubleValue(END_ENERGY, endEnergy, "end energy");
	}

	/**
	 * Gets the centre energy
	 *
	 * @return The end energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getCentreEnergy() throws DeviceException {
		return getDoubleValue(CENTRE_ENERGY_RBV, "centre energy");
	}

	/**
	 * Sets the centre energy
	 *
	 * @param centreEnergy The end energy
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setCentreEnergy(double centreEnergy) throws DeviceException {
		setDoubleValue(CENTRE_ENERGY, centreEnergy, "centre energy");
	}

	/**
	 * Gets the energy width
	 * @return The energy width
	 * @throws DeviceException
	 */
	public double getEnergyWidth() throws DeviceException {
		return getDoubleValue(ENERGY_WIDTH_RBV, "energy width");
	}

	/**
	 * Gets the deflector X value
	 *
	 * @return The deflector X value
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getDeflectorX() throws DeviceException {
		return getDoubleValue(DEFLECTOR_X_RBV, "deflector X");
	}

	/**
	 * Sets the deflector X value
	 *
	 * @param deflectorX The deflector X value
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setDeflectorX(double deflectorX) throws DeviceException {
		setDoubleValue(DEFLECTOR_X, deflectorX, "deflector X");
	}

	/**
	 * Gets the deflector Y value
	 *
	 * @return The deflector Y value
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getDeflectorY() throws DeviceException {
		return getDoubleValue(DEFLECTOR_Y_RBV, "deflector Y");
	}

	/**
	 * Sets the deflector Y value
	 *
	 * @param deflectorY The deflector Y value
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setDeflectorY(double deflectorY) throws DeviceException {
		setDoubleValue(DEFLECTOR_Y, deflectorY, "deflector Y");
	}

	/**
	 * Gets the number of scans
	 *
	 * @return The number of scans
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public int getNumberOfScans() throws DeviceException {
		return getIntegerValue(NUMBER_OF_SCANS_RBV, "number of scans");
	}

	/**
	 * Sets the number of scans
	 *
	 * @param numberOfScans The number of scans
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setNumberOfScans(int numberOfScans) throws DeviceException {
		setIntegerValue(NUMBER_OF_SCANS, numberOfScans, "number of scans");
	}

	/**
	 * Gets the number of slices
	 *
	 * @return The number of slices
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public int getNumberOfSlices() throws DeviceException {
		return getIntegerValue(NUMBER_OF_SLICES_RBV, "number of slices");
	}

	/**
	 * Sets the number of slices
	 *
	 * @param slices The number of slices
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setNumberOfSlices(int slices) throws DeviceException {
		setIntegerValue(NUMBER_OF_SLICES, slices, "number of slices");
	}

	/**
	 * Gets the number of steps
	 *
	 * @return The number of steps
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public int getNumberOfSteps() throws DeviceException {
		return getIntegerValue(NUMBER_OF_STEPS_RBV, "number of steps");
	}

	/**
	 * Sets the number of steps
	 *
	 * @param steps The number of steps
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setNumberOfSteps(int steps) throws DeviceException {
		setIntegerValue(NUMBER_OF_STEPS, steps, "number of steps");
	}

	/**
	 * Gets the number of dither steps
	 *
	 * @return The number of dither steps
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public int getNumberOfDitherSteps() throws DeviceException {
		return getIntegerValue(NUMBER_OF_DITHER_STEPS_RBV, "number of dither steps");
	}

	/**
	 * Sets the number of dither steps
	 *
	 * @param ditherSteps The number of dither steps
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setNumberOfDitherSteps(int ditherSteps) throws DeviceException {
		setIntegerValue(NUMBER_OF_DITHER_STEPS, ditherSteps, "number of dither steps");
	}

	/**
	 * Gets the spin offset
	 *
	 * @return The spin offset
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getSpinOffset() throws DeviceException {
		return getDoubleValue(SPIN_OFFSET_RBV, "spin offset");
	}

	/**
	 * Sets the spin offset
	 *
	 * @param spinOffset The spin offset
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setSpinOffset(double spinOffset) throws DeviceException {
		setDoubleValue(SPIN_OFFSET, spinOffset, "spin offset");
	}

	/**
	 * Gets the step size
	 *
	 * @return step size
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public double getStepSize() throws DeviceException {
		return getDoubleValue(STEP_SIZE_RBV, "step size");
	}

	/**
	 * Sets the step size
	 *
	 * @param stepSize The step size
	 * @throws DeviceException If there is a problem with EPICS communication
	 */
	public void setStepSize(double stepSize) throws DeviceException {
		setDoubleValue(STEP_SIZE, stepSize, "step size");
	}

	public void setSingleImageMode() throws DeviceException {
		try {
			adBase.setImageModeWait(ImageMode.SINGLE);
		} catch (Exception exception) {
			throw new DeviceException("Error while setting image mode", exception);
		}

	}

	public void startAcquiring() throws DeviceException {
		try {
			adBase.startAcquiring();
		} catch (Exception exception) {
			throw new DeviceException("Error while starting acquisition", exception);
		}
	}

	public void stopAcquiring() throws DeviceException {
		try {
			adBase.stopAcquiring();
		} catch (Exception exception) {
			throw new DeviceException("Error while stopping acquisition", exception);
		}
	}

	public MbsAnalyserStatus getAnalyserStatus() throws DeviceException {
		try {
			return MbsAnalyserStatus.get(adBase.getDetectorState_RBV());
		} catch (Exception exception) {
			throw new DeviceException("Error while  getting detector state", exception);
		}
	}

	public String getStatusMessage() throws DeviceException {
		try {
			return adBase.getStatusMessage_RBV();
		} catch (Exception exception) {
			throw new DeviceException("Error while  getting detector state", exception);
		}
	}

	public int waitWhileStatusBusy() throws InterruptedException {
		return adBase.waitWhileStatusBusy();
	}

	public double[] getImageData(int numberOfElements) throws DeviceException {
		try {
			return ndArray.getDoubleArrayData(numberOfElements);
		} catch (Exception exception) {
			throw new DeviceException("Error while  getting image data", exception);
		}
	}

	public int getImageDataWidth() throws DeviceException {
		return getIntegerValue(IMAGE_DATA_WIDTH, "image data width");
	}

	public int getImageDataHeight() throws DeviceException {
		return getIntegerValue(IMAGE_DATA_HEIGHT, "image data height");
	}

	public int getRegionStartX() throws DeviceException {
		try {
			return adBase.getMinX_RBV();
		} catch (Exception exception) {
			throw new DeviceException("Error while getting region start X", exception);
		}
	}

	public int getRegionStartY() throws DeviceException {
		try {
			return adBase.getMinY_RBV();
		} catch (Exception exception) {
			throw new DeviceException("Error while getting region start X", exception);
		}
	}

	public int getRegionSizeX() throws DeviceException {
		try {
			return adBase.getSizeX_RBV();
		} catch (Exception e) {
			throw new DeviceException("Error getting region size X");
		}
	}

	public int getRegionSizeY() throws DeviceException {
		try {
			return adBase.getSizeY_RBV();
		} catch (Exception e) {
			throw new DeviceException("Error getting region size Y");
		}
	}

	public int getSensorSizeX() throws DeviceException {
		try {
			return adBase.getMaxSizeX_RBV();
		} catch (Exception e) {
			throw new DeviceException("Error while getting sensor size X");
		}
	}

	public int getSensorSizeY() throws DeviceException {
		try {
			return adBase.getMaxSizeY_RBV();
		} catch (Exception e) {
			throw new DeviceException("Error while getting sensor size Y");
		}
	}

	private void initialiseEnumChannel(String channel, List<String> list) throws Exception {
		String[] positionLabels = null;
		positionLabels = epicsController.cagetLabels(getChannel(channel));
		if (positionLabels == null || positionLabels.length == 0) {
			throw new DeviceException("Error getting labels from enum channel: " + basePvName + channel);
		}
		// Clear the list here this allows for rerunning configure
		list.clear();
		// Add the positions to the list
		for (String position : positionLabels) {
			if (position == null || position.isEmpty()) {
				logger.warn("Enum channel {} contains empty entries", basePvName + channel);
			} else {
				list.add(position);
			}
		}
	}

	private int getIntegerValue(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(channelName));
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	private void setIntegerValue(String channelName, int value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(channelName), value);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	private double getDoubleValue(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(channelName));
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	private void setDoubleValue(String channelName, double value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(channelName), value);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	private String getStringValue(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetString(getChannel(channelName));
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	private void setStringValue(String channelName, String value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(channelName), value);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	private Channel getChannel(String pvSuffix) throws TimeoutException, CAException {
		String fullPvName = basePvName + pvSuffix;
		Channel channel = channels.get(fullPvName);

		if (channel == null) {
			channel = epicsController.createChannel(fullPvName);
			channels.put(fullPvName, channel);
		}

		return channel;
	}
}
