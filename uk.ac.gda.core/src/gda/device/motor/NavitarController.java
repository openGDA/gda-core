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
import gda.device.Serial;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.Finder;

/**
 * NavitarController Class
 */
public class NavitarController extends DeviceBase {
	
	private static final Logger logger = LoggerFactory.getLogger(NavitarController.class);
	
	private String serialDeviceName = null;

	private Serial serial = null;

	private int timeout = 5000;

	private StringReader reader;

	private StringWriter writer;

	private String reply;

	/**
	 * Constructor
	 */
	public NavitarController() {
	}

	@Override
	public void configure() {
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setReadTimeout(timeout);
				serial.flush();
				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				reader.stringProps.setTermination(false);
				writer.stringProps.setTermination(false);
			} catch (DeviceException de) {
				logger.error("Exception while connecting the Serial Port" + de);
			}
		}
	}

	/**
	 * @param command
	 * @return reply
	 */
	public synchronized String sendCommand(String command) {
		reply = null;

		try {
			writer.write(command);
			reply = reader.read(7);
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		return reply;
	}

	// XML getters and setters

	/**
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
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
}
