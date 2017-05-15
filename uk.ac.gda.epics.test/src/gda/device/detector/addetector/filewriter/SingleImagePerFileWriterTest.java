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

package gda.device.detector.addetector.filewriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.jython.ICurrentScanInformationHolder;
import gda.jython.IJythonNamespace;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.observable.ObservableUtil;
import gda.scan.ScanInformation;


// TODO: Tests here are minimal

public class SingleImagePerFileWriterTest {

	private NDPluginBase mockNDPluginBase;

	private NDFile mockNdFile;

	private SingleImagePerFileWriter writer;


	@Before
	public void setUp() throws Exception {
		mockNDPluginBase = mock(NDPluginBase.class);
		mockNdFile = mock(NDFile.class);
		when(mockNdFile.getPluginBase()).thenReturn(mockNDPluginBase);
		when(mockNdFile.filePathExists()).thenReturn(true);
		when(mockNdFile.isWriteStatusErr()).thenReturn(false);
		when(mockNdFile.createWriteStatusObservable()).thenReturn(new ObservableUtil<Short>());
		writer = new SingleImagePerFileWriter("detname");
		writer.setNdFile(mockNdFile);
		writer.setWaitForFileArrival(false);
		writer.afterPropertiesSet();
		LocalProperties.set(PathConstructor.getDefaultPropertyName(), "absolute/path/to/datadir");
		configureScanInformationHolder();
		InterfaceProvider.setTerminalPrinterForTesting(mock(ITerminalPrinter.class));

	}

	private void configureScanInformationHolder() {
		ScanInformation scanInfo = mock(ScanInformation.class);
		ICurrentScanInformationHolder currentScanHolder = mock(ICurrentScanInformationHolder.class);
		when(currentScanHolder.getCurrentScanInformation()).thenReturn(scanInfo);
		when(scanInfo.getScanNumber()).thenReturn(12345);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(currentScanHolder);
		InterfaceProvider.setTerminalPrinterForTesting(mock(ITerminalPrinter.class));
	}

	@Test
	public void testGetFileTemplateDefault() {
		assertEquals("%s%s%05d.tif", writer.getFileTemplate());
	}

	@Test
	public void testGetFilePathDefault() {
		assertEquals(new File("absolute/path/to/datadir/12345-detname-files").getAbsolutePath(), writer.getFilePath());
	}

	@Test
	public void testGetFileNameDefault() {
		assertEquals("", writer.getFileName());
	}

	@Test
	public void testPrepareforCollectionSetsNextNumberDefault() throws Exception {
		writer.prepareForCollection(1, null);
		verify(mockNdFile).setFileNumber(1);
	}

	@Test
	public void testPrepareforCollectionSetsNextNumberNonDefault() throws Exception {
		writer.setFileNumberAtScanStart(54321);
		writer.prepareForCollection(1, null);
		verify(mockNdFile).setFileNumber(54321);
	}

	@Test
	public void atScanStartConfiguresMetadataPathTemplate() throws Exception {
		IJythonNamespace namespace = mock(IJythonNamespace.class);
		InterfaceProvider.setJythonNamespaceForTesting(namespace);
		when(namespace.getFromJythonNamespace("SRSWriteAtFileCreation")).thenReturn("existing\n");

		writer.setKeyNameForMetadataPathTemplate("detector_path_template");
		writer.prepareForCollection(1, null);

		String expected = "existing\ndetector_path_template='12345-detname-files/%05d.tif'\n";
		verify(namespace).placeInJythonNamespace("SRSWriteAtFileCreation", expected);

	}

	@Test
	public void atScanStartConfiguresMetadataPathTemplateWithNoGlobalToAstart() throws Exception {
		IJythonNamespace namespace = mock(IJythonNamespace.class);
		InterfaceProvider.setJythonNamespaceForTesting(namespace);
		when(namespace.getFromJythonNamespace("SRSWriteAtFileCreation")).thenReturn(null);

		writer.setKeyNameForMetadataPathTemplate("detector_path_template");
		writer.prepareForCollection(1, null);

		String expected = "detector_path_template='12345-detname-files/%05d.tif'\n";
		verify(namespace).placeInJythonNamespace("SRSWriteAtFileCreation", expected);

	}

	@Test
	public void testFilePathsOnRead() throws Exception {
		writer.prepareForCollection(3, null);
		List<NXDetectorDataAppender> readValues = writer.read(Integer.MAX_VALUE);
		NXDetectorDataFileAppenderForSrs value = (NXDetectorDataFileAppenderForSrs)readValues.get(0);
		NXDetectorDataWithFilepathForSrs data = new NXDetectorDataWithFilepathForSrs();
		value.appendTo(data, "detname");
		assertEquals(new File("absolute/path/to/datadir/12345-detname-files/00001.tif").getAbsolutePath(), data.getFilepath());
	}

	@Test
	public void testRelativeFilePathsOnRead() throws Exception {
		writer.setReturnPathRelativeToDatadir(true);
		writer.prepareForCollection(3, null);
		List<NXDetectorDataAppender> readValues = writer.read(Integer.MAX_VALUE);
		NXDetectorDataFileAppenderForSrs value = (NXDetectorDataFileAppenderForSrs)readValues.get(0);
		NXDetectorDataWithFilepathForSrs data = new NXDetectorDataWithFilepathForSrs();
		value.appendTo(data, "detname");
		assertEquals(data.getFilepath(), "12345-detname-files/00001.tif");
	}
}
