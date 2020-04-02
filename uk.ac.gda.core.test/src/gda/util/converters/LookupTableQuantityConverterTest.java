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
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.BeforeClass;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.jscience.physics.units.NonSIext;
import gda.util.QuantityFactory;
import gda.util.converters.LookupTableQuantityConverter.Mode;

/**
 * LookupTableQuantityConverterTest Class. It is assumed the that working directory points to the test folder above gda
 */
public class LookupTableQuantityConverterTest {
	/**
	 * Delta to use when comparing doubles.
	 */
	private static final double DELTA = 0.00001;

	@BeforeClass
	public static void setUp() {
		NonSIext.initializeClass();
		LocalProperties.set("gda.function.columnDataFile.lookupDir", "testfiles/gda/util/converters/LookupTableQuantityConverterTest");
	}

	@SuppressWarnings("unused")
	@Test
	public final void ToInvalidColumns() {
		try {
			new LookupTableQuantityConverter<Length, Length>("Simple.txt", false, 0, 3);
			fail("Calling LookupTableQuantityConverter with invalid columns should throw exception");
		} catch (Exception e) {
			final String msg = e.getMessage();
			assertEquals(
					"LookupTableQuantityConverter.LookupTableQuantityConverter: Error accessing data from ColumnDataFile - check the column indices are correct. LookupTableQuantityConverter using details in Simple.txt. sColumn=0 tColumn=3 mode = BOTH_DIRECTIONS",
					msg);
		}
	}

	@Test
	public final void toSource() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt", false, 0, 1);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);
		final Quantity<Length> source = converter.toSource(targetBeforeConversion);
		final Quantity<Length> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetAfterConversion.getValue().doubleValue(), targetBeforeConversion.getValue().doubleValue(), DELTA);
	}

	@Test
	public final void getMode() {
		final LookupTableQuantityConverter.Mode mode_Both = LookupTableQuantityConverter.getMode(LookupTableQuantityConverter.Mode_Both);
		assertEquals(LookupTableQuantityConverter.Mode.BOTH_DIRECTIONS, mode_Both);

		final LookupTableQuantityConverter.Mode mode_TtoS = LookupTableQuantityConverter.getMode(LookupTableQuantityConverter.Mode_TtoS);
		assertEquals(LookupTableQuantityConverter.Mode.T_TO_S_ONLY, mode_TtoS);

		final LookupTableQuantityConverter.Mode mode_StoT = LookupTableQuantityConverter.getMode(LookupTableQuantityConverter.Mode_StoT);
		assertEquals(LookupTableQuantityConverter.Mode.S_TO_T_ONLY, mode_StoT);

		try {
			LookupTableQuantityConverter.getMode("Bad");
			fail("Calling LookupTableQuantityConverter.getMode() with bad mode should fail");
		} catch (IllegalArgumentException e) {
			final String msg = e.getMessage();
			assertEquals("LookupTableQuantityConverter.getMode. Mode is invalid - Bad", msg);
		}

		try {
			LookupTableQuantityConverter.getMode(null);
			fail("Calling LookupTableQuantityConverter.getMode() with null mode should fail");
		} catch (IllegalArgumentException e) {
			final String msg = e.getMessage();
			assertEquals("LookupTableQuantityConverter.getMode. modeString is null", msg);
		}
	}

	@Test(expected = UnsupportedConversionException.class)
	public final void testModeToTargetOnly() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt",
				false, 0, 1, LookupTableQuantityConverter.Mode.S_TO_T_ONLY);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);

		// Should not be able to do T→S conversion with converter that only supports S→T conversion
		converter.toSource(targetBeforeConversion);
	}

	@Test
	public final void DuplicateSourceValuesIncreasing() throws Exception {
		final LookupTableQuantityConverter<Energy, Energy> converter = new LookupTableQuantityConverter<>(
				"DuplicateSourceValuesIncreasing.txt", false, 0, 1, LookupTableQuantityConverter.Mode.S_TO_T_ONLY);
		final Unit<Energy> acceptableSourceUnits = getAcceptableSourceUnits(converter);

		Quantity<Energy> source = QuantityFactory.createFromObject(2.5, acceptableSourceUnits);
		Quantity<Energy> target = converter.toTarget(source);
		assertEquals(2.5, target.getValue().doubleValue(), 1E-10);

		source = QuantityFactory.createFromObject(3.5, acceptableSourceUnits);
		target = converter.toTarget(source);
		assertEquals(1.5, target.getValue().doubleValue(), 1E-10);
	}

	@Test
	public final void DuplicateSourceValuesDecreasing() throws Exception {
		final LookupTableQuantityConverter<Energy, Energy> converter = new LookupTableQuantityConverter<>(
				"DuplicateSourceValuesDecreasing.txt", false, 0, 1, LookupTableQuantityConverter.Mode.S_TO_T_ONLY);
		final Unit<Energy> acceptableSourceUnits = getAcceptableSourceUnits(converter);

		Quantity<Energy> source = QuantityFactory.createFromObject(2.5, acceptableSourceUnits);
		Quantity<Energy> target = converter.toTarget(source);
		assertEquals(2.5, target.getValue().doubleValue(),1E-10);

		source = QuantityFactory.createFromObject(3.6, acceptableSourceUnits);
		target = converter.toTarget(source);
		assertEquals(1.4, target.getValue().doubleValue(),1E-10);
	}

	/**
	 * Out of order meaning one ascending while other descending. sourceMinIsTargetMax should be true
	 *
	 * @throws Exception
	 */
	@Test
	public final void testOutOfOrder() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("OutOfOrder.txt", false, 0, 1);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion = QuantityFactory.createFromObject(5.0, acceptableTargetUnits);

		final Quantity<Length> source = converter.toSource(targetBeforeConversion);
		final Quantity<Length> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetBeforeConversion, targetAfterConversion);
		assertEquals(true, converter.sourceMinIsTargetMax());
	}

	/**
	 * Descending target with a duplicate value
	 *
	 * @throws Exception
	 */
	@Test
	public final void testOutOfOrderDuplicateTargetValues() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("OutOfOrderDuplicateAndDecreasingTargetValues.txt", false, 0, 1);
		final Unit<Length> acceptableSourceUnits = getAcceptableSourceUnits(converter);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion1 = QuantityFactory.createFromObject(-5, acceptableTargetUnits);
		final Quantity<Length> targetBeforeConversion2 = QuantityFactory.createFromObject(-105, acceptableTargetUnits);

		final Quantity<Length> source1 = converter.toSource(targetBeforeConversion1);
		final Quantity<Length> source2 = converter.toSource(targetBeforeConversion2);
		assertEquals(QuantityFactory.createFromObject(0.25, acceptableSourceUnits), source1);
		assertEquals(QuantityFactory.createFromObject(1.5, acceptableSourceUnits), source2);
		assertEquals(true, converter.sourceMinIsTargetMax());
	}

	/**
	 * Test similar situation to that on I24 with descending source with a duplicate value
	 *
	 * @throws Exception
	 */
	@Test
	public final void testOutOfOrderDuplicateSourceValues() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("OutOfOrderDuplicateAndDecreasingSourceValues.txt", false, 0, 1);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Unit<Length> acceptableSourceUnits = getAcceptableSourceUnits(converter);
		final Quantity<Length> targetBeforeConversion1 = QuantityFactory.createFromObject(-5, acceptableTargetUnits);
		final Quantity<Length> targetBeforeConversion2 = QuantityFactory.createFromObject(4.5, acceptableTargetUnits);

		final Quantity<Length> source1 = converter.toSource(targetBeforeConversion1);
		final Quantity<Length> source2 = converter.toSource(targetBeforeConversion2);
		assertEquals(QuantityFactory.createFromObject(0.25, acceptableSourceUnits), source1);
		assertEquals(-0.25, source2.getValue().doubleValue(), DELTA); //using quantities gives an error for identical values
		assertEquals(true, converter.sourceMinIsTargetMax());
	}

	/**
	 * Test whether a non-ordered source causes the expected exception
	 */
	@SuppressWarnings("unused")
	@Test
	public final void testNotInOrder() {
		try {
			new LookupTableQuantityConverter<Length, Length>("NotInOrder.txt", false, 0, 1);
			fail("Creating LookupTableQuantityConverter with data out of order should fail");
		} catch (Exception e) {
			assertEquals("LookupTableQuantityConverter.LookupTableQuantityConverter: InterpolationFunction. xValues must be increasing or decreasing", e.getMessage());
		}
	}

	@Test
	public final void testReal() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt", false, 0, 1);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);

		final Quantity<Length> source = converter.toSource(targetBeforeConversion);
		final Quantity<Length> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetBeforeConversion.getValue().doubleValue(), targetAfterConversion.getValue().doubleValue(), DELTA);
	}

	/**
	 * real case seen on I04
	 *
	 * @throws Exception
	 */
	@Test
	public final void testI04Pitch() throws Exception {
		final LookupTableQuantityConverter<Angle, Angle> converter = new LookupTableQuantityConverter<>(
				"BeamLineEnergy_DCM_Pitch_converter.txt", false, 0, 1);
		final Unit<Angle> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Angle> targetBeforeConversion = QuantityFactory.createFromObject(-.77, acceptableTargetUnits);

		final Unit<Angle> acceptableSourceUnits = QuantityFactory.createUnitFromString(converter.getAcceptableSourceUnits().get(0));
		final Quantity<Angle> source = converter.toSource(targetBeforeConversion);
		final Quantity<Angle> expectedSource = QuantityFactory.createFromObject(14.63, acceptableSourceUnits);
		assertEquals(Math.round(expectedSource.getValue().doubleValue() * 1E+6) / 1E+6,
				Math.round(source.getValue().doubleValue() * 1E+6) / 1E+6, DELTA);
	}

	@Test
	public final void testCoupled() throws Exception {
		final LookupTableQuantityConverter<Angle, Length> converterTarget = new LookupTableQuantityConverter<>("DegToAngstrom.txt", false, 0, 1);
		final LookupTableQuantityConverter<Length, Angle> converterSource = new LookupTableQuantityConverter<>("mmToDeg.txt", false, 0, 1);
		final CoupledQuantityConverter<Length, Length, Angle> converter = new CoupledQuantityConverter<>(converterSource, converterTarget);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);

		final Quantity<Length> source = converter.toSource(targetBeforeConversion);
		// target in Angstrom, source should be in mm
		final Quantity<Length> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetBeforeConversion.getValue().doubleValue(), targetAfterConversion.getValue().doubleValue(), DELTA);
	}

	/**
	 * test to see effect of the rounding in the LookupTableQuantityConverter - prevents round trip. We need to remove
	 * the rounding.
	 *
	 * @throws Exception
	 */
	@Test
	public final void testCoupledHighFactor() throws Exception {
		final LookupTableQuantityConverter<Angle, Length> converterTarget = new LookupTableQuantityConverter<>("DegToAngstromHighFactor.txt", false, 0, 1);
		final LookupTableQuantityConverter<Length, Angle> converterSource = new LookupTableQuantityConverter<>("mmToDegHighFactor.txt", false, 0, 1);
		final CoupledQuantityConverter<Length, Length, Angle> converter = new CoupledQuantityConverter<>(converterSource, converterTarget);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);

		final Quantity<Length> source = converter.toSource(targetBeforeConversion); // target
		// in Angstrom, source should be in mm
		final Quantity<Length> targetAfterConversion = converter.toTarget(source);
		assertEquals(targetBeforeConversion.getValue().doubleValue(),
				Math.round(targetAfterConversion.getValue().doubleValue() * 1E+6) / 1E+6, DELTA);
	}

	@SuppressWarnings("unused")
	@Test
	public final void testBadCoupled() {
		final LookupTableQuantityConverter<Length, Length> converterTarget = new LookupTableQuantityConverter<>("Simple.txt", false, 0, 1);
		final LookupTableQuantityConverter<Length, Length> converterSource = new LookupTableQuantityConverter<>("Simple.txt", false, 0, 1);
		try {
			new CoupledQuantityConverter<Length, Length, Length>(converterSource, converterTarget);
			fail("Creating CoupledQuantityConverter with incompatible units should throw exception");
		} catch (IllegalArgumentException e) {
			final String msg = e.getMessage();
			assertEquals(
					"CoupledQuantityConverter.CoupledQuantityConverter: Error target unit (mm)\n"
							+ " of converter (LookupTableQuantityConverter using details in Simple.txt. sColumn=0 tColumn=1 mode = BOTH_DIRECTIONS)\n"
							+ " does not match source units (Ang)\n"
							+ " of converter (LookupTableQuantityConverter using details in Simple.txt. sColumn=0 tColumn=1 mode = BOTH_DIRECTIONS)",
					msg);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(expected = InvalidUnitsException.class)
	public final void testIncompatibleUnits() throws Exception {
		final LookupTableQuantityConverter converter = new LookupTableQuantityConverter<>("mmToDeg.txt", false, 0, 1);
		final LookupTableQuantityConverter dummyToGetUnits = new LookupTableQuantityConverter<>("DegToAngstrom.txt", false, 0, 1);
		final Unit acceptableTargetUnits = getAcceptableTargetUnits(dummyToGetUnits);
		final Quantity targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);

		// Should not be able to do conversion using invalid units
		converter.toSource(targetBeforeConversion);
	}

	@Test
	public final void testCompatibleUnitsConvert() throws Exception {
		final LookupTableQuantityConverter<Length, Angle> converter = new LookupTableQuantityConverter<>("mmToDeg.txt", false, 0, 1);
		final Unit<Length> unit = QuantityFactory.createUnitFromString("Angstrom");
		// 1 angstrom == 1e-7 mm
		final Quantity<Length> toConvert = QuantityFactory.createFromObject(1e4, unit);
		final Quantity<Angle> result = converter.toTarget(toConvert);
		assertEquals(1e-3, result.getValue().doubleValue(), 1e-9);
	}

	@Test
	public final void testUnity() throws Exception {
		final LookupTableQuantityConverter<Length, Dimensionless> converter = new LookupTableQuantityConverter<>("mmToUnity.txt", false, 0, 1);
		final Unit<Dimensionless> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Dimensionless> targetBeforeConversion = QuantityFactory.createFromObject(1.0, acceptableTargetUnits);

		final Quantity<Length> back = converter.toSource(targetBeforeConversion);
		final LookupTableQuantityConverter<Dimensionless, Length> converterBack = new LookupTableQuantityConverter<>("mmToUnity.txt", false, 1, 0);
		final Quantity<Dimensionless> backAgain = converterBack.toSource(back);
		assertEquals(targetBeforeConversion, backAgain);
	}

	@Test
	public final void testSourceMinIsTargetMax() {
		assertEquals(false, new LookupTableQuantityConverter<Length, Length>("Simple.txt", false, 0, 1).sourceMinIsTargetMax());
		assertEquals(true, new LookupTableQuantityConverter<Length, Length>("SourceMinIsTargetMax.txt", false, 0, 1)
				.sourceMinIsTargetMax());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testOutOfRangeToTarget() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt",
				false, 0, 1, Mode.S_TO_T_ONLY, false);
		final Unit<Length> acceptableSourceUnits = getAcceptableSourceUnits(converter);
		final Quantity<Length> sourceQuantity = QuantityFactory.createFromObject(3.0, acceptableSourceUnits);

		// IllegalArgumentException expected when converting value out of range when not allowed to extrapolate
		converter.toTarget(sourceQuantity);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testOutOfRangeToSource() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt",
				false, 0, 1, Mode.T_TO_S_ONLY, false);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetQuantity = QuantityFactory.createFromObject(300.0, acceptableTargetUnits);

		// IllegalArgumentException expected when converting value out of range when not allowed to extrapolate
		converter.toSource(targetQuantity);
	}

	@Test
	public final void testExtrapolatedToTarget() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt", false, 0, 1, Mode.S_TO_T_ONLY, true);
		final Unit<Length> acceptableSourceUnits = getAcceptableSourceUnits(converter);
		final Quantity<Length> sourceQuantity = QuantityFactory.createFromObject(3.0, acceptableSourceUnits);
		final Quantity<Length> targetQuantity = converter.toTarget(sourceQuantity);
		assertEquals(300.0, targetQuantity.getValue().doubleValue(), DELTA);
	}

	@Test
	public final void testExtrapolatedToSource() throws Exception {
		final LookupTableQuantityConverter<Length, Length> converter = new LookupTableQuantityConverter<>("Simple.txt", false, 0, 1, Mode.T_TO_S_ONLY, true);
		final Unit<Length> acceptableTargetUnits = getAcceptableTargetUnits(converter);
		final Quantity<Length> targetQuantity = QuantityFactory.createFromObject(300.0, acceptableTargetUnits);
		final Quantity<Length> sourceQuantity = converter.toSource(targetQuantity);
		assertEquals(3.0, sourceQuantity.getValue().doubleValue(), DELTA);
	}

	private <S extends Quantity<S>, T extends Quantity<T>> Unit<T> getAcceptableTargetUnits(final IQuantityConverter<S, T> converter) {
		return QuantityFactory.createUnitFromString(converter.getAcceptableTargetUnits().get(0));
	}

	private <S extends Quantity<S>, T extends Quantity<T>> Unit<S> getAcceptableSourceUnits(final IQuantityConverter<S, T> converter) {
		return QuantityFactory.createUnitFromString(converter.getAcceptableSourceUnits().get(0));
	}
}
