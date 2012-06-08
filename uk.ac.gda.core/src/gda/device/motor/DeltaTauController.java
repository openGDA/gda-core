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
 * Single point of access to a shared Serial connection for Delta Tau motor controllers.
 * 
 * @see McLennanMotor
 * @see McLennanServoMotor
 */
public class DeltaTauController extends DeviceBase {
	
	private static final Logger logger = LoggerFactory.getLogger(DeltaTauController.class);
	
	private static final int READ_TIMEOUT = 1000;

	private static final String WRITE_TERMINATOR = "\r"; // carriage
	// return

	private static final String READ_TERMINATOR = "\n"; // line feed

	private String motorReply;

	private StringReader reader;

	private StringWriter writer;

	private String serialDeviceName;

	// RS232 communications protocol defaults:
	private Serial serial = null;

	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_38400;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_8;

	@Override
	public void configure() {
		logger.debug("DeltaTauController: Finding: " + serialDeviceName);
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
				reader.stringProps.setTermination(true);
				writer.stringProps.setTerminator(WRITE_TERMINATOR);
				writer.write("I3=1"); // set option for LF replies
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
		((DeltaTauController) serial).close();
	}

	/**
	 * Transmits a command string to a single motor on the serial line. This method is synchronised to ensure the
	 * correct reply returns, to the correct Object. DeltaTau replies, particularly errors, are generously sprinkled
	 * with carriage returns (without line feeds). These mess up the output, hence the replaceAll() in the printing out
	 * of replies.
	 * 
	 * @param command
	 *            Command code string
	 * @return reply from motor
	 * @throws MotorException
	 */
	public synchronized String sendCommand(String command) throws MotorException {
		try {
			motorReply = "";
			logger.debug("Controller: The command sent is: " + command);
			writer.write(command);
			motorReply = reader.read();
			logger.debug("Controller: The reply from the motor is: " + motorReply.replaceAll("\r", "<CR>"));
		} catch (DeviceException e) {
			logger.error("Controller: Exception caught from communication ");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}

		if (motorReply.charAt(0) == 0x7) {
			logger.error("DeltaTauController: The motor responded with an error");
			throw new MotorException(MotorStatus.FAULT, "The motor responded with an error" + motorReply);
		}

		return motorReply;
	}
}
