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
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 *
 */
public class XanesScanPointCreatorTest {

	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 0.0000000001;
	/**
	 *
	 */
	@Test
	public void testGetScanEnergies() {
		XanesScanPointCreator creator = new XanesScanPointCreator();

		boolean exceptionseen = false;
		try {
			creator.getEnergies();
		} catch (Exception e) {
			exceptionseen = true;
		}
		if (!exceptionseen){
			fail("exception not thrown when nothing set!");
		}


		double[][] newregions = new double[3][3];
		newregions[0][0] = 1000;
		newregions[0][1] = 25;
		newregions[0][2] = 1;

		newregions[1][0] = 1200;
		newregions[1][1] = 50;
		newregions[1][2] = 2;

		newregions[2][0] = 1400;
		newregions[2][1] = 10;
		newregions[2][2] = 3;
		creator.setRegions(newregions);

		exceptionseen = false;
		try {
			creator.getEnergies();
		} catch (Exception e) {
			exceptionseen = true;
		}
		if (!exceptionseen){
			fail("exception not thrown when nothing set!");
		}


		creator.setFinalEnergy(1500);

		try {
			double[][] energies = creator.getEnergies();
			assertEquals(23,energies.length);
			assertEquals(1300.0,energies[10][0], FP_TOLERANCE);
			assertEquals(1490.0,energies[21][0], FP_TOLERANCE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
