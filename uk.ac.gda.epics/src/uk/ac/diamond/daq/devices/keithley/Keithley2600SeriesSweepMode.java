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

import java.util.stream.IntStream;

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

public class Keithley2600SeriesSweepMode extends Keithley2600Series implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(Keithley2600SeriesSweepMode.class);

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

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData nexusData = new NXDetectorData(this);

		int pulses = getPulses();
		int [] pulsesAxis = IntStream.rangeClosed(1, pulses).toArray();
		nexusData.addData(getName(), "pulses_array", new NexusGroupData(pulsesAxis), "Pulses");
		nexusData.addData(getName(), "number_of_pulses", new NexusGroupData(pulses));

		nexusData.addData(getName(), "time_on", new NexusGroupData(getTimeOn()), "s");
		nexusData.addData(getName(), "time_off", new NexusGroupData(getTimeOff()), "s");
		nexusData.addData(getName(), "resistance_mode", new NexusGroupData(getResistanceMode().toEpics()));
		nexusData.addData(getName(), "integration_time", new NexusGroupData(getIntegrationTime()), "ms");

		SourceMode sourceMode = getSourceMode();
		nexusData.addData(getName(), "source_mode", new NexusGroupData(sourceMode.toEpics()));

		if (sourceMode == SourceMode.CURRENT) {
			nexusData.addData(getName(), "voltages_array", new NexusGroupData(getData(pulses)), "V");
			nexusData.addData(getName(), "current_start", new NexusGroupData(getSweepCurrentStart()), "A");
			nexusData.addData(getName(), "current_stop", new NexusGroupData(getSweepCurrentStop()), "A");
			nexusData.addData(getName(), "current_level_setpoint", new NexusGroupData(getDemandCurrent()), "A");
		} else if (sourceMode == SourceMode.VOLTAGE) {
			nexusData.addData(getName(), "currents_array", new NexusGroupData(getData(pulses)), "A");
			nexusData.addData(getName(), "voltage_start", new NexusGroupData(getSweepVoltageStart()), "V");
			nexusData.addData(getName(), "voltage_stop", new NexusGroupData(getSweepVoltageStop()), "V");
			nexusData.addData(getName(), "voltage_level_setpoint", new NexusGroupData(getDemandVoltage()), "V");
		}

		return nexusData;
	}

}
