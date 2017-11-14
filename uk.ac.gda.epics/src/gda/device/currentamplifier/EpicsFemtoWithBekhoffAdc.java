/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.currentamplifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;

/**
 * This class is for controlling a current detector which consists of a Femto current amplifier connected to a Bekhoff ADC.
 * <p>
 * The Femto amplifies the input current usually in mA or less and outputs a voltage as determined by the gain setting. i.e a <i>1x10^-6 A</i> current with a
 * <i>10^6</i> gain will output <i>1 V</i>
 * <p>
 * The ADC then digitizes this voltage so it can be read into EPICS. The ADC also offers the ability to average the voltage over time to improve the signal to
 * noise.
 * <p>
 * The EPICS EDM for these devices are: <br>
 * The Femto <img src="Femto_EDM.png"> <br>
 * The Bekhoff ADC <img src="Bekhoff_ADC_EDM.png">
 *
 * @author James Mudd
 */
public class EpicsFemtoWithBekhoffAdc extends DetectorBase implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(EpicsFemtoWithBekhoffAdc.class);

	// Values internal to the object for Channel Access
	private final transient EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName = null;
	// Map that stores the channel against the PV name
	private transient Map<String, Channel> channelMap = new HashMap<>();

	// Data PVs Although these are called I they are actually voltages from the Femto output measured by the ADC
	private static final String I_INSTANTANEOUS = "I";
	private static final String I_AVERAGE = "IAV";

	// Femto PVs
	private static final String FEMTO_GAIN = "GAIN";
	private static final String FEMTO_COUPLING = "ACDC";

	// ADC PVs // Some are currently unused as I don't understand what they do
	private static final String ADC_PREFIX = "ADC";
	private static final String ADC_MODE = "MODE";
	private static final String ADC_ENABLE = "ENABLED";
	private static final String ADC_RETRIGGER = "RETRIGGER";
	private static final String ADC_START = "TRIGGER";
	private static final String ADC_CLEAR = "CLEAR";
	private static final String ADC_SAMPLES = "SAMPLES";
	private static final String ADC_OFFSET = "OFFSET";
	private static final String ADC_AVERAGE = "AVERAGE"; //on I21 this is the integration time for the averaged value
	private static final String ADC_BUFFFER_COUNT = "BUFFERCOUNT";
	private static final String ADC_CHANNEL_BUFFFER = "CHANBUFF";
	private static final String ADC_CAPTURE = "CAPTURE";
	private static final String ADC_VALUE = "VALUE"; //on I21 this is the actual averaged value over the specifid integration time.
	private static final String ADC_STATE = "STATE";
	private static final String ADC_INTERRUPT = "INTERRUPT";

	private boolean hasIAVinPV = true;
	private boolean hasIinPV = true;
	private boolean supportsCoupling = true; // The I05 femtos don't support this DC only

	// Mode, gain, enum string eg. "Low Noise", "10E4", "10^4 low noise"
	private Map<String, Map<Double, String>> modeToGainToGainStringMap;
	// The next two maps are inverses
	// enum string, mode map This is used to figure out the mode from EPICS it is filled during configure
	private Map<String, String> gainStringToModeMap;
	private Map<String, Double> gainStringToGainMap;
	// This map is used for doing auto-gain within one mode
	private Map<String, List<Double>> modeToGainMap;

	private int adcChannel = 1; // Maybe all using 1. In this case you don't need to set this

	// Used to convert time into number of samples
	private int adcSamplingRate = 1000; // Hz

	// Auto-gain parameters
	private boolean autoGain = true; // Default to true as this should result in the best behaviour
	private long settleTime = 250; // ms The time to wait after a gain change for stability
	private double lowerVoltageBound = 0; // V. If ADC input is below this increase gain
	private double upperVoltageBound = 10; // V. If ADC input is above this decrease gain

	// This latch is used when acquiring to block while ADC acquires
	private transient CountDownLatch acquiringLatch = new CountDownLatch(0);

	// This is used to only add scan metadata when required
	private boolean firstReadoutInScan;

	private AdcMode adcMode;
	private boolean adcEnable;
	private boolean adcRetrigger;
	// This monitors the ADC state and is used to decrement the acquiring latch once the acquire is finished
	private final transient MonitorListener adcStateMonitor = ev -> {
		logger.trace("Received update from ADC state");
		// If the ADC state has changed to waiting its not busy so decrement the latch
		if (((short[]) ev.getDBR().getValue())[0] == 0) {
			logger.trace("ADC is now waiting");
			// Decrement the latch so waitWhileBusy will return if in a scan;
			acquiringLatch.countDown();
		}
	};

	public enum AdcMode {
		CONTINUOUS, TRIGGERED, GATED
	}

	public enum CouplingMode {
		AC, DC;
	}

	/**
	 * Lazy initialise channels and store them in a map for retrieval later
	 *
	 * @param pvPostFix
	 * @return
	 * @throws CAException
	 * @throws TimeoutException
	 */
	private Channel getChannel(String pvPostFix) throws CAException, TimeoutException {
		String fullPvName = basePVName + pvPostFix;
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
			logger.trace("Created channel for PV: {}", fullPvName);
		}
		return channel;
	}

	/**
	 * The full PV will be {@link #basePVName} + {@value #I_AVERAGE} + {@value #ADC_PREFIX} + {@link #adcChannel} + "_" + pvPostFix
	 *
	 * @param pvPostFix
	 *            The ADC post fix required from the class constants
	 * @return The channel associated with the PV requested
	 * @throws CAException
	 * @throws TimeoutException
	 */
	private Channel getAdcChannel(String pvPostFix) throws CAException, TimeoutException {
		if (!isHasIAVinPV()) return getChannel(ADC_PREFIX + adcChannel + "_" + pvPostFix);
		return getChannel(I_AVERAGE + ":" + ADC_PREFIX + adcChannel + "_" + pvPostFix);
	}

	/**
	 * The full PV will be {@link #basePVName} + {@value #I_INSTANTANEOUS} + ":" + pvPostFix
	 *
	 * @param pvPostFix
	 *            The femto post fix required from the class constants
	 * @return The channel associated with the PV requested
	 * @throws CAException
	 * @throws TimeoutException
	 */
	private Channel getFemtoChannel(String pvPostFix) throws CAException, TimeoutException {
		if (!isHasIinPV()) return getChannel(pvPostFix);
		return getChannel(I_INSTANTANEOUS + ":" + pvPostFix);
	}

	@Override
	public void configure() throws FactoryException {
		logger.trace("configure called");
		super.configure();

		// Check if we are already configured
		if (configured) {
			logger.trace("Already configured");
			return;
		}

		// First verify the Spring configuration
		if (basePVName == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new IllegalStateException("Configure called with no basePVName. Check spring configuration!");
		}
		if (modeToGainToGainStringMap == null) {
			logger.error("Configure called with no modeGainMap. Check spring configuration!");
			throw new IllegalStateException("Configure called with no modeGainMap. Check spring configuration!");
		}

		// Check the basePv ends with : if not add it
		if (!basePVName.endsWith(":")) {
			logger.debug("basePv didn't end with : adding one");
			basePVName += ":";
		}

		logger.info("Configuring Femto+ADC with base PV: {}", basePVName);
		final Set<String> epicsGainStrings = new HashSet<>();
		try {
			logger.debug("Adding monitor for ADC State");
			EPICS_CONTROLLER.setMonitor(getAdcChannel(ADC_STATE), adcStateMonitor);

			// Get the contents of the gain enum from EPICS and check if we have the same
			epicsGainStrings.addAll(Arrays.asList(EPICS_CONTROLLER.cagetLabels(getFemtoChannel(FEMTO_GAIN))));

		} catch (Exception e) {
			logger.error("Failed to configure current amplifier: {}", getName(), e);
		}

		logger.trace("Creating internal lookup maps");
		gainStringToModeMap = new HashMap<>();
		for (Entry<String, Map<Double, String>> mode : modeToGainToGainStringMap.entrySet()) {
			Map<Double, String> modeMap = mode.getValue();
			for (String epicsString : modeMap.values()) {
				gainStringToModeMap.put(epicsString, mode.getKey());
			}
		}
		gainStringToGainMap = new HashMap<>();
		for (Map<Double, String> modeMap : modeToGainToGainStringMap.values()) {
			for (Entry<Double, String> gainEntry : modeMap.entrySet()) {
				gainStringToGainMap.put(gainEntry.getValue(), gainEntry.getKey());
			}
		}
		modeToGainMap = new HashMap<>();
		for (Entry<String, Map<Double, String>> mode : modeToGainToGainStringMap.entrySet()) {
			List<Double> gainsForMode = new ArrayList<>(mode.getValue().keySet());
			gainsForMode.sort(null); // Sort the gains to ensure they are in ascending order
			modeToGainMap.put(mode.getKey(), gainsForMode);
		}

		// Check if the gain enum from EPICS has any elements we don't have and vice versa
		Set<String> mismatchedGainStrings = new HashSet<>();
		// If gainStringToGainMap does not contain the gain string add it to mismatchedGainStrings
		mismatchedGainStrings.addAll(epicsGainStrings.stream().filter(e -> !gainStringToGainMap.containsKey(e)).collect(Collectors.toSet()));
		// If epicsGainStrings does not contain the gain string add it to mismatchedGainStrings
		mismatchedGainStrings.addAll(gainStringToGainMap.keySet().stream().filter(e -> !epicsGainStrings.contains(e)).collect(Collectors.toSet()));
		// Warn if there is any mismatch
		if (!mismatchedGainStrings.isEmpty()) {
			logger.warn("Mismatched gain strings detected between EPICS and the Spring configuration");
			logger.warn("Detected mismatches: {}", mismatchedGainStrings);
		}

		// No input names its read-only
		setInputNames(new String[] {});
		setExtraNames(new String[] { getName() });

		configured = true;
		logger.info("Finished configuring Femto+ADC " + getName());
	}

	/**
	 * Changes the operation mode of the femto.
	 *
	 * @param mode
	 *            The requested operation mode
	 * @throws DeviceException
	 *             If the mode change fails
	 * @throws IllegalArgumentException
	 *             If mode is not in modeToGainToGainStringMap
	 */
	public void setFemtoMode(String mode) throws DeviceException {
		if (!hasMultipleFemtoModes()) {
			throw new UnsupportedOperationException("Femto only has one mode");
		}

		logger.debug("Changing mode to: {}", mode);
		// Check if the requested mode is valid
		if (!modeToGainToGainStringMap.containsKey(mode)) {
			throw new IllegalArgumentException("Invalid mode '" + mode + "' Avaliable modes are: " + modeToGainToGainStringMap.keySet());
		}

		// Change mode then perform autogain to get the best gain setting within the mode
		double lowestGainForMode = modeToGainMap.get(mode).get(0); // Get the lowest gain for this mode
		String newGain = modeToGainToGainStringMap.get(mode).get(lowestGainForMode);
		setGain(newGain); // Change the gain here, which also changes the mode

		// Optimise the gain for the new mode
		optimiseGain();
	}

	/**
	 * Gets the operation mode of the femto.
	 *
	 * @return The current operation mode
	 * @throws DeviceException
	 *             If the mode change fails
	 */
	public String getFemtoMode() throws DeviceException {
		String gainString = getGainString();
		return gainStringToModeMap.get(gainString);
	}

	private String getGainString() throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetString(getFemtoChannel(FEMTO_GAIN));
		} catch (TimeoutException | CAException | InterruptedException e) {
			String msg = "Error getting Femto gain";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	/**
	 * Increase the gain by one step in the current mode if possible.
	 *
	 * @return true if the gain was changed
	 * @throws DeviceException
	 */
	private boolean increaseGain() throws DeviceException {
		return increaseOrDecreaseGain(1);
	}

	/**
	 * Decrease the gain by one step in the current mode if possible.
	 *
	 * @return true if the gain was changed
	 * @throws DeviceException
	 */
	private boolean decreaseGain() throws DeviceException {
		return increaseOrDecreaseGain(-1);
	}

	private boolean increaseOrDecreaseGain(int indexIncrement) throws DeviceException {
		if (indexIncrement != 1 && indexIncrement != -1) {
			throw new IllegalArgumentException("The index increment must be 1 or -1");
		}

		final String mode = getFemtoMode();
		final double gain = getGain();

		final List<Double> gainsAvailiable = modeToGainMap.get(mode);
		final int indexOfCurrentGain = gainsAvailiable.indexOf(gain);

		try {
			final double newGain = gainsAvailiable.get(indexOfCurrentGain + indexIncrement);
			setGain(modeToGainToGainStringMap.get(mode).get(newGain));
			return true;
		} catch (IndexOutOfBoundsException e) {
			logger.debug("No higher gains avaliable in '{}' mode", mode, e);
			return false;
		}
	}

	public void setGain(double newGain) throws DeviceException {
		String mode = getFemtoMode();

		if (!modeToGainMap.get(mode).contains(newGain)) {
			throw new IllegalArgumentException("The reqested gain '" + newGain + "' is not avaliable in the current mode '" + mode + "'");
		}

		setGain(modeToGainToGainStringMap.get(mode).get(newGain));
	}

	public double getGain() throws DeviceException {
		String gainString = getGainString();
		return gainStringToGainMap.get(gainString);
	}

	/**
	 * This is the method which actually writes to the gain PV. It should only be passed strings taken from the {@link #modeToGainToGainStringMap}
	 *
	 * @param gain
	 */
	private void setGain(String gain) throws DeviceException {
		logger.debug("Changing gain to: {}", gain);
		try {
			EPICS_CONTROLLER.caputWait(getFemtoChannel(FEMTO_GAIN), gain);
		} catch (TimeoutException | CAException | InterruptedException e) {
			String msg = "Error setting gain";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}

		logger.trace("Waiting for settling time...");
		// Wait for the settling time
		try {
			Thread.sleep(settleTime);
		} catch (InterruptedException e) {
			logger.error("Interuppted waiting for settling", e);
			Thread.currentThread().interrupt(); // Re-interrupt
		}
		logger.trace("Finished waiting for settling time");
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		logger.trace("setCollectionTime called with {}", collectionTime);
		super.setCollectionTime(collectionTime);

		// Convert from seconds to ms and double to int
		long samples = Math.round(collectionTime * adcSamplingRate);

		// If you want instantaneous measurements collectionTime might be set to 0 or -1 but that would mean need 1 sample
		if (samples <= 0) {
			samples = 1;
		}

		setAdcAveragingSamples(samples);
	}

	/**
	 * This sets up the averaging performed by the ADC it sets both the number of samples and the average PVs.
	 *
	 * @param samples
	 *            Number of samples must be >=1
	 * @throws IllegalArgumentException
	 *             if samples < 1
	 */
	private void setAdcAveragingSamples(final long samples) throws DeviceException {
		// Check that the minimum number of samples is one
		if (samples < 1) {
			throw new IllegalArgumentException("Number of samples must be >= 1");
		}

		try {
			EPICS_CONTROLLER.caputWait(getAdcChannel(ADC_SAMPLES), samples);
			EPICS_CONTROLLER.caputWait(getAdcChannel(ADC_AVERAGE), samples);
		} catch (TimeoutException | CAException | InterruptedException e) {
			String msg = "Error setting ADC averaging samples";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
		logger.debug("Set ADC samples to: {}", samples);
	}

	@Override
	public void collectData() throws DeviceException {
		logger.trace("collectData called");
		try {
			// Start the ADC acquiring
			EPICS_CONTROLLER.caput(getAdcChannel(ADC_START), 1);
			logger.trace("Started acquiring");
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error setting triggering ADC";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
		// Set the latch this will be decremented by the monitor when the ADC state changes back to WAITING
		acquiringLatch = new CountDownLatch(1);
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		logger.trace("waitWhileBusy called");
		// Wait for the acquisition to complete will be updated by the CA monitor
		logger.trace("Waiting for acquire to finish...");
		acquiringLatch.await();
		logger.trace("Acquiring finshed");
	}

	@Override
	public int getStatus() throws DeviceException {
		logger.trace("getStatus called");
		// Get the status from the ADC and map it to the Detector states
		String adcState = getAdcStatus();
		// If its triggered then its acquiring i.e busy else not
		if (adcState.equals("TRIGGERED")) {
			return Detector.BUSY;
		}
		return Detector.IDLE;
	}

	public String getAdcStatus() throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetString(getAdcChannel(ADC_STATE));
		} catch (TimeoutException | CAException | InterruptedException e) {
			String msg = "Error getting ADC status";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// Doesn't write its own files
		return false;
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();

		// Ensure the ADC is setup for software triggering
		setupAdcForSoftwareTriggering();

		// Get the file writing ready for metadata.
		firstReadoutInScan = true;
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		// restore ADC mode to what it was before scan.
		restoreADCMode();
		firstReadoutInScan=false;
	}
	private void setupAdcForSoftwareTriggering() throws DeviceException {
		adcMode = getAdcMode();
		adcEnable = getAdcEnable();
		adcRetrigger = getAdcRetrigger();
		setAdcMode(AdcMode.TRIGGERED);
		setAdcEnable(true);
		setAdcRetrigger(true);
	}

	private void restoreADCMode() throws DeviceException {
		if (adcMode != null) setAdcMode(adcMode);
		setAdcEnable(adcEnable);
		setAdcRetrigger(adcRetrigger);
	}

	@Override
	public void atPointStart() throws DeviceException {
		logger.trace("atPointStart called");
		// Here check and adjust the gain setting if auto-gain is on
		if (autoGain) {
			optimiseGain();
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		restoreADCMode();
		super.atCommandFailure();
	}
	@Override
	public void stop() throws DeviceException {
		restoreADCMode();
		super.stop();
	}
	/**
	 * Adjusts the gain of the current amplifier so the its output the ADC input is within the voltage bounds.
	 *
	 * @throws DeviceException
	 */
	public void optimiseGain() throws DeviceException {
		logger.trace("optimiseGain called");
		// As the femto output range is -10 to +10 V the required gain is dependent on the absolute value.
		double voltage = Math.abs(getInstantaneousVoltage());

		// Check if its already in range if it is return
		if (voltage > lowerVoltageBound && voltage < upperVoltageBound) {
			logger.trace("Don't need to adjust gain");
			return; // Don't need to change the gain
		}

		// Need to increase gain
		else if (voltage < lowerVoltageBound) {
			logger.trace("Increasing gain...");
			// Keep increasing while still to low
			while (Math.abs(getInstantaneousVoltage()) < lowerVoltageBound) {
				// Try to increase the gain if it can't be increased more warn and return
				if (!increaseGain()) {
					logger.warn("Couldn't increase gain further. Input current might be out of range");
					return;
				}
			}
		}

		// Need to decrease gain
		else { // voltage > upperVoltageBound
			logger.trace("Decreasing gain...");
			// Keep decreasing while still to high
			while (Math.abs(getInstantaneousVoltage()) > upperVoltageBound) {
				// Try to increase the gain if it can't be increased more warn and return
				if (!decreaseGain()) {
					logger.warn("Couldn't decrease gain further. Input current might be out of range");
					return;
				}
			}
		}
		logger.debug("Optimised gain");
	}

	private double getInstantaneousVoltage() throws DeviceException {
		logger.trace("getInstantaneousVoltage called");
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(I_INSTANTANEOUS));
		} catch (TimeoutException | CAException | InterruptedException e) {
			String msg = "Error getting instantaneous voltage";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	private double getAverageVoltage() throws DeviceException {
		logger.trace("getAverageVoltage called");
		try {
			if (!isHasIAVinPV()) return EPICS_CONTROLLER.cagetDouble(getFemtoChannel(ADC_PREFIX + adcChannel + "_" + ADC_VALUE));
			return EPICS_CONTROLLER.cagetDouble(getChannel(I_AVERAGE));
		} catch (TimeoutException | CAException | InterruptedException e) {
			String msg = "Error getting average voltage";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		logger.trace("readout called");

		// Get the data from EPICS
		final double voltage = getAverageVoltage();
		final double gain = getGain();
		// Divide by the gain to change from V back to amps (Gain is in V/A)
		final double current = voltage / gain;

		// Build the NXDetectorData pass in this to setup input/extra names and output format
		NXDetectorData data = new NXDetectorData(this);
		// Just plot the current during the scan
		data.setPlottableValue(getName(), current);

		// Add the data to be written to the file, write the current with the detector name, convention and allows processing to work
		data.addData(getName(), getName(), new NexusGroupData(current), "A");
		data.addData(getName(), "gain", new NexusGroupData(gain), "V/A");

		// If its the first point add the metadata
		if (firstReadoutInScan) {
			logger.trace("Adding metadata for file writter");
			// Only add the data if  it makes sense for this femto
			if (supportsCoupling) {
				data.addElement(getName(), "coupling", new NexusGroupData(getFemtoCouplingMode().toString()), null, false);
			}
			if (hasMultipleFemtoModes()) {
				data.addElement(getName(), "mode", new NexusGroupData(getFemtoMode()), null, false);
			}
			data.addElement(getName(), "count_time", new NexusGroupData(getCollectionTime()), "sec", false);
			firstReadoutInScan = false; // Reset the flag don't need to add this again
		}

		return data;
	}

	/**
	 * Set the Femto coupling mode, takes a string for Jython to use
	 *
	 * @param couplingMode
	 *            The requested coupling mode
	 * @throws DeviceException
	 */
	public void setFemtoCouplingMode(final String couplingMode) throws DeviceException {
		setFemtoCouplingMode(CouplingMode.valueOf(couplingMode));
	}

	public void setFemtoCouplingMode(final CouplingMode couplingMode) throws DeviceException {
		if (!supportsCoupling) {
			throw new UnsupportedOperationException("Femto is configured not to support coupling");
		}

		try {
			EPICS_CONTROLLER.caput(getFemtoChannel(FEMTO_COUPLING), couplingMode.toString());
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error setting coupling";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
		logger.debug("Changed coupling mode to: {}", couplingMode);
	}

	public CouplingMode getFemtoCouplingMode() throws DeviceException {
		if (!supportsCoupling) {
			throw new UnsupportedOperationException("Femto is configured not to support coupling");
		}

		try {
			return CouplingMode.valueOf(EPICS_CONTROLLER.cagetString(getFemtoChannel(FEMTO_COUPLING)));
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error getting coupling";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	/**
	 * Set the ADC mode, takes a string for Jython to use
	 *
	 * @param adcMode
	 *            The requested ADC mode
	 * @throws DeviceException
	 */
	public void setAdcMode(final String adcMode) throws DeviceException {
		setAdcMode(AdcMode.valueOf(adcMode));
	}

	public void setAdcMode(final AdcMode adcMode) throws DeviceException {
		try {
			EPICS_CONTROLLER.caput(getAdcChannel(ADC_MODE), adcMode.toString());
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error setting ADC mode";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
		logger.debug("Changed ADC mode to: {}", adcMode);
	}

	public AdcMode getAdcMode() throws DeviceException {
		try {
			return AdcMode.valueOf(EPICS_CONTROLLER.cagetString(getAdcChannel(ADC_MODE)));
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error getting ADC mode";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	public void setAdcEnable(final boolean enabled) throws DeviceException {
		try {
			EPICS_CONTROLLER.caput(getAdcChannel(ADC_ENABLE), enabled ? "ENABLED" : "DISABLED");
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error setting ADC enabled";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
		logger.debug("Changed ADC enabled to: {}", enabled);
	}

	public boolean getAdcEnable() throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetString(getAdcChannel(ADC_ENABLE)).equals("ENABLED");
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error getting ADC enabled";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	public void setAdcRetrigger(final boolean enabled) throws DeviceException {
		try {
			EPICS_CONTROLLER.caput(getAdcChannel(ADC_RETRIGGER), enabled ? "ENABLED" : "DISABLED");
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error setting ADC retrigger";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
		logger.debug("Changed ADC enabled to: {}", enabled);
	}

	public boolean getAdcRetrigger() throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetString(getAdcChannel(ADC_RETRIGGER)).equals("ENABLED");
		} catch (CAException | InterruptedException | TimeoutException e) {
			String msg = "Error getting ADC retrigger";
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public String toFormattedString() {
		try {
			// Get the data from EPICS
			final double voltage = getInstantaneousVoltage();
			final double gain = getGain();
			// Divide by the gain to change from V back to amps (Gain is in V/A)
			final double current = voltage / gain;
			// Format using the output format for the current
			return String.format("%s : " + getOutputFormat()[0] + " A (Gain = %1.0g)", getName(), current, gain);
		} catch (Exception e) {
			logger.error("Error getting {} status", getName(), e);
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
	}

	private boolean hasMultipleFemtoModes() {
		return modeToGainToGainStringMap.size() > 1 ? true : false;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public int getAdcChannel() {
		return adcChannel;
	}

	public void setAdcChannel(int adcChannel) {
		this.adcChannel = adcChannel;
	}

	public Map<String, Map<Double, String>> getModeToGainToGainStringMap() {
		return modeToGainToGainStringMap;
	}

	public void setModeToGainToGainStringMap(Map<String, Map<Double, String>> modeToGainToGainStringMap) {
		this.modeToGainToGainStringMap = modeToGainToGainStringMap;
	}

	public boolean isAutoGain() {
		return autoGain;
	}

	public void setAutoGain(boolean autoGain) {
		this.autoGain = autoGain;
	}

	public long getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(long settleTime) {
		this.settleTime = settleTime;
	}

	public double getLowerVoltageBound() {
		return lowerVoltageBound;
	}

	public void setLowerVoltageBound(double lowerVoltageBound) {
		this.lowerVoltageBound = lowerVoltageBound;
	}

	public double getUpperVoltageBound() {
		return upperVoltageBound;
	}

	public void setUpperVoltageBound(double upperVoltageBound) {
		this.upperVoltageBound = upperVoltageBound;
	}

	public boolean isHasIAVinPV() {
		return hasIAVinPV;
	}

	public void setHasIAVinPV(boolean hasAverage) {
		this.hasIAVinPV = hasAverage;
	}

	public boolean isHasIinPV() {
		return hasIinPV;
	}

	public void setHasIinPV(boolean hasIinPV) {
		this.hasIinPV = hasIinPV;
	}

	public boolean isSupportsCoupling() {
		return supportsCoupling;
	}

	public void setSupportsCoupling(boolean supportsCoupling) {
		this.supportsCoupling = supportsCoupling;
	}

}
