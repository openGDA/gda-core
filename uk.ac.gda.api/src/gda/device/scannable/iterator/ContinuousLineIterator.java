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

package gda.device.scannable.iterator;

import java.util.ListIterator;

/**
 * Iterates continuously between two values with a variable step length.
 * <p>
 * This class does a single step walks forth and back between a fixed range. The step is defined by the
 * {@link #actualResolution()} and when the class is instantiated is equal to the {@code resolution} parameter in the
 * constructor. {@link #scaleResolution(long)} allows to scale the initial {@code resolution} of an integer number.
 * </p>
 * <p>
 * When a step goes over the range extremes, in a way or another, the class inverts the direction and set the position
 * to the closest range extreme.
 * </p>
 * <p>
 * While the range extremes and the minimal step are fixed when the class is instantiated {@link #scaleResolution(long)}
 * allows to change the step used to walk over the line.
 * </p>
 *
 * @author Maurizio Nagni
 */
class ContinuousLineIterator implements ListIterator<Double> {
	private final double minimum;
	private final double maximum;
	private final double resolution;
	private long resolutionScale = 1;
	private final double start;
	private double lastPosition;
	private boolean directionChanged = true;

	/**
	 * Instantiates this class so that {@link #next()} allows to walk from the {@code minimum} value to the
	 * {@code maximum} with a defined {@code resolution}.
	 *
	 * @param miminum
	 *            the smallest value on the line
	 * @param maximum
	 *            the larger value on the line
	 * @param resolution
	 *            the smallest step to use during any iteration. The value is unsigned.
	 * @param start
	 *            where the iteration starts. If outside {@code [minimum, maximum]} range it is equal to {@code minimum}
	 */
	public ContinuousLineIterator(double miminum, double maximum, double resolution, double start) {
		super();
		this.minimum = miminum;
		this.maximum = maximum;
		this.resolution = Double.isNaN(resolution) ? 0.00005 : Math.abs(resolution);
		this.start = Double.isNaN(start) || start < this.minimum || start > this.maximum ? this.minimum : start;
		lastPosition = this.start;
	}

	/**
	 * Returns {@code true} if has more element forward the actual traversing direction, {@code false} otherwise
	 */
	@Override
	public boolean hasNext() {
		double actualStep = actualResolution();
		return ((lastPosition + actualStep <= maximum) && (lastPosition + actualStep >= minimum));
	}

	/**
	 * If {@link #hasNext()} is {@code true}, returns the next element in on the line. The element is calculate summing
	 * the {@link #actualResolution()} to the last position. If {@link #hasNext()} is {@code false}, inverts the actual
	 * walking direction and returns the {@code minimum} value, if the direction was backward, or the {@code maximum} if
	 * the direction was forward.
	 */
	@Override
	public Double next() {
		lastPosition = lastPosition + actualResolution();
		return validateStep();
	}

	/**
	 * If {@link #hasPrevious()} is {@code true}, returns the previous element in on the line. The element is calculate
	 * subtracting the {@link #actualResolution()} from the last position. If {@link #hasPrevious()} is {@code false},
	 * inverts the actual walking direction and returns the {@code maximum} value, if the direction was backward, or the
	 * {@code minimum} if the direction was forward.
	 */
	@Override
	public Double previous() {
		lastPosition = lastPosition - actualResolution();
		return validateStep();
	}

	/**
	 * Sets the multiplication factor for the next step. If the {@code resolutionScale} sign is opposite to @link
	 * ContinuousLineIterator#actualResolution() the iteration direction is inverted.
	 *
	 * @param resolutionScale
	 */
	public void scaleResolution(long resolutionScale) {
		if (Math.abs(resolutionScale) > 0) {
			this.resolutionScale = resolutionScale;
		}
	}

	/**
	 * Returns the actual iteration step calculated as the initial {@code resolution} multiplied by the
	 * {@code resolutionScale}. The sign is given by the actual direction.
	 *
	 * @return the actual step. A positive number implies a forward direction, backward otherwise.
	 */
	public double actualResolution() {
		return resolutionScale * resolution;
	}

	/**
	 * Indicates if the last {@link #next()} or @link {@link #previous()} call inverted the iteration direction.
	 *
	 * @return {@code true} if the direction changed, {@code false} otherwise
	 */
	public boolean getDirectionChanged() {
		return directionChanged;
	}

	/**
	 * Returns {@code true} if has more element backward the actual traversing direction, {@code false otherwise}
	 */
	@Override
	public boolean hasPrevious() {
		double actualStep = actualResolution();
		return ((lastPosition - actualStep <= maximum) && (lastPosition - actualStep >= minimum));
	}

	/**
	 * Not implemented
	 */
	@Override
	public void add(Double e) {
		// Not used
	}

	/**
	 * Not implemented
	 */
	@Override
	public int nextIndex() {
		return 0;
	}

	/**
	 * Not implemented
	 */
	@Override
	public int previousIndex() {
		return 0;
	}

	/**
	 * Not implemented
	 */
	@Override
	public void remove() {
		// Not used
	}

	/**
	 * Allows to force the internal last position, only if the value is not outside the line ranges.
	 */
	@Override
	public void set(Double e) {
		if (minimum <= lastPosition && maximum >= lastPosition) {
			lastPosition = e;
		}
	}

	private Double validateStep() {
		directionChanged = false;
		if (minimum <= lastPosition && maximum >= lastPosition) {
			return lastPosition;
		}
		// the step goes outside, consequently invert the iteration direction
		resolutionScale = resolutionScale > 0 ? -1 * resolutionScale : resolutionScale;
		directionChanged = true;
		// was iterating backward?
		if (minimum > lastPosition) {
			lastPosition = minimum;
		} else {
			// then was iterating forward
			lastPosition = maximum;
		}
		return lastPosition;
	}
}
