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

package uk.ac.gda.devices.vgscienta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.pes.api.DetectorConfiguration;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * The EPICS controller class for operating VG Scienta electron analysers. The class interact only with the CAM plugin which contains the specific PVs to
 * control the electron analyser, and to read back the collected data.
 * <p>
 * Only operation in kinetic energy (KE) mode is supported. For users to work in binding energy (BE) that needs to be implemented at a higher level.
 * <p>
 * <ul>
 * <li>All energies are in electron volts (eV)
 * <li>All angles are in degrees (deg)
 * <li>All positions are in millimetres (mm)
 * </ul>
 *
 * @author James Mudd
 */
public class VGScientaController extends ConfigurableBase {
	private static final DeprecationLogger logger = DeprecationLogger.getLogger(VGScientaController.class);

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName = null;
	/** Map that stores the channel against the PV name */
	private Map<String, Channel> channelMap = new HashMap<>();

	/**
	 * PSU mode defines the current setup of the hardware. This should only be changed if the power supplies are physically rewired!
	 * VG Scienta refer to this as element set (its the set of power supply elements used but PSU mode is clearer and more general so its used here)
	 */
	private static final String PSU_MODE = "ELEMENT_SET";
	private static final String PSU_MODE_RBV = "ELEMENT_SET_RBV";

	// Lens mode
	private static final String LENS_MODE = "LENS_MODE";
	private static final String LENS_MODE_RBV = "LENS_MODE_RBV";
	/** Acquisition mode is fixed or swept */
	private static final String ACQUISITION_MODE = "ACQ_MODE";
	private static final String ACQUISITION_MODE_RBV = "ACQ_MODE_RBV";

	// To allow conversion to binding energy the analyser needs to know the excitation energy
	// This shouldn't be used as binding energy mode is not supported but is here for legacy support
	// private static final String EXCITATION_ENERGY = "EXCITATION_ENERGY";
	// private static final String EXCITATION_ENERGY_RBV = "EXCITATION_ENERGY_RBV";

	/** This can be used to switch the detector between ADC and Pulse Counting modes (ADC is typical) */
	private static final String DETECTOR_MODE = "DETECTOR_MODE";
	private static final String DETECTOR_MODE_RBV = "DETECTOR_MODE_RBV";

	// Scan energy settings
	/** The pass energy can be seen as a energy resolution setting. Small pass energy -> high energy resolution */
	private static final String PASS_ENERGY = "PASS_ENERGY";
	private static final String PASS_ENERGY_RBV = "PASS_ENERGY_RBV";
	/** Centre energy is used in fixed mode to define the centre of the KE range */
	private static final String CENTRE_ENERGY = "CENTRE_ENERGY";
	private static final String CENTRE_ENERGY_RBV = "CENTRE_ENERGY_RBV";
	// In swept mode the energy range is defined by start, stop and step
	private static final String START_ENERGY = "LOW_ENERGY";
	private static final String START_ENERGY_RBV = "LOW_ENERGY_RBV";
	private static final String END_ENERGY = "HIGH_ENERGY";
	private static final String END_ENERGY_RBV = "HIGH_ENERGY_RBV";
	/** Energy step is only applicable in swept mode, in fixed the energy step is a function of pass energy. */
	private static final String ENERGY_STEP = "STEP_SIZE";
	private static final String ENERGY_STEP_RBV = "STEP_SIZE_RBV";

	// Scan exposure settings
	// Note frames and exposure time are linked by the fixed camera frame rate
	private static final String FRAMES = "FRAMES";
	private static final String FRAMES_RBV = "FRAMES_RBV";
	/** The fixed camera frame rate SES is using */
	private static final String CAMERA_FRAME_RATE = "MAX_FRAMES_RBV";
	/** Exposure time requested. SES will give the closest number of frames available (Mirroring a standard AD PV) */
	private static final String EXPOSURE_TIME = "AcquireTime";
	@SuppressWarnings("unused") // See getExposureTime()
	private static final String EXPOSURE_TIME_RBV = "AcquireTime_RBV";
	/** Number of repeats to be summed in SES (Mirroring a standard AD PV) */
	private static final String ITERATIONS = "NumExposures";
	private static final String ITERATIONS_RBV = "NumExposures_RBV";
	/** CURRENT_ITERATION_COUNT refers to the current number in the running iteration sequence */
	private static final String CURRENT_ITERATION_COUNT = "NumExposuresCounter_RBV";
	/** ITERATIONS_LAST_RBV refers to the number of iterations that were actually completed in the previously run sequence */
	private static final String ITERATIONS_LAST_RBV = "NEXPOSURES_LAST_RBV";
	// This PV shoudn't be used set the EXPOSURE_TIME instead. I09-13
	// private static final String STEP_TIME = "STEP_TIME";

	// Sensor size and ROI PVs
	private static final String SENSOR_SIZE_X = "MaxSizeX_RBV";
	private static final String SENSOR_SIZE_Y = "MaxSizeY_RBV";
	private static final String ROI_START_X = "MinX";
	private static final String ROI_START_X_RBV = "Min_RBV";
	private static final String ROI_START_Y = "MinY";
	private static final String ROI_START_Y_RBV = "MinY_RBV";
	private static final String ROI_SIZE_X = "SizeX";
	private static final String ROI_SIZE_X_RBV = "SizeX_RBV";
	private static final String ROI_SIZE_Y = "SizeY";
	private static final String ROI_SIZE_Y_RBV = "SizeY_RBV";

	/** Slices define the number of y channels, can be used to reduce the data */
	private static final String SLICES = "SLICES";
	private static final String SLICES_RBV = "SLICES_RBV";

	//Data size PVs.
	/** Linked by TOTAL_DATA_POINTS_RBV = TOTAL_LEAD_POINTS_RBV + TOTAL_POINTS_ITERATION_RBV
	 * For fixed mode TOTAL_LEAD_POINTS_RBV = 0 as there is no pre scan.
	 */
	private static final String TOTAL_DATA_POINTS_RBV = "TOTAL_DATA_POINTS_RBV";
	// Number of points in the pre scan only for swept mode
	private static final String TOTAL_LEAD_POINTS_RBV = "TOTAL_LEAD_POINTS_RBV";
	private static final String TOTAL_POINTS_ITERATION_RBV = "TOTAL_POINTS_ITERATION_RBV";
	private static final String TOTAL_POINTS_RBV = "TOTAL_POINTS_RBV";


	// Data scales and units
	/** The energy scale is in KE eV */
	private static final String ENERGY_SCALE_RBV = "X_SCALE_RBV";
	private static final String ENERGY_UNITS_RBV = "X_UNITS_RBV";
	private static final String ENERGY_COUNT_RBV = "X_COUNT_RBV";
	// Y scale is the angle or position in deg or mm
	private static final String Y_SCALE_RBV = "Y_SCALE_RBV";
	private static final String Y_UNITS_RBV = "Y_UNITS_RBV";
	private static final String Y_COUNT_RBV = "Y_COUNT_RBV";
	// Intensity unit is the value of the image e.g. counts/sec
	private static final String INTENSITY_UNITS_RBV = "I_UNITS_RBV";

	//Data PVs.
	/** Image is the full 2D data the size will be TOTAL_DATA_POINTS_RBV * SLICES_RBV */
	private static final String IMAGE_DATA = "IMAGE";
	/**
	 * IMAGE_LAST is the image collected from the last completed iteration as opposed
	 * to IMAGE which is the current data
	 */
	private static final String IMAGE_LAST = "IMAGE_LAST";
	// Spectrum is the integrated energy spectrum (sum of all y channels) the size is TOTAL_DATA_POINTS_RBV
	private static final String SPECTRUM_DATA = "INT_SPECTRUM";
	// External IO allows data to be collected by SES synchronised with the analyser acquisition
	private static final String EXTERNAL_IO_DATA = "EXTIO";

	//Special function PVs.
	/** Setting ZERO_SUPPLIES=1 causes the HV to be switched off */
	private static final String ZERO_SUPPLIES = "ZERO_SUPPLIES";

	/** If STOP_NEXT_ITERATION is set to 1, the scan will abort after the current iteration is complete */
	private static final String STOP_NEXT_ITERATION = "STOP_NEXT_ITERATION";


	// Lists for holding valid values of the enum PVs
	private final List<String> passEnergies = new ArrayList<>();
	private final List<String> lensModes = new ArrayList<>();
	private final List<String> psuModes = new ArrayList<>();
	private final List<String> detectorModes = new ArrayList<>();
	private final List<String> acquisitionModes = new ArrayList<>();

	/** The camera frame rate in SES */
	private double cameraFrameTime = -1;
	/**
	 *  This is used to cache the excitation energy inside this class as it is only useful to EPICS in binding energy mode,
	 *  which is never used.
	 */
	private double excitationEnergy;


	/** The X size of the sensor as defined in SES (retrieved from EPICS) */
	private int sensorXSize;
	/** The Y size of the sensor as defined in SES (retrieved from EPICS) */
	private int sensorYSize;

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	private Channel getChannel(String pvPostFix) throws CAException, TimeoutException {
		String fullPvName = basePVName + pvPostFix;
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (basePVName == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new FactoryException("Configure called with no basePVName. Check spring configuration!");
		}
		logger.info("Configuring analyser with base PV: {}", basePVName);

		try {
			// Inspect the analyser for the available options
			initaliseEnumChannel(PSU_MODE, psuModes);
			logger.debug("Avaliable PSU modes: {}", this.psuModes);
			initaliseEnumChannel(LENS_MODE, lensModes);
			logger.debug("Avaliable lens modes: {}", this.lensModes);
			initaliseEnumChannel(PASS_ENERGY, passEnergies);
			logger.debug("Avaliable pass energies: {}", this.passEnergies);
			initaliseEnumChannel(DETECTOR_MODE, detectorModes);
			logger.debug("Avaliable detector modes: {}", this.detectorModes);
			initaliseEnumChannel(ACQUISITION_MODE, acquisitionModes);
			logger.debug("Avaliable acquisition modes: {}", this.acquisitionModes);


			// Detect the SES camera frame rate for validation and predicting timings
			int cameraFrameRate = getCameraFrameRate();
			// Calculate the frame time
			cameraFrameTime = 1.0 / cameraFrameRate;
			logger.debug("Detected SES camera frame rate as: {} fps, and frame time: {} sec", cameraFrameRate, cameraFrameTime);

			sensorXSize = getSensorXSize();
			sensorYSize = getSensorYSize();
			logger.debug("Detected SES sensor size as: {} x {} (Energy x Y)", sensorXSize, sensorYSize);

		} catch (Exception e) {
			throw new FactoryException("Configuring the analyser failed", e);
		}

		logger.info("Finished configuring analyser");
		setConfigured(true);
	}

	/**
	 * @return The X size of the sensor as set in SES
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getSensorXSize() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SENSOR_SIZE_X));
	}

	/**
	 * @return The Y size of the sensor as set in SES
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getSensorYSize() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SENSOR_SIZE_Y));
	}

	/**
	 * This inspects a enum channel for its values and adds them to a list. It will warn if the channel contains empty values and these will not be added to the
	 * list. It will clear the existing values in the list.
	 *
	 * @param channel The channel to inspect
	 * @param list The list to contain valid positions
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	private void initaliseEnumChannel(String channel, List<String> list) throws Exception {
		String[] positionLabels = null;
		positionLabels = EPICS_CONTROLLER.cagetLabels(getChannel(channel));
		if (positionLabels == null || positionLabels.length == 0) {
			throw new DeviceException("Error getting lables from enum channel: " + basePVName + channel);
		}
		// Clear the list here this allows for rerunning configure
		list.clear();
		// Add the positions to the list
		for (String position : positionLabels) {
			if (position == null || position.isEmpty()) {
				logger.warn("Enum channel {} contains empty entries", basePVName + channel);
			} else {
				list.add(position);
			}
		}
	}

	/**
	 * Sets the current lens mode.
	 *
	 * @param value The required lens mode.
	 * @throws IllegalArgumentException If the lens mode requested is invalid
	 * @throws Exception If there is a problem with the EPICS communication
	 * @see #getLensModes()
	 */
	public void setLensMode(String value) throws Exception {
		if (!lensModes.contains(value)) {
			throw new DeviceException("The lens mode requested: " + value + " is not valid");
		}
		// It is valid so set it
		EPICS_CONTROLLER.caputWait(getChannel(LENS_MODE), value);
		logger.debug("Lens Mode set to: {}", value);
	}

	/**
	 * Gets the current lens mode
	 *
	 * @return The current lens mode
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public String getLensMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(LENS_MODE_RBV));
	}

	/**
	 * Gets the current PSU mode. (Also known as element set)
	 *
	 * @return The current power supply mode
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public String getPsuMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(PSU_MODE_RBV));
	}

	/**
	 * Sets the PSU mode (also known as element set) it must be set to match the physical wiring of the power supplies. This is to allow different
	 * kinetic energy ranges to be reached.
	 *
	 * @param value The required PSU mode. <b>Must match the physical hardware wiring!</b>
	 * @throws IllegalArgumentException If the PSU mode requested is invalid
	 * @throws Exception If there is a problem with the EPICS communication
	 * @see #getPsuModes()
	 */
	public void setPsuMode(String value) throws Exception {
		// Check if the PSU mode is valid
		if (!psuModes.contains(value)) {
			throw new IllegalArgumentException("The PSU mode requested: " + value + " is not valid");
		}
		// It is valid so set it.
		EPICS_CONTROLLER.caputWait(getChannel(PSU_MODE), value);
		logger.debug("PSU Mode set to: {}", value);
	}

	/**
	 * Sets the acquisition mode. Acquisition modes provide different ways of operating the detector, which might be better in different count rate regimes.
	 *
	 * @param value The required acquisition mode.
	 * @throws IllegalArgumentException If the acquisition mode requested is invalid
	 * @throws Exception If there is a problem with the EPICS communication
	 * @see #getAcquisitionModes()
	 */
	public void setAcquisitionMode(String value) throws Exception {
		// Check if the acquisition mode is valid
		if (!acquisitionModes.contains(value)) {
			throw new DeviceException("The acquisition mode requested: " + value + " is not valid");
		}
		// It is valid so set it.
		EPICS_CONTROLLER.caputWait(getChannel(ACQUISITION_MODE), value);
		logger.debug("Acquisition Mode set to: {}", value);
	}

	/**
	 * Gets the acquisition mode. Acquisition modes provide different ways of operating the detector, which might be better in different count rate regimes.
	 *
	 * @return The current acquisition mode
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public String getAcquisitionMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ACQUISITION_MODE_RBV));
	}

	/**
	 * Acquisition modes provide different ways of operating the detector. This returns a copy of the list of available modes.
	 *
	 * @return List of the available acquisition modes
	 */
	public List<String> getAcquisitionModes() {
		return new ArrayList<>(acquisitionModes);
	}

	/**
	 * Sets the pass energy to the requested value. It will check if the pass energy is available in EPICS. This does <b>not</b> check if the pass energy is
	 * available with the current PSU mode
	 *
	 * @param value The requested pass energy
	 * @throws Exception If there is a problem with the EPICS communication
	 * @throws IllegalArgumentException If requested pass energy is not available in EPICS
	 * @see #getPassEnergies()
	 */
	public void setPassEnergy(int value) throws Exception {
		// Convert to string as the PE internally is handled as a string
		String valueString = Integer.valueOf(value).toString();
		// Check if the pass energy is valid
		if (!passEnergies.contains(valueString)) {
			throw new DeviceException("The pass energy requested: " + value + " is not valid");
		}
		// It is valid so set it. As string to avoid enum position conversion
		EPICS_CONTROLLER.caputWait(getChannel(PASS_ENERGY), valueString);
		logger.debug("Pass Energy set to: {}", valueString);
	}

	/**
	 * Gets the currently set pass energy.
	 *
	 * @return The current pass energy
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getPassEnergy() throws Exception {
		// Get as string to avoid enum position conversion
		return Integer.parseInt(EPICS_CONTROLLER.cagetString(getChannel(PASS_ENERGY_RBV)));
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

	public void setStartEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Start energy must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(START_ENERGY), value);
		logger.debug("Start Energy set to: {}", value);
	}

	public double getStartEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(START_ENERGY_RBV));
	}

	public void setCentreEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Centre energy must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(CENTRE_ENERGY), value);
		logger.debug("Centre Energy set to: {}", value);
	}

	public double getCentreEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(CENTRE_ENERGY_RBV));
	}

	public void setEndEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("End energy must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(END_ENERGY), value);
		logger.debug("End Energy set to: {}", value);
	}

	public double getEndEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(END_ENERGY_RBV));
	}

	public void setEnergyStep(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Energy step must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(ENERGY_STEP), value);
		logger.debug("Energy Step set to: {}", value);
	}

	public double getEnergyStep() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ENERGY_STEP_RBV));
	}

	public void setFrames(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("Frames must be greater than or equal to 1");
		}
		EPICS_CONTROLLER.caputWait(getChannel(FRAMES), value);
		logger.debug("Number of Frames set to: {}", value);
	}

	public int getFrames() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAMES_RBV));
	}

	/**
	 * Sets the number of slices for the Y axis
	 * @param value Number of slices
	 * @throws IllegalArgumentException If an invalid number of slices is requested
	 */
	public void setSlices(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("Slices must be greater than or equal to 1");
		}
		if (value > getRoiYSize()) {
			throw new IllegalArgumentException(String.format("Slices must be less than or equal to ROI Y size %d", getRoiYSize()));
		}
		EPICS_CONTROLLER.caputWait(getChannel(SLICES), value);
		logger.debug("Number of Slices set to: {}", value);
	}

	public int getSlice() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SLICES_RBV));
	}

	/**
	 * Zeroing the supplies sets all of the voltages to zero. This is to leave the detector in a safe state. It also causes any currently running acquisition to
	 * be aborted.
	 *
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public void zeroSupplies() throws Exception {
		logger.debug("zeroSupplies called. Attempting to zero the voltages");
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ZERO_SUPPLIES), 1);
			logger.info("Zeroed supplies");
		} catch (Exception e) {
			logger.error("Error while zeroing voltages. They may not be zeroed!", e);
			throw e;
		}
	}

	/**
	 * Stopping the scan after next iteration allows the scan to continue until the next iteration is complete, at which point it will
	 * be aborted.
	 *
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public void stopAfterCurrentIteration() throws Exception {
		logger.debug("stopAfterCurrentIteration called. Attempting to stop the scan!");
		try {
			EPICS_CONTROLLER.caputWait(getChannel(STOP_NEXT_ITERATION), 1);
			logger.info("Asked EPICS to stop after current iteration");
		} catch (Exception e) {
			logger.error("Error whist trying to stop after current iteration!", e);
			throw e;
		}
	}

	public double[] getEnergyAxis() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ENERGY_SCALE_RBV), getEnergyChannels());
	}

	public double[] getYAxis() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(Y_SCALE_RBV), getYCount());
	}

	public String getEnergyUnits() throws Exception {
		return parseUnitsString(EPICS_CONTROLLER.cagetString(getChannel(ENERGY_UNITS_RBV)));
	}

	public String getYUnits() throws Exception {
		return parseUnitsString(EPICS_CONTROLLER.cagetString(getChannel(Y_UNITS_RBV)));
	}

	public String getIntensityUnits() throws Exception {
		return parseUnitsString(EPICS_CONTROLLER.cagetString(getChannel(INTENSITY_UNITS_RBV)));
	}

	/**
	 * The units strings returned from EPICS are of the form "Y-Scale [mm]" were the actual units component, "mm", is enclosed in square brackets. This method extracts
	 * the unit and returns it.
	 *
	 * @param fullString As returned from EPICS e.g. "Y-Scale [mm]"
	 * @return The extracted units e.g. "mm"
	 */
	private String parseUnitsString(String fullString) {
		return fullString.substring(fullString.indexOf('[') + 1, fullString.indexOf(']'));
	}

	public int getEnergyChannels() throws Exception {
		return getTotalDataPoints();
	}

	public int getTotalSteps() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTAL_POINTS_RBV));
	}

	public int getTotalLeadPoints() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTAL_LEAD_POINTS_RBV));
	}

	public int getTotalDataPoints() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ENERGY_COUNT_RBV));
	}

	public int getYCount() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(Y_COUNT_RBV));
	}

	public void setDetectorMode(String value) throws Exception {
		if (!detectorModes.contains(value)) {
			throw new DeviceException("The detector mode requested: " + value + " is not valid");
		}
		EPICS_CONTROLLER.caputWait(getChannel(DETECTOR_MODE), value);
		logger.debug("Detector Mode set to: {}", value);
	}

	public String getDetectorMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(DETECTOR_MODE_RBV));
	}

	public double[] getSpectrum() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(SPECTRUM_DATA), getEnergyChannels());
	}

	/**
	 * This calls {@link #getImage(int)} but is more convenient as it handles the size for you. If performance is critical it would be better to reduce the
	 * number of requests to EPICS to do this you can call {@link #getImage(int)} directly if you already know the size required.
	 *
	 * @return The image data as a 1D array
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public double[] getImage() throws Exception {
		return getImage(getEnergyChannels() * getSlice());
	}

	public double[] getExtIO() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(EXTERNAL_IO_DATA));
	}

	public double[] getSpectrum(int i) throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(SPECTRUM_DATA), i);
	}


	/**
	 * This gets the image as a double. You need to request the correct number of elements, if you don't know call {@link #getImage()} instead as this will be
	 * handled automatically.
	 *
	 * @param i The number of array elements requested. Should be the the image size
	 * @return The image data as a 1D array
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public double[] getImage(int i) throws Exception {
		long startTime = System.nanoTime();
		double[] data = EPICS_CONTROLLER.cagetDoubleArray(getChannel(IMAGE_DATA), i);
		long endTime = System.nanoTime();
		logger.trace("Getting image, {} double values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	/**
	 * This gets the image as a float, this maybe useful as type conversion is performed server side by EPICS so the data transfered is halved this can help
	 * improve performance (at the loss of precision). You need to request the correct number of elements.
	 *
	 * @param i The number of array elements requested. Should be the the image size
	 * @return The image data as a 1D array
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public float[] getImageAsFloat(int i) throws Exception {
		long startTime = System.nanoTime();
		float[] data = EPICS_CONTROLLER.cagetFloatArray(getChannel(IMAGE_DATA), i);
		long endTime = System.nanoTime();
		logger.trace("Getting image, {} float values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	/**
	 * This gets the image from the last completed scan as a float, this image is the one recorded after the most recent
	 * iteration has been completed as opposed to a current, possibly partial, image .
	 * Getting data as a float maybe useful as type conversion is performed server side by EPICS so the data transfered is
	 * halved this can help improve performance (at the loss of precision). You need to request the correct number of elements.
	 *
	 * @param i The number of array elements requested. Should be the the image size
	 * @return The image data as a 1D array
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public float[] getLastImageAsFloat(int i) throws Exception {
		long startTime = System.nanoTime();
		float[] data = EPICS_CONTROLLER.cagetFloatArray(getChannel(IMAGE_LAST), i);
		long endTime = System.nanoTime();
		logger.trace("Getting image, {} float values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	public double[] getExtIO(int i) throws Exception {
		long startTime = System.nanoTime();
		double[] data = EPICS_CONTROLLER.cagetDoubleArray(getChannel(EXTERNAL_IO_DATA), i);
		long endTime = System.nanoTime();
		logger.trace("Getting external IO, {} double values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	/**
	 * This returns a copy of the list of the available lens modes.
	 *
	 * @return List of the available lens modes
	 */
	public List<String> getLensModes() {
		return new ArrayList<>(lensModes);
	}

	/**
	 * This returns a copy of the list of the available power supply modes as a Set.
	 * These are only switchable by physical hardware rewiring, and are to allow different
	 * kinetic energy ranges to be reached.
	 *
	 * @return List of the available power supply modes
	 */
	public Set<String> getPsuModes() {
		return new LinkedHashSet<>(psuModes);
	}

	/**
	 * This returns a copy of the list of the available detector modes
	 *
	 * @return List of the available detector modes
	 */
	public List<String> getDetectorModes() {
		return new ArrayList<>(detectorModes);
	}

	/**
	 * Sets the cached excitation energy value in eV The excitation energy is used to convert between binding energy and kinetic energy. As only kinetic energy
	 * mode is used in EPICS, there is no reason to set it so it is cached here to maintain compatibility.
	 *
	 * @see #setExcitationEnergy(double)
	 * @deprecated There shouldn't be a need to set the excitation energy at the EPICS level.
	 */
	@Deprecated(since="GDA 8.58")
	public void setExcitationEnergy(double value) throws IllegalArgumentException {
		logger.deprecatedMethod("setExcitationEnergy(double)");
		if (value < 0) {
			throw new IllegalArgumentException("Excitation energy must be greater than or equal to 0");
		}
		excitationEnergy = value;
	}

	/**
	 * The excitation energy is used to convert between binding energy and kinetic energy.
	 *
	 * @return The cached excitation energy in eV
	 * @see #setExcitationEnergy(double)
	 * @deprecated There shouldn't be a need to set the excitation energy at the EPICS level.
	 */
	@Deprecated(since="GDA 8.58")
	public double getExcitationEnergy() {
		logger.deprecatedMethod("getExcitationEnergy()");
		return excitationEnergy;
	}

	/**
	 * Gets the currently set exposure time in seconds. Will always be a multiple of the frame time {@link #getCameraFrameTime()} as SES uses a fixed camera frame rate.
	 *
	 * @return The exposure time in seconds
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public double getExposureTime() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(EXPOSURE_TIME_RBV));
	}

	/**
	 * Sets the exposure time. On the VG Scienta analysers the actual camera is always operated at a fixed frame rate (see {@link #getCameraFrameRate()}),
	 * therefore the possible exposure times are actually multiples of that frame time. When you request an exposure it will be converted to the closest
	 * possible number of frames and the actual exposure taken will be frames * camera frame time (see {@link #getCameraFrameTime()})
	 *
	 * @param value The requested exposure time in seconds
	 * @throws IllegalArgumentException If the requested exposure is below one frame time.
	 * @throws Exception If there is a problem with the EPICS communication
	 * @see #getCameraFrameTime()
	 */
	public void setExposureTime(double value) throws Exception {
		if (value < cameraFrameTime) {
			throw new IllegalArgumentException("Exposure time must be greater than or equal to " + cameraFrameTime);
		}
		EPICS_CONTROLLER.caputWait(getChannel(EXPOSURE_TIME), value);
		logger.debug("Exposure Time set to: {}", value);
	}

	/**
	 * This returns the fixed camera frame rate SES is using for the camera.
	 *
	 * @return the SES camera frame rate
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getCameraFrameRate() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(CAMERA_FRAME_RATE));
	}

	/**
	 * This returns the camera exposure time SES is using for the camera.
	 *
	 * @return the SES camera frame rate
	 */
	public double getCameraFrameTime() {
		return cameraFrameTime;
	}

	/**
	 * Gets the currently set number of iterations.
	 *
	 * @return The number of iterations
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getIterations() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ITERATIONS_RBV));
	}

	/**
	 * Gets the number of completed iterations
	 * from the most recent scan.
	 *
	 * @return The number of complete iterations from previous scan
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getCompletedIterations() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ITERATIONS_LAST_RBV));
	}

	/**
	 * Gets the current iteration number in the scan
	 *
	 * @return The the current iteration number in the scan
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getCurrentIterations() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(CURRENT_ITERATION_COUNT));
	}

	/**
	 * Sets the number of iterations to be performed. SES handles the summing.
	 *
	 * @param value Number of iterations to run
	 * @throws Exception If there is a problem with the EPICS communication
	 * @throws IllegalArgumentException If value is <1
	 */
	public void setIterations(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("Iterations must be greater than or equal to 1");
		}
		EPICS_CONTROLLER.caputWait(getChannel(ITERATIONS), value);
		logger.debug("Number of Iterations set to: {}", value);
	}

	/**
	 * Sets the X start position of the region of interest.
	 * @param value X axis start position for the ROI
	 * @throws IllegalArgumentException If the position requested is invalid
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	private void setRoiXStart(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("ROI X start position must be at least 1");
		}
		if (value > sensorXSize) {
			throw new IllegalArgumentException(
					String.format("ROI X start position must be less than X size of sensor: %d", sensorXSize));
		}
		EPICS_CONTROLLER.caputWait(getChannel(ROI_START_X), value);
	}

	/**
	 * Sets the Y start position of the region of interest.
	 * @param value Y axis start position for the ROI
	 * @throws IllegalArgumentException If the position requested is invalid
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	private void setRoiYStart(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("ROI Y start position must be at least 1");
		}
		if (value > sensorYSize) {
			throw new IllegalArgumentException(
					String.format("ROI Y start position must be less than Y size of sensor: %d", sensorYSize));
		}
		EPICS_CONTROLLER.caputWait(getChannel(ROI_START_Y), value);
	}

	/**
	 * Validates and then sets the X size of the region of interest.
	 * @param value X axis size of the ROI
	 * @throws IllegalArgumentException If the ROI size is larger than the sensor size
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	private void setRoiXSize(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("ROI X size must be at least 1");
		}
		if (value > sensorXSize) {
			throw new IllegalArgumentException("ROI X size cannot be larger than sensor X size");
		}
		EPICS_CONTROLLER.caputWait(getChannel(ROI_SIZE_X), value);
	}

	/**
	 * Retrieves the X size of the ROI from EPICS.
	 * @return The X size of the ROI
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getRoiXSize() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ROI_SIZE_X_RBV));
	}

	/**
	 * Validates and then sets the Y size of the region of interest.
	 * @param value Y axis size of the ROI
	 * @throws IllegalArgumentException If the ROI size is larger than the sensor size
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	private void setRoiYSize(int value) throws Exception {
		if (value > sensorYSize) {
			throw new IllegalArgumentException("ROI Y cannot be larger than Y sensor size");
		}
		EPICS_CONTROLLER.caputWait(getChannel(ROI_SIZE_Y), value);
	}

	/**
	 * Retrieves the Y size of the ROI from EPICS.
	 * @return The Y size of the ROI
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public int getRoiYSize() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ROI_SIZE_Y_RBV));
	}

	/**
	 * Configures the detector using a DetectorConfiguration object with validation of parameters.
	 * @param configuration DetectorConfiguration object.
	 * @throws IllegalArgumentException if the supplied configuration if not possible.
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public void setDetectorConfiguration(DetectorConfiguration configuration) throws Exception{
		logger.debug("setDetectorConfiguration called with configuration {}", configuration);
		logger.debug("Currently Sensor Size X is {} and Sensor Size Y is {}", sensorXSize, sensorYSize);

		// Validate configuration
		if (configuration.getSizeX() > sensorXSize) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI X size %d is bigger than detector X size %d",
					configuration.getSizeX(), sensorXSize));
		}
		if (configuration.getSizeY() > sensorYSize) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI Y size %d is bigger than detector Y size %d",
					configuration.getSizeY(), sensorYSize));
		}

		if(configuration.getStartX() + configuration.getSizeX() - 1 > sensorXSize) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI X stop %d is larger than detector X size %d",
					configuration.getStartX() + configuration.getSizeX() - 1, sensorXSize));
		}

		if(configuration.getStartY() + configuration.getSizeY() - 1 > sensorYSize) {
			throw new IllegalArgumentException(String.format("Configuration error: ROI Y stop %d is larger than detector Y size %d",
					configuration.getStartY() + configuration.getSizeY() - 1, sensorYSize));
		}

		if (configuration.getSlices() < 1) {
			throw new IllegalArgumentException("There must be at least 1 slice!");
		}

		if (configuration.getSlices() > configuration.getSizeY()) {
			throw new IllegalArgumentException(String.format("Number of slices (%d) must be less than or equal to Y size (%d)",
					configuration.getSlices(), configuration.getSizeY()));
		}
		// Looks possible lets set it up
		setRoiXStart(configuration.getStartX());
		setRoiYStart(configuration.getStartY());
		setRoiXSize(configuration.getSizeX());
		setRoiYSize(configuration.getSizeY());
		setSlices(configuration.getSlices());
		logger.info("Set detector ROI to: {}", configuration);
	}
}