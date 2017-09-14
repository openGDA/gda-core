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

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Finder;
import gda.observable.IObservable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NavitarMotor Class
 */
public class NavitarMotor extends MotorBase implements IObservable, Motor {

	private static final Logger logger = LoggerFactory.getLogger(NavitarMotor.class);

	private enum CommandType {
		/**
		 *
		 */
		GET_STATUS, /**
		 *
		 */
		GET_CURRENT, /**
		 *
		 */
		SET_TARGET_ABSOLUTE, /**
		 *
		 */
		SET_TARGET_INCREMENT, /**
		 *
		 */
		SET_TARGET_LIMIT, /**
		 *
		 */
		SET_MAX_VELOCITY, /**
		 *
		 */
		GET_MAX_VELOCITY, /**
		 *
		 */
		SET_CURRENT
	}

	private char[] commandByte = { 0x14, 0x12, 0x90, 0x91, 0x93, 0x97, 0x17, 0x92 };

	private enum commandStatus {
		/**
		 *
		 */
		SUCCESS, /**
		 *
		 */
		TIMEOUT, /**
		 *
		 */
		CHECKSUM_ERROR
	}

	private final int HOME = 0;

	private final int LIMIT = 1;

	private final int STOP = 2;

	/*
	 * What information the manual contains indicates the following status codes can be obtained form the user status
	 * register. The lower 8 bits (bits 0 through 7) indicate a substate of moving (only 4 bits seem to be actually
	 * used). I suspect that between DRIVING_TO_LIMIT and SEEKING_FORWARD there should be a state DRIVING_OF_LIMIT, but
	 * the manual doesn't say this so the below are as in the manual. Bit 8 indicates the home sensor has been hit. Bit
	 * 9 indicates the limit sensor has been hit.
	 */
	private final int BITS_IN_USE_FOR_BUSY = 15;

	private final int HOME_SENSOR = 256;

	private final int LIMIT_SENSOR = 512;

	private String navitarControllerName = null;

	private NavitarController navitarController = null;

	private int motorNumber;

	private char[] commandChars = new char[7];

	private String command = null;

	private String returnString = null;

	private int commandValue;

	private boolean motorMoving = false;

	/**
	 * Constructor
	 */
	public NavitarMotor() {
	}

	@Override
	public void configure() {
		if ((navitarController = (NavitarController) Finder.getInstance().find(navitarControllerName)) == null) {
			logger.error("NavitarController " + navitarControllerName + " not found");
		}
	}

	private synchronized commandStatus sendCommand() {
		commandStatus status = commandStatus.SUCCESS;

		returnString = navitarController.sendCommand(command);
		if (returnString != null) {
			getCommandValue();
		} else {
			// FIXME
		}

		return status;
	}

	private void constructCommand(CommandType type, int value) {
		constructCommand(type);
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(value);
		for (int i = 2; i < 6; i++) {
			commandChars[i] = (char) bb.get(i - 2);
		}
		commandChars[6] = calculateChecksum(new String(commandChars));

		command = new String(commandChars);
	}

	private void constructCommand(CommandType type) {
		commandChars[0] = 0xffff;
		commandChars[1] = commandByte[type.ordinal()];
		if (motorNumber == 1)
			commandChars[1] += 16;
		commandChars[2] = 0;
		commandChars[3] = 0;
		commandChars[4] = 0;
		commandChars[5] = 0;
		commandChars[6] = calculateChecksum(new String(commandChars));

		command = new String(commandChars);
	}

	private void getCommandValue() {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 2; i < 6; i++)
			bb.put((byte) returnString.charAt(i));
		commandValue = bb.getInt(0);
	}

	/**
	 * The Navitar checksum is the seventh byte of the command. This checksum is calculated so that all seven bytes sum
	 * to 0.
	 *
	 * @param command
	 *            A string containing the string for which the checksum is to be calculated.
	 * @return The calculated command checksum.
	 */

	private char calculateChecksum(String command) {
		byte checksum = 0;

		for (int i = 0; i < 6; i++) {
			checksum += command.charAt(i);
		}
		if (checksum < 256)
			checksum = (byte) (256 - checksum);

		return (char) checksum;
	}

	// Motor implementation

	@Override
	public void moveBy(double steps) throws MotorException {
		constructCommand(CommandType.SET_TARGET_INCREMENT, (int) steps);
		sendCommand();
		motorMoving = true;
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		constructCommand(CommandType.SET_TARGET_ABSOLUTE, (int) steps);
		sendCommand();
		motorMoving = true;
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		int steps = HOME;
		if (direction > 0)
			steps = LIMIT; // Limit
		constructCommand(CommandType.SET_TARGET_LIMIT, steps);
		sendCommand();
		motorMoving = true;
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		/*
		 * This does seem to work. However when one of the limits is hit the position returns to 0. I have not been able
		 * to find a way to stop this behaviour (SHK).
		 */
		constructCommand(CommandType.SET_CURRENT, (int) steps);
		sendCommand();
	}

	@Override
	public double getPosition() throws MotorException {
		constructCommand(CommandType.GET_CURRENT);
		sendCommand();
		return commandValue;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		constructCommand(CommandType.SET_MAX_VELOCITY, (int) speed);
		sendCommand();
	}

	@Override
	public double getSpeed() throws MotorException {
		constructCommand(CommandType.GET_MAX_VELOCITY);
		sendCommand();
		getCommandValue();
		return commandValue;
	}

	@Override
	public void stop() throws MotorException {
		constructCommand(CommandType.SET_TARGET_LIMIT, STOP);
		sendCommand();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			String msg = "Thread interrupted while stopping " + getName();
			logger.error(msg, e);
			Thread.currentThread().interrupt();
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public void panicStop() throws MotorException {
		stop();
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		MotorStatus status = MotorStatus.READY;

		constructCommand(CommandType.GET_STATUS);
		sendCommand();
		if ((commandValue & BITS_IN_USE_FOR_BUSY) > 0) {
			status = MotorStatus.BUSY;
		} else if (commandValue != 0) {
			if ((commandValue & HOME_SENSOR) == HOME_SENSOR) {
				status = MotorStatus.LOWER_LIMIT;
			} else if ((commandValue & LIMIT_SENSOR) == LIMIT_SENSOR) {
				status = MotorStatus.UPPER_LIMIT;
			}
			motorMoving = false;
		}

		return status;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return motorMoving;
	}

	// XML getters and setters

	/**
	 * @return motor number
	 */
	public int getMotorNumber() {
		return motorNumber;
	}

	/**
	 * @param motorNumber
	 */
	public void setMotorNumber(int motorNumber) {
		this.motorNumber = motorNumber;
	}

	/**
	 * @return navitarControllerName
	 */
	public String getNavitarControllerName() {
		return navitarControllerName;
	}

	/**
	 * @param navitarControllerName
	 */
	public void setNavitarControllerName(String navitarControllerName) {
		this.navitarControllerName = navitarControllerName;
	}
}
