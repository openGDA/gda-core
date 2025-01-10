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

package uk.ac.diamond.daq.devices.keithley;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.IntStream;

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
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.event.PutListener;
import uk.ac.gda.epics.nexus.device.DetectorDataEntry;

public class Keithley2600SeriesSweepMode extends Keithley2600Series implements NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(Keithley2600SeriesSweepMode.class);

	private static final String VOLTAGE_STOP = "voltage_stop";
	private static final String VOLTAGE_START = "voltage_start";
	private static final String CURRENTS_ARRAY = "currents_array";
	private static final String CURRENT_STOP = "current_stop";
	private static final String CURRENT_START = "current_start";
	private static final String VOLTAGES_ARRAY = "voltages_array";
	private static final String RESISTANCE_MODE2 = "resistance_mode";
	private static final String TIME_OFF = "time_off";
	private static final String TIME_ON = "time_on";
	private static final String NUMBER_OF_PULSES = "number_of_pulses";
	private static final String PULSES_ARRAY = "pulses_array";

	private static final String SW_PERIOD = "SwPeriod";
	private static final String SW_PULSES = "SwNPulses";
	private static final String SW_TIME_ON = "SwTmOn";
	private static final String SW_TIME_OFF = "SwTmOff";
	private static final String SW_VOLTAGE_START = "SwVStart";
	private static final String SW_VOLTAGE_STOP = "SwVStop";
	private static final String SW_CURRENT_START = "SwIStart";
	private static final String SW_CURRENT_STOP = "SwIStop";
	private static final String SW_RUN_ERROR = "SwErrRun";
	private static final String SW_CONF_ERROR = "SwErrConf";
	private static final String SW_ERROR_MSG = "SwErrStr";
	private static final String SW_ACQUIRE = "SwAcquire";
	private static final String SW_DATA = "SwData";

	private int status = IDLE;

	private final Set<String> perScanDetectorData = Set.of(DWELL_TIME,NUMBER_OF_READINGS);

	@Override
	public void configure() throws FactoryException {
		logger.debug("configure Keithley called for {}",getName());
		if (isConfigured()) {
			logger.debug("Already configured Keithley {}", getName());
			return;
		}
		//Eliminate errors related to setting input names on a detector
		setInputNames(new String[0]);

		// First verify the Spring configuration
		if (getBasePVName() == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new IllegalStateException("Configure called with no basePVName. Check spring configuration!");
		}

		// Check the basePv ends with : if not add it
		if (!getBasePVName().endsWith(":")) {
			logger.debug("basePv didn't end with : adding one");
			setBasePVName(getBasePVName() + ":");
		}
		logger.info("Configuring Keithley with base PV: {}", getBasePVName());

		setExtraNames(new String[] { "demand", "voltage", "current", "resistance" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g" });

		setConfigured(true);
		logger.info("Finished configuring Keithley '{}'", getName());
	}

	@Override
	public String toFormattedString() {
		String statusString;
		if (status == IDLE) {
			statusString = "IDLE";
		} else {
			statusString = "BUSY";
		}
		return getName() + " : " + statusString;
	}

	public void setPulses(int value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_PULSES), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep pulses to: " + value, e);
		}
	}

	private int getPulses() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(SW_PULSES));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep pulses", e);
		}
	}

	public void setTimeOn(double value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_TIME_ON), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep time on to: " + value, e);
		}
	}

	private double getTimeOn() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_TIME_ON));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep time on", e);
		}
	}

	public void setTimeOff(double value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_TIME_OFF), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep time off to: " + value, e);
		}
	}

	private double getTimeOff() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_TIME_OFF));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep time on", e);
		}
	}

	public void setSweepVoltageStart(double value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_VOLTAGE_START), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep start voltage to: " + value, e);
		}
	}

	private double getSweepVoltageStart() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_VOLTAGE_START));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep voltage start", e);
		}
	}

	public void setSweepVoltageStop(double value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_VOLTAGE_STOP), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep stop voltage to: " + value, e);
		}
	}

	private double getSweepVoltageStop() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_VOLTAGE_STOP));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep voltage stop", e);
		}
	}

	public void setSweepCurrentStart(double value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_CURRENT_START), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep start current to: " + value, e);
		}
	}

	private double getSweepCurrentStart() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_CURRENT_START));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep current start", e);
		}
	}

	public void setSweepCurrentStop(double value) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(SW_CURRENT_STOP), value);
		} catch (Exception e) {
			throw new DeviceException("Failed to set sweep stop current to: " + value, e);
		}
	}

	private double getSweepCurrentStop() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_CURRENT_STOP));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep current stop", e);
		}
	}

	public boolean hasRunError() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_RUN_ERROR)) == 1;
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep mode run error", e);
		}
	}

	public boolean hasConfError() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_CONF_ERROR)) == 1;
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep mode conf error", e);
		}
	}

	public String getSweepErrorMessage() throws DeviceException {
		try {
			return epicsController.cagetString(getChannel(SW_ERROR_MSG));
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep mode error message", e);
		}
	}

	/**
	 * Use this method to get only a slice of the array
	 *
	 * @param size
	 * @throws DeviceException
	 */
	private double[] getData(int size) throws DeviceException {
		try {
			return epicsController.cagetDoubleArray(getChannel(SW_DATA), size);
		} catch (Exception e) {
			throw new DeviceException("Failed to get sweep mode data", e);
		}
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while(getStatus() == BUSY) {
			Thread.sleep(1000);
		}
	}

	PutListener sweepComplete = event -> {
		if (event.getStatus() == CAStatus.NORMAL) {
			logger.debug("{}: Sweep completed", getName());
		} else {
			logger.error("Sweep failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event.getStatus());
		}
		status = IDLE;
	};

	@Override
	public void collectData() throws DeviceException {
		status = BUSY;

		outputOn();

		try {
			epicsController.caput(getChannel(SW_ACQUIRE), "Acquire", sweepComplete);
		} catch (CAException e) {
			status = IDLE;
			throw new DeviceException("An error occured while initiating sweep mode", e);
		} catch (InterruptedException e) {
			status = IDLE;
			Thread.currentThread().interrupt();
			throw new DeviceException("Sweep mode was interrupted.", e);
		}

	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		// do nothing - collection time defined from pulses and timeOn, timeOff
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(SW_PERIOD));
		} catch (Exception exception) {
			throw new DeviceException("Failed to get sweep period", exception);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		// nothing needed

	}

	@Override
	public void endCollection() throws DeviceException {
		// nothing needed

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Keithley 2600 Series";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "sourceMeter";
	}

	public NexusTreeProvider readoutOriginal() throws DeviceException {
		NXDetectorData nexusData = new NXDetectorData(this);

		int pulses = getPulses();
		int [] pulsesAxis = IntStream.rangeClosed(1, pulses).toArray();
		nexusData.addData(getName(), PULSES_ARRAY, new NexusGroupData(pulsesAxis), "Pulses");
		nexusData.addData(getName(), NUMBER_OF_PULSES, new NexusGroupData(pulses));

		nexusData.addData(getName(), TIME_ON, new NexusGroupData(getTimeOn()), "s");
		nexusData.addData(getName(), TIME_OFF, new NexusGroupData(getTimeOff()), "s");
		nexusData.addData(getName(), RESISTANCE_MODE2, new NexusGroupData(getResistanceMode().toEpics()));
		nexusData.addData(getName(), INTEGRATION_TIME, new NexusGroupData(getIntegrationTime()), "ms");

		SourceMode sourceMode = getSourceMode();
		nexusData.addData(getName(), SOURCE_MODE, new NexusGroupData(sourceMode.toEpics()));

		if (sourceMode == SourceMode.CURRENT) {
			nexusData.addData(getName(), VOLTAGES_ARRAY, new NexusGroupData(getData(pulses)), "V");
			nexusData.addData(getName(), CURRENT_START, new NexusGroupData(getSweepCurrentStart()), "A");
			nexusData.addData(getName(), CURRENT_STOP, new NexusGroupData(getSweepCurrentStop()), "A");
			nexusData.addData(getName(), CURRENT_LEVEL_SETPOINT, new NexusGroupData(getDemandCurrent()), "A");
		} else if (sourceMode == SourceMode.VOLTAGE) {
			nexusData.addData(getName(), CURRENTS_ARRAY, new NexusGroupData(getData(pulses)), "A");
			nexusData.addData(getName(), VOLTAGE_START, new NexusGroupData(getSweepVoltageStart()), "V");
			nexusData.addData(getName(), VOLTAGE_STOP, new NexusGroupData(getSweepVoltageStop()), "V");
			nexusData.addData(getName(), VOLTAGE_LEVEL_SETPOINT, new NexusGroupData(getDemandVoltage()), "V");
		}

		return nexusData;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		int pulses = getPulses();
		int [] pulsesAxis = IntStream.rangeClosed(1, pulses).toArray();

		dataMapToWrite.clear();
		if (detectorDataEntryMap.isEmpty()) setDetectorDataEntryMap();

		if (detectorDataEntryMap.containsKey(PULSES_ARRAY)) dataMapToWrite.put(PULSES_ARRAY, pulsesAxis);
		if (detectorDataEntryMap.containsKey(NUMBER_OF_PULSES)) dataMapToWrite.put(NUMBER_OF_PULSES, pulses);
		if (detectorDataEntryMap.containsKey(TIME_ON)) dataMapToWrite.put(TIME_ON, getTimeOn());
		if (detectorDataEntryMap.containsKey(TIME_OFF)) dataMapToWrite.put(TIME_OFF, getTimeOff());
		if (detectorDataEntryMap.containsKey(RESISTANCE_MODE)) dataMapToWrite.put(RESISTANCE_MODE, getResistanceMode().toEpics());
		if (detectorDataEntryMap.containsKey(INTEGRATION_TIME)) dataMapToWrite.put(INTEGRATION_TIME, getIntegrationTime());
		if (detectorDataEntryMap.containsKey(SOURCE_MODE)) dataMapToWrite.put(SOURCE_MODE, getSourceMode().toEpics());
		if (detectorDataEntryMap.containsKey(VOLTAGES_ARRAY)) dataMapToWrite.put(VOLTAGES_ARRAY, getData(pulses));
		if (detectorDataEntryMap.containsKey(CURRENTS_ARRAY)) dataMapToWrite.put(CURRENTS_ARRAY, getData(pulses));
		if (detectorDataEntryMap.containsKey(CURRENT_START)) dataMapToWrite.put(CURRENT_START, getSweepCurrentStart());
		if (detectorDataEntryMap.containsKey(VOLTAGE_START)) dataMapToWrite.put(VOLTAGE_START, getSweepVoltageStart());
		if (detectorDataEntryMap.containsKey(CURRENT_STOP)) dataMapToWrite.put(CURRENT_STOP, getSweepCurrentStop());
		if (detectorDataEntryMap.containsKey(VOLTAGE_STOP)) dataMapToWrite.put(VOLTAGE_STOP, getSweepVoltageStop());
		if (detectorDataEntryMap.containsKey(CURRENT_LEVEL_SETPOINT)) dataMapToWrite.put(CURRENT_LEVEL_SETPOINT, getDemandCurrent());
		if (detectorDataEntryMap.containsKey(VOLTAGE_LEVEL_SETPOINT)) dataMapToWrite.put(VOLTAGE_LEVEL_SETPOINT, getDemandVoltage());

		setDetectorDataEntryMap(dataMapToWrite);
		//disable per scan monitors for subsequent readouts
		detectorDataEntryMap.values().stream().forEach(entry -> entry.setEnabled(!(perScanDetectorData.contains(entry.getName()) && !(isFirstPoint))));
		return getDetectorData();
	}

	@Override
	protected void setDetectorDataEntryMap(HashMap<?, ?>... data) throws DeviceException {
		logger.debug("Configuring detectorDataEntryMap with values of length {}", data.length);
		detectorDataEntryMap.clear();
		detectorDataEntryMap.put(PULSES_ARRAY, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 0):DatasetFactory.createFromObject(IntegerDataset.class,data[0].get(PULSES_ARRAY),((int[]) data[0].get(PULSES_ARRAY)).length),PULSES_ARRAY,"Pulses"));
		detectorDataEntryMap.put(NUMBER_OF_PULSES, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 1):DatasetFactory.createFromObject(IntegerDataset.class,data[0].get(NUMBER_OF_PULSES),1),NUMBER_OF_PULSES,""));
		detectorDataEntryMap.put(TIME_ON, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(TIME_ON),1),TIME_ON,"s"));
		detectorDataEntryMap.put(TIME_OFF, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(TIME_OFF),1),TIME_OFF,"s"));
		detectorDataEntryMap.put(RESISTANCE_MODE, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(RESISTANCE_MODE),1),RESISTANCE_MODE,""));
		detectorDataEntryMap.put(INTEGRATION_TIME, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(INTEGRATION_TIME),1),INTEGRATION_TIME,"ms"));
		detectorDataEntryMap.put(SOURCE_MODE, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(SOURCE_MODE),1),SOURCE_MODE,""));

		SourceMode sourceMode = getSourceMode();
		if (sourceMode == SourceMode.CURRENT) {
			detectorDataEntryMap.put(VOLTAGES_ARRAY, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 0):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(VOLTAGES_ARRAY),((double[]) data[0].get(VOLTAGES_ARRAY)).length),VOLTAGES_ARRAY,"V",true));
			detectorDataEntryMap.put(CURRENT_START, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENT_START),1),CURRENT_START,"A"));
			detectorDataEntryMap.put(CURRENT_STOP, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENT_STOP),1),CURRENT_STOP,"A"));
			detectorDataEntryMap.put(CURRENT_LEVEL_SETPOINT, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENT_LEVEL_SETPOINT),1),CURRENT_LEVEL_SETPOINT,"A"));
		} else if (sourceMode == SourceMode.VOLTAGE) {
			detectorDataEntryMap.put(CURRENTS_ARRAY, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 0):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENTS_ARRAY),((double[]) data[0].get(CURRENTS_ARRAY)).length),CURRENTS_ARRAY,"A",true));
			detectorDataEntryMap.put(VOLTAGE_START, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(VOLTAGE_START),1),VOLTAGE_START,"V"));
			detectorDataEntryMap.put(VOLTAGE_STOP, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(VOLTAGE_STOP),1),VOLTAGE_STOP,"V"));
			detectorDataEntryMap.put(VOLTAGE_LEVEL_SETPOINT, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(VOLTAGE_LEVEL_SETPOINT),1),VOLTAGE_LEVEL_SETPOINT,"V"));
		}
		logger.debug("Configuring detectorDataEntryMap finished");
	}


	@Override
	protected NexusTreeProvider getDetectorData() {
		final NXDetectorData detectorData =  new NXDetectorData(this);
		// add detector data
		detectorDataEntryMap.values().stream().filter(entry-> entry.isEnabled()).forEach(entry -> detectorData.addData(getName(), entry.getName(), new NexusGroupData(entry.getValue()),entry.getUnits(),entry.getIsDetectorEntry()));
		// set plottable values
		return detectorData;
	}
}
