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

package uk.ac.gda.server.exafs.epics.device.scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.ScannableMotor;


/**
 * This class is a simple ScannableMotor for use in a Continuous scan. The underlying motor can be either a real or dummy motor.
 * <li> The motor will move from start to end position at constant velocity in the time specified by the {@link ContinuousParameters}.
 * <li> A rampDistance either end of the start and end positions can be used to help ensure the motor
 * is moving at a uniform speed within the scan range.
 * <li> The motor move to the initial position is carried out at the maxMotorSpeed.
 * <li> The initial motor speed before the scan is restored after the move is complete.
 */
public class QexafsTestingScannable extends ScannableMotor implements ContinuouslyScannable {

	private static final Logger logger = LoggerFactory.getLogger(QexafsTestingScannable.class);

	private ContinuousParameters continuousParameters;

	private double rampDistance = 0.0; //Distance either side of ContinuousParameters start and end positions the motor will actually move between
	private double maxMotorSpeed = 1000.0;
	private double speedBeforeScan = 1.0;

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		try {
			speedBeforeScan = getSpeed();
			super.setSpeed(maxMotorSpeed);
		} catch (DeviceException e) {
			logger.error("Could not set speed to {} before move to start position", maxMotorSpeed, e);
		}
		super.moveTo(continuousParameters.getStartPosition()-getRampInScanDirection());
	}

	@Override
	public void performContinuousMove() throws DeviceException {
		double start = continuousParameters.getStartPosition();
		double end = continuousParameters.getEndPosition();
		double speed = (end-start) / continuousParameters.getTotalTime();
		super.setSpeed(speed);
		super.asynchronousMoveTo(continuousParameters.getEndPosition()+getRampInScanDirection());
	}

	@Override
	public void continuousMoveComplete(){
		try {
			super.stop();
			super.setSpeed(speedBeforeScan);
		} catch (DeviceException e) {
			logger.error("Could not set speed back to {} at end of move", speedBeforeScan, e);
		}
		logger.info("Continuous move completed.");
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	@Override
	public double calculateEnergy(int frameIndex) {
		double start = continuousParameters.getStartPosition();
		double end = continuousParameters.getEndPosition();
		int noPoints = continuousParameters.getNumberDataPoints();
		double step = (end-start)/noPoints;
		return start+(step*frameIndex);
	}

	@Override
	public int getNumberOfDataPoints() {
		return continuousParameters.getNumberDataPoints();
	}

	private double getRampInScanDirection() {
		if (continuousParameters.getStartPosition() < continuousParameters.getEndPosition()) {
			return rampDistance;
		} else {
			return -1.0 * rampDistance;
		}
	}

	public double getRampDistance() {
		return rampDistance;
	}

	public void setRampDistance(double rampDistance) {
		this.rampDistance = rampDistance;
	}

	public double getMaxMotorSpeed() {
		return maxMotorSpeed;
	}

	public void setMaxMotorSpeed(double maxMotorSpeed) {
		this.maxMotorSpeed = maxMotorSpeed;
	}
}