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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;

public class DummyKeithley2600SweepMode extends DummyKeithley2600Series implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(DummyKeithley2600SweepMode.class);

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Future runningAcquisition;

	private double dummyPeriod = 10000;
	private double[] dummyData = new double[] {-2, 0, 2, -2, 0, 2, -2, 0, 2, -2, 0, 2};
	private double[] dummyPulses = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	private int status = IDLE;


	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		//Eliminate errors related to setting input names on a detector
		setInputNames(new String[0]);
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
		NXDetectorData data = new NXDetectorData(this);

		if (getSourceMode() == SourceMode.CURRENT) {
			data.addData(getName(), "voltages array", new NexusGroupData(dummyData), "V");
		} else if (getSourceMode() == SourceMode.VOLTAGE) {
			data.addData(getName(), "currents array", new NexusGroupData(dummyData), "A");
		}
		data.addData(getName(), "pulses array", new NexusGroupData(dummyPulses), "Pulses");

		return data;
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
