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
 * Class to implement McLennan PM301, PM341 and PM381 stepper motor controllers. PM341 motor controllers can operate as
 * closed loop or open loop, which is established by checking a status bit. PM381 motor controllers are multichannel and
 * need to have a "Channel" number added to the XML file.
 *
 * @see McLennanMotor
 */
public class McLennanStepperMotor extends McLennanMotor {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private static final String VER = "v";

	// command codes specific to this class
	private static final String SET_CMD_POS = "CP";

	private static final String SET_ACT_POS = "AP";

	private static final String GET_CMD_POS = "OC";

	private static final String GET_ACT_POS = "OA";

	private static final String GET_SPEED = "OV";

	// private static final String GET_ALL = "QA";

	// status flag bit values
	private static final int ERROR = 0;

	// private static final int AUTO_CORRECT = 1; // 341 only
	private static final int LOWERLIMIT = 2;

	private static final int UPPERLIMIT = 3;

	private static final int STALLED = 4; // 341 only

	private static final int NOT_IN_WIN = 5; // 341 only

	private static final int BUSY = 6;

	private static final int EM_STOP = 7;

	private static final int LOOP_ACTIVE = 8; // 341 only

	private static final int JOGS_ACTIVE = 9; // 341 only

	private IMcLennanController mcLennanController;

	// private double romID = 0;
	private int axis = 0;

	private boolean isClosedLoop = false;

	// Move this up to McLennanMotor and protect
	// private MotorStatus status = MotorStatus.READY;
	private double position = 0.0;

	private double speed = 0.0;

	@Override
	public void configure() {
		try {
			super.configure();
			logger.debug("configure: after super configure");
			mcLennanController = getMcLennanController();
			logger.debug("configure: controller name is " + mcLennanController.getName());
			axis = getAxis();
			logger.debug("axis is " + axis);
			// not sure if this is necessary - status = MotorStatus.READY;

			isClosedLoop = checkLoopActive();
			logger.debug("The motor is operating as closed loop = " + isClosedLoop);
		} catch (Exception e) {
			logger.error("Exception while initialising the StepperMotor" + e);
		}
	}

	@Override
	public double getPosition() throws MotorException {
		if (channelSelector.selectChannel(this, getChannel())) {
			if (isClosedLoop) {
				position = getValue(GET_ACT_POS, BASE_10);
			} else {
				position = getValue(GET_CMD_POS, BASE_10);
				// README If the motor is a 381:
				// The channelSelector should be released during this call ONLY
				// when the motor is NOT moving.
				// Calling getStatus here means the release is controlled by the
				// current state of the motor.
				// The overhead for other non-servoed steppers must be accepted.
				status = getStatus();
			}
		}
		return position;
	}

	/**
	 * Sends a command to the physical motor to set its position in steps. README Must cast the double value from the
	 * Positioner to int as McLennan sends a 32 bit value and trunkates any value beyond the decimal point in a given
	 * command. Converting a double value to a String results in "1.2E9" for example, thus the motor will only see 1
	 * step rather than the 1,200,000,000 intended.
	 *
	 * @param steps
	 *            The amount of steps to set as the position.
	 * @throws MotorException
	 */
	@Override
	public void setPosition(double steps) throws MotorException {
		if (channelSelector.selectChannel(this, getChannel())) {
			// README See the method header documentation
			if (isClosedLoop) {
				mcLennanController.sendCommand(axis + SET_ACT_POS + (int) steps);
			} else {
				mcLennanController.sendCommand(axis + SET_CMD_POS + (int) steps);
			}
			channelSelector.releaseChannel(this);
		} else {
			throw new MotorException(MotorStatus.BUSY, "Channel selector is busy");
		}
	}

	@Override
	public double getSpeed() throws MotorException {
		if (channelSelector.selectChannel(this, getChannel())) {
			speed = getValue(GET_SPEED, BASE_10);
			channelSelector.releaseChannel(this);
		} else {
			throw new MotorException(MotorStatus.BUSY, "Channel selector is busy");
		}
		return speed;
	}

	/**
	 * Creates a command to send to the physical motor to set its velocity. README Must cast the double value from the
	 * Positioner to int as McLennan sends a 32 bit value and trunkates any value beyond the decimal point in a given
	 * command. Converting a double value to a String results in "1.2E9" for example, thus the motor will only see 1
	 * step rather than the 1,200,000,000 intended.
	 *
	 * @param stepsPerSecond
	 *            The velocity value
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		// README See method header documentation
		if (channelSelector.selectChannel(this, getChannel())) {
			mcLennanController.sendCommand(axis + SET_SPEED + (int) stepsPerSecond);
			channelSelector.releaseChannel(this);
		} else {
			throw new MotorException(MotorStatus.BUSY, "Channel selector is busy");
		}
		return;
	}

	@Override
	public MotorStatus getStatus() {
		String reply = null;

		status = MotorStatus.FAULT;

		try {
			if (channelSelector.selectChannel(this, getChannel())) {
				logger.debug("Channel Selector is locked for this channel");
				reply = mcLennanController.sendCommand(axis + GET_STATUS);
			} else {
				// This will only happen for McLennan 381s. Only one motor can
				// move
				// at a time and therefore any 'doing' commands should be
				// prevented
				// from getting to the controller.
				logger.debug("Channel Selector is NOT locked for this channel");
				return MotorStatus.BUSY;
			}
			reply = checkReplyAxis(reply);
			logger.debug("The status reply from the motor is: " + reply);

			if (reply.charAt(BUSY) == ON) {
				status = MotorStatus.BUSY;
			} else {
				status = MotorStatus.READY;
				channelSelector.releaseChannel(this);
				// README enclose all following checks here because busy flag
				// may
				// also have the following flags set when moving out of an error
				// condition, but the move should still be allowed to continue.
				if (isClosedLoop) {
					if (reply.charAt(NOT_IN_WIN) == ON || reply.charAt(JOGS_ACTIVE) == ON
							|| reply.charAt(STALLED) == ON || reply.charAt(EM_STOP) == ON) {
						status = MotorStatus.FAULT;
						logger.error("There is a fault with the motor at axis " + axis);
						channelSelector.releaseChannel(this);
					}
				}

				if ((reply.charAt(ERROR) == ON) || (reply.charAt(UPPERLIMIT) == ON && reply.charAt(LOWERLIMIT) == ON)) {
					status = MotorStatus.FAULT;
					logger.error("There is a fault with the motor at axis " + axis);
					channelSelector.releaseChannel(this);
				}

				else if (reply.charAt(LOWERLIMIT) == ON) {
					status = MotorStatus.LOWER_LIMIT;
					logger.error("The motor at axis " + axis + " is at its lower limit.");
					channelSelector.releaseChannel(this);
				}

				else if (reply.charAt(UPPERLIMIT) == ON) {
					status = MotorStatus.UPPER_LIMIT;
					logger.error("The motor at axis " + axis + " is at its upper limit.");
					channelSelector.releaseChannel(this);
				}
			}
		} catch (MotorException me) {
			logger.error("Exception caught while getting status");
			// README: Have used the following UNKNOWN status to enable
			// calling methods (eg checkStatus()) to return an exception.
			status = MotorStatus.UNKNOWN;
		}
		return status;
	}

	/**
	 * Gets a status string and checks if loop is active. Used to establish if 341 stepper controllers are acting in
	 * closed loop mode.
	 *
	 * @return boolean
	 * @exception MotorException
	 */
	private boolean checkLoopActive() throws MotorException {
		boolean active = false;
		String reply = null;

		if (channelSelector.selectChannel(this, getChannel())) {
			reply = mcLennanController.sendCommand(axis + GET_STATUS);
			logger.debug("Checking loop active: command sent, status is " + reply);
			channelSelector.releaseChannel(this);
		} else {
			throw new MotorException(MotorStatus.BUSY, "The channel selector is busy.");
		}

		reply = checkReplyAxis(reply);
		if (reply.charAt(LOOP_ACTIVE) == ON) {
			active = true;
		}
		return active;
	}

	/**
	 * Constructs a command to query the ROM version of the firmware.
	 *
	 * @return the ROM Version or a default value when not found
	 * @throws MotorException
	 */
	public double getRomVersion() throws MotorException {
		String reply, tokenString, romString = null;
		double romVersion = DEFAULT;

		try {
			if (channelSelector.selectChannel(this, getChannel())) {
				reply = mcLennanController.sendCommand(axis + GET_ID);
				channelSelector.releaseChannel(this);
			} else {
				throw new MotorException(MotorStatus.BUSY, "The channel selector is busy.");
			}

			reply = checkReplyAxis(reply);
			StringTokenizer splitId = new StringTokenizer(reply);
			while (splitId.hasMoreTokens()) {
				tokenString = splitId.nextToken();

				if (tokenString.startsWith(VER)) {
					// discard this token and return value token
					logger.debug("String found: " + tokenString);
					romString = splitId.nextToken();
					romVersion = Double.parseDouble(romString.trim());
				}
			}
		} catch (Exception e) {
			logger.error("Exception getting the ROM version");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
		return romVersion;
	}

	/**
	 * Moves the motor in a continuous mode Code not written as soft limits do not apply in this mode and it is
	 * considered an unacceptable risk.
	 *
	 * @param direction
	 *            the direction of travel
	 * @throws MotorException
	 */
	@Override
	public void moveContinuously(int direction) throws MotorException {
		// README: code to be written if needed
		// don't forget offset and isMaster
	}
}