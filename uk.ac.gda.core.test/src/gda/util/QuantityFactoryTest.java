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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static si.uom.NonSI.ANGSTROM;
import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.HERTZ;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.RADIAN;
import static tec.units.indriya.unit.Units.SECOND;

import javax.measure.Quantity;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;

import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyString;

import tec.units.indriya.AbstractUnit;
import tec.units.indriya.quantity.Quantities;

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
		final Quantity<Length> expected = Quantities.getQuantity(Double.valueOf(value), MILLI(METRE));
		final Quantity<Length> result = QuantityFactory.createFromString(valueUnitString);
		assertEquals(expected, result);

		// SI unit for length is metres
		assertEquals((value / 1000.0), (double) result.to(METRE).getValue(), 0.00001);
	}

	@Test
	public void testCreateLengthQuantityNoSpace() {
		// quantity constructed from single string length and unit, no space separation
		final double value = 56.78e-2;
		final String valueUnitString = "56.78e-2mm";
		final Quantity<Length> expected = Quantities.getQuantity(Double.valueOf(value), MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueUnitString));
	}

	@Test
	public void testCreateLengthQuantityTwoStrings() {
		// quantity constructed from two strings, length and unit
		final String unitString = "mm";
		final double value = -187.89;
		final String valueString = "-187.89";
		final Quantity<Length> expected = Quantities.getQuantity(value, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateLengthQuantityTwoStringsNegative() {
		// quantity constructed from two strings, -ve length and unit
		final String unitString = "nm";
		final String valueString = "-0.3";
		final double value = -0.3;
		final Quantity<Length> expected = Quantities.getQuantity(value, NANO(METRE));
		assertEquals(expected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateLengthQuantityTwoStringsMicrons() {
		// quantity constructed from two strings, length and unit
		final String unitString = "micron";
		final String value = "0.1";
		final Quantity<Length> expected = Quantities.getQuantity(Double.valueOf(value), MICRO(METRE));
		final Quantity<? extends Quantity<?>> result = QuantityFactory.createFromTwoStrings(value, unitString);
		assertEquals(expected, result);
	}

	@Test
	public void testCreateLengthQuantityTwoStringsAngstroms() {
		// quantity constructed from two strings, length and unit
		final String unitString = "Ang";
		final String value = "1.2";
		final Quantity<Length> expected = Quantities.getQuantity(Double.valueOf(value), ANGSTROM);
		final Quantity<? extends Quantity<?>> result = QuantityFactory.createFromTwoStrings(value, unitString);
		assertEquals(expected, result);
	}

	@Test
	public void testCreateLengthQuantityMinValueMilli() {
		final String valueString = Double.toString(MIN_DOUBLE) + " mm";
		final Quantity<Length> expected = Quantities.getQuantity(MIN_DOUBLE, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxValueMilli() {
		final String valueString = Double.toString(MAX_DOUBLE) + " mm";
		final Quantity<Length> expected = Quantities.getQuantity(MAX_DOUBLE, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinNegativeValueMilli() {
		final String valueString = Double.toString(MIN_NEG_DOUBLE) + " mm";
		final Quantity<Length> expected = Quantities.getQuantity(MIN_NEG_DOUBLE, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxNegativeValueMilli() {
		final String valueString = Double.toString(MAX_NEG_DOUBLE) + " mm";
		final Quantity<Length> expected = Quantities.getQuantity(MAX_NEG_DOUBLE, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinValueNano() {
		final String valueString = Double.toString(MIN_DOUBLE) + " nm";
		final Quantity<Length> expected = Quantities.getQuantity(MIN_DOUBLE, NANO(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxValueNano() {
		final String valueString = Double.toString(MAX_DOUBLE) + " nm";
		final Quantity<Length> expected = Quantities.getQuantity(MAX_DOUBLE, NANO(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinNegativeValueNano() {
		final String valueString = Double.toString(MIN_NEG_DOUBLE) + " nm";
		final Quantity<Length> expected = Quantities.getQuantity(MIN_NEG_DOUBLE, NANO(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxNegativeValueNano() {
		final String valueString = Double.toString(MAX_NEG_DOUBLE) + " nm";
		final Quantity<Length> expected = Quantities.getQuantity(MAX_NEG_DOUBLE, NANO(METRE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinValueAngstrom() {
		final String valueString = Double.toString(MIN_DOUBLE) + " Ang";
		final Quantity<Length> expected = Quantities.getQuantity(MIN_DOUBLE, ANGSTROM);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxValueAngstrom() {
		final String valueString = Double.toString(MAX_DOUBLE) + " Ang";
		final Quantity<Length> expected = Quantities.getQuantity(MAX_DOUBLE, ANGSTROM);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMinNegativeValueAngstrom() {
		final String valueString = Double.toString(MIN_NEG_DOUBLE) + " Ang";
		final Quantity<Length> expected = Quantities.getQuantity(MIN_NEG_DOUBLE, ANGSTROM);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateLengthQuantityMaxNegativeValueAngstrom() {
		final String valueString = Double.toString(MAX_NEG_DOUBLE) + " Ang";
		final Quantity<Length> expected = Quantities.getQuantity(MAX_NEG_DOUBLE, ANGSTROM);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	// -------------------------------------------------------------------------------------
	// Test creating length quantities with invalid input
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateLengthQuantityMismatchedUnits() {
		// mismatch in factory and length generated units
		final String valueString = "187.27";
		final String unitString = "micron";
		final Quantity<Length> unexpected = Quantities.getQuantity(187.27, MILLI(METRE));
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
		final Quantity<Angle> expected = Quantities.getQuantity(2.3, RADIAN);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMilliradianWithSpace() {
		// quantity constructed from single string mRadian angle and unit, space separated
		final String valueString = "0.0099 mRad";
		final Quantity<Angle> expected = Quantities.getQuantity(0.0099, MILLI(RADIAN));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMicroradianNoSpace() {
		final String valueString = "99 uRad";
		final Quantity<Angle> expected = Quantities.getQuantity(99, MICRO(RADIAN));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityDegreeWithSpace() {
		// quantity constructed from single string degree angle and unit, space separated
		final String valueString = "360 Deg";
		final Quantity<Angle> expected = Quantities.getQuantity(360, DEGREE_ANGLE);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMillidegreeNoSpace() {
		// quantity constructed from single string radian angle and unit, not space separated
		final String valueString = "-360mDeg";
		final Quantity<Angle> expected = Quantities.getQuantity(-360, MILLI(DEGREE_ANGLE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	// -------------------------------------------------------------------------------------
	// Range tests for angle quantities
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateAngleQuantityMinDegree() {
		final String valueString = Double.toString(MIN_DOUBLE) + " Deg";
		final Quantity<Angle> expected = Quantities.getQuantity(MIN_DOUBLE, DEGREE_ANGLE);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMaxDegree() {
		final String valueString = Double.toString(MAX_DOUBLE) + " Deg";
		final Quantity<Angle> expected = Quantities.getQuantity(MAX_DOUBLE, DEGREE_ANGLE);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMinMilliDegree() {
		final String valueString = Double.toString(MIN_DOUBLE) + " mDeg";
		final Quantity<Angle> expected = Quantities.getQuantity(MIN_DOUBLE, MILLI(DEGREE_ANGLE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMaxMilliDegree() {
		final String valueString = Double.toString(MAX_DOUBLE) + " mDeg";
		final Quantity<Angle> expected = Quantities.getQuantity(MAX_DOUBLE, MILLI(DEGREE_ANGLE));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMinMilliRadian() {
		final String valueString = Double.toString(MIN_DOUBLE) + " mRad";
		final Quantity<Angle> expected = Quantities.getQuantity(MIN_DOUBLE, MILLI(RADIAN));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMaxMilliRadian() {
		final String valueString = Double.toString(MAX_DOUBLE) + " mRad";
		final Quantity<Angle> expected = Quantities.getQuantity(MAX_DOUBLE, MILLI(RADIAN));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMinMicroRadian() {
		final String valueString = Double.toString(MIN_DOUBLE) + " uRad";
		final Quantity<Angle> expected = Quantities.getQuantity(MIN_DOUBLE, MICRO(RADIAN));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateAngleQuantityMaxMicroRadian() {
		final String valueString = Double.toString(MAX_DOUBLE) + " uRad";
		final Quantity<Angle> expected = Quantities.getQuantity(MAX_DOUBLE, MICRO(RADIAN));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of other quantities
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateElectronVolt() {
		final String valueString = "2.3 eV";
		final Quantity<Energy> expected = Quantities.getQuantity(2.3, ELECTRON_VOLT);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateNoUnit() {
		// quantity constructed from single value string (no unit)
		final String valueString = "2.38";
		final Quantity<Dimensionless> expected = Quantities.getQuantity(2.38, AbstractUnit.ONE);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateEmptyUnit() {
		// quantity constructed from two strings, a value and an empty unit
		final String valueString = "12.89";
		final String unitString = "";
		final Quantity<Dimensionless> expected = Quantities.getQuantity(12.89, AbstractUnit.ONE);
		assertEquals(expected, QuantityFactory.createFromTwoStrings(valueString, unitString));
	}

	@Test
	public void testCreateHertz() {
		// quantity constructed from single string Hz value and unit, space separated
		final String valueString = "7.23e+5 Hz";
		final Quantity<Frequency> expected = Quantities.getQuantity(7.23e+5, HERTZ);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateMillimetresPerSecond() {
		// quantity constructed from single string mm/s speed and unit, not space separated
		final String valueString = "0.1mm/s";
		final Quantity<? extends Quantity<?>> expected = Quantities.getQuantity(.1, MILLI(METRE).divide(SECOND));
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateElectronVoltMinValue() {
		final String valueString = Double.toString(MIN_DOUBLE) + " eV";
		final Quantity<Energy> expected = Quantities.getQuantity(MIN_DOUBLE, ELECTRON_VOLT);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateElectronVoltMaxValue() {
		final String valueString = Double.toString(MAX_DOUBLE) + " eV";
		final Quantity<Energy> expected = Quantities.getQuantity(MAX_DOUBLE, ELECTRON_VOLT);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateHertzMinValue() {
		final String valueString = Double.toString(MIN_DOUBLE) + " Hz";
		final Quantity<Frequency> expected = Quantities.getQuantity(MIN_DOUBLE, HERTZ);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	@Test
	public void testCreateHertzMaxValue() {
		final String valueString = Double.toString(MAX_DOUBLE) + " Hz";
		final Quantity<Frequency> expected = Quantities.getQuantity(MAX_DOUBLE, HERTZ);
		assertEquals(expected, QuantityFactory.createFromString(valueString));
	}

	// -------------------------------------------------------------------------------------
	// Test creation of length units
	// -------------------------------------------------------------------------------------
	@Test
	public void testCreateLengthUnitMilli() {
		assertEquals(MILLI(METRE), QuantityFactory.createUnitFromString("mm"));
	}

	@Test
	public void testCreateLengthUnitNano() {
		assertEquals(NANO(METRE), QuantityFactory.createUnitFromString("nm"));
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
		final Unit<? extends Quantity<?>> degree = QuantityFactory.createUnitFromString("Deg");
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
		assertEquals(AbstractUnit.ONE, QuantityFactory.createUnitFromString(""));
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
		final Quantity<Length> expected = Quantities.getQuantity(value, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromObject(new Double(value), MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectInteger() {
		final int value = 345;
		final Quantity<Length> expected = Quantities.getQuantity(value, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromObject(new Integer(value), MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectString() {
		final Quantity<Length> expected = Quantities.getQuantity(3.142, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromObject("3.142 mm", MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectStringConvertUnits() {
		final Quantity<Length> result = QuantityFactory.createFromObject("3.142 m", MILLI(METRE));
		assertEquals(3142.0, result.getValue().doubleValue(), 0.0001);
		assertEquals(MILLI(METRE), result.getUnit());
	}

	@Test
	public void testCreateFromObjectStringInvalid() {
		assertNull(QuantityFactory.createFromObject("abcdef", MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectPyString() {
		final Quantity<Length> expected = Quantities.getQuantity(7.234, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromObject(new PyString("7.234 mm"), MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectPyStringConvertUnits() {
		final Quantity<Length> result = QuantityFactory.createFromObject(new PyString("7.234 m"), MILLI(METRE));
		assertEquals(7234.0, result.getValue().doubleValue(), 0.0001);
		assertEquals(MILLI(METRE), result.getUnit());
	}

	@Test
	public void testCreateFromObjectPyStringInvalid() {
		assertNull(QuantityFactory.createFromObject(new PyString("abcdef"), MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectPyFloat() {
		final double value = 3.142;
		final Quantity<Length> expected = Quantities.getQuantity(value, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromObject(new PyFloat(value), MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectPyInteger() {
		final int value = 345;
		final Quantity<Length> expected = Quantities.getQuantity(value, MILLI(METRE));
		assertEquals(expected, QuantityFactory.createFromObject(new PyInteger(value), MILLI(METRE)));
	}

	@Test
	public void testCreateFromObjectQuantity() {
		final Quantity<Length> inputQuantity = Quantities.getQuantity(0.258, METRE);
		final Quantity<Length> result = QuantityFactory.createFromObject(inputQuantity, MILLI(METRE));
		assertEquals(258.0, result.getValue().doubleValue(), 0.0001);
		assertEquals(MILLI(METRE), result.getUnit());
	}

	@Test(expected = UnconvertibleException.class)
	public void testCreateFromObjectQuantityInvalidConversion() {
		final Quantity<Length> inputQuantity = Quantities.getQuantity(0.258, METRE);
		QuantityFactory.createFromObject(inputQuantity, RADIAN);
	}

	@Test
	public void testCreateFromObjectDimensionless() {
		final double value = 9.237;
		final Quantity<Dimensionless> inputQuantity = Quantities.getQuantity(value, AbstractUnit.ONE);
		final Quantity<Dimensionless> expected = Quantities.getQuantity(value, AbstractUnit.ONE);
		assertEquals(expected, QuantityFactory.createFromObject(inputQuantity, AbstractUnit.ONE));
	}

	@Test(expected = UnconvertibleException.class)
	public void testCreateFromObjectDimensionlessInvalidConversion() {
		final double value = 9.237;
		QuantityFactory.createFromObject(Quantities.getQuantity(value, AbstractUnit.ONE), METRE);
	}
}
