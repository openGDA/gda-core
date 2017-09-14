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
import gda.device.MotorStatus;
import gda.factory.Finder;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement features of NewportMotor.
 */
public class NewportMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private int axis;

	// private boolean initialised = false;
	private NewportController newportController;

	private String newportControllerName;

	private static final String SET_SPEED = "VA";

	private static final String MOVE_ABS = "PA";

	private static final String MOVE_REL = "PR";

	private static final String EMERGENCY_STOP = "MF";

	private static final String LOWER_LIMIT = "SL";

	private static final String UPPER_LIMIT = "SR";

	private static final String CONTROL_STOP = "ST";

	private static final String READ_POSITION = "TP";

	private static final String READ_STATUS = "MS";

	// private static final String POWER_ON = "MO";

	// position of status bit in reply converted to binary string
	private static final int MOVING = 7;

	private static final int POWER = 6;

	private static final int POS_LIMIT = 4;

	private static final int NEG_LIMIT = 3;

	protected static final int DEFAULT = -1;

	protected static final int BASE_10 = 10;

	protected static final char OFF = '0';

	protected static final char ON = '1';

	private String command = "";

	private String reply = "";

	private NumberFormat formatter = new DecimalFormat("00000000");

	/**
	 * Constructor
	 */
	public NewportMotor() {
	}

	@Override
	public void configure() {
		logger.debug("Finding: " + newportControllerName);
		if ((newportController = (NewportController) Finder.getInstance().find(newportControllerName)) == null) {
			logger.error("newportController " + newportControllerName + " not found");
		}
	}

	/**
	 * @param newportControllerName
	 */
	public void setNewportControllerName(String newportControllerName) {
		this.newportControllerName = newportControllerName;
	}

	/**
	 * @return newport controller name
	 */
	public String getNewportControllerName() {
		return newportControllerName;
	}

	/**
	 * @param axis
	 */
	public void setAxis(int axis) {
		this.axis = axis;
	}

	/**
	 * @return axis
	 */
	public int getAxis() {
		return axis;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (checkStatus() == MotorStatus.BUSY);
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		addInBacklash(getBacklashSteps());
		newportController.sendCommand(axis + MOVE_REL + steps);
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		addInBacklash(getBacklashSteps());
		newportController.sendCommand(axis + MOVE_ABS + steps);
	}

	@Override
	public void stop() throws MotorException {
		newportController.sendCommand(axis + CONTROL_STOP);
	}

	@Override
	public void panicStop() throws MotorException {
		// README This command switches off the motor power.
		// Should power be switched on after this command?
		// Unlikely as we wish to report a fault condition.
		newportController.sendCommand(EMERGENCY_STOP);
	}

	/**
	 * @param minPosition
	 *            amount to adjust lower softlimit
	 * @param maxPosition
	 *            amount to adjust upper softlimit
	 * @throws MotorException
	 */
	@Override
	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException {
		newportController.sendCommand(axis + LOWER_LIMIT + minPosition);
		newportController.sendCommand(axis + UPPER_LIMIT + maxPosition);
	}

	/**
	 * A wrapper method to enable getStatus() to throw a MotorException to a calling method. FIXME: Can be disposed of
	 * if getStatus() throws MotorException.
	 *
	 * @return the motor status
	 * @throws MotorException
	 */
	protected MotorStatus checkStatus() throws MotorException {
		MotorStatus status = getStatus();
		if (status == MotorStatus.UNKNOWN) {
			throw new MotorException(MotorStatus.FAULT, "Exception while getting status ");
		}
		return status;
	}

	/**
	 * @see gda.device.Motor#moveContinuously(int)
	 */
	@Override
	public void moveContinuously(int direction) throws MotorException {
	}

	/**
	 * Sets the current position of the motor
	 *
	 * @param steps
	 *            the position to be set as current
	 * @throws MotorException
	 */
	@Override
	public void setPosition(double steps) throws MotorException {
		// README : no command available
	}

	/**
	 * Gets the current position of the motor
	 *
	 * @return the current position
	 * @throws MotorException
	 */
	@Override
	public double getPosition() throws MotorException {
		command = axis + READ_POSITION;
		reply = newportController.sendReplyCommand(command);
		double position = Double.parseDouble(reply.substring(command.length()));
		return position;
	}

	/**
	 * Sets the speed of the motor
	 *
	 * @param stepsPerSecond
	 *            the speed in steps per second
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		newportController.sendCommand(axis + SET_SPEED + stepsPerSecond);
	}

	/**
	 * Gets the current speed setting of the motor
	 *
	 * @return the speed in steps per second
	 * @throws MotorException
	 */
	@Override
	public double getSpeed() throws MotorException {
		command = axis + SET_SPEED;
		reply = newportController.sendReplyCommand(command + "?");
		double speed = Double.parseDouble(reply.substring(command.length()));
		return speed;
	}

	/**
	 * Gets the state of the motor As the Motor interface does not allow exceptions to be thrown all diagnostics must be
	 * thrown away
	 *
	 * @return a value from the MotorStatus enum
	 */
	@Override
	public MotorStatus getStatus() {
		// MotorStatus ms = MotorStatus.READY;
		MotorStatus ms = MotorStatus.FAULT;
		command = axis + READ_STATUS;
		try {
			reply = newportController.sendReplyCommand(command);
		} catch (MotorException e) {
			// Should it be FAULT??
			return MotorStatus.UNKNOWN;
		}
		reply = Integer.toBinaryString(reply.charAt(command.length()));
		reply = formatter.format(Integer.parseInt(reply));

		// README The old code did not work as expected and turned on the power
		// WHATEVER the status condition - NOT what was wanted. GMB Feb 05.
		// For now - disable turning the power on until we know exactly what
		// happens.

		// REWRITE:
		// If moving, presumably all is well - check ??
		if (reply.charAt(MOVING) == '1')
			ms = MotorStatus.BUSY;
		else if (reply.charAt(POS_LIMIT) == '1')
			ms = MotorStatus.UPPER_LIMIT;
		else if (reply.charAt(NEG_LIMIT) == '1')
			ms = MotorStatus.LOWER_LIMIT;
		else if (reply.charAt(POWER) == '1')
			ms = MotorStatus.FAULT;
		// This next may be presumptious??
		else
			ms = MotorStatus.READY;
		return ms;
	}
}
