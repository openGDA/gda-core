/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to control a Tango motor
 */
public class TangoMotor extends MotorBase implements Motor{
	private static final Logger logger = LoggerFactory.getLogger(TangoMotor.class);

	private volatile double currentPosition = Double.NaN;
	private volatile double currentSpeed = Double.NaN;
	private volatile MotorStatus motorStatus = MotorStatus.UNKNOWN;
	private volatile double targetPosition = Double.NaN;

	// Attribute names defined by the Tango Motor API
	private static final String positionAttributeName = "Position"; //$NON-NLS-1$
	private static final String speedAttributeName = "Velocity"; //$NON-NLS-1$
	private static final String accelerationAttributeName = "Acceleration"; //$NON-NLS-1$
	private static final String firstVelocityAttributeName = "FirstVelocity"; //$NON-NLS-1$
	private static final String presetPositionAttributeName = "PresetPosition"; //$NON-NLS-1$
	private static final String stepsPerUnitAttributeName = "Steps_per_unit"; //$NON-NLS-1$
	private static final String backlashAttributeName = "Backlash"; //$NON-NLS-1$
	private static final String homePositionAttributeName = "Home_position"; //$NON-NLS-1$
	private static final String homeSideAttributeName = "Home_side"; //$NON-NLS-1$
	private static final String hardLimitLowAttributeName = "HardLimitLow"; //$NON-NLS-1$
	private static final String hardLimitHighAttributeName = "HardLimitHigh"; //$NON-NLS-1$
	private static final String stepSizeAttributeName = "StepSize"; //$NON-NLS-1$

	private TangoDeviceProxy dev;
	private boolean calibrated = false;

	@Override
	public void configure() throws FactoryException {
		try {
			isAvailable();
			motorStatus = MotorStatus.READY;
		} catch (Exception e) {
			logger.error("TangoMotor configure: {}", e);
			logger.error("TangoMotor configure {}", e.getMessage());
			motorStatus = MotorStatus.FAULT;
		}
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return dev;
	}

	/**
	 * @param dev The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy dev) {
		this.dev = dev;
	}

	/**
	 * This method returns the current position of the motor in natural units.
	 * 
	 * @return the current position in natural units
	 * @throws MotorException
	 */
	@Override
	public double getPosition() throws MotorException {
		isAvailable();
		try {
			currentPosition = dev.read_attribute(positionAttributeName).extractDouble();
			return currentPosition;
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get position", e);
		}
	}

	/**
	 * Gets the current speed of the motor in steps/second
	 * 
	 * @return double the motor speed in steps per second
	 * @throws MotorException
	 */
	@Override
	public double getSpeed() throws MotorException {
		isAvailable();
		try {
			currentSpeed = dev.read_attribute(speedAttributeName).extractLong();
			return currentSpeed;
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get speed", e);
		}
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		isAvailable();
		try {
			DevState state = dev.state();
			switch (state.value())
			{
			case DevState._ON:
				motorStatus = MotorStatus.READY;
				break;
			case DevState._OFF:
			case DevState._FAULT:
			case DevState._DISABLE:
				motorStatus = MotorStatus.FAULT;
				break;
			case DevState._ALARM:
				if (getHardLimitLow())
					motorStatus = MotorStatus.LOWERLIMIT;
				else if (getHardLimitHigh())
					motorStatus = MotorStatus.UPPERLIMIT;
				else
					motorStatus = MotorStatus.FAULT;
				break;
			case DevState._MOVING:
				motorStatus = MotorStatus.BUSY;
				break;
			default:
				motorStatus = MotorStatus.UNKNOWN;
				break;
			}
		} catch (DevFailed e) {
			throw new MotorException(MotorStatus.UNKNOWN, "failed to get motor state", e);
		}
		return motorStatus;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (getStatus() == MotorStatus.BUSY);
	}

	/**
	 * Relative move, moves the motor by the specified mount in natural units.
	 * 
	 * @param increment the distance that motor need to travel in natural units (eg. mm, deg)
	 * @throws MotorException
	 */
	@Override
	public void moveBy(double increment) throws MotorException {
		targetPosition = getPosition() + increment;
		moveTo(targetPosition);
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		isAvailable();
		try {
			if (direction > 0) {
				dev.command_inout("StepUp");
			} else {
				dev.command_inout("StepDown");
			}
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to move continuously", e);
		}
	}

	/**
	 * Absolute move, moves the motor to the specified position in natural units
	 * 
	 * @param position the absolute position of the motor in natural units (eg. mm, deg)
	 * @throws MotorException
	 */
	@Override
	public void moveTo(double position) throws MotorException {
		isAvailable();
		targetPosition = position;
		targetRangeCheck(position);
		motorStatus = getStatus();
		if (motorStatus == MotorStatus.BUSY)
			throw new MotorException(motorStatus, "moveTo aborted because previous move not yet completed");
		if (motorStatus == MotorStatus.FAULT)
			throw new MotorException(motorStatus, "moveTo aborted because Motor is at Fault status.");
		try {
			// Attribute Position: Position in natural units - Tango_DEV_DOUBLE 
			dev.write_attribute(new DeviceAttribute(positionAttributeName, position));
		} catch (DevFailed e) {
			throw new MotorException(motorStatus, "failed to initiate start move", e);
		}
	}

	@Override
	public void home() throws MotorException {
		isAvailable();
		motorStatus = getStatus();
		if (motorStatus == MotorStatus.BUSY)
			throw new MotorException(motorStatus, "moveTo aborted because previous move not yet completed");
		if (motorStatus == MotorStatus.FAULT)
			throw new MotorException(motorStatus, "moveTo aborted because Motor is at Fault status.");
		try {
			dev.command_inout("GoHome");
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to initiate home move", e);
		}
	}

	@Override
	public void setPosition(double position) throws MotorException {
		try {
//			 Attribute PresetPosition: expressed in natural units (mm, degree, etc) it is 
//			 proportional to the steps attribute- Tango_DEV_DOUBLE 
			dev.write_attribute(new DeviceAttribute(presetPositionAttributeName, position));
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to preset motor position", e);
		}
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				// !!!!!!!!! GDA use double speed Tango use long.
				// Attribute Velocity: cruise velocity in steps/s - Tango_DEV_LONG
				dev.write_attribute(new DeviceAttribute(speedAttributeName,(int) speed));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set speed", e);
			}
		}
	}

	@Override
	public void stop() throws MotorException {
		isAvailable();
		try {
			dev.command_inout("Abort");
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to stop", e);
		}
	}

	@Override
	public void panicStop() throws MotorException {
		stop();
	}

	/**
	 * checks motor Status
	 * 
	 * @return MotorStatus
	 * @throws MotorException
	 */
	protected MotorStatus checkStatus() throws MotorException {
		MotorStatus status = getStatus();
		if (status == MotorStatus.UNKNOWN || status == MotorStatus.FAULT) {
			throw new MotorException(MotorStatus.FAULT, "Exception while getting status ");
		}
		return status;
	}
	/**
	 * This method check the target position is within the limit range.
	 * 
	 * @param requestedPosition
	 *            absolute requested target to validate within limits
	 * @throws MotorException
	 */
	private void targetRangeCheck(double requestedPosition) throws MotorException {
		final double lowerLimit = getMinPosition();
		final double upperLimit = getMaxPosition();

		if (requestedPosition < lowerLimit) {
			throw (new MotorException(MotorStatus.LOWERLIMIT, requestedPosition + " outside lower hardware limit of " + lowerLimit));
		}

		else if (requestedPosition > upperLimit) {
			throw (new MotorException(MotorStatus.UPPERLIMIT, requestedPosition + " outside upper hardware limit of " + upperLimit));
		}
	}

	public int getAcceleration() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(accelerationAttributeName).extractLong();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get acceleration", e);
		}
	}

	public void setAcceleration(int acceleration) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				// Attribute Velocity: cruise velocity in steps/s - Tango_DEV_LONG
				dev.write_attribute(new DeviceAttribute(accelerationAttributeName, acceleration));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set acceleration", e);
			}
		}
	}

	public int getFirstVelocity() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(firstVelocityAttributeName).extractLong();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get first velocity", e);
		}
	}

	public void setFirstVelocity(int firstVelocity) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				// Attribute Velocity: cruise velocity in steps/s - Tango_DEV_LONG
				dev.write_attribute(new DeviceAttribute(firstVelocityAttributeName, firstVelocity));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set firstVelocity", e);
			}
		}
	}

	public double getStepsPerUnit() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(stepsPerUnitAttributeName).extractDouble();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get steps per unit", e);
		}
	}

	public void setStepsPerUnit(double stepsPerUnit) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				dev.write_attribute(new DeviceAttribute(stepsPerUnitAttributeName, stepsPerUnit));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set steps per unit", e);
			}
		}
	}

	public double getBacklash() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(backlashAttributeName).extractDouble();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get backlash", e);
		}
	}

	public void setBacklash(double backlash) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				dev.write_attribute(new DeviceAttribute(backlashAttributeName, backlash));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set backlash", e);
			}
		}
	}

	public boolean getHomeSide() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(homeSideAttributeName).extractBoolean();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get home side", e);
		}
	}

	public boolean getHardLimitLow() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(hardLimitLowAttributeName).extractBoolean();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get hard limit low", e);
		}
	}

	public boolean getHardLimitHigh() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(hardLimitHighAttributeName).extractBoolean();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get hard limit high", e);
		}
	}

	public double getHomePosition() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(homePositionAttributeName).extractDouble();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get home position", e);
		}
	}

	public void setHomePosition(double homePosition) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				dev.write_attribute(new DeviceAttribute(homePositionAttributeName, homePosition));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set home position", e);
			}
		}
	}

	public double getStepSize() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(stepSizeAttributeName).extractDouble();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get step size", e);
		}
	}

	public void setStepSize(double stepSize) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				dev.write_attribute(new DeviceAttribute(stepSizeAttributeName, stepSize));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set Step Size", e);
			}
		}
	}

	public double getPresetPosition() throws MotorException {
		isAvailable();
		try {
			return dev.read_attribute(presetPositionAttributeName).extractDouble();
		} catch (DevFailed e) {
			throw new MotorException(getStatus(), "failed to get preset position", e);
		}
	}

	public void setPresetPosition(double presetPosition) throws MotorException {
		isAvailable();
		if (!calibrated) {
			try {
				dev.write_attribute(new DeviceAttribute(presetPositionAttributeName, presetPosition));
			} catch (DevFailed e) {
				throw new MotorException(getStatus(), "failed to set preset position", e);
			}
		}
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		try {
			if (accelerationAttributeName.equalsIgnoreCase(attributeName)) {
				setAcceleration((Integer) value);
			}
			else if (firstVelocityAttributeName.equalsIgnoreCase(attributeName)) {
				setFirstVelocity((Integer) value);
			}
			else if (stepsPerUnitAttributeName.equalsIgnoreCase(attributeName)) {
				setStepsPerUnit((Double) value);
			}
			else if (backlashAttributeName.equalsIgnoreCase(attributeName)) {
				setBacklash((Double) value);
			}
			else if (homePositionAttributeName.equalsIgnoreCase(attributeName)) {
				setHomePosition((Double) value);
			}
			else if (stepSizeAttributeName.equalsIgnoreCase(attributeName)) {
				setStepSize((Double) value);
			}
			else if (presetPositionAttributeName.equalsIgnoreCase(attributeName)) {
				setPresetPosition((Double) value);
			}
			else {
				throw new DeviceException("Unknown attribute " + attributeName);
			}
			} catch (MotorException e) {
			throw new DeviceException(e.getMessage());
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		try {
			if (accelerationAttributeName.equalsIgnoreCase(attributeName)) {
				return getAcceleration();
			}
			else if (firstVelocityAttributeName.equalsIgnoreCase(attributeName)) {
				return getFirstVelocity();
			}
			else if (stepsPerUnitAttributeName.equalsIgnoreCase(attributeName)) {
				return getStepsPerUnit();
			}
			else if (backlashAttributeName.equalsIgnoreCase(attributeName)) {
				return getBacklash();
			}
			else if (homePositionAttributeName.equalsIgnoreCase(attributeName)) {
				return getHomePosition();
			}
			else if (homeSideAttributeName.equalsIgnoreCase(attributeName)) {
				return getHomeSide();
			}
			else if (hardLimitLowAttributeName.equalsIgnoreCase(attributeName)) {
				return getHardLimitLow();
			}
			else if (hardLimitHighAttributeName.equalsIgnoreCase(attributeName)) {
				return getHardLimitHigh();
			}
			else if (stepSizeAttributeName.equalsIgnoreCase(attributeName)) {
				return getStepSize();
			}
			else if (presetPositionAttributeName.equalsIgnoreCase(attributeName)) {
				return getPresetPosition();
			}
			logger.error("Unknown attribute {}", attributeName);
		} catch (MotorException e) {
			throw new DeviceException(e.getMessage());
		}
		return null;
	}

	private void isAvailable() throws MotorException {
		try {
			// Is the device still connected or just started
			dev.status();
			// if it was not connected and just started
			if (!configured) {
				dev.command_inout("ON");
				if (dev.use_db()) {
					DbDatum property = dev.get_property("Calibrated");
					if (property != null) {
						calibrated = property.extractBoolean();
					}
				}
				configured = true;
				motorStatus = MotorStatus.READY;
			}
		} catch (DevFailed e) {
			// device has lost connection
			configured = false;
			throw new MotorException(MotorStatus.UNKNOWN, "Tango device server " + dev.get_name() + " failed");
		} catch (Exception e) {
			throw new MotorException(MotorStatus.UNKNOWN, "Tango device server stuffed");			
		}
	}
}
