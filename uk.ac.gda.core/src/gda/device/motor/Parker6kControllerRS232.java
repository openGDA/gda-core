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

import gda.device.MotorException;
import gda.factory.Findable;

/**
 * Provides RS232 communications for Parker6kController and is embedded in Parker6kMotor instance. Currently this
 * provides a simulator class as base class methods are not overridden (i.e. sendCommand() and buildReply())
 * 
 * @see Parker6kMotor
 * @see Parker6kController
 */
public class Parker6kControllerRS232 extends Parker6kController implements Findable {
	
	private static final Logger logger = LoggerFactory.getLogger(Parker6kControllerRS232.class);
	
	private String className = getClass().getName();

	private String port = "none";

	private String parity = "odd";

	private int baud = -1;

	private int stopBits = -1;

	/**
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param parity
	 */
	public void setParity(String parity) {
		this.parity = parity;
	}

	/**
	 * @return parity
	 */
	public String getParity() {
		return parity;
	}

	/**
	 * @param baud
	 */
	public void setBaud(int baud) {
		this.baud = baud;
	}

	/**
	 * @return baud
	 */
	public int getBaud() {
		return baud;
	}

	/**
	 * @param stopBits
	 */
	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	/**
	 * @return stop bits
	 */
	public int getStopBits() {
		return stopBits;
	}

	/**
	 * displays internal data on screen (superclass + this) <BR>
	 */
	@Override
	public void debug() {
		super.debug();
		logger.debug(className + " : port  : " + getPort());
		logger.debug(className + " : baud     : " + getBaud());
		logger.debug(className + " : parity   : " + getParity());
		logger.debug(className + " : stopBits : " + getStopBits());
	}

	@Override
	public void configure() {
	}

	@Override
	public synchronized String sendCommand(String command) throws MotorException {
		return null;
	}

	@Override
	public void tidyup() {
	}
}
