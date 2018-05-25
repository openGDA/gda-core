/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.scan;

import static gda.scan.ScanDataPointProvider.getPoint;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SortedScanDataPointCacheTest {

	private SortedScanDataPointCache cache;

	@Before
	public void setup() {
		cache = new SortedScanDataPointCache();
	}

	@Test
	public void testSortedPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 3, asList(2.0), asList(6.3)));
		cache.update(null, getPoint(1, 3, asList(3.0), asList(6.7)));
		cache.update(null, getPoint(2, 3, asList(4.0), asList(6.45)));

		List<Double> scanPositions = cache.getPositionsFor("scan0");
		List<Double> detPositions = cache.getPositionsFor("det0");
		assertEquals(asList(2.0, 3.0, 4.0), scanPositions);
		assertEquals(asList(6.3, 6.7, 6.45), detPositions);
	}

	@Test
	public void testUnsortedPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 3, asList(3.0), asList(6.3)));
		cache.update(null, getPoint(1, 3, asList(2.0), asList(6.7)));
		cache.update(null, getPoint(2, 3, asList(4.0), asList(6.45)));

		List<Double> scanPositions = cache.getPositionsFor("scan0");
		List<Double> detPositions = cache.getPositionsFor("det0");
		assertEquals(asList(2.0, 3.0, 4.0), scanPositions);
		assertEquals(asList(6.7, 6.3, 6.45), detPositions);
	}

	@Test
	public void testGridOfPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 4, asList(3.0, 1.0), asList(6.3)));
		cache.update(null, getPoint(1, 4, asList(3.0, 1.5), asList(6.7)));
		cache.update(null, getPoint(2, 4, asList(4.0, 1.0), asList(6.45)));
		cache.update(null, getPoint(3, 4, asList(4.0, 1.5), asList(6.13)));

		List<Double> scanPositions = cache.getPositionsFor("scan0");
		List<Double> scan1Positions = cache.getPositionsFor("scan1");
		List<Double> detPositions = cache.getPositionsFor("det0");
		assertEquals(asList(3.0, 3.0, 4.0, 4.0), scanPositions);
		assertEquals(asList(1.0, 1.5, 1.0, 1.5), scan1Positions);
		assertEquals(asList(6.3, 6.7, 6.45, 6.13), detPositions);
	}

	@Test
	public void testUnsortedGridOfPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 4, asList(3.0, 1.5), asList(6.7)));
		cache.update(null, getPoint(1, 4, asList(4.0, 1.5), asList(6.13)));
		cache.update(null, getPoint(2, 4, asList(3.0, 1.0), asList(6.3)));
		cache.update(null, getPoint(3, 4, asList(4.0, 1.0), asList(6.45)));

		List<Double> scanPositions = cache.getPositionsFor("scan0");
		List<Double> scan1Positions = cache.getPositionsFor("scan1");
		List<Double> detPositions = cache.getPositionsFor("det0");
		assertEquals(asList(3.0, 3.0, 4.0, 4.0), scanPositions);
		assertEquals(asList(1.0, 1.5, 1.0, 1.5), scan1Positions);
		assertEquals(asList(6.3, 6.7, 6.45, 6.13), detPositions);
	}


	@Test(expected=IllegalArgumentException.class)
	public void testEmptyPointsReturnEmptyValues() {
		cache.update(null, getPoint(0, 3, asList(), asList()));
		cache.update(null, getPoint(1, 3, asList(), asList()));
		cache.update(null, getPoint(2, 3, asList(), asList()));

		cache.getPositionsFor("scan0");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetInvalidScannable() {
		cache.update(null, getPoint(0, 3, asList(3.0), asList(6.3)));
		cache.update(null, getPoint(1, 3, asList(2.0), asList(6.7)));
		cache.update(null, getPoint(2, 3, asList(4.0), asList(6.45)));

		cache.getPositionsFor("non-existant detector");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testZeroPoints() {
		cache.getPositionsFor("scan0");
	}

	@Test
	public void testOnlyCurrentScanPointsUsed() {
		cache.update(null, getPoint(0, 2, asList(1.0), asList(5.7)));
		cache.update(null, getPoint(1, 2, asList(2.0), asList(5.45)));
		cache.update(null, getPoint(0, 2, asList(3.0), asList(6.7))); // scan point 0 should reset scan
		cache.update(null, getPoint(1, 2, asList(4.0), asList(6.45)));

		List<Double> scanPositions = cache.getPositionsFor("scan0");
		List<Double> detPositions = cache.getPositionsFor("det0");
		assertEquals(asList(3.0, 4.0), scanPositions);
		assertEquals(asList(6.7, 6.45), detPositions);
	}
}
