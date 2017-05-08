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

import gda.TestHelpers;
import gda.device.scannable.DummyScannable;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.util.TestUtils;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.exafs.ui.describers.XasDescriber;
import uk.ac.gda.util.PackageUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class XasScanParametersTest {
	private static final String testScratchDirectoryName = TestUtils
			.generateDirectorynameFromClassname(XasScanParametersTest.class.getCanonicalName());

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		Factory testFactory = TestHelpers.createTestFactory("XanesScanParametersTest");
		DummyScannable qcm_energy = new DummyScannable();
		qcm_energy.setName("qcm_energy");
		testFactory.addFindable(qcm_energy);
		DummyScannable energy = new DummyScannable();
		energy.setName("energy");
		testFactory.addFindable(energy);
		Finder.getInstance().addFactory(testFactory);
	}

	@Test
	public void testDescriber() {
		try {
			InputStream contents = new FileInputStream(new File(PackageUtils.getTestPath(getClass())
					+ "XAS_Parameters.xml"));
			XasDescriber describer = new XasDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Got broken at one point so have simple test for it.
	 */
	@Test
	public void testCreateFromXMLWithClass() {
		try {
			final XasScanParameters s = (XasScanParameters) XMLHelpers.createFromXML(XasScanParameters.mappingURL,
					XasScanParameters.class, XasScanParameters.schemaUrl, new File(PackageUtils.getTestPath(getClass())
							+ "XAS_Parameters.xml"));
			System.out.println(s);
		} catch (Exception ex) {
			fail("Invalid exception thrown - " + ex.getCause().getMessage());
		}
	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#createFromXML(java.lang.String)}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXML() throws Exception {
		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.setInitialEnergy(7000.0);
		sp.setFinalEnergy(7200.0);
		sp.setEdgeEnergy(7112.0);
		sp.setCoreHole(1.25);
		sp.setGaf1(15.0);
		sp.setGaf2(10.0);
		sp.setPreEdgeStep(4.0);
		sp.setPreEdgeTime(1.0);
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsTime(1.5);
		sp.setExafsStepType("E");

		XasScanParameters s = XasScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "ScanParameters_Valid.xml");
		validate(s);
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#createFromXML(java.lang.String)}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXMLValid2() throws Exception {
		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.setInitialEnergy(7000.0);
		sp.setFinalEnergy(7200.0);
		sp.setEdgeEnergy(7112.0);
		sp.setCoreHole(1.25);
		sp.setGaf1(15.0);
		sp.setGaf2(10.0);
		sp.setPreEdgeStep(4.0);
		sp.setPreEdgeTime(1.0);
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsFromTime(1.5);
		sp.setExafsToTime(3.0);
		sp.setKWeighting(1.0);
		sp.setExafsStepType("E");

		XasScanParameters s = XasScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "ScanParameters_Valid2.xml");
		validate(s);
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}

	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#createFromXML(java.lang.String)}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXMLValid3() throws Exception {
		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.setInitialEnergy(7000.0);
		sp.setFinalEnergy(7200.0);
		sp.setEdgeEnergy(7112.0);
		sp.setCoreHole(1.25);
		sp.setGaf1(15.0);
		sp.setGaf2(10.0);
		sp.setGaf3(7.0);
		sp.setPreEdgeStep(4.0);
		sp.setPreEdgeTime(1.0);
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsFromTime(1.5);
		sp.setExafsToTime(3.0);
		sp.setKWeighting(1.0);
		sp.setExafsStepType("E");

		XasScanParameters s = XasScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "ScanParameters_Valid3.xml");
		validate(s);

		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}

	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#createFromXML(java.lang.String)}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXMLwithAB() throws Exception {
		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.setInitialEnergy(7000.0);
		sp.setFinalEnergy(7200.0);
		sp.setEdgeEnergy(7112.0);
		sp.setA(7090.0);
		sp.setB(7100.0);
		sp.setPreEdgeStep(4.0);
		sp.setPreEdgeTime(1.0);
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsTime(1.5);
		sp.setExafsStepType("E");

		XasScanParameters s = XasScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "ScanParameters_ValidAB.xml");
		validate(s);

		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	/**
	 * Test method for {@link uk.ac.gda.beans.exafs.XasScanParameters#createFromXML(java.lang.String)}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXMLwithABC() throws Exception {
		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.setInitialEnergy(7000.0);
		sp.setFinalEnergy(7200.0);
		sp.setEdgeEnergy(7112.0);
		sp.setA(7090.0);
		sp.setB(7100.0);
		sp.setC(7130.0);
		sp.setPreEdgeStep(4.0);
		sp.setPreEdgeTime(1.0);
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsTime(1.5);
		sp.setExafsStepType("E");

		XasScanParameters s = XasScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "ScanParameters_ValidABC.xml");
		validate(s);
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	/**
	 * Test method for
	 * {@link uk.ac.gda.beans.exafs.XasScanParameters#writeToXML(uk.ac.gda.beans.exafs.XasScanParameters, java.lang.String)}
	 * .
	 *
	 * @throws Exception
	 */
	@Test
	public void testWriteToXML() throws Exception {
		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.setInitialEnergy(7000.0);
		sp.setFinalEnergy(7200.0);
		sp.setEdgeEnergy(7112.0);
		sp.setCoreHole(1.25);
		sp.setGaf1(15.0);
		sp.setGaf2(10.0);
		sp.setPreEdgeStep(4.0);
		sp.setPreEdgeTime(1.0);
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsTime(1.5);
		sp.setExafsStepType("E");

		try {
			XasScanParameters.writeToXML(sp, testScratchDirectoryName + "ScanParameters_written.xml");
		} catch (Exception e) {
			fail("Failed to write xml file - " + e.getCause().getMessage());
		}

		XasScanParameters s = XasScanParameters.createFromXML(testScratchDirectoryName + "ScanParameters_written.xml");
		validate(s);
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	@Test
	public void testValidation() {

		XasScanParameters sp = new XasScanParameters();
		sp.setScannableName("qcm_energy"); // Error
		sp.setElement("Fred");
		sp.setEdge("J1");
		sp.setInitialEnergy(7090.0);
		sp.setFinalEnergy(7000.0); // Error,
		sp.setEdgeEnergy(7112.0);
		sp.setCoreHole(1.25);
		sp.setGaf1(15.0);
		sp.setGaf2(20.0); // Error
		sp.setPreEdgeStep(100.0);// Error
		sp.setPreEdgeTime(-1.0); // Error
		sp.setEdgeStep(0.5);
		sp.setEdgeTime(1.0);
		sp.setExafsStep(3.0);
		sp.setExafsTime(1.5);
		sp.setExafsStepType("Q"); // Error but at XML level

		if (!isInvalid(sp)) {
			fail("Invalid parameters not identified by validation.");
		}
	}

	private void validate(XasScanParameters o) {
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateXasScanParametersForTest(o, 2000, 35000);
		if (errors.size() > 0) {
			fail(errors.get(0).getPrimaryMessage());
		}
	}

	private boolean isInvalid(XasScanParameters o) {
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateXasScanParametersForTest(o, 2000, 35000);
		return errors.size() != 0;
	}
}
