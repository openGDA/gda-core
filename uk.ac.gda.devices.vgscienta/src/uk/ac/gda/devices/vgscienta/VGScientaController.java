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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

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
public class VGScientaController implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaController.class);

	// Values internal to the object for Channel Access
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName = null;
	// Map that stores the channel against the PV name
	private Map<String, Channel> channelMap = new HashMap<>();

	// PSU mode defines the current setup of the hardware. This should only be changed if the power supplies are physically rewired!
	// VG Scienta refer to this as element set (its the set of power supply elements used but PSU mode is clearer and more general so its used here)
	private static final String PSU_MODE = "ELEMENT_SET";
	private static final String PSU_MODE_RBV = "ELEMENT_SET_RBV";

	// Lens mode
	private static final String LENS_MODE = "LENS_MODE";
	private static final String LENS_MODE_RBV = "LENS_MODE_RBV";
	// Acquisition mode is fixed or swept
	private static final String ACQUISITION_MODE = "ACQ_MODE";
	private static final String ACQUISITION_MODE_RBV = "ACQ_MODE_RBV";

	// Only Kinetic energy mode is supported, this is set during configure
	private static final String ENERGY_MODE = "ENERGY_MODE";
	private static final String ENERGY_MODE_RBV = "ENERGY_MODE_RBV";
	// To allow conversion to binding energy the analyser needs to know the excitation energy
	// This shouldn't be used as binding energy mode is not supported but is here for legacy support
	// private static final String EXCITATION_ENERGY = "EXCITATION_ENERGY";
	// private static final String EXCITATION_ENERGY_RBV = "EXCITATION_ENERGY_RBV";

	// This can be used to switch the detector between ADC and Pulse Counting modes (ADC is typical)
	private static final String DETECTOR_MODE = "DETECTOR_MODE";
	private static final String DETECTOR_MODE_RBV = "DETECTOR_MODE_RBV";

	// Scan energy settings
	// The pass energy can be seen as a energy resolution setting. Small pass energy -> high energy resolution
	private static final String PASS_ENERGY = "PASS_ENERGY";
	private static final String PASS_ENERGY_RBV = "PASS_ENERGY_RBV";
	// Centre energy is used in fixed mode to define the centre of the KE range
	private static final String CENTRE_ENERGY = "CENTRE_ENERGY";
	private static final String CENTRE_ENERGY_RBV = "CENTRE_ENERGY_RBV";
	// In swept mode the energy range is defined by start, stop and step
	private static final String START_ENERGY = "LOW_ENERGY";
	private static final String START_ENERGY_RBV = "LOW_ENERGY_RBV";
	private static final String END_ENERGY = "HIGH_ENERGY";
	private static final String END_ENERGY_RBV = "HIGH_ENERGY_RBV";
	// Energy step is only applicable in swept mode, in fixed the energy step is a function of pass energy.
	private static final String ENERGY_STEP = "STEP_SIZE";
	private static final String ENERGY_STEP_RBV = "STEP_SIZE_RBV";

	// Scan exposure settings
	// Note frames and exposure time are linked by the fixed camera frame rate
	private static final String FRAMES = "FRAMES";
	private static final String FRAMES_RBV = "FRAMES_RBV";
	// The fixed camera frame rate SES is using
	private static final String CAMERA_FRAME_RATE = "MAX_FRAMES_RBV";
	// Exposure time requested. SES will give the closest number of frames available (Mirroring a standard AD PV)
	private static final String EXPOSURE_TIME = "AcquireTime";
	@SuppressWarnings("unused") // See getExposureTime()
	private static final String EXPOSURE_TIME_RBV = "AcquireTime_RBV";
	// Number of repeats to be summed in SES (Mirroring a standard AD PV)
	private static final String ITERATIONS = "NumExposures";
	private static final String ITERATIONS_RBV = "NumExposures_RBV";
	// This PV shoudn't be used set the EXPOSURE_TIME instead. I09-13
	// private static final String STEP_TIME = "STEP_TIME";

	// Slices define the number of y channels, can be used to reduce the data
	private static final String SLICES = "SLICES";
	private static final String SLICES_RBV = "SLICES_RBV";

	// Data size PVs. Linked by TOTAL_DATA_POINTS_RBV = TOTAL_LEAD_POINTS_RBV + TOTAL_POINTS_ITERATION_RBV
	// For fixed mode TOTAL_LEAD_POINTS_RBV = 0 as there is no pre scan.
	private static final String TOTAL_DATA_POINTS_RBV = "TOTAL_DATA_POINTS_RBV";
	// Number of points in the pre scan only for swept mode
	private static final String TOTAL_LEAD_POINTS_RBV = "TOTAL_LEAD_POINTS_RBV";
	private static final String TOTAL_POINTS_ITERATION_RBV = "TOTAL_POINTS_ITERATION_RBV";
	private static final String TOTAL_POINTS_RBV = "TOTAL_POINTS_RBV";

	// Data scales and units
	// The energy scale is in KE eV
	private static final String ENERGY_SCALE_RBV = "X_SCALE_RBV";
	private static final String ENERGY_UNITS_RBV = "X_UNITS_RBV";
	// Y scale is the angle or position in deg or mm
	private static final String Y_SCALE_RBV = "Y_SCALE_RBV";
	private static final String Y_UNITS_RBV = "Y_UNITS_RBV";
	// Intensity unit is the value of the image e.g. counts/sec
	private static final String INTENSITY_UNITS_RBV = "I_UNITS_RBV";

	// Data PVs
	// Image is the full 2D data the size will be TOTAL_DATA_POINTS_RBV * SLICES_RBV
	private static final String IMAGE_DATA = "IMAGE";
	// Spectrum is the integrated energy spectrum (sum of all y channels) the size is TOTAL_DATA_POINTS_RBV
	private static final String SPECTRUM_DATA = "INT_SPECTRUM";
	// External IO allows data to be collected by SES synchronised with the analyser acquisition
	private static final String EXTERNAL_IO_DATA = "EXTIO";

	// Special function PVs
	// Setting ZERO_SUPPLIES=1 causes the HV to be switched off
	private static final String ZERO_SUPPLIES = "ZERO_SUPPLIES";

	// Lists for holding valid values of the enum PVs
	private final List<String> passEnergies = new ArrayList<>();
	private final List<String> lensModes = new ArrayList<>();
	private final List<String> psuModes = new ArrayList<>();
	private final List<String> detectorModes = new ArrayList<>();
	private final List<String> acquisitionModes = new ArrayList<>();

	// The camera frame rate in SES
	private double cameraFrameTime = -1;
	// This is used to cache the excitation energy inside this class as it is only useful to EPICS in binding energy mode, which is never used.
	private double excitationEnergy;

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
	public void configure() {
		if (basePVName == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new IllegalStateException("Configure called with no basePVName. Check spring configuration!");
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

			// Set energy mode to kinetic as this is the only supported option
			setEnergyMode("Kinetic");

			// Detect the SES camera frame rate for validation and predicting timings
			int cameraFrameRate = getCameraFrameRate();
			// Calculate the frame time
			cameraFrameTime = 1.0 / cameraFrameRate;
			logger.debug("Detected SES camera frame rate as: {} fps, and frame time: {} sec", cameraFrameRate, cameraFrameTime);

		} catch (Exception e) {
			logger.error("Configuring the analyser failed", e);
		}

		logger.info("Finished configuring analyser");
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
	}

	public double getStartEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(START_ENERGY_RBV));
	}

	public void setCentreEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Centre energy must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(CENTRE_ENERGY), value);
	}

	public double getCentreEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(CENTRE_ENERGY_RBV));
	}

	public void setEndEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("End energy must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(END_ENERGY), value);
	}

	public double getEndEnergy() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(END_ENERGY_RBV));
	}

	public void setEnergyStep(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Energy step must be greater than 0");
		}
		EPICS_CONTROLLER.caputWait(getChannel(ENERGY_STEP), value);
	}

	public double getEnergyStep() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ENERGY_STEP_RBV));
	}

	public void setFrames(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("Frames must be greater than or equal to 1");
		}
		EPICS_CONTROLLER.caputWait(getChannel(FRAMES), value);
	}

	public int getFrames() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAMES_RBV));
	}

	public void setSlices(int value) throws Exception {
		// TODO This should really check if slices < y region size but need to implement region handling here
		if (value < 1) {
			throw new IllegalArgumentException("Slices must be greater than or equal to 1");
		}
		EPICS_CONTROLLER.caputWait(getChannel(SLICES), value);
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

	public double[] getEnergyAxis() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ENERGY_SCALE_RBV), getEnergyChannels());
	}

	public double[] getYAxis() throws Exception {
		return EPICS_CONTROLLER.cagetDoubleArray(getChannel(Y_SCALE_RBV), getSlice());
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
		return EPICS_CONTROLLER.cagetInt(getChannel(TOTAL_DATA_POINTS_RBV));
	}

	/**
	 * This method is remains for backwards compatibility it will be changed to private in future. This is to ensure the analyser is always operated in kinetic
	 * energy mode.
	 *
	 * @deprecated This shouldn't be called as the analyser is always operated in kinetic energy mode.
	 * @param value Only "Kinetic" (not case sensitive)  is valid
	 * @throws IllegalArgumentException If any argument other than "Kinetic" is passed in
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	@Deprecated
	public void setEnergyMode(String value) throws Exception {
		if (!value.equalsIgnoreCase("Kinetic")) {
			throw new IllegalArgumentException("Only kinetic energy mode is supported");
		}
		EPICS_CONTROLLER.caputWait(getChannel(ENERGY_MODE), value);
	}

	public String getEnergyMode() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(ENERGY_MODE_RBV));
	}

	public void setDetectorMode(String value) throws Exception {
		if (!detectorModes.contains(value)) {
			throw new DeviceException("The detector mode requested: " + value + " is not valid");
		}
		EPICS_CONTROLLER.caputWait(getChannel(DETECTOR_MODE), value);
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

	public double[] getExtIO(int i) throws Exception {
		long startTime = System.nanoTime();
		double[] data = EPICS_CONTROLLER.cagetDoubleArray(getChannel(EXTERNAL_IO_DATA), i);
		long endTime = System.nanoTime();
		logger.trace("Getting external IO, {} double values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	/**
	 * @deprecated Use {@link #setPsuMode(String)}
	 * @param value The required element set
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	@Deprecated
	public void setElement(String value) throws Exception {
		EPICS_CONTROLLER.caputWait(getChannel(PSU_MODE), value);
	}

	/**
	 * @deprecated Use {@link #getPsuMode()} instead
	 * @return The current power supply mode
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	@Deprecated
	public String getElement() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(PSU_MODE_RBV));
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
	 * This returns a copy of the list of the available power supply modes. These are only switchable by physical hardware rewiring, and are to allow different
	 * kinetic energy ranges to be reached.
	 *
	 * @return List of the available power supply modes
	 */
	public List<String> getPsuModes() {
		return new ArrayList<>(psuModes);
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
	 * @deprecated This is deprecated use {@link #getPsuModes()} instead
	 * @return The available element sets
	 */
	@Deprecated
	public String[] getElementset() {
		return psuModes.toArray(new String[0]);
	}

	/**
	 * Sets the cached excitation energy value in eV The excitation energy is used to convert between binding energy and kinetic energy. As only kinetic energy
	 * mode is used in EPICS, there is no reason to set it so it is cached here to maintain compatibility.
	 *
	 * @see #setExcitationEnergy(double)
	 * @deprecated There shouldn't be a need to set the excitation energy at the EPICS level.
	 */
	@Deprecated
	public void setExcitationEnergy(double value) throws IllegalArgumentException {
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
	@Deprecated
	public double getExcitationEnergy() {
		return excitationEnergy;
	}

	/**
	 * Gets the currently set exposure time in seconds. Will always be a multiple of the frame time {@link #getCameraFrameTime()} as SES uses a fixed camera frame rate.
	 *
	 * @return The exposure time in seconds
	 * @throws Exception If there is a problem with the EPICS communication
	 */
	public double getExposureTime() throws Exception {
		// For now return the time calculated by getActualExposureTime()
		return getActualExposureTime();
		// TODO This can be reinstated once BC-148 is done
		// return EPICS_CONTROLLER.cagetDouble(getChannel(EXPOSURE_TIME_RBV));
	}

	/**
	 * Gets the actual exposure time in seconds, calculated from the <i>number of frames * frame time</i>.
	 * <p>
	 * Because SES operates the camera at a fixed frame rate the actual exposure time is set by the number of frames multiplied by the frame time.
	 *
	 * @return The exposure time in seconds
	 * @throws Exception If there is a problem with the EPICS communication
	 * @see #getExposureTime()
	 */
	private double getActualExposureTime() throws Exception {
		return getFrames() * cameraFrameTime;
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
	}
}
