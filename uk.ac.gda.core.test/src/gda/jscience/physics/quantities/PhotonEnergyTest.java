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
import static org.junit.Assert.assertTrue;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.junit.Test;

/**
 */
public class PhotonEnergyTest {
	Energy result;

	/**
	 * Test method for
	 * {@link gda.jscience.physics.quantities.PhotonEnergy#photonEnergyOf(org.jscience.physics.quantities.Length)}.
	 */
	@Test
	public void testPhotonEnergyOfLength() {
		Energy photonEnergy = Quantity.valueOf(2.34, NonSI.ELECTRON_VOLT);
		Length negative = Quantity.valueOf(-1.0, NonSI.ANGSTROM);
		Length wavelength = Quantity.valueOf(529.847, SI.NANO(SI.METER));

		result = PhotonEnergy.photonEnergyOf(wavelength);
		System.out.println("r " + result + " pe " + photonEnergy);
		assertEquals(photonEnergy.doubleValue(), result.doubleValue(), 0.000000000001);

		result = PhotonEnergy.photonEnergyOf(negative);
		assertTrue("PhotonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(null);
		assertTrue("PhotonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(Length.ZERO);
		assertTrue("PhotonEnergy should be null", null == result);
	}

	/**
	 * Test method for
	 * {@link gda.jscience.physics.quantities.PhotonEnergy#photonEnergyOf(org.jscience.physics.quantities.Angle, org.jscience.physics.quantities.Length)}
	 * .
	 */
	@Test
	public void testPhotonEnergyOfAngleLength() {
		Length twoD = Quantity.valueOf(6.271, NonSI.ANGSTROM);
		Length negative = Quantity.valueOf(-1.0, NonSI.ANGSTROM);
		Angle negativeAngle = Quantity.valueOf(-1.0, SI.RADIAN);
		Angle braggAngle = Quantity.valueOf(12787.5, SI.MILLI(NonSI.DEGREE_ANGLE));
		Energy photonEnergy = Quantity.valueOf(8932.6, NonSI.ELECTRON_VOLT);

		result = PhotonEnergy.photonEnergyOf(braggAngle, twoD);
		assertEquals(photonEnergy.doubleValue(), result.doubleValue(), 0.0000000000001);

		result = PhotonEnergy.photonEnergyOf(braggAngle, negative);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(negativeAngle, twoD);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(braggAngle, null);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(null, twoD);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(Angle.ZERO, twoD);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(braggAngle, Length.ZERO);
		assertTrue("photonEnergy should be null", null == result);
	}

	/**
	 * Test method for
	 * {@link gda.jscience.physics.quantities.PhotonEnergy#photonEnergyOf(org.jscience.physics.quantities.Quantity, org.jscience.physics.quantities.Quantity)} .
	 */
	@Test
	public void testPhotonEnergyOfEnergyVector() {
		// Energy e = photonEnergyOf(Energy edge, Vector k);
		Energy edge = Quantity.valueOf(8980.197, NonSI.ELECTRON_VOLT);
		Energy negativeEdge = Quantity.valueOf(-1.0, NonSI.ELECTRON_VOLT);
		Energy photonEnergy = Quantity.valueOf(9020.0, NonSI.ELECTRON_VOLT);
		Length l = Quantity.valueOf(3.0, NonSI.ANGSTROM);
		Vector k = Vector.vectorOf(l);
		Length lneg = Quantity.valueOf(-3.0, NonSI.ANGSTROM);
		Vector negative = Vector.vectorOf(lneg);

		result = PhotonEnergy.photonEnergyOf(edge, k);
		assertEquals(photonEnergy.doubleValue(), result.doubleValue(), 0.0000000001);

		result = PhotonEnergy.photonEnergyOf(edge, negative);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(negativeEdge, k);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(edge, null);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(null, k);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(Energy.ZERO, k);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(edge, Vector.ZERO);
		assertTrue("photonEnergy should be null", null == result);
	}

	/**
	 * Test method for
	 * {@link gda.jscience.physics.quantities.PhotonEnergy#photonEnergyOf(org.jscience.physics.quantities.Energy, double)}
	 * .
	 */
	@Test
	public void testPhotonEnergyOfEnergyDouble() {
		double value = 3.0E-10;
		double negative = -1.0;
		Energy edge = Quantity.valueOf(8980.197, NonSI.ELECTRON_VOLT);
		Energy negativeEdge = Quantity.valueOf(-1.0, NonSI.ELECTRON_VOLT);
		Energy photonEnergy = Quantity.valueOf(9014.0, NonSI.ELECTRON_VOLT);

		result = PhotonEnergy.photonEnergyOf(edge, value);
		assertEquals(photonEnergy.doubleValue(), result.doubleValue(), 0.0000000000001);

		result = PhotonEnergy.photonEnergyOf(edge, negative);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(negativeEdge, value);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(null, value);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(Energy.ZERO, value);
		assertTrue("photonEnergy should be null", null == result);

		result = PhotonEnergy.photonEnergyOf(edge, 0.0);
		assertTrue("photonEnergy should be null", null == result);
	}

}
