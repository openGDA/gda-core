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

import gda.device.DeviceException;
import gda.device.serial.SerialController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SlaveCommandThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(SlaveCommandThread.class);

	String reply = "";
	String command;
	String errorString;
	SerialController serialController;

	/**
	 *
	 */
	public SlaveCommandThread() {
		super("commandThread");
		setDaemon(true);
	}

	/**
	 * @param serialController
	 * @param command
	 * @param errorString
	 */
	public SlaveCommandThread(SerialController serialController, String command, String errorString) {
		this();
		this.command = command;
		this.serialController = serialController;
		this.errorString = errorString;
	}

	@Override
	public void run() {
		logger.debug("Sending slave command: " + this.command);
		try {
			if (serialController != null && command != null) {
				serialController.sendCommand(command);
				reply = serialController.getReply();

				logger.debug("Slave reply = " + reply);
				if (reply.equals("") || reply.startsWith(errorString))
					throw new DeviceException(" Error returned from Slave or Slave Timeout: " + reply);
			} else
				throw new DeviceException("null serial controller or command");
		}

		catch (DeviceException e) {
			logger.error("Error running slave command '{}'", command, e);
		}
	}
}
