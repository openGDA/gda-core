/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;

/**
 * Class to implement the ChannelSelect interface for McLennan motion controllers that have multiple channels.
 * 
 * @see McLennanController
 * @see McLennanMotor
 */
public class McLennanChannelSelector implements ChannelSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(McLennanChannelSelector.class);
	
	private IMcLennanController controller;

	private String command;

	private String name;

	private McLennanLockableComponent lc;

	/**
	 * @param controller
	 * @param axis
	 */
	public McLennanChannelSelector(IMcLennanController controller, int axis) {
		lc = new McLennanLockableComponent();

		this.controller = controller;
		logger.debug("McLennanChannelSelector: controller is " + controller);
		this.command = axis + "CH";
		name = controller.getName() + axis;
		logger.debug("McLennanChannelSelector: my name is " + name);
	}

	@Override
	public synchronized boolean selectChannel(Object motor, int channel) throws MotorException {
		boolean rtrn = false;

		logger.debug("in selectChannel");

		if (lc.lockedFor(motor)) {
			logger.debug("McLennanChannelSelector: is already locked for " + motor);
			rtrn = true;
		} else if (lc.lockedFor(null)) {
			logger.debug("McLennanChannelSelector: lockedFor null.");
			lc.lock(motor);
			logger.debug("McLennanChannelSelector: motor " + motor + " has been locked");
			controller.sendCommand(command + channel);
			logger.debug("locked motor + cmd sent");
			rtrn = true;
		}

		return rtrn;
	}

	@Override
	public synchronized void releaseChannel(Object motor) {
		lc.unLock(motor);
	}

	@Override
	public String getName() {
		return name;
	}
}