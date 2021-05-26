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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.util.TestUtils;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.IonChambersBean;
import uk.ac.gda.beans.exafs.TransmissionParameters;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.describers.DetectorDescriber;
import uk.ac.gda.util.OSUtils;
import uk.ac.gda.util.PackageUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * test class for detector parameter xml file
 */
public class DetectorParametersTest {
	final static String testScratchDirectoryName = TestUtils
			.generateDirectorynameFromClassname(DetectorParametersTest.class.getCanonicalName());

	public static DetectorParameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(DetectorParameters.mappingURL, DetectorParameters.class,
				DetectorParameters.schemaUrl, filename);
	}

	public static void writeToXML(DetectorParameters sampleParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(DetectorParameters.mappingURL, sampleParameters, filename);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestUtils.makeScratchDirectory(testScratchDirectoryName);

		final String testFilesPath = EclipseUtils.getAbsoluteUrl(DetectorParametersTest.class.getResource("TestFiles"))
				.getFile();
		System.setProperty("gda.config", testFilesPath);
	}

	@Test
	public void testDescriber() {
		try {
			InputStream contents = new FileInputStream(new File(PackageUtils.getTestPath(getClass())
					+ "DetectorParameters_withTransmission.xml"));
			DetectorDescriber describer = new DetectorDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * test for file existence
	 */
	@Test
	public void testCreateFromXML_FileDoesNotExist() {
		try {
			createFromXML("DoesNotExists");
			fail("File does not exists");
		} catch (Exception ex) {
			if (!(ex instanceof FileNotFoundException)) {
				fail("Invalid exception thrown - " + ex.getCause().getMessage());
			}
		}
	}

	private IonChamberParameters getI0IonchamberParameters() {
		IonChamberParameters ionChamberParameters = new IonChamberParameters();
		ionChamberParameters.setName("I0");
		ionChamberParameters.setDeviceName("counterTimer01");
		ionChamberParameters.setChannel(1);
		ionChamberParameters.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters.setGain("1 nA/V");
		ionChamberParameters.setGasType("Ar");
		ionChamberParameters.setPercentAbsorption(70.0);
		return ionChamberParameters;
	}
	private IonChamberParameters getItIonchamberParameters() {
		IonChamberParameters ionChamberParameters = new IonChamberParameters();
		ionChamberParameters.setName("It");
		ionChamberParameters.setDeviceName("counterTimer01");
		ionChamberParameters.setChannel(2);
		ionChamberParameters.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters.setGain("1 nA/V");
		ionChamberParameters.setGasType("Ar");
		ionChamberParameters.setPercentAbsorption(67.0);
		return ionChamberParameters;
	}
	private IonChamberParameters getIRefIonchamberParameters() {
		IonChamberParameters ionChamberParameters = new IonChamberParameters();
		ionChamberParameters.setName("Iref");
		ionChamberParameters.setDeviceName("counterTimer01");
		ionChamberParameters.setChannel(3);
		ionChamberParameters.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters.setGain("1 nA/V");
		ionChamberParameters.setGasType("Ar");
		ionChamberParameters.setPercentAbsorption(83.0);
		return ionChamberParameters;
	}
	/**
	 * test for ion chamber parameters
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXML_withTransmission() throws Exception {
		TestUtils
				.skipTestIf(
						!OSUtils.isLinuxOS(),
						this.getClass().getCanonicalName()
								+ ".testCreateFromXML_withTransmission skipped, since PressureCalculation currently only runs on linux");
		DetectorParameters expectedValue = new DetectorParameters();
		expectedValue.addDetectorGroup(new DetectorGroup("transmission", new String[] { "counterTimer01" }));
		expectedValue.addDetectorGroup(new DetectorGroup("Silicon", new String[] { "xmapMca", "counterTimer01" }));
		expectedValue.addDetectorGroup(new DetectorGroup("Germanium", new String[] { "counterTimer02",
				"xspress2system", "counterTimer01" }));
		expectedValue.setExperimentType("Transmission");
		TransmissionParameters transmissionParameters = new TransmissionParameters();
		transmissionParameters.setDetectorType("Transmission");
		transmissionParameters.setWorkingEnergy(7100.0);
		transmissionParameters.setMythenEnergy(9000.0);
		transmissionParameters.setMythenTime(10.0);

		transmissionParameters.addIonChamberParameter(getI0IonchamberParameters());
		transmissionParameters.addIonChamberParameter(getItIonchamberParameters());
		transmissionParameters.addIonChamberParameter(getIRefIonchamberParameters());
		expectedValue.setTransmissionParameters(transmissionParameters);

		DetectorParameters d = createFromXML(PackageUtils.getTestPath(getClass())
				+ "DetectorParameters_withTransmission.xml");

		assertEquals(expectedValue, d);

		// if (!expectedValue.equals(d)) {
		// fail("Values read are incorrect - " + d.toString());
		// }
	}

	/**
	 * test fluorescence detector parameters
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXML_withFluorescence() throws Exception {
		TestUtils
				.skipTestIf(
						!OSUtils.isLinuxOS(),
						this.getClass().getCanonicalName()
								+ ".testCreateFromXML_withFluorescence skipped, since PressureCalculation currently only runs on linux");
		DetectorParameters expectedValue = new DetectorParameters();
		expectedValue.addDetectorGroup(new DetectorGroup("transmission", new String[] { "counterTimer01" }));
		expectedValue.addDetectorGroup(new DetectorGroup("Silicon", new String[] { "xmapMca", "counterTimer01" }));
		expectedValue.addDetectorGroup(new DetectorGroup("Germanium", new String[] { "counterTimer02",
				"xspress2system", "counterTimer01" }));
		expectedValue.setExperimentType("Fluorescence");
		FluorescenceParameters fluorescenceParameters = new FluorescenceParameters();
		fluorescenceParameters.setDetectorType("Germanium");
		fluorescenceParameters.setWorkingEnergy(7100.0);
		fluorescenceParameters.setConfigFileName("detectors.cnf");

		fluorescenceParameters.addIonChamberParameter(getI0IonchamberParameters());
		fluorescenceParameters.addIonChamberParameter(getItIonchamberParameters());
		IonChamberParameters irefIonchambers = getIRefIonchamberParameters();
		irefIonchambers.setPercentAbsorption(67.0);
		irefIonchambers.setCurrentAmplifierName("Keithley");
		fluorescenceParameters.addIonChamberParameter(irefIonchambers);

		fluorescenceParameters.setMythenEnergy(9000);
		fluorescenceParameters.setMythenTime(10);

		expectedValue.setFluorescenceParameters(fluorescenceParameters);

		DetectorParameters d = createFromXML(PackageUtils.getTestPath(getClass())
				+ "DetectorParameters_withFluorescence.xml");

		if (!expectedValue.equals(d)) {
			fail("Values read are incorrect - " + d.toString());
		}
	}

	@Test
	public void testWriteToXML() throws Exception {
		DetectorParameters dp = new DetectorParameters();
		dp.addDetectorGroup(new DetectorGroup("transmission", new String[] { "counterTimer01" }));
		dp.addDetectorGroup(new DetectorGroup("Silicon", new String[] { "xmapMca", "counterTimer01" }));
		dp.addDetectorGroup(new DetectorGroup("Germanium", new String[] { "counterTimer02", "xspress2system",
				"counterTimer01" }));

		TransmissionParameters transmissionParameters = new TransmissionParameters();
		transmissionParameters.setDetectorType("transmission");
		transmissionParameters.setWorkingEnergy(7100.0);

		transmissionParameters.addIonChamberParameter(getI0IonchamberParameters());
		transmissionParameters.addIonChamberParameter(getItIonchamberParameters());
		transmissionParameters.addIonChamberParameter(getIRefIonchamberParameters());

		transmissionParameters.setMythenTime(1.);
		transmissionParameters.setCollectDiffractionImages(false);
		transmissionParameters.setMythenEnergy(9000.);
		dp.setTransmissionParameters(transmissionParameters);

		try {
			writeToXML(dp, testScratchDirectoryName + "DetectorParameters_written.xml");
		} catch (Exception e) {
			fail("Failed to write xml file - " + e.getCause().getMessage());
		}

		DetectorParameters s = createFromXML(testScratchDirectoryName + "DetectorParameters_written.xml");
		// ExafsValidator.getInstance().validate(s);
		if (!dp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}
	@Test
	public void createMinimalXml() throws Exception {
		DetectorParameters minParameters = new DetectorParameters();
		// need one or more detector groups, as a minimum

//		minParameters.addDetectorGroup(new DetectorGroup("transmission", new String[] { "counterTimer01" }));

		writeToXML(minParameters, testScratchDirectoryName + "DetectorParameters_minimal.xml");

		DetectorParameters writtenParameters = createFromXML(testScratchDirectoryName + "DetectorParameters_minimal.xml");
		assertEquals(minParameters, writtenParameters);
		assertTrue(writtenParameters.getDetectorGroups().isEmpty());
	}
	@Test
	public void createGenericParametersXml() throws Exception {
		DetectorParameters minParameters = new DetectorParameters();
		minParameters.setDetectorConfigurations(getDetectorConfigs());

	}
	private List<DetectorConfig> getDetectorConfigs() {
		DetectorConfig conf1 = new DetectorConfig("testDetector1");
		DetectorConfig conf2 = new DetectorConfig("testDetector2");
		conf2.setUseDetectorInScan(false);
		conf2.setDescription("Test detector #2");

		DetectorConfig conf3 = new DetectorConfig("testDetector3");
		conf3.setUseDetectorInScan(true);
		conf3.setScriptCommand("script command");
		conf3.setUseConfigFile(true);
		conf3.setConfigFileName("configFile.xml");

		return Arrays.asList(conf1, conf2, conf3);
	}

	@Test
	public void testWriteReadDetectorConfigs() throws Exception {
		String xmlFilePath = testScratchDirectoryName + "DetectorParameters_genericMinimal.xml";
		testWriteRead(getDetectorConfigs(), xmlFilePath);
	}

	@Test
	public void createIonchambersBeanXmlFile() throws Exception {
		String xmlFilePath = testScratchDirectoryName + "IonchambersBean.xml";

		IonChambersBean bean = new IonChambersBean();
		bean.setEnergy(7800.2);
		bean.addIonChamber(getI0IonchamberParameters());
		bean.addIonChamber(getItIonchamberParameters());
		bean.addIonChamber(getIRefIonchamberParameters());

		XMLHelpers.writeToXML(DetectorParameters.mappingURL, bean, xmlFilePath);

		IonChambersBean beanFromXml = XMLHelpers.createFromXML(DetectorParameters.mappingURL, IonChambersBean.class,
				DetectorParameters.schemaUrl, xmlFilePath);

		assertEquals("Energy is not correct", bean.getEnergy(), beanFromXml.getEnergy(), 1e-6);
		assertEquals("IonchambersBean does not have expected number of ionchambers", bean.getIonChambers().size(), beanFromXml.getIonChambers().size());
		assertEquals(bean.getIonChambers(),  beanFromXml.getIonChambers());
	}

	private void testWriteRead(List<DetectorConfig> detectorConfigs, String filePath) throws Exception {
		DetectorParameters detParams = new DetectorParameters();
		detParams.setDetectorConfigurations(detectorConfigs);

		writeToXML(detParams, filePath);

		DetectorParameters writtenParameters = createFromXML(filePath);
		assertEquals(detParams, writtenParameters);
	}
}
