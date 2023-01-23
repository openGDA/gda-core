/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan.preparers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import gda.TestHelpers;
import gda.device.DeviceException;
import gda.device.detector.NXDetector;
import gda.device.detector.addetector.filewriter.FileWriterBase;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.device.detector.nxdetector.roi.MutableRectangularIntegerROI;
import gda.device.detector.xmap.NexusXmap;
import gda.device.detector.xmap.NexusXmapFluorescenceDetectorAdapter;
import gda.device.detector.xspress.Xspress2Detector;
import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.medipix.MedipixParameters;
import uk.ac.gda.beans.medipix.ROIRegion;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.DummyXspress3Controller;
import uk.ac.gda.devices.detector.xspress4.DummyXspress4Controller;
import uk.ac.gda.devices.detector.xspress4.Xspress4Detector;
import uk.ac.gda.server.exafs.scan.DetectorPreparerFunctions;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class DetectorPreparerFunctionsTest {

	private DetectorPreparerFunctions preparerFunctions;
	private Xspress2Detector xspress2;
	private Xspress3Detector xspress3;
	private FluorescenceDetector xmap;

	private NexusXmap xmpaMca;
	private NXDetector medipix;
	private String hdfPathXspress3 = "/xspress3/hdfPath";
	private String hdfPathXspress4 = "/xspress4/hdfPath";

	private String medipixPath = "medipixPath";

	private Xspress4Detector xspress4;
	private MutableRectangularIntegerROI roiForMedipix;
	private List<NXPluginBase> mutableRoiPluginList = Collections.emptyList();
	private List<NXPluginBase> origMedipixPlugins;
	private FileWriterBase fileWriterPlugin;
	private MockedStatic<XMLHelpers> xmlHelpersMock;

	@Before
	public void setup() throws DeviceException {
		setupMockDetectors();

		preparerFunctions = new DetectorPreparerFunctions();
		preparerFunctions.setDataDirectory("/dataDirectory");
		preparerFunctions.setConfigFileDirectory("/scratch/xml");
		preparerFunctions.setMutableRoiForMedipix(roiForMedipix);
		preparerFunctions.setMutableRoiPluginList(mutableRoiPluginList);

		xmlHelpersMock = Mockito.mockStatic(XMLHelpers.class);
	}

	private void setupMockDetectors() throws DeviceException {
		xspress2 = createMock(Xspress2Detector.class, "xspress2");
		xspress3 = createMock(Xspress3Detector.class, "xspress3");
		xspress4 = createMock(Xspress4Detector.class, "xspress4");
		xmpaMca =  createMock(NexusXmap.class, "xmpaMca");
		medipix = createMock(NXDetector.class, "medipix");
		roiForMedipix = new MutableRectangularIntegerROI();

		// Mock controller for Xspress3
		DummyXspress3Controller controllerXsp3 = createMock(DummyXspress3Controller.class, "controllerXsp3");
		Mockito.when(xspress3.getController()).thenReturn(controllerXsp3);
		Mockito.when(controllerXsp3.getFilePath()).thenReturn(hdfPathXspress3);

		DummyXspress4Controller controllerXsp4 = createMock(DummyXspress4Controller.class, "controllerXsp4");
		Mockito.when(xspress4.getController()).thenReturn(controllerXsp4);
		Mockito.when(controllerXsp4.getHdfFilePath()).thenReturn(hdfPathXspress4);

		xmap = Mockito.mock(NexusXmapFluorescenceDetectorAdapter.class);
		Mockito.when(xmap.getName()).thenReturn("xmap");
		Mockito.when(((NexusXmapFluorescenceDetectorAdapter)xmap).getXmap()).thenReturn(xmpaMca);

		// Mock 'additional plugins' for medipix - to return filepath
		fileWriterPlugin = Mockito.mock(FileWriterBase.class);
		origMedipixPlugins = Arrays.asList(fileWriterPlugin);
		Mockito.when(fileWriterPlugin.getFilePathTemplate()).thenReturn(medipixPath);
		Mockito.when(medipix.getAdditionalPluginList()).thenReturn(origMedipixPlugins);
	}

	private <T> OngoingStubbing<T> mockReadBean() throws Exception {
		return Mockito.when(XMLHelpers.readBean(ArgumentMatchers.any(), ArgumentMatchers.<Class<T>>any()));
	}

	private OngoingStubbing<XMLRichBean> mockGetBean() throws Exception {
		return Mockito.when(XMLHelpers.getBean(ArgumentMatchers.<File>any()));
	}

	private <T extends Findable> T createMock(Class<T> clazz, String name) {
		T newMock = Mockito.mock(clazz);
		Mockito.when(newMock.getName()).thenReturn(name);
		return newMock;
	}

	@After
	public void tearDown() {
		// Remove factories from Finder so they do not affect other tests
		Finder.removeAllFactories();
		xmlHelpersMock.close();
	}


	private void setupFinder(String testName) throws Exception {
		String outputDir = TestHelpers.setUpTest(DetectorPreparerFunctionsTest.class, testName, true);
		Findable[] findables = new Findable[] {xspress2, xspress3, xspress4, xmap, medipix};
		// Findables the server needs to know about
		final Factory factory = TestHelpers.createTestFactory();
		for(Findable f : findables) {
			factory.addFindable(f);
			InterfaceProvider.getJythonNamespace().placeInJythonNamespace(f.getName(), f);
		}

		// Need to add object factory to Finder if using Finder.getInstance().find(...) to get at scannables.
		Finder.addFactory(factory);

		preparerFunctions.setConfigFileDirectory(outputDir);
	}

	private DetectorConfig createDetectorConfig(Findable det) throws IOException {
		DetectorConfig detConfig = new DetectorConfig();
		detConfig.setDetectorName(det.getName());
		detConfig.setConfigFileName(det.getName()+".xml");
		detConfig.setUseDetectorInScan(true);
		detConfig.setUseConfigFile(true);

		// create empty detector config xml file
		File detConfigFile = Paths.get(preparerFunctions.getConfigFileDirectory(), detConfig.getConfigFileName()).toFile();
		assertTrue("Problem creating empty detector file "+detConfigFile.getName(), detConfigFile.createNewFile());

		return detConfig;
	}

	@Test(expected = FileNotFoundException.class)
	public void testConfigFailsIfMissingFile() throws Exception {
		setupFinder("testConfigFailsIfMissingFile");
		DetectorConfig detConfig = createDetectorConfig(xspress2);

		// Delete the detector config xml file ...
		File detConfigFile = Paths.get(preparerFunctions.getConfigFileDirectory(), detConfig.getConfigFileName()).toFile();
		assertTrue("Problem deleting detector config file "+detConfigFile.getName(), detConfigFile.delete());

		// ... to make configure throw FileNotFoundException
		preparerFunctions.configure(detConfig);
	}


	@Test
	public void testXspress3WithoutConfig() throws Exception {
		setupFinder("testXspress3WithoutConfig");
		DetectorConfig detConfig = createDetectorConfig(xspress3);
		detConfig.setUseConfigFile(false);
		preparerFunctions.configure(detConfig);
		checkXspress3(detConfig, null);
	}

	@Test
	public void testXspress4WithoutConfig() throws Exception {
		setupFinder("testXspress4WithoutConfig");
		DetectorConfig detConfig = createDetectorConfig(xspress4);
		detConfig.setUseConfigFile(false);
		preparerFunctions.configure(detConfig);
		checkXspress4(detConfig, null);
	}

	@Test
	public void testConfigXspress2() throws Exception {
		setupFinder("testConfigXspress2");
		DetectorConfig detConfig = createDetectorConfig(xspress2);

		XspressParameters xspressParams = new XspressParameters();
		mockGetBean().thenReturn(xspressParams);

		preparerFunctions.configure(detConfig);

		checkXspress2(detConfig, xspressParams);
	}

	@Test
	public void testConfigXspress3() throws Exception {
		setupFinder("testConfigXspress3");
		DetectorConfig detConfig = createDetectorConfig(xspress3);

		Xspress3Parameters xspressParams = new Xspress3Parameters();
		mockGetBean().thenReturn(xspressParams);

		preparerFunctions.configure(detConfig);

		checkXspress3(detConfig, xspressParams);

		preparerFunctions.restoreDetectorState();

		checkXspress3Restored();
	}

	@Test
	public void testConfigXspress4() throws Exception {
		setupFinder("testConfigXspress4");
		DetectorConfig detConfig = createDetectorConfig(xspress4);

		XspressParameters xspressParams = new XspressParameters();
		mockGetBean().thenReturn(xspressParams);

		preparerFunctions.configure(detConfig);

		checkXspress4(detConfig, xspressParams);

		preparerFunctions.restoreDetectorState();

		checkXspress4Restored();
	}

	@Test
	public void testConfigMedipix() throws Exception {
		setupFinder("testConfigMedipix");
		DetectorConfig detConfig = createDetectorConfig(medipix);
		MedipixParameters parameters = new MedipixParameters();
		parameters.addRegion(new ROIRegion("region", 10, 20, 100, 200));
		mockReadBean().thenReturn(parameters);

		preparerFunctions.setDataDirectory(InterfaceProvider.getPathConstructor().createFromDefaultProperty());
		preparerFunctions.setDirForDetectorData(medipix.getName(), "medipix");
		preparerFunctions.configure(detConfig);

		checkMedipix(parameters);
	}

	@Test
	public void testConfigMedipixXspress4() throws Exception {
		setupFinder("testConfigMedipixXspress4");
		DetectorConfig medipixConfig = createDetectorConfig(medipix);
		MedipixParameters medipixParams = new MedipixParameters();
		medipixParams.addRegion(new ROIRegion("region", 10, 20, 100, 200));

		DetectorConfig xspress4Config = createDetectorConfig(xspress4);
		XspressParameters xspressParams = new XspressParameters();
		xspressParams.setDetectorName(xspress4.getName());

		List<DetectorConfig> detParams = new ArrayList<>();
		detParams.add(xspress4Config);
		detParams.add(medipixConfig);

		mockGetBean().thenReturn(xspressParams);
		mockReadBean().thenReturn(medipixParams);

		preparerFunctions.setDataDirectory(InterfaceProvider.getPathConstructor().createFromDefaultProperty());
		preparerFunctions.setDirForDetectorData(medipix.getName(), "medipix");
		preparerFunctions.setDirForDetectorData(xspress4.getName(), "xspress4");

		preparerFunctions.configure(detParams);

		checkXspress4(xspress4Config, xspressParams);
		checkMedipix(medipixParams);

		preparerFunctions.restoreDetectorState();
		checkXspress4Restored();
		checkMedipixRestored();
	}

	private String detectorConfigXmlFullPath(DetectorConfig detConfig) {
		return Paths.get(preparerFunctions.getConfigFileDirectory(), detConfig.getConfigFileName()).toAbsolutePath().toString();
	}

	private void checkXspress2(DetectorConfig detConfig, XspressParameters xspressParams) throws Exception {
		if (Boolean.TRUE.equals(detConfig.isUseConfigFile())) {
			Mockito.verify(xspress2).applyConfigurationParameters(xspressParams);
			Mockito.verify(xspress2).setConfigFileName(detectorConfigXmlFullPath(detConfig));
		}
	}

	private void checkXspress3(DetectorConfig detConfig, Xspress3Parameters xspressParams) throws Exception {
		if (Boolean.TRUE.equals(detConfig.isUseConfigFile())) {
			Mockito.verify(xspress3).applyConfigurationParameters(xspressParams);
			Mockito.verify(xspress3).setConfigFileName(detectorConfigXmlFullPath(detConfig));
		} else {
			Mockito.verify(xspress3, never()).applyConfigurationParameters(xspressParams);
			Mockito.verify(xspress3, never()).setConfigFileName(detectorConfigXmlFullPath(detConfig));
		}
		Mockito.verify(xspress3.getController()).setFilePath(Paths.get(preparerFunctions.getDataDirectory(), "nexus").toString());

		// Check the name of hdf directory of the detector before it was configured has been stored
		String intialHdfPath = preparerFunctions.getInitialHdfFilePaths().get(xspress3);
		assertEquals(hdfPathXspress3, intialHdfPath);
	}

	private void checkXspress3Restored() throws DeviceException {
		// Check hdf path was set back to its initial value
		Mockito.verify(xspress3.getController()).setFilePath(hdfPathXspress3);
	}

	private void checkXspress4(DetectorConfig detConfig, XspressParameters xspressParams) throws Exception {
		if (Boolean.TRUE.equals(detConfig.isUseConfigFile())) {
			Mockito.verify(xspress4).applyConfigurationParameters(xspressParams);
			Mockito.verify(xspress4).setConfigFileName(detectorConfigXmlFullPath(detConfig));
		} else {
			Mockito.verify(xspress4, never()).applyConfigurationParameters(xspressParams);
			Mockito.verify(xspress4, never()).setConfigFileName(detectorConfigXmlFullPath(detConfig));
		}
		Mockito.verify(xspress4.getController()).setHdfFilePath(Paths.get(preparerFunctions.getDataDirectory(), preparerFunctions.getDirForDetectorData(xspress4.getName())).toString());

		// Check the name of hdf directory of the detector before it was configured has been stored
		String intialHdfPath = preparerFunctions.getInitialHdfFilePaths().get(xspress4);
		assertEquals(hdfPathXspress4, intialHdfPath);
	}

	private void checkXspress4Restored() throws DeviceException {
		// Check hdf path was set back to its initial value
		Mockito.verify(xspress4.getController()).setHdfFilePath(hdfPathXspress4);
	}

	private void checkMedipix(MedipixParameters medipixParams) {
		// Plugin list has been set
		Mockito.verify(medipix).setAdditionalPluginList(mutableRoiPluginList);
		ROIRegion roi = medipixParams.getRegionList().get(0);

		// Check the ROI has been set correctly
		assertEquals(Integer.valueOf(roi.getXRoi().getRoiStart()), roiForMedipix.getXstart());
		assertEquals(Integer.valueOf(roi.getYRoi().getRoiStart()), roiForMedipix.getYstart());
		assertEquals(Integer.valueOf(roi.getXRoi().getRoiEnd() - roi.getXRoi().getRoiStart()), roiForMedipix.getXsize());
		assertEquals(Integer.valueOf(roi.getYRoi().getRoiEnd() - roi.getYRoi().getRoiStart()), roiForMedipix.getYsize());

		String initialHdfPath = preparerFunctions.getInitialHdfFilePaths().get(medipix);
		assertEquals(medipixPath, initialHdfPath);

		// Check output directory has been set correctly.
		String expectedTemplate = Paths.get("$datadir$", preparerFunctions.getDirForDetectorData(medipix.getName())).toString();
		Mockito.verify(fileWriterPlugin).setFilePathTemplate(expectedTemplate);
	}

	private void checkMedipixRestored() {
		// Check the additional plugin list is restored to the initial value
		Mockito.verify(medipix).setAdditionalPluginList(origMedipixPlugins);
	}
}
