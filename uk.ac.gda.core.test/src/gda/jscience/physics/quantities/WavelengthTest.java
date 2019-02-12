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
import static org.jscience.physics.units.SI.METER;
import static org.jscience.physics.units.SI.NANO;
import static org.jscience.physics.units.SI.RADIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.junit.Test;

public class WavelengthTest {
	private static final Energy PHOTON_ENERGY = Quantity.valueOf(2.34, ELECTRON_VOLT);
	private static final Energy NEGATIVE_ENERGY = Quantity.valueOf(-1.0, ELECTRON_VOLT);
	private static final Length TWO_D = Quantity.valueOf(6.271, ANGSTROM);
	private static final Angle BRAGG_ANGLE = Quantity.valueOf(12787.5, SI.MILLI(DEGREE_ANGLE));
	private static final Length NEGATIVE_LENGTH = Quantity.valueOf(-1.0, ANGSTROM);
	private static final Angle NEGATIVE_ANGLE = Quantity.valueOf(-1.0, RADIAN);

	// -------------------------------------------------------------------------------
	// Wavelength from energy
	// -------------------------------------------------------------------------------
	@Test
	public void testWavelengthOfEnergy() {
		final Length expected = Quantity.valueOf(529.847, NANO(METER));
		final Length result = Wavelength.wavelengthOf(PHOTON_ENERGY);
		assertEquals(expected.doubleValue(), result.doubleValue(), 0.0000000000001);
	}

	@Test
	public void testWavelengthOfEnergyNullEnergy() {
		assertNull(Wavelength.wavelengthOf(null));
	}

	@Test
	public void testWavelengthOfEnergyNegativeEnergy() {
		assertNull(Wavelength.wavelengthOf(NEGATIVE_ENERGY));
	}

	@Test
	public void testWavelengthOfEnergyZeroEnergy() {
		assertNull(Wavelength.wavelengthOf(Energy.ZERO));
	}

	// -------------------------------------------------------------------------------
	// Wavelength from Bragg angle
	// -------------------------------------------------------------------------------
	@Test
	public void testWavelengthOfAngle() {
		final Length expectedWavelength = Quantity.valueOf(1.388, ANGSTROM);
		final Length result = Wavelength.wavelengthOf(BRAGG_ANGLE, TWO_D);
		assertEquals(expectedWavelength.doubleValue(), result.doubleValue(), 0.0000000000001);
	}

	@Test
	public void testWavelengthOfAngleNegativeLength() {
		assertNull(Wavelength.wavelengthOf(BRAGG_ANGLE, NEGATIVE_LENGTH));
	}

	@Test
	public void testWavelengthOfAngleNegativeAngle() {
		assertNull(Wavelength.wavelengthOf(NEGATIVE_ANGLE, TWO_D));
	}

	@Test
	public void testWavelengthOfAngleNullLength() {
		assertNull(Wavelength.wavelengthOf(BRAGG_ANGLE, null));
	}

	@Test
	public void testWavelengthOfAngleNullAngle() {
		assertNull(Wavelength.wavelengthOf(null, TWO_D));
	}

	@Test
	public void testWavelengthOfAngleZeroAngle() {
		assertNull(Wavelength.wavelengthOf(Angle.ZERO, TWO_D));
	}

	@Test
	public void testWavelengthOfAngleZeroLength() {
		assertNull(Wavelength.wavelengthOf(BRAGG_ANGLE, Length.ZERO));
	}
}
