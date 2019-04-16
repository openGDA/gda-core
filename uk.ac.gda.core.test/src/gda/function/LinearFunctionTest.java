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

import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.MICRO;
import static javax.measure.unit.SI.NANO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.measure.converter.ConversionException;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

public class LinearFunctionTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 0.000000001;

	private static final Amount<Length> LENGTH1 = Amount.valueOf(2.66, MICRO(METER));
	private static final Amount<Length> LENGTH2 = Amount.valueOf(1.83, MICRO(METER));
	private static final Amount<Length> LENGTH3 = Amount.valueOf(22.9, NANO(METER));

	private static final Amount<Energy> ENERGY1 = Amount.valueOf(1.2, ELECTRON_VOLT);
	private static final Amount<Energy> ENERGY2 = Amount.valueOf(3.7, ELECTRON_VOLT);

	@Test
	public void testNoArgsConstructor() {
		final LinearFunction function = new LinearFunction();
		assertEquals(1.0, function.getSlopeDividend().getEstimatedValue(), FP_TOLERANCE);
		assertEquals(Unit.ONE, function.getSlopeDividend().getUnit());
		assertEquals(1.0, function.getSlopeDivisor().getEstimatedValue(), FP_TOLERANCE);
		assertEquals(Unit.ONE, function.getSlopeDivisor().getUnit());
		assertEquals(0.0, function.getInterception().getEstimatedValue(), FP_TOLERANCE);
		assertEquals(Unit.ONE, function.getInterception().getUnit());	}

	@Test
	public void testThreeArgsConstructor() {
		final LinearFunction function = new LinearFunction(LENGTH2, ENERGY1, LENGTH1);
		assertFalse(LENGTH2 == function.getSlopeDividend());
		assertAmountEquals(LENGTH2, function.getSlopeDividend());

		assertFalse(ENERGY1 == function.getSlopeDivisor());
		assertAmountEquals(ENERGY1, function.getSlopeDivisor());

		assertFalse(LENGTH1 == function.getInterception());
		assertAmountEquals(LENGTH1, function.getInterception());
	}

	@Test
	public void testSettersCloneInput() {
		final LinearFunction function = new LinearFunction();
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
		final LinearFunction function = new LinearFunction();
		final Amount<? extends Quantity> input = Amount.valueOf(5.32, Unit.ONE);
		final Amount<? extends Quantity> result = function.apply(input);
		assertEquals(input.getEstimatedValue(), result.getEstimatedValue(), FP_TOLERANCE);
		assertEquals(Unit.ONE, result.getUnit());
	}

	public void testApplyThreeArgsContructor() {
		final LinearFunction function = new LinearFunction(LENGTH1, ENERGY1, LENGTH2);
		final Amount<? extends Quantity> result = function.apply(ENERGY2);
		assertEquals(8.3025, result.getEstimatedValue(), FP_TOLERANCE);
		assertEquals(MICRO(METER), result.getUnit());
	}

	@Test
	public void testApply() {
		final LinearFunction function = new LinearFunction();
		function.setInterception(LENGTH1);
		function.setSlopeDividend(LENGTH2);
		function.setSlopeDivisor(ENERGY1);

		final Amount<? extends Quantity> result = function.apply(ENERGY2);
		assertEquals(8.3025, result.getEstimatedValue(), FP_TOLERANCE);
		assertEquals(MICRO(METER), result.getUnit());
	}

	@Test
	public void testApplyDifferentUnits() {
		// Interception & dividend have different (but compatible) units
		final LinearFunction function = new LinearFunction(LENGTH3, ENERGY1, LENGTH1);
		final Amount<? extends Quantity> result = function.apply(ENERGY2);
		assertEquals(2730.60833333333, result.getEstimatedValue(), FP_TOLERANCE);
		assertEquals(NANO(METER), result.getUnit());
	}

	@Test(expected = ConversionException.class)
	public void testApplyIncorrectUnits() {
		final LinearFunction function = new LinearFunction(LENGTH1, ENERGY1, LENGTH2);
		function.apply(LENGTH3);
	}

	private void assertAmountEquals(Amount<? extends Quantity> expected, Amount<? extends Quantity> actual) {
		assertEquals(expected.getUnit(), actual.getUnit());
		assertEquals(expected.getEstimatedValue(), expected.getEstimatedValue(), FP_TOLERANCE);
	}

}
