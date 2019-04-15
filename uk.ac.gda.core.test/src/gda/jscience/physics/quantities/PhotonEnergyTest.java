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
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.NANO;
import static javax.measure.unit.SI.RADIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.measure.converter.ConversionException;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

public class PhotonEnergyTest {
	private static final Amount<Length> WAVELENGTH = Amount.valueOf(529.847, NANO(METER));
	private static final Amount<Length> NEGATIVE_LENGTH = Amount.valueOf(-1.0, ANGSTROM);
	private static final Amount<Length> TWO_D = Amount.valueOf(6.271, ANGSTROM);
	private static final Amount<Angle> BRAGG_ANGLE = Amount.valueOf(12787.5, MILLI(DEGREE_ANGLE));
	private static final Amount<Angle> NEGATIVE_ANGLE = Amount.valueOf(-1.0, RADIAN);

	private static final Amount<Energy> EDGE = Amount.valueOf(8980.197, ELECTRON_VOLT);
	private static final Amount<Energy> NEGATIVE_EDGE = Amount.valueOf(-1.0, ELECTRON_VOLT);
	private static final Amount<Length> EDGE_LENGTH = Amount.valueOf(3.0, ANGSTROM);
	private static final Amount<Vector> EDGE_LENGTH_VECTOR = vectorOf(EDGE_LENGTH);
	private static final Amount<Length> NEGATIVE_EDGE_LENGTH = Amount.valueOf(-3.0, ANGSTROM);
	private static final Amount<Vector> NEGATIVE_EDGE_LENGTH_VECTOR = vectorOf(NEGATIVE_EDGE_LENGTH);

	private static final double DOUBLE_VALUE = 3.0E-10;
	private static final double NEGATIVE_DOUBLE_VALUE = -1.0;

	//------------------------------------------------------------------------------
	// Photon energy from wavelength
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfWavelength() {
		final Amount<Energy> expectedEnergy = Amount.valueOf(2.34, ELECTRON_VOLT);
		final Amount<Energy> result = PhotonEnergy.photonEnergyFromWavelength(WAVELENGTH);
		assertEquals(expectedEnergy.doubleValue(Energy.UNIT), result.doubleValue(Energy.UNIT), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfWavelengthNegativeWavelength() {
		assertNull(PhotonEnergy.photonEnergyFromWavelength(NEGATIVE_LENGTH));
	}

	@Test
	public void testPhotonEnergyOfWavelengthNullWavelength() {
		assertNull(PhotonEnergy.photonEnergyFromWavelength(null));
	}

	@Test
	public void testPhotonEnergyOfWavelengthZeroWavelength() {
		assertNull(PhotonEnergy.photonEnergyFromWavelength(ZERO_LENGTH));
	}

	//------------------------------------------------------------------------------
	// Photon energy from Bragg angle
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfAngle() {
		final Amount<Energy> expectedEnergy = Amount.valueOf(8932.6, ELECTRON_VOLT);
		final Amount<Energy> result = PhotonEnergy.photonEnergyFromBraggAngle(BRAGG_ANGLE, TWO_D);
		assertEquals(expectedEnergy.doubleValue(Energy.UNIT), result.doubleValue(Energy.UNIT), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfAngleNegativeTwoD() {
		assertNull(PhotonEnergy.photonEnergyFromBraggAngle(BRAGG_ANGLE, NEGATIVE_LENGTH));
	}

	@Test
	public void testPhotonEnergyOfAngleNegativeAngle() {
		assertNull(PhotonEnergy.photonEnergyFromBraggAngle(NEGATIVE_ANGLE, TWO_D));
	}

	@Test
	public void testPhotonEnergyOfAngleNullTwoD() {
		assertNull(PhotonEnergy.photonEnergyFromBraggAngle(BRAGG_ANGLE, null));
	}

	@Test
	public void testPhotonEnergyOfAngleNullAngle() {
		assertNull(PhotonEnergy.photonEnergyFromBraggAngle(null, TWO_D));
	}

	@Test
	public void testPhotonEnergyOfAngleZeroAngle() {
		assertNull(PhotonEnergy.photonEnergyFromBraggAngle(ZERO_ANGLE, TWO_D));
	}

	public void testPhotonEnergyOfAngleZeroTwoD() {
		assertNull(PhotonEnergy.photonEnergyFromBraggAngle(BRAGG_ANGLE, ZERO_LENGTH));
	}

	//------------------------------------------------------------------------------
	// Photon energy from edge (with Quantity k)
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfEdgeVector() {
		final Amount<Energy> expectedEnergy = Amount.valueOf(9020.0, ELECTRON_VOLT);
		final Amount<Energy> result = PhotonEnergy.photonEnergyFromEdgeAndVector(EDGE, EDGE_LENGTH_VECTOR);
		assertEquals(expectedEnergy.doubleValue(Energy.UNIT), result.doubleValue(Energy.UNIT), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNegativeVector() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndVector(EDGE, NEGATIVE_EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNegativeEdge() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndVector(NEGATIVE_EDGE, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNullVector() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndVector(EDGE, null));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNullEdge() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndVector(null, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorZeroEdge() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndVector(ZERO_ENERGY, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorZeroVector() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndVector(EDGE, Vector.ZERO));
	}

	//------------------------------------------------------------------------------
	// Photon energy from edge (with double k)
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfEdgeDouble() {
		final Amount<Energy> expectedEnergy = Amount.valueOf(9014.0, ELECTRON_VOLT);
		final Amount<Energy> result = PhotonEnergy.photonEnergyFromEdgeAndValue(EDGE, DOUBLE_VALUE);
		assertEquals(expectedEnergy.doubleValue(Energy.UNIT), result.doubleValue(Energy.UNIT), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNegativeK() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndValue(EDGE, NEGATIVE_DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNegativeEdge() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndValue(NEGATIVE_EDGE, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNullEdge() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndValue(null, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleZeroEdge() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndValue(ZERO_ENERGY, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleZeroK() {
		assertNull(PhotonEnergy.photonEnergyFromEdgeAndValue(EDGE, 0.0));
	}

	/**
	 * Returns the {@link Length} corresponding to the specified quantity.
	 *
	 * @param length
	 *            a quantity compatible with {@link Length}.
	 * @return the specified quantity or a new {@link Length} instance.
	 * @throws ConversionException
	 *             if the current model does not allow the specified quantity to be converted to {@link Length}.
	 */
	public static Amount<Vector> vectorOf(Amount<Length> length) {
		return length.inverse().to(Vector.UNIT);
	}
}
