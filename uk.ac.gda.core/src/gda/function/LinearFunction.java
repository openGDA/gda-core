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

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * LinearFunction (yValue = xValue * (slopeDividend / slopeDivisor) + interception)
 */
@ServiceInterface(ILinearFunction.class)
public class LinearFunction extends FindableFunction implements ILinearFunction {
	private static final Logger logger = LoggerFactory.getLogger(LinearFunction.class);

	private Amount<? extends Quantity> slopeDividend;

	private Amount<? extends Quantity> slopeDivisor;

	private Amount<? extends Quantity> interception;

	/**
	 * Constructor with no arguments. (Must exist or creation by Finder fails.)
	 */
	public LinearFunction() {
		slopeDividend = Amount.valueOf(1.0, Unit.ONE);
		slopeDivisor = Amount.valueOf(1.0, Unit.ONE);
		interception = Amount.valueOf(0.0, Unit.ONE);
	}

	/**
	 * Constructor setting all values
	 */
	public LinearFunction(Amount<? extends Quantity> slopeDividend, Amount<? extends Quantity> slopeDivisor, Amount<? extends Quantity> interception) {
		this.slopeDividend = cloneAmount(slopeDividend);
		this.slopeDivisor = cloneAmount(slopeDivisor);
		this.interception = cloneAmount(interception);
		logger.debug("constructor: slopeDividend: {}, slopeDivisor: {}, interception: {}",
				this.slopeDividend, this.slopeDivisor, this.interception);
	}

	/**
	 * @return Returns the interception.
	 */
	@Override
	public Amount<? extends Quantity> getInterception() {
		return cloneAmount(interception);
	}

	/**
	 * @param interception
	 *            The interception to set.
	 */
	@Override
	public void setInterception(Amount<? extends Quantity> interception) {
		this.interception = cloneAmount(interception);
	}

	/**
	 * @return Returns the slopeDividend.
	 */
	@Override
	public Amount<? extends Quantity> getSlopeDividend() {
		return cloneAmount(slopeDividend);
	}

	/**
	 * @param slopeDividend
	 *            The slopeDividend to set.
	 */
	@Override
	public void setSlopeDividend(Amount<? extends Quantity> slopeDividend) {
		this.slopeDividend = cloneAmount(slopeDividend);
	}

	/**
	 * @return Returns the slopeDivisor.
	 */
	@Override
	public Amount<? extends Quantity> getSlopeDivisor() {
		return cloneAmount(slopeDivisor);
	}

	/**
	 * @param slopeDivisor
	 *            The slopeDivisor to set.
	 */
	@Override
	public void setSlopeDivisor(Amount<? extends Quantity> slopeDivisor) {
		this.slopeDivisor = cloneAmount(slopeDivisor);

	}

	@Override
	public Amount<? extends Quantity> apply(Amount<? extends Quantity> xValue) {
		final Amount<? extends Quantity> slope = slopeDividend.divide(slopeDivisor);
		final Amount<? extends Quantity> interim = xValue.times(slope).plus(interception);
		logger.debug("apply(): xValue: {}, slope: {}, interception: {}, interim: {}", xValue, slope, interception, interim);
		// The return value required from this method must have units compatible with those of the interception field.
		return interim;
	}

	private Amount<? extends Quantity> cloneAmount(Amount<? extends Quantity> quantity) {
		return Amount.valueOf(quantity.getEstimatedValue(), quantity.getUnit());
	}

	@Override
	public String getAsString() {
		return toString();
	}

	@Override
	public String toString() {
		return "LinearFunction [slopeDividend=" + slopeDividend + ", slopeDivisor=" + slopeDivisor + ", interception="
				+ interception + "]";
	}
}
