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

package gda.plots;

/**
 * A class to represent coordinates on a plot in terms only of the data (i.e. without any knowledge of how the plotting
 * is done and internal plot coordinates and that kind of thing). Exists only to simplify future (e.g. 3D) developments
 */
public class SimpleDataCoordinate {
	private double x;

	private double y;
	private double emForXAxis = Double.POSITIVE_INFINITY;
	private double emForYAxis= Double.POSITIVE_INFINITY;
	private double seeForXAxis= Double.POSITIVE_INFINITY;
	private double seeForYAxis= Double.POSITIVE_INFINITY;

	/**
	 * Constructor.
	 *
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public SimpleDataCoordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	/**
	 * Sets the gradient and intercept values for calculating the dependent X axis
	 * @param emForXAxis
	 * @param seeForXAxis
	 */
	void setDependentXCalibrationValues(double emForXAxis, double seeForXAxis) {
		this.emForXAxis = emForXAxis;
		this.seeForXAxis = seeForXAxis;
	}
	/**
	 *  Sets the gradient and intercept values for calculating the dependent Y axis
	 * @param emForYAxis
	 * @param seeForYAxis
	 */
	void setDependentYCalibrationValues(double emForYAxis, double seeForYAxis) {
		this.emForYAxis = emForYAxis;
		this.seeForYAxis = seeForYAxis;
	}

	/**
	 * Returns the x value.
	 *
	 * @return the x value
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y value.
	 *
	 * @return the y value
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return value of dependent x axis
	 */
	public double getDependentX()
	{
		return (emForXAxis * x + seeForXAxis);
	}
	/**
	 * @return value of dependent Y axis
	 */
	public double getDependentY()
	{
		return (emForYAxis * x + seeForYAxis);
	}

	/**
	 * Returns the coordinates as an array of doubles.
	 *
	 * @return the array of doubles
	 */
	public double[] toArray() {
		double[] values = new double[2];

		values[0] = x;
		values[1] = y;

		return values;
	}
}
