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

import org.junit.BeforeClass;
import org.junit.Test;

import gda.jscience.physics.units.NonSIext;
import gda.util.QuantityFactory;

/**
 * JEPQuantityConverterTest Class
 * <p>
 * It is assumed the that working directory points to the test folder above gda
 */
public class JEPQuantityConverterTest {
	private static final String TEST_FILE_FOLDER = "testfiles/gda/util/converters/JEPQuantityConverterTest";

	@BeforeClass
	public static void setUpClass() {
		NonSIext.initializeClass();
	}

	@Test
	public void testToSource() throws Exception {
		final JEPQuantityConverter converter = new JEPQuantityConverter(JEPQuantityConverterParameters.jUnitTestFileName);
		testConverter(converter);
	}

	@Test
	public final void testReal() throws Exception {
		final JEPQuantityConverter converter = new JEPQuantityConverter(TEST_FILE_FOLDER + "/Simple.xml");
		testConverter(converter);
	}

	@Test
	public final void testSourceMinIsTargetMax() {
		assertEquals(false, new JEPQuantityConverter(TEST_FILE_FOLDER + "/Simple.xml").sourceMinIsTargetMax());
		assertEquals(true, new JEPQuantityConverter(TEST_FILE_FOLDER + "/SourceMinIsTargetMax.xml")
				.sourceMinIsTargetMax());
		assertEquals(false, new JEPQuantityConverter(TEST_FILE_FOLDER + "/SourceMinIsNOTTargetMax.xml")
				.sourceMinIsTargetMax());
	}

	@Test
	public final void testComplex() throws Exception {
		final JEPQuantityConverter converter = new JEPQuantityConverter(TEST_FILE_FOLDER + "/Complex.xml");
		testConverter(converter);
	}

	@Test
	public final void testCoupled() throws Exception {
		final JEPQuantityConverter converterTarget = new JEPQuantityConverter(TEST_FILE_FOLDER + "/DegToAngstrom.xml");
		final JEPQuantityConverter converterSource = new JEPQuantityConverter(TEST_FILE_FOLDER + "/mmToDeg.xml");
		final CoupledQuantityConverter converter = new CoupledQuantityConverter(converterSource, converterTarget);
		// target in Angstrom, source should be in mm
		testConverter(converter);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testBadCoupled() {
		final JEPQuantityConverter converterTarget = new JEPQuantityConverter(TEST_FILE_FOLDER + "/mmToDeg.xml");
		final JEPQuantityConverter converterSource = new JEPQuantityConverter(TEST_FILE_FOLDER + "/DegToAngstrom.xml");
		try {
			new CoupledQuantityConverter(converterSource, converterTarget);
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

	@Test
	public final void testUnits() {
		final JEPQuantityConverter converter = new JEPQuantityConverter(TEST_FILE_FOLDER + "/mmToDeg.xml");
		final JEPQuantityConverter dummyToGetUnits = new JEPQuantityConverter(TEST_FILE_FOLDER + "/DegToAngstrom.xml");
		final Quantity<? extends Quantity<?>> targetBeforeConversion = QuantityFactory.createFromObjectUnknownUnit(1.0, getAcceptableTargetUnits(dummyToGetUnits));
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

	private Unit<? extends Quantity<?>> getAcceptableTargetUnits(final IQuantityConverter converter) {
		return QuantityFactory.createUnitFromString(converter.getAcceptableTargetUnits().get(0));
	}

	private void testConverter(final IQuantityConverter converter) throws Exception {
		final Unit<? extends Quantity<?>> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<? extends Quantity<?>> targetBeforeConversion = QuantityFactory.createFromObjectUnknownUnit(1.0, acceptableTargetUnits);
		final Quantity<? extends Quantity<?>> source = converter.toSource(targetBeforeConversion);
		final Quantity<? extends Quantity<?>> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetAfterConversion, targetBeforeConversion);
	}
}
