/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorProperties.MotorEvent;
import gda.device.MotorStatus;
import gda.device.scannable.component.MotorLimitsComponent;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Adapter class for motor to work as scannables. This class uses units and has an offset/scaling factor for the motor
 * position.
 * <p>
 * The returned position (user position) is calculated by:
 * <p>
 * userPosition = (motorPosition*scalingFactor) + offset
 * <p>
 * GDALimits are based on the userPosition, but must be in the same units as the motor.
 */
@ServiceInterface(IScannableMotor.class)
public class ScannableMotor extends ScannableMotionUnitsBase implements IScannableMotor {

	/**
	 * String to use in getAttribute to get the motor name
	 */
	private static final String MOTOR_NAME = "motorName";

	/**
	 * Name of java property which when set true causes the upper & lower gda limits to be set from the motor limits if
	 * not already set. Due to the possibility the motor high limit can be associated with a gda low limit we cannot
	 * separate out the setting of the upper gda limit without also setting the lower gda limit and vice versa. If the
	 * motor lower and upper limits are not set they are taken to be -Double.MAX_VALUE and Double.MAX_VALUE
	 * respectively.
	 */
	public static final String COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS = "gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits";

	private static final Logger logger = LoggerFactory.getLogger(ScannableMotor.class);

	/**
	 * Set of motor states which are considered error states i.e. a move is considered to have failed if the motor ends
	 * up in one of these states.
	 */
	private static final Set<MotorStatus> ERROR_STATES = EnumSet.of(MotorStatus.FAULT, MotorStatus.UPPER_LIMIT,
			MotorStatus.LOWER_LIMIT, MotorStatus.SOFT_LIMIT_VIOLATION);

	private String motorName;

	private Motor motor;

	private MotorLimitsComponent motorLimitsComponent;

	private boolean returnDemandPosition = false;

	private double demandPositionTolerance;

	private Double lastDemandedInternalPosition = null;

	/**
	 * Field used to identify whether the demand position tolerance is set.
	 */
	private boolean isDemandPositionToleranceSet = false;

	private boolean logMoveRequestsWithInfo = false;

	private final Object isBusyAndMovetoLock = new Object();

	private boolean demandMsgShown = false;

	/**
	 * Sets the motor used by this scannable motor.
	 *
	 * @param motor
	 *            the motor
	 */
	@Override
	public void setMotor(Motor motor) {
		this.motor = motor;
		if (motorLimitsComponent == null)
			setMotorLimitsComponent(new MotorLimitsComponent(motor));
	}

	/**
	 * Method required by scripts which need to access the real motor at times.<br>
	 * Before the script could get the motor name but now that the motor may be set by spring, scripts cannot get the
	 * underlying motor.
	 *
	 * @return Motor
	 */
	@Override
	public Motor getMotor() {
		return motor;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (motor == null) {
			if (motorName == null || motorName.length() == 0) {
				throw new FactoryException("No motor configured and no motor name set");
			}
			final Motor motorFromFinder = Finder.getInstance().find(motorName);
			if (motorFromFinder == null) {
				throw new FactoryException(String.format("Motor %s not found", motorName));
			}
			setMotor(motorFromFinder);
		}

		motor.addIObserver((theObserved, changeCode) -> handleMotorUpdates(changeCode));
		this.inputNames = new String[] { getName() };

		try {
			// get the hardware units for the underlying motor
			// perhaps hardware units should be in the motor interface?
			final String motorUnit = motor.getUnitString();
			if (motorUnit != null && motorUnit.trim().length() > 0) {
				// try to work out the units the motor works in
				unitsComponent.setHardwareUnitString(motorUnit);
			} else if (getHardwareUnitString() == null) {
				// else use the value from this.motorUnitString, but if that has not been set then log an error
				logger.warn("No hardware units set for {}. This will probably cause exceptions and should be resolved.", getName());
			}

			if (LocalProperties.check(COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, false)) {
				copyMotorLimitsIntoScannableLimits();
			}
		} catch (Exception e) {
			throw new FactoryException("Exception during configure of " + getName(), e);
		}
		setConfigured(true);
	}

	private void copyMotorLimitsIntoScannableLimits() throws Exception {
		if (getUpperGdaLimits() == null || getLowerGdaLimits() == null) {
			final Double upperMotorLimit = getUpperMotorLimit();
			final Double lowerMotorLimit = getLowerMotorLimit();
			setLowerGdaLimits((lowerMotorLimit == null) ? -Double.MAX_VALUE : lowerMotorLimit);
			setUpperGdaLimits((upperMotorLimit == null) ? Double.MAX_VALUE : upperMotorLimit);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals(MOTOR_NAME)) {
			return this.motorName;
		} else if (attributeName.equals("lowerMotorLimit")) {
			return this.getLowerMotorLimit();
		} else if (attributeName.equals("upperMotorLimit")) {
			return this.getUpperMotorLimit();
		}
		return super.getAttribute(attributeName);
	}

	@Override
	public Double[] getFirstInputLimits() throws DeviceException {
		final Double lowerInnerLimit = getLowerInnerLimit();
		final Double upperInnerLimit = getUpperInnerLimit();
		return lowerInnerLimit == null && upperInnerLimit == null ? null : new Double[] { lowerInnerLimit,
				upperInnerLimit };
	}

	/**
	 * @return Returns the motorName.
	 */
	@Override
	public String getMotorName() {
		return motorName;
	}

	/**
	 * @param motorName
	 *            The motorName to set.
	 */
	@Override
	public void setMotorName(String motorName) {
		this.motorName = motorName;
	}

	/**
	 * {@inheritDoc}. Triggers a motor to move. Throws a DeviceException if the motor is already busy. This method's
	 * guts are synchronised so that another thread can't enter it before the first thread has completed the motor's
	 * (asynchronous) moveTo call. For the DeviceException to be thrown properly when a move request is made of a moving
	 * motor, the motor's getStatus() method must *immediately* report BUSY after it's moveTo call has completed.
	 */
	@Override
	public void rawAsynchronousMoveTo(Object internalPosition) throws DeviceException {

		synchronized (isBusyAndMovetoLock) {

			Double internalDoublePosition;

			// check motor status. Throw an error if it is not idle. TODO: There is a race condition here
			if (this.isBusy()) {
				throw new DeviceException("The scannable motor " + getName() + " (" + motor.getName() + ") " + IScannableMotor.WAS_ALREADY_BUSY_SO_COULD_NOT_BE_MOVED);
				// RotationViewer.moveMotor now depends on this DeviceException containing the substring defined by IScannableMotor.WAS_ALREADY_BUSY_SO_COULD_NOT_BE_MOVED
			}

			try {
				internalDoublePosition = PositionConvertorFunctions.toDouble(internalPosition);
				if (isLogMoveRequestsWithInfo()) {
					logger.info("{}: move to {}", getName(), internalPosition);
				} else {
					logger.debug("{}: move to {}", getName(), internalPosition);
				}
				notifyIObservers(this, ScannableStatus.BUSY);
				this.motor.moveTo(internalDoublePosition);
				lastDemandedInternalPosition = internalDoublePosition;
			} catch (IllegalArgumentException e) {
				throw new DeviceException(getName() + ".rawAsynchronousMoveTo() could not convert "
						+ internalPosition.toString() + " to a double.");
			} catch (MotorException e) {
				throw new DeviceException("Error during async move to", e);
			}
			demandMsgShown = false;
		}
	}

	/**
	 * Read the position in its internal (user) representation. If configured to return demand positions with
	 * {@link #setReturnDemandPosition(boolean)} and the last demanded position is within
	 * {@link #getDemandPositionTolerance()} of the physical internal motor position, then return this demand position
	 * instead.
	 */
	@Override
	public Object rawGetPosition() throws DeviceException {
		if (motor == null) {
			throw new DeviceException("No motor configured");
		}
		try {
			return motor.getPosition();
		} catch (MotorException e) {
			throw new DeviceException("Error getting position", e);
		}
	}

	/**
	 * Return the last demanded motor/internal position if one has been set, otherwise return the current position. If
	 * the motor is stopped, and the actual position is not close to the the demand position, then the current position
	 * is returned instead.
	 *
	 * @throws DeviceException
	 */
	protected Object rawGetDemandPosition() throws DeviceException {

		final double currentInternalPosition = (Double) rawGetPosition();

		if (lastDemandedInternalPosition == null) {
			return currentInternalPosition;
		}

		if (isBusy()) {
			return lastDemandedInternalPosition;
		}

		// motor is not moving
		final double tolerance = getDemandPositionTolerance();
		if (Math.abs(currentInternalPosition - lastDemandedInternalPosition) <= tolerance) {
			return lastDemandedInternalPosition;
		} // else

		// motor is not moving but is not at the last demand position.
		if (!demandMsgShown) {
			final String msg = MessageFormat.format(
					"{0} is returning a position based on its real motor position ({1}) rather than its last demanded position({2}),\n"
							+ "as these differ by more than the configured demand position tolerance ({3}).",
					getName(), currentInternalPosition, lastDemandedInternalPosition, tolerance);
			demandMsgShown = true; // reset by rawAsynchMoveto
			logger.info(msg);
			InterfaceProvider.getTerminalPrinter().print("WARNING: " + msg);
		}
		return currentInternalPosition;

	}

	/**
	 * {@inheritDoc} Return the demand position from {@link #getDemandPosition()} if configured with
	 * {@link #setReturnDemandPosition(boolean)} to do so and the motor is not moving, otherwise returns the actual
	 * position. i.e. if the motor is moving the actual position is always returned.
	 */
	@Override
	public Object getPosition() throws DeviceException {
		if (isReturningDemandPosition() && !isBusy()) {
			return getDemandPosition();
		}
		return getActualPosition();
	}

	/**
	 * Set the position
	 *
	 * @throws DeviceException
	 */
	@Override
	public void setPosition(Object position) throws DeviceException {
		if (isBusy()) {
			throw new DeviceException("Motor is moving! Cannot set position");
		}
		this.motor.setPosition((Double)position);
	}

	/**
	 * Return the last demanded user/external position if one has been set. If the motor is stopped, and the actual
	 * position is not close to the the demand position, then the current position is returned instead.
	 *
	 * @throws DeviceException
	 */
	protected Object getDemandPosition() throws DeviceException {
		return internalToExternal(rawGetDemandPosition());
	}

	protected Object getActualPosition() throws DeviceException {
		return internalToExternal(rawGetPosition());
	}

	/**
	 * Return true if motor is busy.
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return this.motor.getStatus() == MotorStatus.BUSY;
		} catch (MotorException e) {
			throw new DeviceException("Error getting busy status", e);
		}
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		logger.trace("{}: start waiting", getName());

		motor.waitWhileStatusBusy();

		logger.trace("{}: waiting complete >> motor position={}, motor status={}, lastDemandedInternalPosition={}, returnDemandPosition={}",
				getName(), motor.getPosition(), motor.getStatus(), lastDemandedInternalPosition, returnDemandPosition);

		final MotorStatus motorStatus = motor.getStatus();
		if (ERROR_STATES.contains(motorStatus)) {
			final String message = String.format(
					"During %s.waitWhileBusy(), EPICS Motor was found in %s status. Please check EPICS Screen.",
					getName(), motorStatus);
			throw new MotorException(motorStatus, message);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			this.motor.stop();
		} catch (MotorException e) {
			throw new DeviceException("Could not stop motor", e);
		}
	}

	private boolean isScalingFactorNegative() {
		return (getScalingFactor() == null) ? false : getScalingFactor()[0] < 0.;
	}

	/**
	 * Returns the lower motor limit in its external representation. Null if there is no lower Motor limit.
	 *
	 * @return limit in external representation
	 * @throws DeviceException
	 */
	@Override
	public Double getLowerMotorLimit() throws DeviceException {
		if (motorLimitsComponent == null) {
			throw new DeviceException(
					"Could not check "
							+ getName()
							+ "'s lower motor limit as there is no limits component (probably because the no motor has been set).");
		}
		final Double[] internalArray = (isScalingFactorNegative()) ? motorLimitsComponent.getInternalUpper()
				: motorLimitsComponent.getInternalLower();
		final Double internal = (internalArray == null) ? null : internalArray[0];
		return PositionConvertorFunctions.toDouble(internalToExternal(internal));
	}

	/**
	 * Returns the upper motor limit in its external representation. Null if there is no upper Motor limit.
	 *
	 * @return limit in external representation
	 * @throws DeviceException
	 */
	@Override
	public Double getUpperMotorLimit() throws DeviceException {
		if (motorLimitsComponent == null) {
			throw new DeviceException(
					"Could not check "
							+ getName()
							+ "'s upper motor limit as there is no limits component (probably because the no motor has been set).");
		}
		final Double[] internalArray = (isScalingFactorNegative()) ? motorLimitsComponent.getInternalLower()
				: motorLimitsComponent.getInternalUpper();
		final Double internal = (internalArray == null) ? null : internalArray[0];
		return PositionConvertorFunctions.toDouble(internalToExternal(internal));
	}

	/**
	 * Returns the innermost (i.e. the most limiting) of the lower Scannable and Motor limits.
	 *
	 * @return the highest minimum limit, or null if neither are set.
	 * @throws DeviceException
	 */
	@Override
	public Double getLowerInnerLimit() throws DeviceException {

		final Double lowerGdaLimit = (getLowerGdaLimits() == null) ? null : getLowerGdaLimits()[0];
		final Double lowerMotorLimit = getLowerMotorLimit();
		if (lowerGdaLimit == null && lowerMotorLimit == null)
			return null;
		if (lowerGdaLimit == null)
			return lowerMotorLimit;
		if (lowerMotorLimit == null)
			return lowerGdaLimit;
		return (lowerGdaLimit > lowerMotorLimit) ? lowerGdaLimit : lowerMotorLimit;
	}

	/**
	 * Returns the innermost (i.e. the most limiting) of the upper Scannable and Motor limits.
	 *
	 * @return the lowest maximum limit, or null if neither are set.
	 * @throws DeviceException
	 */
	@Override
	public Double getUpperInnerLimit() throws DeviceException {

		final Double upperGdaLimit = (getUpperGdaLimits() == null) ? null : getUpperGdaLimits()[0];
		final Double upperMotorLimit = getUpperMotorLimit();
		if (upperGdaLimit == null && upperMotorLimit == null)
			return null;
		if (upperGdaLimit == null)
			return upperMotorLimit;
		if (upperMotorLimit == null)
			return upperGdaLimit;
		return (upperGdaLimit < upperMotorLimit) ? upperGdaLimit : upperMotorLimit;
	}

	@Override
	public String toFormattedString() {
		String report = super.toFormattedString();
		Double lowerMotorLimit = null;
		Double upperMotorLimit = null;
		if (motorLimitsComponent == null) {
			return report;
		}
		try {
			lowerMotorLimit = getLowerMotorLimit();
			upperMotorLimit = getUpperMotorLimit();
		} catch (DeviceException e) {
			logger.warn("{} exception while getting Motor limits.", getName(), e);
			return report;
		}

		if (lowerMotorLimit != null || upperMotorLimit != null) {
			report += " mot(";
			if (lowerMotorLimit != null)
				report += String.format(getOutputFormat()[0], lowerMotorLimit);
			report += ":";
			if (upperMotorLimit != null)
				report += String.format(getOutputFormat()[0], upperMotorLimit);
			report += ")";
		}

		try {
			if (isReturningDemandPosition() && (lastDemandedInternalPosition != null) && (!isBusy())) {
				if (Math.abs(((Double) rawGetPosition()) - lastDemandedInternalPosition) <= getDemandPositionTolerance()) {
					report += " demand";
				} else {
					// stopped, but not at target so show demand as well
					final Double[] offsetArray = new Double[getInputNames().length + getExtraNames().length];
					if (getOffset() != null) {
						// Complication - the offset array may not have offsets for the extra fields.
						System.arraycopy(getOffset(), 0, offsetArray, 0, getOffset().length);
					}
					final String[] unitStringArray = new String[getInputNames().length + getExtraNames().length];
					for (int i = 0; i < unitStringArray.length; i++) {
						unitStringArray[i] = getUserUnits();
					}

					report += " *demand="
							+ ScannableUtils.getFormattedCurrentPositionArray(
									internalToExternal(lastDemandedInternalPosition), 1, getOutputFormat())[0] + "*";
				}
			}
		} catch (DeviceException e) {
			logger.warn("{}: exception formatting demand position", getName(), e);
		}
		return report;
	}

	/**
	 * Get the speed of the underlying motor
	 *
	 * @return speed in the motor's units
	 * @throws DeviceException
	 */
	@Override
	public double getSpeed() throws DeviceException {
		try {
			if (this.motor != null) {
				return motor.getSpeed();
			}
		} catch (MotorException e) {
			logger.error("{}: exception while getting speed from motor.", getName(), e);
			throw new DeviceException("Could not get speed", e);
		}
		return -1;
	}

	/**
	 * Set the speed of the underlying motor
	 *
	 * @param theSpeed
	 *            in the motor's units
	 * @throws DeviceException
	 */
	@Override
	public void setSpeed(double theSpeed) throws DeviceException {
		try {
			if (this.motor != null) {
				motor.setSpeed(theSpeed);
			}
		} catch (MotorException e) {
			logger.error(getName() + ": exception while setting speed of motor.", e);
			throw new DeviceException("Could not set speed to " + theSpeed, e);
		}
	}

	/**
	 * Returns this motor's time to velocity.
	 */
	@Override
	public double getTimeToVelocity() throws DeviceException {
		try {
			return motor.getTimeToVelocity();
		} catch (MotorException me) {
			logger.error(String.format("%s: unable to get time to velocity", getName()), me);
			throw new DeviceException("Couldn't get time to velocity", me);
		}
	}

	/**
	 * Sets this motor's time to velocity.
	 */
	@Override
	public void setTimeToVelocity(double timeToVelocity) throws DeviceException {
		try {
			motor.setTimeToVelocity(timeToVelocity);
		} catch (MotorException me) {
			logger.error("{}: unable to set time to velocity to {}", getName(), timeToVelocity, me);
			throw new DeviceException("Could not set timeToVelocity to " + timeToVelocity, me);
		}
	}

	public void handleMotorUpdates(Object changeCode) {

		if (changeCode instanceof MotorStatus) {
			final MotorStatus motorStatus = (MotorStatus) changeCode;

			if (motorStatus == MotorStatus.READY) {
				notifyIObservers(this, ScannableStatus.IDLE);
			} else if (motorStatus == MotorStatus.BUSY) {
				// do nothing as should have already informed IObservers during asynchronousMoveTo
			} else {
				notifyIObservers(this, ScannableStatus.FAULT);
			}
		}

		// to account for inconsistent message types from EpicsMotor
		else if (changeCode instanceof MotorEvent) {
			final MotorEvent motorEvent = (MotorEvent) changeCode;

			if (motorEvent == MotorEvent.MOVE_COMPLETE) {
				notifyIObservers(this, ScannableStatus.IDLE);
			}
		}
	}

	public void setMotorLimitsComponent(MotorLimitsComponent motorLimitsComponent) {
		if (this.motorLimitsComponent != null) {
			removePositionValidator(this.motorLimitsComponent);
		}
		this.motorLimitsComponent = motorLimitsComponent;
		addPositionValidator(getMotorLimitsComponent());
	}

	public MotorLimitsComponent getMotorLimitsComponent() {
		return motorLimitsComponent;
	}

	private boolean isLogMoveRequestsWithInfo() {
		return logMoveRequestsWithInfo;
	}

	public void setLogMoveRequestsWithInfo(boolean logMoveRequestsWithInfo) {
		this.logMoveRequestsWithInfo = logMoveRequestsWithInfo;
	}

	/**
	 * If true, calls to {@link #rawGetPosition()} will return the last demanded position. If the current position is
	 * greater than demandPositionTolerance from the last demanded position the actual current position is returned and
	 * a warning logged and displayed on the console.
	 *
	 * @param returnDemandPosition
	 */
	public void setReturnDemandPosition(boolean returnDemandPosition) {
		this.returnDemandPosition = returnDemandPosition;
	}

	/**
	 * See {@link #setReturnDemandPosition(boolean)}
	 */
	public boolean isReturningDemandPosition() {
		return returnDemandPosition;
	}

	/**
	 * See {@link #setReturnDemandPosition(boolean)} Value is in internal/motor units and scale
	 */
	public void setDemandPositionTolerance(double demandPositionTolerance) {
		this.demandPositionTolerance = demandPositionTolerance;
		this.isDemandPositionToleranceSet = true;
	}

	/**
	 * See {@link #setReturnDemandPosition(boolean)} Value is in internal/motor units and scale
	 */
	@Override
	public double getDemandPositionTolerance() {
		if (!isDemandPositionToleranceSet) {
			try {
				// Do not update isDemandPositionToleranceSet to allow dynamic updating from EPICS
				demandPositionTolerance = motor.getRetryDeadband();
			} catch (MotorException e) {
				logger.error("{} failed to get the tolerance from motor: {}", getName(), e);
			}
		}
		return demandPositionTolerance;
	}

	@Override
	public double getMotorResolution() throws DeviceException {
		// Motor resolution from Epics may be negative!
		return Math.abs(motor.getMotorResolution());
	}

	@Override
	public double getUserOffset() throws DeviceException {
		return motor.getUserOffset();
	}
}
