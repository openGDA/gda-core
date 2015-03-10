/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.base.impl.BaseImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoMcLennanController extends BaseImpl implements IMcLennanController {

	private static final Logger logger = LoggerFactory.getLogger(TangoMcLennanController.class);
	private static final String ERROR_CHARS = "!";
	private static final int DEFAULT = -1;

	private static final String parity = "even";
	private static final int baudRate = 9600;
	private static final int stopBits = 1;
	private static final int dataBits = 7;
	private static final int timeout = 1000;
	private static final String terminator = "\r";

	private String motorReply;
	private String name;
	
	public void configure() {

		try {
			getTangoDeviceProxy().isAvailable();
			DeviceData argin = new DeviceData();
			argin.insert(baudRate);
			getTangoDeviceProxy().command_inout("DevSerSetBaudrate", argin);
			argin.insert(stopBits);
			getTangoDeviceProxy().command_inout("DevSerSetStopbits", argin);
			argin.insert(dataBits);
			getTangoDeviceProxy().command_inout("DevSerSetCharlength", argin);
			argin.insert(parity);
			getTangoDeviceProxy().command_inout("DevSerSetParity", argin);
			argin.insert(timeout);
			getTangoDeviceProxy().command_inout("DevSerSetTimeout", argin);
			argin.insert(terminator);
			getTangoDeviceProxy().command_inout("DevSerSetNewline", argin);			
			getTangoDeviceProxy().command_inout("Flush");
		} catch (DevFailed e) {
			logger.error("TangoMcLennanController configure: {}", e);
			logger.error("TangoMcLennanController configure {}", e.getMessage());
		} catch (DeviceException e) {
			logger.error("TangoMcLennanController configure: {}", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void close() {
		try {
			getTangoDeviceProxy().sendSimpleCommand("Close");
		} catch (DevFailed e) {
			logger.error("TangoMclennanController: close: {}", e);
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
			logger.debug("McLennanController: sent command {}", command);
			DeviceData argin = new DeviceData();
			argin.insert(command);
//			getTangoDeviceProxy().command_inout("DevSerWriteString", argin);
//			motorReply = getTangoDeviceProxy().getStringFromCommand("ReadString");			 
			motorReply = getTangoDeviceProxy().getStringFromCommand("WriteRead", argin);			 
			logger.debug("McLennanController: reply is: {}", motorReply.replaceAll("\r", " "));
		} catch (DevFailed e) {
			logger.error("Controller: Exception caught from serial communication {}", e);
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
	
	@Override
	public void  globalCommand(char command) throws MotorException {
	}
}
	


