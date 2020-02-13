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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static si.uom.NonSI.ANGSTROM;
import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.JOULE;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.RADIAN;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.Test;

import tec.units.indriya.quantity.Quantities;

public class WavelengthTest {
	private static final Quantity<Energy> PHOTON_ENERGY = Quantities.getQuantity(2.34, ELECTRON_VOLT);
	private static final Quantity<Energy> NEGATIVE_ENERGY = Quantities.getQuantity(-1.0, ELECTRON_VOLT);
	private static final Quantity<Length> TWO_D = Quantities.getQuantity(6.271, ANGSTROM);
	private static final Quantity<Angle> BRAGG_ANGLE = Quantities.getQuantity(12787.5, MILLI(DEGREE_ANGLE));
	private static final Quantity<Length> NEGATIVE_LENGTH = Quantities.getQuantity(-1.0, ANGSTROM);
	private static final Quantity<Angle> NEGATIVE_ANGLE = Quantities.getQuantity(-1.0, RADIAN);

	// -------------------------------------------------------------------------------
	// Wavelength from energy
	// -------------------------------------------------------------------------------
	@Test
	public void testWavelengthOfEnergy() {
		final Quantity<Length> expected = Quantities.getQuantity(529.847, NANO(METRE));
		final Quantity<Length> result = QuantityConverters.wavelengthOf(PHOTON_ENERGY);
		assertEquals(expected.to(METRE).getValue().doubleValue(), result.to(METRE).getValue().doubleValue(), 0.0000000000001);
	}

	@Test
	public void testWavelengthOfEnergyNullEnergy() {
		assertNull(QuantityConverters.wavelengthOf(null));
	}

	@Test
	public void testWavelengthOfEnergyNegativeEnergy() {
		assertNull(QuantityConverters.wavelengthOf(NEGATIVE_ENERGY));
	}

	@Test
	public void testWavelengthOfEnergyZeroEnergy() {
		assertNull(QuantityConverters.wavelengthOf(Quantities.getQuantity(0, JOULE)));
	}

	// -------------------------------------------------------------------------------
	// Wavelength from Bragg angle
	// -------------------------------------------------------------------------------
	@Test
	public void testWavelengthOfAngle() {
		final Quantity<Length> expectedWavelength = Quantities.getQuantity(1.388, ANGSTROM);
		final Quantity<Length> result = QuantityConverters.wavelengthOf(BRAGG_ANGLE, TWO_D);
		assertEquals(expectedWavelength.to(METRE).getValue().doubleValue(), result.to(METRE).getValue().doubleValue(), 0.0000000000001);
	}

	@Test
	public void testWavelengthOfAngleNegativeLength() {
		assertNull(QuantityConverters.wavelengthOf(BRAGG_ANGLE, NEGATIVE_LENGTH));
	}

	@Test
	public void testWavelengthOfAngleNegativeAngle() {
		assertNull(QuantityConverters.wavelengthOf(NEGATIVE_ANGLE, TWO_D));
	}

	@Test
	public void testWavelengthOfAngleNullLength() {
		assertNull(QuantityConverters.wavelengthOf(BRAGG_ANGLE, null));
	}

	@Test
	public void testWavelengthOfAngleNullAngle() {
		assertNull(QuantityConverters.wavelengthOf(null, TWO_D));
	}

	@Test
	public void testWavelengthOfAngleZeroAngle() {
		assertNull(QuantityConverters.wavelengthOf(Quantities.getQuantity(0, RADIAN), TWO_D));
	}

	@Test
	public void testWavelengthOfAngleZeroLength() {
		assertNull(QuantityConverters.wavelengthOf(BRAGG_ANGLE, Quantities.getQuantity(0, METRE)));
	}
}
