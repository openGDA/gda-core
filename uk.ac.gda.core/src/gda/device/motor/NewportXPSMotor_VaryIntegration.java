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

package gda.device.motor;

import gda.device.MotorException;
import gda.device.MotorStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A temporary fix for certain unstable Newport XPS motors. When moves are made the KI value in the PID integration time
 * must be adjusted before making the move and then reset after the move has completed.
 */
public final class NewportXPSMotor_VaryIntegration extends NewportXPSMotor {

	private static final Logger logger = LoggerFactory.getLogger(NewportXPSMotor_VaryIntegration.class);

	private double KIWhenMoving = 0;

	private double KIWhenStationary = 0;

	@Override
	public void moveBy(double steps) throws MotorException {
		checkControllerAvailable();

		changeKI(getKIWhenMoving());

		// do move
		newportXpsController1.xpswritenowait("GroupMoveRelative(" + xpsGroupName + ", " + steps + ")");

		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: moveBy() Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}

		// start a thread which will reset KI when the move has completed
		new resetKIWhenMoveComplete().start();
	}

	@Override
	public void moveTo(double steps) throws MotorException {

		checkControllerAvailable();

		changeKI(getKIWhenMoving());

		newportXpsController1.xpswritenowaitWithHalt("GroupMoveAbsolute(" + xpsGroupName + ", " + steps + ")");

		int error = Integer.parseInt(newportXpsController1.getErrnum());
		logger.debug("NewportXPSMotor: moveTo() returned error no:" + error);

		// start a thread which will reset KI when the move has completed
		new resetKIWhenMoveComplete().start();
	}

	/**
	 * @return Returns the kIWhenMoving.
	 */
	public double getKIWhenMoving() {
		return KIWhenMoving;
	}

	/**
	 * @param whenMoving
	 *            The kIWhenMoving to set.
	 */
	public void setKIWhenMoving(double whenMoving) {
		KIWhenMoving = whenMoving;
	}

	/**
	 * @return Returns the kIWhenStationary.
	 */
	public double getKIWhenStationary() {
		return KIWhenStationary;
	}

	/**
	 * @param whenStationary
	 *            The kIWhenStationary to set.
	 */
	public void setKIWhenStationary(double whenStationary) {
		KIWhenStationary = whenStationary;
	}

	/**
	 * @param newKIValue
	 * @throws MotorException
	 */
	public synchronized void changeKI(double newKIValue) throws MotorException {

		// get the current values
		newportXpsController1
				.xpswrite("PositionerCorrectorPIDFFVelocityGet("
						+ this.xpsGroupName
						+ "."
						+ this.xpsPositionerName
						+ ",bool *, double *, double *, double *, double *, double *, double *, double *, double *, double *, double *, double *)");
		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}

		// get the 12 element array back. KI is element number 2
		String[] retVal = newportXpsController1.getRetValue();

		// set the new values keeping all the same except KI
		String command = "PositionerCorrectorPIDFFVelocitySet(" + this.xpsGroupName + "." + this.xpsPositionerName
				+ "," + retVal[0] + "," + retVal[1] + "," + newKIValue + "," + retVal[3] + "," + retVal[4] + ","
				+ retVal[5] + "," + retVal[6] + "," + retVal[7] + "," + retVal[8] + "," + retVal[9] + "," + retVal[10]
				+ "," + retVal[11] + ")";
		newportXpsController1.xpswrite(command);

		error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	/**
	 * Thread which will wait for the current move to complete and then send an update to the KI value
	 */
	public class resetKIWhenMoveComplete extends Thread {

		@Override
		public void run() {
			// wait until the movement is complete
			try {
				do {
					Thread.sleep(50);
				} while (isMoving());
			} catch (Exception e) {
				// ignore errors, whatever happens we want to try to reset KI
			}

			// set the KI value
			try {
				changeKI(getKIWhenStationary());
			} catch (MotorException e) {
				logger.error("{} error occurred after move completed when trying to reset the KI value", getName(), e);
			}

		}
	}

}
