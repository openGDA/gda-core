/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import gda.device.detector.NexusDetector;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.concurrent.Async.ListeningFuture;
import uk.ac.gda.epics.nexus.device.DetectorDataEntry;

public abstract class AbstractKeithley6400Series extends ScannableBase implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(AbstractKeithley6400Series.class);

	protected long settleTimeMs = 1;

	private String basePVName = null;

	private int status=IDLE;
	private int waitWhileBusySleepTime=100;

	protected boolean isFirstPoint;

	protected Future<?> setting = null;

	private ListeningFuture<?> collectionDelay;

	private long cachedCollectionTimeMs;

	public abstract double getCollectionTimeS() throws DeviceException;

	public abstract String getReadbackRate() throws DeviceException;

	public abstract void setReadbackRate(double demand) throws DeviceException;

	public abstract double getReading() throws DeviceException;

	protected abstract boolean isDisabled() throws DeviceException;

	protected final ArrayList<DetectorDataEntry<?>> detectorDataEntryList = new ArrayList<>();
	protected HashMap<String,DetectorDataEntry<?>> detectorDataEntryMap = new HashMap<>();
	protected final HashMap<String,Object> dataMapToWrite = new HashMap<>();
	protected Set<String> plottableValueDetectorData = new HashSet<>();

	protected static final String DAMPING = "damping";
	protected static final String VOLTAGE_SOURCE_VALUE = "voltage source value";
	protected static final String CURRENT = "current";
	protected static final String VOLTAGE_SOURCE_READBACK_RATE = "voltage_source_readback_rate";
	protected static final String VOLTAGE_SOURCE_INTERLOCK_STATUS = "voltage_source_interlock status";
	protected static final String VOLTAGE_SOURCE_INTERLOCK = "voltage_source_interlock";
	protected static final String VOLTAGE_SOURCE_CURRENT_LIMIT = "voltage_source_current_limit";
	protected static final String VOLTAGE_SOURCE_RANGE = "voltage_source_range";
	protected static final String VOLTAGE_SOURCE_SETPOINT = "voltage_source_setpoint";
	protected static final String VOLTAGE_SOURCE_READING = "voltage_source_reading";
	protected static final String VOLTAGE_SOURCE_ENABLED = "voltage_source_enabled";
	protected static final String LOCAL_CONTROLS = "local controls";
	protected static final String FILTER = "filter";
	protected static final String RANGE = "range";
	protected static final String AUTORANGE = "autorange";
	protected static final String ZERO_CHECK = "zero_check";
	protected static final String READBACK_RATE = "readback_rate";

	protected final Set<String> perScanDetectorData = Set.of(READBACK_RATE,
															ZERO_CHECK,
															AUTORANGE,
															RANGE,
															FILTER,
															DAMPING,
															LOCAL_CONTROLS,
															VOLTAGE_SOURCE_ENABLED,
															VOLTAGE_SOURCE_READING,
															VOLTAGE_SOURCE_SETPOINT,
															VOLTAGE_SOURCE_RANGE,
															VOLTAGE_SOURCE_CURRENT_LIMIT,
															VOLTAGE_SOURCE_INTERLOCK,
															VOLTAGE_SOURCE_INTERLOCK_STATUS,
															VOLTAGE_SOURCE_READBACK_RATE);

	private final Map<String,String> stringDataFields = Map.ofEntries(
			entry(READBACK_RATE,"s"),
			entry(ZERO_CHECK,""),
			entry(AUTORANGE,""),
			entry(RANGE,""),
			entry(FILTER,""),
			entry(DAMPING,""),
			entry(LOCAL_CONTROLS,""),
			entry(VOLTAGE_SOURCE_ENABLED,""),
			entry(VOLTAGE_SOURCE_RANGE,""),
			entry(VOLTAGE_SOURCE_CURRENT_LIMIT,""),
			entry(VOLTAGE_SOURCE_INTERLOCK,""),
			entry(VOLTAGE_SOURCE_INTERLOCK_STATUS,""),
			entry(VOLTAGE_SOURCE_READBACK_RATE,""));

	private final Map<String,String> doubleDataFields = Map.ofEntries(
			entry(CURRENT,"mA"),
			entry(VOLTAGE_SOURCE_VALUE,"V"),
			entry(VOLTAGE_SOURCE_READING,""),
			entry(VOLTAGE_SOURCE_SETPOINT,""));

	private final Map<String,String> integerDataFields = Map.ofEntries();

	public String getBasePVName() {
		return basePVName;
		}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
		}

	/** Set readback rate here instead of exposure time in scan command "scan detector exposure" */
	@Override
	public void setCollectionTime(double time) throws DeviceException {
		setReadbackRate(time);
	}

	/* Simply get some dummy period (note, not exposure time nor readback rate) after which can read data */
	@Override
	public double getCollectionTime() throws DeviceException {
		return getCollectionTimeS();
	}

	/** Return readback rate and current in a "pos" command */
	@Override
	public Object rawGetPosition() throws DeviceException {
		// input names
		String rr = getReadbackRate();
		// extra names
		String cc = Double.toString(getReading());
		return new String[] {rr, cc};
	}

	@Override
	public void collectData() throws DeviceException {
		setStatus(BUSY);
		// there is no "acquire" command, using some dummy collection time
		logger.debug("{}: Initiating acquisition", getName());
		// collectData must be non-blocking
		collectionDelay = Async.schedule(()-> setStatus(IDLE), cachedCollectionTimeMs, TimeUnit.MILLISECONDS).onFailure(this::failure);
		}

	protected void failure(Throwable t) {
		logger.error("Stop acquire command after error", t);
		setStatus(IDLE);
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (status == BUSY) {
			Thread.sleep(getWaitWhileBusySleepTime());
		}
		super.waitWhileBusy();
		logger.debug("Acquisition for {} finished", getName());
	}

	protected void waitForSettling() {
		logger.debug("Waiting for settling...");
		try {
			Thread.sleep(getSettleTimeMs());
			// if (isFirstPoint) { Thread.sleep(getAdditionalFirstPointSettleTimeMs()); }
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted waiting for settling", e);
		}
	}

	protected void setOutputDemandAndWaitToSettle(double demand) {
		// Set readback rate when used with "pos" command - called from rawAsynchronousMoveTo
		try {
			setCollectionTime(demand);
		} catch (DeviceException e) {
			throw new RuntimeException("Failed to set readback rate", e);
		}
		waitForSettling();
	}

	protected void setupNamesAndFormat() {
		setInputNames(new String[] { "readback_rate" });
		setExtraNames(new String[] { "current" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g" });
		plottableValueDetectorData.addAll(Arrays.asList(getExtraNames()));

	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		double demand = PositionConvertorFunctions.toDouble(position);
		setting = Async.submit(() -> setOutputDemandAndWaitToSettle(demand));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (setting != null){
			return !setting.isDone() || status == BUSY;
			}
		return status == BUSY;
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		isFirstPoint = true;
		cachedCollectionTimeMs = (long) (getCollectionTime()*1000);
		setDetectorDataEntryMap();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		isFirstPoint = false;
		if (collectionDelay!=null) {
			collectionDelay.cancel(true);
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		isFirstPoint = false;
	}

	@Override
	public void stop() throws DeviceException {
		if (collectionDelay!=null) {
			collectionDelay.cancel(true);
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		if (collectionDelay!=null) {
			collectionDelay.cancel(true);
		}
	}

	@Override
	public String getDescription() {
		return "Keithley 6487 as NXDetector";
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		// pass
	}

	@Override
	public void endCollection() throws DeviceException {
		// pass
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getSettleTimeMs() {
		return settleTimeMs;
	}

	public void setSettleTimeMs(long settleTimeMs) {
		this.settleTimeMs = settleTimeMs;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}


	public int getWaitWhileBusySleepTime() {
		return waitWhileBusySleepTime;
	}

	public void setWaitWhileBusySleepTime(int waitWhileBusySleepTime) {
		this.waitWhileBusySleepTime = waitWhileBusySleepTime;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Keithley 6487";
	}

	@Override
	public String toFormattedString() {
		try {
			return ScannableUtils.getFormattedCurrentPosition(this);
		} catch (Exception e) {
			logger.error("Error getting {} status", getName(), e);
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
	}

	@Override
	public NexusTreeProvider getFileStructure() throws DeviceException{
		logger.info("Setting up initial file structure for device \"{}\"", getName());
		setDetectorDataEntryMap();
		return getDetectorData();
	}

	protected void setDetectorDataEntryMap(HashMap<?, ?>... data) {
		logger.debug("Configuring detectorDataEntryMap with values of length {}", data.length);
		detectorDataEntryMap.clear();
		integerDataFields.entrySet().stream().forEach(entry->detectorDataEntryMap.put(entry.getKey(), new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 1):DatasetFactory.createFromObject(IntegerDataset.class,data[0].get(entry.getKey()),1),entry.getKey(),entry.getValue())));
		doubleDataFields.entrySet().stream().forEach(entry->detectorDataEntryMap.put(entry.getKey(), new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(entry.getKey()),1),entry.getKey(),entry.getValue())));
		stringDataFields.entrySet().stream().forEach(entry->detectorDataEntryMap.put(entry.getKey(), new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(entry.getKey()),1),entry.getKey(),entry.getValue())));
		// set detector entry
		detectorDataEntryMap.get(CURRENT).setIsDetectorEntry(true);
		logger.debug("Configuring detectorDataEntryMap finished");

	}

	protected NexusTreeProvider getDetectorData() {
		final NXDetectorData detectorData =  new NXDetectorData(this);
		// add detector data
		detectorDataEntryMap.values().stream().filter(entry-> entry.isEnabled()).forEach(entry -> detectorData.addData(getName(), entry.getName(), new NexusGroupData(entry.getValue()),entry.getUnits(),entry.getIsDetectorEntry()));
		//set plottable values
		detectorDataEntryMap.values().stream().filter(entry->plottableValueDetectorData.contains(entry.getName())).forEach(entry->detectorData.setPlottableValue(entry.getName(),entry.getValue().getDouble()));
		return detectorData;
	}

	protected Set<String> getPlottableValueDetectorData() {
		return plottableValueDetectorData;
	}
}
