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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;
import uk.ac.gda.epics.nexus.device.DetectorDataEntry;

public class DummyKeithley2600SweepMode extends DummyKeithley2600Series implements NexusDetector {

	private static final String PULSES_ARRAY = "pulses array";

	private static final String CURRENTS_ARRAY = "currents array";

	private static final String VOLTAGES_ARRAY = "voltages array";

	private static final Logger logger = LoggerFactory.getLogger(DummyKeithley2600SweepMode.class);

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Future runningAcquisition;

	private double dummyPeriod = 10000;
	private double[] dummyData = new double[] {-2, 0, 2, -2, 0, 2, -2, 0, 2, -2, 0, 2};
	private double[] dummyPulses = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	private int status = IDLE;
	private final Set<String> perScanDetectorData = Set.of();

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		//Eliminate errors related to setting input names on a detector
		setInputNames(new String[0]);
		setExtraNames(new String[] { "demand", "voltage", "current", "resistance" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g" });
		setConfigured(true);
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

	@Override
	public void collectData() throws DeviceException {
		status = BUSY;
		logger.debug("{} Collecting data", getName());
		// Simulate data collection
		runningAcquisition = executorService.submit(() -> {
			try {
				Thread.sleep((long)dummyPeriod);
			} catch (InterruptedException e) {
				logger.debug("Thread was interupted", e);
			}
		});

	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		// This detector does not simply sets collection time
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return dummyPeriod;
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
		// Do nothing

	}

	@Override
	public void endCollection() throws DeviceException {
		// Do nothing

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Keithley 2600 Series sweep mode (Dummy)";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "SourceMeter";
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		dataMapToWrite.clear();
		if (detectorDataEntryMap.isEmpty()) setDetectorDataEntryMap();
		if (detectorDataEntryMap.containsKey(PULSES_ARRAY)) dataMapToWrite.put(PULSES_ARRAY, dummyPulses);
		if (detectorDataEntryMap.containsKey(RESISTANCE_MODE)) dataMapToWrite.put(RESISTANCE_MODE, getResistanceMode().toEpics());
		if (detectorDataEntryMap.containsKey(INTEGRATION_TIME)) dataMapToWrite.put(INTEGRATION_TIME, getIntegrationTime());
		if (detectorDataEntryMap.containsKey(SOURCE_MODE)) dataMapToWrite.put(SOURCE_MODE, getSourceMode().toEpics());
		if (detectorDataEntryMap.containsKey(VOLTAGES_ARRAY)) dataMapToWrite.put(VOLTAGES_ARRAY, dummyData);
		if (detectorDataEntryMap.containsKey(CURRENTS_ARRAY)) dataMapToWrite.put(CURRENTS_ARRAY, dummyData);
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
		detectorDataEntryMap.put(PULSES_ARRAY, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 0):DatasetFactory.createFromObject(IntegerDataset.class,data[0].get(PULSES_ARRAY),dummyPulses.length),PULSES_ARRAY,"Pulses"));
		detectorDataEntryMap.put(RESISTANCE_MODE, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(RESISTANCE_MODE),1),RESISTANCE_MODE,""));
		detectorDataEntryMap.put(INTEGRATION_TIME, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(INTEGRATION_TIME),1),INTEGRATION_TIME,"ms"));
		detectorDataEntryMap.put(SOURCE_MODE, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(StringDataset.class, 1):DatasetFactory.createFromObject(StringDataset.class,data[0].get(SOURCE_MODE),1),SOURCE_MODE,""));

		SourceMode sourceMode = getSourceMode();
		if (sourceMode == SourceMode.CURRENT) {
			detectorDataEntryMap.put(VOLTAGES_ARRAY, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 0):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(VOLTAGES_ARRAY),dummyData.length),VOLTAGES_ARRAY,"V", true));
			detectorDataEntryMap.put(CURRENT_LEVEL_SETPOINT, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENT_LEVEL_SETPOINT),1),CURRENT_LEVEL_SETPOINT,"A"));
		} else if (sourceMode == SourceMode.VOLTAGE) {
			detectorDataEntryMap.put(CURRENTS_ARRAY, new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, 0):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(CURRENTS_ARRAY),dummyData.length),CURRENTS_ARRAY,"A",true));
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

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (!runningAcquisition.isDone()) {
			Thread.sleep(1000);
		}
		status = IDLE;
		logger.debug("Acquisition for {} finished", getName());
	}


}
