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

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NexusDetector;

/**
 * Dummy version of {@link Keithley2600SeriesAverageMode}.
 */
public class DummyKeithley2600AverageMode extends DummyKeithley2600Series implements NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(DummyKeithley2600AverageMode.class);

	//dummy device settings
	private int points = 10;
	private int interval = 1000;
	private int status = IDLE;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Future<?> runningAcquisition;

	private final Set<String> perScanDetectorData = Set.of(DWELL_TIME,NUMBER_OF_READINGS);

	private double meanVoltage;
	private double meanCurrent;


	@Override
	protected void setupNamesAndFormat() {
		super.setupNamesAndFormat();
		extraNames = (String[]) ArrayUtils.addAll(getExtraNames(), new String[] {getName().concat("_"+MEAN_CURRENT), getName().concat("_"+MEAN_VOLTAGE)});
		outputFormat = (String[]) ArrayUtils.addAll(getOutputFormat(), new String[] {"%5.5g", "%5.5g"});
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		double[] oldPositions = (double[]) super.rawGetPosition();
		return ArrayUtils.addAll(oldPositions, new double[] {meanCurrent, meanVoltage});
	}

	public void setInterval(int demand) {
		logger.debug("{} setting interval time to: {}", getName(), demand);
		this.interval = demand;
	}

	public void setPoints(int demand) {
		logger.debug("{} setting points to: {}", getName(), demand);
		this.points = demand;
	}

	public int getInterval() {
		return interval;
	}

	public int getPoints() {
		return points;
	}

	public double getMeanVoltage() {
		return random.nextDouble();
	}

	public double getMeanCurrent() {
		return random.nextDouble();
	}


	@Override
	public void collectData() throws DeviceException {
		status = BUSY;
		logger.debug("{} Collecting data", getName());
		// Simulate data collection
		runningAcquisition = executorService.submit(() -> {
			try {
				//Simulate collection
				Thread.sleep((long)points * interval);
			} catch (InterruptedException e) {
				logger.debug("Thread was interupted", e);
			}
		});
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		double timeInMilliseconds = time * 1000;
		int dwellTime = (int) (timeInMilliseconds / getPoints());
		setInterval(dwellTime);
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return interval * points;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void endCollection() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Keithley 2600 Series average mode (Dummy)";
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
		meanVoltage = getMeanVoltage();
		meanCurrent = getMeanCurrent();

		dataMapToWrite.clear();
		if (detectorDataEntryMap.isEmpty()) setDetectorDataEntryMap();

		if (detectorDataEntryMap.containsKey(MEAN_CURRENT)) dataMapToWrite.put(MEAN_CURRENT, meanCurrent);
		if (detectorDataEntryMap.containsKey(MEAN_VOLTAGE)) dataMapToWrite.put(MEAN_VOLTAGE, meanVoltage);
		if (detectorDataEntryMap.containsKey(INTEGRATION_TIME)) dataMapToWrite.put(INTEGRATION_TIME, getIntegrationTime());
		if (detectorDataEntryMap.containsKey(RESISTANCE_MODE)) dataMapToWrite.put(RESISTANCE_MODE, getResistanceMode().toEpics());
		if (detectorDataEntryMap.containsKey(SOURCE_MODE)) dataMapToWrite.put(SOURCE_MODE, getSourceMode().toEpics());
		if (detectorDataEntryMap.containsKey(CURRENT_LEVEL_SETPOINT)) dataMapToWrite.put(CURRENT_LEVEL_SETPOINT, getDemandCurrent());
		if (detectorDataEntryMap.containsKey(VOLTAGE_LEVEL_SETPOINT)) dataMapToWrite.put(VOLTAGE_LEVEL_SETPOINT, getDemandVoltage());
		if (detectorDataEntryMap.containsKey(DWELL_TIME)) dataMapToWrite.put(DWELL_TIME, isFirstPoint? 1:0);
		if (detectorDataEntryMap.containsKey(NUMBER_OF_READINGS)) dataMapToWrite.put(NUMBER_OF_READINGS, isFirstPoint?1:0);
		setDetectorDataEntryMap(dataMapToWrite);

		//disable per scan monitors for subsequent readouts
		detectorDataEntryMap.values().stream().forEach(entry -> entry.setEnabled(!(perScanDetectorData.contains(entry.getName()) && !(isFirstPoint))));
		return getDetectorData();
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
