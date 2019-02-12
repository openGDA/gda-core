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

import static org.jscience.physics.units.NonSI.ANGSTROM;
import static org.jscience.physics.units.NonSI.DEGREE_ANGLE;
import static org.jscience.physics.units.NonSI.ELECTRON_VOLT;
import static org.jscience.physics.units.SI.MILLI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test suite for {@link BraggAngle} class
 */
public class BraggAngleTest {
	private static final Length WAVELENGTH = Quantity.valueOf(1.388, ANGSTROM);
	private static final Length TWO_D = Quantity.valueOf(6.271, ANGSTROM);
	private static final Energy PHOTON_ENERGY = Quantity.valueOf(8.9326e+3, ELECTRON_VOLT);
	private static final Length NEGATIVE_LENGTH = Quantity.valueOf(-0.01, ANGSTROM);
	private static final Energy NEGATIVE_ENERGY = Quantity.valueOf(-0.01, ELECTRON_VOLT);

	//------------------------------------------------------------------------------------------
	// Bragg angle from wavelength
	//------------------------------------------------------------------------------------------
	@Test
	public void testBraggAngleFromWavelength() {
		final Angle expectedAngle = Quantity.valueOf(12787.5, MILLI(DEGREE_ANGLE));
		final Angle result = BraggAngle.braggAngleOf(WAVELENGTH, TWO_D);
		assertEquals(expectedAngle.doubleValue(), result.doubleValue(), 0.000001);
	}

	@Ignore
	@Test
	public void testBraggAngleFromWavelengthNullWavelength() {
		// Cannot currently be run, as type erasure makes the call ambiguous
		// Reinstate after update to jscience4
		//assertNull(BraggAngle.braggAngleOf(null, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthNullTwoD() {
		assertNull(BraggAngle.braggAngleOf(WAVELENGTH, null));
	}

	@Test
	public void testBraggAngleFromWavelengthZeroWavelength() {
		assertNull(BraggAngle.braggAngleOf(Length.ZERO, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthZeroTwoD() {
		assertNull(BraggAngle.braggAngleOf(WAVELENGTH, Length.ZERO));
	}

	@Test
	public void testBraggAngleFromWavelengthNegativeWavelength() {
		assertNull(BraggAngle.braggAngleOf(NEGATIVE_LENGTH, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthNegativeTwoD() {
		assertNull(BraggAngle.braggAngleOf(WAVELENGTH, NEGATIVE_LENGTH));
	}

	//------------------------------------------------------------------------------------------
	// Bragg angle from photon energy
	//------------------------------------------------------------------------------------------
	@Test
	public void testBraggAngleFromPhotonEnergy() {
		final Angle expectedAngle = Quantity.valueOf(12787.5, MILLI(DEGREE_ANGLE));
		final Angle result = BraggAngle.braggAngleOf(PHOTON_ENERGY, TWO_D);
		assertEquals(expectedAngle.doubleValue(), result.doubleValue(), 0.000001);
	}

	@Ignore
	@Test
	public void testBraggAngleFromPhotonEnergyNullEnergy() {
		// Cannot currently be run, as type erasure makes the call ambiguous
		// Reinstate after update to jscience4
		//assertNull(BraggAngle.braggAngleOf(null, twoD));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNullTwoD() {
		assertNull(BraggAngle.braggAngleOf(PHOTON_ENERGY, null));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyZeroEnergy() {
		assertNull(BraggAngle.braggAngleOf(Energy.ZERO, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyZeroTwoD() {
		assertNull(BraggAngle.braggAngleOf(PHOTON_ENERGY, Length.ZERO));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNegativeEnergy() {
		assertNull(BraggAngle.braggAngleOf(NEGATIVE_ENERGY, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNegativeTwoD() {
		assertNull(BraggAngle.braggAngleOf(PHOTON_ENERGY, NEGATIVE_LENGTH));
	}
}
