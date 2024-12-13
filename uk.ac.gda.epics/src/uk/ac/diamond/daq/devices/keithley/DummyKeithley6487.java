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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.factory.FactoryException;

public class DummyKeithley6487 extends AbstractKeithley6400Series{
	private static final Logger logger = LoggerFactory.getLogger(DummyKeithley6487.class);
	private DummyKeithley6487Controller controller;

	// plottableValueDetectorData must also be in extraNames, otherwise double data is not added

	protected final ArrayList<Object> dataToWrite = new ArrayList<>();

	public DummyKeithley6487(DummyKeithley6487Controller controller) {
		super();
		this.controller = controller;
	}

	@Override
	public void configure() throws FactoryException {
		controller.configure();
		try {
			if (isDisabled()) {
				logger.warn("'{}' Is disabled and will not be configured", getName());
				return;
			}
			setupNamesAndFormat();
		} catch (Exception e) {
			logger.error("Failed to configure: {}", getName(), e);
		}
		setConfigured(true);
		logger.info("Finished configuring '{}'", getName());
	}


	@Override
	public NexusTreeProvider readout() throws DeviceException {
		double current = getReading();
		dataMapToWrite.clear();
		if (detectorDataEntryMap.isEmpty()) setDetectorDataEntryMap();
		if (detectorDataEntryMap.containsKey(CURRENT)) dataMapToWrite.put(CURRENT, current);
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_VALUE)) dataMapToWrite.put(VOLTAGE_SOURCE_VALUE, current*10);

		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_READING)) dataMapToWrite.put(VOLTAGE_SOURCE_READING, isFirstPoint? current/10:0.0);
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_SETPOINT)) dataMapToWrite.put(VOLTAGE_SOURCE_SETPOINT, isFirstPoint?current/15:0.0);
		if (detectorDataEntryMap.containsKey(READBACK_RATE)) dataMapToWrite.put(READBACK_RATE, isFirstPoint? getReadbackRate():"");
		if (detectorDataEntryMap.containsKey(ZERO_CHECK)) dataMapToWrite.put(ZERO_CHECK, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(AUTORANGE)) dataMapToWrite.put(AUTORANGE, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(RANGE)) dataMapToWrite.put(RANGE, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(FILTER)) dataMapToWrite.put(FILTER, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(DAMPING)) dataMapToWrite.put(DAMPING, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(LOCAL_CONTROLS)) dataMapToWrite.put(LOCAL_CONTROLS, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_ENABLED)) dataMapToWrite.put(VOLTAGE_SOURCE_ENABLED, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_RANGE)) dataMapToWrite.put(VOLTAGE_SOURCE_RANGE, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_CURRENT_LIMIT)) dataMapToWrite.put(VOLTAGE_SOURCE_CURRENT_LIMIT, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_INTERLOCK)) dataMapToWrite.put(VOLTAGE_SOURCE_INTERLOCK, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_INTERLOCK_STATUS)) dataMapToWrite.put(VOLTAGE_SOURCE_INTERLOCK_STATUS, isFirstPoint? getName():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_READBACK_RATE)) dataMapToWrite.put(VOLTAGE_SOURCE_READBACK_RATE, isFirstPoint? getName():"");
		setDetectorDataEntryMap(dataMapToWrite);
		//disable per scan monitors for subsequent readouts
		detectorDataEntryMap.values().stream().forEach(entry -> entry.setEnabled(!(perScanDetectorData.contains(entry.getName()) && !(isFirstPoint))));
		return getDetectorData();
	}

	@Override
	public double getReading() throws DeviceException {
		return controller.getReading();
	}

	@Override
	public void setReadbackRate(double readbackrate) throws DeviceException {
		controller.setReadbackRate(readbackrate);
	}

	@Override
	public String getReadbackRate() throws DeviceException {
		return controller.getReadbackRate();
	}

	@Override
	protected boolean isDisabled() throws DeviceException {
		return controller.isDisabled();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Dummy Keithley 6487";
	}

	@Override
	public double getCollectionTimeS() throws DeviceException {
		return controller.getCollectionTimeS();
	}
}
