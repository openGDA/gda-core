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
 * Class to implement features common to all McLennan motor controllers. All McLennans must have an Axis number and a
 * McLennanController to communicate and all have a channelSelector. The channelSelector is a dummy (ie has no effect)
 * for McLennans without channels. This design is based on the fact that 381s (a type of stepper) can only accept
 * 'doing' commands whilst not moving, but can accept query commands at any time, thus the timing of the channel release
 * is critical. Any class that overrides moveTo() and moveBy() MUST take care to accomodate backOffSteps - needed to
 * give a minimum move off a hard limit to stop the DOF preventing movements in both directions if limit is still
 * showing as on. It has been assumed that 608s will have similar constraints.
 *
 * @see McLennanChannelSelector
 * @see McLennanController
 * @see McLennanServoMotor
 * @see McLennanStepperMotor
 * @see McLennan600Motor
 */
public abstract class McLennanMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private static final String SPLIT_CHARS = "#:";

	// NB the space in PARAM_CHARS is essential for parsing the reply
	// to the 600 QS command.
	private static final String PARAM_CHARS = "=:, ";

	// global commands
	protected static final char PANIC_STOP = '\03'; // Control+C

	// command codes used by all McLennan motor controllers
	protected static final String SET_CHANNEL = "CH";

	protected static final String GET_ID = "ID";

	protected static final String GET_STATUS = "OS";

	protected static final String SET_SPEED = "SV";

	private static final String MOVE_ABS = "MA";

	private static final String MOVE_REL = "MR";

	private static final String CONTROL_STOP = "ST";

	private static final String LOWER_LIMIT = "LL";

	private static final String UPPER_LIMIT = "UL";

	// constants used by all McLennan classes:
	protected static final int DEFAULT = -1;

	protected static final int BASE_10 = 10;

	protected static final char OFF = '0';

	protected static final char ON = '1';

	private int axis;

	private int channel;

	private int slaveAxis = DEFAULT;

	private double offset = 0;

	private boolean isMaster = false;

	private String mcLennanControllerName;
	private IMcLennanController mcLennanController;
	protected ChannelSelector channelSelector;
	protected MotorStatus status;

	// README default value of 1 for backOffsSteps
	// for motors without "squashy" hard limits
	protected int backOffSteps = 1;

	/**
	 * Constructor
	 */
	public McLennanMotor() {
	}

	@Override
	public void configure() {
		if (isMaster() && slaveAxis == DEFAULT)
			logger.debug("Slave Axis is not set for this master");

		logger.debug("channel is " + channel);
		// README Whether or not the motor has a channel - it needs a
		// ChannelSelector - see main class description.
		channelSelector = McLennanChannelSelectorFactory.createChannelSelector(this.mcLennanController, this.axis,
				this.channel);
		logger.debug("McLennanMotor: channel selector " + channelSelector);

	}

	public IMcLennanController getMcLennanController() {
		return mcLennanController;
	}

	public void setMcLennanController(IMcLennanController mcLennanController) {
		this.mcLennanController = mcLennanController;
	}

	/**
	 * @param mcLennanControllerName
	 */
	public void setMcLennanControllerName(String mcLennanControllerName) {
		this.mcLennanControllerName = mcLennanControllerName;
	}

	/**
	 * @return mclennan controller name
	 */
	public String getMcLennanControllerName() {
		return mcLennanControllerName;
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

	/**
	 * @param channel
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * @return channel
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param offset
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}

	/**
	 * @return offset
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * @param backOffSteps
	 */
	public void setBackOffSteps(int backOffSteps) {
		this.backOffSteps = backOffSteps;
	}

	/**
	 * @return back off steps
	 */
	public int getBackOffSteps() {
		return backOffSteps;
	}

	/**
	 * @param slaveAxis
	 */
	public void setSlaveAxis(int slaveAxis) {
		this.slaveAxis = slaveAxis;
		isMaster = true;
	}

	/**
	 * @return slave axis
	 */
	public int getSlaveAxis() {
		return slaveAxis;
	}

	/**
	 * @param isMaster
	 */
	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	/**
	 * @return boolean
	 */
	public boolean isMaster() {
		return isMaster;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (checkStatus() == MotorStatus.BUSY);
	}

	/**
	 * Move the physical motor BY the given number of steps. README Must cast the double value from the Positioner to
	 * int as McLennan sends a 32 bit value and trunkates any value beyond the decimal point in a given command.
	 * Converting a double value to a String results in "1.2E9" for example, thus the motor will only move 1 step rather
	 * than the 1,200,000,000 intended. In addition, motors with "squashy" hard limits must have a minimum move to get
	 * off hard limit, otherwise the combination of the Positioner and the motor replies will prevent further movements
	 * in both directions. backOffSteps must always be +ve. It has a default of 1. The backlash move must be allowed to
	 * happen.
	 *
	 * @param steps
	 *            The number of steps to move BY
	 * @throws MotorException
	 */
	@Override
	public void moveBy(double steps) throws MotorException {
		// README See documentation in method header.
		if ((status == MotorStatus.UPPER_LIMIT || status == MotorStatus.LOWER_LIMIT) && backOffSteps > 1) {
			logger.warn("McLennanMotor: moveTo found limit on, checking move size");
			if (Math.abs(steps) < backOffSteps) {
				// change it for the backOffSteps with the same sense as steps
				logger.warn("McLennanMotor: Move size too small, changing from: " + steps + " to " + backOffSteps);
				double moveSign = (steps < 0.0) ? -1.0 : 1.0;
				steps = backOffSteps * moveSign;
			}
		}

		// now safe to call addInBacklash for all cases
		steps = addInBacklash(steps);
		if (channelSelector.selectChannel(this, channel)) {
			// README See documentation in method header
			mcLennanController.sendCommand(axis + MOVE_REL + (int) Math.round(steps));
		} else {
			throw new MotorException(MotorStatus.BUSY, "The channel selector is busy");
		}
	}

	/**
	 * Move the physical motor TO the given number of steps. README Must cast the double value from the Positioner to
	 * int as McLennan sends a 32 bit value and trunkates any value beyond the decimal point in a given command.
	 * Converting a double value to a String results in "1.2E9" for example, thus the motor will only move 1 step rather
	 * than the 1,200,000,000 intended. In addition, motors with "squashy" hard limits must have a minimum move to get
	 * off hard limit, otherwise the combination of the Positioner and the motor replies will prevent further movements
	 * in both directions. backOffSteps must always be +ve. It has a default of 1. The backlash move must be allowed to
	 * happen.
	 *
	 * @param steps
	 *            The number of steps to move TO
	 * @throws MotorException
	 */
	@Override
	public void moveTo(double steps) throws MotorException {
		double currentPosition = getPosition();
		double increment = steps - currentPosition;

		// README See documentation in method header.
		if ((status == MotorStatus.UPPER_LIMIT || status == MotorStatus.LOWER_LIMIT) && backOffSteps > 1) {
			logger.warn("McLennanMotor: moveTo found limit on, checking move size");
			if (Math.abs(increment) < backOffSteps) {
				// change it for the backOffSteps with the same sense as
				// increment
				logger.warn("McLennanMotor: Move size too small, changing from: " + steps + " to " + backOffSteps);
				double moveSign = (increment < 0.0) ? -1.0 : 1.0;
				steps = currentPosition + (backOffSteps * moveSign);
			}
		}

		// now safe to call addInBacklash in all cases.
		steps = addInBacklash(increment) + currentPosition;
		if (channelSelector.selectChannel(this, channel)) {
			// README See documentation in method header
			mcLennanController.sendCommand(axis + MOVE_ABS + (int) Math.round(steps));
		} else {
			throw new MotorException(MotorStatus.BUSY, "The channel selector is busy");
		}
	}

	@Override
	public void stop() throws MotorException {
		if (channelSelector.selectChannel(this, channel)) {
			mcLennanController.sendCommand(axis + CONTROL_STOP);
			// README If the motor is a 381:
			// The channelSelector should be released during this call ONLY
			// when the motor is NOT moving.
			// Calling getStatus here means the release is controlled by the
			// current state of the motor.
			// The overhead for other non-servoed steppers must be accepted.
			status = getStatus();
		} else {
			throw new MotorException(MotorStatus.BUSY, "The channel selector is busy");
		}
	}

	@Override
	public void panicStop() throws MotorException {
		// FIXME - This command affects ALL axes on the serial line and requires
		// a RESET command (RS) sending TO EACH AXIS afterwards.
		// This cannot be done here nor can the 'put back in gearbox' command,
		// which also needs sending on appropriate axes.
		mcLennanController.globalCommand(PANIC_STOP);
	}

	/**
	 * Reset soft limits after a setPosition command has altered the value of the actual position. Sends a command to
	 * the motor to write the new limits to its ROM. This method uses the flag allowSetLimits. README Must cast the
	 * double value from the Positioner to int as McLennan sends a 32 bit value and trunkates any value beyond the
	 * decimal point in a given command. Converting a double value to a String results in "1.2E9" for example, thus the
	 * motor will only see 1 step rather than the 1,200,000,000 intended.
	 *
	 * @param minPosition
	 *            amount to adjust lower softlimit
	 * @param maxPosition
	 *            amount to adjust upper softlimit
	 * @throws MotorException
	 */
	@Override
	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException {
		if (channelSelector.selectChannel(this, channel)) {
			// README See documentation in method header
			synchronized (this) {
				mcLennanController.sendCommand(axis + LOWER_LIMIT + (int) minPosition);
				mcLennanController.sendCommand(axis + UPPER_LIMIT + (int) maxPosition);
				channelSelector.releaseChannel(this);
			}
		} else {
			throw new MotorException(MotorStatus.BUSY, "Unable to set limits, channel selector is busy");
		}
	}

	/**
	 * A wrapper method to enable getStatus() to throw a MotorException to a calling method. README: Perhaps can be
	 * disposed of if getStatus() throws MotorException?.
	 *
	 * @return Motor
	 * @exception MotorException
	 */
	protected MotorStatus checkStatus() throws MotorException {
		status = getStatus();
		if (status == MotorStatus.UNKNOWN) {
			// README Important to release ChannelSelector here
			channelSelector.releaseChannel(this);
			// The UNKNOWN message is important for debugging
			throw new MotorException(MotorStatus.FAULT, "Exception while getting status - status UNKNOWN ");
		}
		return status;
	}

	/**
	 * Split reply from McLennan motor into axis and message components. Check that the correct axis has responded.
	 *
	 * @param reply
	 *            Reply from motor
	 * @return message component
	 * @exception MotorException
	 */
	protected String checkReplyAxis(String reply) throws MotorException {
		StringTokenizer splitAxis = new StringTokenizer(reply, SPLIT_CHARS);
		String replyString = null;

		if (splitAxis.countTokens() > 1) // delimiter found
		{
			try {
				String firstToken = splitAxis.nextToken().trim();
				int axisValue = Integer.parseInt(firstToken);
				logger.debug("Axis value is: " + axisValue);

				if (axisValue != axis) {
					throw new MotorException(MotorStatus.FAULT, "Wrong axis responded" + reply);
				}
				replyString = splitAxis.nextToken();
				logger.debug("Message component is:  " + replyString);
			} catch (NumberFormatException e) {
				throw new MotorException(MotorStatus.FAULT, e.toString());
			}
		} else {
			// return the whole string
			// the address component may be switched off
			replyString = reply;
		}
		return replyString;
	}

	/**
	 * Send a command to the motor and parse the reply to extract the integer value.
	 *
	 * @param command
	 *            Command code
	 * @param radix
	 *            Base for conversion
	 * @return Integer value
	 * @throws MotorException
	 */
	public int getValue(String command, int radix) throws MotorException {
		String reply, paramValue = null;
		int value = DEFAULT;
		try {
			// README The ChannelSelect code has been removed from this
			// method
			// and taken back into the calling methods to give them
			// responsibility.
			reply = mcLennanController.sendCommand(axis + command);

			reply = checkReplyAxis(reply);
			paramValue = getParamValue(reply);
			value = Integer.parseInt(paramValue.trim(), radix);
			logger.debug("The value is: " + value);
		} catch (NumberFormatException e) {
			logger.debug("Number format exception while getting value " + e.toString());
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
		return value;
	}

	/**
	 * Send a command to the motor and parse the replies to extract the integer value matching "param"
	 *
	 * @param command
	 *            Command name
	 * @param param
	 *            Parameter to search for
	 * @return Integer value
	 * @throws MotorException
	 */
	public int getValue(String command, String param) throws MotorException {
		String reply, paramValue = null;
		int value = DEFAULT;
		try {
			// README The ChannelSelect code has been removed from this
			// method
			// and taken back into the calling methods to give them
			// responsibility.
			reply = mcLennanController.sendCommand(axis + command);
			reply = checkReplyAxis(reply);
			paramValue = getParamValue(reply, param);
			value = Integer.parseInt(paramValue.trim());
			logger.debug("The value is: " + value);
		} catch (NumberFormatException e) {
			logger.debug("Number format exception while getting value " + e.toString());
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
		return value;
	}

	/**
	 * Get value from a name value string, for example where reply is "AP=1000".
	 *
	 * @param reply
	 *            whole reply from motor, containing ONLY one name value pair.
	 * @return parameter value component
	 */
	private String getParamValue(String reply) {
		StringTokenizer splitParam = new StringTokenizer(reply.trim(), PARAM_CHARS);
		String paramString = null;

		if (splitParam.countTokens() > 1 && splitParam.countTokens() < 3) {
			// Single instance of split character found
			// Discard the first token and return the value token
			paramString = splitParam.nextToken();
			logger.debug("The identifier is: " + paramString);
			paramString = splitParam.nextToken();
		} else {
			// None found, return whole string
			paramString = reply;
			logger.debug("The reply is: " + paramString);
		}
		return paramString;
	}

	/**
	 * Get the value for a particular name from a name value string, for example where the reply is "SV=200, SC=1,SA=20,
	 * SD=21".
	 *
	 * @param reply
	 *            whole reply from motor, containing MULTIPLE name value pairs.
	 * @param param
	 *            parameter to find for example SA
	 * @return matching parameter value component
	 * @exception MotorException
	 */
	private String getParamValue(String reply, String param) throws MotorException {
		StringTokenizer splitParam = new StringTokenizer(reply.trim(), PARAM_CHARS);
		String paramString = null;
		try {
			while (splitParam.hasMoreTokens()) {
				paramString = splitParam.nextToken();
				// match param to reply tokens
				if (paramString.equalsIgnoreCase(param)) {
					paramString = splitParam.nextToken();
					logger.debug("paramString found: " + paramString);
					break;
				}
				paramString = null;
			}
		} catch (Exception e) {
			logger.debug("Exception occurred while finding parameter");
			throw new MotorException(MotorStatus.FAULT, e.toString());
		}
		return paramString;
	}
}