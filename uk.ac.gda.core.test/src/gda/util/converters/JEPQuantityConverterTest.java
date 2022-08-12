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

package gda.util.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gda.jscience.physics.units.NonSIext;
import gda.util.QuantityFactory;

/**
 * JEPQuantityConverterTest Class
 * <p>
 * It is assumed the that working directory points to the test folder above gda
 */
public class JEPQuantityConverterTest {
	private static final String TEST_FILE_FOLDER = "testfiles/gda/util/converters/JEPQuantityConverterTest";

	@BeforeAll
	public static void setUpClass() {
		NonSIext.initializeClass();
	}

	@Test
	public void testToSource() throws Exception {
		final JEPQuantityConverter<Length, Length> converter = new JEPQuantityConverter<>(JEPQuantityConverterParameters.jUnitTestFileName);
		testConverter(converter);
	}

	@Test
	public final void testReal() throws Exception {
		final JEPQuantityConverter<Angle, Length> converter = new JEPQuantityConverter<>(TEST_FILE_FOLDER + "/Simple.xml");
		testConverter(converter);
	}

	@Test
	public final void testSourceMinIsTargetMax() {
		assertEquals(false, new JEPQuantityConverter<Angle, Length>(TEST_FILE_FOLDER + "/Simple.xml").sourceMinIsTargetMax());
		assertEquals(true, new JEPQuantityConverter<Angle, Length>(TEST_FILE_FOLDER + "/SourceMinIsTargetMax.xml")
				.sourceMinIsTargetMax());
		assertEquals(false, new JEPQuantityConverter<Angle, Length>(TEST_FILE_FOLDER + "/SourceMinIsNOTTargetMax.xml")
				.sourceMinIsTargetMax());
	}

	@Test
	public final void testComplex() throws Exception {
		final JEPQuantityConverter<Angle, Length> converter = new JEPQuantityConverter<>(TEST_FILE_FOLDER + "/Complex.xml");
		testConverter(converter);
	}

	@Test
	public final void testCoupled() throws Exception {
		final JEPQuantityConverter<Angle, Length> converterTarget = new JEPQuantityConverter<>(TEST_FILE_FOLDER + "/DegToAngstrom.xml");
		final JEPQuantityConverter<Length, Angle> converterSource = new JEPQuantityConverter<>(TEST_FILE_FOLDER + "/mmToDeg.xml");
		final CoupledQuantityConverter<Length, Length, Angle> converter = new CoupledQuantityConverter<>(converterSource, converterTarget);
		// target in Angstrom, source should be in mm
		testConverter(converter);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testBadCoupled() {
		final JEPQuantityConverter<Length, Angle> converterTarget = new JEPQuantityConverter<>(TEST_FILE_FOLDER + "/mmToDeg.xml");
		final JEPQuantityConverter<Angle, Length> converterSource = new JEPQuantityConverter<>(TEST_FILE_FOLDER + "/DegToAngstrom.xml");
		try {
			new CoupledQuantityConverter<Angle, Angle, Length>(converterSource, converterTarget);
			fail("Creating CoupledQuantityConverter with incompatible units should throw exception");
		} catch (IllegalArgumentException e) {
			final String msg = e.getMessage();
			assertEquals("CoupledQuantityConverter.CoupledQuantityConverter: Error target unit (Ang)\n"
					+ " of converter (JEPQuantityConverter using details in " + converterTarget.getExpressionFileName()
					+ ")\n" + " does not match source units (mm)\n"
					+ " of converter (JEPQuantityConverter using details in " + converterSource.getExpressionFileName()
					+ ")", msg);
		}
	}

	/*
	 * The Java compiler used by Buckminster treats the call to QuantityFactory.createFromObject() as an compilation error
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public final void testUnits() {
		final JEPQuantityConverter converter = new JEPQuantityConverter(TEST_FILE_FOLDER + "/mmToDeg.xml");
		final JEPQuantityConverter dummyToGetUnits = new JEPQuantityConverter(TEST_FILE_FOLDER + "/DegToAngstrom.xml");
		final Quantity targetBeforeConversion = QuantityFactory.createFromObject(1.0, getAcceptableTargetUnits(dummyToGetUnits));
		try {
			converter.toSource(targetBeforeConversion);
			fail("Calling JEPQuantityConverter with incompatible units should throw exception");
		} catch (IllegalArgumentException e) {
			final String msg = e.getMessage();
			assertEquals(
					"JEPQuantityConverter.ToSource: target units (Å) do not match acceptableUnits (deg)JEPQuantityConverter using details in "
							+ converter.getExpressionFileName(), msg);
		}
	}
	*/

	private <S extends Quantity<S>, T extends Quantity<T>> Unit<T> getAcceptableTargetUnits(final IQuantityConverter<S, T> converter) {
		return QuantityFactory.createUnitFromString(converter.getAcceptableTargetUnits().get(0));
	}

	private <S extends Quantity<S>, T extends Quantity<T>> void testConverter(final IQuantityConverter<S, T> converter) throws Exception {
		final Unit<T> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<T> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);
		final Quantity<S> source = converter.toSource(targetBeforeConversion);
		final Quantity<T> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetAfterConversion, targetBeforeConversion);
	}
}
