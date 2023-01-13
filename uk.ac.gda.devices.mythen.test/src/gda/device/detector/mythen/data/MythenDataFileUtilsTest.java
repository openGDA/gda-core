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

package gda.device.detector.mythen.data;

import static gda.device.detector.mythen.data.MythenDataFileUtils.getDataSubset;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the {@link MythenDataFileUtils} class.
 */
public class MythenDataFileUtilsTest {

	/**
	 * Ensures a Mythen raw data file can be loaded.
	 */
	public void testReadMythenDataFileWithRawFile() {
		File file = new File("testfiles/gda/device/detector/mythen/data/MythenDataFileUtilsTest/9keV_THscan_20_1_800_adv700_55.raw");
		double[][] data = MythenDataFileUtils.readMythenRawDataFile(file.getAbsolutePath());
		
		// Check dimensions of data
		assertEquals("Data does not contain the correct number of lines", 1280, data.length);
		assertEquals(2, data[0].length);
		
		// Spot-check a couple of values
		assertEquals("Unexpected data value", 64.0, data[105][1], 0);
		assertEquals("Unexpected data value", 67.0, data[1089][1], 0);
	}
	
	/**
	 * Ensures a Mythen processed data file can be loaded.
	 */
	@Test
	public void testReadMythenDataFileWithProcessedFile() {
		File file = new File("testfiles/gda/device/detector/mythen/data/Si_15keV_5.dat");
		double[][] data = MythenDataFileUtils.readMythenProcessedDataFile(file.getAbsolutePath(), false);
		
		// Check dimensions of data
		assertEquals(23040, data.length);
		assertEquals(3, data[0].length);
		
		// Spot-check  a couple of values
		assertEquals(72.752274, data[16076][0], 0.001);
		assertEquals(394.0, data[16076][1], 0);
		assertEquals(19.0, data[16076][2], 0);
	}
	
	@Test
	public void testReadMythenProcessedDataFiles() {
		File file = new File("testfiles/gda/device/detector/mythen/data/Si_15keV_5.dat");
		String[] filenames = new String[50];
		Arrays.fill(filenames, file.getAbsolutePath());
		
		// Load data. Should get 50 datasets
		double[][][] data = MythenDataFileUtils.readMythenProcessedDataFiles(filenames);
		assertEquals(50, data.length);
		
		// Ensure all datasets are identical
		for (int i=1; i<data.length; i++) {
			if (!Arrays.deepEquals(data[0], data[i])) {
				fail("Datasets are not identical");
			}
		}
	}
	
	@Test
	public void testGetDataSubset() {
		double[][] singleDataset = {
			{1.0, 10},
			{2.0, 20},
		};
		double[][][] input = {singleDataset};
		
		// alternately increase the min/max angles
		assertEquals(0, getDataSubset(input,  0.0, 0.5)[0].length);
		assertEquals(1, getDataSubset(input,  0.0, 1.0)[0].length);
		assertEquals(1, getDataSubset(input,  0.5, 1.0)[0].length);
		assertEquals(1, getDataSubset(input,  0.5, 1.5)[0].length);
		assertEquals(1, getDataSubset(input,  1.0, 1.5)[0].length);
		assertEquals(2, getDataSubset(input,  1.0, 2.0)[0].length);
		assertEquals(1, getDataSubset(input,  1.5, 2.0)[0].length);
		assertEquals(1, getDataSubset(input,  1.5, 2.5)[0].length);
		assertEquals(1, getDataSubset(input,  2.0, 2.5)[0].length);
		assertEquals(1, getDataSubset(input,  2.0, 3.0)[0].length);
		assertEquals(0, getDataSubset(input,  2.5, 3.0)[0].length);
	}
	
	@Test
	public void testGetInclusiveIndexForMinIncludedAngle() {
		final double[] angles = {1.0, 2.0, 3.0};
		assertEquals(0, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 0.5));
		assertEquals(0, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 1.0));
		assertEquals(1, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 1.5));
		assertEquals(1, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 2.0));
		assertEquals(2, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 2.5));
		assertEquals(2, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 3.0));
		assertEquals(3, MythenDataFileUtils.getInclusiveIndexForMinIncludedAngle(angles, 3.5));
	}
	
	@Test
	public void testGetExclusiveIndexForMaxIncludedAngle() {
		final double[] angles = {1.0, 2.0, 3.0};
		assertEquals(0, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 0.5));
		assertEquals(1, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 1.0));
		assertEquals(1, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 1.5));
		assertEquals(2, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 2.0));
		assertEquals(2, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 2.5));
		assertEquals(3, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 3.0));
		assertEquals(3, MythenDataFileUtils.getExclusiveIndexForMaxIncludedAngle(angles, 3.5));
	}
	
}
