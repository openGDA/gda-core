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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.factory.FactoryException;

public class Keithley6487 extends AbstractKeithley6400Series {
	private static final Logger logger = LoggerFactory.getLogger(Keithley6487.class);
	private Keithley6487Controller controller;



	public Keithley6487(Keithley6487Controller controller) {
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
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_VALUE)) dataMapToWrite.put(VOLTAGE_SOURCE_VALUE, getVoltageSourceRBV());

		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_READING)) dataMapToWrite.put(VOLTAGE_SOURCE_READING, isFirstPoint? getVoltageSourceRBV():0.0);
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_SETPOINT)) dataMapToWrite.put(VOLTAGE_SOURCE_SETPOINT, isFirstPoint? getVoltageSourceSetpoint():0.0);
		if (detectorDataEntryMap.containsKey(READBACK_RATE)) dataMapToWrite.put(READBACK_RATE, isFirstPoint? getReadbackRate():"");
		if (detectorDataEntryMap.containsKey(ZERO_CHECK)) dataMapToWrite.put(ZERO_CHECK, isFirstPoint? getZeroCheckRBV():"");
		if (detectorDataEntryMap.containsKey(AUTORANGE)) dataMapToWrite.put(AUTORANGE, isFirstPoint? getRangeAutoRBV():"");
		if (detectorDataEntryMap.containsKey(RANGE)) dataMapToWrite.put(RANGE, isFirstPoint? getRange():"");
		if (detectorDataEntryMap.containsKey(FILTER)) dataMapToWrite.put(FILTER, isFirstPoint? getFilter():"");
		if (detectorDataEntryMap.containsKey(DAMPING)) dataMapToWrite.put(DAMPING, isFirstPoint? getDamping():"");
		if (detectorDataEntryMap.containsKey(LOCAL_CONTROLS)) dataMapToWrite.put(LOCAL_CONTROLS, isFirstPoint? getLocalControls():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_ENABLED)) dataMapToWrite.put(VOLTAGE_SOURCE_ENABLED, isFirstPoint? getVoltageSourceEnabledRBV():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_RANGE)) dataMapToWrite.put(VOLTAGE_SOURCE_RANGE, isFirstPoint? getVoltageSourceRange():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_CURRENT_LIMIT)) dataMapToWrite.put(VOLTAGE_SOURCE_CURRENT_LIMIT, isFirstPoint? getVoltageSourceILimit():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_INTERLOCK)) dataMapToWrite.put(VOLTAGE_SOURCE_INTERLOCK, isFirstPoint? getVoltageSourceInterlock():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_INTERLOCK_STATUS)) dataMapToWrite.put(VOLTAGE_SOURCE_INTERLOCK_STATUS, isFirstPoint? getVoltageSourceInterlockStatus():"");
		if (detectorDataEntryMap.containsKey(VOLTAGE_SOURCE_READBACK_RATE)) dataMapToWrite.put(VOLTAGE_SOURCE_READBACK_RATE, isFirstPoint? getVoltageSourceReadbackRate():"");

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
	protected boolean isDisabled() throws DeviceException {
		return controller.isDisabled();
	}

	@Override
	public void setReadbackRate(double readbackrate) throws DeviceException {
		controller.setReadbackRate(readbackrate);
	}

	@Override
	public String getReadbackRate() throws DeviceException {
		return controller.getReadbackRate();
	}

	private String getDamping() throws DeviceException {
		return controller.getDamping();
	}

	private String getFilter() throws DeviceException {
		return controller.getFilter();
	}

	private String getRange() throws DeviceException {
		return controller.getRange();
	}

	private String getRangeAutoRBV() throws DeviceException {
		return controller.getRangeAutoRBV();
	}

	private String getZeroCheckRBV() throws DeviceException {
		return controller.getZeroCheckRBV();
	}

	private String getVoltageSourceInterlock() throws DeviceException {
		return controller.getVoltageSourceInterlock();
	}

	private String getVoltageSourceILimit() throws DeviceException {
		return controller.getVoltageSourceILimit();
	}

	private String getVoltageSourceRange() throws DeviceException {
		return controller.getVoltageSourceRange();
	}

	private double getVoltageSourceSetpoint() throws DeviceException {
		return controller.getVoltageSourceSetpoint();
	}

	private double getVoltageSourceRBV() throws DeviceException {
		return controller.getVoltageSourceRBV();
	}

	private String getVoltageSourceEnabledRBV() throws DeviceException {
		return controller.getVoltageSourceEnabledRBV();
	}

	private String getVoltageSourceReadbackRate() throws DeviceException{
		return controller.getVoltageReadbackRate();
	}

	private String getVoltageSourceInterlockStatus() throws DeviceException {
		return controller.getVoltageSourceInterlockStatus();
	}

	private String getLocalControls() throws DeviceException {
		return controller.getLocalControls();
	}

	@Override
	public double getCollectionTimeS() throws DeviceException {
		return controller.getCollectionTimeS();
	}

	@Override
	protected Set<String> getPlottableValueDetectorData() {
		return plottableValueDetectorData;
	}

}