/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import static gda.device.MotorProperties.MotorEvent.MOVE_COMPLETE;
import static gda.device.MotorStatus.BUSY;
import static gda.device.MotorStatus.FAULT;
import static gda.device.MotorStatus.LOWER_LIMIT;
import static gda.device.MotorStatus.READY;
import static gda.device.MotorStatus.UPPER_LIMIT;
import static java.lang.Math.abs;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * A dummy version of {@link Motor}.
 * <p>
 * While motor is not moving, no background thread is required.
 */
public class ThreadlessDummyMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(ThreadlessDummyMotor.class);

	/** Random number generator to create noise - can be seeded for testing */
	private static final Random random = new Random();

	/** The default time in ms between position or status updates */
	private static final long DEFAULT_UPDATE_INTERVAL_MS = 500;

	/** The time (in ms) between updates for this motor - default {@value #DEFAULT_UPDATE_INTERVAL_MS} */
	private long updateInterval = DEFAULT_UPDATE_INTERVAL_MS;

	/** Limit logging messages when using a high update rate */
	private transient RateLimiter limiter = RateLimiter.create(2);

	/** The future of the background update task */
	private transient ScheduledFuture<?> updateProcess;

	/** The current position of this motor */
	private volatile double position;
	/** The requested position of this motor */
	private volatile double targetPosition;

	/** Holder for the status of this motor */
	private AtomicReference<MotorStatus> status = new AtomicReference<>(READY);

	/** The amount this motor moves on each update - the speed is a combination of this and the update interval */
	private double stepSize = 0.1;

	/** The deadband within which two positions are declared the same */
	private double tolerance = 0.001;

	/** The maximum speed at which this motor can move */
	private double maxSpeed = Double.MAX_VALUE;

	/** Whether this motor can be homed */
	private boolean homeable;

	/** The home position of this motor */
	private double home;

	/** The units this motor uses */
	private String unitString;

	/** The lower limit of this motor's range */
	private double lowerHardLimit = -Double.MAX_VALUE;
	/** The upper limit of this motor's range */
	private double upperHardLimit = Double.MAX_VALUE;

	/** Whether this motor produces random errors when requesting moves - for testing */
	private boolean randomlyProduceExceptions;
	/**
	 * Likelihood of an exception being thrown when requesting a move.
	 * 1 - every move
	 * 0 - never
	 * No exceptions are thrown unless {@link #randomlyProduceExceptions} is set.
	 */
	private double randomExceptionLevel;
	/**
	 * Likelihood of a limit exception being thrown when requesting a move.
	 * 1 - every move
	 * 0 - never
	 * No exceptions are thrown unless {@link #randomlyProduceExceptions} is set.
	 */
	private double randomLimitLevel;

	/** Whether the reported position should include random variation */
	private boolean randomPositionVariation;
	/**
	 * The noise in the reported motor position. This only applies if {@link #randomPositionVariation}
	 * is set.
	 */
	private double randomPositionVariationAmount;

	@Override
	public void configure() throws FactoryException {
		shutdown();
		if (getSpeed() < 0 || getSpeed() > maxSpeed) {
			throw new FactoryException(String.format("%s - Speed (%f cannot be above maximum speed (%f)",
					getName(), getSpeed(), maxSpeed));
		} else if (lowerHardLimit >= upperHardLimit) {
			throw new FactoryException(String.format("%s - upperHardLimit (%f) must be greater than lowerHardLimit (%f)",
					getName(), upperHardLimit, lowerHardLimit));
		}
	}

	/** Stop all updates for this motor. This motor will no longer respond to move requests */
	public void shutdown() {
		if (updateProcess != null) {
			updateProcess.cancel(true);
		}
	}

	/**
	 * Update the state of this motor. Move current position towards a target if required and set status.
	 * This is intended to be called by a {@link ScheduledExecutorService}.
	 */
	private void runUpdates() {
		if (limiter.tryAcquire() && logger.isTraceEnabled()) {
			logger.trace("Updating motor {} - position: {}, target: {}, status: {}",
					getName(), position, targetPosition, getStatus());
		}
		updatePosition();
	}

	private void updatePosition() {
		if (isAt(targetPosition)) {
			position = targetPosition; // eg adjust 2.7e-15 to 0
			setStatus(READY);
			notifyIObservers(this, MOVE_COMPLETE);
			shutdown();
		} else {
			moveTowardsTarget();
		}
	}

	private void moveTowardsTarget() {
		double delta = targetPosition - position;
		setStatus(BUSY);
		if (Math.abs(delta) < stepSize) {
			position = targetPosition;
		} else {
			double step = delta > 0 ? stepSize : -stepSize;
			double next = position + step;
			if (next >= upperHardLimit) {
				setStatus(UPPER_LIMIT);
				shutdown();
				position = upperHardLimit;
			} else if (next <= lowerHardLimit) {
				setStatus(LOWER_LIMIT);
				shutdown();
				position = lowerHardLimit;
			} else {
				position += step;
			}
		}
	}

	private boolean isAt(double other) {
		return abs(position - other) < tolerance;
	}

	private void checkRandomErrors() throws MotorException {
		if (randomlyProduceExceptions) {
			if (random.nextDouble() < randomExceptionLevel) {
				throw new MotorException(FAULT, getName() + " - Motor in random error state");
			} else if (random.nextDouble() < randomLimitLevel) {
				throw new MotorException(random.nextBoolean() ? LOWER_LIMIT : UPPER_LIMIT, "Random limit violation");
			}
		}
	}

	@Override
	public void moveBy(double increment) throws MotorException {
		setTarget(position + increment);
	}

	@Override
	public void moveTo(double position) throws MotorException {
		checkRandomErrors();
		setTarget(position);
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		setTarget(direction > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE);
	}

	@Override
	public void setPosition(double steps) {
		this.position = steps;
	}

	@Override
	public double getPosition() {
		return position + (randomPositionVariation ? (random.nextGaussian()-0.5)*randomPositionVariationAmount : 0.0);
	}

	@Override
	public void setSpeed(double speed) {
		if (speed <= 0 || speed > maxSpeed) {
			throw new IllegalArgumentException(getName() + " - Speed must be between 0 and " + maxSpeed);
		}
		this.stepSize = updateInterval * speed / 1000.0;
	}

	@Override
	public double getSpeed() {
		return stepSize * (1000.0 / updateInterval);
	}

	@Override
	public void stop() {
		targetPosition = position;
		shutdown();
	}

	@Override
	public void panicStop() {
		stop();
	}

	@Override
	public MotorStatus getStatus() {
		return status.get();
	}

	@Override
	public boolean isMoving() {
		return getStatus().equals(BUSY);
	}

	/** Set the motor's target position - moves are made in the update methods
	 * @throws MotorException */
	private void setTarget(double target) throws MotorException {
		if (target < getMinPosition() || target > getMaxPosition()) {
			throw new IllegalArgumentException(getName() + " - position " + target + " outside allowed range");
		}
		setStatus(BUSY);
		ensureRunning();
		targetPosition = target;
	}

	private void ensureRunning() {
		if (updateProcess == null || updateProcess.isDone()) {
			updateProcess = Async.scheduleAtFixedRate(
					this::runUpdates,
					updateInterval/2,
					updateInterval,
					MILLISECONDS,
					"%s - PseudoMotor update",
					getName());
		}
	}

	/** Set the status of the motor */
	private void setStatus(MotorStatus status) {
		this.status.set(status);
	}

	/**
	 * Get the tolerance of this motor - two positions within this distance of each other
	 * are considered equal.
	 */
	public double getTolerance() {
		return tolerance;
	}

	/**
	 * Set the tolerance of this motor - two positions within this distance of each other
	 * are considered equal.
	 * <p>
	 * Tolerance must be > 0;
	 */
	public void setTolerance(double tolerance) {
		if (tolerance <= 0) {
			throw new IllegalArgumentException("Tolerance must be >0");
		}
		this.tolerance = tolerance;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * Set the time (in ms) between position and status updates. This cannot be changed
	 * while the motor is running.
	 * @param updateInterval Time between position/status updates (in ms).
	 */
	public void setUpdateInterval(long updateInterval) {
		if (updateProcess != null && !updateProcess.isDone()) {
			throw new IllegalStateException("Update interval cannot be changed while motor is moving");
		}
		if (updateInterval <= 0) {
			throw new IllegalArgumentException("Update interval must be >0");
		}
		double previousSpeed = getSpeed();
		this.updateInterval = updateInterval;
		setSpeed(previousSpeed);
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		if (maxSpeed <= 0) {
			throw new IllegalArgumentException(getName() + " - Maximum speed must be >0");
		}
		if (getSpeed() > maxSpeed) {
			logger.info("{} - current speed ({}) is higher than new max speed ({}). Setting speed to {}",
					getName(), getSpeed(), maxSpeed, maxSpeed);
		}
		this.maxSpeed = maxSpeed;
	}

	@Override
	public boolean isHomeable() {
		return homeable;
	}

	public void setHomeable(boolean isHomeable) {
		homeable = isHomeable;
	}

	@Override
	public void home() throws MotorException {
		if (isHomeable()) {
			setTarget(home);
		} else {
			throw new UnsupportedOperationException("This motor is not homeable");
		}
	}

	@Override
	public boolean isHomed() {
		return isAt(home) && getStatus() == READY;
	}

	public boolean isRandomlyProduceExceptions() {
		return randomlyProduceExceptions;
	}

	public void setRandomlyProduceExceptions(boolean randomlyProduceExceptions) {
		this.randomlyProduceExceptions = randomlyProduceExceptions;
	}

	public double getRandomExceptionTriggerLevel() {
		return randomExceptionLevel;
	}

	public void setRandomExceptionTriggerLevel(double randomExceptionTriggerLevel) {
		randomExceptionLevel = randomExceptionTriggerLevel;
	}

	public double getRandomLimitTriggerLevel() {
		return randomLimitLevel;
	}

	public void setRandomLimitTriggerLevel(double randomLimitTriggerLevel) {
		randomLimitLevel = randomLimitTriggerLevel;
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
	public String getUnitString() {
		return unitString;
	}

	public void setUnitString(String unitString) {
		this.unitString = unitString;
	}

	public double getLowerHardLimit() {
		return lowerHardLimit;
	}

	public void setLowerHardLimit(double lowerHardLimit) {
		this.lowerHardLimit = lowerHardLimit;
	}

	public double getUpperHardLimit() {
		return upperHardLimit;
	}

	public void setUpperHardLimit(double upperHardLimit) {
		this.upperHardLimit = upperHardLimit;
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) {
		logger.info("{} - this motor does not support timeToVelocity", getName());
	}

	public static void seedRandomVariation(long seed) {
		random.setSeed(seed);
	}
}
