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

import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.MICRO;
import static org.jscience.physics.units.SI.NANO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.junit.Test;

import gda.util.QuantityFactory;

public class LinearFunctionTest {
	// For the time being, LinearFunction takes Amounts as strings
	private static final String LENGTH1_STRING = "2.66 µm";
	private static final String LENGTH2_STRING = "1.83 µm";
	private static final String LENGTH3_STRING = "22.9 nm";

	private static final String ENERGY1_STRING = "1.2 eV";
	private static final String ENERGY2_STRING = "3.7 eV";

	private static final Quantity LENGTH1 = QuantityFactory.createFromString(LENGTH1_STRING);
	private static final Quantity LENGTH2 = QuantityFactory.createFromString(LENGTH2_STRING);
	private static final Quantity LENGTH3 = QuantityFactory.createFromString(LENGTH3_STRING);

	private static final Quantity ENERGY2 = QuantityFactory.createFromString(ENERGY2_STRING);

	@Test
	public void testNoArgsConstructor() {
		final LinearFunction function = new LinearFunction();
		assertNull(function.getInterception());
		assertNull(function.getSlopeDividend());
		assertNull(function.getSlopeDivisor());
	}

	@Test
	public void testTwoArgsConstructor() {
		final LinearFunction function = new LinearFunction(LENGTH1, LENGTH2);
		assertNull(function.getInterception());
		assertNull(function.getSlopeDividend());
		assertNull(function.getSlopeDivisor());
	}

	@Test(expected = NullPointerException.class)
	public void testApplyNoArgsContructor() {
		final LinearFunction function = new LinearFunction();
		function.apply(LENGTH1);
	}

	@Test(expected = NullPointerException.class)
	public void testApplyTwoArgsContructor() {
		final LinearFunction function = new LinearFunction(LENGTH1, LENGTH2);
		function.apply(LENGTH1);
	}

	@Test
	public void testApply() {
		final LinearFunction function = new LinearFunction();
		function.setInterception(LENGTH1_STRING);
		function.setSlopeDividend(LENGTH2_STRING);
		function.setSlopeDivisor(ENERGY1_STRING);

		final Quantity result = function.apply(ENERGY2);
		assertEquals(8.3025, result.getAmount(), 0.00001);
		assertEquals(MICRO(METER), result.getUnit());
	}

	@Test
	public void testApplyDifferentUnits() {
		final LinearFunction function = new LinearFunction();
		// Interception & dividend have different (but compatible) units
		function.setInterception(LENGTH1_STRING);
		function.setSlopeDividend(LENGTH3_STRING);
		function.setSlopeDivisor(ENERGY1_STRING);

		final Quantity result = function.apply(ENERGY2);
		assertEquals(2730.608333, result.getAmount(), 0.00001);
		assertEquals(NANO(METER), result.getUnit());
	}

	@Test(expected = ConversionException.class)
	public void testApplyIncorrectUnits() {
		final LinearFunction function = new LinearFunction();
		function.setInterception(LENGTH1_STRING);
		function.setSlopeDividend(LENGTH2_STRING);
		function.setSlopeDivisor(ENERGY1_STRING);

		function.apply(LENGTH3);
	}

}
