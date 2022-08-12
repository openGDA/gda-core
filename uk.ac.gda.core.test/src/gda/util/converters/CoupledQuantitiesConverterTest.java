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
import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tec.units.indriya.quantity.Quantities;

public class CoupledQuantitiesConverterTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 1e-9;

	private static final double SOURCE_TO_INTERMEDIATE_FACTOR = 2.0;
	private static final double INTERMEDIATE_TO_TARGET_FACTOR = 3.0;

	private static final List<String> SOURCE_UNITS = Arrays.asList("mm", "um");
	private static final List<String> INTERMEDIATE_UNITS = Arrays.asList("deg", "mdeg");
	private static final List<String> TARGET_UNITS = Arrays.asList("eV", "keV");

	private CoupledQuantitiesConverter<Length, Energy, Angle> coupledConverter;

	@BeforeEach
	public void setUp() {
		coupledConverter = new CoupledQuantitiesConverter<>(new SourceToIntermediateConverter(), new IntermediateToTargetConverter());
	}

	@Test
	public void testToSource() throws Exception {
		final Quantity<Energy> targetEnergy = Quantities.getQuantity(73.9, ELECTRON_VOLT);
		final Quantity<Length> result = coupledConverter.toSource(targetEnergy);
		assertEquals(12.3166666667, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MILLI(METRE), result.getUnit());
	}

	@Test
	public void testToSourceMultiple() throws Exception {
		@SuppressWarnings("unchecked")
		final Quantity<Energy>[] targetEnergies = new Quantity[] { Quantities.getQuantity(73.9, ELECTRON_VOLT),
				Quantities.getQuantity(-0.34, ELECTRON_VOLT) };
		final Quantity<Length>[] result = coupledConverter.toSource(targetEnergies, null);

		assertEquals(12.3166666667, result[0].getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MILLI(METRE), result[0].getUnit());
		assertEquals(-0.0566666667, result[1].getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MILLI(METRE), result[1].getUnit());
	}

	@Test
	public void testToTarget() throws Exception {
		final Quantity<Length> sourceLength = Quantities.getQuantity(12.3167, MILLI(METRE));
		final Quantity<Energy> result = coupledConverter.toTarget(sourceLength);
		assertEquals(73.9002, result.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ELECTRON_VOLT, result.getUnit());
	}

	@Test
	public void testToTargetMultiple() throws Exception {
		@SuppressWarnings("unchecked")
		final Quantity<Length>[] sourceLengths = new Quantity[] { Quantities.getQuantity(12.3167, MILLI(METRE)),
				Quantities.getQuantity(7.23, MICRO(METRE)) };

		final Quantity<Energy>[] result = coupledConverter.calculateMoveables(sourceLengths, null);
		assertEquals(73.9002, result[0].getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ELECTRON_VOLT, result[0].getUnit());
		assertEquals(0.04338, result[1].getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(ELECTRON_VOLT, result[1].getUnit());
	}

	@Test
	public void testGetAcceptableUnits() {
		final List<List<String>> units = coupledConverter.getAcceptableUnits();
		assertEquals(1, units.size());
		assertEquals(Arrays.asList("mm", "um"), units.get(0));
	}

	@Test
	public void testGetMoveableUnits() {
		final List<List<String>> units = coupledConverter.getAcceptableMoveableUnits();
		assertEquals(1, units.size());
		assertEquals(Arrays.asList("eV", "keV"), units.get(0));
	}

	/**
	 * Convert {@link Length} to {@link Angle} with a constant factor
	 */
	private static class SourceToIntermediateConverter implements IQuantitiesConverter<Length, Angle> {

		@Override
		public Quantity<Length>[] toSource(Quantity<Angle>[] targets, Object[] moveables) throws Exception {
			@SuppressWarnings("unchecked")
			final Quantity<Length>[] result = new Quantity[targets.length];
			for (int i = 0; i < targets.length; i++) {
				final double numDegrees = targets[i].to(DEGREE_ANGLE).getValue().doubleValue();
				result[i] = Quantities.getQuantity(numDegrees / SOURCE_TO_INTERMEDIATE_FACTOR, MILLI(METRE));
			}
			return result;
		}

		@Override
		public Quantity<Angle>[] calculateMoveables(Quantity<Length>[] sources, Object[] moveables) throws Exception {
			@SuppressWarnings("unchecked")
			final Quantity<Angle>[] result = new Quantity[sources.length];
			for (int i = 0; i < sources.length; i++) {
				final double numMillis = sources[i].to(MILLI(METRE)).getValue().doubleValue();
				result[i] = Quantities.getQuantity(numMillis * SOURCE_TO_INTERMEDIATE_FACTOR, DEGREE_ANGLE);
			}
			return result;
		}

		@Override
		public List<List<String>> getAcceptableUnits() {
			final List<List<String>> result = new ArrayList<>();
			result.add(SOURCE_UNITS);
			return result;
		}

		@Override
		public List<List<String>> getAcceptableMoveableUnits() {
			final List<List<String>> result = new ArrayList<>();
			result.add(INTERMEDIATE_UNITS);
			return result;
		}

		@Override
		public boolean sourceMinIsTargetMax() {
			return false;
		}

		@Override
		public Quantity<Length> toSource(Quantity<Angle> target) throws Exception {
			final double numDegrees = target.to(DEGREE_ANGLE).getValue().doubleValue();
			return Quantities.getQuantity(numDegrees / SOURCE_TO_INTERMEDIATE_FACTOR, MILLI(METRE));
		}

		@Override
		public Quantity<Angle> toTarget(Quantity<Length> source) throws Exception {
			final double numMillis = source.to(MILLI(METRE)).getValue().doubleValue();
			return Quantities.getQuantity(numMillis * SOURCE_TO_INTERMEDIATE_FACTOR, DEGREE_ANGLE);
		}

		@Override
		public List<String> getAcceptableSourceUnits() {
			return SOURCE_UNITS;
		}

		@Override
		public List<String> getAcceptableTargetUnits() {
			return INTERMEDIATE_UNITS;
		}

		@Override
		public boolean handlesStoT() {
			return true;
		}

		@Override
		public boolean handlesTtoS() {
			return true;
		}
	}

	/**
	 * Convert {@link Angle} to {@link Energy} with a constant factor
	 */
	private static class IntermediateToTargetConverter implements IQuantitiesConverter<Angle, Energy> {
		private List<String> sourceUnits = Arrays.asList("deg", "mdeg");
		private List<String> targetUnits = Arrays.asList("eV", "keV");

		@Override
		public Quantity<Angle>[] toSource(Quantity<Energy>[] targets, Object[] moveables) throws Exception {
			@SuppressWarnings("unchecked")
			final Quantity<Angle>[] result = new Quantity[targets.length];
			for (int i = 0; i < targets.length; i++) {
				final double numEv = targets[i].to(ELECTRON_VOLT).getValue().doubleValue();
				result[i] = Quantities.getQuantity(numEv / INTERMEDIATE_TO_TARGET_FACTOR, DEGREE_ANGLE);
			}
			return result;
		}

		@Override
		public Quantity<Energy>[] calculateMoveables(Quantity<Angle>[] sources, Object[] moveables) throws Exception {
			@SuppressWarnings("unchecked")
			final Quantity<Energy>[] result = new Quantity[sources.length];
			for (int i = 0; i < sources.length; i++) {
				final double numDegrees = sources[i].to(DEGREE_ANGLE).getValue().doubleValue();
				result[i] = Quantities.getQuantity(numDegrees * INTERMEDIATE_TO_TARGET_FACTOR, ELECTRON_VOLT);
			}
			return result;
		}

		@Override
		public List<List<String>> getAcceptableUnits() {
			final List<List<String>> result = new ArrayList<>();
			result.add(sourceUnits);
			return result;
		}

		@Override
		public List<List<String>> getAcceptableMoveableUnits() {
			final List<List<String>> result = new ArrayList<>();
			result.add(targetUnits);
			return result;
		}

		@Override
		public boolean sourceMinIsTargetMax() {
			return false;
		}

		@Override
		public Quantity<Angle> toSource(Quantity<Energy> target) throws Exception {
			final double numEv = target.to(ELECTRON_VOLT).getValue().doubleValue();
			return Quantities.getQuantity(numEv / INTERMEDIATE_TO_TARGET_FACTOR, DEGREE_ANGLE);
		}

		@Override
		public Quantity<Energy> toTarget(Quantity<Angle> source) throws Exception {
			final double numDegrees = source.to(DEGREE_ANGLE).getValue().doubleValue();
			return Quantities.getQuantity(numDegrees * INTERMEDIATE_TO_TARGET_FACTOR, ELECTRON_VOLT);
		}

		@Override
		public List<String> getAcceptableSourceUnits() {
			return INTERMEDIATE_UNITS;
		}

		@Override
		public List<String> getAcceptableTargetUnits() {
			return TARGET_UNITS;
		}

		@Override
		public boolean handlesStoT() {
			return true;
		}

		@Override
		public boolean handlesTtoS() {
			return true;
		}
	}
}
