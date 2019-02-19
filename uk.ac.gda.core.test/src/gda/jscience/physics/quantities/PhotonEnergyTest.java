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
import static org.jscience.physics.units.SI.MILLI;
import static org.jscience.physics.units.SI.NANO;
import static org.jscience.physics.units.SI.RADIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.ConversionException;
import org.junit.Test;

public class PhotonEnergyTest {
	private static final Length WAVELENGTH = Quantity.valueOf(529.847, NANO(METER));
	private static final Length NEGATIVE_LENGTH = Quantity.valueOf(-1.0, ANGSTROM);
	private static final Length TWO_D = Quantity.valueOf(6.271, ANGSTROM);
	private static final Angle BRAGG_ANGLE = Quantity.valueOf(12787.5, MILLI(DEGREE_ANGLE));
	private static final Angle NEGATIVE_ANGLE = Quantity.valueOf(-1.0, RADIAN);

	private static final Energy EDGE = Quantity.valueOf(8980.197, ELECTRON_VOLT);
	private static final Energy NEGATIVE_EDGE = Quantity.valueOf(-1.0, ELECTRON_VOLT);
	private static final Length EDGE_LENGTH = Quantity.valueOf(3.0, ANGSTROM);
	private static final Vector EDGE_LENGTH_VECTOR = vectorOf(EDGE_LENGTH);
	private static final Length NEGATIVE_EDGE_LENGTH = Quantity.valueOf(-3.0, ANGSTROM);
	private static final Vector NEGATIVE_EDGE_LENGTH_VECTOR = vectorOf(NEGATIVE_EDGE_LENGTH);
	private static final double DOUBLE_VALUE = 3.0E-10;
	private static final double NEGATIVE_DOUBLE_VALUE = -1.0;

	//------------------------------------------------------------------------------
	// Photon energy from wavelength
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfWavelength() {
		final Energy expectedEnergy = Quantity.valueOf(2.34, ELECTRON_VOLT);

		final Energy result = PhotonEnergy.photonEnergyOf(WAVELENGTH);
		assertEquals(expectedEnergy.doubleValue(), result.doubleValue(), 0.000000000001);
	}

	@Test
	public void testPhotonEnergyOfWavelengthNegativeWavelength() {
		assertNull(PhotonEnergy.photonEnergyOf(NEGATIVE_LENGTH));
	}

	@Test
	public void testPhotonEnergyOfWavelengthNullWavelength() {
		assertNull(PhotonEnergy.photonEnergyOf(null));
	}

	@Test
	public void testPhotonEnergyOfWavelengthZeroWavelength() {
		assertNull(PhotonEnergy.photonEnergyOf(Length.ZERO));
	}

	//------------------------------------------------------------------------------
	// Photon energy from Bragg angle
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfAngle() {
		final Energy expectedEnergy = Quantity.valueOf(8932.6, ELECTRON_VOLT);
		final Energy result = PhotonEnergy.photonEnergyOf(BRAGG_ANGLE, TWO_D);
		assertEquals(expectedEnergy.doubleValue(), result.doubleValue(), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfAngleNegativeTwoD() {
		assertNull(PhotonEnergy.photonEnergyOf(BRAGG_ANGLE, NEGATIVE_LENGTH));
	}

	@Test
	public void testPhotonEnergyOfAngleNegativeAngle() {
		assertNull(PhotonEnergy.photonEnergyOf(NEGATIVE_ANGLE, TWO_D));
	}

	@Test
	public void testPhotonEnergyOfAngleNullTwoD() {
		assertNull(PhotonEnergy.photonEnergyOf(BRAGG_ANGLE, null));
	}

	@Test
	public void testPhotonEnergyOfAngleNullAngle() {
		assertNull(PhotonEnergy.photonEnergyOf(null, TWO_D));
	}

	@Test
	public void testPhotonEnergyOfAngleZeroAngle() {
		assertNull(PhotonEnergy.photonEnergyOf(Angle.ZERO, TWO_D));
	}

	public void testPhotonEnergyOfAngleZeroTwoD() {
		assertNull(PhotonEnergy.photonEnergyOf(BRAGG_ANGLE, Length.ZERO));
	}

	//------------------------------------------------------------------------------
	// Photon energy from edge (with Quantity k)
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfEdgeVector() {
		final Energy expectedPhotonEnergy = Quantity.valueOf(9020.0, ELECTRON_VOLT);
		final Energy result = PhotonEnergy.photonEnergyOf(EDGE, EDGE_LENGTH_VECTOR);
		assertEquals(expectedPhotonEnergy.doubleValue(), result.doubleValue(), 0.0000000001);
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNegativeVector() {
		assertNull(PhotonEnergy.photonEnergyOf(EDGE, NEGATIVE_EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNegativeEdge() {
		assertNull(PhotonEnergy.photonEnergyOf(NEGATIVE_EDGE, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNullVector() {
		assertNull(PhotonEnergy.photonEnergyOf(EDGE, null));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorNullEdge() {
		assertNull(PhotonEnergy.photonEnergyOf(null, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorZeroEdge() {
		assertNull(PhotonEnergy.photonEnergyOf(Energy.ZERO, EDGE_LENGTH_VECTOR));
	}

	@Test
	public void testPhotonEnergyOfEdgeVectorZeroVector() {
		assertNull(PhotonEnergy.photonEnergyOf(EDGE, Vector.ZERO));
	}

	//------------------------------------------------------------------------------
	// Photon energy from edge (with double k)
	//------------------------------------------------------------------------------
	@Test
	public void testPhotonEnergyOfEdgeDouble() {
		final Energy expectedPhotonEnergy = Quantity.valueOf(9014.0, ELECTRON_VOLT);
		final Energy result = PhotonEnergy.photonEnergyOf(EDGE, DOUBLE_VALUE);
		assertEquals(expectedPhotonEnergy.doubleValue(), result.doubleValue(), 0.0000000000001);
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNegativeK() {
		assertNull(PhotonEnergy.photonEnergyOf(EDGE, NEGATIVE_DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNegativeEdge() {
		assertNull(PhotonEnergy.photonEnergyOf(NEGATIVE_EDGE, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleNullEdge() {
		assertNull(PhotonEnergy.photonEnergyOf(null, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleZeroEdge() {
		assertNull(PhotonEnergy.photonEnergyOf(Energy.ZERO, DOUBLE_VALUE));
	}

	@Test
	public void testPhotonEnergyOfEdgeDoubleZeroK() {
		assertNull(PhotonEnergy.photonEnergyOf(EDGE, 0.0));
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
	public static Vector vectorOf(Length length) {
		return length.inverse().to(Vector.UNIT);
	}
}
