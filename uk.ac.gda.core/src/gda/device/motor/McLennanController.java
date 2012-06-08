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
 * Single point of access to a shared Serial connection for McLennan motor controllers.
 * 
 * @see McLennanMotor
 * @see McLennanServoMotor
 */
public class McLennanController extends DeviceBase implements IMcLennanController {
	
	private static final Logger logger = LoggerFactory.getLogger(McLennanController.class);
	
	private static final int DEFAULT = -1;

	private static final int READ_TIMEOUT = 1000;

	private static final String WRITE_TERMINATOR = "\r"; // carriage
	// return

	private static final String READ_TERMINATOR = "\r\n"; // carriage
	// return

	private static final String ERROR_CHARS = "!";

	private String motorReply;

	private StringReader reader;

	private StringWriter writer;

	private String serialDeviceName;

	// RS232 communications protocol defaults:
	private Serial serial = null;

	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_7;

	// private String flowControlIn = Serial.FLOWCONTROL_NONE;
	// private String flowControlOut = Serial.FLOWCONTROL_NONE;

	@Override
	public void configure() {
		logger.debug("McLennanController: Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(READ_TIMEOUT);
				serial.flush();
				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				// set up communicators
				reader.stringProps.setTerminator(READ_TERMINATOR);
				writer.stringProps.setTerminator(WRITE_TERMINATOR);
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
		((McLennanController) serial).close();
	}

	/**
	 * Issue a single character command to the connected serial device that acts on all motors with immediate effect.
	 * 
	 * @param command
	 *            Global command character
	 * @throws MotorException
	 */
	@Override
	public void globalCommand(char command) throws MotorException {
		try {
			serial.writeChar(command);
		} catch (DeviceException e) {
			throw new MotorException(MotorStatus.FAULT, "Error sending the global command" + e.toString());
		}
	}

	/**
	 * Transmits a command string to a single motor on the serial line. This method is synchronised to ensure the
	 * correct reply returns, to the correct Object. McLennan replies, particularly errors, are generously sprinkled
	 * with carriage returns (without line feeds). These mess up the output, hence the replaceAll() in the printing out
	 * of replies.
	 * 
	 * @param command
	 *            Command code string
	 * @return reply from motor
	 * @throws MotorException
	 */
	@Override
	public synchronized String sendCommand(String command) throws MotorException {
		try {
			motorReply = "";
			logger.debug("Controller: The command sent is: " + command);
			writer.write(command);
			logger.debug("Controller: command successfully sent");
			motorReply = reader.read();
			logger.debug("Controller: The reply from the motor is: " + motorReply.replaceAll("\r", " "));
		} catch (DeviceException e) {
			logger.error("Controller: Exception caught from communication ");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}

		if (motorReply.indexOf(ERROR_CHARS) != DEFAULT) {
			// If the command was a controlled stop and the motor is already
			// stopped then we may get an error of the form
			// 'NOT ALLOWED IN THIS MODE' (from a 600). We just
			// ignore this. See bug #766.
			if (motorReply.contains("NOT ALLOWED IN THIS MODE")) {
				logger.error("McLennanController: deliberately ignoring error reply - \""
						+ motorReply.replaceAll("\r", " ") + "\"");
			} else {
				logger.error("McLennanController: The motor responded with an error");
				throw new MotorException(MotorStatus.FAULT, "The motor responded with an error" + motorReply);
			}
		}
		return motorReply;
	}
}
