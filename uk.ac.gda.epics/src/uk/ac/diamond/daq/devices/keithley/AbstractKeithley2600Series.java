/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.keithley;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.StringDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.epics.nexus.device.DetectorDataEntry;

/**
 * Abstract base class for Keithley 2600 Series source meter.
 *
 * @author James Mudd
 * @since GDA 9.11
 */
public abstract class AbstractKeithley2600Series extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(AbstractKeithley2600Series.class);

	protected static final String SOURCE_MODE = "source_mode";
	protected static final String MEAN_CURRENT = "mean_current";
	protected static final String MEAN_VOLTAGE = "mean_voltage";
	protected static final String VOLTAGE_LEVEL_SETPOINT = "voltage_level_setpoint";
	protected static final String CURRENT_LEVEL_SETPOINT = "current_level_setpoint";
	protected static final String INTEGRATION_TIME = "integration_time";
	protected static final String RESISTANCE_MODE = "resistance_mode";
	protected static final String NUMBER_OF_READINGS = "number_of_readings";
	protected static final String DWELL_TIME = "dwell_time";

	protected HashMap<String,DetectorDataEntry<?>> detectorDataEntryMap = new HashMap<>();
	protected final HashMap<String,Object> dataMapToWrite = new HashMap<>();

	private final Map<String,String> stringDataFields = Map.ofEntries(
			entry(RESISTANCE_MODE,""),
			entry(SOURCE_MODE,""));

	private final Map<String,String> doubleDataFields = Map.ofEntries(
			entry(MEAN_CURRENT,"A"),
			entry(MEAN_VOLTAGE,"V"),
			entry(INTEGRATION_TIME,"ms"));

	private final Map<String,String> integerDataFields = Map.ofEntries(
			entry(DWELL_TIME,"ms"),
			entry(NUMBER_OF_READINGS,"ms"));

	/** The time to wait after a output change for stability in ms */
	private long settleTimeMs = 1100;
	/** The time to wait after a output change for stability in ms */
	private long switchOnDelayTimeMs = 4000;
	/** The additional time to wait for settling on the first point in mss
	 * As settling has been observed to sometimes take longer here. */
	private long additionalFirstPointSettleTimeMs = 0;

	/** The status showing if the voltage or current limits are reached */
	protected Status limitStatus = Status.NORMAL;

	/** Future to block on while settling */
	protected Future<?> setting;

	/** Flag indicating the next time {@link #rawAsynchronousMoveTo(Object)} is called the output will be switched on */
	protected boolean switchOnAtNextMove;

	/** Flag indicating whether we are on the first point */
	protected boolean isFirstPoint;

	protected enum SourceMode {
		/** The source will target a voltage setpoint by varying current */
		VOLTAGE("DCVOLTS"),
		/** The source will target a current setpoint by varying voltage */
		CURRENT("DCAMPS");

		private static final Map<String, SourceMode> LOOKUP = Arrays.asList(SourceMode.values()).stream().collect(toMap(k -> k.toEpics(), k -> k));

		private final String epicsName;

		private SourceMode(String epicsName) {
			this.epicsName = epicsName;
		}

		public String toEpics() {
			return epicsName;
		}

		protected static SourceMode fromEpics(String mode) {
			return LOOKUP.get(mode);
		}
	}

	protected enum ResistanceMode {
		TWO_WIRE("2-wire"), FOUR_WIRE("4-wire");

		private static final Map<String, ResistanceMode> LOOKUP = Arrays.asList(ResistanceMode.values()).stream().collect(toMap(k -> k.toEpics(), k -> k));

		private final String epicsName;

		private ResistanceMode(String epicsName) {
			this.epicsName = epicsName;
		}

		public String toEpics() {
			return epicsName;
		}

		protected static ResistanceMode fromEpics(String mode) {
			return LOOKUP.get(mode);
		}
	}

	protected enum Status {
		// Order is important ordinals must match EPICS enum
		NORMAL, VOLTAGE_LIMIT, CURRENT_LIMIT;

		// Cache values for efficiency
		private static final Status[] values = Status.values();

		/**
		 * Convert from EPICS enum ordinal to {@link Status}
		 *
		 * @param value
		 *            (ordinal)
		 * @return the MotorStatus instance corresponding to value
		 */
		public static Status fromEpicsInt(int value) {
			return values[value];
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setupNamesAndFormat();
		setConfigured(true);
	}

	public Set<SourceMode> getSourceModes() {
		return EnumSet.allOf(SourceMode.class);
	}

	public Set<ResistanceMode> getResistanceModes() {
		return EnumSet.allOf(ResistanceMode.class);
	}

	@Override
	public String toFormattedString() {
		try {
			// Custom to add mode
			return ScannableUtils.getFormattedCurrentPosition(this) + " mode: " + getSourceMode();
		} catch (Exception e) {
			logger.error("Error getting {} status", getName(), e);
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
	}

	protected abstract SourceMode getSourceMode() throws DeviceException;

	protected void waitForSettling() {
		logger.debug("Waiting for settling...");
		try {
			Thread.sleep(getSettleTime());
			if (isFirstPoint) {
				Thread.sleep(getAdditionalFirstPointSettleTimeMs());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted waiting for settling", e);
		}
	}

	protected void waitForSwitchOn() {
		logger.debug("Waiting for switch on...");
		try {
			Thread.sleep(getSwitchOnDelayTimeMs());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted waiting for switch on", e);
		}
	}

	/**
	 * The idea is to all this whenever an current or voltage limit might be reached. If it is this will throw aborting the ongoing request or scan.
	 *
	 * @throws DeviceException
	 */
	protected void checkStatusAndThrow() throws DeviceException {
		if (limitStatus != Status.NORMAL) {
			throw new DeviceException(getName() + " is at " + limitStatus);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// Demand (input names)
		double demand = getDemand();
		// Read backs (extra names)
		double voltage = getActualVoltage();
		double current = getActualCurrent();
		double resistance = getActualResistance();

		return new double[] { demand, voltage, current, resistance };
	}

	protected abstract double getActualResistance() throws DeviceException;

	protected abstract double getActualCurrent() throws DeviceException;

	protected abstract double getActualVoltage() throws DeviceException;

	protected double getDemand() throws DeviceException {
		SourceMode sourceMode = getSourceMode();
		switch (sourceMode) {
		case VOLTAGE:
			return getDemandVoltage();
		case CURRENT:
			return getDemandCurrent();
		default:
			throw new IllegalStateException("Unkown SourceMode: " + sourceMode);
		}
	}

	protected abstract double getDemandCurrent() throws DeviceException;

	protected abstract double getDemandVoltage() throws DeviceException;

	protected void setupNamesAndFormat() {
		setInputNames(new String[] { "demand" });
		setExtraNames(new String[] { "voltage", "current", "resistance" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g" });
	}

	@Override
	public boolean isBusy() throws DeviceException {
		checkStatusAndThrow();
		if (setting != null) {
			return !setting.isDone();
		}
		else {
			return false;
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		checkStatusAndThrow();
		double demand = PositionConvertorFunctions.toDouble(position);
		setting = Async.submit(() -> setOutputDemandAndWaitToSettle(demand));
	}

	protected abstract void setOutputDemandAndWaitToSettle(double demand);

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		logger.trace("waitWhileBusy called");
		checkStatusAndThrow();
		if (setting != null) {
			try {
				setting.get(); // Waits for settling to finish
			} catch (Exception e) {
				throw new DeviceException("Exception waiting for settling", e);
			}
		}
		checkStatusAndThrow();
		logger.trace("Settling finshed");
	}

	protected abstract void outputOff() throws DeviceException;

	protected abstract void outputOn() throws DeviceException;

	@Override
	public void stop() throws DeviceException {
		isFirstPoint = false;
	}

	public void setSourceMode(String mode) throws DeviceException {
		setSourceMode(SourceMode.valueOf(mode.toUpperCase()));
	}

	protected abstract void setSourceMode(SourceMode mode) throws DeviceException;

	public void setResistanceMode(String mode) throws DeviceException {
		setResistanceMode(ResistanceMode.valueOf(mode.toUpperCase()));
	}

	protected abstract void setResistanceMode(ResistanceMode mode) throws DeviceException;

	@Override
	public void atScanStart() throws DeviceException {
		isFirstPoint = true;
		switchOnAtNextMove = true;
		setDetectorDataEntryMap();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		isFirstPoint = false;
	}

	public abstract ResistanceMode getResistanceMode() throws DeviceException;

	public abstract boolean isOutputOn() throws DeviceException;

	/**
	 * @return The delay applied after the demand is changed.
	 */
	public long getSettleTime() {
		return settleTimeMs;
	}

	/**
	 * @param settleTime
	 *            The delay to be applied after the demand is changed.
	 */
	public void setSettleTime(long settleTime) {
		this.settleTimeMs = settleTime;
	}

	/**
	 * @return The delay applied after the source is switched on.
	 */
	public long getSwitchOnDelayTimeMs() {
		return switchOnDelayTimeMs;
	}

	/**
	 * @param switchOnDelayTimeMs
	 *            The delay applied after the source is switched on.
	 */
	public void setSwitchOnDelayTimeMs(long switchOnDelayTimeMs) {
		this.switchOnDelayTimeMs = switchOnDelayTimeMs;
	}

	/**
	 * The additional delay to be applied on the first point of the scan
	 * after setting the demand.	 *
	 */
	public long getAdditionalFirstPointSettleTimeMs() {
		return additionalFirstPointSettleTimeMs;
	}

	/**
	 * The additional delay to be applied on the first point of the scan
	 * after setting the demand.
	 *
	 * @param additionalFirstPointSettleTimeMs
	 */
	public void setAdditionalFirstPointSettleTimeMs(long additionalFirstPointSettleTimeMs) {
		this.additionalFirstPointSettleTimeMs = additionalFirstPointSettleTimeMs;
	}

	public NexusTreeProvider getFileStructure() throws DeviceException{
		logger.info("Setting up initial file structure for device \"{}\"", getName());
		setDetectorDataEntryMap();
		return getDetectorData();
	}

	protected void setDetectorDataEntryMap(HashMap<?, ?>... data) throws DeviceException {
		logger.debug("Configuring detectorDataEntryMap with values of length {}", data.length);
		detectorDataEntryMap.clear();
		integerDataFields.entrySet().stream().forEach(entry->detectorDataEntryMap.put(entry.getKey(), new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 1):DatasetFactory.createFromObject(IntegerDataset.class,data[0].get(entry.getKey()),1),entry.getKey(),entry.getValue())));
		doubleDataFields.entrySet().stream().forEach(entry->detectorDataEntryMap.put(entry.getKey(), new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(entry.getKey()),1),entry.getKey(),entry.getValue())));
		stringDataFields.entrySet().stream().forEach(entry->detectorDataEntryMap.put(entry.getKey(), new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(entry.getKey()),1),entry.getKey(),entry.getValue())));
		SourceMode sourceMode = getSourceMode();
		if (sourceMode == SourceMode.CURRENT) {
			detectorDataEntryMap.put(CURRENT_LEVEL_SETPOINT, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENT_LEVEL_SETPOINT),1),CURRENT_LEVEL_SETPOINT,"A"));
		} else if (sourceMode == SourceMode.VOLTAGE) {
			detectorDataEntryMap.put(VOLTAGE_LEVEL_SETPOINT, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(VOLTAGE_LEVEL_SETPOINT),1),VOLTAGE_LEVEL_SETPOINT,"V"));
		}
		// set detector entry
		detectorDataEntryMap.get(MEAN_CURRENT).setIsDetectorEntry(true);

		logger.debug("Configuring detectorDataEntryMap finished");
	}

	protected NexusTreeProvider getDetectorData() {
		final NXDetectorData detectorData =  new NXDetectorData(this);
		// add detector data
		detectorDataEntryMap.values().stream().filter(entry-> entry.isEnabled()).forEach(entry -> detectorData.addData(getName(), entry.getName(), new NexusGroupData(entry.getValue()),entry.getUnits(),entry.getIsDetectorEntry()));
		// set plottable values
		if (detectorDataEntryMap.containsKey(CURRENT_LEVEL_SETPOINT)) {
			detectorData.setPlottableValue(getName().concat("_"+MEAN_VOLTAGE), detectorDataEntryMap.get(MEAN_VOLTAGE).getValue().getDouble());
		} else if (detectorDataEntryMap.containsKey(VOLTAGE_LEVEL_SETPOINT)) {
			detectorData.setPlottableValue(getName().concat("_"+MEAN_CURRENT), detectorDataEntryMap.get(MEAN_CURRENT).getValue().getDouble());
		}
		return detectorData;
	}
}
