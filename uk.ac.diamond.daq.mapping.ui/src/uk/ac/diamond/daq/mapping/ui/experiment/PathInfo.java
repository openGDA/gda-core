/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple class to hold information about a 2D mapping scan path for plotting/metadata
 */
public final class PathInfo {
	private static final String POINT_COUNT_FORMAT = "%,d";
	private static final String DOUBLE_FORMAT = "%.4g";

	/*
	 * Number of 2D points in the scan
	 */
	private final int pointCount;

	/*
	 * Smallest change in x position between any two points
	 */
	private final double smallestXStep;

	/*
	 * Smallest change in y position between any two points
	 */
	private final double smallestYStep;

	/*
	 * Smallest change in position between any two points
	 */
	private final double smallestAbsStep;

	/*
	 * Positions in x axis of all coordinates
	 */
	private final Collection<Double> xCoordinates;

	/*
	 * Positions in y axis of all coordinates
	 */
	private final Collection<Double> yCoordinates;

	public PathInfo(
			final int pointCount,
			final double smallestXStep,
			final double smallestYStep,
			final double smallestAbsStep,
			final Collection<Double> xCoordinates,
			final Collection<Double> yCoordinates) {
		super();
		this.pointCount = pointCount;
		this.smallestXStep = smallestXStep;
		this.smallestYStep = smallestYStep;
		this.smallestAbsStep = smallestAbsStep;
		this.xCoordinates = Collections.unmodifiableCollection(xCoordinates);
		this.yCoordinates = Collections.unmodifiableCollection(yCoordinates);
	}

	public int getPointCount() {
		return pointCount;
	}

	public double getSmallestXStep() {
		return smallestXStep;
	}

	public double getSmallestYStep() {
		return smallestYStep;
	}

	public double getSmallestAbsStep() {
		return smallestAbsStep;
	}

	public String getFormattedPointCount() {
		return String.format(POINT_COUNT_FORMAT, pointCount);
	}

	public String getFormattedSmallestXStep() {
		return formatDouble(smallestXStep);
	}

	public String getFormattedSmallestYStep() {
		return formatDouble(smallestYStep);
	}

	public String getFormattedSmallestAbsStep() {
		return formatDouble(smallestAbsStep);
	}

	private String formatDouble(double value) {
		if (value == Double.MAX_VALUE) {
			return "N/A";
		}
		return String.format(DOUBLE_FORMAT, value);
	}

	public double[] getXCoordinates() {
		return asArray(xCoordinates);
	}

	public double[] getYCoordinates() {
		return asArray(yCoordinates);
	}

	private double[] asArray(Collection<Double> coordinates) {
		return coordinates.stream()
				.mapToDouble(Double::doubleValue)
				.toArray();
	}
}