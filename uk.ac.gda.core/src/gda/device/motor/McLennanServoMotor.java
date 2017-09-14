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

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement McLennan PM300 and PM304 servo motor controllers These motors can operate in Master/Slave mode -
 * in which case a a bool value for "IsMaster" and the int value of the "SlaveAxis" will need to be added to the XML
 * file under the Master motor. Sometimes these motors have an offset.
 *
 * @see McLennanMotor
 */
public class McLennanServoMotor extends McLennanMotor {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	// command codes
	private static final String SET_ACTUAL_POS = "AP";

	private static final String GET_ACTUAL_POS = "OA";

	// private static final String GET_ALL = "QA";
	private static final String GET_SPEED = "QS";

	private static final String GET_CONDITION = "CO";

	private static final String SET_GEARBOX = "GB";

	private static final String GET_SET_SPEED = "SP";

	// status flag bit values
	private static final int LOWERLIMIT = 0;

	private static final int UPPERLIMIT = 1;

	private static final int ERROR = 2;

	private static final int IDLE = 3;

	private static final int USER_ABORT = 4;

	private static final int TRACKING = 5;

	private static final int STALLED = 6;

	private static final int EM_STOP = 7;

	private double romID = 0;

	private int axis = 0;

	private int slaveAxis = 0;

	private static final String VER = "V";

	private static final int EARLY_MODEL = 4; // no of status bits

	private static final double CHANGE_MODEL = 4.8; // RomVersion

	private IMcLennanController mcLennanController;

	@Override
	public void configure() {
		try {
			super.configure();
			mcLennanController = getMcLennanController();
			axis = getAxis();
			slaveAxis = getSlaveAxis();
			romID = getRomVersion();
			logger.debug("The Rom version of this motor is " + romID);
			if (isMaster()) {
				checkSlaveGearBox();
			}
		} catch (Exception e) {
			logger.error("Exception caught at McLennanServoMotor.configure()");
		}
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		if (isMaster()) {
			checkSlaveGearBox();
		}
		super.moveBy(steps);
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		if (isMaster()) {
			checkSlaveGearBox();
		}
		super.moveTo(steps);
	}

	/**
	 * Method for motors running in master/slave mode. Checks slave motor is in gearbox mode.
	 *
	 * @throws MotorException
	 */
	public synchronized void checkSlaveGearBox() throws MotorException {
		String reply = checkReplyAxis(mcLennanController.sendCommand(slaveAxis + GET_CONDITION));
		String condition = reply.trim();
		logger.debug("condition = " + condition);
		if (!condition.equalsIgnoreCase("GearBox")) {
			logger.debug("Slave was not in gearbox mode");
			if (condition.equalsIgnoreCase("Idle")) {
				checkReplyAxis(mcLennanController.sendCommand(slaveAxis + SET_GEARBOX));
				// Ignore reply
			} else {
				// README: Likely to be a major problem
				// Should we send a stop?? - may give more problems though
				logger.debug("Slave motor condition is not Idle or Gearbox ");
				throw new MotorException(MotorStatus.FAULT, reply);
			}
		}
	}

	@Override
	public double getPosition() throws MotorException {
		return getValue(GET_ACTUAL_POS, BASE_10);
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		mcLennanController.sendCommand(axis + SET_ACTUAL_POS + steps);
	}

	@Override
	public double getSpeed() throws MotorException {
		double speed = DEFAULT;
		if (romID <= CHANGE_MODEL) {
			speed = getValue(GET_SPEED, GET_SET_SPEED);
		} else {
			speed = getValue(GET_SPEED, SET_SPEED);
		}
		return speed;
	}

	/**
	 * CARE MUST BE TAKEN WHEN USING THIS METHOD. McLennan motors are optimised for performance and changing the speed
	 * setting alters this optimisation.
	 *
	 * @param stepsPerSecond
	 *            the speed to set
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		mcLennanController.sendCommand(axis + SET_SPEED + stepsPerSecond);
	}

	@Override
	public MotorStatus getStatus() {
		status = MotorStatus.FAULT;
		try {
			String reply = checkReplyAxis(mcLennanController.sendCommand(axis + GET_STATUS));
			reply = reply.trim();
			logger.debug("The status reply is :" + reply);
			logger.debug("The length of the reply is :" + reply.length());
			if (reply.charAt(IDLE) == ON)
				status = MotorStatus.READY;
			else
				status = MotorStatus.BUSY;

			if (reply.length() > EARLY_MODEL)
				if (reply.charAt(USER_ABORT) == ON || reply.charAt(TRACKING) == ON || reply.charAt(STALLED) == ON
						|| reply.charAt(EM_STOP) == ON) {
					status = MotorStatus.FAULT;
					logger.error("There is a fault with the motor at axis " + axis);
				}

			if ((reply.charAt(ERROR) == ON) || (reply.charAt(UPPERLIMIT) == ON && reply.charAt(LOWERLIMIT) == ON)) {
				status = MotorStatus.FAULT;
				logger.error("There is a fault with the motor at axis " + axis);
			}

			if (reply.charAt(LOWERLIMIT) == ON) {
				status = MotorStatus.LOWER_LIMIT;
				logger.error("The motor at axis " + axis + " is at its lower limit.");
			}

			if (reply.charAt(UPPERLIMIT) == ON) {
				status = MotorStatus.UPPER_LIMIT;
				logger.error("The motor at axis " + axis + " is at its upper limit.");
			}
		} catch (MotorException e) {
			logger.error("Exception caught - unknown status");
			// README: Have used the following UNKNOWN status to enable
			// calling methods (eg checkStatus()) to return an exception.
			status = MotorStatus.UNKNOWN;
		}
		return status;
	}

	/**
	 * @return rom version
	 * @throws MotorException
	 */
	public double getRomVersion() throws MotorException {
		String tokenString, romString = null;
		double romVersion = DEFAULT;

		try {
			String reply = checkReplyAxis(mcLennanController.sendCommand(axis + GET_ID));
			StringTokenizer splitId = new StringTokenizer(reply);
			while (splitId.hasMoreTokens()) {
				tokenString = splitId.nextToken();

				if (tokenString.startsWith(VER)) {
					logger.debug("String found: " + tokenString);
					romString = tokenString.substring(1, 4);
					romVersion = Double.parseDouble(romString.trim());
					// romVersion = new
					// Double(romString.trim()).doubleValue();
				}
			}
		} catch (Exception e) {
			logger.error("Exception when getting the ROM version");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
		return romVersion;
	}

	/**
	 * Moves the motor in a continuous mode Code not written as soft limits do not apply in this mode and it is
	 * considered an unacceptable risk.
	 *
	 * @param direction
	 *            the direction
	 * @throws MotorException
	 */
	@Override
	public void moveContinuously(int direction) throws MotorException {
		// README: code to be written if needed
		// don't forget offset and isMaster
	}
}
