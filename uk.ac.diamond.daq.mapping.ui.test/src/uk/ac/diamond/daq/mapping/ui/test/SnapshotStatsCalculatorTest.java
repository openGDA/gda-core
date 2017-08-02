/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.ui.experiment.SnapshotStatsCalculator;

public class SnapshotStatsCalculatorTest {

	private IDataset data;
	private SnapshotStatsCalculator stats;
	private static final double DELTA = 1e-8;

	@Before
	public void setUp() {
		data = create2DArray(3,3);
		stats = new SnapshotStatsCalculator();
	}

	@Test
	public void testMean() {
		assertEquals(4, stats.calculateMean(data), DELTA);
	}

	@Test
	public void testStandardDeviation() {
		assertEquals(2.40370085, stats.calculateStdDev(data), DELTA);
	}

	@Test
	public void badValues() {
		assertEquals(1, stats.countBadPoints(data, 8));
		assertEquals(0, stats.countBadPoints(data, 9));
	}

	@Test
	public void testTotalCount() {
		assertEquals(36, stats.calculateTotalCount(data), DELTA);
	}

	@Test
	public void testSliceCount() {
		assertEquals(6, stats.calculateSliceCount(data, 0), DELTA);
		assertEquals(12, stats.calculateSliceCount(data, 1), DELTA);
		assertEquals(18, stats.calculateSliceCount(data, 2), DELTA);
	}

	@Test
	public void testMaxAndMin() {

		data = create2DArray(4, 3);

		assertEquals(1.0, (double) stats.findMinimumIntensity(data), DELTA);
		assertArrayEquals(new int[] {0,0}, stats.findMinimumPosition(data));

		assertEquals(12.0, (double) stats.findMaximumIntensity(data), DELTA);
		assertArrayEquals(new int[] {3,2}, stats.findMaximumPosition(data));
	}

	@Test
	public void handlingNaN() {
		data.set(Double.NaN, new int[] {1,0});
		assertFalse(Double.isNaN(stats.calculateTotalCount(data)));
		assertFalse(Double.isNaN(stats.calculateMean(data)));
		assertFalse(Double.isNaN(stats.calculateStdDev(data)));
	}

	@Test (expected=IndexOutOfBoundsException.class)
	public void badDetectorElement1() {
		data = createBadElementDataset();
		stats.findMaximumPosition(data);
	}

	@Test (expected=IndexOutOfBoundsException.class)
	public void badDetectorElement2() {
		data = createBadElementDataset();
		stats.findMinimumPosition(data);
	}

	private IDataset createBadElementDataset() {
		double[] x = new double[] {Double.NaN, Double.NaN, Double.NaN};
		return DatasetFactory.createFromObject(x);
	}

	private IDataset create2DArray(int d1, int d2) {
		double[][] x = new double[d1][d2];
		for (int i=1; i<d1+1; i++) {
			for (int j=1; j<d2+1; j++) {
				x[i-1][j-1] = i*j;
			}
		}
		return DatasetFactory.createFromObject(x);
	}

}
