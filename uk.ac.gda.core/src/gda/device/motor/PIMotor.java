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
import gda.factory.Finder;

/**
 * Class to implement PI piezo controllers that use the E-816 command set.
 * 
 * @see PIController
 */
public class PIMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	// commands
	private static final String INIT = "SVO";

	private static final String GET_STATUS = "I2C?";

	private static final String MOVE_ABS = "MOV";

	private static final String MOVE_REL = "MVR";

	private static final String GET_ACT_POS = "POS?"; // get actual position

	private static final char ON = '1';

	private static final char axis = 'A';

	private static final int MAX_POSITION = 90;

	private static final int MIN_POSITION = 1;

	private String piControllerName;

	private PIController piController;

	private MotorStatus status;

	private static final int[] errorCodes = { 0, 1, 5, 303, 304, 305, 306 };

	private static final String[] errorMessages = { "No error", "Parameter syntax error",
			"Cannot set position before INI or when servo is off", "Cannot set voltage when servo is on",
			"Received command is too long", "Error in reading/writing EEPROM", "Error in !2C bus" };

	// README - these are for if we decide to make sense of the system
	// replies
	// private String[] systemMessages =
	// { "CHK_SEN0 timeout", "CHK_PEN0 timeout", "CHK_RSEN0 timeout",
	// "CHK_RW0 timeout", "CHK_BF0 timeout", "CHK_BF1 timeout",
	// "CHK_ACK0 timeout", "SLAVE_BUSY timeout" };

	/**
	 * Constructor.
	 */
	public PIMotor() {
	}

	@Override
	public void configure() {
		logger.debug("Finding: " + piControllerName);
		if ((piController = (PIController) Finder.getInstance().find(piControllerName)) != null) {
			// send the initialisation sequence to put the PI in servo mode
			try {
				String command = INIT + " " + axis + ON;
				piController.sendCommand(command, false);
				logger.debug("Initialisation string sent");
			} catch (Exception e) {
				logger.error("Exception caught initialising the piezo unit");
			}
		} else {
			logger.error("piController " + piControllerName + " not found");
		}
	}

	/**
	 * @param mcLennanControllerName
	 */
	public void setPIControllerName(String mcLennanControllerName) {
		this.piControllerName = mcLennanControllerName;
	}

	/**
	 * @return Returns the PIControllerName.
	 */
	public String getPIControllerName() {
		return piControllerName;
	}

	/**
	 * This method queries the error code first to see if an error has been recorded. If it has, a second command is
	 * sent to query the system status.
	 * 
	 * @return MotorStatus equivalent to the error code reading
	 */
	@Override
	public MotorStatus getStatus() {
		int errorCode = 0;
		String systemStatus = "00000000";
		String reply = null;

		// Get the error code first//
		errorCode = piController.getErrorCode();
		logger.debug("PIMotor: Error code = " + errorCode);

		// if all is well do nothing//
		if (errorCode == errorCodes[0]) {
			status = MotorStatus.READY;
			logger.debug("PIMotor: " + errorMessages[0]);
		} else {
			// README - all other errors SHOULD not occur if the piezo is in
			// a fit
			// state, therfore we can assume a FAULT has occurred.
			// Actually this MAY be sledgehammer !!!

			status = MotorStatus.FAULT;
			// ALL this should sort any problem out - so it may be better to
			// return
			// READY as this is likely to be the case?????
			for (int i = 1; i < errorCodes.length; i++) {
				if (errorCode == errorCodes[i]) {
					logger.debug("PIMotor: " + errorMessages[i]);
					// README check for System Status to clear system
					// buffer, but
					// don't worry about trying to make sense of reply!!
					try {
						systemStatus = piController.sendCommand(GET_STATUS, true).trim();
						logger.debug("PIMotor: System status = " + systemStatus);
					} catch (Exception e) {
						logger.error("PIController: Exception caught getting system status " + reply + e);
					}
					break;
				}
			}

		}
		return status;
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		// FIXME Will need to limit this movement in similar way to below if
		// Motors ever can move relative.
		String command = MOVE_REL + " " + axis + steps;
		piController.sendCommand(command, false);
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		// README The fault that happens when too high or too low a value is
		// sent
		// results in a complete loss of communication.
		// This hard coded fix limits this and should avoid problems.
		if (steps > MAX_POSITION) {
			steps = MAX_POSITION;
		} else if (steps < MIN_POSITION) {
			steps = MIN_POSITION;
		}
		String command = MOVE_ABS + " " + axis + steps;
		piController.sendCommand(command, false);
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// deliberately do nothing
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		// README cannot set position - only zero
	}

	@Override
	public double getPosition() throws MotorException {
		String command = GET_ACT_POS + " " + axis;
		String reply = piController.sendCommand(command, true);
		double position = Double.parseDouble(reply.trim());
		return position;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		// deliberately do nothing
	}

	@Override
	public double getSpeed() throws MotorException {
		// deliberately do nothing
		return 0;
	}

	@Override
	public void stop() throws MotorException {
		// deliberately do nothing
	}

	@Override
	public void panicStop() throws MotorException {
		// deliberately do nothing
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (checkStatus() == MotorStatus.BUSY);
	}

	private MotorStatus checkStatus() throws MotorException {
		status = getStatus();
		if (status == MotorStatus.UNKNOWN) {
			// The UNKNOWN message is important for debugging
			throw new MotorException(MotorStatus.FAULT, "Exception while getting status - status UNKNOWN ");
		}
		return status;
	}
}
