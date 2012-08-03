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

package gda.device.detector.addetectorprovisional.filewriter;

import static org.junit.Assert.*;

import java.io.IOException;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.jython.ICurrentScanInformationHolder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gda.util.TestUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;


// TODO: Tests here are minimal

public class SingleImagePerFileWriterWithNumTrackerTest {

	private NDPluginBase mockNDPluginBase;
	
	private NDFile mockNdFile;

	private SingleImagePerFileWriter writer;

	private void setGdaVarDir(String path) {
		LocalProperties.set(LocalProperties.GDA_VAR_DIR, path);
	}
	
	@Before
	public void setUp() throws IOException {
		mockNDPluginBase = mock(NDPluginBase.class);
		mockNdFile = mock(NDFile.class);
		when(mockNdFile.getPluginBase()).thenReturn(mockNDPluginBase);
		LocalProperties.set(PathConstructor.getDefaultPropertyName(), "path/to/datadir");
		configureScanInformationHolder();
	}

	private void configureScanInformationHolder() {
		ScanInformation scanInfo = mock(ScanInformation.class);
		ICurrentScanInformationHolder currentScanHolder = mock(ICurrentScanInformationHolder.class);
		when(currentScanHolder.getCurrentScanInformation()).thenReturn(scanInfo);
		when(scanInfo.getScanNumber()).thenReturn((long) 12345);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(currentScanHolder);
	}
	
	@Test
	public void testGetFileTemplateDefault() throws Exception {
		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testGetFileTemplateDefault", true));
		writer = new SingleImagePerFileWriterWithNumTracker(mockNdFile, "detname", "detname_numtracker");
		assertEquals("%s%s%5.5d.jpg", writer.getFileTemplate());
	}


	@Test
	public void testGetFilePathDefault() throws Exception {
		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testGetFilePathDefault", true));
		writer = new SingleImagePerFileWriterWithNumTracker(mockNdFile, "detname", "detname_numtracker");
		assertEquals("path/to/datadir/snapped-data/detname", writer.getFilePath());
	}
	
	@Test
	public void testGetFileNameDefault() throws Exception {
		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testGetFileNameDefault", true));
		writer = new SingleImagePerFileWriterWithNumTracker(mockNdFile, "detname", "detname_numtracker");
		assertEquals("", writer.getFileName());
	}
	
	@Test
	public void testPrepareforCollectionAdvancesImageNumber() throws Exception {
		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testPrepareforCollectionSetsNextNumberDefault", true));
		writer = new SingleImagePerFileWriterWithNumTracker(mockNdFile, "detname", "detname_numtracker");

		writer.prepareForCollection(1);
		writer.prepareForCollection(1);
		writer.prepareForCollection(1);

		InOrder inOrder = inOrder(mockNdFile);
		inOrder.verify(mockNdFile).setFileNumber(1);
		inOrder.verify(mockNdFile).setFileNumber(2);
		inOrder.verify(mockNdFile).setFileNumber(3);
	}
	@Test
	public void testPrepareforCollectionIsUsingNumTracker() throws Exception {
		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testPrepareforCollectionSetsNextNumberDefault", true));
		writer = new SingleImagePerFileWriterWithNumTracker(mockNdFile, "detname", "detname_numtracker");
		NumTracker numTracker = new NumTracker("detname_numtracker");

		numTracker.setFileNumber(100);
		writer.prepareForCollection(1);
		writer.prepareForCollection(1);

		InOrder inOrder = inOrder(mockNdFile);
		inOrder.verify(mockNdFile).setFileNumber(101);
		inOrder.verify(mockNdFile).setFileNumber(102);
	}
	
	
}
