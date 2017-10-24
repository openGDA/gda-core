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
import gda.device.MotorStatus;
import gda.device.serial.SerialController;
import gda.factory.Finder;

/**
 * A Generic class to implement an RS232 Slave motor interface
 */
public class SlaveMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private String serialControllerName;

	private SerialController serialController;

	private String moveCommand;

	private String stopCommand;

	private double currentPosition = 0;

	private MotorStatus status = MotorStatus.READY;

	private String homeCommand;

	private double targetPosition;

	private double simulatedPosition;

	private String slaveErrorString = "a";

	private boolean positionalHoming = true;

	static String POSITIONALHOMING = "positionalHoming";

	@Override
	public void configure() {
		logger.debug("Finding: " + serialControllerName);
		if ((serialController = (SerialController) Finder.getInstance().find(serialControllerName)) == null) {
			logger.error("SerialController " + serialControllerName + " not found");
		} else {
			logger.debug("super configure, serialController found.");
		}
	}

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
		if (name.equals(POSITIONALHOMING) && value instanceof Boolean)
			this.positionalHoming = ((Boolean) value).booleanValue();
	}

	@Override
	public Object getAttribute(String name) {
		Object o = null;
		if (name.equals(POSITIONALHOMING))
			o = new Boolean(positionalHoming);

		return o;
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

	@Override
	public void moveBy(double steps) throws MotorException {
		logger.debug("moveBy not available for SlaveMotor ");
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

		try {
			serialController.sendCommand(command);
			status = MotorStatus.BUSY;
			parseReply(serialController.getReply());
		} catch (Exception e) {
			throw new MotorException(MotorStatus.FAULT, "Error moving to " + steps, e);
		} finally {
			status = MotorStatus.READY;
		}
	}

	private void parseReply(String reply) throws Exception {
		if (reply.startsWith(slaveErrorString))
			throw new Exception("Error returned from Slave Motor");
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		logger.debug("moveContinuosly not available for SlaveMotor ");
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		currentPosition = steps;
		simulatedPosition = steps;
		status = MotorStatus.READY;
		logger.debug("setPosition command for SlaveMotor does not set actual mono posn");
	}

	@Override
	public double getPosition() throws MotorException {
		// if motor busy then simulate a position for OeMove display purposes
		// only
		// else assume currentposition has been updated with real value
		if (status == MotorStatus.BUSY && (targetPosition - simulatedPosition > 1))
			simulatedPosition += 1.0;
		else
			simulatedPosition = currentPosition;

		return simulatedPosition;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		logger.debug("command not available for SlaveMotor ");
	}

	@Override
	public double getSpeed() throws MotorException {
		logger.debug("setSpeed command not available for SlaveMotor ");
		return 0;
	}

	@Override
	public void stop() throws MotorException {
		logger.debug("stop command not available for SlaveMotor ");
	}

	@Override
	public void panicStop() throws MotorException {
		logger.debug("panicStop command not available for SlaveMotor ");
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		return status;
	}

	@Override
	public boolean isMoving() throws MotorException {
		boolean isMoving = false;
		if (getStatus() == MotorStatus.BUSY)
			isMoving = true;
		return isMoving;
	}
}
