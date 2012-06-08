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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.jscience.physics.units.Unit;
import org.junit.Test;

/**
 * Test suite for QuantityFactory class
 */
public class QuantityFactoryTest {
	private Quantity q, q1;
	private String value, valueUnitStr;
	private String unitString;
	private final double maxDouble = Double.MAX_VALUE;
	private final double minDouble = Double.MIN_VALUE;
	private final double maxNegDouble = -Double.MAX_VALUE;
	private final double minNegDouble = -Double.MIN_VALUE;
	private Unit<?> u;

	/**
	 * Test creation of length quantities
	 */
	@Test
	public void testCreateLengthQuantity() {
		// *** valid lengths that should work

		// quantity constructed from single string length and unit, space
		// separated
		value = "12.34";
		valueUnitStr = value + " mm";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// check quantity can yield creation string - internally stored in meters
		// so comes back as such (alas).
		String ValueMeters = Double.toString((new Double(value).doubleValue()) / 1000.);
		String outVal = new Double(q.doubleValue()).toString();
		assertTrue(outVal.equals(ValueMeters));

		// quantity constructed from single string length and unit, no space
		// separation
		value = "56.78e-2";
		valueUnitStr = value + "mm";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from two strings, length and unit
		unitString = "mm";
		value = "-187.89";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + value + unitString, q.equals(QuantityFactory
				.createFromTwoStrings(value, unitString)));

		// quantity constructed from two strings, -ve length and unit
		unitString = "nm";
		value = "-0.3";
		q = Quantity.valueOf(Double.valueOf(value), SI.NANO(SI.METER));
		assertTrue("quantity factory length not at expected value " + value + unitString, q.equals(QuantityFactory
				.createFromTwoStrings(value, unitString)));

		// quantity constructed from two strings, length and unit
		// !!! fails if use equals() instead of approxEquals !!!
		unitString = "micron";
		value = "0.1";
		q = Quantity.valueOf(Double.valueOf(value), SI.MICRO(SI.METER));
		assertTrue("quantity factory length not at expected value " + value + unitString, q
				.approxEquals(QuantityFactory.createFromTwoStrings(value, unitString)));

		// quantity constructed from two strings, length and unit
		// !!! fails if use equals() instead of approxEquals !!!
		unitString = "Ang";
		value = "1.2";
		q = Quantity.valueOf(Double.valueOf(value), NonSI.ANGSTROM);
		q1 = QuantityFactory.createFromTwoStrings(value, unitString);
		assertTrue("quantity factory length not at expected value " + value + unitString, q.approxEquals(q1));

		// *** range tests

		// max and min double values for mm
		value = Double.toString(minDouble);
		valueUnitStr = value + " mm";
		q = Quantity.valueOf(minDouble, SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " mm";
		q = Quantity.valueOf(maxDouble, SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(minNegDouble);
		valueUnitStr = value + " mm";
		q = Quantity.valueOf(minNegDouble, SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxNegDouble);
		valueUnitStr = value + " mm";
		q = Quantity.valueOf(maxNegDouble, SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// max and min double values for nm
		value = Double.toString(minDouble);
		valueUnitStr = value + " nm";
		q = Quantity.valueOf(minDouble, SI.NANO(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " nm";
		q = Quantity.valueOf(maxDouble, SI.NANO(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(minNegDouble);
		valueUnitStr = value + " nm";
		q = Quantity.valueOf(minNegDouble, SI.NANO(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxNegDouble);
		valueUnitStr = value + " nm";
		q = Quantity.valueOf(maxNegDouble, SI.NANO(SI.METER));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// max and min double values for Angstroms
		// !!! fails if use equals() instead of approxEquals !!!
		value = Double.toString(minDouble);
		valueUnitStr = value + " Ang";
		q = Quantity.valueOf(minDouble, NonSI.ANGSTROM);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " Ang";
		q = Quantity.valueOf(maxDouble, NonSI.ANGSTROM);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(minNegDouble);
		valueUnitStr = value + " Ang";
		q = Quantity.valueOf(minNegDouble, NonSI.ANGSTROM);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxNegDouble);
		valueUnitStr = value + " Ang";
		q = Quantity.valueOf(maxNegDouble, NonSI.ANGSTROM);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// *** invalid requested values

		// mismatch in factory and length generated units
		unitString = "micron";
		value = "187.27";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length at unexpected value " + value + unitString, !q.equals(QuantityFactory
				.createFromTwoStrings(value, unitString)));

		// quantity constructed from single null value string (no unit)
		value = null;
		assertTrue("quantity factory quantity should be null", null == QuantityFactory.createFromString(value));

		// quantity constructed from two strings, a value and a null unit
		unitString = null;
		value = "12.89";
		assertTrue("quantity factory quantity should be null", null == QuantityFactory.createFromTwoStrings(value,
				unitString));

		// quantity constructed from two strings, a null value and a length unit
		unitString = "mm";
		value = null;
		assertTrue("quantity factory quantity should be null", null == QuantityFactory.createFromTwoStrings(value,
				unitString));

		// quantity constructed from two strings, a null value and a null unit
		unitString = null;
		value = null;

		assertTrue("quantity factory quantity should be " + value, null == QuantityFactory.createFromTwoStrings(value,
				unitString));

		// quantity constructed from single string, a null value
		value = "";

		assertTrue("quantity factory quantity should be null", null == QuantityFactory.createFromString(value));
	}

	/**
	 * Test creation of angle quantities
	 */
	@Test
	public void testCreateAngleQuantity() {
		// *** valid angles that should work

		// quantity constructed from single string radian angle and unit, space
		// separated
		value = "2.3";
		valueUnitStr = value + " rad";
		q = Quantity.valueOf(Double.valueOf(value), SI.RADIAN);
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from single string mRadian angle and unit, space
		// separated
		// !!! fails if use equals() instead of approxEquals !!!
		value = ".0099";
		valueUnitStr = value + " mRad";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.RADIAN));
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from single string uradian angle and unit, not
		// space separated
		// !!! fails if use equals() instead of approxEquals !!!
		value = "99";
		valueUnitStr = value + "uRad";
		q = Quantity.valueOf(Double.valueOf(value), SI.MICRO(SI.RADIAN));
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from single string degree angle and unit, space
		// separated
		// !!! fails if use equals() instead of approxEquals !!!
		value = "360";
		valueUnitStr = value + " Deg";
		q = Quantity.valueOf(Double.valueOf(value), NonSI.DEGREE_ANGLE);
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from single string radian angle and unit, not
		// space separated
		// !!! fails if use equals() instead of approxEquals !!!
		value = "-360";
		valueUnitStr = value + " mDeg";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(NonSI.DEGREE_ANGLE));
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// *** range tests

		// max and min double values for Deg
		// !!! fails if use equals() instead of approxEquals !!!
		value = Double.toString(minDouble);
		valueUnitStr = value + " Deg";
		q = Quantity.valueOf(minDouble, NonSI.DEGREE_ANGLE);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " Deg";
		q = Quantity.valueOf(maxDouble, NonSI.DEGREE_ANGLE);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// max and min double values for mDeg
		// !!! fails if use equals() instead of approxEquals !!!
		value = Double.toString(minDouble);
		valueUnitStr = value + " mDeg";
		q = Quantity.valueOf(minDouble, SI.MILLI(NonSI.DEGREE_ANGLE));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " mDeg";
		q = Quantity.valueOf(maxDouble, SI.MILLI(NonSI.DEGREE_ANGLE));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// max and min double values for mRad
		// !!! fails if use equals() instead of approxEquals !!!
		value = Double.toString(minDouble);
		valueUnitStr = value + " mRad";
		q = Quantity.valueOf(minDouble, SI.MILLI(SI.RADIAN));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " mRad";
		q = Quantity.valueOf(maxDouble, SI.MILLI(SI.RADIAN));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// max and min double values for uRad
		// !!! fails if use equals() instead of approxEquals !!!
		value = Double.toString(minDouble);
		valueUnitStr = value + " uRad";
		q = Quantity.valueOf(minDouble, SI.MICRO(SI.RADIAN));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " uRad";
		q = Quantity.valueOf(maxDouble, SI.MICRO(SI.RADIAN));
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// *** invalid requested values

	}

	/**
	 * Test creation of other quantities
	 */
	@Test
	public void testCreateOtherQuantity() {
		// *** valid values that should work

		// quantity constructed from single string eV value and unit, space
		// separated
		value = "2.3";
		valueUnitStr = value + " eV";
		q = Quantity.valueOf(Double.valueOf(value), NonSI.ELECTRON_VOLT);
		assertTrue("quantity factory quantity not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from single value string (no unit)
		value = "2.38";
		q = Quantity.valueOf(Double.valueOf(value), Unit.ONE);
		assertTrue("quantity factory quantity not at expected value " + value, q.equals(QuantityFactory
				.createFromString(value)));

		// quantity constructed from two strings, a value and an empty unit
		unitString = "";
		value = "12.89";
		q = Quantity.valueOf(Double.valueOf(value), Unit.ONE);
		assertTrue("quantity factory quantity not at expected value " + value, q.equals(QuantityFactory
				.createFromTwoStrings(value, unitString)));

		// quantity constructed from single string Hz value and unit, space
		// separated
		value = "7.23e+5";
		valueUnitStr = value + " Hz";
		q = Quantity.valueOf(Double.valueOf(value), SI.HERTZ);
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// quantity constructed from single string mm/s speed and unit, not
		// space separated
		// !!! fails if use equals() instead of approxEquals !!!
		value = ".1";
		valueUnitStr = value + " mm/s";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER_PER_SECOND));
		assertTrue("quantity factory angle not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// *** range tests

		// max and min double values for eV
		// !!! fails if use equals() instead of approxEquals !!!
		value = Double.toString(minDouble);
		valueUnitStr = value + " eV";
		q = Quantity.valueOf(minDouble, NonSI.ELECTRON_VOLT);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " eV";
		q = Quantity.valueOf(maxDouble, NonSI.ELECTRON_VOLT);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.approxEquals(QuantityFactory
				.createFromString(valueUnitStr)));

		// max and min double values for Hz
		value = Double.toString(minDouble);
		valueUnitStr = value + " Hz";
		q = Quantity.valueOf(minDouble, SI.HERTZ);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		value = Double.toString(maxDouble);
		valueUnitStr = value + " Hz";
		q = Quantity.valueOf(maxDouble, SI.HERTZ);
		assertTrue("quantity factory length not at expected value " + valueUnitStr, q.equals(QuantityFactory
				.createFromString(valueUnitStr)));

		// *** invalid requested values
	}

	/**
	 * force an exception with null value arithmatic operation
	 */
	@Test(expected = NullPointerException.class)
	public void testException() {
		// exception test - a null quantity generates null pointer exception
		double dblVal = (QuantityFactory.createFromString("")).doubleValue();
		dblVal = dblVal / 0.;
		assertFalse("expected null pointer exception not seen", false);
	}

	/**
	 * Test creation of length units
	 */
	@Test
	public void testCreateLengthUnit() {
		// *** valid units that should work

		// unit constructed from valid length string
		unitString = "mm";
		value = "12.89";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		u = q.getUnit();
		assertTrue("unit string should be " + unitString, u.equals(QuantityFactory.createUnitFromString(unitString)));

		// *** range tests

		// *** invalid requested values

	}

	/**
	 * Test creation of angle units
	 */
	@Test
	public void testCreateAngleUnit() {
		// *** valid units that should work

		// unit constructed from valid radian angle string
		unitString = "rad";
		value = "12.89";
		q = Quantity.valueOf(Double.valueOf(value), SI.RADIAN);
		u = q.getUnit();
		assertTrue("unit string should be " + unitString, u.equals(QuantityFactory.createUnitFromString(unitString)));

		// *** range tests

		// *** invalid requested values

	}

	/**
	 * Test creation of other units
	 */
	@Test
	public void testCreateOtherUnit() {
		// *** valid units that should work

		// unit constructed from valid eV string
		unitString = "eV";
		value = "12.89";
		q = Quantity.valueOf(Double.valueOf(value), NonSI.ELECTRON_VOLT);
		u = q.getUnit();
		assertTrue("unit string should be " + unitString, u.equals(QuantityFactory.createUnitFromString(unitString)));

		// *** range tests

		// *** invalid requested values

		// unit constructed from null string
		unitString = null;
		assertTrue("unit string should be " + unitString, null == QuantityFactory.createUnitFromString(unitString));

		// unit constructed from empty string
		unitString = "";
		u = Unit.ONE;
		assertTrue("unit string should be " + unitString, u.equals(QuantityFactory.createUnitFromString(unitString)));

		// unit constructed from silly string
		unitString = "sticksOfRhubarb";
		assertTrue("unit string should be null", null == QuantityFactory.createUnitFromString(unitString));
	}

	/**
	 * Test creation of other units
	 */
	@Test
	public void testCreateFromObject() {
		value = "3.142";
		Double d = new Double(value);
		Object o = d;
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + value, q.equals(QuantityFactory.createFromObject(
				o, SI.MILLI(SI.METER))));

		value = "345";
		Integer i = new Integer(value);
		o = i;
		q = Quantity.valueOf(Integer.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + value, q.equals(QuantityFactory.createFromObject(
				o, SI.MILLI(SI.METER))));

		value = "3.142";
		o = value + " mm";
		q = Quantity.valueOf(Double.valueOf(value), SI.MILLI(SI.METER));
		assertTrue("quantity factory length not at expected value " + value, q.equals(QuantityFactory.createFromObject(
				o, SI.MILLI(SI.METER))));
	}
}
