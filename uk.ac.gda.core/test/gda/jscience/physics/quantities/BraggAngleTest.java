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

import gda.jscience.physics.quantities.BraggAngle;
import junit.framework.TestCase;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;

/**
 * Test suite for BraggAngle class
 */
public class BraggAngleTest extends TestCase {
	/**
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(BraggAngleTest.class);
	}

	/**
	 * Test conversion from length to bragg angle
	 */
	public void testBraggAngleFromLength() {
		Length wavelength = Quantity.valueOf(1.388, NonSI.ANGSTROM);
		Length twoD = Quantity.valueOf(6.271, NonSI.ANGSTROM);
		Angle a = Quantity.valueOf(12787.5, SI.MILLI(NonSI.DEGREE_ANGLE));
		Angle result;

		result = BraggAngle.braggAngleOf(wavelength, twoD);
		assertEquals(a.doubleValue(), result.doubleValue(), 0.000001);
		/*
		 * result = BraggAngle.braggAngleOf(wavelength, null); assertEquals(a.doubleValue(), result.doubleValue(),
		 * 0.000001); result = BraggAngle.braggAngleOf((Length)null, twoD); assertEquals(a.doubleValue(),
		 * result.doubleValue(), 0.000001); twoD = Length.valueOf(0.0, NonSI.ANGSTROM); result =
		 * BraggAngle.braggAngleOf(wavelength, twoD); assertEquals(a.doubleValue(), result.doubleValue(), 0.000001);
		 */
	}
	// public static Angle braggAngleOf(Energy photonEnergy, Length twoD)
	// public static Angle braggAngleOf(ScatteringVector scatteringVector,
	// Length wavelength)

}
