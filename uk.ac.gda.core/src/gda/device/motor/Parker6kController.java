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

import gda.device.DeviceBase;
import gda.device.MotorException;
import gda.factory.Configurable;
import gda.factory.Findable;

/**
 * Used by Parker6kMotor as the point of communication with the actual hardware. Base class to be enclosed within a
 * Parker6kMotor object as either an RS232 or Ethernet serial communications subclass instance.
 * 
 * @see Parker6kMotor
 * @see Parker6kControllerEnet
 * @see Parker6kControllerRS232
 */
public abstract class Parker6kController extends DeviceBase implements Findable, Configurable {
	
	private static final Logger logger = LoggerFactory.getLogger(Parker6kController.class);
	
	private int controllerNo = 0;

	private int maxNoOfMotors;

	/**
	 * Address of controller (set as default or by RS232 daisychain). Used by Castor for instantiation.
	 * 
	 * @param controllerNo
	 *            controller number
	 */
	public void setControllerNo(int controllerNo) {
		this.controllerNo = controllerNo;
	}

	/**
	 * Return the address of this controller. Used by Castor for instantiation.
	 * 
	 * @return the address of the controller
	 */
	public int getControllerNo() {
		return controllerNo;
	}

	/**
	 * max motors controlled, 2, 4 or 8 per-axis positions and speeds are stored to allow reply simulation. Used by
	 * Castor for instantiation.
	 * 
	 * @param maxNoOfMotors
	 *            is the maximum number of motors this controller can support
	 */
	public void setMaxNoOfMotors(int maxNoOfMotors) {
		this.maxNoOfMotors = maxNoOfMotors;
	}

	/**
	 * Returns the maximum number of motors this controller will support Used by Castor for instantiation.
	 * 
	 * @return the maximum number of motors this controller will support.
	 */
	public int getMaxNoOfMotors() {
		return maxNoOfMotors;
	}

	/**
	 * Subclass will provide a concrete Ethernet or RS232 i/o method.
	 * 
	 * @param command
	 *            parker command to send
	 * @return a reply string
	 * @throws MotorException
	 */
	public abstract String sendCommand(String command) throws MotorException;

	/**
	 * Utility method to print out all debug information about this class.
	 */
	public void debug() {
		logger.debug(getClass().getName() + " : name          : " + getName());
		logger.debug(getClass().getName() + " : controllerNo  : " + getControllerNo());
		logger.debug(getClass().getName() + " : maxNoOfMotors : " + getMaxNoOfMotors());
	}

	/**
	 * dummy tidyup method to be provided by base classes
	 */
	public abstract void tidyup();
}