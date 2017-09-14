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
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.text.NumberFormat;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parker6kMotor Motor class using an enclosed derived instance of Parker6kController base class for communications via
 * Ethernet or RS232 (both may be wired at once).
 *
 * @see gda.device.Motor
 * @see gda.device.motor.Parker6kController
 * @see gda.device.motor.Parker6kControllerEnet
 * @see gda.device.motor.Parker6kControllerRS232
 */
public class Parker6kMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	/**
	 *
	 */
	public static final int SOFT_LIMIT_ENABLE = 3;

	private static final String terminator = "\r";

	private String className = getClass().getName();

	private Parker6kController parker6kController;

	private String parker6kControllerName;

	private int axisNo;

	private boolean stepper = true;

	private double currentSpeed = 0;

	private double minSpeed = 0;

	private double maxSpeed = 0;

	private double currentPosition = 0;

	private double targetPosition = 0;

	private String axisBitFieldX;

	private String axisBitField;

	private String axisCommas;

	private double encoderScalingFactor = 1.0;

	private String storedPosLocation = "";

	private String homingStatusLocation = "";

	private boolean motorHomingWithStatusCheck = false;

	private String homeCommandString = "";

	private boolean homeable;

	private volatile boolean motorMoving = false; // needed for isMoving()

	private int controllerNo;

	private NumberFormat nf = NumberFormat.getInstance();

	/**
	 * Default constructor
	 */
	public Parker6kMotor() {
	}

	@Override
	public void configure() throws FactoryException {
		logger.debug("Finding: " + parker6kControllerName);
		if ((parker6kController = (Parker6kController) Finder.getInstance().find(parker6kControllerName)) == null) {
			logger.error("Parker6kController " + parker6kControllerName + " not found");
		} else {
			controllerNo = parker6kController.getControllerNo();
			createAxisBitFieldX();
			createAxisBitField();
			createAxisCommas();
			nf.setGroupingUsed(false);
		}

		// display the attributes
		Message();

		try {
			// loadPosition();
			getPosition();
			logger.debug("Loaded motor position " + currentPosition);
		} catch (MotorException e) {
			logger.debug("Failed to obtain motor position. Setting to " + currentPosition);
		}
	}

	/**
	 * Sets the minimum speed (steps/sec?).
	 *
	 * @param minSpeed
	 *            the minimum speed
	 */
	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	/**
	 * Get the minimum speed
	 *
	 * @return the minimum speed
	 */
	public double getMinSpeed() {
		return minSpeed;
	}

	/**
	 * Sets the maximum speed (steps/sec?).
	 *
	 * @param maxSpeed
	 *            the maximum speed
	 */
	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/**
	 * Get the maximum speed
	 *
	 * @return the maximum speed
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Set the axis number in the motor, the minimum value is 1 and the maximum depends on the number of motors this
	 * unit can support.
	 *
	 * @see Parker6kController#getMaxNoOfMotors
	 * @param axisNo
	 *            the axis number
	 */
	public void setAxisNo(int axisNo) {
		this.axisNo = axisNo;
	}

	/**
	 * Get the axis number
	 *
	 * @return the axis number
	 */
	public int getAxisNo() {
		return axisNo;
	}

	/**
	 * Get the minimum position
	 *
	 * @return the minimum position
	 */
	@Override
	public double getMinPosition() {
		return minPosition;
	}

	/**
	 * Get the minimum position
	 *
	 * @return the minimum position
	 */
	@Override
	public double getMaxPosition() {
		return maxPosition;
	}

	/**
	 * Set the motor type
	 *
	 * @param stepper
	 *            true = stepper motor, false = servo motor
	 */
	public void setStepper(boolean stepper) {
		this.stepper = stepper;
	}

	/**
	 * Get the motor type
	 *
	 * @return true = stepper motor, false = servo motor
	 */
	public boolean isStepper() {
		return stepper;
	}

	/**
	 * Set the controller name associated with the motor.
	 *
	 * @param parker6kControllerName
	 *            the contorller name for the motor.
	 */
	public void setParker6kControllerName(String parker6kControllerName) {
		this.parker6kControllerName = parker6kControllerName;
	}

	/**
	 * Get the controller name associated with the motor.
	 *
	 * @return the controller name.
	 */
	public String getParker6kControllerName() {
		return parker6kControllerName;
	}

	/**
	 * Get the encoderScalingFactor.
	 *
	 * @return Returns the encoderScalingFactor.
	 */
	public double getEncoderScalingFactor() {
		return encoderScalingFactor;
	}

	/**
	 * Set the encoderScalingFactor.
	 *
	 * @param encoderScalingFactor
	 *            The encoderScalingFactor to set.
	 */
	public void setEncoderScalingFactor(double encoderScalingFactor) {
		this.encoderScalingFactor = encoderScalingFactor;
	}

	/**
	 * Get the home command string.
	 *
	 * @return Returns the homeCommandString.
	 */
	public String getHomeCommandString() {
		return homeCommandString;
	}

	/**
	 * Set the home command string.
	 *
	 * @param homeCommandString
	 *            The homeCommandString to set.
	 */
	public void setHomeCommandString(String homeCommandString) {
		this.homeCommandString = homeCommandString;
	}

	/**
	 * Get the homing status location.
	 *
	 * @return Returns the homingStatusLocation.
	 */
	public String getHomingStatusLocation() {
		return homingStatusLocation;
	}

	/**
	 * Set hte homing status location.
	 *
	 * @param homingStatusLocation
	 *            The homingStatusLocation to set.
	 */
	public void setHomingStatusLocation(String homingStatusLocation) {
		this.homingStatusLocation = homingStatusLocation;
	}

	/**
	 * Get the stored position location.
	 *
	 * @return Returns the storedPosLocation.
	 */
	public String getStoredPosLocation() {
		return storedPosLocation;
	}

	/**
	 * Set the stored position location.
	 *
	 * @param storedPosLocation
	 *            The storedPosLocation to set.
	 */
	public void setStoredPosLocation(String storedPosLocation) {
		this.storedPosLocation = storedPosLocation;
	}

	/**
	 * Set a falg to indicate whether the motor is homeable.
	 *
	 * @param homeable
	 *            The homeable to set.
	 */
	public void setHomeable(boolean homeable) {
		this.homeable = homeable;
	}

	/**
	 * Moves the motor by the specified number of steps achieved by converting to an absolute move
	 *
	 * @param steps
	 *            the number of steps to move by
	 * @throws MotorException
	 */
	@Override
	public synchronized void moveBy(double steps) throws MotorException {
		// do absolute drive
		moveTo(getPosition() + steps);
	}

	/**
	 * Moves the motor to the specified number of steps
	 *
	 * @param steps
	 *            the number of steps to move to
	 * @throws MotorException
	 */
	@Override
	public synchronized void moveTo(double steps) throws MotorException {
		String command;

		logger.debug(className + " moveTo steps " + steps);

		double tempCurrentPosition = getPosition();
		double increment = steps - tempCurrentPosition;
		steps = addInBacklash(increment) + tempCurrentPosition;

		targetRangeCheck(steps);
		command = "" + controllerNo + "_" + axisNo + "D" + nf.format(steps) + terminator;
		parker6kController.sendCommand(command);

		command = "" + controllerNo + "_" + "GO" + axisBitFieldX + terminator;
		parker6kController.sendCommand(command);

		motorMoving = true;
		targetPosition = steps;
	}

	/**
	 * Sets the current position of the motor
	 *
	 * @param steps
	 *            the position to be set as current
	 * @throws MotorException
	 */
	@Override
	public synchronized void setPosition(double steps) throws MotorException {
		String command;

		command = "" + controllerNo + "_" + "PESET" + axisCommas + steps + terminator;
		parker6kController.sendCommand(command);

		command = "" + controllerNo + "_" + "PSET" + axisCommas + steps + terminator;
		parker6kController.sendCommand(command);

		currentPosition = steps;
	}

	/**
	 * Gets the current position of the motor
	 *
	 * @return the current position (integer truncation possible)
	 * @throws MotorException
	 */
	@Override
	public synchronized double getPosition() throws MotorException {
		String command;
		String mnemonic = axisNo + "TPE";

		command = "" + controllerNo + "_" + mnemonic + terminator;
		String reply = parker6kController.sendCommand(command);

		// README ** encoderScalingFactor added 17_06_04 to integrate stn11.1
		currentPosition = extractPos(reply, mnemonic) * encoderScalingFactor;
		return currentPosition;
	}

	/**
	 * Sets the speed of the motor
	 *
	 * @param stepsPerSecond
	 *            the speed in steps per second
	 * @throws MotorException
	 */
	@Override
	public synchronized void setSpeed(double stepsPerSecond) throws MotorException {
		String command;

		if (stepsPerSecond < getMinSpeed()) {
			throw (new MotorException(MotorStatus.LOWER_LIMIT, stepsPerSecond
					+ " outside lower hardware speed limit of " + getMinSpeed()));
		} else if (stepsPerSecond > getMaxSpeed()) {
			throw (new MotorException(MotorStatus.UPPER_LIMIT, stepsPerSecond
					+ " outside upper hardware speed limit of " + getMaxSpeed()));
		}

		command = "" + controllerNo + "_" + axisNo + "V" + nf.format(stepsPerSecond) + terminator;
		parker6kController.sendCommand(command);

		// assume speed set ok
		currentSpeed = stepsPerSecond;
	}

	/**
	 * Gets the current speed setting of the motor
	 *
	 * @return the speed in steps per second (int truncation possible)
	 * @throws MotorException
	 */
	@Override
	public synchronized double getSpeed() throws MotorException {
		String command;
		String mnemonic = axisNo + "V";

		command = "" + controllerNo + "_" + mnemonic + terminator;
		String reply = parker6kController.sendCommand(command);
		currentSpeed = extractPos(reply, mnemonic);
		return currentSpeed;
	}

	/**
	 * Brings the motor to a controlled stop if possible
	 *
	 * @throws MotorException
	 */
	@Override
	public synchronized void stop() throws MotorException {
		String command;
		command = "" + controllerNo + "_" + "S" + axisBitField + terminator;
		parker6kController.sendCommand(command);
	}

	/**
	 * Brings the motor to an uncontrolled stop if necessary and tidyup
	 *
	 * @throws MotorException
	 */
	@Override
	public synchronized void panicStop() throws MotorException {
		/*
		 * This involves a special command for tidying up before exit. If don't use it with Ethernet 6k then server
		 * hangs up and can't be contacted without a reset (from RS232) or power cycle. a real motor function is needed
		 * for this with all motors !!
		 */
		try {
			stop(); // stop the motor before tidyup
		} catch (MotorException e) {
			logger.debug(className + " : caught MotorException in Stop : " + e.getMessage());
		} catch (Exception e) {
			logger.debug(className + " : caught exception in Stop : " + e.toString());
		} finally {
			parker6kController.tidyup();
		}
	}

	/**
	 * Gets the state of the motor As the Motor interface does not allow exceptions to be thrown all diagnostics must be
	 * thrown away
	 *
	 * @return a value from the MotorStatus enum
	 */
	@Override
	public synchronized MotorStatus getStatus() {
		MotorStatus motorStatus;
		String command;
		String mnemonic = axisNo + "TAS";

		try {
			command = "" + controllerNo + "_" + mnemonic + terminator;
			String reply = parker6kController.sendCommand(command);
			motorStatus = extractStatus(reply, mnemonic);

			// README This next section added for integration of stn11.1
			if (motorHomingWithStatusCheck && (motorStatus == MotorStatus.READY)) {
				// added to integrate stn11.1 17.06.04
				mnemonic = homingStatusLocation;
				command = "" + controllerNo + "_" + homingStatusLocation + terminator;
				reply = parker6kController.sendCommand(command);

				/*
				 * if homing routine not yet finished this will appear as if motor is moving when processed by
				 * Parker6KReply since the value of a binary variable is returned in the format of a TAS reply with the
				 * moving bit set to this value
				 */
				motorStatus = extractStatus(reply, mnemonic + "=");
				if (motorStatus != MotorStatus.BUSY) {
					motorHomingWithStatusCheck = false;
				}
			}
			// end of added integration section

			if (motorStatus != MotorStatus.BUSY) {
				motorMoving = false;
			}
		} catch (MotorException e) {
			logger.debug(className + " : caught MotorException in getStatus : " + e.getMessage());
			motorStatus = MotorStatus.FAULT;
		} catch (Exception e) {
			logger.debug(className + " : caught exception in getStatus : " + e.toString());
			motorStatus = MotorStatus.FAULT;
		}
		return motorStatus;
	}

	@Override
	public synchronized void moveContinuously(int direction) throws MotorException {
		if (direction > 0)
			moveTo(getMaxPosition());
		else
			moveTo(getMinPosition());
	}

	/**
	 * new Motor method to return state of motorMoving flag
	 *
	 * @return true if the motor is moving
	 */
	@Override
	public boolean isMoving() {
		return motorMoving;
	}

	/**
	 * checks if the motor is homeable or not
	 *
	 * @return if the motor is homeable
	 */
	@Override
	public boolean isHomeable() {
		return homeable;
	}

	/**
	 * returns the motor to a initial repeatable starting location Parker motors homing operation has several homing
	 * functions that can be customized to suit the needs of the application. For each motor in a controller a seperate
	 * program for homing should be stored in the controller. This method simply calls that program to perform homing
	 *
	 * @throws MotorException
	 */
	@Override
	public synchronized void home() throws MotorException {
		String command;

		// FIXME *** in stn11.1 code - this second terminator must be removed
		// In CD12 it is likely to cause problems - URGENT NEED TO FIX HOMING***
		command = "" + controllerNo + "_" + homeCommandString + terminator + terminator;
		parker6kController.sendCommand(command);

		motorMoving = true;
		targetPosition = 0;
		if (homingStatusLocation != "") {
			motorHomingWithStatusCheck = true;
		}
	}

	/**
	 * changes the software limits in the motor This method should be used to set the soft limits in the controller, if
	 * hitting hard limits should be avoided
	 *
	 * @param minimum
	 *            the minimum softlimit
	 * @param maximum
	 *            the maximum softlimit
	 * @throws MotorException
	 */
	@Override
	public synchronized void setSoftLimits(double minimum, double maximum) throws MotorException {
		String command;

		double maxValue = Math.max(minimum, maximum);
		double minValue = Math.min(minimum, maximum);

		command = "" + controllerNo + "_" + "LSPOS" + axisCommas + nf.format(maxValue) + terminator;
		parker6kController.sendCommand(command);

		command = "" + controllerNo + "_" + "LSNEG" + axisCommas + nf.format(minValue) + terminator;
		parker6kController.sendCommand(command);

		// FIXME - surrounding this section with an if statement was done on
		// 11.1.
		// but is probably no longer needed - URGENT STEP THROUGH THIS *****
		if (homingStatusLocation == "") {
			command = "" + controllerNo + "_" + "LS" + axisCommas + nf.format(SOFT_LIMIT_ENABLE) + terminator;
			parker6kController.sendCommand(command);

		}
		setMaxPosition(maxValue);
		setMinPosition(minValue);

	}

	/**
	 * @param tmpTarget
	 *            absolute requested target to validate within limits
	 * @throws MotorException
	 */
	private void targetRangeCheck(double tmpTarget) throws MotorException {
		if (tmpTarget < getMinPosition()) {
			throw (new MotorException(MotorStatus.LOWER_LIMIT, tmpTarget + " outside lower hardware limit of "
					+ getMinPosition()));
		} else if (tmpTarget > getMaxPosition()) {
			throw (new MotorException(MotorStatus.UPPER_LIMIT, tmpTarget + " outside upper hardware limit of "
					+ getMaxPosition()));
		}
	}

	/**
	 * displays internal data on screen
	 */
	public void Message() {
		logger.debug(className + " : name               : " + getName());
		logger.debug(className + " : axisNo             : " + axisNo);
		logger.debug(className + " : isStepper          : " + isStepper());
		logger.debug(className + " : minSpeed           : " + getMinSpeed());
		logger.debug(className + " : maxSpeed           : " + getMaxSpeed());
		logger.debug(className + " : currentPosition    : " + currentPosition);
		logger.debug(className + " : minPosition        : " + getMinPosition());
		logger.debug(className + " : maxPosition        : " + getMaxPosition());
		logger.debug(className + " : Parker6kController : " + parker6kController.getName());

		// view enclosed controller internals
		parker6kController.debug();
	}

	/**
	 * Creates a string of commas to be added to a command before the current axis value.
	 */
	private void createAxisCommas() {
		axisCommas = "";
		for (int j = 0; j < axisNo - 1; j++)
			axisCommas += ",";
	}

	/**
	 * Create a string to represent all axes in the controller. The current axis must be "1" (activate) and the others
	 * "x" (leave in current state) e.g. goXX1X
	 */
	private void createAxisBitFieldX() {
		axisBitFieldX = "";
		for (int j = 1; j <= parker6kController.getMaxNoOfMotors(); j++)
			axisBitFieldX += (j == axisNo) ? "1" : "X";
	}

	/**
	 * Create a string to represent all axes in the controller must be represented. The current axis must be "1"
	 * (activate) and the others "0" (do not activate) e.g. 2_s0010
	 */
	private void createAxisBitField() {
		axisBitField = "";
		for (int j = 1; j <= parker6kController.getMaxNoOfMotors(); j++)
			axisBitField += (j == axisNo) ? "1" : "0";
	}

	/**
	 * correctBacklash in MotorBase cannot not be used as the parker motors use encoder position for performing a moveBy
	 * operation.
	 *
	 * @throws MotorException
	 */
	@Override
	public void correctBacklash() throws MotorException {
		if (correctBacklash) {
			logger.debug("MotorBase correctBacklash about to move by " + getBacklashSteps() + " steps");
			moveTo(targetPosition + getBacklashSteps());
		}
	}

	/**
	 * Extract position values as Doubles from the matched input command string.
	 *
	 * <pre>
	 *
	 *       e.g. *2TPE+80 &lt;eot&gt;&lt;errok&gt;
	 *
	 * </pre>
	 *
	 * @param reply
	 *            the reply from the Parker motor
	 * @param commandEcho
	 *            command echo in reply (ERRLVL4, 3 and 2)
	 * @return extracted List of hardware positions as Doubles (as some are floating point)
	 * @throws MotorException
	 */
	private synchronized double extractPos(String reply, String commandEcho) throws MotorException {
		int echoIndex;
		String replyStr;
		String token = "";
		StringTokenizer numStrBuf;
		double pos = 0.0;

		replyStr = reply;

		// match mnemonic in reply command echo
		if ((echoIndex = replyStr.indexOf(commandEcho)) > 0) {
			// extract characters after command echo as tokenizer object
			numStrBuf = new StringTokenizer(replyStr.substring(echoIndex + commandEcho.length()), " ,\t\n\r\f>?");
			while (numStrBuf.hasMoreTokens()) {
				try {
					token = numStrBuf.nextToken();
					pos = new Double(token).doubleValue();
				}
				// to be safe catch everything (should be
				// "NumberFormatException")
				catch (Exception e) {
					logger.debug(className + " : caught " + e.toString() + " token " + token);
					break;
				}
			}
			logger.debug(className + " : numeric String : " + pos);
			return pos;
		}
		throw (new MotorException(MotorStatus.UNKNOWN, " Invalid reply from Parker (extractPos) : \n" + this
				+ commandEcho));
	}

	/**
	 * Extract Motor status from the matched input command string via filtered status string.
	 *
	 * <pre>
	 *
	 *         e.g. &quot;00000000000000000000000000000000&quot;
	 *
	 * </pre>
	 *
	 * @param reply
	 *            the reply from the Parker motor
	 * @param commandEcho
	 *            command echo in reply (ERRLVL4, 3 and 2)
	 * @return MotorStatus object
	 * @throws MotorException
	 */
	private synchronized MotorStatus extractStatus(String reply, String commandEcho)
			throws MotorException {
		/*
		 * minimal indices of Parker status bits which are used to derive overall MotorStatus. index starts from 0 (i.e. =
		 * status bit number -1). Extra ones may be added as necessary.
		 */
		final int MOVING = 0;
		final int STALLED = 11;
		final int SHUTDOWN = 12;
		final int FAULT = 13;
		final int POS_HW_LIMIT = 14;
		final int NEG_HW_LIMIT = 15;
		final int POS_SW_LIMIT = 16;
		final int NEG_SW_LIMIT = 17;
		final int POSN_ERROR_EXCEEDED = 22;
		final int TARGET_ZONE_TIMEOUT = 24;
		final int MOTION_SUSPENDED = 25;

		// obtain String of concatenated "0" false or "1" true status vals
		String strStatus = extractBinaryStatus(reply, commandEcho);

		// map onto available MotorStatus possibilities
		// based on status bit flags
		if (strStatus.charAt(MOVING) == '1') {
			return (MotorStatus.BUSY);
		} else if ((strStatus.charAt(POS_HW_LIMIT) == '1') || (strStatus.charAt(POS_SW_LIMIT) == '1')) {
			return (MotorStatus.UPPER_LIMIT);
		} else if ((strStatus.charAt(NEG_HW_LIMIT) == '1') || (strStatus.charAt(NEG_SW_LIMIT) == '1')) {
			return (MotorStatus.LOWER_LIMIT);
		} else if ((strStatus.charAt(STALLED) == '1') || (strStatus.charAt(SHUTDOWN) == '1')
				|| (strStatus.charAt(FAULT) == '1') || (strStatus.charAt(POSN_ERROR_EXCEEDED) == '1')
				|| (strStatus.charAt(TARGET_ZONE_TIMEOUT) == '1') || (strStatus.charAt(MOTION_SUSPENDED) == '1')) {
			return (MotorStatus.FAULT);
		} else {
			return (MotorStatus.READY);
		}
	}

	/**
	 * Extract Boolean binary status from the matched input command string e.g.
	 * "*TAS0000_0000_0000_0000_0000_0000_0000_0000" is returned as "00000000000000000000000000000000"
	 *
	 * @param reply
	 *            the reply from the Parker motor
	 * @param commandEcho
	 *            command echo in reply (ERRLVL4, 3 and 2)
	 * @return status digits "0" or "1" only as a String
	 * @throws MotorException
	 */
	private synchronized String extractBinaryStatus(String reply, String commandEcho) throws MotorException {
		final int NO_STATUS_BITS = 32;
		StringBuffer statusStrBuf = new StringBuffer(50);
		int statusStart; // axis # could be a status digit
		char charElement;

		statusStart = reply.indexOf(commandEcho) + commandEcho.length();
		if (statusStart > 0) {
			// only append "1" or "0" to string buffer
			for (int j = statusStart; j < reply.length(); j++) {
				charElement = reply.charAt(j);
				if ((charElement == '0') || (charElement == '1')) {
					statusStrBuf.append(charElement);
				}
			}

			// ensure status list has found all the bits needed
			if (statusStrBuf.length() != NO_STATUS_BITS) {
				throw (new MotorException(MotorStatus.UNKNOWN, " Parker reply needs " + NO_STATUS_BITS
						+ " from reply : \n" + this + "\n but got : \n" + statusStrBuf + "\n length : "
						+ statusStrBuf.length()));
			}

			logger.debug(className + " : Status String : " + statusStrBuf);

			return statusStrBuf.toString();
		}

		// no match of mnemonic in a reply so i/o sequence has got screwed
		throw (new MotorException(MotorStatus.UNKNOWN, " Invalid reply from Parker (extractBinaryStatus) : \n" + reply
				+ commandEcho));
	}
}