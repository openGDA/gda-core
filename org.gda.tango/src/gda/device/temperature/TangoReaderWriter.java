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

package gda.device.temperature;

import gda.device.DeviceException;

import gda.device.serial.TangoSerial;
import gda.device.SerialReaderWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to communicate  with a Tango Serial device
 */
public class TangoReaderWriter implements SerialReaderWriter, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(TangoReaderWriter.class);

	private TangoSerial tangoSerial = null;
	private final String terminator = "\r";
	
	public TangoSerial getTangoSerial() {
		return tangoSerial;
	}

	public void setTangoSerial(TangoSerial tangoSerial) {
		this.tangoSerial = tangoSerial;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoSerial == null) {
			throw new IllegalArgumentException("tangoSerial device needs to be set");
		}
		tangoSerial.flush();
	}
	
	@Override
	public void close() {
		try {
			tangoSerial.flush();
		} catch (DeviceException e) {
			logger.error("TangoReadWriter flush exception", e);
		}

	}
	
	/**
	 * Handles commands which do not need a reply.
	 * 
	 * @param command
	 *            the command to send
	 */
	@Override
	public void handleCommand(String command) {
		try {
			sendCommandAndGetReply(command);
		} catch (DeviceException de) {
			logger.error("TangoReaderWriter.handleCommand() caught DeviceException \"" + de.getMessage()
					+ "\" sending command \"" + command + "\"");
		}
	}


	/**
	 * Actually writes a string to the device.
	 * 
	 * @param command the command
	 * @throws DeviceException
	 */
	private void sendCommand(String command) throws DeviceException {
		logger.debug("TangoReaderWriter: writing {} to device", command);
		tangoSerial.writeString(command+terminator);
	}

	/**
	 * Sends a command to the hardware and reads back a data reply.
	 * 
	 * @param command the command sent to the hardware
	 * @return the reply
	 * @throws DeviceException
	 */
	@Override
	public String sendCommandAndGetReply(String command) throws DeviceException {
//		System.out.println("TangoReaderWriter: writing " + command + " to device");
		String reply = tangoSerial.writeReadString(command+terminator);
//		System.out.println("TangoReaderWriter: reply is " + reply);
//		if (reply.length() > 0)
//			System.out.println("TangoReaderWriter: first char " + (int) reply.charAt(0));
		return reply;
	}
}