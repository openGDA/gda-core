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

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Configurable;
import gda.observable.IObservable;

/**
 * A Dummy motor class.
 */
public class AerotechDummyMotor extends MotorBase implements Runnable, Configurable, IObservable, Motor {

	private static final Logger logger = LoggerFactory.getLogger(AerotechDummyMotor.class);

	// README: This is used to ensure that the waiting thread know it has
	// been
	// woken for the correct reason, and that a move is requested. It is
	// reset once the move is completed OR by the stop method as a means
	// to end the move part way through.
	private volatile boolean simulatedMoveRequired = false;

	private volatile boolean motorMoving = false;

	private volatile boolean waiting = false;

	private volatile double currentPosition;

	private double currentspeed;

	private String name;

	private Thread runner;

	private double targetposition;

	private volatile MotorStatus status;

	// private int axis = 1;
	// private int nextErrorStatus = MotorStatus._UPPERLIMIT;
	private boolean homeable = false; // motor is homeable if true

	/**
	 * Poistion increment when motor is driven.
	 */
	private double posinc;

	// FIXME: rename variable to make it clearer at the point of use.
	// private boolean initialized = false;
	private Random random = new Random();

	/**
	 * Class generates random errors, used to determine frequency.
	 */
	private double gaussianErrorThreshold = 10.0;

	private int limitCount = 0; // FIXME: clarify.

	/**
	 * Number of position updates when being driven in any mode other than continuous.
	 */
	private int nonContinuousIncrements = 10;

	/**
	 * Number of position updates when being driven in continuous mode. Can be configured via XML.
	 */
	private static final int continuousIncrements = 1000000;

	private int increments = nonContinuousIncrements;

	private long sleepTime = 5;

	// private int speed = 0;
	// private Aerotech3200Controller controller = null;
	private String aerotechControllerName = null;

	/**
	 * Constructor.
	 */
	public AerotechDummyMotor() {
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		runner.start();

		// README: We have to be sure that the monitoring thread is ready for
		// duty.
		// Problems have occured by things being notified before they are
		// waiting and thus they never come out of their wakes when they
		// finally get there.
		while (!waiting) {
			Thread.yield();
		}
		Thread.yield();

		status = MotorStatus.READY;
	}

	@Override
	public String toString() {
		return ("An AerotechDummyMotor named " + name);
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

	@Override
	public void configure() {
		try {
		} catch (Exception e) {
			logger.error("Exception while initialising the Aerotech Motor");
		}
		loadPosition(this.getName());
	}

	/**
	 * @return nonContinuousIncrements
	 */
	public int getNonContinuousIncrements() {
		return nonContinuousIncrements;
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
	public void moveTo(double newpos) {
		// README: Set the status now otherwise checking status immediately
		// after
		// moveTo may return the wrong answer
		status = MotorStatus.BUSY;
		increments = nonContinuousIncrements;
		double increment = newpos - currentPosition;
		newpos = addInBacklash(increment) + currentPosition;
		targetposition = newpos;
		posinc = (targetposition - currentPosition) / increments;
		motorMoving = true;
		synchronized (this) {
			simulatedMoveRequired = true;
			notifyAll();
		}
	}

	@Override
	public void moveContinuously(int direction) {
		// README: Set the status now otherwise checking status immediately
		// after
		// moveContinuously may return the wrong answer
		status = MotorStatus.BUSY;
		increments = continuousIncrements;
		posinc = 1 * direction;
		motorMoving = true;
		synchronized (this) {
			simulatedMoveRequired = true;
			notifyAll();
		}
	}

	@Override
	public void moveBy(double amount) {
		// README: Set the status now otherwise checking status immediately
		// after
		// moveBy may return the wrong answer
		status = MotorStatus.BUSY;
		increments = nonContinuousIncrements;
		targetposition = currentPosition + addInBacklash(amount);
		posinc = (targetposition - currentPosition) / increments;
		motorMoving = true;
		synchronized (this) {
			simulatedMoveRequired = true;
			notifyAll();
		}
	}

	@Override
	public void setPosition(double newPosition) {
		currentPosition = newPosition;
	}

	@Override
	public double getPosition() {
		return currentPosition;
	}

	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		currentspeed = stepsPerSecond;
	}

	@Override
	public double getSpeed() throws MotorException {
		return currentspeed;
	}

	@Override
	public void stop() throws MotorException {
		simulatedMoveRequired = false;
	}

	@Override
	public void panicStop() throws MotorException {
	}

	@Override
	public MotorStatus getStatus() {
		return status;
	}

	@Override
	public boolean isMoving() {
		return motorMoving;
	}

	/**
	 * Thread to do the position updating. This is where the real work of the class is done.
	 */

	@Override
	public void run() {
		int i = 0;

		while (runner != null) {
			synchronized (this) {
				try {
					notifyIObservers(this, null);
					waiting = true;
					do {
						logger.debug("dummy motor main wait");
						wait();
						logger.debug("dummy motor main wake up");
					} while (!simulatedMoveRequired);
				} catch (Exception ex) {
					logger.warn(ex.getMessage());
				}
			}

			if (limitCount == 0)
				status = MotorStatus.BUSY;

			for (i = 0; i < increments; i++) {
				if (simulatedMoveRequired) {
					if (status == MotorStatus.UPPER_LIMIT) {
						if (posinc < 0)
							limitCount--;
						else
							break;
					} else if (status == MotorStatus.LOWER_LIMIT) {
						if (posinc > 0)
							limitCount--;
						else
							break;
					} else {
						if (Math.abs(random.nextGaussian()) > gaussianErrorThreshold) {
							limitCount = 4;
							if (posinc > 0)
								status = MotorStatus.UPPER_LIMIT;
							else if (posinc < 0)
								status = MotorStatus.LOWER_LIMIT;
							break;
						}
					}

					if (limitCount == 0)
						status = MotorStatus.BUSY;
					currentPosition += posinc;
					notifyIObservers(this, null);
				} else
					break;

				try {
					logger.debug("dummy motor sleep wait");
					Thread.sleep(sleepTime);
					logger.debug("dummy motor sleep wake up");
				} catch (InterruptedException ex) {
				}
			}

			if (i == increments)
				currentPosition = targetposition;
			if (status == MotorStatus.BUSY)
				status = MotorStatus.READY;

			simulatedMoveRequired = false;
			motorMoving = false;
		}
	}

	/**
	 * returns the motor to a initial repeatable starting location
	 *
	 * @throws MotorException
	 */
	@Override
	public synchronized void home() throws MotorException {
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
}