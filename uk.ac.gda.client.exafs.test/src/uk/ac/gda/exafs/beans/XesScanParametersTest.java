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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.util.TestUtils;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.exafs.ui.describers.XesScanParametersDescriber;
import uk.ac.gda.util.PackageUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class XesScanParametersTest {
	private final String xesParametersFilePath = PackageUtils.getTestPath(getClass()) + "XES_Parameters.xml";
	private final String detectorParametersFilePath = PackageUtils.getTestPath(getClass()) + "DetectorParameters_withXES.xml";

	private static final String testScratchDirectoryName =
		TestUtils.generateDirectorynameFromClassname(XesScanParametersTest.class.getCanonicalName());

	private static XesScanParameters createFromXML(String filename) throws Exception {
		return (XesScanParameters) XMLHelpers.createFromXML(XesScanParameters.mappingURL, XesScanParameters.class,
				XesScanParameters.schemaURL, filename);
	}

	private static void writeToXML(XesScanParameters params, String filename) throws Exception {
		XMLHelpers.writeToXML(XesScanParameters.mappingURL, params, filename);
	}

	private static DetectorParameters createDetectorsFromXML(String filename) throws Exception {
		return (DetectorParameters) XMLHelpers.createFromXML(DetectorParameters.mappingURL, DetectorParameters.class,
				DetectorParameters.schemaUrl, filename);
	}

	@BeforeClass
	public static void beforeClass() throws Exception{
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	@Test
	public void testDescriber() {
		try {
			final InputStream contents = new FileInputStream(new File(xesParametersFilePath));
			final XesScanParametersDescriber describer = new XesScanParametersDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}


	/**
	 * Got broken at one point so have simple test for it.
	 */
	@Test
	public void testCreateFromXMLWithClass() throws Exception {
		final File testFile = new File(xesParametersFilePath);
		final XesScanParameters s = (XesScanParameters)XMLHelpers.createFromXML(XesScanParameters.mappingURL,
									XesScanParameters.class,
									XesScanParameters.schemaURL,
									testFile);
		System.out.println(s);
	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#createFromXML(java.lang.String)}.
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXML()  throws Exception{
		final XesScanParameters sp = new XesScanParameters();
		sp.setScanType(1);
		sp.setMonoEnergy(15000d);
		sp.setXesInitialEnergy(15000d);
		sp.setXesFinalEnergy(16000d);
		sp.setXesStepSize(1d);
		sp.setXesIntegrationTime(1d);
		sp.setAdditionalCrystal0(false);
		sp.setAdditionalCrystal1(false);
		sp.setAdditionalCrystal2(false);
		sp.setAdditionalCrystal3(false);

		final XesScanParameters s = createFromXML(xesParametersFilePath);
		final DetectorParameters d = createDetectorsFromXML(detectorParametersFilePath);
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateXesScanParametersForTest(s,d);
		if (errors.size() > 0){
			fail(errors.get(0).getPrimaryMessage());
		}
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#writeToXML(uk.ac.gda.beans.exafs.XasScanParameters, java.lang.String)}.
	 * @throws Exception
	 */
	@Test
	public void testWriteToXML()  throws Exception{
		final XesScanParameters sp = new XesScanParameters();
		sp.setScanType(1);
		sp.setMonoEnergy(15000d);
		sp.setXesInitialEnergy(15000d);
		sp.setXesFinalEnergy(16000d);
		sp.setXesStepSize(1d);
		sp.setXesIntegrationTime(1d);
		sp.setAdditionalCrystal0(false);
		sp.setAdditionalCrystal1(false);
		sp.setAdditionalCrystal2(false);
		sp.setAdditionalCrystal3(false);

		try {
			writeToXML(sp, testScratchDirectoryName + "XesScanParameters_written.xml");
		} catch (Exception e) {
			fail("Failed to write xml file - " + e.getCause().getMessage());
		}

		final XesScanParameters s = createFromXML(testScratchDirectoryName + "XesScanParameters_written.xml");
		final DetectorParameters d = createDetectorsFromXML(detectorParametersFilePath);
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateXesScanParametersForTest(s,d);
		if (errors.size() > 0){
			fail(errors.get(0).getPrimaryMessage());
		}
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

}
