/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.optimiser;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LadderSampleFinderTest {

	@Test
	public void testFindBaseLine0() {
		LadderSampleFinder lsf = new LadderSampleFinder();
		
		double baseLine = lsf.findBaseLine(new double[] { 0.0, 0.0, 0.0});
		assertTrue(baseLine == 0.0);
	}
	
	@Test
	public void testFindBaseLine01() {
		LadderSampleFinder lsf = new LadderSampleFinder();
		
		double[] array = new double[] { 10.0, 9.9, 10.0, 9.9, -1.0, -1.1, 10.1, 9.8, 10.1, 10.2, 10.0, 9.9, 10.0, 10.0, 10.0 };
		lsf.initMaxMin(array);
		double baseLine = lsf.findBaseLine(array);
		assertTrue(baseLine < 11.0 && baseLine > 9.0);
	}
	
}
