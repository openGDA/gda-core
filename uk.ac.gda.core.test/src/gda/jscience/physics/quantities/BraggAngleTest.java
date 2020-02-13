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
import static si.uom.SI.ELECTRON_VOLT;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.JOULE;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.RADIAN;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.Test;

import tec.units.indriya.quantity.Quantities;

/**
 * Test suite for Bragg Angle conversion functions
 */
public class BraggAngleTest {
	private static final Quantity<Length> WAVELENGTH = Quantities.getQuantity(1.388, ANGSTROM);
	private static final Quantity<Length> TWO_D = Quantities.getQuantity(6.271, ANGSTROM);
	private static final Quantity<Energy> PHOTON_ENERGY = Quantities.getQuantity(8.9326e+3, ELECTRON_VOLT);
	private static final Quantity<Length> NEGATIVE_LENGTH = Quantities.getQuantity(-0.01, ANGSTROM);
	private static final Quantity<Energy> NEGATIVE_ENERGY = Quantities.getQuantity(-0.01, ELECTRON_VOLT);
	private static final Quantity<Length> ZERO_LENGTH = Quantities.getQuantity(0, METRE);
	private static final Quantity<Energy> ZERO_ENERGY = Quantities.getQuantity(0, JOULE);

	//------------------------------------------------------------------------------------------
	// Bragg angle from wavelength
	//------------------------------------------------------------------------------------------
	@Test
	public void testBraggAngleFromWavelength() {
		final Quantity<Angle> expectedAngle = Quantities.getQuantity(12787.5, MILLI(DEGREE_ANGLE));
		final Quantity<Angle> result = QuantityConverters.braggAngleFromWavelength(WAVELENGTH, TWO_D);
		assertEquals(expectedAngle.to(RADIAN).getValue().doubleValue(), result.to(RADIAN).getValue().doubleValue(), 0.000001);
	}

	@Test
	public void testBraggAngleFromWavelengthNullWavelength() {
		assertNull(QuantityConverters.braggAngleFromWavelength(null, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthNullTwoD() {
		assertNull(QuantityConverters.braggAngleFromWavelength(WAVELENGTH, null));
	}

	@Test
	public void testBraggAngleFromWavelengthZeroWavelength() {
		assertNull(QuantityConverters.braggAngleFromWavelength(ZERO_LENGTH, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthZeroTwoD() {
		assertNull(QuantityConverters.braggAngleFromWavelength(WAVELENGTH, ZERO_LENGTH));
	}

	@Test
	public void testBraggAngleFromWavelengthNegativeWavelength() {
		assertNull(QuantityConverters.braggAngleFromWavelength(NEGATIVE_LENGTH, TWO_D));
	}

	@Test
	public void testBraggAngleFromWavelengthNegativeTwoD() {
		assertNull(QuantityConverters.braggAngleFromWavelength(WAVELENGTH, NEGATIVE_LENGTH));
	}

	//------------------------------------------------------------------------------------------
	// Bragg angle from photon energy
	//------------------------------------------------------------------------------------------
	@Test
	public void testBraggAngleFromPhotonEnergy() {
		final Quantity<Angle> expectedAngle = Quantities.getQuantity(12787.5, MILLI(DEGREE_ANGLE));
		final Quantity<Angle> result = QuantityConverters.braggAngleFromEnergy(PHOTON_ENERGY, TWO_D);
		assertEquals(expectedAngle.to(RADIAN).getValue().doubleValue(), result.to(RADIAN).getValue().doubleValue(), 0.00001);
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNullEnergy() {
		assertNull(QuantityConverters.braggAngleFromEnergy(null, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNullTwoD() {
		assertNull(QuantityConverters.braggAngleFromEnergy(PHOTON_ENERGY, null));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyZeroEnergy() {
		assertNull(QuantityConverters.braggAngleFromEnergy(ZERO_ENERGY, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyZeroTwoD() {
		assertNull(QuantityConverters.braggAngleFromEnergy(PHOTON_ENERGY, ZERO_LENGTH));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNegativeEnergy() {
		assertNull(QuantityConverters.braggAngleFromEnergy(NEGATIVE_ENERGY, TWO_D));
	}

	@Test
	public void testBraggAngleFromPhotonEnergyNegativeTwoD() {
		assertNull(QuantityConverters.braggAngleFromEnergy(PHOTON_ENERGY, NEGATIVE_LENGTH));
	}
}
