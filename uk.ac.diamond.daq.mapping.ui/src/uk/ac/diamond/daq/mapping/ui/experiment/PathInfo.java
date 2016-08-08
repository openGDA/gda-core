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

import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to hold information about a scan path
 */
class PathInfo {
	int pointCount = 0;
	double smallestXStep = Double.MAX_VALUE;
	double smallestYStep = Double.MAX_VALUE;
	double smallestAbsStep = Double.MAX_VALUE;
	List<Double> xCoordinates = new ArrayList<>();
	List<Double> yCoordinates = new ArrayList<>();

	private String pointCountFormat = "%,d";
	private String doubleFormat = "%.4g";

	String getFormattedPointCount() {
		return String.format(pointCountFormat, pointCount);
	}
	String getFormattedSmallestXStep() {
		return formatDouble(smallestXStep);
	}
	String getFormattedSmallestYStep() {
		return formatDouble(smallestYStep);
	}
	String getFormattedSmallestAbsStep() {
		return formatDouble(smallestAbsStep);
	}
	double[] getXCoordinates() {
		double[] xCoords = new double[xCoordinates.size()];
		for (int index = 0; index < xCoordinates.size(); index++) {
			xCoords[index] = xCoordinates.get(index).doubleValue();
		}
		return xCoords;
	}
	double[] getYCoordinates() {
		double[] yCoords = new double[yCoordinates.size()];
		for (int index = 0; index < yCoordinates.size(); index++) {
			yCoords[index] = yCoordinates.get(index).doubleValue();
		}
		return yCoords;
	}

	private String formatDouble(double value) {
		if (value == Double.MAX_VALUE) {
			return "N/A";
		}
		return String.format(doubleFormat, value);
	}
}