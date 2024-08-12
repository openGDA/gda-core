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

import javax.measure.Quantity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.units.indriya.quantity.Quantities;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * LinearFunction (yValue = xValue * (slopeDividend / slopeDivisor) + interception)
 */
@ServiceInterface(ILinearFunction.class)
public class LinearFunction<T extends Quantity<T>, R extends Quantity<R>> extends FindableFunction<T, R> implements ILinearFunction<T, R> {
	private static final Logger logger = LoggerFactory.getLogger(LinearFunction.class);

	private Quantity<R> slopeDividend;

	private Quantity<T> slopeDivisor;

	private Quantity<R> interception;

	/**
	 * Constructor with no arguments. (Must exist or creation by Finder fails.)
	 */
	public LinearFunction() {
		// nothing to do
	}

	/**
	 * Constructor setting all values
	 */
	public LinearFunction(Quantity<R> slopeDividend, Quantity<T> slopeDivisor, Quantity<R> interception) {
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
	public Quantity<R> getInterception() {
		return cloneAmount(interception);
	}

	/**
	 * @param interception
	 *            The interception to set.
	 */
	@Override
	public void setInterception(Quantity<R> interception) {
		this.interception = cloneAmount(interception);
	}

	/**
	 * @return Returns the slopeDividend.
	 */
	@Override
	public Quantity<R> getSlopeDividend() {
		return cloneAmount(slopeDividend);
	}

	/**
	 * @param slopeDividend
	 *            The slopeDividend to set.
	 */
	@Override
	public void setSlopeDividend(Quantity<R> slopeDividend) {
		this.slopeDividend = cloneAmount(slopeDividend);
	}

	/**
	 * @return Returns the slopeDivisor.
	 */
	@Override
	public Quantity<T> getSlopeDivisor() {
		return cloneAmount(slopeDivisor);
	}

	/**
	 * @param slopeDivisor
	 *            The slopeDivisor to set.
	 */
	@Override
	public void setSlopeDivisor(Quantity<T> slopeDivisor) {
		this.slopeDivisor = cloneAmount(slopeDivisor);
	}

	@Override
	public Quantity<R> apply(Quantity<T> xValue) {
		final double slopeFactor = xValue.divide(slopeDivisor).getValue().doubleValue();
		final Quantity<R> result = slopeDividend.multiply(slopeFactor).add(interception);
		logger.debug("apply(): xValue: {}, slope factor: {}, interception: {}, interim: {}", xValue, slopeFactor, interception, result);
		return result;
	}

	private static <Q extends Quantity<Q>> Quantity<Q> cloneAmount(Quantity<Q> quantity) {
		if (quantity == null) {
			return null;
		}
		return Quantities.getQuantity(quantity.getValue().doubleValue(), quantity.getUnit());
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
