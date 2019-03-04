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
import org.python.core.PyFloat;
import org.python.core.PyTuple;

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

		final PyTuple energies = creator.getEnergies();
		assertEquals(42, energies.__len__());
		assertEquals(107.0, (double) energies.__getitem__(10).__getitem__(0).__tojava__(Double.class), FP_TOLERANCE);
		assertEquals(113.2656, (double) energies.__getitem__(21).__getitem__(0).__tojava__(Double.class), FP_TOLERANCE);

		creator.setPreEdgeStep(2);
		creator.setEdgeStep(1);
		creator.setExafsStep(5);

		final PyTuple energies2 = creator.getEnergies();
		testValueAlwaysIncreases(energies2);
		assertEquals(17, energies2.__len__());
		assertEquals(109.848, (double) energies2.__getitem__(5).__getitem__(0).__tojava__(Double.class), FP_TOLERANCE);
		assertEquals(125.0, (double) energies2.__getitem__(15).__getitem__(0).__tojava__(Double.class), FP_TOLERANCE);
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

		final PyTuple energies = creator.getEnergies();
		testValueAlwaysIncreases(energies);
		assertEquals(42, energies.__len__());
		assertEquals(0.1, (double) energies.__getitem__(38).__getitem__(1).__tojava__(Double.class), FP_TOLERANCE);
		assertEquals(0.82, (double) energies.__getitem__(40).__getitem__(1).__tojava__(Double.class), FP_TOLERANCE);
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

		final PyTuple energies = creator.getEnergies();
		testValueAlwaysIncreases(energies);
		assertEquals(51, energies.__len__());
		final double e38 = (Double) energies.__getitem__(38).__getitem__(0).__tojava__(Double.class);
		assertTrue("Expected >= 120., but actually " + e38, e38 >= 120.);
		final double e48 = (Double) energies.__getitem__(48).__getitem__(0).__tojava__(Double.class);
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

		final PyTuple energies = creator.getEnergies();
		testValueAlwaysIncreases(energies);
		assertEquals(37, energies.__len__());
		assertEquals(7080.0, (double) energies.__getitem__(3).__getitem__(0).__tojava__(Double.class), FP_TOLERANCE);
		assertTrue((Double) energies.__getitem__(32).__getitem__(0).__tojava__(Double.class) >= 7122.7);
	}

	private void testValueAlwaysIncreases(PyTuple energies) {
		double last = 0;
		for (int i = 1; i < energies.__len__(); i++) {
			final PyFloat[] lastObj = (PyFloat[]) energies.get(i - 1);
			final PyFloat[] thisObj = (PyFloat[]) energies.get(i);
			last = lastObj[0].asDouble();
			final double thisValue = thisObj[0].asDouble();
			assertTrue("Value should always increase", thisValue > last);
			last = thisValue;
		}
	}
}
