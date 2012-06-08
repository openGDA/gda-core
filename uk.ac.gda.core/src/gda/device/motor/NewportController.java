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
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.factory.Finder;

/**
 * Used by NewportMotor as the point of communication with the actual hardware. class to be enclosed within a
 * NewportMotor object as an RS232 communications instance.
 */
public class NewportController extends DeviceBase implements Configurable, Findable {
	
	private static final Logger logger = LoggerFactory.getLogger(NewportController.class);
	
	// set static parameters for timeout, error handling and delimiters:
	private static final int READ_TIMEOUT = 1000;

	private static final String TERMINATOR = "\r"; // carriage return

	private static final String ERROR_COMMAND = "TE";

	// private String name;
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

	@Override
	public void configure() {
		logger.debug("Finding: " + serialDeviceName);
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
				reader.stringProps.setTerminator(TERMINATOR);
				writer.stringProps.setTerminator(TERMINATOR);
			}
			// want to pick up all exceptions here
			catch (Exception de) {
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
		((NewportController) serial).close();
	}

	/**
	 * Transmits a command string to a single motor on the serial line. This method is synchronised to ensure the
	 * correct reply returns, to the correct Object.
	 * 
	 * @param command
	 *            Command code string
	 * @throws MotorException
	 */
	public synchronized void sendCommand(String command) throws MotorException {
		try {
			motorReply = null;
			logger.debug("Controller: The command sent is: " + command);
			writer.write(command);
			// see if there is any error
			writer.write(ERROR_COMMAND);
			motorReply = reader.read();
			if (motorReply.indexOf("@") == -1) {
				String errorMessage = motorReply.substring(ERROR_COMMAND.length());
				throw new MotorException(MotorStatus.FAULT, errorMessage);
			}
			logger.debug("Controller: The reply from the motor is: " + motorReply);
		} catch (DeviceException e) {
			logger.error("Controller: Exception caught from communication ");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
	}

	/**
	 * Transmits a command string to a single motor on the serial line. This method is synchronised to ensure the
	 * correct reply returns, to the correct Object.
	 * 
	 * @param command
	 *            Command code string
	 * @return motor reply string
	 * @throws MotorException
	 */
	public synchronized String sendReplyCommand(String command) throws MotorException {
		try {
			motorReply = null;
			logger.debug("Controller: The command sent is: " + command);
			writer.write(command);
			motorReply = reader.read();
			logger.debug("Controller: The reply from the motor is: " + motorReply);
		} catch (DeviceException e) {
			logger.error("Controller: Exception caught from communication ");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
		return motorReply;
	}
}
