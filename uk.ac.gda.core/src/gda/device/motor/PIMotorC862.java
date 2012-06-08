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
public class PIMotorC862 extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	// commands
	private static final byte ADDRESS_SELECTION = 0x01;

	private static final byte axis = 0x30;

	private static final String TELL_POSITION = "TP";

	private static final String TELL_STATUS = "TS";

	private static final String MOVE_ABS = "MA";

	private static final String MOVE_REL = "MR";

	private static final String TELL_VELOCITY = "TY";

	private static final String SET_VELOCITY = "SV";

	private static final String ABORT_MOTION = "AB";

	private static final String STOP_MOTION = "AB1";

	private static final int MAX_POSITION = 1073741823;

	private static final int MIN_POSITION = -1073741824;

	private String piControllerName;

	private PIControllerC862 piController;

	private MotorStatus status;

	private static final byte[] errorCodes = { 0x00, 0x01, 0x02, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };

	/**
	 * Constructor.
	 */
	public PIMotorC862() {
	}

	@Override
	public void configure() {
		logger.debug("Finding: " + piControllerName);
		if ((piController = (PIControllerC862) Finder.getInstance().find(piControllerName)) != null) {
			// send the initialisation sequence to put the PI in servo mode
			try {
				byte[] address = new byte[2];
				address[0] = ADDRESS_SELECTION;// Address Selection Code: 0x01
				// +
				// 0x00
				address[1] = axis;
				String command = new String(address);
				String reply = piController.sendCommand(command, true);
				System.out.println(reply);

				// piController.sendCommand(MOTOR_ON, false);
				// piController.sendCommand(this.BRAKE_OFF, false);

				logger.debug("Initialisation string sent");
			} catch (Exception e) {
				logger.error("Exception caught initialising the PI_C862 DC Motor Controller");
			}
		} else {
			logger.error("piController " + piControllerName + " not found");
		}
	}

	/**
	 * 
	 */
	public void reset() {
		try {
			byte[] address = new byte[2];
			address[0] = ADDRESS_SELECTION;// Address Selection Code: 0x01
			// + 0x00
			address[1] = axis;
			String command = new String(address);
			String reply = piController.sendCommand(command, true);
			System.out.println(reply);

			// piController.sendCommand(MOTOR_ON, false);
			// piController.sendCommand(BRAKE_OFF, false);

			logger.debug("Initialisation string sent");
		} catch (Exception e) {
			logger.error("Exception caught initialising the PI_C862 DC Motor Controller");
		}
	}

	/*
	 * >>>md.sendCommand("tp") '\n\x03P:+0000000117' md.sendCommand("TS") '\n\x03S:04 AC 00 03 00 02'
	 */

	/**
	 * @return String
	 */
	public String tellPosition() {
		String reply = null;
		try {
			reply = piController.sendCommand(TELL_POSITION, true);
			logger.debug("Initialisation string sent");
		} catch (Exception e) {
			logger.error("Exception caught initialising the PI_C862 DC Motor Controller");
		}

		return reply;
	}

	/**
	 * @param command
	 * @param replyGenerated
	 * @return String
	 */
	public String sendCommand(String command, boolean replyGenerated) {
		String reply = "";
		try {
			reply = piController.sendCommand(command, replyGenerated);
		} catch (Exception e) {
			logger.error("Exception caught sending command to PI_C862 DC Motor Controller");
		}
		System.out.println("Command send: " + command);
		System.out.println("Reply:      : " + reply);
		return reply;
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
		String command = TELL_STATUS;
		String reply = null;
		try {
			reply = piController.sendCommand(command, true);
		} catch (Exception e) {
			logger.error("Exception caught sending command to PI_C862 DC Motor Controller");
		}

		System.out.println("Current status is: " + reply);
		// returned status string: '\n\x03S:04 AC 00 03 00 02'
		if (reply == null)
			return status = MotorStatus.FAULT;

		String[] sr = reply.split(":");
		String[] fs = sr[1].split(" ");
		if (fs.length != 6) {
			System.out.println("Somethings wrong");
			status = MotorStatus.FAULT;
			return status;
		}

		// byte lmb = Byte.parseByte(fs[0]);
		int lmb = (Integer.decode("0x" + fs[0])).intValue();
		if ((lmb & 0x04) == 0) { // LM629 trajectory not complete
			status = MotorStatus.BUSY;
			System.out.println("LM629 busy");
			return status;
		}
		System.out.println("LM629 Trajectory completed");

		// First to check the 6F: Error Codes
		// byte errorCode = Byte.parseByte(fs[5]);
		int errorCode = (Integer.decode("0x" + fs[5])).intValue();
		logger.debug("PIMotor: Error code = " + errorCode);
		if (errorCode != errorCodes[0]) {
			status = MotorStatus.FAULT;
		} else {

			status = MotorStatus.READY;
		}

		return status;
	}

	@Override
	public void moveBy(double newPos) throws MotorException {
		int steps = (int) newPos;
		String command = MOVE_REL + " " + steps;
		piController.sendCommand(command, false);
	}

	@Override
	public void moveTo(double newPos) throws MotorException {
		// README The fault that happens when too high or too low a value is
		// sent
		// results in a complete loss of communication.
		// This hard coded fix limits this and should avoid problems.
		int steps = (int) newPos;
		if (steps > MAX_POSITION) {
			steps = MAX_POSITION;
		} else if (steps < MIN_POSITION) {
			steps = MIN_POSITION;
		}
		String command = MOVE_ABS + " " + steps;
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
		String command = TELL_POSITION;
		String reply = piController.sendCommand(command, true);
		// returned string: '\n\x03P:+0000000117'
		String[] sr = reply.split(":");
		double position = Double.parseDouble(sr[1]);
		return position;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		String command = SET_VELOCITY + " " + (int) speed;
		piController.sendCommand(command, false);
	}

	@Override
	public double getSpeed() throws MotorException {
		String command = TELL_VELOCITY;
		String reply = piController.sendCommand(command, true);
		// returned string: '\n\x03Y:+0000000117'
		String[] sr = reply.split(":");
		double speed = Double.parseDouble(sr[1]);
		return speed;
	}

	@Override
	public void stop() throws MotorException {
		String command = STOP_MOTION;
		piController.sendCommand(command, false);
	}

	@Override
	public void panicStop() throws MotorException {
		String command = ABORT_MOTION;
		piController.sendCommand(command, false);
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
