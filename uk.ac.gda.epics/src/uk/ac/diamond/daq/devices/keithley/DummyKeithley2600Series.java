/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import java.util.Random;

import gda.device.DeviceException;

/**
 * Dummy version of {@link Keithley2600Series}. Just caches settings that would be set to EPICS and returns random data
 *
 * @author James Mudd
 * @since GDA 9.11
 */
public class DummyKeithley2600Series extends AbstractKeithley2600Series {

	SourceMode sourceMode = SourceMode.VOLTAGE;
	ResistanceMode resistanceMode = ResistanceMode.TWO_WIRE;

	boolean outputOn = false;
	double demand = 1.0;
	int dwellTime = 20;
	int numberOfReadings = 500;

	Random random = new Random();

	@Override
	public SourceMode getSourceMode() {
		return sourceMode;
	}

	@Override
	protected void setSourceMode(SourceMode mode) throws DeviceException {
		this.sourceMode = mode;
	}

	@Override
	public ResistanceMode getResistanceMode() throws DeviceException {
		return resistanceMode;
	}

	@Override
	public void setResistanceMode(ResistanceMode resistanceMode) {
		this.resistanceMode = resistanceMode;
	}

	@Override
	protected double getActualResistance() throws DeviceException {
		return random.nextDouble();
	}

	@Override
	protected double getActualCurrent() throws DeviceException {
		return random.nextDouble();
	}

	@Override
	protected double getActualVoltage() throws DeviceException {
		return random.nextDouble();
	}

	@Override
	protected double getDemandCurrent() throws DeviceException {
		return demand;
	}

	@Override
	protected double getDemandVoltage() throws DeviceException {
		return demand;
	}

	@Override
	protected void setOutputDemandAndWaitToSettle(double demand) {
		this.demand = demand;
		waitForSettling();
	}

	@Override
	protected void outputOff() throws DeviceException {
		outputOn = false;
	}

	@Override
	protected void outputOn() throws DeviceException {
		outputOn = true;
		waitForSwitchOn();
	}

	@Override
	public boolean isOutputOn() throws DeviceException {
		return outputOn;
	}
}
