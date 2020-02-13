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

import static gda.jscience.physics.units.NonSIext.PER_ANGSTROM;
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
import javax.measure.UnconvertibleException;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.junit.Test;

import tec.units.indriya.quantity.Quantities;

public class PhotonEnergyTest {
	private static final Quantity<Length> WAVELENGTH = Quantities.getQuantity(529.847, NANO(METRE));
	private static final Quantity<Length> NEGATIVE_LENGTH = Quantities.getQuantity(-1.0, ANGSTROM);
	private static final Quantity<Length> TWO_D = Quantities.getQuantity(6.271, ANGSTROM);
	private static final Quantity<Angle> BRAGG_ANGLE = Quantities.getQuantity(12787.5, MILLI(DEGREE_ANGLE));
	private static final Quantity<Angle> NEGATIVE_ANGLE = Quantities.getQuantity(-1.0, RADIAN);

	private static final Quantity<Energy> EDGE = Quantities.getQuantity(8980.197, ELECTRON_VOLT);
	private static final Quantity<Energy> NEGATIVE_EDGE = Quantities.getQuantity(-1.0, ELECTRON_VOLT);
	private static final Quantity<Length> EDGE_LENGTH = Quantities.getQuantity(3.0, ANGSTROM);
	private static final Quantity<WaveVector> EDGE_LENGTH_VECTOR = vectorOf(EDGE_LENGTH);
	private static final Quantity<Length> NEGATIVE_EDGE_LENGTH = Quantities.getQuantity(-3.0, ANGSTROM);
	private static final Quantity<WaveVector> NEGATIVE_EDGE_LENGTH_VECTOR = vectorOf(NEGATIVE_EDGE_LENGTH);

	private static final Quantity<Length> ZERO_LENGTH = Quantities.getQuantity(0, METRE);
	private static final Quantity<Energy> ZERO_ENERGY = Quantities.getQuantity(0, JOULE);
	private static final Quantity<Angle> ZERO_ANGLE = Quantities.getQuantity(0, RADIAN);
	private static final Quantity<WaveVector> ZERO_VECTOR = Quantities.getQuantity(0, PER_ANGSTROM);

	private static final double DOUBLE_VALUE = 3.0E-10;
	private static final double NEGATIVE_DOUBLE_VALUE = -1.0;

	//------------------------------------------------------------------------------
	// Photon energy from wavelength
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfWavelength() {
		final Quantity<Energy> expectedEnergy = Quantities.getQuantity(2.34, ELECTRON_VOLT);
		final Quantity<Energy> result = QuantityConverters.photonEnergyFromWavelength(WAVELENGTH);
		assertEquals(expectedEnergy.to(JOULE).getValue().doubleValue(), result.to(JOULE).getValue().doubleValue(), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfWavelengthNegativeWavelength() {
		assertNull(QuantityConverters.photonEnergyFromWavelength(NEGATIVE_LENGTH));
	}

	@Test
	public void testPhotonEnergyOfWavelengthNullWavelength() {
		assertNull(QuantityConverters.photonEnergyFromWavelength(null));
	}

	@Test
	public void testPhotonEnergyOfWavelengthZeroWavelength() {
		assertNull(QuantityConverters.photonEnergyFromWavelength(ZERO_LENGTH));
	}

	//------------------------------------------------------------------------------
	// Photon energy from Bragg angle
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfAngle() {
		final Quantity<Energy> expectedEnergy = Quantities.getQuantity(8932.6, ELECTRON_VOLT);
		final Quantity<Energy> result = QuantityConverters.photonEnergyFromBraggAngle(BRAGG_ANGLE, TWO_D);
		assertEquals(expectedEnergy.to(JOULE).getValue().doubleValue(), result.to(JOULE).getValue().doubleValue(), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfAngleNegativeTwoD() {
		assertNull(QuantityConverters.photonEnergyFromBraggAngle(BRAGG_ANGLE, NEGATIVE_LENGTH));
	}

	@Test
	public void testPhotonEnergyOfAngleNegativeAngle() {
		assertNull(QuantityConverters.photonEnergyFromBraggAngle(NEGATIVE_ANGLE, TWO_D));
	}

	@Test
	public void testPhotonEnergyOfAngleNullTwoD() {
		assertNull(QuantityConverters.photonEnergyFromBraggAngle(BRAGG_ANGLE, null));
	}

	@Test
	public void testPhotonEnergyOfAngleNullAngle() {
		assertNull(QuantityConverters.photonEnergyFromBraggAngle(null, TWO_D));
	}

	@Test
	public void testPhotonEnergyOfAngleZeroAngle() {
		assertNull(QuantityConverters.photonEnergyFromBraggAngle(ZERO_ANGLE, TWO_D));
	}

	public void testPhotonEnergyOfAngleZeroTwoD() {
		assertNull(QuantityConverters.photonEnergyFromBraggAngle(BRAGG_ANGLE, ZERO_LENGTH));
	}

	//------------------------------------------------------------------------------
	// Photon energy from edge (with Quantity k)
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfEdgeVector() {
		final Quantity<Energy> expectedEnergy = Quantities.getQuantity(9020.0, ELECTRON_VOLT);
		final Quantity<Energy> result = QuantityConverters.photonEnergyFromEdgeAndVector(EDGE, EDGE_LENGTH_VECTOR);
		assertEquals(expectedEnergy.to(JOULE).getValue().doubleValue(), result.to(JOULE).getValue().doubleValue(), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNegativeVector() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndVector(EDGE, NEGATIVE_EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNegativeEdge() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndVector(NEGATIVE_EDGE, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNullVector() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndVector(EDGE, null));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNullEdge() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndVector(null, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorZeroEdge() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndVector(ZERO_ENERGY, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorZeroVector() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndVector(EDGE, ZERO_VECTOR));
	}

	//------------------------------------------------------------------------------
	// Photon energy from edge (with double k)
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfEdgeDouble() {
		final Quantity<Energy> expectedEnergy = Quantities.getQuantity(9014.0, ELECTRON_VOLT);
		final Quantity<Energy> result = QuantityConverters.photonEnergyFromEdgeAndValue(EDGE, DOUBLE_VALUE);
		assertEquals(expectedEnergy.to(JOULE).getValue().doubleValue(), result.to(JOULE).getValue().doubleValue(), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNegativeK() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndValue(EDGE, NEGATIVE_DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNegativeEdge() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndValue(NEGATIVE_EDGE, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNullEdge() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndValue(null, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleZeroEdge() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndValue(ZERO_ENERGY, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleZeroK() {
		assertNull(QuantityConverters.photonEnergyFromEdgeAndValue(EDGE, 0.0));
	}

	/**
	 * Returns the {@link Length} corresponding to the specified quantity.
	 *
	 * @param length
	 *            a quantity compatible with {@link Length}.
	 * @return the specified quantity or a new {@link Length} instance.
	 * @throws UnconvertibleException
	 *             if the current model does not allow the specified quantity to be converted to {@link Length}.
	 */
	public static Quantity<WaveVector> vectorOf(Quantity<Length> length) {
		return length.inverse().asType(WaveVector.class);
	}
}
