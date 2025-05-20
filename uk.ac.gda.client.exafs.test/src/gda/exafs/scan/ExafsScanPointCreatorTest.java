/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExafsScanPointCreatorTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 0.0000000001;

	@Test(expected = NullPointerException.class)
	public void testGetEnergiesFailsWhenNothingSet() throws Exception {
		final ExafsScanPointCreator creator = new ExafsScanPointCreator();
		creator.getEnergies();
	}

	@Test
	public void testGetScanEnergies() throws Exception {
		final ExafsScanPointCreator creator = new ExafsScanPointCreator();

		creator.setInitialEnergy(100);
		creator.setaEnergy(110.);
		creator.setbEnergy(115.);
		creator.setcEnergy(120.);
		creator.setFinalEnergy(130);

		creator.setPreEdgeStep(0.7);
		creator.setEdgeStep(0.4);
		creator.setExafsStep(4);

		creator.setPreEdgeTime(1.);
		creator.setEdgeTime(5.);
		creator.setExafsTime(0.5);

		creator.setNumberDetectors(4);

		creator.getEnergies();

		double[][] energies = creator.getEnergies();
		assertEquals(42, energies.length);
		assertEquals(107.0, energies[10][0], FP_TOLERANCE);
		assertEquals(113.2656, energies[21][0], FP_TOLERANCE);

		creator.setPreEdgeStep(2);
		creator.setEdgeStep(1);
		creator.setExafsStep(5);

		double[][] energies2 = creator.getEnergies();
		testValueAlwaysIncreases(energies2);
		assertEquals(17, energies2.length);
		assertEquals(109.848, energies2[5][0], FP_TOLERANCE);
		assertEquals(125.0, energies2[15][0], FP_TOLERANCE);
	}

	@Test
	public void testVaryingExafsTime() throws Exception {
		final ExafsScanPointCreator creator = new ExafsScanPointCreator();

		creator.setInitialEnergy(100);
		creator.setaEnergy(110.);
		creator.setbEnergy(115.);
		creator.setcEnergy(120.);
		creator.setFinalEnergy(130);

		creator.setPreEdgeStep(0.7);
		creator.setEdgeStep(0.4);
		creator.setExafsStep(4);

		creator.setPreEdgeTime(1.);
		creator.setEdgeTime(5.);
		creator.setExafsConstantTime(false);
		creator.setExafsFromTime(0.1);
		creator.setExafsToTime(1.);
		creator.setkWeighting(1.);

		final double[][] energies = creator.getEnergies();
		testValueAlwaysIncreases(energies);
		assertEquals(42, energies.length);
		assertEquals(0.1, energies[38][1], FP_TOLERANCE);
		assertEquals(0.82, energies[40][1], FP_TOLERANCE);
	}

	@Test
	public void testVaryingExafsByK() throws Exception {
		final ExafsScanPointCreator creator = new ExafsScanPointCreator();

		creator.setInitialEnergy(100);
		creator.setaEnergy(110.);
		creator.setbEnergy(115.);
		creator.setcEnergy(120.);
		creator.setFinalEnergy(130);

		creator.setPreEdgeStep(0.7);
		creator.setEdgeStep(0.4);
		creator.setExafsStep(0.1);

		creator.setPreEdgeTime(1.);
		creator.setEdgeTime(2.);
		creator.setExafsTime(3.);

		creator.setExafsConstantEnergyStep(false);
		creator.setEdgeEnergy(117.);

		final double[][] energies = creator.getEnergies();
		testValueAlwaysIncreases(energies);
		assertEquals(51, energies.length);
		final double e38 = energies[38][0];
		assertTrue("Expected >= 120., but actually " + e38, e38 >= 120.);
		final double e48 = energies[48][0];
		assertTrue("Expected >= 127., but actually " + e48, e48 >= 127.);
	}

	@Test
	public void testExafsWhenCalculatingABC() throws Exception {
		final Double[] abc = ExafsScanRegionCalculator.calculateABC("Fe", "K", null, 20., 10., 10., false);

		final ExafsScanPointCreator creator = new ExafsScanPointCreator();

		creator.setInitialEnergy(7050);
		creator.setaEnergy(abc[0]);
		creator.setbEnergy(abc[1]);
		creator.setcEnergy(abc[2]);
		creator.setFinalEnergy(7200);

		creator.setPreEdgeStep(10);
		creator.setEdgeStep(1);
		creator.setExafsStep(20);

		creator.setPreEdgeTime(1.);
		creator.setEdgeTime(2.);
		creator.setExafsTime(3.);

		final double[][] energies = creator.getEnergies();
		testValueAlwaysIncreases(energies);
		assertEquals(37, energies.length);
		assertEquals(7080.0, energies[3][0], FP_TOLERANCE);
		assertTrue(energies[32][0] >= 7122.7);
	}

	private void testValueAlwaysIncreases(double[][] energies) {
		double last = 0;
		for (int i = 1; i < energies.length; i++) {
			double lastEnergy = energies[i-1][0];
			double thisEnergy = energies[i][0];
			assertTrue("Value should always increase", thisEnergy > lastEnergy);
		}
	}
}
