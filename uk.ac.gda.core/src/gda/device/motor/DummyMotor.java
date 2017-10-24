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
import gda.device.MotorProperties.MotorEvent;
import gda.device.MotorStatus;
import gda.device.scannable.MotorUnitStringSupplier;
import gda.observable.IObservable;
import uk.ac.gda.util.ThreadManager;

/**
 * A Dummy motor class
 */
public class DummyMotor extends MotorBase implements Runnable, IObservable, Motor, MotorUnitStringSupplier {

	private static final Logger logger = LoggerFactory.getLogger(DummyMotor.class);

	// Moves are simulated in a separate Thread (see run()) which
	// add positionIncrement to currentPosition numberOfIncrements times
	// at intervals of incrementalSleepTime.
	private int numberOfIncrements;

	private int incrementalSleepTime;

	private double positionIncrement;

	// These are the two default values for numberOfIncrements
	private static final int CONTINUOUS_INCREMENTS = 1000000;

	private static final int NON_CONTINUOUS_INCREMENTS = 10;

	// This flag is used to indicate to the run() thread
	// whether or not it should be trying to simulate a move.
	private volatile boolean simulatedMoveRequired = false;

	// This flag has the same purpose as similar flags in real motors -
	// to indicate to callers of isMoving() whether or not the motor is
	// actually moving.
	private volatile boolean motorMoving = false;

	// This flag is used by the run() thread to indicate that it has
	// reached the wait() state - i.e. that it is ready to work.
	private volatile boolean waiting = false;

	private volatile double currentPosition;

	private volatile MotorStatus status;

	private double speed = 0;

	private double timeToVelocity = 0.1;

	private double targetPosition;

	private Thread runner;

	private Random random = new Random();

	//dimensionless ( == 1) by default
	private String unitString = "";

	// If randomlyProduceExceptions is true then a limit will be generated
	// during a move if random.nextGaussian() produces a value greater
	// than randomLimitTriggerLevel. When a limit is set limitCount is set
	// to 4.This means that if the next move is away from the limit the
	// first
	// 4 incremental moves will still show the limit and then it will clear.
	private boolean randomlyProduceLimits = false;

	private boolean randomPositionVariation=false;
	private double randomPositionVariationAmount = 0.;

	private double randomLimitTriggerLevel = 10.0;

	// See comment on randomlyProduceLimits
	private int limitCount = 0;

	// If randomlyProduceExceptions is true then moveTo will generate
	// an exception if random.nextGaussian() produces a value greater
	// than randomeExceptionTriggerLevel.
	private boolean randomlyProduceExceptions = false;

	private double randomExceptionTriggerLevel = 10.0;

	// These flags are used to simulate a homing move.
	private boolean isHomeable = false;

	private boolean homed = false;

	private boolean homing = false;

	@Override
	public void configure() {
		runner = ThreadManager.getThread(this, getClass().getName());
		runner.start();

		// We have to be sure that the monitoring thread is ready for
		// work. So this thread yields until the runner thread has set
		// the waiting flag to true. It might be better to actually
		// wait() but since this only happens at creation it is unlikely
		// to cause a problem.
		while (!waiting) {
			Thread.yield();
		}
		Thread.yield();

		status = MotorStatus.READY;
		isInitialised = true;

		runner.setName(getClass().getName() + " " + getName());
		loadPosition(getName(), currentPosition);
		logger.debug("Loaded motor position " + getPosition());
		if (speed == 0.0) {
			speed = getSlowSpeed();
			if (speed == 0.0) {
				speed = 1.0;
			}
		}

		// if limits not set, then set them to something useable during testing
		if (Double.isNaN(minPosition)) {
			this.minPosition = -Double.MAX_VALUE;
		}

		if (Double.isNaN(maxPosition)) {
			this.maxPosition = Double.MAX_VALUE;
		}

		this.isInitialised = true;
	}

	/**
	 * Returns a string to represent the motor.
	 *
	 * @return a string to represent the motor
	 */
	@Override
	public String toString() {
		return ("DummyMotor " + getName() + " currently at position " + currentPosition + " with status " + status);
	}

	/**
	 * Starts a move to requestedPosition
	 *
	 * @param requestedPosition
	 * @throws MotorException
	 */
	@Override
	public void moveTo(double requestedPosition) throws MotorException {

		if (randomlyProduceExceptions && Math.abs(random.nextGaussian()) > randomExceptionTriggerLevel) {
			logger.debug("DummyMotor {} randomly throwing exception", getName());
			throw new MotorException(MotorStatus.FAULT, "Random dummy motor fault");
		}

		double positionChange = requestedPosition - currentPosition;
		if (positionChange != 0.0) {
			// The status must be set first otherwise checking status immediately
			// after moveTo may return the wrong answer
			status = MotorStatus.BUSY;

			// The targetPosition is the currentPosition plus the positionChange
			// adjusted for any specified backlash. (The backlash will be corrected
			// later by the Positioner.)
			targetPosition = addInBacklash(positionChange) + currentPosition;

			// The numberOfIncrements used is based on the expected time the move
			// will take. It should be large enough so that the position changes
			// at least every half a second.
			double totalExpectedMoveTime = Math.abs((targetPosition - currentPosition) / speed);
			numberOfIncrements = Math.max(NON_CONTINUOUS_INCREMENTS, (int) (totalExpectedMoveTime * 2.0));

			// This is the number of steps the motor will appear to move each time.
			positionIncrement = (targetPosition - currentPosition) / numberOfIncrements;

			// The currentSpeed is in steps per second, incrementalSleepTime should
			// be in ms hence the 1000.0 in the calculation.
			incrementalSleepTime = (int) Math.abs(positionIncrement * 1000.0 / speed);

			// An incrementalSleepTime of 0 will cause disaster,
			if (incrementalSleepTime == 0)
				incrementalSleepTime = 1;

			logger.trace("DummyMotor speed is: {} steps per second", speed);
			logger.trace("DummyMotor total move is: {} steps", (targetPosition - currentPosition));
			logger.trace("DummyMotor move ought to take: {} seconds", totalExpectedMoveTime);
			logger.trace("DummyMotor number of numberOfIncrements: {}", numberOfIncrements);
			logger.trace("DummyMotor incrementalSleepTime is: {} milliseconds", incrementalSleepTime);
			logger.trace("DummyMotor expected total time for this move: {}s",
					Math.abs(incrementalSleepTime * numberOfIncrements / 1000.0));

			motorMoving = true;
			synchronized (this) {
				simulatedMoveRequired = true;
				notifyAll();
			}
		} else {
			notifyIObservers(this, MotorEvent.MOVE_COMPLETE);
		}
	}

	/**
	 * Starts the motor moving continuously
	 *
	 * @param direction
	 *            the direction to move in
	 */
	@Override
	public void moveContinuously(int direction) {
		// Continuous movement is simulated by doing a very large
		// numberOfIncrements in the relevant direction with a very small
		// incrementalSleepTime.
		status = MotorStatus.BUSY;
		numberOfIncrements = CONTINUOUS_INCREMENTS;
		positionIncrement = direction;
		incrementalSleepTime = 1;
		motorMoving = true;
		synchronized (this) {
			simulatedMoveRequired = true;
			notifyAll();
		}
	}

	/**
	 * Moves the motor by the specified amount.
	 *
	 * @param amount
	 *            the specified amount.
	 * @throws MotorException
	 */
	@Override
	public void moveBy(double amount) throws MotorException {
		moveTo(currentPosition + amount);
	}

	/**
	 * Sets the current position (i.e. changes the value without actually moving).
	 *
	 * <p>This property can be used in Spring configuration to define what the
	 * initial position of the motor should be after the motor object has been
	 * configured.
	 *
	 * <p>However if a different position has previously been saved to file,
	 * the motor will instead be at that position after it has been configured.
	 *
	 * <p>In other words: at configuration time, a previously-saved position
	 * takes precedence over the initial position set using the
	 * {@code position} property.
	 *
	 * @param newPosition
	 *            the current position
	 */
	@Override
	public void setPosition(double newPosition) {
		if (currentPosition != newPosition) {
			currentPosition = newPosition;
			if (isInitialised) {
				notifyIObservers(this, MotorEvent.MOVE_COMPLETE);
			}
		}
	}

	/**
	 * Returns the current position
	 *
	 * @return the current position
	 */
	@Override
	public double getPosition() {
		return currentPosition + ( randomPositionVariation ? (random.nextGaussian()-0.5)*randomPositionVariationAmount : 0.);
	}

	/**
	 * Set the speed
	 *
	 * @param stepsPerSecond
	 *            the new speed
	 * @throws MotorException
	 */
	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		speed = stepsPerSecond;
	}

	/**
	 * Return the current speed.
	 *
	 * @return the current speed
	 * @throws MotorException
	 */
	@Override
	public double getSpeed() throws MotorException {
		return speed;
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
		this.timeToVelocity = timeToVelocity;
	}

	@Override
	public double getTimeToVelocity() throws MotorException {
		return timeToVelocity;
	}

	/**
	 * Stop the motor
	 *
	 * @throws MotorException
	 */
	@Override
	public void stop() throws MotorException {
		simulatedMoveRequired = false;
		status = MotorStatus.READY;
		motorMoving = false;
	}

	/**
	 * Should do an immediate stop but actually does the same as stop() *
	 *
	 * @throws MotorException
	 */
	@Override
	public void panicStop() throws MotorException {
		simulatedMoveRequired = false;
		status = MotorStatus.READY;
	}

	/**
	 * Returns the current status.
	 *
	 * @return the current status
	 */
	@Override
	public MotorStatus getStatus() {
		return status;
	}

	public void setStatus(MotorStatus status) {
		this.status = status;
	}

	public int getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}

	/**
	 * Returns whether or not the motor is moving.
	 *
	 * @return true if the motor is moving
	 */
	@Override
	public boolean isMoving() {
		return motorMoving;
	}

	/**
	 * Does the position updating which simulates a move.
	 */

	@Override
	@SuppressWarnings("squid:S2189") // Otherwise SonarLint complains about lack of end condition
	public synchronized void run() {
		int i = 0;

		while (true) {
			// Wait until the simulatedMoveRequired flag is set to true
			try {
				waiting = true;
				do {
					wait();
				} while (!simulatedMoveRequired);
			} catch (Exception ex) {
				logger.debug("Error while waiting for dummy motor move", ex);
			}

			// If limitCount is 0 set the status to BUSY a limit may still
			// be set from last move.
			if (limitCount == 0)
				status = MotorStatus.BUSY;

			for (i = 0; i < numberOfIncrements; i++) {
				if (simulatedMoveRequired) {

					logger.trace("Moving {}, status {}, position {}", getName(), status, currentPosition);
					// When a limit is randomly set, limitCount is set to 4.
					// It is decremented here and when it reaches 0 the limit
					// is cleared. This means that at the beginning of a move
					// away from a limit the limit will still show. After 4
					// increments it will clear.
					if (status == MotorStatus.UPPER_LIMIT) {
						if (positionIncrement < 0)
							limitCount--;
						else
							break;
					} else if (status == MotorStatus.LOWER_LIMIT) {
						if (positionIncrement > 0)
							limitCount--;
						else
							break;
					} else {
						// If not at a limit see whether a random limit should be set,
						// upper or lower is set according to the direction of movement.
						if (randomlyProduceLimits && Math.abs(random.nextGaussian()) > randomLimitTriggerLevel) {
							limitCount = 4;
							if (positionIncrement > 0)
								status = MotorStatus.UPPER_LIMIT;
							else if (positionIncrement < 0)
								status = MotorStatus.LOWER_LIMIT;
							break;
						}
					}

					// Clear limit
					if (limitCount == 0)
						status = MotorStatus.BUSY;

					// Wait for the incrementalSleepTime
					try {
						logger.trace("DummyMotor {} incremental wait starting", getName());
						wait(incrementalSleepTime);
						logger.trace("DummyMotor {} incremental wait over", getName());
					} catch (InterruptedException ex) {
						logger.error("DummyMotor {} InterruptedException in incremental wait", getName());
					}

					// Increment the position
					currentPosition += positionIncrement;
					logger.trace("DummyMotor {} position is now {}", getName(), currentPosition);
				}

				// This is the else for the if (simulatedMoveRequired). This flag
				// can be set to false by the stop() method which should cause the
				// move to be abandoned hence the break.
				else {
					status = MotorStatus.READY;
					break;
				}
			}

			// If the move has been completed need to adjust the final position
			// to what it should be (may be slightly wrong due to rounding).
			if (i == numberOfIncrements)
				currentPosition = targetPosition;

			savePosition(getName());

			// If the status is still BUSY the move was completed without a
			// limit flag being set and so status should be set to READY.
			if (status == MotorStatus.BUSY)
				status = MotorStatus.READY;

			logger.debug("Dummy motor {} finished moving at position {}; status now {}",
					this.getName(), currentPosition, status);

			// Switch off the moving flags
			simulatedMoveRequired = false;
			motorMoving = false;

			// If this was a homing move, and it succeeded, then mark the motor as
			// homed. NB status = READY is not enough to say that the move succeeded
			// as this will happen when a move is stopped.
			if (currentPosition == targetPosition && homing) {
				homed = true;
				homing = false;
			}
			notifyIObservers(this, MotorEvent.MOVE_COMPLETE);

		} // End of while(true) loop
	}

	/**
	 * Returns whether the motor will randomly produce exceptions. Stupid name of method forced by the conventions.
	 *
	 * @return whether the motor will randomly produce exceptions
	 */
	public boolean isRandomlyProduceExceptions() {
		return randomlyProduceExceptions;
	}

	/**
	 * Sets the randomlyProduceExceptions flag.
	 *
	 * @param randomlyProduceExceptions
	 */
	public void setRandomlyProduceExceptions(boolean randomlyProduceExceptions) {
		this.randomlyProduceExceptions = randomlyProduceExceptions;
	}

	/**
	 * Returns whether or not the motor is homeable. For real motors this is a fixed property however DummyMotor allows
	 * it to be set in XML.
	 *
	 * @return true if motor is homeable
	 */
	@Override
	public boolean isHomeable() {
		return isHomeable;
	}

	/**
	 * Tells the motor to home itself.
	 *
	 * @throws MotorException
	 */
	@Override
	public void home() throws MotorException {
		// homing = true;
		// moveTo(0.0);
		homed = true;
	}

	/**
	 * Returns whether or not the motor has been homed.
	 *
	 * @return true if motor is already homed
	 */
	@Override
	public boolean isHomed() {
		return homed;
	}

	/**
	 * Gets the value of the trigger level for randomly produced expections.
	 *
	 * @return the trigger level
	 */
	public double getRandomExceptionTriggerLevel() {
		return randomExceptionTriggerLevel;
	}

	/**
	 * Sets the trigger level for randomly produced exceptions. The value is used in the same way as the
	 * randomLimitTriggerLevel. See setRandomLimitTriggerLevel() for an explanation.
	 *
	 * @param randomExceptionTriggerLevel
	 *            the new value
	 */
	public void setRandomExceptionTriggerLevel(double randomExceptionTriggerLevel) {
		this.randomExceptionTriggerLevel = randomExceptionTriggerLevel;
	}

	/**
	 * Gets the trigger level for randomly produced limits.
	 *
	 * @return the trigger level for randomly produced limits
	 */
	public double getRandomLimitTriggerLevel() {
		return randomLimitTriggerLevel;
	}

	/**
	 * Sets the trigger level for randomly produced limits. The random number generator is used to generate a Gaussian
	 * distribution with centre 0.0 and sigma 1.0. A random limit is triggered if abs(random number) is greater than
	 * trigger level. This means a value of 1.0 will trigger roughly 40% of the time, 2.0 roughly 5% and 3.0 roughly
	 * 0.3%. The default value of 10.0 has a negligable chance of triggering an event.
	 *
	 * @param randomLimitTriggerLevel
	 */
	public void setRandomLimitTriggerLevel(double randomLimitTriggerLevel) {
		this.randomLimitTriggerLevel = randomLimitTriggerLevel;
	}

	/**
	 * Gets the value of the randomlyProduceLimits flag. Stupid name because of conventions.
	 *
	 * @return true if motor produces ramndom limits
	 */
	public boolean isRandomlyProduceLimits() {
		return randomlyProduceLimits;
	}

	/**
	 * Sets randomlyProduceLimits flag.
	 *
	 * @param randomlyProduceLimits
	 */
	public void setRandomlyProduceLimits(boolean randomlyProduceLimits) {
		this.randomlyProduceLimits = randomlyProduceLimits;
	}

	/**
	 * Sets the isHomeable flag - real motors would not allow this of course.
	 *
	 * @param isHomeable
	 */
	public void setHomeable(boolean isHomeable) {
		this.isHomeable = isHomeable;
	}

	public boolean isRandomPositionVariation() {
		return randomPositionVariation;
	}

	public void setRandomPositionVariation(boolean randomPositionVariation) {
		this.randomPositionVariation = randomPositionVariation;
	}

	public double getRandomPositionVariationAmount() {
		return randomPositionVariationAmount;
	}

	public void setRandomPositionVariationAmount(double randomPositionVariationAmount) {
		this.randomPositionVariationAmount = randomPositionVariationAmount;
	}

	@Override
	public double getUserOffset() throws MotorException {
		return 0.;
	}

	@Override
	public String getUnitString() throws MotorException {
		return unitString;
	}

	public void setUnitString(String unitString) {
		this.unitString = unitString;
	}

}
