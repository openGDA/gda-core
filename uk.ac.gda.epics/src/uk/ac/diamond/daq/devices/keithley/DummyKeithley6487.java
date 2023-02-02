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

public class DummyKeithley6487 extends AbstractKeithley6400Series{

	private static final Logger logger = LoggerFactory.getLogger(DummyKeithley6487.class);
	private DummyKeithley6487Controller controller;


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
		NXDetectorData nexusData = new NXDetectorData(this);
		double current = getReading();
		nexusData.setPlottableValue(getExtraNames()[0],  current);
		nexusData.addData(getName(), "current", new NexusGroupData(current), "mA");

	if (firstReadoutInScan) {
		nexusData.addElement(getName(), "integration_rate", new NexusGroupData(getReadbackRate()), "s", false);
		firstReadoutInScan = false;
		}
		return nexusData;
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
