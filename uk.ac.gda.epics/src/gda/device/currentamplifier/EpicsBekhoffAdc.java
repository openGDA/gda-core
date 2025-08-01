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

package gda.device.currentamplifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.StringDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.AmplifierAutoGain;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.util.functions.ThrowingFunction;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.epics.nexus.device.DetectorDataEntry;

public class EpicsBekhoffAdc extends DetectorBase implements NexusDetector {

	private static final String COUNT_TIME = "count_time";
	private static final String MODE = "mode";
	private static final String COUPLING = "coupling";
	private static final String GAIN = "gain";
	private static final Logger logger = LoggerFactory.getLogger(EpicsBekhoffAdc.class);
	private final transient EpicsController epicsController = EpicsController.getInstance();
	protected String basePVName = null;
	private transient Map<String, Channel> channelMap = new HashMap<>();

	// Data PVs Although these are called I they are actually voltages from the amplifier output measured by the ADC
	private static final String I_AVERAGE = "IAV";

	// ADC control PVs
	private static final String ADC_PREFIX = "ADC";
	private static final String ADC_MODE = "MODE";
	private static final String ADC_ENABLE = "ENABLED";
	private static final String ADC_RETRIGGER = "RETRIGGER";
	private static final String ADC_START = "TRIGGER";
	private static final String ADC_SAMPLES = "SAMPLES";
	private static final String ADC_AVERAGE = "AVERAGE"; //on I21 this is the integration time for the averaged value
	private static final String ADC_VALUE = "VALUE"; //on I21 this is the actual averaged value over the specified integration time
	private static final String ADC_STATE = "STATE";
	private static final String ADC_INTEGRAL = "INTEGRAL"; // I21 ADC has INTEGRAL in addition to those above

	private boolean hasIAVinPV = true; // some beamline ADC contains extra 3rd part of 'IAV' in its PVs
	private int adcChannel = 1;
	private int adcSamplingRate = 1000; // Used to convert time into number of samples
	private boolean autoGain = true; // Default to true as this should result in the best behaviour

	// This latch is used when acquiring to block while ADC acquires
	private transient CountDownLatch acquiringLatch = new CountDownLatch(0);

	// This is used to only add scan metadata when required
	private boolean isFirstPoint;

	private AdcMode adcMode;
	private boolean adcEnable;
	private boolean adcRetrigger;
	private int adcSamples;
	private int adcAverage;
	private boolean integrated = false;
	private boolean adcModeEnableRetriggerCached = false;
	private boolean adcSamplesAverageCached = false;
	private transient AmplifierAutoGain amplifier;

	private boolean writeAbsValues = false;

	protected transient HashMap<String,DetectorDataEntry<?>> detectorDataEntryMap = new HashMap<>();
	protected final HashMap<String,Object> dataMapToWrite = new HashMap<>();

	private final Set<String> perScanDetectorData = Set.of(COUPLING,MODE,COUNT_TIME);
	private Set<String>  plottableValueDetectorData = new HashSet<>();

	protected final transient MonitorListener adcStateMonitor = ev -> {
		logger.trace("Received update from ADC state");
		// If the ADC state has changed to waiting its not busy so decrement the latch
		if (((short[]) ev.getDBR().getValue())[0] == 0) {
			logger.trace("ADC is now waiting");
			// Decrement the latch so waitWhileBusy will return if in a scan
			acquiringLatch.countDown();
		}
	};
	public enum AdcMode {
		CONTINUOUS, TRIGGERED, GATED
	}

	protected EpicsBekhoffAdc() {
		super();
	}

	/**
	 * check bean properties have been set.
	 *
	 * This must be used to specify a custom init-method in an XML bean definition.
	 */
	public void afterPropertiesSet() {
		if (basePVName == null) {
			logger.error("No basePVName is set. Check spring configuration!");
			throw new IllegalStateException("No basePVName is set. Check spring configuration!");
		}
		if (amplifier == null) {
			logger.error("No amplifier is set. Check spring configuration!");
			throw new IllegalStateException("No amplifier is set. Check spring configuration!");
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

		// Check the basePv ends with : if not add it
		if (!basePVName.endsWith(":")) {
			logger.debug("basePv didn't end with : adding one");
			basePVName += ":";
		}

		try {
			logger.debug("Adding monitor for ADC State");
			epicsController.setMonitor(getAdcChannel(ADC_STATE), adcStateMonitor);
		} catch (Exception e) {
			logger.error("{}: Failed to add monitor to ADC State", getName(), e);
		}

		// No input names its read-only
		setInputNames(new String[] {});

		if (isWriteAbsValues()) {
			setExtraNames(new String[] {getName(), getName().concat("_abs")});
			setOutputFormat(new String[] {"%5.5g", "%5.5g"});
		} else {
			setExtraNames(new String[] { getName() });
			setOutputFormat(new String[] {"%5.5g"});
		}
		plottableValueDetectorData.addAll(Arrays.asList(getExtraNames()));
		setConfigured(true);
		logger.info("{}: Finished configuring ADC ", getName());
	}

	/**
	 * Lazy initialise channels and store them in a map for retrieval later
	 *
	 * @param pvPostFix
	 * @return channel
	 */
	private Channel getChannel(String pvPostFix) {
		String fullPvName = basePVName + pvPostFix;
		ThrowingFunction<String, Channel> f = epicsController::createChannel;
		Channel channel = channelMap.computeIfAbsent(fullPvName, f);
		logger.trace("Created channel for PV: {}", fullPvName);
		return channel;
	}

	/**
	 * The full PV will be {@link #basePVName} + {@value #I_AVERAGE} + {@value #ADC_PREFIX} + {@link #adcChannel} + "_" + pvPostFix
	 *
	 * @param pvPostFix
	 *            The ADC post fix required from the class constants
	 * @return The channel associated with the PV requested
	 */
	private Channel getAdcChannel(String pvPostFix) {
		if (!isHasIAVinPV())
			return getChannel(ADC_PREFIX + adcChannel + "_" + pvPostFix);
		return getChannel(I_AVERAGE + ":" + ADC_PREFIX + adcChannel + "_" + pvPostFix);
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
		// cache existing settings before applying changes
		adcSamples = getAdcSamples();
		adcAverage = getAdcAverage();
		adcSamplesAverageCached = true;
		// apply new parameters
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
			epicsController.caputWait(getAdcChannel(ADC_SAMPLES), samples);
			epicsController.caputWait(getAdcChannel(ADC_AVERAGE), samples);
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error setting ADC averaging samples", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting ADC averaging samples", e);
		}
		logger.debug("Set ADC samples to: {}", samples);
	}

	@Override
	public void collectData() throws DeviceException {
		logger.trace("collectData called");
		try {
			// Start the ADC acquiring
			epicsController.caput(getAdcChannel(ADC_START), 1);
			logger.trace("Started acquiring");
		} catch (CAException e) {
			throw new DeviceException("Error setting triggering ADC", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting triggering ADC", e);
		}
		// Set the latch this will be decremented by the monitor when the ADC state changes back to WAITING
		acquiringLatch = new CountDownLatch(1);
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		logger.trace("waitWhileBusy called");
		// Wait for the acquisition to complete, will be updated by the CA monitor
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
			return epicsController.cagetString(getAdcChannel(ADC_STATE));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error getting ADC status", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting ADC status", e);
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
		isFirstPoint = true;
		setDetectorDataEntryMap();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		// restore ADC mode to what it was before scan.
		restoreADCMode();
		isFirstPoint = false;
	}

	private void setupAdcForSoftwareTriggering() throws DeviceException {
		adcMode = getAdcMode();
		adcEnable = getAdcEnable();
		adcRetrigger = getAdcRetrigger();
		adcModeEnableRetriggerCached = true;
		setAdcMode(AdcMode.TRIGGERED);
		setAdcEnable(true);
		setAdcRetrigger(true);
	}

	private void restoreADCMode() throws DeviceException {
		if (adcModeEnableRetriggerCached) {
			if (adcMode != null)
				setAdcMode(adcMode);
			setAdcEnable(adcEnable);
			setAdcRetrigger(adcRetrigger);
		}
		if (adcSamplesAverageCached) {
			setAdcSamples(adcSamples);
			setAdcAverage(adcAverage);
		}
		adcModeEnableRetriggerCached = false;
		adcSamplesAverageCached = false;
	}

	@Override
	public void atPointStart() throws DeviceException {
		logger.trace("atPointStart called");
		// Here check and adjust the gain setting if auto-gain is on
		if (autoGain) {
			amplifier.optimiseGain();
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		isFirstPoint = false;
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		restoreADCMode();
		super.atCommandFailure();
	}

	@Override
	public void stop() throws DeviceException {
		isFirstPoint = false;
		restoreADCMode();
		super.stop();
	}

	private double getAverageVoltage() throws DeviceException {
		logger.trace("getAverageVoltage called");
		try {
			if (!isHasIAVinPV())
				return epicsController.cagetDouble(getAdcChannel(ADC_VALUE));
			return epicsController.cagetDouble(getChannel(I_AVERAGE));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error getting average voltage", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting average voltage", e);
		}
	}

	private double getIntegralVoltage() throws DeviceException {
		logger.trace("getIntegralVoltage called");
		try {
			return epicsController.cagetDouble(getAdcChannel(ADC_INTEGRAL));
		} catch (TimeoutException | CAException e) {
			throw new DeviceException("Error getting integral voltage", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting integral voltage", e);
		}
	}

	/**
	 * Calculate the current given the gain
	 *
	 * @param gain
	 * @return
	 * @throws DeviceException
	 */
	private double calculateCurrent(double gain) throws DeviceException {
		// Get the data from EPICS
		final double voltage;
		if (!isIntegrated()) {
			// Default - EPICS driver return averaged value only
			voltage = getAverageVoltage();
		} else {
			// I21 EPICS driver give integrated value
			voltage = getIntegralVoltage();
		}
		// Divide by the gain to change from V back to amps (Gain is in V/A)
		return voltage / gain;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		logger.trace("readout called");
		final double gain = Double.parseDouble(amplifier.getGain());
		final double current = calculateCurrent(gain);

		dataMapToWrite.clear();
		if (detectorDataEntryMap.isEmpty()) setDetectorDataEntryMap();
		if ((getExtraNames().length>0)&&(detectorDataEntryMap.containsKey(getExtraNames()[0]))) dataMapToWrite.put(getExtraNames()[0],Double.valueOf(current));
		if ((getExtraNames().length>1)&&(detectorDataEntryMap.containsKey(getExtraNames()[1]))) dataMapToWrite.put(getExtraNames()[1],Math.abs(current));
		if (detectorDataEntryMap.containsKey(GAIN)) dataMapToWrite.put(GAIN,amplifier instanceof EpicsStanfordAmplifer amp?Double.parseDouble(amp.getGainFromEpics()):gain);

		if (detectorDataEntryMap.containsKey(COUPLING)) dataMapToWrite.put(COUPLING,isFirstPoint?amplifier.getCouplingMode():"");
		if (detectorDataEntryMap.containsKey(MODE)) dataMapToWrite.put(MODE,isFirstPoint?amplifier.getMode():"");
		if (detectorDataEntryMap.containsKey(COUNT_TIME)) dataMapToWrite.put(COUNT_TIME,isFirstPoint?getCollectionTime():0.0);
		setDetectorDataEntryMap(dataMapToWrite);
		//disable per scan monitors for subsequent readouts
		detectorDataEntryMap.values().stream().forEach(entry -> entry.setEnabled(!perScanDetectorData.contains(entry.getName()) || isFirstPoint));
		return getDetectorData();
	}

	@Override
	public NexusTreeProvider getFileStructure() throws DeviceException{
		logger.info("Setting up initial file structure for device \"{}\"", getName());
		setDetectorDataEntryMap();
		return getDetectorData();
	}

	protected void setDetectorDataEntryMap(HashMap<?, ?>... data) throws DeviceException {
		logger.debug("Configuring detectorDataEntryMap with values of length {}", data.length);
		detectorDataEntryMap.clear();
		if (getExtraNames().length>0) {
			detectorDataEntryMap.put(getExtraNames()[0], new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(getExtraNames()[0]),1),getExtraNames()[0],"A",true));
		}
		if (isWriteAbsValues() && getExtraNames().length>1) {
			detectorDataEntryMap.put(getExtraNames()[1], new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(getExtraNames()[1]),1),getExtraNames()[1],"A"));
		}
		if ((amplifier instanceof EpicsFemtoAmplifier) || (amplifier instanceof EpicsStanfordAmplifer)){
			detectorDataEntryMap.put(GAIN, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(GAIN),1),GAIN,amplifier.getGainUnit()));
		}
		if (amplifier.isSupportsCoupling()) {
			detectorDataEntryMap.put(COUPLING, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(COUPLING),1),COUPLING,""));
		}
		if (amplifier.hasMultipleModes()) {
			detectorDataEntryMap.put(MODE, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(MODE),1),MODE,""));
		}
		detectorDataEntryMap.put(COUNT_TIME, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(COUNT_TIME),1),COUNT_TIME,"sec"));

		logger.debug("Configuring detectorDataEntryMap finished");
	}

	private NexusTreeProvider getDetectorData() {
		final NXDetectorData detectorData =  new NXDetectorData(this);
		// add detector data
		for (var e : detectorDataEntryMap.entrySet()) {
			DetectorDataEntry<?> dde = e.getValue();
			if (Boolean.TRUE.equals(e.getValue().isEnabled())) {
				INexusTree data = detectorData.addData(getName(), dde.getName(), new NexusGroupData(dde.getValue()),dde.getUnits(),dde.getIsDetectorEntry());
				if (dde.getName().contains(getName())) {
					data.addChildNode(new NexusTreeNode("local_name",NexusExtractor.AttrClassName, data, new NexusGroupData(String.format("%s.%s", getName(), dde.getName()))));
				}
			}
		}
		// set plottable values
		detectorDataEntryMap.values().stream().filter(entry->plottableValueDetectorData.contains(entry.getName())).forEach(entry->detectorData.setPlottableValue(entry.getName(),entry.getValue().getDouble()));
		return detectorData;
	}

	@Override
	public Object getPosition() throws DeviceException {
		final double gain = Double.parseDouble(amplifier.getGain());
		return calculateCurrent(gain);
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
			epicsController.caput(getAdcChannel(ADC_MODE), adcMode.toString());
		} catch (CAException e) {
			throw new DeviceException("Error setting ADC mode", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting ADC mode", e);
		}
		logger.debug("Changed ADC mode to: {}", adcMode);
	}

	public AdcMode getAdcMode() throws DeviceException {
		try {
			return AdcMode.valueOf(epicsController.cagetString(getAdcChannel(ADC_MODE)));
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error getting ADC mode", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting ADC mode", e);
		}
	}

	public int getAdcSamples() throws DeviceException {
		try {
			return epicsController.cagetInt(getAdcChannel(ADC_SAMPLES));
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error getting ADC Samples", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting ADC Samples", e);
		}
	}

	public void setAdcSamples(final int num) throws DeviceException {
		try {
			epicsController.caputWait(getAdcChannel(ADC_SAMPLES), num);
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error setting ADC Samples", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting ADC Samples", e);
		}
		logger.debug("Changed ADC Samples to: {}", num);
	}

	public int getAdcAverage() throws DeviceException {
		try {
			return epicsController.cagetInt(getAdcChannel(ADC_AVERAGE));
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error getting ADC Average", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting ADC Average", e);
		}
	}

	public void setAdcAverage(final int num) throws DeviceException {
		try {
			epicsController.caputWait(getAdcChannel(ADC_AVERAGE), num);
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error setting ADC Average", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting ADC Average", e);
		}
		logger.debug("Changed ADC Average to: {}", num);
	}

	public void setAdcEnable(final boolean enabled) throws DeviceException {
		try {
			epicsController.caput(getAdcChannel(ADC_ENABLE), enabled ? ADC_ENABLE : "DISABLED");
		} catch (CAException e) {
			throw new DeviceException("Error setting ADC enabled", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting ADC enabled", e);
		}
		logger.debug("Changed ADC enabled to: {}", enabled);
	}

	public boolean getAdcEnable() throws DeviceException {
		try {
			return epicsController.cagetString(getAdcChannel(ADC_ENABLE)).equals(ADC_ENABLE);
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error getting ADC enabled", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting ADC enabled", e);
		}
	}

	public void setAdcRetrigger(final boolean enabled) throws DeviceException {
		try {
			epicsController.caput(getAdcChannel(ADC_RETRIGGER), enabled ? ADC_ENABLE : "DISABLED");
		} catch (CAException e) {
			throw new DeviceException("Error setting ADC retrigger", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error setting ADC retrigger", e);
		}
		logger.debug("Changed ADC enabled to: {}", enabled);
	}

	public boolean getAdcRetrigger() throws DeviceException {
		try {
			return epicsController.cagetString(getAdcChannel(ADC_RETRIGGER)).equals(ADC_ENABLE);
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("Error getting ADC retrigger", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Error getting ADC retrigger");
		}
	}

	@Override
	public String toFormattedString() {
		try {
			// Get the data from EPICS
			final double current = amplifier.getCurrent();
			String gain = amplifier.getGain();
			// Format using the output format for the current
			String string = "%s : " + getOutputFormat()[0] + " A (Gain = %s)";
			return String.format(string, getName(), current, gain);
		} catch (Exception e) {
			logger.error("Error getting {} status", getName(), e);
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
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

	public boolean isAutoGain() {
		return autoGain;
	}

	public void setAutoGain(boolean autoGain) {
		this.autoGain = autoGain;
	}

	public boolean isHasIAVinPV() {
		return hasIAVinPV;
	}

	public void setHasIAVinPV(boolean hasAverage) {
		this.hasIAVinPV = hasAverage;
	}

	public boolean isIntegrated() {
		return integrated;
	}

	public void setIntegrated(boolean integrated) {
		this.integrated = integrated;
	}

	public AmplifierAutoGain getAmplifier() {
		return amplifier;
	}

	public void setAmplifier(AmplifierAutoGain amplifier) {
		this.amplifier = amplifier;
	}

	public boolean isWriteAbsValues() {
		return writeAbsValues;
	}

	public void setWriteAbsValues(boolean writeAbsValues) {
		this.writeAbsValues = writeAbsValues;
	}

}