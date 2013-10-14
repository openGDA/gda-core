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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

public class ExafsScanPointCreatorTest {

	@Test
	public void testGetScanEnergies() {
		ExafsScanPointCreator creator = new ExafsScanPointCreator();

		boolean exceptionseen = false;
		try {
			creator.getEnergies();
		} catch (Exception e) {
			exceptionseen = true;
		}
		if (!exceptionseen) {
			fail("exception not thrown when nothing set!");
		}

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

		try {
			creator.getEnergies();
		} catch (Exception e) {
			fail("exception thrown when all set!");
		}

		try {
			PyTuple energies = creator.getEnergies();
			assertEquals(42, energies.__len__());
			assertEquals(107.0, energies.__getitem__(10).__getitem__(0).__tojava__(java.lang.Double.class));
			assertEquals(113.2656, energies.__getitem__(21).__getitem__(0).__tojava__(java.lang.Double.class));
		} catch (Exception e) {
			fail(e.getMessage());
		}

		creator.setPreEdgeStep(2);
		creator.setEdgeStep(1);
		creator.setExafsStep(5);

		try {
			PyTuple energies = creator.getEnergies();
			testValueAlwaysIncreases(energies);
			assertEquals(17, energies.__len__());
			assertEquals(109.848, energies.__getitem__(5).__getitem__(0).__tojava__(java.lang.Double.class));
			assertEquals(125.0, energies.__getitem__(15).__getitem__(0).__tojava__(java.lang.Double.class));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testVaryingExafsTime() {
		ExafsScanPointCreator creator = new ExafsScanPointCreator();

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

		try {
			PyTuple energies = creator.getEnergies();
			testValueAlwaysIncreases(energies);
			assertEquals(42, energies.__len__());
			assertEquals(0.1, energies.__getitem__(38).__getitem__(1).__tojava__(java.lang.Double.class));
			assertEquals(0.82, energies.__getitem__(40).__getitem__(1).__tojava__(java.lang.Double.class));
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void testVaryingExafsByK() {
		ExafsScanPointCreator creator = new ExafsScanPointCreator();

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

		try {
			PyTuple energies = creator.getEnergies();
			testValueAlwaysIncreases(energies);
			assertEquals(51, energies.__len__());
			double e38 = (Double) energies.__getitem__(38).__getitem__(0).__tojava__(java.lang.Double.class);
			assertTrue("Expected >= 120., but actually " + e38, e38 >= 120.);
			double e48 = (Double) energies.__getitem__(48).__getitem__(0).__tojava__(java.lang.Double.class);
			assertTrue("Expected >= 127., but actually " + e48, e48 >= 127.);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@SuppressWarnings("null")
	@Test
	public void testExafsWhenCalculatingABC() {

		Double[] abc = null;
		try {
			abc = ExafsScanRegionCalculator.calculateABC("Fe", "K", null, 20., 10., 10., false);
		} catch (Exception e1) {
			fail(e1.getMessage());
		}

		ExafsScanPointCreator creator = new ExafsScanPointCreator();

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

		try {
			PyTuple energies = creator.getEnergies();
			testValueAlwaysIncreases(energies);
			assertEquals(37, energies.__len__());
			assertEquals(7080.0, energies.__getitem__(3).__getitem__(0).__tojava__(java.lang.Double.class));
			assertTrue((Double) energies.__getitem__(32).__getitem__(0).__tojava__(java.lang.Double.class) >= 7122.7);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	private void testValueAlwaysIncreases(PyTuple energies) {
		double last = 0;
		for (int i = 1; i < energies.__len__(); i++) {
			PyFloat[] lastObj = (PyFloat[]) energies.get(i - 1);
			PyFloat[] thisObj = (PyFloat[]) energies.get(i);
			last = lastObj[0].asDouble();
			double thisValue = thisObj[0].asDouble();
			if (last >= thisValue) {
				fail(last + " not greater than " + thisValue);
			}
			last = thisValue;
		}
	}
}
