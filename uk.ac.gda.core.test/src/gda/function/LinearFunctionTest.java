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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.AbstractUnit.ONE;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.METRE;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.jupiter.api.Test;

import tec.units.indriya.quantity.Quantities;

public class LinearFunctionTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 0.000000001;

	private static final Quantity<Length> LENGTH1 = Quantities.getQuantity(2.66, MICRO(METRE));
	private static final Quantity<Length> LENGTH2 = Quantities.getQuantity(1.83, MICRO(METRE));
	private static final Quantity<Length> LENGTH3 = Quantities.getQuantity(22.9, NANO(METRE));

	private static final Quantity<Energy> ENERGY1 = Quantities.getQuantity(1.2, ELECTRON_VOLT);
	private static final Quantity<Energy> ENERGY2 = Quantities.getQuantity(3.7, ELECTRON_VOLT);

	@Test
	public void testNoArgsConstructor() {
		final LinearFunction<Energy, Length> function = new LinearFunction<>();
		assertNull(function.getSlopeDividend());
		assertNull(function.getSlopeDivisor());
		assertNull(function.getInterception());
	}

	@Test
	public void testThreeArgsConstructor() {
		final LinearFunction<Energy, Length> function = new LinearFunction<>(LENGTH2, ENERGY1, LENGTH1);
		assertFalse(LENGTH2 == function.getSlopeDividend());
		assertAmountEquals(LENGTH2, function.getSlopeDividend());

		assertFalse(ENERGY1 == function.getSlopeDivisor());
		assertAmountEquals(ENERGY1, function.getSlopeDivisor());

		assertFalse(LENGTH1 == function.getInterception());
		assertAmountEquals(LENGTH1, function.getInterception());
	}

	@Test
	public void testSettersCloneInput() {
		final LinearFunction<Energy, Length> function = new LinearFunction<>();
		function.setSlopeDividend(LENGTH2);
		function.setSlopeDivisor(ENERGY1);
		function.setInterception(LENGTH1);

		assertFalse(LENGTH2 == function.getSlopeDividend());
		assertAmountEquals(LENGTH2, function.getSlopeDividend());

		assertFalse(ENERGY1 == function.getSlopeDivisor());
		assertAmountEquals(ENERGY1, function.getSlopeDivisor());

		assertFalse(LENGTH1 == function.getInterception());
		assertAmountEquals(LENGTH1, function.getInterception());
	}

	public void testApplyNoArgsContructor() {
		final LinearFunction<Dimensionless, Dimensionless> function = new LinearFunction<>();
		final Quantity<Dimensionless> input = Quantities.getQuantity(5.32, ONE);
		final Quantity<Dimensionless> result = function.apply(input);
		assertEquals(input.getValue().doubleValue(), result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ONE, result.getUnit());
	}

	public void testApplyThreeArgsContructor() {
		final LinearFunction<Energy, Length> function = new LinearFunction<>(LENGTH1, ENERGY1, LENGTH2);
		final Quantity<Length> result = function.apply(ENERGY2);
		assertEquals(8.3025, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MICRO(METRE), result.getUnit());
	}

	@Test
	public void testApply() {
		final LinearFunction<Energy, Length> function = new LinearFunction<>();
		function.setInterception(LENGTH1);
		function.setSlopeDividend(LENGTH2);
		function.setSlopeDivisor(ENERGY1);

		final Quantity<? extends Quantity<?>> result = function.apply(ENERGY2);
		assertEquals(8.3025, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MICRO(METRE), result.getUnit());
	}

	@Test
	public void testApplyDifferentUnits() {
		// Interception & dividend have different (but compatible) units
		final LinearFunction<Energy, Length> function = new LinearFunction<>(LENGTH3, ENERGY1, LENGTH1);
		final Quantity<? extends Quantity<?>> result = function.apply(ENERGY2);
		assertEquals(2730.60833333333, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(NANO(METRE), result.getUnit());
	}

	private void assertAmountEquals(Quantity<? extends Quantity<?>> expected, Quantity<? extends Quantity<?>> actual) {
		assertEquals(expected.getUnit(), actual.getUnit());
		assertEquals(expected.getValue().doubleValue(), expected.getValue().doubleValue(), FP_TOLERANCE);
	}

}
