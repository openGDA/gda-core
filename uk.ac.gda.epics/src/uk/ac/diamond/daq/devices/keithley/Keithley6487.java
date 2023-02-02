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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
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
		NXDetectorData nexusData = new NXDetectorData(this);
		double current = getReading();
		nexusData.setPlottableValue(getExtraNames()[0],  current);
		nexusData.addData(getName(), "current", new NexusGroupData(current), "mA");
		nexusData.addData(getName(), "voltage source value", new NexusGroupData(getVoltageSourceRBV()), "V");

		if (firstReadoutInScan) {
			nexusData.addElement(getName(), "readback_rate", new NexusGroupData(getReadbackRate()), "s", false);
			nexusData.addElement(getName(), "zero_check", new NexusGroupData(getZeroCheckRBV()), "", false);
			nexusData.addElement(getName(), "autorange", new NexusGroupData(getRangeAutoRBV()), "", false);
			nexusData.addElement(getName(), "range", new NexusGroupData(getRange()), "", false);
			nexusData.addElement(getName(), "filter", new NexusGroupData(getFilter()), "", false);
			nexusData.addElement(getName(), "damping", new NexusGroupData(getDamping()), "", false);
			nexusData.addElement(getName(), "damping", new NexusGroupData(getLocalControls()), "", false);
			nexusData.addElement(getName(), "voltage_source_enabled", new NexusGroupData(getVoltageSourceEnabledRBV()), "", false);
			nexusData.addElement(getName(), "voltage_source_reading", new NexusGroupData(getVoltageSourceRBV()), "", false);
			nexusData.addElement(getName(), "voltage_source_setpoint", new NexusGroupData(getVoltageSourceSetpoint()), "", false);
			nexusData.addElement(getName(), "voltage_source_range", new NexusGroupData(getVoltageSourceRange()), "", false);
			nexusData.addElement(getName(), "voltage_source_current_limit", new NexusGroupData(getVoltageSourceILimit()), "", false);
			nexusData.addElement(getName(), "voltage_source_interlock", new NexusGroupData(getVoltageSourceInterlock()), "", false);
			nexusData.addElement(getName(), "voltage_source_interlock", new NexusGroupData(getVoltageSourceInterlockStatus()), "", false);
			nexusData.addElement(getName(), "voltage_source_readback_rate", new NexusGroupData(getVoltageSourceReadbackRate()), "", false);
			firstReadoutInScan = false;
			}
		return nexusData;
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

}