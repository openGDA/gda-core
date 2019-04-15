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

import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ANGLE;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_LENGTH;
import static javax.measure.unit.NonSI.ANGSTROM;
import static javax.measure.unit.NonSI.DEGREE_ANGLE;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.NANO;
import static javax.measure.unit.SI.RADIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

public class WavelengthTest {
	private static final Amount<Energy> PHOTON_ENERGY = Amount.valueOf(2.34, ELECTRON_VOLT);
	private static final Amount<Energy> NEGATIVE_ENERGY = Amount.valueOf(-1.0, ELECTRON_VOLT);
	private static final Amount<Length> TWO_D = Amount.valueOf(6.271, ANGSTROM);
	private static final Amount<Angle> BRAGG_ANGLE = Amount.valueOf(12787.5, SI.MILLI(DEGREE_ANGLE));
	private static final Amount<Length> NEGATIVE_LENGTH = Amount.valueOf(-1.0, ANGSTROM);
	private static final Amount<Angle> NEGATIVE_ANGLE = Amount.valueOf(-1.0, RADIAN);

	// -------------------------------------------------------------------------------
	// Wavelength from energy
	// -------------------------------------------------------------------------------
	@Test
	public void testWavelengthOfEnergy() {
		final Amount<Length> expected = Amount.valueOf(529.847, NANO(METER));
		final Amount<Length> result = Wavelength.wavelengthOf(PHOTON_ENERGY);
		assertEquals(expected.doubleValue(Length.UNIT), result.doubleValue(Length.UNIT), 0.0000000000001);
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
		assertNull(Wavelength.wavelengthOf(ZERO_ENERGY));
	}

	// -------------------------------------------------------------------------------
	// Wavelength from Bragg angle
	// -------------------------------------------------------------------------------
	@Test
	public void testWavelengthOfAngle() {
		final Amount<Length> expectedWavelength = Amount.valueOf(1.388, ANGSTROM);
		final Amount<Length> result = Wavelength.wavelengthOf(BRAGG_ANGLE, TWO_D);
		assertEquals(expectedWavelength.doubleValue(Length.UNIT), result.doubleValue(Length.UNIT), 0.0000000000001);
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
		assertNull(Wavelength.wavelengthOf(ZERO_ANGLE, TWO_D));
	}

	@Test
	public void testWavelengthOfAngleZeroLength() {
		assertNull(Wavelength.wavelengthOf(BRAGG_ANGLE, ZERO_LENGTH));
	}
}
