/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

public class SplitFilesProvidersTest {

	private ModuleDefinitions mythenModuleList;
	private DataConverter dataConverter;
	private SplitFilesBadChannelProvider badChannelProvider;
	private SplitFilesFlatFieldCorrectionProvider flatFieldDatasetProvider;

	@Before
	public void createCalibrationObjects() {

		Vector<String> moduleNames = new Vector<String>();
		moduleNames.add("SN01c");
		moduleNames.add("SN061");
		moduleNames.add("SN05c");
		moduleNames.add("SN05b");
		moduleNames.add("SN040");
		moduleNames.add("SN04d");

		mythenModuleList = new ModuleDefinitions();
		mythenModuleList.setModules(moduleNames);
		mythenModuleList.setMode("standard");
		mythenModuleList.setFlatFilePrefix("FlatCu");
		mythenModuleList.setCalibrationFolder("testfiles/gda/device/detector/mythen/data/SplitFilesProvidersTest");

		badChannelProvider = new SplitFilesBadChannelProvider();
		badChannelProvider.setModules(mythenModuleList);

		flatFieldDatasetProvider = new SplitFilesFlatFieldCorrectionProvider();
		flatFieldDatasetProvider.setModules(mythenModuleList);

		dataConverter = new DataConverter();
		dataConverter.setBadChannelProvider(badChannelProvider);
		dataConverter.setFlatFieldDatasetProvider(flatFieldDatasetProvider);
	}

	// test that the standard flat files and bad channels can be read
	@Test
	public void testCalibrationFilesRead(){
		try {
			MythenRawDataset rawData = new MythenRawDataset(new File("testfiles/gda/device/detector/mythen/data/SplitFilesProvidersTest/5485-mythen-0001.raw"));
			MythenProcessedDataset processed = dataConverter.process(rawData, 0);
			
			assertEquals(7675,processed.getAngleArray().length);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	// test that you can change to highgain on the fly
	@Test
	public void testChangeCalibrationData() {
		try {
			MythenRawDataset rawData = new MythenRawDataset(new File("testfiles/gda/device/detector/mythen/data/SplitFilesProvidersTest/5485-mythen-0001.raw"));
			MythenProcessedDataset standardprocessed = dataConverter.process(rawData, 0);
			
			MythenProcessedDataset standardprocessedAgain = dataConverter.process(rawData, 0);
			assertTrue(standardprocessed.getAngleArray().length == standardprocessedAgain.getAngleArray().length);
			assertTrue(standardprocessed.equals(standardprocessedAgain));

			mythenModuleList.setMode("highgain");
			MythenProcessedDataset highgainprocessed = dataConverter.process(rawData, 0);
			assertTrue(standardprocessed.getAngleArray().length == highgainprocessed.getAngleArray().length);
			assertTrue(standardprocessed != highgainprocessed);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
