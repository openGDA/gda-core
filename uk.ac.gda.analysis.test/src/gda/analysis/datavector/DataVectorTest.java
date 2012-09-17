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

package gda.analysis.datavector;

import gda.analysis.datastructure.DataVector;
import junit.framework.TestCase;

/**
 * DataVectorTest Class
 */
public class DataVectorTest extends TestCase {

	DataVector dv0;

	DataVector dv1;

	DataVector dv2;

	DataVector dv3;

	DataVector dv4;

	DataVector dv5;

	DataVector dv6;

	DataVector dv7;

	DataVector dv8;

	DataVector dv9;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// generate a set of datavectors to test with
		double data[] = { 1.0, 2.0, 3.0 };
		dv0 = new DataVector(data);
		int dim[] = { 2, 2 };
		dv1 = new DataVector(dim);
		dv2 = new DataVector(3, data);
		double data2[] = { 1.5, 2.5, 3.5, 4.5 };
		dv3 = new DataVector(2, 2, data2);
		double data3[] = { 1.8, 2.8, 3.8, 4.8, 5.8, 6.8, 7.8, 8.8 };
		dv4 = new DataVector(2, 2, 2, data3);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testAllocation() {
		if (dv0 == null) {
			fail("Not allocated DataVector(double[] data) properly");
		}

		if (dv1 == null) {
			fail("Not allocated DataVector(int[] dim) properly");
		}

		if (dv2 == null) {
			fail("Not allocated DataVector(int length, double[] data) properly");
		}

		if (dv3 == null) {
			fail("Not allocated DataVector(int width, int height, double[] data) properly");
		}

		if (dv4 == null) {
			fail("Not allocated DataVector(int width, int height,int depth, double[] data) properly");
		}

	}

	/**
	 * 
	 */
	public void testGetMin() {
		assertEquals(dv0.getMin(), 1.0, 0.00001);
		assertEquals(dv1.getMin(), 0.0, 0.00001);
		assertEquals(dv2.getMin(), 1.0, 0.00001);
		assertEquals(dv3.getMin(), 1.5, 0.00001);
		assertEquals(dv4.getMin(), 1.8, 0.00001);
	}

	/**
	 * 
	 */
	public void testGetMax() {
		assertEquals(dv0.getMax(), 3.0, 0.00001);
		assertEquals(dv1.getMax(), 0.0, 0.00001);
		assertEquals(dv2.getMax(), 3.0, 0.00001);
		assertEquals(dv3.getMax(), 4.5, 0.00001);
		assertEquals(dv4.getMax(), 8.8, 0.00001);
	}

	/**
	 * 
	 */
	public void testGetMean() {
		assertEquals(dv0.getMean(), 2.0, 0.00001);
		assertEquals(dv1.getMean(), 0.0, 0.00001);
		assertEquals(dv2.getMean(), 2.0, 0.00001);
		assertEquals(dv3.getMean(), 3.0, 0.00001);
		assertEquals(dv4.getMean(), 5.3, 0.00001);
	}

	/**
	 * 
	 */
	public void testGetRMS() {
		assertEquals(dv0.getRMS(), 2.160246899, 0.00001);
		assertEquals(dv1.getRMS(), 0.0, 0.00001);
		assertEquals(dv2.getRMS(), 2.160246899, 0.00001);
		assertEquals(dv3.getRMS(), 3.201562119, 0.00001);
		assertEquals(dv4.getRMS(), 5.77408013, 0.00001);
	}

	/**
	 * 
	 */
	public void testGetSubset() {
		// first check basic subsetting
		dv5 = dv0.getSubset(0, 1);
		dv6 = dv1.getSubset(0, 0, 1, 0);
		dv7 = dv3.getSubset(1, 0, 1, 1);
		dv8 = dv4.getSubset(0, 0, 0, 1, 0, 1);

		assertEquals(dv5.getIndex(0), 1.0);
		assertEquals(dv5.getIndex(1), 2.0);

		assertEquals(dv6.getIndex(0, 0), 0.0);
		assertEquals(dv6.getIndex(1, 0), 0.0);

		assertEquals(dv7.getIndex(0, 0), 2.5);
		assertEquals(dv7.getIndex(0, 1), 4.5);

		assertEquals(dv8.getIndex(0, 0, 0), 1.8);
		assertEquals(dv8.getIndex(1, 0, 0), 2.8);
		assertEquals(dv8.getIndex(0, 0, 1), 5.8);
		assertEquals(dv8.getIndex(1, 0, 1), 6.8);

		// check out reversing the dirs, this should still work
		dv5 = dv0.getSubset(1, 0);
		dv6 = dv1.getSubset(1, 0, 0, 0);
		dv7 = dv3.getSubset(0, 1, 0, 0);
		dv8 = dv4.getSubset(1, 1, 1, 0, 1, 0);

		assertEquals(dv5.getIndex(0), 2.0);
		assertEquals(dv5.getIndex(1), 1.0);

		assertEquals(dv6.getIndex(0, 0), 0.0);
		assertEquals(dv6.getIndex(1, 0), 0.0);

		assertEquals(dv7.getIndex(0, 0), 3.5);
		assertEquals(dv7.getIndex(0, 1), 1.5);

		assertEquals(dv8.getIndex(0, 0, 0), 8.8);
		assertEquals(dv8.getIndex(1, 0, 0), 7.8);
		assertEquals(dv8.getIndex(0, 0, 1), 4.8);
		assertEquals(dv8.getIndex(1, 0, 1), 3.8);

	}

}
