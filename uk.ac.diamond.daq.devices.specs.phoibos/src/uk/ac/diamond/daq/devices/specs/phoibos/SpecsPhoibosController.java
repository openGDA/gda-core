/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * The EPICS controller class for operating SPECS Phoibos electron analysers. The class interact only with the CAM
 * plugin which contains the specific PVs to control the electron analyser, and to read back the collected data.
 * <p>
 * Only operation in kinetic energy (KE) mode is supported. For users to work in binding energy (BE) that needs to be
 * implemented at a higher level.
 * <p>
 * <ul>
 * <li>All energies are in electron volts (eV)
 * <li>All angles are in degrees (deg)
 * <li>All positions are in millimetres (mm)
 * </ul>
 *
 * This class is closely related to the equivalent class for VG Scienta analysers VGScientaController
 *
 * @author James Mudd
 */
public class SpecsPhoibosController implements Configurable, IObservable {
	private static final Logger logger = LoggerFactory.getLogger(SpecsPhoibosController.class);

	// Values internal to the object for Channel Access
	private final EpicsController epicsController = EpicsController.getInstance();
	private String basePVName = null;
	// Map that stores the channel against the PV name
	private final Map<String, Channel> channelMap = new HashMap<>();
	// Used to hold and notify observers
	private final ObservableComponent observableComponent = new ObservableComponent();

	// PV to see if the IOC is connected to the SpecsLab software
	private static final String CONNECTED_RBV = "CONNECTED_RBV";

	// PSU mode defines the current energy range accessable
	// SPECS refer to this as scan range but PSU mode is clearer and more general so its used here
	private static final String PSU_MODE = "SCAN_RANGE";
	private static final String PSU_MODE_RBV = "SCAN_RANGE_RBV";

	// Lens mode
	private static final String LENS_MODE = "LENS_MODE";
	private static final String LENS_MODE_RBV = "LENS_MODE_RBV";
	// Acquisition mode is fixed or swept
	private static final String ACQUISITION_MODE = "ACQ_MODE";
	private static final String ACQUISITION_MODE_RBV = "ACQ_MODE_RBV";

	// Scan energy settings
	// The pass energy can be seen as a energy resolution setting. Small pass energy -> high energy resolution
	private static final String PASS_ENERGY = "PASS_ENERGY";
	private static final String PASS_ENERGY_RBV = "PASS_ENERGY_RBV";
	// In swept mode the energy range is defined by start, stop and step
	private static final String LOW_ENERGY = "LOW_ENERGY";
	private static final String LOW_ENERGY_RBV = "LOW_ENERGY_RBV";
	private static final String HIGH_ENERGY = "HIGH_ENERGY";
	private static final String HIGH_ENERGY_RBV = "HIGH_ENERGY_RBV";
	// Energy step
	private static final String ENERGY_STEP = "STEP_SIZE";
	private static final String ENERGY_STEP_RBV = "STEP_SIZE_RBV";
	// Centre Energy - This is only used in "Fixed Energy" mode where the detector is used as a point detector
	private static final String CENTRE_ENERGY = "KINETIC_ENERGY";
	private static final String CENTRE_ENERGY_RBV = "KINETIC_ENERGY_RBV";
	// Retard Ratio (Defined as RR = KE/PE) - This is only used in "Fixed Retarding Ratio" mode to define the PE
	private static final String RETARDING_RATIO = "RETARDING_RATIO";
	private static final String RETARDING_RATIO_RBV = "RETARDING_RATIO";
	// Values - In "Snapshot" mode this is used to define how many fixed mode regions the range will be broken into.
	private static final String VALUES = "VALUES";
	private static final String VALUES_RBV = "VALUES_RBV";

	// Scan exposure settings
	private static final String EXPOSURE_TIME = "AcquireTime";
	private static final String EXPOSURE_TIME_RBV = "AcquireTime_RBV";
	// Number of repeats to be summed in SES (Mirroring a standard AD PV)
	private static final String ITERATIONS = "NumExposures";
	private static final String ITERATIONS_RBV = "NumExposures_RBV";

	// Slices define the number of y channels, can be used to reduce the data.
	// SPECS seem to require this to be a factor of the detector size
	private static final String SLICES = "SLICES";
	private static final String SLICES_RBV = "SLICES_RBV";

	// Pause for suspending the analyser mid scan
	private static final String PAUSE = "PAUSE";
	private static final String PAUSE_RBV = "PAUSE_RBV";

	// TODO Check status and progress PVs
	// Number of points in the pre scan only for swept mode
	private static final String TOTAL_LEAD_POINTS_RBV = "TOTAL_LEAD_POINTS_RBV";
	private static final String TOTAL_POINTS_ITERATION_RBV = "TOTAL_POINTS_ITERATION_RBV";
	private static final String TOTAL_POINTS_RBV = "TOTAL_POINTS_RBV";
	private static final String CURRENT_CHANNEL_RBV = "CURRENT_CHANNEL_RBV";

	//Y scale is the angle or position in deg or mm
	private static final String Y_MIN_RBV = "Y_MIN_RBV";
	private static final String Y_MAX_RBV = "Y_MAX_RBV";
	private static final String Y_UNITS_RBV = "Y_UNITS_RBV";

	// Data PVs
	// Image is the full 2D data the size will be TOTAL_DATA_POINTS_RBV * SLICES_RBV
	private static final String IMAGE_DATA = "IMAGE";
	// Spectrum is the integrated energy spectrum (sum of all y channels) the size is TOTAL_DATA_POINTS_RBV
	private static final String SPECTRUM_DATA = "INT_SPECTRUM";

	// Lists for holding valid values of the enum PVs
	private final Set<String> lensModes = new LinkedHashSet<>();
	private final Set<String> psuModes = new LinkedHashSet<>();
	private final Set<String> acquisitionModes = new LinkedHashSet<>();

	private ADBaseImpl adBase;

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	private Channel getChannel(String pvPostFix) throws TimeoutException, CAException {
		String fullPvName = basePVName + pvPostFix;
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = epicsController.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	/**
	 * Configures the analyser controller and checks it is in a state to be used by GDA.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() {
		if (basePVName == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new IllegalStateException("Configure called with no basePVName. Check spring configuration!");
		}
		logger.info("Configuring analyser with base PV: {}", basePVName);

		try {
			// Check if EPICS is connected to SpecsLab
			if (!isConnected()) {
				logger.error("EPICS IOC is not connected to SpecsLab");
				throw new IllegalStateException("EPICS IOC is not connected to SpecsLab");
			}
			// Inspect the analyser for the available options
			initaliseEnumChannel(PSU_MODE, psuModes);
			logger.debug("Avaliable PSU modes: {}", this.psuModes);
			initaliseEnumChannel(LENS_MODE, lensModes);
			logger.debug("Avaliable lens modes: {}", this.lensModes);
			initaliseEnumChannel(ACQUISITION_MODE, acquisitionModes);
			logger.debug("Avaliable acquisition modes: {}", this.acquisitionModes);

			// Add monitor to find out when new data is available
			logger.debug("Adding monitor to: {}", CURRENT_CHANNEL_RBV);
			epicsController.setMonitor(getChannel(CURRENT_CHANNEL_RBV), evt -> {
				logger.trace("Received event: {}", evt);
				notifyListeners(evt);
			});

			// Setup an ADBase for interacting with common PVs
			logger.debug("Making ADBaseImpl");
			adBase = new ADBaseImpl();
			adBase.setBasePVName(basePVName);
			adBase.afterPropertiesSet();
			logger.debug("Finished creating ADBase");

			logger.info("Finished configuring analyser");

		} catch (Exception e) {
			logger.error("Configuring the analyser failed. Check EPICS IOC", e);
		}
	}

	/**
	 * This inspects a enum channel for its values and adds them to a set. It will warn if the channel contains empty
	 * values and these will not be added to the set. It will clear the existing values in the set.
	 *
	 * @param channel
	 *            The channel to inspect
	 * @param set
	 *            The set to contain valid positions
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	private void initaliseEnumChannel(String channel, Set<String> set) throws Exception {
		String[] positionLabels = epicsController.cagetLabels(getChannel(channel));
		if (positionLabels == null || positionLabels.length == 0) {
			throw new DeviceException("Error getting lables from enum channel: " + basePVName + channel);
		}
		// Clear the list here this allows for rerunning configure
		set.clear();
		// Add the positions to the list
		for (String position : positionLabels) {
			if (position == null || position.isEmpty()) {
				logger.warn("Enum channel {} contains empty entries", basePVName + channel);
			} else {
				// Add to set log error if duplicate values are seen.
				if (!set.add(position)) {
					logger.error("Duplicate value '{}' found in PV: {}", position, channel);
				}
			}
		}
	}

	/**
	 * Sets the current lens mode.
	 *
	 * @param value
	 *            The required lens mode.
	 * @throws IllegalArgumentException
	 *             If the lens mode requested is invalid
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @see #getLensModes()
	 */
	public void setLensMode(String value) throws Exception {
		if (!lensModes.contains(value)) {
			throw new DeviceException("The lens mode requested: " + value + " is not valid");
		}
		// It is valid so set it
		epicsController.caputWait(getChannel(LENS_MODE), value);
	}

	/**
	 * Gets the current lens mode
	 *
	 * @return The current lens mode
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public String getLensMode() throws Exception {
		return epicsController.cagetString(getChannel(LENS_MODE_RBV));
	}

	/**
	 * Gets the current PSU mode.
	 *
	 * @return The current power supply mode
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public String getPsuMode() throws Exception {
		return epicsController.cagetString(getChannel(PSU_MODE_RBV));
	}

	/**
	 * Sets the PSU mode. This is to allow different kinetic energy ranges to be reached.
	 *
	 * @param value
	 *            The required PSU mode
	 * @throws IllegalArgumentException
	 *             If the PSU mode requested is invalid
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @see #getPsuModes()
	 */
	public void setPsuMode(String value) throws Exception {
		// Check if the PSU mode is valid
		if (!psuModes.contains(value)) {
			throw new IllegalArgumentException("The PSU mode requested: " + value + " is not valid");
		}
		// It is valid so set it.
		epicsController.caputWait(getChannel(PSU_MODE), value);
	}

	/**
	 * Sets the acquisition mode. Acquisition modes provide different ways of operating the detector, which might be
	 * better in different count rate regimes.
	 *
	 * @param value
	 *            The required acquisition mode.
	 * @throws IllegalArgumentException
	 *             If the acquisition mode requested is invalid
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @see #getAcquisitionModes()
	 */
	public void setAcquisitionMode(String value) throws Exception {
		// Check if the acquisition mode is valid
		if (!acquisitionModes.contains(value)) {
			throw new DeviceException("The acquisition mode requested: " + value + " is not valid");
		}
		// It is valid so set it.
		epicsController.caputWait(getChannel(ACQUISITION_MODE), value);
	}

	/**
	 * Gets the acquisition mode. Acquisition modes provide different ways of operating the detector, which might be
	 * better in different count rate regimes.
	 *
	 * @return The current acquisition mode
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public String getAcquisitionMode() throws Exception {
		return epicsController.cagetString(getChannel(ACQUISITION_MODE_RBV));
	}

	/**
	 * Acquisition modes provide different ways of operating the detector. This returns a read-only copy of the list of
	 * available modes.
	 *
	 * @return Set of the available acquisition modes
	 */
	public Set<String> getAcquisitionModes() {
		return Collections.unmodifiableSet(acquisitionModes);
	}

	/**
	 * Sets the pass energy to the requested value, in eV.
	 *
	 * @param value
	 *            The requested pass energy, in eV
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setPassEnergy(double value) throws Exception {
		epicsController.caputWait(getChannel(PASS_ENERGY), value);
	}

	/**
	 * Gets the currently set pass energy, in eV.
	 *
	 * @return The current pass energy, in eV
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getPassEnergy() throws Exception {
		return epicsController.cagetDouble(getChannel(PASS_ENERGY_RBV));
	}

	/**
	 * Sets the low kinetic energy for the scan, in eV.
	 *
	 * @param value
	 *            The requested start energy, in eV
	 * @throws IllegalArgumentException
	 *             If value is <= 0
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setLowEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Start energy must be greater than 0");
		}
		epicsController.caputWait(getChannel(LOW_ENERGY), value);
	}

	/**
	 * Gets the current low kinetic energy for the scan, in eV.
	 *
	 * @return The current end energy, in eV
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getLowEnergy() throws Exception {
		return epicsController.cagetDouble(getChannel(LOW_ENERGY_RBV));
	}

	/**
	 * Sets the high kinetic energy for the scan, in eV.
	 *
	 * @param value
	 *            The requested end energy, in eV
	 * @throws IllegalArgumentException
	 *             If value is <= 0
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setHighEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("End energy must be greater than 0");
		}
		epicsController.caputWait(getChannel(HIGH_ENERGY), value);
	}

	/**
	 * Gets the current high kinetic energy for the scan, in eV.
	 *
	 * @return The current end energy, in eV
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getHighEnergy() throws Exception {
		return epicsController.cagetDouble(getChannel(HIGH_ENERGY_RBV));
	}

	/**
	 * Sets the step energy for the scan, in eV. This is only used in "Fixed Transmission" and "Fixed Retarding Ratio"
	 * modes.
	 *
	 * @param value
	 *            The requested energy step, in eV
	 * @throws IllegalArgumentException
	 *             If value is <= 0
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setEnergyStep(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Energy step must be greater than 0");
		}
		epicsController.caputWait(getChannel(ENERGY_STEP), value);
	}

	/**
	 * Gets the current step energy for the scan. This is only used in "Fixed Transmission" and "Fixed Retarding Ratio"
	 * modes
	 *
	 * @return The current step energy, in eV
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getEnergyStep() throws Exception {
		return epicsController.cagetDouble(getChannel(ENERGY_STEP_RBV));
	}

	/**
	 * Sets the centre energy to be used for "Fixed Energy" scans where the whole detector is integrated, in eV.
	 *
	 * @param value
	 *            The requested centre energy in eV
	 * @throws IllegalArgumentException
	 *             If value is <= 0
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setCentreEnergy(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Centre energy must be greater than 0");
		}
		epicsController.caputWait(getChannel(CENTRE_ENERGY), value);
	}

	/**
	 * Gets the current centre energy for "Fixed Energy" scans, in eV.
	 *
	 * @return The current centre energy, in eV
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getCentreEnergy() throws Exception {
		return epicsController.cagetDouble(getChannel(CENTRE_ENERGY_RBV));
	}

	/**
	 * Sets the retarding ratio to be used for "Fixed Retarding Ratio" scans. This is then used to vary the pass energy
	 * with the kinetic energy according to RR = KE/PE.
	 *
	 * @param value
	 *            The requested retard ratio
	 * @throws IllegalArgumentException
	 *             If value is <= 0
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setRetardingRatio(double value) throws Exception {
		if (value <= 0) {
			throw new IllegalArgumentException("Retard ratio must be greater than 0");
		}
		epicsController.caputWait(getChannel(RETARDING_RATIO), value);
	}

	/**
	 * Gets the retarding ratio to be used for "Fixed Retarding Ratio" scans. This is then used to vary the pass energy
	 * with the kinetic energy according to RR = KE/PE.
	 *
	 * @return The current retarding ratio
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getRetardingRatio() throws Exception {
		return epicsController.cagetDouble(getChannel(RETARDING_RATIO_RBV));
	}

	/**
	 * Sets the number of values (also called samples) to be used in "Snapshot" or "Fixed Energy" modes.
	 * <p>
	 * In snapshot mode this defines the number of fixed energy windows that the energy range will be divided into. This
	 * allow larger KE ranges to be covered using a smaller pass energy.
	 * <p>
	 * In fixed energy mode this defines the number of repeats that define one iteration.
	 *
	 * @param value
	 *            The requested number of values for the scan
	 * @throws IllegalArgumentException
	 *             If value is < 1
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setValues(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("Values must be greater or equal to 1");
		}
		epicsController.caput(getChannel(VALUES), value);
	}

	/**
	 * Gets the number of values (also called samples) to be used in "Snapshot" or "Fixed Energy" modes.
	 * <p>
	 * In snapshot mode this defines the number of fixed energy windows that the energy range will be divided into. This
	 * allow larger KE ranges to be covered using a smaller pass energy.
	 * <p>
	 * In fixed energy mode this defines the number of repeats that define one iteration.
	 *
	 * @return The current values
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public int getValues() throws Exception {
		return epicsController.cagetInt(getChannel(VALUES_RBV));
	}

	public int getSlices() throws Exception {
		return epicsController.cagetInt(getChannel(SLICES_RBV));
	}

	public int getEnergyChannels() throws Exception {
		return epicsController.cagetInt(getChannel(TOTAL_POINTS_ITERATION_RBV));
	}

	public int getTotalLeadPoints() throws Exception {
		return epicsController.cagetInt(getChannel(TOTAL_LEAD_POINTS_RBV));
	}

	public double[] getSpectrum() throws Exception {
		return epicsController.cagetDoubleArray(getChannel(SPECTRUM_DATA), getEnergyChannels());
	}

	/**
	 * This calls {@link #getImage(int)} but is more convenient as it handles the size for you. If performance is
	 * critical it would be better to reduce the number of requests to EPICS to do this you can call
	 * {@link #getImage(int)} directly if you already know the size required.
	 *
	 * @return The image data as a 1D array
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double[] getImage() throws Exception {
		return getImage(getEnergyChannels() * getSlices());
	}

	public double[] getSpectrum(int i) throws Exception {
		return epicsController.cagetDoubleArray(getChannel(SPECTRUM_DATA), i);
	}

	/**
	 * This gets the image as a double. You need to request the correct number of elements, if you don't know call
	 * {@link #getImage()} instead as this will be handled automatically.
	 *
	 * @param i
	 *            The number of array elements requested. Should be the the image size
	 * @return The image data as a 1D array
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double[] getImage(int i) throws Exception {
		long startTime = System.nanoTime();
		double[] data = epicsController.cagetDoubleArray(getChannel(IMAGE_DATA), i);
		long endTime = System.nanoTime();
		logger.trace("Getting image, {} double values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	/**
	 * This gets the image as a float, this maybe useful as type conversion is performed server side by EPICS so the
	 * data transfered is halved this can help improve performance (at the loss of precision). You need to request the
	 * correct number of elements.
	 *
	 * @param i
	 *            The number of array elements requested. Should be the the image size
	 * @return The image data as a 1D array
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public float[] getImageAsFloat(int i) throws Exception {
		long startTime = System.nanoTime();
		float[] data = epicsController.cagetFloatArray(getChannel(IMAGE_DATA), i);
		long endTime = System.nanoTime();
		logger.trace("Getting image, {} float values took: {} ms", i, (endTime - startTime) / 1.0E6);
		return data;
	}

	/**
	 * This returns a read-only copy of the available lens modes.
	 *
	 * @return Set of the available lens modes
	 */
	public Set<String> getLensModes() {
		return Collections.unmodifiableSet(lensModes);
	}

	/**
	 * This returns a read-only copy of the available power supply modes.
	 *
	 * @return Set of the available power supply modes
	 */
	public Set<String> getPsuModes() {
		return Collections.unmodifiableSet(psuModes);
	}

	/**
	 * Gets the currently set exposure time in seconds.
	 *
	 * @return The exposure time in seconds
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getExposureTime() throws Exception {
		return epicsController.cagetDouble(getChannel(EXPOSURE_TIME_RBV));
	}

	/**
	 * Sets the exposure time.
	 *
	 * @param value
	 *            The requested exposure time in seconds
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public void setExposureTime(double value) throws Exception {
		epicsController.caputWait(getChannel(EXPOSURE_TIME), value);
	}

	/**
	 * Gets the currently set number of iterations.
	 *
	 * @return The number of iterations
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public int getIterations() throws Exception {
		return epicsController.cagetInt(getChannel(ITERATIONS_RBV));
	}

	/**
	 * Sets the number of iterations to be performed.
	 *
	 * @param value
	 *            Number of iterations to run
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @throws IllegalArgumentException
	 *             If value is <1
	 */
	public void setIterations(int value) throws Exception {
		if (value < 1) {
			throw new IllegalArgumentException("Iterations must be greater than or equal to 1");
		}
		epicsController.caputWait(getChannel(ITERATIONS), value);
	}

	/**
	 * Check if the EPICS IOC is connected to SpecsLab.
	 *
	 * @return true if the IOC is connected to SpecsLab
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public boolean isConnected() throws Exception {
		return "Connected".equals(epicsController.cagetString(getChannel(CONNECTED_RBV)));
	}

	/**
	 * Pauses the current scan.
	 *
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @see #resume()
	 * @see #isPaused()
	 */
	public void pause() throws Exception {
		epicsController.caputWait(getChannel(PAUSE), 1);
	}

	/**
	 * Resumes the current scan.
	 *
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @see #pause()
	 * @see #isPaused()
	 */
	public void resume() throws Exception {
		epicsController.caputWait(getChannel(PAUSE), 0);
	}

	/**
	 * Check the current paused status of the analyser.
	 *
	 * @return True if the analyser is paused
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 * @see #pause()
	 * @see #resume()
	 */
	public boolean isPaused() throws Exception {
		return epicsController.cagetInt(getChannel(PAUSE_RBV)) == 1;
	}

	/**
	 * Gets the current start (or low) Y scale, in eV.
	 *
	 * @return The current start Y
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getStartY() throws Exception {
		return epicsController.cagetDouble(getChannel(Y_MIN_RBV));
	}

	/**
	 * Gets the current end (or high) Y scale, in eV.
	 *
	 * @return The current start Y
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public double getEndY() throws Exception {
		return epicsController.cagetDouble(getChannel(Y_MAX_RBV));
	}

	/**
	 * Gets the Y scale units
	 *
	 * @return The current start Y
	 * @throws Exception
	 *             If there is a problem with the EPICS communication
	 */
	public String getYUnits() throws Exception {
		return epicsController.cagetString(getChannel(Y_UNITS_RBV));
	}

	public void setImageMode(ImageMode imagemode) throws Exception {
		adBase.setImageModeWait(imagemode);
	}

	public void startAcquiring() throws Exception {
		adBase.startAcquiring();
	}

	public void stopAcquiring() throws Exception {
		adBase.stopAcquiring();
	}

	public SpecsPhoibosStatus getDetectorStatus() throws Exception {
		return SpecsPhoibosStatus.get(adBase.getDetectorState_RBV());
	}

	public String getStatusMessage() throws Exception {
		return adBase.getStatusMessage_RBV();
	}

	public int waitWhileStatusBusy() throws InterruptedException {
		return adBase.waitWhileStatusBusy();
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	private void notifyListeners(Object evt) {
		observableComponent.notifyIObservers(this, evt);
	}

	public int getTotalPoints() throws Exception {
		return epicsController.cagetInt(getChannel(TOTAL_POINTS_RBV));
	}

	public int getCurrentPoint() throws Exception {
		return epicsController.cagetInt(getChannel(CURRENT_CHANNEL_RBV));
	}
}

