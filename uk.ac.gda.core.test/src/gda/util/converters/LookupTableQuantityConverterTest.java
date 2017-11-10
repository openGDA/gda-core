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

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.util.converters.LookupTableQuantityConverter.Mode;

/**
 * LookupTableQuantityConverterTest Class. It is assumed the that working directory points to the test folder above gda
 */
public class LookupTableQuantityConverterTest {

	/**
	 * Delta to use when comparing doubles.
	 */
	private static final double DELTA = 0.001;

	public LookupTableQuantityConverterTest() {
	}

	@BeforeClass
	public static void setUp() {
		LocalProperties.set("gda.function.columnDataFile.lookupDir", "testfiles/gda/util/converters/LookupTableQuantityConverterTest");
	}

	@SuppressWarnings("unused")
	@Test
	public final void ToInvalidColumns() {
		try {
			new LookupTableQuantityConverter("Simple.txt", false, 0, 3);
			fail();
		} catch (Exception e) {
			String msg = e.getMessage();
			assertEquals(
					"LookupTableQuantityConverter.LookupTableQuantityConverter: Error accessing data from ColumnDataFile - check the column indices are correct. LookupTableQuantityConverter using details in Simple.txt. sColumn=0 tColumn=3 mode = BOTH_DIRECTIONS",
					msg);
		}
	}

	@Test
	public final void ToSource() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion);
			Quantity targetAfterConversion = converter.toTarget(source);
			assertEquals(targetAfterConversion, targetBeforeConversion);
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	@Test
	public final void getMode() {
		LookupTableQuantityConverter.Mode mode_Both = LookupTableQuantityConverter
				.getMode(LookupTableQuantityConverter.Mode_Both);
		assertEquals(mode_Both, LookupTableQuantityConverter.Mode.BOTH_DIRECTIONS);

		LookupTableQuantityConverter.Mode mode_TtoS = LookupTableQuantityConverter
				.getMode(LookupTableQuantityConverter.Mode_TtoS);
		assertEquals(mode_TtoS, LookupTableQuantityConverter.Mode.T_TO_S_ONLY);

		LookupTableQuantityConverter.Mode mode_StoT = LookupTableQuantityConverter
				.getMode(LookupTableQuantityConverter.Mode_StoT);
		assertEquals(mode_StoT, LookupTableQuantityConverter.Mode.S_TO_T_ONLY);

		try {
			LookupTableQuantityConverter.getMode("Bad");
			fail();
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("LookupTableQuantityConverter.getMode. Mode is invalid - Bad", msg);
		}

		try {
			LookupTableQuantityConverter.getMode(null);
			fail();
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("LookupTableQuantityConverter.getMode. modeString is null", msg);
		}
	}

	@Test
	public final void testModeToTargetOnly() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1,
				LookupTableQuantityConverter.Mode.S_TO_T_ONLY);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		try {
			converter.toSource(targetBeforeConversion);
			fail("Should not have been able to do T→S conversion with converter that only supports S→T conversion");
		} catch (UnsupportedConversionException e) {
			// expected
		}
	}

	@Test
	public final void DuplicateSourceValuesIncreasing() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("DuplicateSourceValuesIncreasing.txt", false, 0, 1,
				LookupTableQuantityConverter.Mode.S_TO_T_ONLY);

		Quantity source = Quantity.valueOf(2.5, converter.getAcceptableSourceUnits().get(0));
		Quantity target = converter.toTarget(source);
		assertEquals(2.5, target.getAmount(), 1E-10);

		source = Quantity.valueOf(3.5, converter.getAcceptableSourceUnits().get(0));
		target = converter.toTarget(source);
		assertEquals(1.5, target.getAmount(), 1E-10);
	}

	@Test
	public final void DuplicateSourceValuesDecreasing() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("DuplicateSourceValuesDecreasing.txt", false, 0, 1,
				LookupTableQuantityConverter.Mode.S_TO_T_ONLY);
		Quantity source = Quantity.valueOf(2.5, converter.getAcceptableSourceUnits().get(0));
		Quantity target = converter.toTarget(source);
		assertEquals(2.5, target.getAmount(),1E-10);

		source = Quantity.valueOf(3.6, converter.getAcceptableSourceUnits().get(0));
		target = converter.toTarget(source);
		assertEquals(1.4, target.getAmount(),1E-10);
	}

	/**
	 * Out of order meaning one ascending while other descending. sourceMinIsTargetMax should be true
	 */
	@Test
	public final void testOutOfOrder() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("OutOfOrder.txt", false, 0, 1);
		Quantity targetBeforeConversion = Quantity.valueOf(5.0, converter.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion);
			Quantity targetAfterConversion = converter.toTarget(source);
			assertEquals(targetBeforeConversion, targetAfterConversion);
			assertEquals(true, converter.sourceMinIsTargetMax());
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}


	/**
	 * Descending target with a duplicate value
	 */
	@Test
	public final void testOutOfOrderDuplicateTargetValues() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("OutOfOrderDuplicateAndDecreasingTargetValues.txt", false, 0, 1);
		Quantity targetBeforeConversion1 = Quantity.valueOf(-5, converter.getAcceptableTargetUnits().get(0));
		Quantity targetBeforeConversion2 = Quantity.valueOf(-105, converter.getAcceptableTargetUnits().get(0));

		try {
			Quantity source1 = converter.toSource(targetBeforeConversion1);
			Quantity source2 = converter.toSource(targetBeforeConversion2);
			assertEquals(Quantity.valueOf(0.25, converter.getAcceptableSourceUnits().get(0)), source1);
			assertEquals(Quantity.valueOf(1.5, converter.getAcceptableSourceUnits().get(0)), source2);
			assertEquals(true, converter.sourceMinIsTargetMax());
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	/**
	 * Test similar situation to that on I24 with descending source with a duplicate value
	 */
	@Test
	public final void testOutOfOrderDuplicateSourceValues() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("OutOfOrderDuplicateAndDecreasingSourceValues.txt", false, 0, 1);
		Quantity targetBeforeConversion1 = Quantity.valueOf(-5, converter.getAcceptableTargetUnits().get(0));
		Quantity targetBeforeConversion2 = Quantity.valueOf(4.5, converter.getAcceptableTargetUnits().get(0));

		try {
			Quantity source1 = converter.toSource(targetBeforeConversion1);
			Quantity source2 = converter.toSource(targetBeforeConversion2);
			assertEquals(Quantity.valueOf(0.25, converter.getAcceptableSourceUnits().get(0)), source1);
			assertEquals(-0.25, source2.getAmount(), DELTA); //using quantities gives an error for identical values
			assertEquals(true, converter.sourceMinIsTargetMax());
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	/**
	 * Test whether a non-ordered source causes the expected exception
	 */
	@SuppressWarnings("unused")
	@Test
	public final void testNotInOrder() {
		try {
			new LookupTableQuantityConverter("NotInOrder.txt", false, 0, 1);
			assertEquals(0,1); //should never reach here
		} catch (Exception e) {
			assertEquals("LookupTableQuantityConverter.LookupTableQuantityConverter: InterpolationFunction. xValues must be increasing or decreasing", e.getMessage());
		}
	}

	@Test
	public final void testReal() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion);
			Quantity targetAfterConversion = converter.toTarget(source);
			assertEquals(targetBeforeConversion, targetAfterConversion);
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	/**
	 * real case seen on I04
	 */
	@Test
	public final void testI04Pitch() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter(
				"BeamLineEnergy_DCM_Pitch_converter.txt", false, 0, 1);
		Quantity targetBeforeConversion = Quantity.valueOf(-.77, converter.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion);
			Quantity expectedSource = Quantity.valueOf(14.63, converter.getAcceptableSourceUnits().get(0));
			assertEquals(Math.round(expectedSource.getAmount() * 1E+6) / 1E+6,
					Math.round(source.getAmount() * 1E+6) / 1E+6, DELTA);
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	@Test
	public final void testCoupled() {
		LookupTableQuantityConverter converterTarget = new LookupTableQuantityConverter("DegToAngstrom.txt", false, 0,
				1);
		LookupTableQuantityConverter converterSource = new LookupTableQuantityConverter("mmToDeg.txt", false, 0, 1);
		CoupledQuantityConverter converter = new CoupledQuantityConverter(converterSource, converterTarget);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converterTarget.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion); // target
			// in Angstrom, source should be in mm
			Quantity targetAfterConversion = converter.toTarget(source);
			assertEquals(targetBeforeConversion, targetAfterConversion);
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	/**
	 * test to see effect of the rounding in the LookupTableQuantityConverter - prevents round trip. We need to remove
	 * the rounding.
	 */
	@Test
	public final void testCoupledHighFactor() {
		LookupTableQuantityConverter converterTarget = new LookupTableQuantityConverter("DegToAngstromHighFactor.txt",
				false, 0, 1);
		LookupTableQuantityConverter converterSource = new LookupTableQuantityConverter("mmToDegHighFactor.txt", false,
				0, 1);
		CoupledQuantityConverter converter = new CoupledQuantityConverter(converterSource, converterTarget);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converterTarget.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion); // target
			// in Angstrom, source should be in mm
			Quantity targetAfterConversion = converter.toTarget(source);
			assertEquals(targetBeforeConversion.getAmount(),
					Math.round(targetAfterConversion.getAmount() * 1E+6) / 1E+6, DELTA);
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	@Test
	public final void testBadCoupled() {
		LookupTableQuantityConverter converterTarget = new LookupTableQuantityConverter("Simple.txt", false, 0, 1);
		LookupTableQuantityConverter converterSource = new LookupTableQuantityConverter("Simple.txt", false, 0, 1);
		try {
			new CoupledQuantityConverter(converterSource, converterTarget);
			fail();
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals(
					"CoupledQuantityConverter.CoupledQuantityConverter: Error target unit (mm)\n"
							+ " of converter (LookupTableQuantityConverter using details in Simple.txt. sColumn=0 tColumn=1 mode = BOTH_DIRECTIONS)\n"
							+ " does not match source units (Ang)\n"
							+ " of converter (LookupTableQuantityConverter using details in Simple.txt. sColumn=0 tColumn=1 mode = BOTH_DIRECTIONS)",
					msg);
		}
	}

	@Test
	public final void testIncompatibleUnits() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("mmToDeg.txt", false, 0, 1);
		LookupTableQuantityConverter dummyToGetUnits = new LookupTableQuantityConverter("DegToAngstrom.txt", false, 0,
				1);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, dummyToGetUnits.getAcceptableTargetUnits().get(0));
		try {
			converter.toSource(targetBeforeConversion);
			fail("Should not have been able to do conversion using invalid units");
		} catch (InvalidUnitsException e) {
			// expected
		}
	}

	@Test
	public final void testCompatibleUnitsConvert() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("mmToDeg.txt", false, 0, 1);
		Unit<?> unit = Unit.valueOf("Angstrom");
		// 1 angstrom == 1e-7 mm
		Quantity toConvert = Quantity.valueOf(1e4, unit);
		Quantity result = converter.toTarget(toConvert);
		assertEquals(1e-3, result.getAmount(), 1e-9);
	}

	@Test
	public final void testUnity() {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("mmToUnity.txt", false, 0, 1);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		try {
			Quantity back = converter.toSource(targetBeforeConversion);
			LookupTableQuantityConverter converterBack = new LookupTableQuantityConverter("mmToUnity.txt", false, 1, 0);
			Quantity backAgain = converterBack.toSource(back);
			assertEquals(targetBeforeConversion, backAgain);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public final void testSourceMinIsTargetMax() {
		assertEquals(false, new LookupTableQuantityConverter("Simple.txt", false, 0, 1).sourceMinIsTargetMax());
		assertEquals(true, new LookupTableQuantityConverter("SourceMinIsTargetMax.txt", false, 0, 1)
				.sourceMinIsTargetMax());
	}

	@Test
	public final void testOutOfRangeToTarget() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1,
				Mode.S_TO_T_ONLY, false);
		Quantity sourceQuantity = Quantity.valueOf(3.0, converter.getAcceptableSourceUnits().get(0));
		try {
			converter.toTarget(sourceQuantity);
			fail("IllegalArgumentException expected when converting value out of range when not allowed to extrapolate");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	@Test
	public final void testOutOfRangeToSource() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1,
				Mode.T_TO_S_ONLY, false);
		Quantity targetQuantity = Quantity.valueOf(300.0, converter.getAcceptableTargetUnits().get(0));
		try {
			converter.toSource(targetQuantity);
			fail("IllegalArgumentException expected when converting value out of range when not allowed to extrapolate");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	@Test
	public final void testExtrapolatedToTarget() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1,
				Mode.S_TO_T_ONLY, true);
		Quantity sourceQuantity = Quantity.valueOf(3.0, converter.getAcceptableSourceUnits().get(0));
		Quantity targetQuantity = converter.toTarget(sourceQuantity);
		assertEquals(300.0, targetQuantity.getAmount(), DELTA);
	}

	@Test
	public final void testExtrapolatedToSource() throws Exception {
		LookupTableQuantityConverter converter = new LookupTableQuantityConverter("Simple.txt", false, 0, 1,
				Mode.T_TO_S_ONLY, true);
		Quantity targetQuantity = Quantity.valueOf(300.0, converter.getAcceptableTargetUnits().get(0));
		Quantity sourceQuantity = converter.toSource(targetQuantity);
		assertEquals(3.0, sourceQuantity.getAmount(), DELTA);
	}
}
