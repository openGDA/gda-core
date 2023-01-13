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

import uk.ac.gda.devices.pressurecell.controller.PressureValve;

public class DummyPressureValve implements PressureValve {
	private static final Logger logger = LoggerFactory.getLogger(DummyPressureValve.class);

	private ValveState state = ValveState.CLOSED;
	private String name;

	@Override
	public ValveState getState() {
		return state;
	}

	@Override
	public void open() {
		logger.debug("{} - Opening valve", name);
		state = ValveState.OPEN;
	}

	@Override
	public void close() {
		logger.debug("{} - Closing valve", name);
		state = ValveState.CLOSED;
	}

	@Override
	public void reset() {
		logger.debug("{} - Resetting valve", name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
