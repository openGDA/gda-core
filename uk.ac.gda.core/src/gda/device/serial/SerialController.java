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

package gda.device.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Serial;
import gda.factory.Finder;

/**
 * A Distributed Controller class for Serial devices
 */
public class SerialController extends DeviceBase {
	
	private static final Logger logger = LoggerFactory.getLogger(SerialController.class);
	
	private StringReader reader;

	private StringWriter writer;

	private String serialDeviceName;

	private String commandTerminator;

	private String replyTerminator;

	private int serialTimeout;

	private String errorChars;

	// RS232 communications protocol defaults (all XML configurable):
	private Serial serial = null;

	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_7;

	private String flowControl = Serial.FLOWCONTROL_NONE;

	@Override
	public void configure() {
		logger.debug("SerialController: Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(serialTimeout);
				serial.flush();

				reader = new StringReader(serial);
				writer = new StringWriter(serial);

				reader.stringProps.setTerminator(replyTerminator);
				writer.stringProps.setTerminator(commandTerminator);
			} catch (DeviceException de) {
				logger.error("Exception while connecting the Serial Port" + de);
			}
		}
	}

	/**
	 * @return the no. data bits
	 */
	public int getByteSize() {
		return (byteSize);
	}

	/**
	 * @param i
	 *            int number of data bits
	 */
	public void setByteSize(int i) {
		this.byteSize = i;
	}

	/**
	 * @return int value of bits/second
	 */
	public int getBaudRate() {
		return (baudRate);
	}

	/**
	 * @param i
	 *            int value of bits/second
	 */
	public void setBaudRate(int i) {
		this.baudRate = i;
	}

	/**
	 * @return the String command terminator
	 */
	public String getCommandTerminator() {
		return (commandTerminator);
	}

	/**
	 * @param terminator
	 *            the String command terminator
	 */
	public void setCommandTerminator(String terminator) {
		commandTerminator = terminator;
		logger.debug("SerialController commandTerminator = " + terminator);
	}

	/**
	 * @return String errorChars
	 */
	public String getErrorChars() {
		return (errorChars);
	}

	/**
	 * @param chars
	 *            String error Chars
	 */
	public void setErrorChars(String chars) {
		errorChars = chars;
		logger.debug("SerialController errorChars = " + chars);
	}

	/**
	 * @return String flow control
	 */
	public String getFlowControl() {
		return (flowControl);
	}

	/**
	 * @param str
	 *            String flow control
	 */
	public void setFlowControl(String str) {
		flowControl = str;
		logger.debug("SerialController flowControl = " + str);
	}

	/**
	 * @return String parity
	 */
	public String getParity() {
		return (parity);
	}

	/**
	 * @param str
	 *            String parity
	 */
	public void setParity(String str) {
		parity = str;
		logger.debug("SerialController parity = " + str);
	}

	/**
	 * @return String reply Terminator
	 */
	public String getReplyTerminator() {
		return (replyTerminator);
	}

	/**
	 * @param terminator
	 *            String reply Terminator
	 */
	public void setReplyTerminator(String terminator) {
		replyTerminator = terminator;
		logger.debug("SerialController replyTerminator = " + terminator);
	}

	/**
	 * @return String Serial Device Name
	 */
	public String getSerialDeviceName() {
		return (serialDeviceName);
	}

	/**
	 * @param name
	 *            String Serial Device Name
	 */
	public void setSerialDeviceName(String name) {
		serialDeviceName = name;
		logger.debug("SerialController serialDeviceName =" + name);
	}

	/**
	 * @return int serial timeout
	 */
	public int getSerialTimeout() {
		return (serialTimeout);
	}

	/**
	 * @param t
	 *            int serial timeout
	 */
	public void setSerialTimeout(int t) {
		this.serialTimeout = t;
	}

	/**
	 * @return int stop bits
	 */
	public int getStopBits() {
		return (stopBits);
	}

	/**
	 * @param i
	 *            int stop bits
	 */
	public void setStopBits(int i) {
		this.stopBits = i;
	}

	@Override
	public void close() {
		((SerialComm) serial).close();
	}

	/**
	 * Send a command to the serial port
	 * 
	 * @param command
	 *            String serial device command
	 * @throws DeviceException
	 */
	public synchronized void sendCommand(String command) throws DeviceException {
		// String reply = "";
		try {
			logger.debug("SerialController: The command sent is: " + command);
			writer.write(command);
			logger.debug("SerialController: command successfully sent");
		} catch (DeviceException e) {
			logger.error("SerialController: Exception caught from communication ");
			throw new DeviceException(e.toString());
		}
	}

	/**
	 * @return reply
	 * @throws DeviceException
	 */
	public synchronized String getReply() throws DeviceException {
		String reply = "";

		try {
			reply = reader.read();
			logger.debug("SerialController: The reply from the serial device is: " + reply);
		} catch (DeviceException e) {
			logger.error("SerialController: Exception caught from communication ");
			throw new DeviceException(e.toString());
		}

		if (reply.indexOf(errorChars) != -1) {
			logger.error("SerialController: The serial device responded with an error");
			throw new DeviceException("The device responded with an error" + reply);
		}
		return reply;
	}
}
