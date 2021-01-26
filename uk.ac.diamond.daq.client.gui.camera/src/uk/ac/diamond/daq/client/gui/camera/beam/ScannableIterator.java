/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.gda.client.event.ScannableStateEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.MotorUtils;

/**
 * Allows to drive in a {@link ListIterator} fashion an {@link IScannableMotor}
 * dividing the motor range in a defined number of steps, or, equivalently in
 * steps+1 number of points.
 * <p>
 * The number of steps can be modified at any time calling
 * {@link #setSteps(int)}
 * </p>
 *
 * <p>
 * When the motor moves the class publishes a {@link ScannableStateEvent}
 * containing information about the read position.
 * </p>
 *
 * @author Maurizio Nagni
 */
public class ScannableIterator {
	/**
	 * The motor to drive
	 */
	private final IScannableMotor motor;
	/**
	 * The {@code LineIterator} containing the motion points
	 */
	private ListIterator<Double> stepsIterator;
	/**
	 * The steps dividing the motor range
	 */
	private int steps = 2;

	private final Logger logger = LoggerFactory.getLogger(ScannableIterator.class);

	/**
	 * Creates an instance based on a given motor
	 *
	 * @param motor the motor to drive
	 */
	public ScannableIterator(IScannableMotor motor) {
		this.motor = motor;
	}

	/**
	 * Reads and return the motor position
	 *
	 * @return the motor position
	 * @throws GDAClientException if cannot read the motor position
	 */
	public double getDriverPosition() throws GDAClientException {
		try {
			return (double) motor.getPosition();
		} catch (DeviceException e) {
			throw new GDAClientException("Cannot read motor position", e);
		}
	}

	/**
	 * Utility to now the step resolution as the motor range divided by the actual
	 * steps
	 *
	 * @return the step resolution
	 */
	public double getActualResolution() {
		try {
			double lowerLimit = motor.getLowerInnerLimit();
			double upperLimit = motor.getUpperInnerLimit();
			return Math.abs(upperLimit - lowerLimit) / this.steps;
		} catch (DeviceException e) {
			logger.error("TODO put description of error here", e);
		}
		return 0;
	}

	/**
	 * Returns {@code true} if this list iterator has more elements when traversing
	 * the list in the forward direction. (In other words, returns {@code true} if
	 * {@link #next} would return an element rather than throwing an exception.)
	 *
	 * @return {@code true} if the list iterator has more elements when traversing
	 *         the list in the forward direction
	 */
	public boolean hasNext() {
		return hasIteration(ListIterator::hasNext);
	}

	/**
	 * Returns the next element in the list and advances the cursor position.
	 *
	 * <p>
	 * This method may be called repeatedly to iterate through the list, or intermixed
	 * with calls to {@link #previous} to go back and forth. (Note that alternating
	 * calls to {@code next} and {@code previous} will return the same element
	 * repeatedly.)
	 * Blocks the thread until the movement is complete.
	 * </p>
	 *
	 * @throws NoSuchElementException if the iteration has no next element
	 */
	public void next() {
		move(ListIterator::next);
	}

	/**
	 * Returns {@code true} if this list iterator has more elements when traversing
	 * the list in the reverse direction. (In other words, returns {@code true} if
	 * {@link #previous} would return an element rather than throwing an exception.)
	 *
	 * @return {@code true} if the list iterator has more elements when traversing
	 *         the list in the reverse direction
	 */
	public boolean hasPrevious() {
		return hasIteration(ListIterator::hasPrevious);
	}

	/**
	 * Returns the previous element in the list and moves the cursor position
	 * backwards.
	 * <p>
	 * This method may be called repeatedly to iterate through the list
	 * backwards, or intermixed with calls to {@link #next} to go back and forth.
	 * (Note that alternating calls to {@code next} and {@code previous} will return
	 * the same element repeatedly.)
	 * Blocks the thread until the movement is complete.
	 * </p>
	 *
	 * @throws NoSuchElementException if the iteration has no previous element
	 */
	public void previous() {
		move(ListIterator::previous);
	}

	private void move(Function<? super ListIterator<Double>, ? extends Double> iteration) {
		Double newPosition = Optional.ofNullable(stepsIterator)
				.map(iteration)
				.orElse(null);
		if (newPosition != null) {
			getMotorUtils().moveMotorSynchronously(motor, newPosition.doubleValue());
		}
	}

	private boolean hasIteration(Function<? super ListIterator<Double>, ? extends Boolean> iteration) {
		return Optional.ofNullable(stepsIterator)
				.map(iteration)
				.map(Boolean::booleanValue)
				.orElseGet(() -> false);
	}

	/**
	 * The name of the motor
	 *
	 * @return the motor name
	 */
	public String getScannableName() {
		return motor.getName();
	}

	/**
	 * Allows to change the iterator number of steps. Calling this method reset the
	 * iterator index.
	 *
	 * @param steps the number of the range divisions
	 * @throws DeviceException if the new iterator cannot me initialised
	 */
	public void setSteps(int steps) throws DeviceException {
		if (steps < 2) {
			return;
		}
		this.steps = steps;
		initialise();
	}

	private void initialise() throws DeviceException {
		double lowerLimit = motor.getLowerInnerLimit();
		double upperLimit = motor.getUpperInnerLimit();
		double step = Math.abs(upperLimit - lowerLimit) / this.steps;
		List<Double> stepsList = new ArrayList<>();
		IntStream.rangeClosed(0, this.steps).forEachOrdered(i -> stepsList.add(lowerLimit + i * step));
		stepsIterator = stepsList.listIterator();
	}

	private MotorUtils getMotorUtils() {
		return SpringApplicationContextFacade.getBean(MotorUtils.class);
	}
}
