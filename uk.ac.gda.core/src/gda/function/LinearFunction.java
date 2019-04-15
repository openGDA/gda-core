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

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.QuantityFactory;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * LinearFunction (yValue = xValue * slope + intercept)
 */
@ServiceInterface(ILinearFunction.class)
public class LinearFunction extends FindableFunction implements ILinearFunction {
	private static final Logger logger = LoggerFactory.getLogger(LinearFunction.class);

	private Amount<? extends Quantity> slope;

	private Amount<? extends Quantity> slopeNumerator;

	private Amount<? extends Quantity> slopeDenominator;

	private Amount<? extends Quantity> intercept;

	private String slopeDivisor;

	private String slopeDividend;

	private String interception;

	/**
	 * Constructor with no arguments. (Must exist or creation by Finder fails.)
	 */
	public LinearFunction() {
		slope = Amount.valueOf(0.0, Dimensionless.UNIT);
		intercept = Amount.valueOf(0.0, Dimensionless.UNIT);
	}

	/**
	 * Another constructor.
	 *
	 * @param slope
	 *            the slope
	 * @param intercept
	 *            the intercept
	 */
	public LinearFunction(Amount<? extends Quantity> slope, Amount<? extends Quantity> intercept) {
		this.slope = slope;
		this.intercept = intercept;

		logger.debug("LinearFunction: slope is {}", this.slope);
		logger.debug("LinearFunction: intercept is {}", this.intercept);
	}

	/**
	 * @return Returns the interception.
	 */
	@Override
	public String getInterception() {
		return interception;
	}

	/**
	 * @param interception
	 *            The interception to set.
	 */
	@Override
	public void setInterception(String interception) {
		this.interception = interception;
		intercept = QuantityFactory.createFromString(interception);
	}

	/**
	 * @return Returns the slopeDividend.
	 */
	@Override
	public String getSlopeDividend() {
		return slopeDividend;
	}

	/**
	 * @param slopeDividend
	 *            The slopeDividend to set.
	 */
	@Override
	public void setSlopeDividend(String slopeDividend) {
		slopeNumerator = QuantityFactory.createFromString(slopeDividend);
		this.slopeDividend = slopeDividend;
	}

	/**
	 * @return Returns the slopeDivisor.
	 */
	@Override
	public String getSlopeDivisor() {
		return slopeDivisor;
	}

	/**
	 * @param slopeDivisor
	 *            The slopeDivisor to set.
	 */
	@Override
	public void setSlopeDivisor(String slopeDivisor) {
		slopeDenominator = QuantityFactory.createFromString(slopeDivisor);
		this.slopeDivisor = slopeDivisor;
	}

	@Override
	public Amount<? extends Quantity> apply(Amount<? extends Quantity> xValue) {
		slope = slopeNumerator.divide(slopeDenominator);

		logger.debug("LinearFunction.evaluate: xValue is {}", xValue);
		logger.debug("LinearFunction.evaluate: slope is {}", slope);
		logger.debug("LinearFunction.evaluate: intercept is {}", intercept);

		Amount<? extends Quantity> interim = xValue.times(slope).plus(intercept);

		logger.debug("LinearFunction.evaluate: interim is {}", interim);

		// The return value required from this method must have the same
		// subclass as the intercept field. Interim, has the correct
		// numerical value and the correct units. However, it is a Quantity
		// and the class of its units field is Unit. DOFs all expect to be
		// given Quantities of a particular subclass and with units of a
		// particular subclass of Unit.
		return interim;
	}

	@Override
	public String getAsString() {
		return toString();
	}

	@Override
	public String toString() {
		return "LinearFunction [interception=" + interception + ", slopeDividend=" + slopeDividend + ", slopeDivisor=" + slopeDivisor
				+ ", slope=" + slope + ", slopeNumerator=" + slopeNumerator + ", slopeDenominator=" + slopeDenominator + ", intercept=" + intercept + "]";
	}
}
