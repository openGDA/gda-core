/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.addetector;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.detector.addetector.filewriter.SingleImagePerFileWriter;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.impl.ADBaseSimulator;
import gda.device.detector.areadetector.v17.impl.NDArraySimulator;
import gda.device.detector.areadetector.v17.impl.NDFileSimulator;
import gda.device.detector.areadetector.v17.impl.NDPluginBaseSimulator;
import gda.scan.ConcurrentScan;
import gda.scan.RepeatScan;
import junit.framework.Assert;

import org.junit.Test;

public class ADDetectorIntegrationWithSingleImagePerFileWriterTest {

	String runTest(boolean templatesRequireScanNumber) throws Exception {
		String name="det";

		ADDetector adDetector = new ADDetector();
		ADBaseSimulator adBaseSimulator = new ADBaseSimulator();
		adDetector.setAdBase(adBaseSimulator);
		
		
		NDPluginBaseSimulator pluginBase = new NDPluginBaseSimulator();
		pluginBase.setDims(new int[]{1000,1000});
		pluginBase.setDatatype(NDPluginBase.DataType.UINT8 );
		
		NDFileSimulator ndFileSimulator = new NDFileSimulator(pluginBase);
		adDetector.setNdFile(ndFileSimulator);
		NDArraySimulator ndArraySimulator = new NDArraySimulator();
		ndArraySimulator.setPluginBase(pluginBase);
		adDetector.setReadArray(true);
		adDetector.setNdArray(ndArraySimulator);
		adDetector.setReadFilepath(true);
		SingleImagePerFileWriter fileWriter = 
				new SingleImagePerFileWriter(ndFileSimulator, "filename", 
						templatesRequireScanNumber ? "%%s%%s%d-%%d.tif" : "%s%s-%d.tif", 
								templatesRequireScanNumber ? "%s/%d/pco1/" : "%s/pco1",true,true);
		fileWriter.setTemplatesRequireScanNumber(templatesRequireScanNumber);
		adDetector.setFileWriter(fileWriter);
		adDetector.setName(name);
		adDetector.afterPropertiesSet();
		adDetector.configure();
		ConcurrentScan scan = RepeatScan.create_repscan(10, adDetector, .1);
		scan.runScan();	
		scan.getScanNumber();
		return ndFileSimulator.getFullFileName_RBV();
		
	}

	@Test
	public void testSingleImagePerFileWriterScan() throws Exception {
		String dir = TestHelpers.setUpTest(ADDetectorIntegrationWithSingleImagePerFileWriterTest.class, "testSingleImagePerFileWriterScan", true);
		LocalProperties.setScanSetsScanNumber(true);
		LocalProperties.set(NexusDataWriter.GDA_NEXUS_CREATE_SRS, "false");
		String fileName = runTest(true);
		Assert.assertEquals(dir+"/Data/1/pco1/filename1-0.tif", fileName);
	}
	@Test
	public void testSingleImagePerFileWriterScanScanSetsScanNumber() throws Exception {
		String dir = TestHelpers.setUpTest(ADDetectorIntegrationWithSingleImagePerFileWriterTest.class, "testSingleImagePerFileWriterScanScanSetsScanNumber", true);
		LocalProperties.setScanSetsScanNumber(false);
		LocalProperties.set(NexusDataWriter.GDA_NEXUS_CREATE_SRS, "false");
		String fileName = runTest(false);
		Assert.assertEquals(dir+"/Data/pco1filename-0.tif", fileName);
	}
}
