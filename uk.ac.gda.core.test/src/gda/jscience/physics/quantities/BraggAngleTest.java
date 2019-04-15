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

package gda.jscience.physics.quantities;

import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_LENGTH;
import static javax.measure.unit.NonSI.ANGSTROM;
import static javax.measure.unit.NonSI.DEGREE_ANGLE;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.SI.MILLI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

/**
 * Test suite for {@link BraggAngle} class
 */
public class BraggAngleTest {
	private static final Amount<Length> WAVELENGTH = Amount.valueOf(1.388, ANGSTROM);
	private static final Amount<Length> TWO_D = Amount.valueOf(6.271, ANGSTROM);
	private static final Amount<Energy> PHOTON_ENERGY = Amount.valueOf(8.9326e+3, ELECTRON_VOLT);
	private static final Amount<Length> NEGATIVE_LENGTH = Amount.valueOf(-0.01, ANGSTROM);
	private static final Amount<Energy> NEGATIVE_ENERGY = Amount.valueOf(-0.01, ELECTRON_VOLT);

	//------------------------------------------------------------------------------------------
	// Bragg angle from wavelength
	//------------------------------------------------------------------------------------------
	@Test
	public void testBraggAngleFromWavelength() {
		final Amount<Angle> expectedAngle = Amount.valueOf(12787.5, MILLI(DEGREE_ANGLE));
		final Amount<Angle> result = BraggAngle.braggAngleFromWavelength(WAVELENGTH, TWO_D);
		assertEquals(expectedAngle.doubleValue(Angle.UNIT), result.doubleValue(Angle.UNIT), 0.000001);
	}

	@Test
	public void testBraggAngleFromWavelengthNullWavelength() {
		assertNull(BraggAngle.braggAngleFromWavelength(null, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthNullTwoD() {
		assertNull(BraggAngle.braggAngleFromWavelength(WAVELENGTH, null));
	}

	@Test
	public void testBraggAngleFromWavelengthZeroWavelength() {
		assertNull(BraggAngle.braggAngleFromWavelength(ZERO_LENGTH, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthZeroTwoD() {
		assertNull(BraggAngle.braggAngleFromWavelength(WAVELENGTH, ZERO_LENGTH));
	}

	@Test
	public void testBraggAngleFromWavelengthNegativeWavelength() {
		assertNull(BraggAngle.braggAngleFromWavelength(NEGATIVE_LENGTH, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthNegativeTwoD() {
		assertNull(BraggAngle.braggAngleFromWavelength(WAVELENGTH, NEGATIVE_LENGTH));
	}

	//------------------------------------------------------------------------------------------
	// Bragg angle from photon energy
	//------------------------------------------------------------------------------------------
	@Test
	public void testBraggAngleFromPhotonEnergy() {
		final Amount<Angle> expectedAngle = Amount.valueOf(12787.5, MILLI(DEGREE_ANGLE));
		final Amount<Angle> result = BraggAngle.braggAngleFromEnergy(PHOTON_ENERGY, TWO_D);
		assertEquals(expectedAngle.doubleValue(Angle.UNIT), result.doubleValue(Angle.UNIT), 0.00001);
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNullEnergy() {
		assertNull(BraggAngle.braggAngleFromEnergy(null, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNullTwoD() {
		assertNull(BraggAngle.braggAngleFromEnergy(PHOTON_ENERGY, null));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyZeroEnergy() {
		assertNull(BraggAngle.braggAngleFromEnergy(ZERO_ENERGY, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyZeroTwoD() {
		assertNull(BraggAngle.braggAngleFromEnergy(PHOTON_ENERGY, ZERO_LENGTH));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNegativeEnergy() {
		assertNull(BraggAngle.braggAngleFromEnergy(NEGATIVE_ENERGY, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNegativeTwoD() {
		assertNull(BraggAngle.braggAngleFromEnergy(PHOTON_ENERGY, NEGATIVE_LENGTH));
	}
}
