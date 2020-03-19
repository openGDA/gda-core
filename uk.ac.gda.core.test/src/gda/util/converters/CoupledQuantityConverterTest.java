/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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
import static org.junit.Assert.assertTrue;
import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.KILO;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;

import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.Before;
import org.junit.Test;

import tec.units.indriya.quantity.Quantities;

public class CoupledQuantityConverterTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 1e-9;

	private CoupledQuantityConverter<Length, Energy, Angle> coupledConverter;

	@Before
	public void setUp() {
		coupledConverter = new CoupledQuantityConverter<>(new SourceToIntermediateConverter(), new IntermediateToTargetConverter());
	}

	/*
	 * The constructor for CoupledQuantityConverter requires not only that the two converters have compatible
	 * intermediate units, but that the first one in each list is exactly the same. I'm not convinced that this is
	 * desirable, but for now this test documents the current behaviour.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIntermediateUnitsDifferentOrder() {
		final SourceToIntermediateConverter sourceConverter = new SourceToIntermediateConverter();
		sourceConverter.setTargetUnits(Arrays.asList("deg", "mdeg"));
		final IntermediateToTargetConverter targetConverter = new IntermediateToTargetConverter();
		targetConverter.setSourceUnits(Arrays.asList("mdeg", "deg"));
		coupledConverter = new CoupledQuantityConverter<>(sourceConverter, targetConverter);
	}

	@Test
	public void testGetAcceptableSourceUnits() {
		assertEquals(Arrays.asList("mm", "um"), coupledConverter.getAcceptableSourceUnits());
	}

	@Test
	public void testGetAcceptableTargetUnits() {
		assertEquals(Arrays.asList("eV", "keV"), coupledConverter.getAcceptableTargetUnits());
	}

	@Test
	public void testSourceMinIsTargetMax() {
		assertTrue(coupledConverter.sourceMinIsTargetMax());
	}

	@Test
	public void testToSourceEv() throws Exception {
		final Quantity<Energy> targetEnergy = Quantities.getQuantity(73.9, ELECTRON_VOLT);
		final Quantity<Length> result = coupledConverter.toSource(targetEnergy);
		assertEquals(12.3166666667, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MILLI(METRE), result.getUnit());
	}

	@Test
	public void testToSourceKev() throws Exception {
		final Quantity<Energy> targetEnergy = Quantities.getQuantity(73.9, KILO(ELECTRON_VOLT));
		final Quantity<Length> result = coupledConverter.toSource(targetEnergy);
		assertEquals(12316.6666666667, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MILLI(METRE), result.getUnit());
	}

	@Test
	public void testToTarget() throws Exception {
		final Quantity<Length> sourceLength = Quantities.getQuantity(12.3167, MILLI(METRE));
		final Quantity<Energy> result = coupledConverter.toTarget(sourceLength);
		assertEquals(73.9002, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ELECTRON_VOLT, result.getUnit());
	}

	@Test
	public void testToTargetMicrons() throws Exception {
		final Quantity<Length> sourceLength = Quantities.getQuantity(12316.7, MICRO(METRE));
		final Quantity<Energy> result = coupledConverter.toTarget(sourceLength);
		assertEquals(73.9002, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ELECTRON_VOLT, result.getUnit());
	}

	@Test
	public void testRoundTripConversion() throws Exception {
		final Quantity<Length> sourceLength = Quantities.getQuantity(12.3167, MILLI(METRE));
		final Quantity<Length> roundTripResult = coupledConverter.toSource(coupledConverter.toTarget(sourceLength));
		assertEquals(sourceLength.getValue().doubleValue(), roundTripResult.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(sourceLength.getUnit(), roundTripResult.getUnit());
	}

	/**
	 * Convert {@link Length} to {@link Angle} with a constant factor
	 */
	private static class SourceToIntermediateConverter implements IQuantityConverter<Length, Angle> {
		private static final double SOURCE_TO_TARGET_FACTOR = 2.0;
		private List<String> sourceUnits = Arrays.asList("mm", "um");
		private List<String> targetUnits = Arrays.asList("deg", "mdeg");

		@Override
		public Quantity<Length> toSource(Quantity<Angle> target) throws Exception {
			final double numDegrees = target.to(DEGREE_ANGLE).getValue().doubleValue();
			return Quantities.getQuantity(numDegrees / SOURCE_TO_TARGET_FACTOR, MILLI(METRE));
		}

		@Override
		public Quantity<Angle> toTarget(Quantity<Length> source) throws Exception {
			final double numMillis = source.to(MILLI(METRE)).getValue().doubleValue();
			return Quantities.getQuantity(numMillis * SOURCE_TO_TARGET_FACTOR, DEGREE_ANGLE);
		}

		@Override
		public boolean sourceMinIsTargetMax() {
			return false;
		}

		@Override
		public List<String> getAcceptableSourceUnits() {
			return sourceUnits;
		}

		@Override
		public List<String> getAcceptableTargetUnits() {
			return targetUnits;
		}

		@Override
		public boolean handlesStoT() {
			return true;
		}

		@Override
		public boolean handlesTtoS() {
			return true;
		}

		public void setTargetUnits(List<String> targetUnits) {
			this.targetUnits = targetUnits;
		}
	}

	/**
	 * Convert {@link Angle} to {@link Energy} with a constant factor
	 */
	private static class IntermediateToTargetConverter implements IQuantityConverter<Angle, Energy> {
		private static final double SOURCE_TO_TARGET_FACTOR = 3.0;
		private List<String> sourceUnits = Arrays.asList("deg", "mdeg");
		private List<String> targetUnits = Arrays.asList("eV", "keV");

		@Override
		public Quantity<Angle> toSource(Quantity<Energy> target) throws Exception {
			final double numEv = target.to(ELECTRON_VOLT).getValue().doubleValue();
			return Quantities.getQuantity(numEv / SOURCE_TO_TARGET_FACTOR, DEGREE_ANGLE);
		}

		@Override
		public Quantity<Energy> toTarget(Quantity<Angle> source) throws Exception {
			final double numDegrees = source.to(DEGREE_ANGLE).getValue().doubleValue();
			return Quantities.getQuantity(numDegrees * SOURCE_TO_TARGET_FACTOR, ELECTRON_VOLT);
		}

		@Override
		public boolean sourceMinIsTargetMax() {
			return true;
		}

		@Override
		public List<String> getAcceptableSourceUnits() {
			return sourceUnits;
		}

		@Override
		public List<String> getAcceptableTargetUnits() {
			return targetUnits;
		}

		@Override
		public boolean handlesStoT() {
			return true;
		}

		@Override
		public boolean handlesTtoS() {
			return true;
		}

		public void setSourceUnits(List<String> sourceUnits) {
			this.sourceUnits = sourceUnits;
		}
	}
}
