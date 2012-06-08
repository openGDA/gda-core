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
 * Common base class for real Queensgate and DummyQueensgate. NB. The Piezo documentation and nomenclature use Offset to
 * refer to the positions to be sent and returned. Therefore debug comments have maintained this convention.
 */
public class Queensgate extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private int status;

	private int rawPosition;

	private int positionToSend;

	private int position = 0; // check this about order of collection

	private int axis = 0;

	private double voltage;

	private String queensgateControllerName;

	private PiezoController queensgateController;

	private static final String FRONT_PANEL_VOLTAGE = "FrontPanelVoltage";

	/**
	 * Null argument constructor required by Castor in the instantation phase.
	 */
	public Queensgate() {
	}

	@Override
	public void configure() {
		logger.debug("Finding: " + queensgateControllerName);
		if ((queensgateController = (PiezoController) Finder.getInstance().find(queensgateControllerName)) == null) {
			logger.error("QueensgateController " + queensgateControllerName + " not found");
		} else {
			logger.debug("Found QueensgateCotroller " + queensgateControllerName);

			try {
				String reply = queryPositionAndStatus();

				checkAndConvertReplyInit(reply);

				// FIXME Possibly should send some kind of status object as the
				// argument.

				// README This may need to remain indefinitely, since we need to
				// notify
				// other objects such as Queensgate panel (eg for testing
				// purposes).
				notifyIObservers(this, null);
			} catch (Exception e) {
				logger.error("Queensgate: Exception caught while configuring settings" + e);
			}
		}
	}

	/**
	 * @param queensgateControllerName
	 */
	public void setQueensgateControllerName(String queensgateControllerName) {
		this.queensgateControllerName = queensgateControllerName;
	}

	/**
	 * @return queensgateControllerName
	 */
	public String getQueensgateControllerName() {
		return queensgateControllerName;
	}

	/**
	 * Returns the position, this is equal to the requested offset if rawPosition reply is as expected but is just
	 * assumption really.
	 * 
	 * @return The position as an offset.
	 */
	@Override
	public double getPosition() {
		return position;
	}

	/**
	 * Returns the module number of this Queensgate. For modules 1 and 2 the number returned is the same but if the
	 * module is 3 as specified by the user in the XML configuration file, the number returned is 4 as this is the
	 * number required to communicate with the Queensgate. The Queensgate module is the equivalent of the motor axis.
	 * 
	 * @return The Queensgate module number.
	 */
	public int getAxis() {
		return axis;
	}

	/**
	 * Returns the expected Front Panel Voltage calculated from the returned rawPosition. This method currently only
	 * works for Front Panel Voltage, which will return the voltage, otherwise returns null.
	 * 
	 * @param name
	 *            The attribute be returned.
	 * @return The attribute value.
	 */
	@Override
	public Object getAttribute(String name) {
		Object attribute = null;

		if (name.equals(FRONT_PANEL_VOLTAGE)) {
			attribute = new Double(voltage);
		}

		return attribute;
	}

	/**
	 * Converts the status (0 for good, 1 for bad) to a motorStatus for the positioner to use. The Queensgate is
	 * emulating a motor.
	 * 
	 * @return The status of the Queensgate.
	 */
	@Override
	public MotorStatus getStatus() {
		MotorStatus motorStatus = MotorStatus.FAULT;

		if (status == 0) {
			motorStatus = MotorStatus.READY;
		}

		return (motorStatus);
	}

	/**
	 * Converts a fourteen bit integer into the required four digit string for sending to device. The number is
	 * converted to two's complement form before converting to a hexadecimal string.
	 * 
	 * @param i
	 *            An integer to convert.
	 * @return The converted number string.
	 */
	private String fourDigitTwosComplementString(int i) {
		int j = Math.abs(i);

		logger.debug("i is " + i + " j is " + j);

		if (i < 0) {
			j = (~j) + 1;
		}

		logger.debug("i is " + i + " j is " + j);

		j = j & 0x3fff;
		logger.debug("i is " + i + " j is " + j);

		// README - Queensgate requires uppercase commands
		String rtrn = Integer.toHexString(j).toUpperCase();

		logger.debug("j in hex is " + rtrn);

		while (rtrn.length() < 4) {
			rtrn = "0" + rtrn;
		}

		return rtrn;
	}

	/**
	 * A request from a program to set the offset of the Queensgate, which is the equivalent of a moveTo in a motor. The
	 * range is assumed to be -8192 to 8191. Requested positions outside this range are sent to the relevant minimum or
	 * maximum position. This position then needs converting to a string readable by the Queensgate controller.
	 * 
	 * @param steps
	 *            the requested offset position
	 * @throws MotorException
	 */
	@Override
	public void moveTo(double steps) throws MotorException {
		String positionCommand;
		positionToSend = (int) steps;

		if (steps < -8192) {
			positionToSend = -8192;
		}

		if (steps > 8191) {
			positionToSend = 8191;
		}

		logger.debug("Queensgate moveTo position to send is " + positionToSend);

		// construct the required strings and send them to the device
		positionCommand = "I" + fourDigitTwosComplementString(positionToSend);

		queensgateController.setPosition(axis, positionCommand);

		// The requested offset (positionToSend) is stored to allow comparison
		// with the coverted rawPosition in the subsequent method calls.
		String reply = queryPositionAndStatus();

		checkAndConvertReply(reply);

		// FIXME Possibly should send some kind of status object as the
		// argument.

		// README This may need to remain indefinitely, since we need to notify
		// other objects such as Queensgate panel (eg for testing purposes).
		notifyIObservers(this, null);
	}

	/**
	 * A request from a program to change the offset, which is the equivalent of a moveBy in a motor. This method calls
	 * MoveTo.
	 * 
	 * @param change
	 *            the required offset change
	 * @throws MotorException
	 */
	@Override
	public void moveBy(double change) throws MotorException {
		moveTo(position + ((int) change));
	}

	/**
	 * Sends the commands required to return the current rawPosition (offset), converts this value to a position
	 * (offset) and compares with requested position (offset). Notifies when the process is complete.
	 * 
	 * @return The Queensgate controller reply.
	 * @throws MotorException
	 */
	private String queryPositionAndStatus() throws MotorException {
		String reply = null;
		reply = queensgateController.getPositionAndStatus(axis);

		return reply;
	}

	/**
	 * Examine the Queensgate controller reply, extracting the raw position and status. The returned position value is
	 * not as fine-grained as the desired position, so the difference is examined. If the error is too large (greater
	 * than 4), then the status flag is set to indicate an error condition.
	 * 
	 * @param reply
	 *            The Queensgate controller reply.
	 * @throws MotorException
	 */
	private void checkAndConvertReplyInit(String reply) throws MotorException {
		// interpret reply as 12bit rawPosition and 4bit status
		rawPosition = Integer.parseInt(reply.substring(0, 3), 16);
		int rawStatus = Integer.parseInt(reply.substring(3, 4), 16);

		logger.debug("Read values from device: rawPosition " + rawPosition + " and status " + rawStatus);

		// FIXME This will return the status to ok if all three modules are ok.
		// This may need revisiting, to have individual status flags, since we
		// may
		// need to be able to carry on controlling modules which are not in out
		// of
		// range error status.

		if ((rawStatus & 0x7) == 0) {
			status = 0;
		} else {
			logger.error("One of the modules has out of range error " + rawStatus);
			status = 1;

			throw new MotorException(MotorStatus.FAULT, "One of the modules has out of range error" + rawStatus);
		}

		// should consider here the 16.5 controller's inability to
		// return negative positions
		if ((rawPosition & 0x800) == 0x800) {
			rawPosition = -((~rawPosition + 1) & 0xfff);
		}

		logger.debug("After complementing, the rawPosition is " + rawPosition);

		// voltage is intended only to give a rough guide to what
		// the front panel voltage should be.
		voltage = rawPosition * 5.3 / 2048.0;

		// If the reported rawPosition corresponds to an offset within
		// four of the requested then assume we got what we wanted
		// NB because the rawPosition and offset scales go in reverse
		// directions rawPosition 0 corresponds to offsets -1, -2, -3
		// and -4 and rawPosition -1 corresponds to offsets 0, 1, 2, 3.
		// This means that we have to do different calculations for
		// positive and negative positions.
		if (rawPosition >= 0) {
			position = -(4 * rawPosition + 1);
		} else {
			position = -4 * (rawPosition + 1);
		}

		// save the current position (offset) of the module.
		positionToSend = position;

		logger.debug("Queensgate values after reply from device:");
		logger.debug("Position read:           " + rawPosition);
		logger.debug("Offset (derived from rawPosition):  " + position);
		logger.debug("Voltage (from rawPosition): " + voltage);
		logger.debug("Raw Status:                 " + rawStatus);
		logger.debug("Status:                     " + status);
		logger.debug(" ");
	}

	/**
	 * Examine the Queensgate controller reply, extracting the raw position and status. The returned position value is
	 * not as fine-grained as the desired position, so the difference is examined. If the error is too large (greater
	 * than 4), then the status flag is set to indicate an error condition.
	 * 
	 * @param reply
	 *            The Queensgate controller reply.
	 * @throws MotorException
	 */
	private void checkAndConvertReply(String reply) throws MotorException {
		// interpret reply as 12bit rawPosition and 4bit status
		rawPosition = Integer.parseInt(reply.substring(0, 3), 16);
		int rawStatus = Integer.parseInt(reply.substring(3, 4), 16);

		logger.debug("Read values from device: rawPosition " + rawPosition + " and status " + rawStatus);

		// FIXME This will return the status to ok if all three modules are ok.
		// This may need revisiting, to have individual status flags, since we
		// may
		// need to be able to carry on controlling modules which are not in out
		// of
		// range error status.

		if ((rawStatus & 0x7) == 0) {
			status = 0;
		} else {
			logger.error("One of the modules has out of range error " + rawStatus);
			status = 1;

			throw new MotorException(MotorStatus.FAULT, "One of the modules has out of range error" + rawStatus);
		}

		// should consider here the 16.5 controller's inability to
		// return negative positions
		if ((rawPosition & 0x800) == 0x800) {
			rawPosition = -((~rawPosition + 1) & 0xfff);
		}

		logger.debug("After complementing, the rawPosition is " + rawPosition);

		// voltage is intended only to give a rough guide to what
		// the front panel voltage should be.
		voltage = rawPosition * 5.3 / 2048.0;

		// If the reported rawPosition corresponds to an offset within
		// four of the requested then assume we got what we wanted
		// NB because the rawPosition and offset scales go in reverse
		// directions rawPosition 0 corresponds to offsets -1, -2, -3
		// and -4 and rawPosition -1 corresponds to offsets 0, 1, 2, 3.
		// This means that we have to do different calculations for
		// positive and negative positions.
		if (rawPosition >= 0) {
			position = -(4 * rawPosition + 1);
		} else {
			position = -4 * (rawPosition + 1);
		}

		logger.debug("Requested offset, Position, difference: " + positionToSend + ", " + position + ", "
				+ Math.abs(positionToSend - position));
		// FIXME QG positional error status needs resolving!
		/*
		 * if (Math.abs(positionToSend - position) < 4) { position = positionToSend; } else { // FIXME this needs
		 * testing to ensure errors are not thrown // unnecessarily. Message statement added for monitoring purposes.
		 * Message.out("Offset reached does not correspond to offset requested"); status = 1; }
		 */
		logger.debug("Queensgate values after reply from device:");
		logger.debug("Offset (requested):      " + positionToSend);
		logger.debug("Position read:           " + rawPosition);
		logger.debug("Offset (derived from rawPosition):  " + position);
		logger.debug("Voltage (from rawPosition): " + voltage);
		logger.debug("Raw Status:                 " + rawStatus);
		logger.debug("Status:                     " + status);
		logger.debug(" ");
	}

	/**
	 * Sets the module number of this Queensgate. The Queensgate module is the equivalent of the motor axis.
	 * 
	 * @param axis
	 *            The Queensgate module number to set.
	 */
	public void setAxis(int axis) {
		this.axis = axis;
	}

	// The following methods are implemented to satisfy Motor interface.
	// They do
	// not apply to the Queensgate.

	@Override
	public void moveContinuously(int direction) throws MotorException {
	}

	@Override
	public void setPosition(double steps) throws MotorException {
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
	}

	@Override
	public double getSpeed() throws MotorException {
		return 0;
	}

	@Override
	public void stop() throws MotorException {
	}

	@Override
	public void panicStop() throws MotorException {
	}

	@Override
	public boolean isMoving() throws MotorException {
		return false;
	}

	@Override
	public void correctBacklash() throws MotorException {
	}

	@Override
	public void setSpeedLevel(int speedLevel) throws MotorException {
	}

	@Override
	public boolean isHomeable() {
		return false;
	}

	@Override
	public boolean isHomed() {
		return false;
	}

	@Override
	public void home() throws MotorException {
	}

	@Override
	public boolean isLimitsSettable() {
		return false;
	}

	@Override
	public void setSoftLimits(double min, double max) throws MotorException {
	}

}