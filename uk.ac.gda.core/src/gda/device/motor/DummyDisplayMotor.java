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

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Finder;
import gda.observable.IObservable;

/**
 * A Dummy version of the specialised Motor class that reads feedback from a Protrura encoder display module. It has two
 * motors associated with it and these MUST be configured BEFORE this module.
 */
public class DummyDisplayMotor extends MotorBase implements Runnable, IObservable, Motor {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyDisplayMotor.class);
	
	private double currentPosition;

	private Motor motorOne;

	private Motor motorTwo;

	private String motorOneName;

	private String motorTwoName;

	private Thread runner;

	private long sleepTime = 100;

	/**
	 * Constructor
	 */
	public DummyDisplayMotor() {
	}

	@Override
	public void configure() {
		logger.debug("DummyDisplayMotor: Finding: " + motorOneName);
		if ((motorOne = (Motor) Finder.getInstance().find(motorOneName)) == null) {
			logger.error("DummyDisplayMotor " + motorOneName + " not found");
		} else {
			logger.debug("MotorOne found.");
		}

		logger.debug("DummyDisplayMotor: Finding: " + motorTwoName);
		if ((motorTwo = (Motor) Finder.getInstance().find(motorTwoName)) == null) {
			logger.error("DummyDisplayMotor " + motorTwoName + " not found");
		} else {
			logger.debug("MotorTwo found.");
		}

		try {
			// README This depends on the two motors already being
			// configured
			// otherwise these will fail
			currentPosition = getPosition();
		} catch (MotorException e) {
			logger.error("DummyDisplayMotor: Exception caught getting position");
		}

		// FIXME: Remove this when chance to fully tested removal has worked
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		runner.start();
		runner.setName(getClass().getName() + " " + getName());
		logger.debug("Loaded motor position " + currentPosition);
	}

	/**
	 * @param motorOneName
	 */
	public void setMotorOneName(String motorOneName) {
		this.motorOneName = motorOneName;
	}

	/**
	 * @return motor one name
	 */
	public String getMotorOneName() {
		return motorOneName;
	}

	/**
	 * @param motorTwoName
	 */
	public void setMotorTwoName(String motorTwoName) {
		this.motorTwoName = motorTwoName;
	}

	/**
	 * @return motor two name
	 */
	public String getMotorTwoName() {
		return motorTwoName;
	}

	/**
	 * @param sleepTime
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * @return sleep time
	 */
	public long getSleepTime() {
		return sleepTime;
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		// Cannot move this device
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		// Cannot move this device
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// Cannot move this device
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		// Can only Zero position
	}

	@Override
	public synchronized double getPosition() throws MotorException {
		double coarse = motorOne.getPosition();
		double fine = motorTwo.getPosition();

		logger.debug("DummyDisplayMotor: Coarse position is : " + coarse + "Fine position is: " + fine);
		// convert both to nanometres
		currentPosition = (coarse / 0.021) + (fine);
		logger.debug("DummyDisplayMotor: current position is: " + currentPosition);
		return currentPosition;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		// Cannot set speed on this device
	}

	@Override
	public double getSpeed() throws MotorException {
		return 0;
		// Cannot set speed on this device
	}

	@Override
	public void stop() throws MotorException {
		// Not relevant
	}

	@Override
	public void panicStop() throws MotorException {
		// Not relevant
	}

	@Override
	public MotorStatus getStatus() {
		logger.debug("DummyDisplayMotor returning READY in getStatus ");
		return MotorStatus.READY;
	}

	@Override
	public boolean isMoving() throws MotorException {
		logger.debug("DummyDisplayMotor returning false in isMoving ");
		return false;
	}

	@Override
	public void run() {
		// FIXME: This is not necessary - it should poll - remove runner after
		// fully tested
	}
}
