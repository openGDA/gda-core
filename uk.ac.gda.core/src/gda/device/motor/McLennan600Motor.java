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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement McLennan PM600 series motion controllers. README: It may be necessary to rewrite some of these
 * methods and/or McLennanMotor methods to accomodate verbose and quiet modes.
 *
 * @see McLennanMotor
 */
public class McLennan600Motor extends McLennanMotor {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	// McLennan command strings.
	private static final String SET_ACTUAL_POS = "AP";

	// private static final String SET_CMD_POS = "CP";
	private static final String GET_ACTUAL_POS = "OA";

	// private static final String GET_CMD_POS = "OC";
	// private static final String GET_ALL = "QA";
	private static final String GET_SPEED = "QS"; // README: also OV

	// private static final String VER = "V";

	// status flag bit values
	private static final int IDLE = 0;
	private static final int ERROR = 1;
	private static final int UPPERLIMIT = 2;
	private static final int LOWERLIMIT = 3;
	private static final int JOGS_ACTIVE = 4;

	// private static final int DATUM_SENSOR = 5; // beyond V3.34
	// private static final int FUTURE_USE_1 = 6;
	// private static final int FUTURE_USE_2 = 7;

	private IMcLennanController mcLennanController;
	private double romID = 0;
	private int axis = 0;

	@Override
	public void configure() {
		try {
			super.configure();
			mcLennanController = getMcLennanController();
			axis = getAxis();
			// romID = getRomVersion();
			logger.debug("The Rom version of this motor is " + romID);
		} catch (Exception e) {
			logger.error("Exception while initialising the 600 Motor");
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
		// README: This assumes required value is the setting: maximum velocity
		// If current speed is required, need to use McLennanStepper method
		return getValue(GET_SPEED, SET_SPEED);
	}

	/**
	 * Sets the speed, Math.rint is used on the speed supplied for minimum surprise to the calling class (but it still
	 * might not be exactly what is expected).
	 *
	 * @param stepsPerSecond
	 *            the speed to set (motor units/second)
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		mcLennanController.sendCommand(axis + SET_SPEED + Math.rint(stepsPerSecond));
	}

	@Override
	public MotorStatus getStatus() {
		status = MotorStatus.FAULT;
		try {
			String reply = checkReplyAxis(mcLennanController.sendCommand(axis + GET_STATUS));

			if (reply.charAt(IDLE) == ON)
				status = MotorStatus.READY;
			else
				status = MotorStatus.BUSY;

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

			if (reply.charAt(JOGS_ACTIVE) == ON) {
				status = MotorStatus.FAULT;
				logger.error("The motor at axis " + axis + " is in jog mode.");
			}
		} catch (MotorException e) {
			logger.error("Exception caught while getting Motor status");
			// README: Have used the following UNKNOWN status to enable
			// calling methods (eg checkStatus()) to return an exception.
			status = MotorStatus.UNKNOWN;
		}
		return status;
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// README: implement moveContinuously if needed.
	}

	/**
	 * This overrides the super class method in order to remove any command echo which may be attached to the beginning
	 * of the reply.
	 *
	 * @param reply
	 *            Reply from motor
	 * @return message component
	 * @exception MotorException
	 */
	@Override
	protected String checkReplyAxis(String reply) throws MotorException {
		// If the reply contains an echoed command then it will be of
		// the form, for example, "3OS<CR>03:00000000". If it
		// does not contain one then it will be of the form,
		// "03:000000". So replies containing a CR character
		// need to have the part up to and including it removed.

		String newReply = reply;

		int crIndex = reply.indexOf('\r');
		if (crIndex > 0) {
			newReply = reply.substring(crIndex + 1);
			logger.debug("motor reply has been modified to " + newReply);
		}

		// With the echoed command removed the super class method can deal
		// with the reply.
		return super.checkReplyAxis(newReply);
	}
}
