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

package uk.ac.gda.devices.tenma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.tenma.api.IPsu722931Controller;

public class DummyPsu722931Controller implements IPsu722931Controller {

	private static final Logger logger = LoggerFactory.getLogger(DummyPsu722931Controller.class);

	private double current = 0.5;
	private double voltage = 5;
	private boolean outputIsOn = false;

	@Override
	public double getCurrent() {
		logger.debug("Current requested, returning {}", current);
		return current;
	}

	@Override
	public void setCurrent(double current) {
		logger.debug("Current set to {}", current);
		this.current = current;
	}

	@Override
	public double getVoltage() {
		logger.debug("Voltage requested, returning {}", voltage);
		return voltage;
	}

	@Override
	public void setVoltage(double voltage) {
		logger.debug("Voltage set to {}", voltage);
		this.voltage = voltage;
	}

	@Override
	public void outputOn() {
		logger.debug("Output turned on.");
		outputIsOn = true;
	}

	@Override
	public void outputOff() {
		logger.debug("Output turned off.");
		outputIsOn = false;
	}

	@Override
	public boolean outputIsOn() {
		logger.debug("Output status requested, returning {}", outputIsOn);
		return outputIsOn;
	}
}
