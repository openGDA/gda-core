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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.util.TestUtils;
import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
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
		return (DetectorParameters) XMLHelpers.createFromXML(DetectorParameters.mappingURL, DetectorParameters.class,
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
		IonChamberParameters ionChamberParameters = new IonChamberParameters();
		ionChamberParameters.setName("I0");
		ionChamberParameters.setDeviceName("counterTimer01");
		ionChamberParameters.setChannel(1);
		ionChamberParameters.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters.setGain("1 nA/V");
		ionChamberParameters.setGasType("Ar");
		ionChamberParameters.setPercentAbsorption(70.0);
		transmissionParameters.addIonChamberParameter(ionChamberParameters);

		IonChamberParameters ionChamberParameters2 = new IonChamberParameters();
		ionChamberParameters2.setName("It");
		ionChamberParameters2.setDeviceName("counterTimer01");
		ionChamberParameters2.setChannel(2);
		ionChamberParameters2.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters2.setGain("1 nA/V");
		ionChamberParameters2.setGasType("Ar");
		ionChamberParameters2.setPercentAbsorption(67.0);
		transmissionParameters.addIonChamberParameter(ionChamberParameters2);

		IonChamberParameters ionChamberParameters3 = new IonChamberParameters();
		ionChamberParameters3.setName("Iref");
		ionChamberParameters3.setDeviceName("counterTimer01");
		ionChamberParameters3.setChannel(3);
		ionChamberParameters3.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters3.setGain("1 nA/V");
		ionChamberParameters3.setGasType("Ar");
		ionChamberParameters3.setPercentAbsorption(83.0);
		transmissionParameters.addIonChamberParameter(ionChamberParameters3);
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
		IonChamberParameters ionChamberParameters = new IonChamberParameters();
		ionChamberParameters.setName("I0");
		ionChamberParameters.setDeviceName("counterTimer01");
		ionChamberParameters.setChannel(1);
		ionChamberParameters.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters.setGain("1 nA/V");
		ionChamberParameters.setGasType("Ar");
		ionChamberParameters.setPercentAbsorption(70.0);
		fluorescenceParameters.addIonChamberParameter(ionChamberParameters);

		IonChamberParameters ionChamberParameters2 = new IonChamberParameters();
		ionChamberParameters2.setName("It");
		ionChamberParameters2.setDeviceName("counterTimer01");
		ionChamberParameters2.setChannel(2);
		ionChamberParameters2.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters2.setGain("1 nA/V");
		ionChamberParameters2.setGasType("Ar");
		ionChamberParameters2.setPercentAbsorption(67.0);
		fluorescenceParameters.addIonChamberParameter(ionChamberParameters2);

		IonChamberParameters ionChamberParameters3 = new IonChamberParameters();
		ionChamberParameters3.setName("Iref");
		ionChamberParameters3.setDeviceName("counterTimer01");
		ionChamberParameters3.setChannel(3);
		ionChamberParameters3.setCurrentAmplifierName("Keithley");
		ionChamberParameters3.setGain("1 nA/V");
		ionChamberParameters3.setGasType("Ar");
		ionChamberParameters3.setPercentAbsorption(67.0);
		fluorescenceParameters.addIonChamberParameter(ionChamberParameters3);

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
		IonChamberParameters ionChamberParameters = new IonChamberParameters();
		ionChamberParameters.setName("I0");
		ionChamberParameters.setDeviceName("counterTimer01");
		ionChamberParameters.setChannel(1);
		ionChamberParameters.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters.setGain("1 nA/V");
		ionChamberParameters.setGasType("Xe");
		ionChamberParameters.setPercentAbsorption(70.0);
		transmissionParameters.addIonChamberParameter(ionChamberParameters);

		IonChamberParameters ionChamberParameters2 = new IonChamberParameters();
		ionChamberParameters2.setName("It");
		ionChamberParameters2.setDeviceName("counterTimer01");
		ionChamberParameters2.setChannel(2);
		ionChamberParameters2.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters2.setGain("1 nA/V");
		ionChamberParameters2.setGasType("Xe");
		ionChamberParameters2.setPercentAbsorption(67.0);
		transmissionParameters.addIonChamberParameter(ionChamberParameters2);

		IonChamberParameters ionChamberParameters3 = new IonChamberParameters();
		ionChamberParameters3.setName("Iref");
		ionChamberParameters3.setDeviceName("counterTimer01");
		ionChamberParameters3.setChannel(3);
		ionChamberParameters3.setCurrentAmplifierName("epicsKeithley");
		ionChamberParameters3.setGain("1 nA/V");
		ionChamberParameters3.setGasType("Ar");
		ionChamberParameters3.setPercentAbsorption(83.0);
		transmissionParameters.addIonChamberParameter(ionChamberParameters3);
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
}
