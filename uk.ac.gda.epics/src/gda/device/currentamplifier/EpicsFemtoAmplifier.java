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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.AmplifierAutoGain;
import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.util.functions.ThrowingConsumer;
import gda.util.functions.ThrowingFunction;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * This class is for controlling a Femto current amplifier.
 * <p>
 * The Femto amplifies the input current usually in mA or less and outputs a voltage as determined by the gain setting. i.e a <i>1x10^-6 A</i> current with a
 * <i>10^6</i> gain will output <i>1 V</i>
 * <p>
 * The output of Femto usually feeds into ADC which offers the ability to average the voltage over time to improve the signal to
 * noise ratio.
 * <p>
 * The EPICS EDM for these devices are: <br>
 * The Femto <img src="Femto_EDM.png"> <br>
 *
 * @see{@link EpicsBekhoffAdc}
 *
 * @author Fajin Yuan
 */
public class EpicsFemtoAmplifier extends CurrentAmplifierBase implements AmplifierAutoGain, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(EpicsFemtoAmplifier.class);
	private final EpicsController epicsController = EpicsController.getInstance();
	private String basePVName = null;
	private Map<String, Channel> channelMap = new HashMap<>();

	// Femto PV end point string
	private static final String I_INSTANTANEOUS = "I";
	private static final String FEMTO_GAIN = "GAIN";
	private static final String FEMTO_COUPLING = "ACDC";

	boolean supportsCoupling = true; // The I05 femtos don't support this DC only
	private long settleTime = 500; // ms The time to wait after a gain change for stability
	private boolean hasIinPV = true; // some beamline ADC contains extra 3rd part of 'I' in its PVs

	private double lowerVoltageBound = 0; // V. If ADC input is below this increase gain
	private double upperVoltageBound = 10; // V. If ADC input is below this decrease gain

	// Mode, gain, enum string eg. "Low Noise", "10E4", "10^4 low noise"
	private Map<String, Map<String, String>> modeToGainToGainStringMap;
	// The next two maps are inverses
	// enum string, mode map This is used to figure out the mode from EPICS it is filled during configure
	private Map<String, String> gainStringToModeMap;
	private Map<String, String> gainStringToGainMap;
	// This map is used for doing auto-gain within one mode
	private Map<String, List<String>> modeToGainMap;
	private long startTime = System.currentTimeMillis();
	private static final String UNSUPPORTED_OPERATION_MESSAGE = "Femto does not support this operation";

	public enum CouplingMode {
		AC, DC;
	}

	/**
	 * The full PV will be {@link #basePVName} + {@value #I_INSTANTANEOUS} + ":" + pvPostFix
	 *
	 * @param pvPostFix
	 *            The femto post fix required from the class constants
	 * @return The channel associated with the PV requested
	 */
	private Channel getFemtoChannel(String pvPostFix) {
		if (!isHasIinPV())
			return getChannel(pvPostFix);
		return getChannel(I_INSTANTANEOUS + ":" + pvPostFix);
	}

	/**
	 * check all essential bean properties have been set.
	 *
	 * This must be used to specify a custom init-method in an XML bean definition.
	 */
	@Override
	public void afterPropertiesSet() {
		if (basePVName == null) {
			logger.error("No basePVName is set. Check spring configuration!");
			throw new IllegalStateException("No basePVName is set. Check spring configuration!");
		}
		if (modeToGainToGainStringMap == null) {
			logger.error("No modeToGainToGainStringMap is set. Check spring configuration!");
			throw new IllegalStateException("No modeToGainToGainStringMap is set. Check spring configuration!");
		}
	}

	@Override
	public void configure() throws FactoryException {
		logger.trace("configure called");

		// Check if we are already configured
		if (isConfigured()) {
			logger.trace("Already configured");
			return;
		}

		// First verify the Spring configuration
		if (modeToGainToGainStringMap == null) {
			logger.error("Configure called with no modeGainMap. Check spring configuration!");
			throw new IllegalStateException("Configure called with no modeGainMap. Check spring configuration!");
		}

		logger.info("Configuring Femto with base PV: {}", getBasePVName());
		final Set<String> epicsGainStrings = new HashSet<>();
		try {
			// Get the contents of the gain enum from EPICS and check if we have the same values
			epicsGainStrings.addAll(Arrays.asList(epicsController.cagetLabels(getFemtoChannel(FEMTO_GAIN))));

		} catch (Exception e) {
			logger.error("Failed to get gain strings from EPICS current amplifier: {}", getName(), e);
		}

		logger.trace("Creating internal lookup maps");
		gainStringToModeMap = new HashMap<>();
		for (Entry<String, Map<String, String>> mode : modeToGainToGainStringMap.entrySet()) {
			Map<String, String> modeMap = mode.getValue();
			for (String epicsString : modeMap.values()) {
				gainStringToModeMap.put(epicsString, mode.getKey());
			}
		}
		gainStringToGainMap = new HashMap<>();
		for (Map<String, String> modeMap : modeToGainToGainStringMap.values()) {
			for (Entry<String, String> gainEntry : modeMap.entrySet()) {
				gainStringToGainMap.put(gainEntry.getValue(), gainEntry.getKey());
			}
		}
		modeToGainMap = new HashMap<>();
		for (Entry<String, Map<String, String>> mode : modeToGainToGainStringMap.entrySet()) {
			List<String> gainsForMode = new ArrayList<>(mode.getValue().keySet());
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
		setInputNames(new String[] { getName() });
		setExtraNames(new String[] {});

		setConfigured(true);
		logger.info("{}: Finished configuring Femto ", getName());
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		ThrowingConsumer<String> c = this::setGain;
		Async.execute(() -> c.accept(position.toString()));
		startTime = System.currentTimeMillis();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return getGain();
	}

	@Override
	public boolean isBusy() {
		return System.currentTimeMillis() - startTime < settleTime;
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
	@Override
	public void setMode(String mode) throws DeviceException {
		if (!hasMultipleModes()) {
			throw new UnsupportedOperationException("Femto only has one mode");
		}

		logger.debug("Changing mode to: {}", mode);
		// Check if the requested mode is valid
		if (!modeToGainToGainStringMap.containsKey(mode)) {
			throw new IllegalArgumentException("Invalid mode '" + mode + "' Avaliable modes are: " + modeToGainToGainStringMap.keySet());
		}

		// Change mode then perform auto gain to get the best gain setting within the mode
		String lowestGainForMode = modeToGainMap.get(mode).get(0); // Get the lowest gain for this mode
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
	@Override
	public String getMode() throws DeviceException {
		String gainString = getGainString();
		return gainStringToModeMap.get(gainString);
	}

	private String getGainString() throws DeviceException {
		try {
			return epicsController.cagetString(getFemtoChannel(FEMTO_GAIN));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error getting Femto gain", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting Femto gain", e);
		}
	}

	@Override
	public boolean increaseOrDecreaseGain(int indexIncrement) throws DeviceException {
		if (indexIncrement != 1 && indexIncrement != -1) {
			throw new IllegalArgumentException("The index increment must be 1 or -1");
		}

		final String mode = getMode();
		final String gain = getGain();

		final List<String> gainsAvailiable = modeToGainMap.get(mode);
		final int indexOfCurrentGain = gainsAvailiable.indexOf(gain);

		try {
			final String newGain = gainsAvailiable.get(indexOfCurrentGain + indexIncrement);
			setGain(modeToGainToGainStringMap.get(mode).get(newGain));
			return true;
		} catch (IndexOutOfBoundsException e) {
			logger.debug("No higher gains avaliable in '{}' mode", mode, e);
			return false;
		}
	}

	@Override
	public double getInstantaneousVoltage() throws DeviceException {
		logger.trace("getInstantaneousVoltage called");
		try {
			return epicsController.cagetDouble(getChannel(I_INSTANTANEOUS));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error getting instantaneous voltage", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting instantaneous voltage", e);
		}
	}

	@Override
	public String getGain() throws DeviceException {
		String gainString = getGainString();
		return gainStringToGainMap.get(gainString);
	}

	/**
	 * This is the method which actually writes to the gain PV. It should only be passed strings taken from the {@link #getModeToGainToGainStringMap()}
	 *
	 * @param gain
	 */
	@Override
	public void setGain(String gain) throws DeviceException {
		logger.debug("Changing gain to: {}", gain);
		String mode = getMode();

		if (!modeToGainMap.get(mode).contains(gain)) {
			throw new IllegalArgumentException("The reqested gain '" + gain + "' is not avaliable in the current mode '" + mode + "'");
		}

		try {
			epicsController.caputWait(getFemtoChannel(FEMTO_GAIN), gain);
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error setting gain", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting gain", e);
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

	/**
	 * Set the Femto coupling mode, takes a string for Jython to use
	 *
	 * @param couplingMode
	 *            The requested coupling mode
	 * @throws DeviceException
	 */

	public void setCouplingMode(final String couplingMode) throws DeviceException {
		setFemtoCouplingMode(CouplingMode.valueOf(couplingMode));
	}

	public void setFemtoCouplingMode(final CouplingMode couplingMode) throws DeviceException {
		if (!supportsCoupling) {
			throw new UnsupportedOperationException("Femto is configured not to support coupling");
		}

		try {
			epicsController.caput(getFemtoChannel(FEMTO_COUPLING), couplingMode.toString());
		} catch (CAException e) {
			throw new DeviceException("Error setting coupling", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting coupling", e);
		}
		logger.debug("Changed coupling mode to: {}", couplingMode);
	}

	@Override
	public String getCouplingMode() throws DeviceException {
		if (!supportsCoupling) {
			throw new UnsupportedOperationException("Femto is configured not to support coupling");
		}

		try {
			return epicsController.cagetString(getFemtoChannel(FEMTO_COUPLING));
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error getting coupling", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting coupling", e);
		}
	}

	/**
	 * Lazy initialise channels and store them in a map for retrieval later
	 *
	 * @param pvPostFix
	 * @return channel
	 */
	private Channel getChannel(String pvPostFix) {
		String fullPvName = getBasePVName() + pvPostFix;
		ThrowingFunction<String, Channel> f = epicsController::createChannel;
		Channel channel = channelMap.computeIfAbsent(fullPvName, f);
		logger.trace("Created channel for PV: {}", fullPvName);
		return channel;
	}

	@Override
	public boolean hasMultipleModes() {
		return modeToGainToGainStringMap.size() > 1;
	}

	public Map<String, Map<String, String>> getModeToGainToGainStringMap() {
		return modeToGainToGainStringMap;
	}

	public void setModeToGainToGainStringMap(Map<String, Map<String, String>> modeToGainToGainStringMap) {
		this.modeToGainToGainStringMap = modeToGainToGainStringMap;
	}

	@Override
	public boolean isSupportsCoupling() {
		return supportsCoupling;
	}

	public void setSupportsCoupling(boolean supportsCoupling) {
		this.supportsCoupling = supportsCoupling;
	}

	@Override
	public double getLowerVoltageBound() {
		return lowerVoltageBound;
	}

	public void setLowerVoltageBound(double lowerVoltageBound) {
		this.lowerVoltageBound = lowerVoltageBound;
	}

	@Override
	public double getUpperVoltageBound() {
		return upperVoltageBound;
	}

	public void setUpperVoltageBound(double upperVoltageBound) {
		this.upperVoltageBound = upperVoltageBound;
	}

	public long getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(long settleTime) {
		this.settleTime = settleTime;
	}

	public boolean isHasIinPV() {
		return hasIinPV;
	}

	public void setHasIinPV(boolean hasIinPV) {
		this.hasIinPV = hasIinPV;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	@Override
	public String[] getGainPositions() throws DeviceException {
		return gainStringToGainMap.keySet().toArray(new String[] {});
	}

	@Override
	public String[] getGainUnits() throws DeviceException {
		return new String[] { "V" };
	}

	@Override
	public String[] getModePositions() throws DeviceException {
		return modeToGainToGainStringMap.keySet().toArray(new String[] {});
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		throw new UnsupportedOperationException("Gain Unit for Femto is fixed to 'V'");
	}

	@Override
	public String getGainUnit() throws DeviceException {
		return "V/A";
	}

	@Override
	public double getCurrent() throws DeviceException {
		return getInstantaneousVoltage() / Double.parseDouble(getGain());
	}

	@Override
	public Status getStatus() throws DeviceException {
		logger.debug(UNSUPPORTED_OPERATION_MESSAGE);
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public void listGains() throws DeviceException {
		for (String gain : getGainPositions()) {
			InterfaceProvider.getTerminalPrinter().print(gain);
		}
	}
}
