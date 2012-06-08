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

package gda.device.bpm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.Finder;
import gda.util.Sleep;

/**
 * OxfordDanfysik QBPM.
 */
public class OxfordDanfysikQBPM extends BPMBase {
	
	private static final Logger logger = LoggerFactory.getLogger(OxfordDanfysikQBPM.class);
	
	private StringReader reader;

	private StringWriter writer;

	private String terminator = "\n"; // line feed

	private String serialDeviceName = "/dev/ttyS0";

	private int address = 0;

	private int timeout = 5000;

	private boolean retry = true;

	private Serial serial = null;

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
				reader.stringProps.setTerminator(terminator);
				writer.stringProps.setTerminator(terminator);
			} catch (DeviceException de) {
				logger.error("Exception while connecting the Serial Port" + de);
			}
		}

		initialize();
	}

	private boolean sendCommand(String command, boolean waitForAcknowledgement) {
		boolean timeout = false;

		try {
			writer.write(command);
			Sleep.sleep(100);
			if (waitForAcknowledgement)
				reader.read();
		} catch (DeviceException e) {
			e.printStackTrace();
		}

		return timeout;
	}

	private void initialize() {
		sendCommand("*rst" + address, false);
		Sleep.sleep(1000);
		sendCommand(":conf0:gx1?", true);
		sendCommand(":conf0:gy1?", true);
		sendCommand(":conf0:a11?", true);
		sendCommand(":conf0:a20?", true);
		sendCommand(":conf0:b11?", true);
		sendCommand(":conf0:b20?", true);
		sendCommand(":conf0:c11?", true);
		sendCommand(":conf0:c20?", true);
		sendCommand(":conf0:d11?", true);
		sendCommand(":conf0:d20?", true);
	}

	@Override
	public double getX() {
		double x = 0.0;

		try {
			writer.write(":read" + address + ":posx?");
			x = Double.parseDouble(reader.read());
		} catch (DeviceException e) {
			e.printStackTrace();
		} catch (NumberFormatException nfex) {
			if (retry) {
				initialize();
				retry = false;
				x = getX();
				retry = true;
			}
		}

		return x;
	}

	@Override
	public double getY() {
		double y = 0.0;

		try {
			writer.write(":read" + address + ":posy?");
			y = Double.parseDouble(reader.read());
		} catch (DeviceException e) {
			initialize();
			y = getY();
		} catch (NumberFormatException nfex) {
			if (retry) {
				initialize();
				retry = false;
				y = getY();
				retry = true;
			}
		}

		return y;
	}

	/**
	 * Sets the serial device name.
	 * 
	 * @param serialDeviceName
	 *            the serial device name
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * Gets the serial device name.
	 * 
	 * @return the serial device name
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * Sets the device's address.
	 * 
	 * @param address
	 *            The device's address
	 */
	public void setAddress(int address) {
		this.address = address;
	}

	/**
	 * Gets the device's address.
	 * 
	 * @return The device's address
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * Sets the timeout.
	 * 
	 * @param timeout
	 *            The timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the timeout.
	 * 
	 * @return The timeout
	 */
	public int getTimeout() {
		return timeout;
	}
}
