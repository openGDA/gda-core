/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.timer.Etfg;
import gda.jython.InterfaceProvider;

/**
 * Action to change a tfg idle state to a given value
 * <br>
 * states: 1 => idle high, 0 => idle low
 */
public class TfgIdleStateChange extends BaseNcdAction {
	private static final Logger logger = LoggerFactory.getLogger(TfgIdleStateChange.class);

	private Etfg tfg;
	private int channel;
	private int state;

	@Override
	public void run() {
		if (tfg == null) {
			logger.error("{}: TFG not set -> can't set idle state", getName());
			return;
		}
		int currentInversion = 0;
		Object att = tfg.getAttribute(Etfg.INVERSION);
		try {
			currentInversion = (int)att;
		} catch (ClassCastException cce) {
			logger.warn("{}: Received unexpected inversion attribute from tfg: {}. Expected int", getName(), att);
		}
		// get the state of this channel's bit
		int current = (currentInversion >> channel) & 1;
		// and do nothing if it's correct already
		if (current == state) {
			logger.trace("{}: Not changing idle state of channel {}", getName(), channel);
			return;
		}
		// set the state of this channel's bit to state
		int newInversion = currentInversion ^ (-state ^ currentInversion) & (1 << channel);
		try {
			logger.trace("{}: Setting tfg inversion to {}", getName(), newInversion);
			tfg.setAttribute(Etfg.INVERSION, newInversion);
		} catch (DeviceException e) {
			logger.error("{}: Could not set inversion to {}", getName(), newInversion, e);
			InterfaceProvider.getTerminalPrinter().print("WARNING: Could not set tfg output triggers");
		}
	}

	/**
	 * The TFG to set on which to set the output trigger state
	 * @param tfg
	 */
	public void setTfg(Etfg tfg) {
		if (tfg == null) {
			throw new IllegalArgumentException(getName() + ": Timer must not be null");
		}
		this.tfg = tfg;
	}

	/**
	 * Set the channel that should be changed when this action is run.
	 * @param channel to set. Should be in range 0-7 (inc)
	 */
	public void setChannel(int channel) {
		if (channel < 0 || channel > 7) {
			throw new IllegalArgumentException(getName() + ": Channel must be 0-7, not " + channel);
		}
		this.channel = channel;
	}

	/**
	 * Set the state to set the channel to when this action is run
	 *
	 * @param state 1 is idle high, 0 is idle low
	 */
	public void setState(int state) {
		this.state = state;
	}

}
