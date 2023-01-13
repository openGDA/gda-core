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

package uk.ac.gda.devices.pressurecell.controller.dummy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.pressurecell.controller.ArmablePressureValve;

public class DummyArmablePressureValve implements ArmablePressureValve {
	private static final Logger logger = LoggerFactory.getLogger(DummyArmablePressureValve.class);

	private ArmedValveState state = ArmedValveState.CLOSED;

	@Override
	public ArmedValveState getState() {
		return state;
	}

	@Override
	public void open() {
		logger.debug("Opening valve");
		state = ArmedValveState.OPEN;
	}

	@Override
	public void close() {
		logger.debug("Closing valve");
		state = ArmedValveState.CLOSED;
	}

	@Override
	public void reset() {
		logger.debug("Resetting valve");
	}

	@Override
	public void arm() {
		switch (state) {
		case CLOSED:
			state = ArmedValveState.CLOSED_ARMED;
			break;
		case OPEN:
			state = ArmedValveState.OPEN_ARMED;
			break;
		case FAULT:
			throw new IllegalStateException("Can't arm valve. Valve in fault state");
		default:
			logger.info("Valve already armed");
		}
	}

	@Override
	public void disarm() {
		switch (state) {
		case CLOSED_ARMED:
			state = ArmedValveState.CLOSED;
			break;
		case OPEN_ARMED:
			state = ArmedValveState.OPEN;
			break;
		case FAULT:
			throw new IllegalStateException("Can't disarm valve. Valve in fault state");
		default:
			logger.info("Valve already disarmed");
		}
	}
}
