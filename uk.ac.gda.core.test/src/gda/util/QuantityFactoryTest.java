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

import static org.jscience.physics.units.NonSI.ANGSTROM;
import static org.jscience.physics.units.NonSI.DEGREE_ANGLE;
import static org.jscience.physics.units.NonSI.ELECTRON_VOLT;
import static org.jscience.physics.units.SI.HERTZ;
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.METER_PER_SECOND;
import static org.jscience.physics.units.SI.MICRO;
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.NANO;
import static org.jscience.physics.units.SI.RADIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jscience.physics.quantities.Dimensionless;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.jscience.physics.units.Unit;
import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyString;

/**
 * Test suite for QuantityFactory class
 */
public class QuantityFactoryTest {
	private static final double MAX_DOUBLE = Double.MAX_VALUE;
	private static final double MIN_DOUBLE = Double.MIN_VALUE;
	private static final double MAX_NEG_DOUBLE = -Double.MAX_VALUE;
	private static final double MIN_NEG_DOUBLE = -Double.MIN_VALUE;

	// -----------------------------------------------------------------------
	// Test creation of length quantities
	// -----------------------------------------------------------------------
	@Test
	public void testCreateLengthQuantity() {
		// quantity constructed from single string length and unit, space separated
		final double value = 12.34;
		final String valueUnitString = "12.34 mm";
		final Quantity expected = Quantity.valueOf(Double.valueOf(value), MILLI(METER));
		final Quantity result = QuantityFactory.createFromString(valueUnitString);
		assertEquals(expected, result);

		// SI unit for length is metres, so result's doubleValue() will return this
		assertEquals((value / 1000.0), result.doubleValue(), 0.00001);
	}

	@Test
	public void testCreateLengthQuantityNoSpace() {
		// quantity constructed from single string length and unit, no space separation
		final double value = 56.78e-2;
		final String valueUnitString = "56.78e-2mm";
		final Quantity expected = Quantity.valueOf(Double.valueOf(value), MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueUnitString));
	}

	@Test
	public void testCreateLengthQuantityTwoStrings() {
		// quantity constructed from two strings, length and unit
		final String unitString = "mm";
		final double value = -187.89;
		final String valueString = "-187.89";
		final Quantity expected = Quantity.valueOf(value, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateLengthQuantityTwoStringsNegative() {
		// quantity constructed from two strings, -ve length and unit
		final String unitString = "nm";
		final String valueString = "-0.3";
		final double value = -0.3;
		final Quantity expected = Quantity.valueOf(value, NANO(METER));
		assertEquals(expected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateLengthQuantityTwoStringsMicrons() {
		// quantity constructed from two strings, length and unit
		// !!! fails if use equals() instead of approxEquals !!!
		final String unitString = "micron";
		final String value = "0.1";
		final Quantity expected = Quantity.valueOf(Double.valueOf(value), MICRO(METER));
		final Quantity result = QuantityFactory.createFromTwoStrings(value, unitString);
		assertTrue(result.approxEquals(expected));
	}

	@Test
	public void testCreateLengthQuantityTwoStringsAngstroms() {
		// quantity constructed from two strings, length and unit
		// !!! fails if use equals() instead of approxEquals !!!
		final String unitString = "Ang";
		final String value = "1.2";
		final Quantity expected = Quantity.valueOf(Double.valueOf(value), ANGSTROM);
		final Quantity result = QuantityFactory.createFromTwoStrings(value, unitString);
		assertTrue(result.approxEquals(expected));
	}

	@Test
	public void testCreateLengthQuantityMinValueMilli() {
		final String valueString = Double.toString(MIN_DOUBLE) + " mm";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxValueMilli() {
		final String valueString = Double.toString(MAX_DOUBLE) + " mm";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinNegativeValueMilli() {
		final String valueString = Double.toString(MIN_NEG_DOUBLE) + " mm";
		final Quantity expected = Quantity.valueOf(MIN_NEG_DOUBLE, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxNegativeValueMilli() {
		final String valueString = Double.toString(MAX_NEG_DOUBLE) + " mm";
		final Quantity expected = Quantity.valueOf(MAX_NEG_DOUBLE, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinValueNano() {
		final String valueString = Double.toString(MIN_DOUBLE) + " nm";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, NANO(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxValueNano() {
		final String valueString = Double.toString(MAX_DOUBLE) + " nm";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, NANO(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinNegativeValueNano() {
		final String valueString = Double.toString(MIN_NEG_DOUBLE) + " nm";
		final Quantity expected = Quantity.valueOf(MIN_NEG_DOUBLE, NANO(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxNegativeValueNano() {
		final String valueString = Double.toString(MAX_NEG_DOUBLE) + " nm";
		final Quantity expected = Quantity.valueOf(MAX_NEG_DOUBLE, NANO(METER));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinValueAngstrom() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MIN_DOUBLE) + " Ang";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, ANGSTROM);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateLengthQuantityMaxValueAngstrom() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MAX_DOUBLE) + " Ang";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, ANGSTROM);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateLengthQuantityMinNegativeValueAngstrom() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MIN_NEG_DOUBLE) + " Ang";
		final Quantity expected = Quantity.valueOf(MIN_NEG_DOUBLE, ANGSTROM);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateLengthQuantityMaxNegativeValueAngstrom() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MAX_NEG_DOUBLE) + " Ang";
		final Quantity expected = Quantity.valueOf(MAX_NEG_DOUBLE, ANGSTROM);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	// -------------------------------------------------------------------------------------
	// Test creating length quantities with invalid input
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateLengthQuantityMismatchedUnits() {
		// mismatch in factory and length generated units
		final String valueString = "187.27";
		final String unitString = "micron";
		final Quantity unexpected = Quantity.valueOf(187.27, MILLI(METER));
		assertNotEquals(unexpected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateLengthQuantityFromStringNull() {
		// quantity constructed from single null value string (no unit)
		assertNull(QuantityFactory.createFromString(null));
	}

	@Test
	public void testCreateLengthQuantityFromTwoStringsNullUnit() {
		assertNull(QuantityFactory.createFromTwoStrings("12.89", null));
	}

	@Test
	public void testCreateLengthQuantityFromTwoStringsNullValue() {
		assertNull(QuantityFactory.createFromTwoStrings(null, "mm"));
	}

	@Test
	public void testCreateLengthQuantityFromTwoStringsNullValueAndUnit() {
		assertNull(QuantityFactory.createFromTwoStrings(null, null));
	}

	@Test
	public void testCreateLengthQuantityFromTwoStringsInvalidValue() {
		assertNull(QuantityFactory.createFromTwoStrings("abcdef", "mm"));
	}

	@Test
	public void testCreateLengthQuantityFromEmptyString() {
		assertNull(QuantityFactory.createFromString(""));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of angle quantities
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateAngleQuantityRadianWithSpace() {
		// quantity constructed from single string radian angle and unit, space separated
		final String valueString = "2.3 rad";
		final Quantity expected = Quantity.valueOf(2.3, RADIAN);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMilliradianWithSpace() {
		// quantity constructed from single string mRadian angle and unit, space separated
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = "0.0099 mRad";
		final Quantity expected = Quantity.valueOf(0.0099, MILLI(RADIAN));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMicroradianNoSpace() {
		// quantity constructed from single string uradian angle and unit, not space separated
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = "99 uRad";
		final Quantity expected = Quantity.valueOf(99, MICRO(RADIAN));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityDegreeWithSpace() {
		// quantity constructed from single string degree angle and unit, space separated
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = "360 Deg";
		final Quantity expected = Quantity.valueOf(360, DEGREE_ANGLE);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMillidegreeNoSpace() {
		// quantity constructed from single string radian angle and unit, not space separated
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = "-360mDeg";
		final Quantity expected = Quantity.valueOf(-360, MILLI(DEGREE_ANGLE));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	// -------------------------------------------------------------------------------------
	// Range tests for angle quantities
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateAngleQuantityMinDegree() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MIN_DOUBLE) + " Deg";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, DEGREE_ANGLE);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMaxDegree() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MAX_DOUBLE) + " Deg";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, DEGREE_ANGLE);
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMinMilliDegree() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MIN_DOUBLE) + " mDeg";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, MILLI(DEGREE_ANGLE));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMaxMilliDegree() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MAX_DOUBLE) + " mDeg";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, MILLI(DEGREE_ANGLE));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMinMilliRadian() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MIN_DOUBLE) + " mRad";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, MILLI(RADIAN));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMaxMilliRadian() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MAX_DOUBLE) + " mRad";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, MILLI(RADIAN));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMinMicroRadian() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MIN_DOUBLE) + " uRad";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, MICRO(RADIAN));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateAngleQuantityMaxMicroRadian() {
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = Double.toString(MAX_DOUBLE) + " uRad";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, MICRO(RADIAN));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of other quantities
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateElectronVolt() {
		final String valueString = "2.3 eV";
		final Quantity expected = Quantity.valueOf(2.3, ELECTRON_VOLT);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateNoUnit() {
		// quantity constructed from single value string (no unit)
		final String valueString = "2.38";
		final Quantity expected = Quantity.valueOf(2.38, Unit.ONE);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateEmptyUnit() {
		// quantity constructed from two strings, a value and an empty unit
		final String valueString = "12.89";
		final String unitString = "";
		final Quantity expected = Quantity.valueOf(12.89, Unit.ONE);
		assertEquals(expected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateHertz() {
		// quantity constructed from single string Hz value and unit, space separated
		final String valueString = "7.23e+5 Hz";
		final Quantity expected = Quantity.valueOf(7.23e+5, HERTZ);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateMillimetresPerSecond() {
		// quantity constructed from single string mm/s speed and unit, not space separated
		// !!! fails if use equals() instead of approxEquals !!!
		final String valueString = ".1mm/s";
		final Quantity expected = Quantity.valueOf(.1, MILLI(METER_PER_SECOND));
		assertTrue(expected.approxEquals(QuantityFactory.createFromString(valueString)));
	}

	@Test
	public void testCreateElectronVoltMinValue() {
		final String valueString = Double.toString(MIN_DOUBLE) + " eV";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, ELECTRON_VOLT);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateElectronVoltMaxValue() {
		final String valueString = Double.toString(MAX_DOUBLE) + " eV";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, ELECTRON_VOLT);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateHertzMinValue() {
		final String valueString = Double.toString(MIN_DOUBLE) + " Hz";
		final Quantity expected = Quantity.valueOf(MIN_DOUBLE, HERTZ);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateHertzMaxValue() {
		final String valueString = Double.toString(MAX_DOUBLE) + " Hz";
		final Quantity expected = Quantity.valueOf(MAX_DOUBLE, HERTZ);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of length units
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateLengthUnitMilli() {
		assertEquals(MILLI(METER), QuantityFactory.createUnitFromString("mm"));
	}

	@Test
	public void testCreateLengthUnitNano() {
		assertEquals(NANO(METER), QuantityFactory.createUnitFromString("nm"));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of angle units
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateAngleUnitRadian() {
		assertEquals(RADIAN, QuantityFactory.createUnitFromString("rad"));
	}

	@Test
	public void testCreateAngleUnitDegree() {
		final Unit<? extends Quantity> degree = QuantityFactory.createUnitFromString("Deg");
		assertTrue(degree.isCompatible(DEGREE_ANGLE));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of other (and null/empty) units
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateUnitElectronVolt() {
		assertEquals(ELECTRON_VOLT, QuantityFactory.createUnitFromString("eV"));
	}

	@Test
	public void testCreateUnitNullString() {
		assertNull(QuantityFactory.createUnitFromString(null));
	}

	@Test
	public void testCreateUnitEmptyString() {
		assertEquals(Unit.ONE, QuantityFactory.createUnitFromString(""));
	}

	@Test
	public void testCreateUnitInvalidString() {
		assertNull(QuantityFactory.createUnitFromString("sticksOfRhubarb"));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of from other objects
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateFromObjectDouble() {
		final double value = 3.142;
		final Quantity expected = Quantity.valueOf(value, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(new Double(value), MILLI(METER)));
	}

	@Test
	public void testCreateFromObjectInteger() {
		final int value = 345;
		final Quantity expected = Quantity.valueOf(value, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(new Integer(value), MILLI(METER)));
	}

	@Test
	public void testCreateFromObjectString() {
		final Quantity expected = Quantity.valueOf(3.142, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject("3.142 mm", MILLI(METER)));
	}

	@Test
	public void testCreateFromObjectStringConvertUnits() {
		final Quantity expected = Quantity.valueOf(3142, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject("3.142 m", MILLI(METER)));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateFromObjectStringInvalid() {
		QuantityFactory.createFromObject("abcdef", MILLI(METER));
	}

	@Test
	public void testCreateFromObjectPyString() {
		final Quantity expected = Quantity.valueOf(7.234, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(new PyString("7.234 mm"), MILLI(METER)));
	}

	@Test
	public void testCreateFromObjectPyStringConvertUnits() {
		final Quantity expected = Quantity.valueOf(7234, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(new PyString("7.234 m"), MILLI(METER)));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateFromObjectPyStringInvalid() {
		QuantityFactory.createFromObject(new PyString("abcdef"), MILLI(METER));
	}

	@Test
	public void testCreateFromObjectPyFloat() {
		final double value = 3.142;
		final Quantity expected = Quantity.valueOf(value, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(new PyFloat(value), MILLI(METER)));
	}

	@Test
	public void testCreateFromObjectPyInteger() {
		final int value = 345;
		final Quantity expected = Quantity.valueOf(value, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(new PyInteger(value), MILLI(METER)));
	}

	@Test
	public void testCreateFromObjectQuantity() {
		final Quantity inputQuantity = Quantity.valueOf(0.258, METER);
		final Quantity expected = Quantity.valueOf(258, MILLI(METER));
		assertEquals(expected, QuantityFactory.createFromObject(inputQuantity, MILLI(METER)));
	}

	@Test(expected = ConversionException.class)
	public void testCreateFromObjectQuantityInvalidConversion() {
		final Quantity inputQuantity = Quantity.valueOf(0.258, METER);
		QuantityFactory.createFromObject(inputQuantity, RADIAN);
	}

	@Test
	public void testCreateFromObjectDimensionless() {
		final double value = 9.237;
		final Quantity expected = Quantity.valueOf(value, Unit.ONE);
		assertEquals(expected, QuantityFactory.createFromObject(Dimensionless.valueOf(value), Unit.ONE));
	}

	@Test(expected = ConversionException.class)
	public void testCreateFromObjectDimensionlessInvalidConversion() {
		final double value = 9.237;
		QuantityFactory.createFromObject(Dimensionless.valueOf(value), METER);
	}
}
