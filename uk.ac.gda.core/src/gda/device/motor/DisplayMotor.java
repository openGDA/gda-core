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
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.Serial;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.Finder;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialised Motor class that reads feedback from a Protrura encoder display module. The units returned by the
 * display are in nanometres. FIXME rename to ProtruraDisplay ??
 */
public class DisplayMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(DisplayMotor.class);

	private static final String PARAM_CHARS = ":";

	private static final String QUERY = "?";

	// FIXME This needs setting in XML//
	private int encoderChannel = 2;

	private double currentPosition = 0.0;

	// SERIAL SETTINGS FOR THE DISPLAY UNIT TO RETRIEVE THE ENCODER
	// POSITION.
	private Serial serial = null;

	private String serialDeviceName;

	private StringReader reader;

	private StringWriter writer;

	private static final int READ_TIMEOUT = 1000;

	private static final String TERMINATOR = "\r\n"; // carriage return\line

	// feed
	// RS232 communications protocol defaults:
	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_115200;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_8;

	private String flowControl = "none";

	/**
	 * Constructor
	 */
	public DisplayMotor() {
	}

	@Override
	public void configure() {
		// Connect to the display unit through an RS232 connection
		logger.debug("DisplayMotor: Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			logger.debug("DisplayMotor: Serial Device is " + serialDeviceName);
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setFlowControl(flowControl);
				serial.setReadTimeout(READ_TIMEOUT);
				serial.flush();
				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				// set up communicators
				reader.stringProps.setTerminator(TERMINATOR);
				// NB No terminator character for write.
				// writer.stringProps.setTerminator(TERMINATOR);
			} catch (DeviceException de) {
				logger.error("DisplayMotor: Exception while connecting the Serial Port" + de);
			}
			// README There does not seem to be any need to getPosition
			// here.
		}
	}

	/**
	 * @return serial device name
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * @param serialDeviceName
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * This method sends the command to get the position from the encoder display. It assumes that this will always
	 * return an integer value corresponding to nanometres.
	 *
	 * @param command
	 * @return the position in nanometres
	 */
	private synchronized String sendCommand(String command) {
		String displayReply = "";

		try {
			logger.debug("DisplayMotor: sending query");
			writer.write(command);
			displayReply = reader.read();
			logger.debug("DisplayMotor: Display readout = " + displayReply);
		} catch (Exception e) {
			logger.error("Error sending command '{}' to display", command, e);
			// README Have chosen not to throw an exception here because it
			// may
			// create more problems than it solves.
		}
		return displayReply;
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		// Cannot move this device
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		// Cannot move this device
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// Cannot move this device
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		// Can only Zero the position
	}

	@Override
	public double getPosition() throws MotorException {
		logger.debug("DisplayMotor: getting position");
		String reply = sendCommand(QUERY);
		currentPosition = extractPosition(reply.trim());
		return currentPosition;
	}

	private double extractPosition(String reply) throws MotorException {
		int position = 0;
		logger.debug("DisplayMotor: extracting position");

		StringTokenizer splitAxis = new StringTokenizer(reply, PARAM_CHARS);

		int noOfTokens = splitAxis.countTokens();
		if (noOfTokens > 1) // delimiter found
		{
			int count = 1;
			try {
				for (count = 1; count <= noOfTokens; count++) {
					String encoderValue = splitAxis.nextToken().trim();
					if (count == encoderChannel) {
						position = Integer.parseInt(encoderValue);
						break;
					}
				}

				logger.debug("Display Motor position value is: " + position);
			} catch (NumberFormatException e) {
				throw new MotorException(MotorStatus.FAULT, e.toString());
				// README - this exception is probably necessary
			}
		}
		return position;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		// Cannot set speed on this device
	}

	@Override
	public double getSpeed() throws MotorException {
		return 0;
		// Cannot set speed on this device
	}

	@Override
	public void stop() throws MotorException {
		// Not relevant
	}

	@Override
	public void panicStop() throws MotorException {
		// Not relevant
	}

	@Override
	public MotorStatus getStatus() {
		// Cannot get status
		return MotorStatus.READY;
	}

	@Override
	public boolean isMoving() throws MotorException {
		// Cannot move this device
		return false;
	}
}
