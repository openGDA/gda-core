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

import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRControl 'motor' class to drive SR control network virtual motor
 */
public class SRControlMotor extends MotorBase {

	private static final Logger logger = LoggerFactory.getLogger(SRControlMotor.class);

	private SRControlInterface srControl;

	private String deviceID;

	private double currentPosition = 0;

	private double currentspeed;

	private String signature = getClass().getSimpleName();

	private volatile boolean motorMoving = false; // needed for isMoving()

	private final short NO_INTERLOCK_ERROR = 0;

	private String srControlName;

	private final double MUTUAL_MODE = 1;

	private final double OPPOSING_MODE = 2;

	private final double BLOCK_MODE = 1;

	private final String MODE_SERVER = "U5.MODE";

	private final String USERS_SERVER = "U5.USERS";

	private final String MUTUAL_STRING = "MUTUAL";

	private final String OPPOSING_STRING = "OPPOSING";

	private final int STATUS_LENGTH = 16;

	// The phase motor must be moved to this position to determine the real
	// phase.
	private final double ZERO_PHASE_OFFSET = 0.01;

	@Override
	public void configure() {
		signature = signature + " " + getName();
		try {
			if ((srControl = (SRControlInterface) Finder.getInstance().find(srControlName)) == null) {
				logger.error("SrControl " + srControlName + " not found");
			} else {
				srControl.initialise();
				getPosition();
				logger.debug("Loaded motor position " + currentPosition);
			}
		} catch (Exception e) {
			logger.error("configure caught {}", e.getMessage());
		}
	}

	/**
	 * @return Returns the srControlName.
	 */
	public String getSrControlName() {
		return srControlName;
	}

	/**
	 * @param srControlName
	 *            The srControlName to set.
	 */
	public void setSrControlName(String srControlName) {
		this.srControlName = srControlName;
	}

	@Override
	public double getMinPosition() {
		return minPosition;
	}

	@Override
	public double getMaxPosition() {
		return maxPosition;
	}

	/**
	 * @param deviceID
	 */
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	/**
	 * @return deviceID
	 */
	public String getDeviceID() {
		return deviceID;
	}

	/**
	 * Moves the motor by the specified number of units achieved by converting to an absolute move
	 *
	 * @param increment
	 *            the number of units to move by
	 * @throws MotorException
	 */
	@Override
	public void moveBy(double increment) throws MotorException {
		// Get current position
		double[] value = new double[1];
		try {
			srControl.getValue(deviceID, SRControlInterface.GET_VALUE, value);
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " moveBy(): \n"
					+ e.getMessage()));
		}
		// Check within limits
		value[0] += increment;
		if (value[0] > maxPosition || value[0] < minPosition) {
			throw (new MotorException(MotorStatus.FAULT, signature + "moveBy():  value outside limits"));
		}
		try {
			value[0] = increment;
			srControl.setValue(deviceID, SRControlInterface.SET_VALUE, value);
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " moveBy(): \n"
					+ e.getMessage()));
		}
	}

	/**
	 * Moves the motor to the specified position
	 *
	 * @param position
	 *            the number of position to move to
	 * @throws MotorException
	 */

	@Override
	public void moveTo(double position) throws MotorException {
		// Check within limits
		if (position > maxPosition || position < minPosition) {
			throw (new MotorException(MotorStatus.FAULT, signature + "moveTo(): value outside limits"));
		}
		try {
			double[] value = new double[1];
			value[0] = position;
			srControl.setValue(deviceID, SRControlInterface.SET_VALUE, value);
		} catch (DeviceException e) {
			logger.error(signature + " throwing MotorException in moveTo with message " + e.getMessage());
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " moveTo(): \n"
					+ e.getMessage()));
		}
	}

	/**
	 * Not implemented
	 *
	 * @param direction
	 *            the direction
	 * @throws MotorException
	 */
	@Override
	public void moveContinuously(int direction) throws MotorException {
		logger.debug(signature + " :moveContinuously method not implemented");

	}

	/**
	 * Not implemented
	 *
	 * @param steps
	 *            the position to set
	 * @throws MotorException
	 */
	@Override
	public void setPosition(double steps) throws MotorException {
		logger.debug(signature + " :setPosition method not implemented");
	}

	/**
	 * Gets the current position of the motor
	 *
	 * @return the current position (integer truncation possible)
	 * @throws MotorException
	 */
	@Override
	public double getPosition() throws MotorException {
		double[] value = new double[1];
		try {
			srControl.getValue(deviceID, SRControlInterface.GET_VALUE, value);
			currentPosition = value[0];
		} catch (DeviceException e) {
			logger.error(signature + " throwing MotorException in getPosition with message " + e.getMessage());
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " getPosition(): \n"
					+ e.getMessage()));
		}
		return value[0];
	}

	/**
	 * @param stepsPerSecond
	 *            set the speed to stepsPerSecond
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		currentspeed = stepsPerSecond;
	}

	/**
	 * @return the current speed
	 * @throws MotorException
	 */
	@Override
	public double getSpeed() throws MotorException {
		return currentspeed;
	}

	/**
	 * Not implemented
	 *
	 * @throws MotorException
	 */
	@Override
	public void stop() throws MotorException {
		logger.debug(signature + " :stop method not implemented");
	}

	/**
	 * Not implemented
	 *
	 * @throws MotorException
	 */
	@Override
	public void panicStop() throws MotorException {
		logger.debug(signature + " :panicStop method not implemented");
	}

	/**
	 * Gets the status of the motor
	 *
	 * @return a value from the MotorStatus enum
	 * @throws MotorException
	 */
	@Override
	public synchronized MotorStatus getStatus() throws MotorException {
		double[] value = new double[1];
		StringBuffer response = new StringBuffer("");
		MotorStatus status = null;

		// First check interlocks for errors
		try {
			srControl.getValue(deviceID, SRControlInterface.GET_INTERLOCKS, value);
			short bitValue = (short) value[0];
			if ((bitValue & SRControlInterface.LOWERLIMIT_INTERLOCK) != NO_INTERLOCK_ERROR) {
				status = MotorStatus.LOWERLIMIT;
			} else if ((bitValue & SRControlInterface.UPPERLIMIT_INTERLOCK) != NO_INTERLOCK_ERROR) {
				status = MotorStatus.UPPERLIMIT;
			} else if ((bitValue & SRControlInterface.GEARBOXFAIL_INTERLOCK) != NO_INTERLOCK_ERROR) {
				status = MotorStatus.FAULT;
			} else if ((bitValue & SRControlInterface.ARRAYTILT_INTERLOCK) != NO_INTERLOCK_ERROR) {
				status = MotorStatus.FAULT;
			} else if ((bitValue & SRControlInterface.JOGACTIVE_INTERLOCK) != NO_INTERLOCK_ERROR) {
				status = MotorStatus.UNKNOWN;
			} else if ((bitValue & SRControlInterface.ERROR_INTERLOCK) != NO_INTERLOCK_ERROR) {
				status = MotorStatus.FAULT;
			}

			// Next check whether the control system is allowing user moves.
			// If use
			// is
			// not allowed then throw an exception (so that the message gets
			// through).
			srControl.getValue(USERS_SERVER, SRControlInterface.GET_STATUS, value);
			if (value[0] == BLOCK_MODE) {
				status = MotorStatus.FAULT;
				throw new MotorException(MotorStatus.FAULT, "Undulator use currently not allowed by SR Control System");
			}

			// If the status has not yet been set by the interlocks or
			// the overall control parameter then get it from the motor

			if (status == null) {
				status = MotorStatus.FAULT;
				srControl.getValue(deviceID, SRControlInterface.GET_STATUS, value);
				// Get status string and decode it
				srControl.getStatusString(deviceID, value[0], response, STATUS_LENGTH);
				String string = response.toString();
				if (string.equals("BUSY")) {
					status = MotorStatus.BUSY;
				} else if (string.equals("OK") || string.equals("READY")) {
					motorMoving = false;
					status = MotorStatus.READY;
				} else if (string.equals("FAULT")) {
					motorMoving = false;
					status = MotorStatus.FAULT;
				} else {
					logger.error("Unexpected status for SRControlMotor " + getName() + ": " + string);
				}
			}
		} catch (DeviceException e) {
			logger.error(signature + " throwing MotorException in getStatus with message " + e.getMessage());
			status = MotorStatus.FAULT;
			throw new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " getStatus(): \n"
					+ e.getMessage());
		}

		logger.debug("SRControlMotor.getStatus() " + getName() + " returning status " + status.value());
		return status;
	}

	/**
	 * method to return state of motorMoving flag
	 *
	 * @return true if moving
	 * @throws MotorException
	 */
	@Override
	public boolean isMoving() throws MotorException {
		double[] value = new double[1];
		// Get status value
		try {
			srControl.getValue(deviceID, SRControlInterface.GET_STATUS, value);
			StringBuffer response = new StringBuffer("");
			srControl.getStatusString(deviceID, value[0], response, STATUS_LENGTH);
			logger.debug(signature + " got response " + response.toString() + " from SRControl");
			if (response.toString().equals("MOVING") || response.toString().equals("BUSY")) {
				motorMoving = true;
			} else {
				motorMoving = false;
			}

		} catch (DeviceException e) {
			logger.error(signature + " throwing MotorException in isMoving with message " + e.getMessage());
			motorMoving = false;
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " isMoving(): \n"
					+ e.getMessage()));
		}
		logger.debug("SRControlMotor.isMoving() " + getName() + "returning " + motorMoving);
		return motorMoving;
	}

	/**
	 * method to set motor software limits to the values supplied
	 *
	 * @param minimum
	 *            the minimum limit of motor travel
	 * @param maximum
	 *            the maximum limit of motor travel
	 * @throws MotorException
	 */
	@Override
	public void setSoftLimits(double minimum, double maximum) throws MotorException {
		double[] value = new double[1];
		try {
			// Set new minimum value
			value[0] = minimum;
			srControl.setValue(deviceID, SRControlInterface.SET_MINVALUE, value);
			minPosition = minimum;

			// Set new maximum value
			value[0] = maximum;
			srControl.setValue(deviceID, SRControlInterface.SET_MAXVALUE, value);
			maxPosition = maximum;
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " setSoftLimits(): \n"
					+ e.getMessage()));
		}

	}

	/**
	 * method to get motor software limits
	 *
	 * @throws MotorException
	 */
	public void getSoftLimits() throws MotorException {
		// Get minimum value
		double[] value = new double[1];
		try {
			srControl.getValue(deviceID, SRControlInterface.SET_MINVALUE, value);
			minPosition = value[0];

			srControl.getValue(deviceID, SRControlInterface.GET_MAXVALUE, value);
			maxPosition = value[0];
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, "DeviceException in " + signature + " getSoftLimits(): \n"
					+ e.getMessage()));
		}

	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		double val[] = new double[1];
		boolean validString = false;
		if (attributeName.equals("PHASEMODE")) {
			if (value.equals(MUTUAL_STRING)) {
				val[0] = MUTUAL_MODE;
				validString = true;
			} else if (value.equals(OPPOSING_STRING)) {
				val[0] = OPPOSING_MODE;
				validString = true;
			}
			if (validString) {
				srControl.setValue(MODE_SERVER, SRControlInterface.SET_STATUS, val);
			}
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		String string = null;
		double value[] = new double[1];
		if (attributeName.equals("PHASEMODE")) {
			srControl.getValue(MODE_SERVER, SRControlInterface.GET_STATUS, value);
			if (value[0] == MUTUAL_MODE) {
				string = MUTUAL_STRING;
			} else if (value[0] == OPPOSING_MODE) {
				string = OPPOSING_STRING;
			}
		}
		if (attributeName.equals("ZEROPHASEMODE")) {
			value[0] = getZeroMode();
			if (value[0] == MUTUAL_MODE) {
				string = MUTUAL_STRING;
			} else if (value[0] == OPPOSING_MODE) {
				string = OPPOSING_STRING;
			}
		}
		logger.debug("SRControlMotor.getAttribute() returning: " + string);
		return string;
	}

	private double getZeroMode() throws DeviceException {
		// If phase is at zero can't reliably get phase mode from control system
		// so need to move it off zero read ohase mode then move back to zero
		double value[] = new double[1];
		double retVal = 0;

		// NB keep these two try-catch blocks separate
		MotorStatus status = MotorStatus.FAULT;
		try {
			status = getStatus();
		} catch (MotorException me) {
			// Deliberately do nothing. An exception is thrown if not in
			// communication
			// with SR Control system or if it is not allowing movements.
		}

		if (status != MotorStatus.FAULT) {
			try {
				logger.debug("SRControlMotor.getZeroMode() about to move phase to: " + ZERO_PHASE_OFFSET);
				moveTo(ZERO_PHASE_OFFSET);
				do {
					Thread.sleep(100);
				} while (this.getStatus() == MotorStatus.BUSY);
				logger.debug("SRControlMotor.getZeroMode() finished move to: " + ZERO_PHASE_OFFSET);
				srControl.getValue(MODE_SERVER, SRControlInterface.GET_STATUS, value);
				logger.debug("SRControlMotor.getZeroMode() got value: " + value[0]);
				logger.debug("SRControlMotor.getZeroMode() about to move phase back to 0");
				moveTo(0);
				while (this.getStatus() == MotorStatus.BUSY) {
					Thread.sleep(100);
				}
				logger.debug("SRControlMotor.getZeroMode() finished move to: 0");
				retVal = value[0];
			} catch (MotorException e) {
				// NB since this method is used inside the getAttribute method
				// it
				// must
				// perversely
				// convert the MotorException into a DeviceException. (Because
				// getAttribute is
				// from Device not Motor.)
				logger.error("SRControlMotor.getZeroMode() caught MotorException with message: " + e.getMessage());
				throw new DeviceException(e.getMessage());
			} catch (DeviceException e) {
				logger.error("SRControlMotor.getZeroMode() caught DeviceException with message: " + e.getMessage());
				throw e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				String msg = "Interrupted while getting Zero Mode";
				logger.error(msg, e);
				throw new DeviceException(msg, e);
			}
		}
		logger.debug("SRControlMotor.getZeroMode() about to return: " + retVal);
		return retVal;
	}
}