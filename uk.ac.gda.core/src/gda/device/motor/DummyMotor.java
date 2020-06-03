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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.MotorException;
import gda.device.MotorProperties.MotorEvent;
import gda.device.MotorStatus;

/**
 * A Dummy motor class
 */
public class DummyMotor extends MotorBase {

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

	private double maxSpeed = Double.MAX_VALUE;

	private double timeToVelocity = 0.1;

	private double targetPosition;

	private Thread runner;

	private Random random = new Random();

	//dimensionless ( == 1) by default
	private String unitString = "";


	private boolean randomPositionVariation = false;

	private double randomPositionVariationAmount = 0.;

	private double randomLimitTriggerLevel = 10.0;

	private double lowerHardLimit = -Double.MAX_VALUE;

	private double upperHardLimit = Double.MAX_VALUE;


	// If randomlyProduceExceptions is true then moveTo will generate
	// an exception if random.nextGaussian() produces a value greater
	// than randomeExceptionTriggerLevel.
	private boolean randomlyProduceExceptions = false;

	private double randomExceptionTriggerLevel = 10.0;

	// These flags are used to simulate a homing move.
	private boolean isHomeable = false;

	private boolean homed = false;

	private boolean homing = false;

	/**
	 * Directory where motor positions are saved/restored
	 */
	private final String filePath = LocalProperties.get("gda.motordir", ".");

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		runner = new Thread(this::runMotor, getClass().getName());
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
		if (speed == 0.0) {
			speed = getSlowSpeed();
			if (speed == 0.0) {
				speed = 1.0;
			}
		}

		// if limits not set, then set them to something usable during testing
		if (Double.isNaN(minPosition)) {
			this.minPosition = -Double.MAX_VALUE;
		}

		if (Double.isNaN(maxPosition)) {
			this.maxPosition = Double.MAX_VALUE;
		}

		this.isInitialised = true;
		setConfigured(true);
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
		if (status == MotorStatus.BUSY) {
			throw new MotorException(status, "moveTo() aborted because previous move not yet completed");
		}
		if (status == MotorStatus.FAULT) {
			throw new MotorException(status,
					"moveTo() aborted because EPICS Motor is at Fault status. Please check EPICS Screen.");
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
	 * saves motor's current position, the persistence path is fixed by java.properties
	 *
	 * @param name
	 *            of file for position save
	 * @param currentPosition
	 */
	private void savePosition(String name) {
		final double currentPos = getPosition();
		try {
			// work out the file name
			final String filename = Paths.get(filePath, name).toString();
			final File saveFile = new File(filename);

			// check if file exists
			if (!saveFile.exists()) {
				// if not, first test if the motorPositions folder has been created
				final File motorDir = new File(filePath);

				// create motorPositions folder if necessary
				if (!motorDir.exists()) {
					logger.info("Motor positions folder not found. Creating new folder:" + motorDir);
					motorDir.mkdir();
				}

				// then create a new file
				logger.info("Motor positions file for motor " + name + " not found. Creating new file:" + filename);
				saveFile.createNewFile();
			}

			// open and write the file
			final ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile)));
			out.writeDouble(currentPos);
			out.flush();
			out.close();
		} catch (IOException ex) {
			logger.debug("{}: Could not save position {} as {}", getName(), currentPos, name, ex);
		}
	}

	/**
	 * loads motor's current position, the persistence path is fixed by java.properties FIXME perhaps loadPosition
	 * should throw MotorBaseException but may have big impact on code base
	 *
	 * @param name
	 *            persistent filename
	 * @param defaultPosition
	 *            default position if the motor position file does not exist or is empty
	 */
	private void loadPosition(String name, double defaultPosition) {

		if (!checkFilePathExistsOrCreate()) {
			logger.error("Motor Positions folder " + filePath + " does not exist and could not be created.");
			return;
		}

		final String fullName = Paths.get(filePath, name).toString();
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(fullName);
			in = new ObjectInputStream(new BufferedInputStream(fis));
			setPosition(in.readDouble());
			logger.debug("Loaded motor position {} for {}", getPosition(), name);
			in.close();
		} catch (FileNotFoundException fnfe) {
			logger.info("Motor Position File " + fullName + " not found - setting " + name + " position to "
					+ defaultPosition + " and creating new file.");
			setPosition(defaultPosition);
			savePosition(name);
		} catch (EOFException eofe) {
			logger.error("unexpected EOF in Motor Position File '{}' trying to read position as int", fullName, eofe);
			try {
				// have already asserted EOFException so OK to do this
				if (fis.available() > 0) {
					in = new ObjectInputStream(new BufferedInputStream(fis));
					setPosition(in.readInt());
					in.close();
					savePosition(name);
				} else {
					logger.info("Motor Position File empty setting posn to " + defaultPosition);
					setPosition(defaultPosition);
					savePosition(name);
				}
			} catch (IOException ioe) {
				logger.error("IOException in MotorBase.loadPosition", ioe);
			}
		} catch (IOException ioe) {
			logger.error("IOException in MotorBase.loadPosition", ioe);
		}
	}

	private boolean checkFilePathExistsOrCreate() {
		// to avoid later FileNotFoundExceptions
		final File filePathFolder = new File(filePath);
		if (!filePathFolder.exists()) {
			return filePathFolder.mkdirs();
		}

		return true;
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

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
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

	private synchronized void runMotor() {
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

			for (i = 0; i < numberOfIncrements; i++) {
				if (simulatedMoveRequired) {

					logger.trace("Moving {}, status {}, position {}", getName(), status, currentPosition);

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

					if (currentPosition >= upperHardLimit) {
						currentPosition = upperHardLimit;
						status = MotorStatus.UPPER_LIMIT;
						logger.trace("Dummy Motor {} reached hard limit, position is now {}", getName(), currentPosition);
						break;
					}

					if (currentPosition <= lowerHardLimit) {
						currentPosition = lowerHardLimit;
						status = MotorStatus.LOWER_LIMIT;
						logger.trace("Dummy Motor {} reached hard limit, position is now {}", getName(), currentPosition);
						break;
					}
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
			if (i == numberOfIncrements && status == MotorStatus.BUSY) {
				currentPosition = targetPosition;
			}

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
		if (!isHomeable) {
			throw new MotorException(getStatus(), String.format("Motor %s is not homeable", getName()));
		}

		// Set flags before moving: these will be reset by runMotor() when homing is complete
		homed = false;
		homing = true;
		moveTo(0.0);
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
	 * Gets the value of the trigger level for randomly produced exceptions.
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
	 * 0.3%. The default value of 10.0 has a negligible chance of triggering an event.
	 *
	 * @param randomLimitTriggerLevel
	 */
	public void setRandomLimitTriggerLevel(double randomLimitTriggerLevel) {
		this.randomLimitTriggerLevel = randomLimitTriggerLevel;
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

	public double getTargetPosition() {
		return targetPosition;
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

}
