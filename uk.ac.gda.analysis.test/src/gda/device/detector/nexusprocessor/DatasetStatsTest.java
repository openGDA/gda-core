/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Before;
import org.junit.Test;

import gda.device.detector.GDANexusDetectorData;

public class DatasetStatsTest {

	private DatasetStats stat;

	@Before
	public void setup() {
		stat = new DatasetStats();
		stat.setEnabledStats(Arrays.asList(DatasetStats.Statistic.SUM));
	}

	@Test
	public void testDatasetStats1d() throws Exception {
		Dataset testDataset = DatasetFactory.createFromObject(new int[] { 100, 100 });
		GDANexusDetectorData res = stat.process("det", "data", testDataset);
		double sumresult = res.getDoubleVals()[0];
		assertEquals(200, sumresult, 1);
	}

	@Test
	public void testNegative1d() throws Exception {
		Dataset testDataset = DatasetFactory.createFromObject(new int[] { -1, -1 });
		GDANexusDetectorData res = stat.process("det", "data", testDataset);
		double sumresult = res.getDoubleVals()[0];
		assertEquals(-2, sumresult, 1);
	}

	@Test
	public void testDatasetStats2d() throws Exception {
		Dataset testDataset = DatasetFactory.createFromObject(new int[] { 100, 100 });
		testDataset.resize(new int[] { 1000, 1000 });
		GDANexusDetectorData res = stat.process("det", "data", testDataset);
		double sumresult = res.getDoubleVals()[0];
		assertEquals(200, sumresult, 1);
	}
}
