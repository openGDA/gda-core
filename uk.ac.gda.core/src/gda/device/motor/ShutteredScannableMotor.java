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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO need to better document how the different parameters from MXCameraForDummy are rolled into parameters here
 */
public class ShutteredScannableMotor extends DeviceBase implements ShutteredMotorController {

	private static final Logger logger = LoggerFactory.getLogger(ShutteredScannableMotor.class);

	public double acceleration = 10;
	
	private double startPosition = 0, endPosition = 0, closePosition = 0, moveDistance = 0;

	private double shutterOpenTriggeredPosition = 0, minimumStartToOpenPositionDifference = 0.01;

	private double defaultSpeed = 0;

	private double desiredTimeToVelocity = 0, defaultTimeToVelocity = 0, minimumTimeToVelocity = 0.1;

	private double startTimeFudgeFactor = 0.4, endTimeFudgeFactor = 0.4;

	private ScannableMotor theMotor;

	private double exposureTime = 1;

	private double desiredVelocity = 1;

	private double minExposeTime;

	private double shutterIsOpenPosition;

	private double shutterOpenTime;
	
	@Override
	public void configure() throws FactoryException {
	}

	/**
	 * The fudge factor is for making sure we start outside of the open range
	 */
	private double positionFudgeFactor = 0.0;
	
	//the origin of much of the code below is the gda/px/camera/MXCamera class.
	/**
	 * Calculates the start/stop and shutter open/close parameters during the exposure. This should be automatically
	 * called when any of the three exposure parameters are changed (image-start/time/positions).
	 */
	@Override
	public void doCalculations() {
		calculateDesiredVelocity();
		calculateDesiredAccelerationTime();
		calculatePositionWhereShutterIsTriggeredToOpen();
		startPosition = calculateStartPosition();
		endPosition = calculateEndPosition();
		calculateMinimumExposeTime();
	}

	@Override
	public void expose() throws DeviceException {
		logger.debug("SSM.expose: motor move to " + getEndPosition());
		theMotor.moveTo(getEndPosition());
	}

	protected void calculateDesiredVelocity() {
		desiredVelocity = moveDistance / exposureTime;
	}

	protected void calculateDesiredAccelerationTime() {
		desiredTimeToVelocity = desiredVelocity / acceleration;
		
		// time must be greater than twice the Ixx21 value in the PMAC
		// (effectively a maximum acceleration of the motor)
		if (desiredTimeToVelocity < minimumTimeToVelocity) {
			logger.debug("the desired time to velocity is smaller than the minimum, resetting to " + minimumTimeToVelocity);
			desiredTimeToVelocity = minimumTimeToVelocity;
		}
	}
	
	protected void calculatePositionWhereShutterIsTriggeredToOpen() {
		shutterOpenTriggeredPosition = shutterIsOpenPosition - (desiredVelocity * shutterOpenTime);
	}
	
	protected double calculateStartPosition() {

		double safetyFactor = desiredVelocity * startTimeFudgeFactor;
		double difference = ((desiredVelocity * desiredTimeToVelocity) / 2 + safetyFactor);

		// if difference too small then goniometer may be too close to the shutter open angle and shutter may not open
		// especially on I24, where the number of encoder counts is about 100/degree
		if (difference < getMinimumStartToOpenPositionDifference()) {
			logger.debug("difference between open and start positions is " + difference + ", increasing to " + getMinimumStartToOpenPositionDifference());
			difference = getMinimumStartToOpenPositionDifference();
		}

		return shutterOpenTriggeredPosition - difference  - positionFudgeFactor;
	}

	protected double calculateEndPosition() {
		double imageEnd = shutterIsOpenPosition + moveDistance;
		double safetyFactor = desiredVelocity * endTimeFudgeFactor;
		double difference = (desiredVelocity * desiredTimeToVelocity) / 2 + safetyFactor;

		// if difference too small then goniometer may be too close to the shutter close angle and shutter may not open
		if (difference < getMinimumStartToOpenPositionDifference()) {
			logger.debug("difference between close and end positions is " + difference + ", increasing to " + getMinimumStartToOpenPositionDifference());
			difference = getMinimumStartToOpenPositionDifference();
		}

		return imageEnd + difference  + positionFudgeFactor;
	}

	protected void calculateMinimumExposeTime() {
		// work out a safe time to operate detector for (must be > phi move time)
		// revert to what was in MXCamera before r18219
		double accelTime =  (shutterIsOpenPosition - calculateStartPosition()) / desiredVelocity + desiredTimeToVelocity / 2;
		double decelTime = (calculateEndPosition() - (shutterIsOpenPosition + moveDistance)) /  desiredVelocity + desiredTimeToVelocity / 2;
		minExposeTime  = accelTime + exposureTime + decelTime;
	}

	@Override
	public void setDefaultSpeeds() throws DeviceException {
		setMotorSpeed(defaultSpeed);
		setMotorTimeToVelocity(defaultTimeToVelocity);
	}
	
	public void setDesiredSpeeds() throws DeviceException {
		setMotorSpeed(getDesiredSpeed());
		setMotorTimeToVelocity(getDesiredTimeToVelocity());
	}
	
	private void setMotorSpeed(double speed) throws DeviceException {
		logger.debug("SSM.setMotorSpeed: setting speed to " + speed);
		theMotor.setSpeed(speed);
	}
	
	private void setMotorTimeToVelocity(double ttv) throws DeviceException {
		logger.debug("SSM.setMotorTimeToVelocity: setting timeToVelocity to " + ttv);
		theMotor.setTimeToVelocity(ttv);
	}
	
	@Override
	public double getDefaultSpeed() {
		return defaultSpeed;
	}

	@Override
	public double getDefaultTimeToVelocity() {
		return defaultTimeToVelocity;
	}

	@Override
	public double getDesiredSpeed() {
		return desiredVelocity;
	}

	@Override
	public double getDesiredTimeToVelocity() {
		return desiredTimeToVelocity;
	}

	@Override
	public double getEndPositionOffset() {
		return getEndPosition() - getClosePosition();
	}

	@Override
	public double getEndTimeFudgeFactor() {
		return endTimeFudgeFactor;
	}

	@Override
	public double getStartPositionOffset() {
		return getOpenPosition() - getStartPosition();
	}

	@Override
	public double getStartTimeFudgeFactor() {
		return startTimeFudgeFactor;
	}

	@Override
	public void prepareForExposure() throws DeviceException {
		try {
			logger.debug("SSM.prepareForExposure: move to "+getStartPosition());
			theMotor.moveTo(getStartPosition());
			setMotorSpeed(getDesiredSpeed());
			setMotorTimeToVelocity(getDesiredTimeToVelocity());
		} catch (DeviceException e) {
			logger.error("Exception while preparing axis for exposure",e);
			throw new DeviceException("Exception while preparing axis for exposure", e);
		}
	}

	@Override
	public void setDefaultSpeed(double defaultSpeed) {
		this.defaultSpeed = defaultSpeed;
	}

	@Override
	public void setDefaultTimeToVelocity(double desiredTimeToVelocity) {
		this.defaultTimeToVelocity = desiredTimeToVelocity;
	}

	@Override
	public void setDesiredSpeed(double desiredSpeed) {
		this.desiredVelocity = desiredSpeed;
		doCalculations();
	}

	@Override
	public void setDesiredTimeToVelocity(double desiredTimeToVelocity) {
		this.desiredTimeToVelocity = desiredTimeToVelocity;
		doCalculations();
	}

	@Override
	public void setClosePosition(double closePosition) {
		this.closePosition = closePosition;
		this.moveDistance = closePosition - shutterIsOpenPosition;
		doCalculations();
	}

	@Override
	public double getClosePosition() {
		return closePosition;
	}

	@Override
	public void setEndTimeFudgeFactor(double fudgeTime) {
		this.endTimeFudgeFactor = fudgeTime;
		doCalculations();
	}

	@Override
	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
		doCalculations();
	}

	@Override
	public double getOpenPosition() {
		return shutterIsOpenPosition;
	}

	@Override
	public void setOpenPosition(double openPosition) {
		this.shutterIsOpenPosition = openPosition;
		this.closePosition = this.shutterIsOpenPosition + this.moveDistance;
		doCalculations();
	}
	
	public void setShutterOpenTime(double openTime) {
		this.shutterOpenTime = openTime;
		doCalculations();
	}

	@Override
	public void setStartTimeFudgeFactor(double fudgeTime) {
		this.startTimeFudgeFactor = fudgeTime;
		doCalculations();
	}

	@Override
	public double getStartPosition() {
		return startPosition;
	}

	@Override
	public double getEndPosition() {
		return endPosition;
	}

	@Override
	public double getMoveDistance() {
		return moveDistance;
	}

	@Override
	public void setMoveDistance(double moveDistance) {
		this.moveDistance = moveDistance;
		this.closePosition = this.shutterIsOpenPosition + this.moveDistance;
		doCalculations();
	}

	public double getMinExposeTime() {
		doCalculations();
		return minExposeTime;
	}

	@Override
	public void setMotor(ScannableMotor theMotor) {
		this.theMotor = theMotor;
	}

	@Override
	public double getMinimumTimeToVelocity() {
		return minimumTimeToVelocity;
	}

	@Override
	public void setMinimumTimeToVelocity(double minimumTimeToVelocity) {
		this.minimumTimeToVelocity = minimumTimeToVelocity;
	}

	@Override
	public double getMinimumStartToOpenPositionDifference() {
		return minimumStartToOpenPositionDifference;
	}
	
	public Object getPosition() throws DeviceException {
		return theMotor.getPosition();
	}
	
	public void asynchronousMoveTo(Object newPosition) throws DeviceException {
		theMotor.asynchronousMoveTo(newPosition);
	}

	public double getPositionFudgeFactor() {
		return positionFudgeFactor;
	}
	
	public void setPositionFudgeFactor(double positionFudgeFactor) {
		this.positionFudgeFactor = positionFudgeFactor;
	}

	@Override
	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	@Override
	public double getAcceleration() {
		return acceleration;
	}
}