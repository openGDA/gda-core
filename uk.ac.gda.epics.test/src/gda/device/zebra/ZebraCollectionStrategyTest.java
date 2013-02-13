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

package gda.device.zebra;

import static org.junit.Assert.fail;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ConcurrentScan;
import gda.scan.ScanPositionProvider;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.gda.analysis.hdf5.Hdf5Helper;
import uk.ac.gda.analysis.hdf5.Hdf5HelperData;

public class ZebraCollectionStrategyTest {

	@Test
	public void testSimplePosnCompare() throws InterruptedException, Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(ZebraCollectionStrategyTest.class, "testSimplePosnCompare", true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");

		LocalProperties.set("gda.nexus.createSRS", "false");
		
		
		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
				new String[] { "simpleScannable2" }, 0, new String[] { "%5.2g" }, new String[] { "eV" });
		

		ZebraCollectionStrategy collectionStrategy = new ZebraCollectionStrategy();
		NXDetector det = new NXDetector("nxdet", collectionStrategy, new ArrayList<NXPlugin>());		
		
		ScanPositionProvider scanPositionProvider = new ScanPositionProvider() {
			
			@Override
			public int size() {
				return 100;
			}
			
			@Override
			public Object get(int index) {
				return index;
			}
		};
		collectionStrategy.numPulsesPerLine = scanPositionProvider.size();
		Object[] args = new Object[] { simpleScannable2, scanPositionProvider, det };
		ConcurrentScan scan = new ConcurrentScan(args);
		scan.runScan();		


		//1. test points to 2.nxs 
		Hdf5HelperData helperData = Hdf5Helper.getInstance().readDataSetAll(testScratchDirectoryName + "/Data/2.nxs", "/entry2", "test", true);
		double[] data = (double[]) helperData.data;
		Assert.assertEquals(10.0, data[5], 1e-6);

		
		//2. test2 points to 1.nxs 
		helperData = Hdf5Helper.getInstance().readDataSetAll(testScratchDirectoryName + "/Data/2.nxs", "/entry2", "test2", true);
		data = (double[]) helperData.data;
		Assert.assertEquals(5.0, data[5], 1e-6);

		//3. test3 points to test2 so should be the same assertion 2
		helperData = Hdf5Helper.getInstance().readDataSetAll(testScratchDirectoryName + "/Data/2.nxs", "/entry2", "test3", true);
		data = (double[]) helperData.data;
		Assert.assertEquals(5.0, data[5], 1e-6);
		
		fail("Not yet implemented");
	}

}
