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
import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.Serial;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.Finder;

/**
 * Single point of access to a shared Serial connection for PI piezo controllers.
 * 
 * @see PIMotor
 */
public class PIControllerC862 extends DeviceBase {
	
	private static final Logger logger = LoggerFactory.getLogger(PIControllerC862.class);
	
	private static final int READ_TIMEOUT = 5000;

	private static final String TERMINATOR = "\r"; // Carrage Reture

	private String motorReply;

	private StringReader reader;

	private StringWriter writer;

	private String serialDeviceName;

	// RS232 communications protocol defaults:
	private Serial serial = null;

	@Override
	public void configure() {
		logger.debug("PIControllerC862: Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setReadTimeout(READ_TIMEOUT);
				serial.flush();
				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				// set up communicators
				reader.stringProps.setTerminator(TERMINATOR);
				writer.stringProps.setTerminator(TERMINATOR);
			} catch (DeviceException de) {
				logger.error("Exception while connecting the Serial Port" + de);
			}
		}
	}

	/**
	 * Set the serial device name
	 * 
	 * @param serialDeviceName
	 *            the serial device name
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * Get the serial device name
	 * 
	 * @return the serial device name
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * Close the serial port connection.
	 * 
	 * @see gda.device.serial.SerialComm
	 */
	@Override
	public void close() {
		((PIController) serial).close();
	}

	/**
	 * Transmits a command string to a single piezo on the serial line. This method is synchronised to ensure the
	 * correct reply returns, to the correct Object.
	 * 
	 * @param command
	 *            Command code string
	 * @param replyGenerated
	 *            true to read the motor reply string and return it. Set to false if motor does not send a reply for
	 *            this command.
	 * @return reply from motor
	 * @exception MotorException
	 */
	public synchronized String sendCommand(String command, boolean replyGenerated) throws MotorException {
		try {
			motorReply = "";
			logger.debug("PIController: The command sent is: " + command);
			writer.write(command);

			if (replyGenerated) {
				// README This device does not send a reply for certain commands
				motorReply = reader.read();
				logger.debug("PIController: The reply from the piezo is: " + motorReply);
			}
		} catch (DeviceException de) {
			// README Throwing this exception may cause more problems than
			// it
			// solves. Need to consider assuming all is well in that case !!
			logger.error("PIController: Device exception caught sending command ");
			logger.error("Reply = " + motorReply + ". Error = " + de.toString());
			throw new MotorException(MotorStatus.FAULT, de.toString());
		} catch (Exception e) {
			logger.error("PIController: Exception caught sending command " + "in send command: " + " Exception = " + e);
		}
		return motorReply;
	}
}
