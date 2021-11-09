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
import java.util.List;

import com.cosylab.epics.caj.CAJChannel;

import gda.device.BaseEpicsDeviceController;
import gda.device.DeviceException;
import gda.device.MotorStatus;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDArray;
import gda.factory.FactoryException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.daq.pes.api.DetectorConfiguration;

public class MbsAnalyserController extends BaseEpicsDeviceController implements MonitorListener, IObservable {

	private ADBase adBase;
	private NDArray ndArray;

	private ObservableComponent observableComponent = new ObservableComponent();

	private static final String ACQUIRE_RBV = "CAM:Acquire_RBV";
	private static final String ITERATIONS = "CAM:NumScans";
	private static final String ITERATIONS_RBV = "CAM:NumScans_RBV";
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
	private static final String ESCALE_MULT_RBV = "CAM:EScaleMult_RBV";
	private static final String IMAGE_DATA_WIDTH = "ARR:ArraySize0_RBV";
	private static final String IMAGE_DATA_HEIGHT = "ARR:ArraySize1_RBV";
	private static final String ENERGY_SCALE_RBV = "CAM:EScale_RBV";
	private static final String ENERGY_SCALE_SIZE_RBV = "CAM:EScale_RBV.NORD";
	private static final String LENS_SCALE_RBV = "CAM:LensScale_RBV";
	private static final String LENS_SCALE_SIZE_RBV = "CAM:LensScale_RBV.NORD";
	private static final String ACTUAL_SCANS_RBV = "CAM:ActScans_RBV";
	private static final String CURRENT_SCAN_RBV = "CAM:CurrentScanNumber_RBV";
	private static final String IMAGE_ARRAY_SIZE_RBV = "ARR:ArrayData.NORD";
	private static final String PSU_MODE_RBV = "CAM:PsuMode_RBV";
	private static final String AUTO_DETECTOR_OFF_OVERRIDE = "CAM:DetectorOffOverride";

	private static final String FIXED_MODE_NAME = "Fixed";
	private static final String AUTO_DETECTOR_OFF_ENABLE = "Auto";
	private static final String AUTO_DETECTOR_OFF_DISABLE = "Override";


	private final List<String> passEnergies = new ArrayList<>();
	private final List<String> lensModes = new ArrayList<>();
	private final List<String> acquisitionModes = new ArrayList<>();

	public MbsAnalyserController(ADBase adBase, NDArray ndArray, String basePvName) {
		this.adBase = adBase;
		this.ndArray = ndArray;
		this.setBasePvName(basePvName);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (getBasePvName() == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new FactoryException("Configure called with no basePVName. Check spring configuration!");
		}
		logger.info("Configuring analyser with base PV: {}", getBasePvName());

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

		try {
			getEpicsController().setMonitor(getChannel(ACQUIRE_RBV), this);
		}
		catch (Exception e) {
			throw new FactoryException("Error setting up EPICS monitors", e);
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

	/** Checks whether the analyser is in fixed mode
	 *
	 * @return A boolean indicating whether the analyser is in Fixed mode
	 */
	public boolean isInFixedMode() throws DeviceException {
		return getAcquisitionMode().equalsIgnoreCase(FIXED_MODE_NAME);
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
	public Double getCentreEnergy() throws DeviceException {
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
			throw new DeviceException("Error while setting single image mode", exception);
		}

	}

	public void setContinuousImageMode() throws DeviceException {
		try {
			adBase.setImageModeWait(ImageMode.CONTINUOUS);
		} catch (Exception exception) {
			throw new DeviceException("Error while setting continous image mode", exception);
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

	public double[] getImageData() throws DeviceException {
		try {
			return ndArray.getDoubleArrayData(getImageDataArraySize());
		} catch (Exception exception) {
			throw new DeviceException("Error while  getting image data", exception);
		}
	}

	public int getImageDataArraySize() throws DeviceException {
		return getIntegerValue(IMAGE_ARRAY_SIZE_RBV, "image array size");
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

	public void setRegionStartX(int regionStartX) throws DeviceException {
		try {
			adBase.setMinX(regionStartX);
		} catch (Exception exception) {
			throw new DeviceException("Error while setting region start X", exception);
		}
	}

	public int getRegionStartY() throws DeviceException {
		try {
			return adBase.getMinY_RBV();
		} catch (Exception exception) {
			throw new DeviceException("Error while getting region start X", exception);
		}
	}

	public void setRegionStartY(int regionStartY) throws DeviceException {
		try {
			adBase.setMinY(regionStartY);
		} catch (Exception exception) {
			throw new DeviceException("Error while setting region start Y", exception);
		}
	}

	public int getRegionSizeX() throws DeviceException {
		try {
			return adBase.getSizeX_RBV();
		} catch (Exception e) {
			throw new DeviceException("Error getting region size X");
		}
	}

	public void setRegionSizeX(int regionSizeX) throws DeviceException {
		try {
			adBase.setSizeX(regionSizeX);
		} catch (Exception e) {
			throw new DeviceException("Error setting region size X");
		}
	}

	public int getRegionSizeY() throws DeviceException {
		try {
			return adBase.getSizeY_RBV();
		} catch (Exception e) {
			throw new DeviceException("Error getting region size Y");
		}
	}

	public void setRegionSizeY(int regionSizeY) throws DeviceException {
		try {
			adBase.setSizeY(regionSizeY);
		} catch (Exception e) {
			throw new DeviceException("Error setting region size Y");
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

	public void setInternalTriggerMode() throws DeviceException {
		try {
			adBase.setTriggerMode(0);
		} catch (Exception e) {
			throw new DeviceException("Error while setting internal trigger mode");
		}
	}

	public double getEnergyStepPerPixel() throws DeviceException {
		return getDoubleValue(ESCALE_MULT_RBV, "EScale Mult RBV");
	}

	public double[] getEnergyAxis() throws DeviceException {
		return getDoubleArray(ENERGY_SCALE_RBV, getEnergyAxisSize(), "energy axis");
	}

	public int getEnergyAxisSize() throws DeviceException {
		return getIntegerValue(ENERGY_SCALE_SIZE_RBV, "energy axis size");
	}

	public double[] getAngleAxis() throws DeviceException {
		return getDoubleArray(LENS_SCALE_RBV, getAngleAxisSize(), "angle axis");
	}

	public int getAngleAxisSize() throws DeviceException {
		return getIntegerValue(LENS_SCALE_SIZE_RBV, "angle axis size");
	}

	public int getCompletedIterations() throws DeviceException {
		return getIntegerValue(ACTUAL_SCANS_RBV, "completed iterations");
	}


	public int getCurrentIteration() throws DeviceException {
		return getIntegerValue(CURRENT_SCAN_RBV, "current scan number");
	}

	public String getPsuMode() throws DeviceException {
		return getStringValue(PSU_MODE_RBV, "psu mode");
	}

	public void setDetectorConfiguration(DetectorConfiguration configuration) throws Exception {
		int sensorSizeX = getSensorSizeX();
		int sensorSizeY = getSensorSizeY();

		logger.debug("setDetectorConfiguration called with configuration {}", configuration);
		logger.debug("Currently Sensor Size X is {} and Sensor Size Y is {}", sensorSizeX, sensorSizeY);

		// Validate configuration
		if (configuration.getSizeX() > sensorSizeX) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI X size %d is bigger than detector X size %d",
					configuration.getSizeX(), sensorSizeX));
		}
		if (configuration.getSizeY() > sensorSizeY) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI Y size %d is bigger than detector Y size %d",
					configuration.getSizeY(), sensorSizeY));
		}

		if (configuration.getStartX() + configuration.getSizeX() - 1 > sensorSizeX) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI X stop %d is larger than detector X size %d",
					configuration.getStartX() + configuration.getSizeX() - 1, sensorSizeX));
		}

		if (configuration.getStartY() + configuration.getSizeY() - 1 > sensorSizeY) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI Y stop %d is larger than detector Y size %d",
					configuration.getStartY() + configuration.getSizeY() - 1, sensorSizeY));
		}

		if (configuration.getSlices() < 1) {
			throw new IllegalArgumentException("There must be at least 1 slice!");
		}

		if (configuration.getSlices() > configuration.getSizeY()) {
			throw new IllegalArgumentException(String.format("Number of slices (%d) must be less than or equal to Y size (%d)",
					configuration.getSlices(), configuration.getSizeY()));
		}
		// Looks possible lets set it up
		setRegionStartX(configuration.getStartX());
		setRegionStartY(configuration.getStartY());
		setRegionSizeX(configuration.getSizeX());
		setRegionSizeY(configuration.getSizeY());
		setNumberOfSlices(configuration.getSlices());
		logger.info("Set detector ROI to: {}", configuration);
	}

	public void enableAutomaticDetectorOff() throws DeviceException {
		setStringValue(AUTO_DETECTOR_OFF_OVERRIDE, AUTO_DETECTOR_OFF_ENABLE, "DetectorOffOverride");
	}

	public void disableAutomaticDetectorOff() throws DeviceException {
		setStringValue(AUTO_DETECTOR_OFF_OVERRIDE, AUTO_DETECTOR_OFF_DISABLE, "DetectorOffOverride");
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	// Temporarily borrowed from {@link VGScientaAnalyserCamOnly}, may change later
	@Override
	public void monitorChanged(MonitorEvent event) {
		if (((CAJChannel) event.getSource()).getName().endsWith(ACQUIRE_RBV)) {
			logger.debug("EPICS has notified change of acquire status");
			DBR_Enum enumeration = (DBR_Enum) event.getDBR();
			short[] values = (short[]) enumeration.getValue();
			MotorStatus currentstatus;
			if (values[0] == 0) {
				logger.info("Been informed of a stop. Notifying observers.");
				currentstatus = MotorStatus.READY;
			} else {
				logger.info("Been informed of a start. Notifying observers.");
				currentstatus = MotorStatus.BUSY;
			}
			observableComponent.notifyIObservers(this, currentstatus);
		}
	}

	public short getDetectorState() throws DeviceException {
		try {
			return adBase.getDetectorState_RBV();
		} catch (Exception exception) {
			throw new DeviceException("Error getting detector state", exception);
		}

	}
}
