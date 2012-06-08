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

import gda.device.MotorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DummySlaveMotor Class
 */
public class DummySlaveMotor extends DummyMotor {
	
	private static final Logger logger = LoggerFactory.getLogger(DummySlaveMotor.class);
	
	private String serialControllerName;

	private String moveCommand;

	private String stopCommand;

	private boolean positionalHoming = true;

	private String homeCommand;

	private String slaveErrorString = "a";

	/**
	 * Get the command to send to the RS232 slave to move a motor without iterative positional searching (as defined in
	 * XML)
	 * 
	 * @return moveCommand String the move command
	 */
	public String getMoveCommand() {
		return (moveCommand);
	}

	/**
	 * Set the command to send to the RS232 slave to move a motor without iterative positional searching (as defined in
	 * XML)
	 * 
	 * @param command
	 *            the String command to move a slave motor
	 */
	public void setMoveCommand(String command) {
		moveCommand = command;
		logger.debug("Slave motor moveCommand =" + command);
	}

	/**
	 * Get the serial controller name for this slave motor as defined in XML
	 * 
	 * @return the serial controller name
	 */
	public String getSerialControllerName() {
		return (serialControllerName);
	}

	/**
	 * Set the serial controller name for this slave motor as defined in XML
	 * 
	 * @param s
	 *            the serial controller name
	 */
	public void setSerialControllerName(String s) {
		this.serialControllerName = s;
	}

	/**
	 * Get the command that needs to be issued to stop the slave motor (defind in XML)
	 * 
	 * @return the stop command
	 */
	public String getStopCommand() {
		return (stopCommand);
	}

	/**
	 * Set the command that needs to be issued to stop the slave motor (defind in XML)
	 * 
	 * @param command
	 *            the stop command
	 */
	public void setStopCommand(String command) {
		stopCommand = command;
		logger.debug("Slave motor stopCommand =" + command);
	}

	/**
	 * Get the Home command to move a motor using iterative positional searching.
	 * 
	 * @return homeCommand the command to move a motor with positional searching
	 */
	public String getHomeCommand() {
		return (homeCommand);
	}

	/**
	 * Set the Home command to move a motor using iterative positional searching.
	 * 
	 * @param s
	 *            the home (move with iterative positional searching) command
	 */
	public void setHomeCommand(String s) {
		this.homeCommand = s;
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (name.equals(SlaveMotor.POSITIONALHOMING) && value instanceof Boolean)
			this.positionalHoming = ((Boolean) value).booleanValue();
	}

	@Override
	public Object getAttribute(String name) {
		Object o = null;
		if (name.equals(SlaveMotor.POSITIONALHOMING))
			o = new Boolean(positionalHoming);

		return o;
	}

	/**
	 * Return the string that will be returned from the slave on error (set in XML)
	 * 
	 * @return slaveErrorString the error String
	 */
	public String getSlaveErrorString() {
		return (slaveErrorString);
	}

	/**
	 * Set the string that will be returned from the slave on error (set in XML)
	 * 
	 * @param s
	 *            the error string
	 */
	public void setSlaveErrorString(String s) {
		slaveErrorString = s;
		logger.debug("Slave motor moveCommand =" + s);
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		String command;
		java.io.StringWriter sw = new java.io.StringWriter();
		if (positionalHoming)
			sw.write(homeCommand);
		else
			sw.write(moveCommand);

		sw.append(" ");
		sw.append(new Integer((int) Math.round(steps)).toString());

		command = sw.toString();
		logger.debug("Sending slave command: " + command);
		logger.warn("Sending slave command: " + command);

		super.moveTo(steps);
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		logger.debug("MoveBy commandnot available for slave motor ");
	}
}
