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

package gda.function;

import org.jscience.physics.quantities.Quantity;

import gda.factory.FactoryException;
import gda.util.QuantityFactory;

/**
 * LinearFunction (yValue = xValue * slope + intercept)
 */

public class LinearFunction extends Function {
	private Quantity slope;

	private Quantity slopeNumerator;

	private Quantity slopeDenominator;

	private Quantity intercept;

	private String slopeDivisor;

	private String slopeDividend;

	private String interception;

	/**
	 * Constructor with no arguments. (Must exist or creation by Finder fails.)
	 */
	public LinearFunction() {
		slope = Quantity.valueOf("0.0");
		intercept = Quantity.valueOf("0.0");
	}

	/**
	 * Another constructor.
	 *
	 * @param slope
	 *            the slope
	 * @param intercept
	 *            the intercept
	 */
	public LinearFunction(Quantity slope, Quantity intercept) {
		this.slope = slope;
		this.intercept = intercept;

		logger.debug("LinearFunction: slope is " + this.slope);
		logger.debug("LinearFunction: intercept is " + this.intercept);
	}

	/**
	 * @return Returns the interception.
	 */
	public String getInterception() {
		return interception;
	}

	/**
	 * @param interception
	 *            The interception to set.
	 */
	public void setInterception(String interception) {
		this.interception = interception;
		intercept = QuantityFactory.createFromString(interception);
	}

	/**
	 * @return Returns the slopeDividend.
	 */
	public String getSlopeDividend() {
		return slopeDividend;
	}

	/**
	 * @param slopeDividend
	 *            The slopeDividend to set.
	 */
	public void setSlopeDividend(String slopeDividend) {
		slopeNumerator = QuantityFactory.createFromString(slopeDividend);
		this.slopeDividend = slopeDividend;
	}

	/**
	 * @return Returns the slopeDivisor.
	 */
	public String getSlopeDivisor() {
		return slopeDivisor;
	}

	/**
	 * @param slopeDivisor
	 *            The slopeDivisor to set.
	 */
	public void setSlopeDivisor(String slopeDivisor) {
		slopeDenominator = QuantityFactory.createFromString(slopeDivisor);
		this.slopeDivisor = slopeDivisor;
	}

	@Override
	public Quantity evaluate(Quantity xValue) {
		// slope = slopeNumerator.divide(slopeDenominator);
		slope = slopeNumerator.times(slopeDenominator.reciprocal());

		logger.debug("LinearFunction.evaluate: xValue is " + xValue.toString());
		logger.debug("LinearFunction.evaluate: slope is " + slope.toString());
		logger.debug("LinearFunction.evaluate: intercept is " + intercept.toString());

		Quantity interim = xValue.times(slope).plus(intercept);

		logger.debug("LinearFunction.evaluate: interim is " + interim.toString());

		// The return value required from this method must have the same
		// subclass as the intercept field. Interim, has the correct
		// numerical value and the correct units. However, it is a Quantity
		// and the class of its units field is Unit. DOFs all expect to be
		// given Quantities of a particular subclass and with units of a
		// particular subclass of Unit.
		return interim; // QuantityFactory.createFromString(interim.toString());
	}

	@Override
	public void configure() throws FactoryException {
	}
}
