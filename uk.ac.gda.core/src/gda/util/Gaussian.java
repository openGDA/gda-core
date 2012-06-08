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

package gda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Gaussian curve by its three parameters. Only THREEPOINT works right now.
 */
public class Gaussian {
	private static final Logger logger = LoggerFactory.getLogger(Gaussian.class);

	private double position = 0.0;
	private double width = 0.0;
	private double height = 0.0;

	/**
	 * Indicates the negative side of the peak
	 */
	public static final int NEGATIVE = -1;

	/**
	 * Indicates the positive side of the peak
	 */
	public static final int POSITIVE = +1;

	/**
	 * Three point fitting method
	 */
	public static final int THREEPOINT = 0;

	/**
	 * Least squares fitting method
	 */
	public static final int LEASTSQUARES = 1;

	/**
	 * Constructor
	 */
	public Gaussian() {
	}

	/**
	 * Constructor which creates a Gaussian by directly specifying its position, width and height.
	 * 
	 * @param position
	 *            the position of the peak
	 * @param width
	 *            the width (FWHM)
	 * @param height
	 *            the height of the peak
	 */
	public Gaussian(double position, double width, double height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	/**
	 * Constructor which creates a Gaussian by the three point fit method or least squares fit method from sets of x and
	 * y data.
	 * 
	 * @param xvals
	 *            the array of xvalues
	 * @param yvals
	 *            the array of yvalues
	 * @param fitMethod
	 *            indicates which method of fitting to use
	 */
	public Gaussian(double xvals[], double yvals[], int fitMethod) {
		if (fitMethod == THREEPOINT) {
			threePointInit(xvals, yvals);
		} else {
			leastSquaresInit();
		}
	}

	/**
	 * Initializes using least squares fit method.
	 */
	private void leastSquaresInit() {
		position = 0.0;
		width = 0.0;
		height = 0.0;
	}

	/**
	 * Initializes using a simple three point fit method.
	 * 
	 * @param x
	 *            array of three x values
	 * @param y
	 *            array of three y values
	 */
	private void threePointInit(double x[], double y[]) {
		double bigD;
		double bigE;

		bigD = Math.log(y[0]) - Math.log(y[1]);
		bigE = Math.log(y[1]) - Math.log(y[2]);

		position = bigD * (x[2] * x[2] - x[1] * x[1]) - bigE * (x[1] * x[1] - x[0] * x[0]);
		position = position / (bigE * (x[0] - x[1]) - bigD * (x[1] - x[2]));
		position = 0.5 * position;

		width = Math.pow((x[2] - position), 2.0) - Math.pow((x[1] - position), 2.0);
		width = Math.sqrt(Math.abs(width / (2.0 * bigE)));

		height = y[0] / Math.exp(-0.5 * Math.pow(((x[0] - position) / width), 2.0));

	}

	/**
	 * Calculates the y value at a given x value
	 * 
	 * @param xVal
	 *            the x value to be used
	 * @return the calculated y value
	 */
	public double yAtX(double xVal) {
		double yVal;

		yVal = height * Math.exp(-0.5 * Math.pow(((xVal - position) / width), 2.0));

		return (yVal);
	}

	/**
	 * Calculates the derivative at a given x value
	 * 
	 * @param xVal
	 *            the x value to be used
	 * @return the calculated derivative
	 */
	public double derivativeAtX(double xVal) {
		double derivative;

		derivative = yAtX(xVal);

		derivative = derivative * (-(xVal - position)) / (width * width);

		return (derivative);
	}

	/**
	 * Calculates the x value at a given y value
	 * 
	 * @param yVal
	 *            the y value to be used
	 * @param side
	 *            either Gaussian.NEGATIVE or Gaussian.POSITIVE
	 * @return the calculated y value
	 */
	public double xAtY(double yVal, int side) {
		double xVal;

		// side POSITIVE gives the positive x value and vice versa
		xVal = position + width * side * Math.sqrt(2.0 * (Math.log(height) - Math.log(yVal)));

		logger.debug("gaussian xAtY for yVal " + yVal + " returning " + xVal);
		return (xVal);
	}

	/**
	 * Calculates the derivative at a given y value
	 * 
	 * @param yVal
	 *            the y value to be used
	 * @param side
	 * @return the calculated derivative
	 */
	public double derivativeAtY(double yVal, int side) {
		return (derivativeAtX(xAtY(yVal, side)));
	}
}
