/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

/** A linear function that assumes the calculated amount uses the same units as the original */
public class SimpleAffineFunction extends FindableFunction {

	private double scaler = 1;
	private double offset = 0;

	/**
	 * Calculate scaler * amount + offset
	 */
	@Override
	public Amount<? extends Quantity> apply(Amount<? extends Quantity> amount) {
		Unit<? extends Quantity> unit = amount.getUnit();
		return amount.times(scaler).plus(Amount.valueOf(offset, unit));
	}

	public double getScaler() {
		return scaler;
	}

	public void setScaler(double scaler) {
		this.scaler = scaler;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}
}