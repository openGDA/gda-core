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

package gda.jscience.physics.quantities;

import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

import gda.jscience.physics.units.NonSIext;

public class WaveVectorTest {
	private static final Amount<Energy> EDGE_ENERGY = Amount.valueOf(1.4, ELECTRON_VOLT);
	private static final Amount<Energy> ELECTRON_ENERGY = Amount.valueOf(7.23, ELECTRON_VOLT);
	private static final Amount<Energy> NEGATIVE_ENERGY = Amount.valueOf(-0.01, ELECTRON_VOLT);

	@Test
	public void testWaveVectorFromEnergies() {
		final Amount<WaveVector> result = QuantityConverters.waveVectorOf(EDGE_ENERGY, ELECTRON_ENERGY);
		assertEquals(1.237, result.getEstimatedValue(), 0.0001);
		assertEquals(NonSIext.PER_ANGSTROM, result.getUnit());
	}

	@Test
	public void testWaveVectorNullEdgeEnergy() {
		assertNull(QuantityConverters.waveVectorOf(null, ELECTRON_ENERGY));
	}

	@Test
	public void testWaveVectorNullElectronEnergy() {
		assertNull(QuantityConverters.waveVectorOf(EDGE_ENERGY, null));
	}

	@Test
	public void testWaveVectorNegativeEdgeEnergy() {
		assertNull(QuantityConverters.waveVectorOf(NEGATIVE_ENERGY, ELECTRON_ENERGY));
	}

	@Test
	public void testWaveVectorNegativeElectronEnergy() {
		assertNull(QuantityConverters.waveVectorOf(EDGE_ENERGY, NEGATIVE_ENERGY));
	}

	@Test
	public void testWaveVectorZeroEdgeEnergy() {
		assertNull(QuantityConverters.waveVectorOf(ZERO_ENERGY, ELECTRON_ENERGY));
	}

	@Test
	public void testWaveVectorZeroElectronEnergy() {
		assertNull(QuantityConverters.waveVectorOf(EDGE_ENERGY, ZERO_ENERGY));
	}
}
