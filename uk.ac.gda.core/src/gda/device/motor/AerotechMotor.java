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
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Configurable;
import gda.factory.Finder;
import gda.observable.IObservable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Motor class to drive Aerotech motors via the Aerotech 3200 software environment. Communication between this
 * environment and the motor proceeds via a singleton Aerotech3200Controller class.
 */
public class AerotechMotor extends MotorBase implements Configurable, IObservable, Motor {

	private static final Logger logger = LoggerFactory.getLogger(AerotechMotor.class);

	private Aerotech3200Controller controller = null;

	private String aerotechControllerName = null;

	private boolean homeable = true;

	private int axis = 0;

	private int speed = 0;

	private volatile boolean motorMoving = false;

	private boolean moveIsRelative = false;

	private int totalMoveSize;

	private double totalMoveSign;

	private int maximumSafeMove = 100000;

	private int safeMoveWaitTime = 10;

	private int numberOfIncrements;

	private int leftOverBit;

	private int incrementsDoneSoFar;

	private double target;

	@Override
	public void configure() {
		try {
			if ((controller = (Aerotech3200Controller) Finder.getInstance().find(aerotechControllerName)) == null) {
				logger.error("Aerotech3200Controller " + aerotechControllerName + " not found");
			} else {
				setSpeedLevel(1);
			}
		} catch (Exception e) {
			logger.error("Exception while initialising the Aerotech Motor" + e.getMessage());
		}
	}

	/**
	 * @param homeable
	 *            The homeable to set.
	 */
	public void setHomeable(boolean homeable) {
		this.homeable = homeable;
	}

	/**
	 * @return Returns the axis.
	 */
	public int getAxis() {
		return axis;
	}

	/**
	 * @param axis
	 *            The axis to set.
	 */
	public void setAxis(int axis) {
		this.axis = axis;
	}

	/**
	 * @return Returns the aerotechControllerName.
	 */
	public String getAerotechControllerName() {
		return aerotechControllerName;
	}

	/**
	 * @param aerotechControllerName
	 *            The aerotechControllerName to set.
	 */
	public void setAerotechControllerName(String aerotechControllerName) {
		this.aerotechControllerName = aerotechControllerName;
	}

	/**
	 * Moves the motor by the specified number of units
	 *
	 * @param steps
	 *            the number of steps to move by
	 * @throws MotorException
	 */
	@Override
	public void moveBy(double steps) throws MotorException {

		try {
			totalMoveSign = Math.signum(steps);
			totalMoveSize = (int) Math.rint(Math.abs(steps));
			numberOfIncrements = totalMoveSize / maximumSafeMove;
			leftOverBit = totalMoveSize % maximumSafeMove;
			incrementsDoneSoFar = 0;
			motorMoving = true;
			moveIsRelative = true;
			logger.debug("sign, size, nincs, lob " + totalMoveSign + " " + totalMoveSize + " " + numberOfIncrements
					+ " " + leftOverBit);
			if (numberOfIncrements == 0) {
				controller.moveRelative(axis, totalMoveSign * leftOverBit, speed);
			} else {
				controller.moveRelative(axis, totalMoveSign * maximumSafeMove, speed);
			}
		} catch (DeviceException e) {
			motorMoving = false;
			throw (new MotorException(MotorStatus.FAULT, e.getMessage()));
		}
	}

	/**
	 * Moves the motor to the specified target
	 *
	 * @param target
	 *            the target to move to
	 * @throws MotorException
	 */
	@Override
	public void moveTo(double target) throws MotorException {
		try {
			this.target = target;
			totalMoveSign = Math.signum(target - getPosition());
			totalMoveSize = (int) Math.rint(Math.abs(target - getPosition()));
			numberOfIncrements = totalMoveSize / maximumSafeMove;
			leftOverBit = totalMoveSize % maximumSafeMove;
			incrementsDoneSoFar = 0;
			motorMoving = true;
			moveIsRelative = false;
			logger.debug("sign, size, nincs" + totalMoveSign + " " + totalMoveSize + " " + numberOfIncrements);
			if (numberOfIncrements == 0) {
				controller.moveAbsolute(axis, target, speed);
			} else {
				controller.moveRelative(axis, totalMoveSign * maximumSafeMove, speed);
			}
		} catch (DeviceException e) {
			motorMoving = false;
			throw new MotorException(MotorStatus.FAULT, "Error moving - " + getName(), e);
		} catch (InterruptedException e) {
			logger.error("{} - Interrupted while waiting for absolute move", getName(), e);
			Thread.currentThread().interrupt();
			throw new MotorException(MotorStatus.UNKNOWN, getName() + " - Interrupted while waiting for absolute move", e);
		}
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// Not implemented
	}

	/**
	 * Moves the motor to a repeatable starting location
	 *
	 * @throws MotorException
	 */
	@Override
	public void home() throws MotorException {
		try {
			motorMoving = true;
			controller.moveHome(axis);
		} catch (DeviceException e) {
			motorMoving = false;
			throw (new MotorException(MotorStatus.FAULT, e.getMessage()));
		}
	}

	/**
	 * Sets the current position of the motor
	 *
	 * @param value
	 *            the position to set the motor to
	 * @throws MotorException
	 */
	@Override
	public void setPosition(double value) throws MotorException {
		try {
			controller.setPosition(axis, value);
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, e.getMessage()));
		}
	}

	/**
	 * Gets the current position of the motor
	 *
	 * @return the current position
	 * @throws MotorException
	 */
	@Override
	public double getPosition() throws MotorException {
		double position = 0;
		try {
			position = controller.getPosition(axis);
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, e.getMessage()));
		}
		return position;
	}

	/**
	 * Sets the specified motor speed
	 *
	 * @param stepsPerSecond
	 *            the steps per second
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		speed = (int) stepsPerSecond;
		if (speed < 0) {
			throw (new MotorException(MotorStatus.FAULT, "Speed < 0"));
		}
	}

	/**
	 * Checks if the motor is homeable or not. Motor implementations should overide this method if required.
	 *
	 * @return if the motor is homeable. Returns false!
	 */
	@Override
	public boolean isHomeable() {
		return homeable;
	}

	/**
	 * Checks if the motor has been homed or not.
	 *
	 * @return if the motor is homed. Returns false!
	 */
	@Override
	public boolean isHomed() {

		boolean homed = controller.isHomed(axis);
		return homed;
	}

	/**
	 * Gets the motor speed
	 *
	 * @return the motor speed
	 * @throws MotorException
	 */
	@Override
	public double getSpeed() throws MotorException {
		return speed;
	}

	/**
	 * Performs controlled stop of motor move
	 *
	 * @throws MotorException
	 */
	@Override
	public void stop() throws MotorException {
		try {
			controller.moveHalt(axis);
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, e.getMessage()));
		}
	}

	/**
	 * Performs immediate stop of motor move
	 *
	 * @throws MotorException
	 */
	@Override
	public void panicStop() throws MotorException {
		try {
			controller.abortMove(axis);
		} catch (DeviceException e) {
			throw (new MotorException(MotorStatus.FAULT, e.getMessage()));
		}
	}

	/**
	 * Gets the current status message of the motor As the Motor interface does not allow exceptions to be thrown all
	 * diagnostics must be thrown away
	 *
	 * @return a value from the MotorStatus enum
	 */
	@Override
	public MotorStatus getStatus() {
		MotorStatus status = MotorStatus.FAULT;
		String response = null;
		try {
			// Get status string from controller and decode it
			response = controller.getStatusString(axis);
			if (response.equals("BUSY")) {
				status = MotorStatus.BUSY;
			} else if (response == "READY") {
				if (motorMoving) {
					++incrementsDoneSoFar;

					if (incrementsDoneSoFar < numberOfIncrements) {
						logger.debug("incrementsDoneSoFar, numberOfIncrements, moveRelative amount "
								+ incrementsDoneSoFar + " " + numberOfIncrements + " " + totalMoveSign
								* maximumSafeMove);
						status = MotorStatus.BUSY;
						Thread.sleep(safeMoveWaitTime);
						controller.moveRelative(axis, totalMoveSign * maximumSafeMove, speed);
					} else if (incrementsDoneSoFar == numberOfIncrements) {
						if (moveIsRelative) {
							logger.debug("incrementsDoneSoFar, numberOfIncrements, moveRelative amount "
									+ incrementsDoneSoFar + " " + numberOfIncrements + " " + totalMoveSign
									* leftOverBit);
							status = MotorStatus.BUSY;
							Thread.sleep(safeMoveWaitTime);
							controller.moveRelative(axis, totalMoveSign * leftOverBit, speed);
						} else {
							logger.debug("incrementsDoneSoFar, numberOfIncrements, moveAbsolute target "
									+ incrementsDoneSoFar + " " + numberOfIncrements + " " + target);
							status = MotorStatus.BUSY;
							Thread.sleep(safeMoveWaitTime);
							controller.moveAbsolute(axis, target, speed);
						}
					} else
					// incrementsDoneSoFar > numberOfIncrements (does this
					// ever
					// happen?)
					{
						motorMoving = false;
						status = MotorStatus.READY;
					}
				} else
				// motorMoving == false
				{
					status = MotorStatus.READY;
				}
			} else if (response.equals("UPPERLIMIT")) {
				motorMoving = false;
				status = MotorStatus.UPPERLIMIT;
			} else if (response.equals("LOWERLIMIT")) {
				motorMoving = false;
				status = MotorStatus.LOWERLIMIT;
			}
		} catch (DeviceException e) {
			logger.error(getName() + " : caught DeviceException in getStatus : " + e.getMessage());
			logger.error(response + "!");
			motorMoving = false;
			status = MotorStatus.FAULT;
		} catch (InterruptedException ie) {
			logger.error(getName() + " : caught InterruptedException in getStatus : " + ie.getMessage());
			logger.error(response + "!");
			motorMoving = false;
			status = MotorStatus.FAULT;
		}
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
		return motorMoving;
	}

	/**
	 * Not implemented
	 */
	@Override
	public void reconfigure() {
		// not implemented
	}

	/**
	 * @return maximumSafeMove
	 */
	public int getMaximumSafeMove() {
		return maximumSafeMove;
	}

	/**
	 * @param maximumSafeMove
	 */
	public void setMaximumSafeMove(int maximumSafeMove) {
		this.maximumSafeMove = maximumSafeMove;
	}

	/**
	 * @return safeMoveWaitTime
	 */
	public int getSafeMoveWaitTime() {
		return safeMoveWaitTime;
	}

	/**
	 * @param safeMoveWaitTime
	 */
	public void setSafeMoveWaitTime(int safeMoveWaitTime) {
		this.safeMoveWaitTime = safeMoveWaitTime;
	}
}