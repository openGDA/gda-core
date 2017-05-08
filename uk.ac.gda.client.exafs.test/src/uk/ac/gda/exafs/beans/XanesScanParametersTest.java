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
import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.exafs.ui.describers.XanesDescriber;
import uk.ac.gda.util.PackageUtils;

/**
 * XanesScanParametersTest tests xml serialization and region checking.
 */
public class XanesScanParametersTest {
	private static final String testScratchDirectoryName = TestUtils
			.generateDirectorynameFromClassname(XanesScanParametersTest.class.getCanonicalName());

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		Factory testFactory = TestHelpers.createTestFactory("XanesScanParametersTest");
		DummyScannable qcm_energy = new DummyScannable();
		qcm_energy.setName("qcm_energy");
		testFactory.addFindable(qcm_energy);
		Finder.getInstance().addFactory(testFactory);
	}

	@Test
	public void testDescriber() {
		try {
			InputStream contents = new FileInputStream(new File(PackageUtils.getTestPath(getClass())
					+ "XanesScanParameters.xml"));
			XanesDescriber describer = new XanesDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Compare xml file with hard coded test class - they should be the same.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testCreateFromXML1() throws Throwable {
		XanesScanParameters sp = new XanesScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.addRegion(new Region(7050.0, 1.0, 0.5));
		sp.addRegion(new Region(7100.0, 2.0, 1.0));
		sp.addRegion(new Region(7200.0, 2.3, 1.8));
		sp.addRegion(new Region(7250.0, 8.0, 5.0));
		sp.setFinalEnergy(7300.0);

		XanesScanParameters s = XanesScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "XanesScanParameters.xml");
		validate(s);
		if (!sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
		s.checkRegions();

	}

	/**
	 * Compare xml file with hard coded test class - they should be the different.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testCreateFromXML2() throws Throwable {
		XanesScanParameters sp = new XanesScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.addRegion(new Region(0.0, 1.0, 0.5)); // Wrong
		sp.addRegion(new Region(7100.0, 2.0, 1.0));
		sp.addRegion(new Region(7200.0, 2.3, 1.8));
		sp.addRegion(new Region(7250.0, 8.0, 5.0));
		sp.setFinalEnergy(7300.0);

		XanesScanParameters s = XanesScanParameters.createFromXML(PackageUtils.getTestPath(getClass())
				+ "XanesScanParameters.xml");
		if (sp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
		if (!isInvalid(sp)) {
			sp.checkRegions();
			fail("Regions check failed when it should not - " + sp.toString());
		}

	}

	/**
	 * Compare class with one Region - should be allowed.
	 *
	 * @throws Throwable
	 */
	@Test
	public void testOneRegion() throws Throwable {
		XanesScanParameters sp = new XanesScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.setFinalEnergy(7050.0);

		validate(sp);
		sp.checkRegions();
	}

	/**
	 * Compare class with zero Region - should not be allowed.
	 * @throws Exception
	 */
	@Test
	public void testNoRegion() throws Exception {
		XanesScanParameters sp = new XanesScanParameters();
		sp.setElement("Fe");
		sp.setEdge("K");
		if (!isInvalid(sp)) {
			sp.checkRegions();
			fail("Regions check failed when it should not - " + sp.toString());
		}
	}

	/**
	 * Ensure that regions are increasing in energy
	 *
	 * @throws Throwable
	 */
	@Test
	public void testIncreasingRegionCheck() throws Throwable {
		XanesScanParameters sp = new XanesScanParameters();
		sp.setScannableName("qcm_energy");
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.addRegion(new Region(7050.0, 3.0, 1.0));
		sp.addRegion(new Region(7200.0, 3.0, 1.0));
		sp.setFinalEnergy(7100.0);
		if (!isInvalid(sp)) {
			sp.checkRegions();
			fail("Regions check failed when it should not - " + sp.toString());
		}

		sp = new XanesScanParameters();
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.addRegion(new Region(7050.0, 3.0, 1.0));
		sp.addRegion(new Region(7050.0, 3.0, 1.0));
		sp.setFinalEnergy(7200.0);
		if (!isInvalid(sp)) {
			sp.checkRegions();
			fail("Regions check failed when it should not - " + sp.toString());
		}

		sp = new XanesScanParameters();
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.addRegion(new Region(7050.0, 3.0, 1.0));
		sp.addRegion(new Region(7040.0, 3.0, 1.0));
		sp.setFinalEnergy(7200.0);
		if (!isInvalid(sp)) {
			sp.checkRegions();
			fail("Regions check failed when it should not - " + sp.toString());
		}

		sp = new XanesScanParameters();
		sp.setElement("Fe");
		sp.setEdge("K");
		sp.addRegion(new Region(7000.0, 3.0, 1.0));
		sp.setFinalEnergy(6080.0);
		if (!isInvalid(sp)) {
			sp.checkRegions();
			fail("Regions check failed when it should not - " + sp.toString());
		}

	}

	private void validate(XanesScanParameters o) {
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateXanesScanParametersForTest(o);
		if (errors.size() > 0) {
			fail(errors.get(0).getPrimaryMessage());
		}
	}

	private boolean isInvalid(XanesScanParameters o) {
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateXanesScanParametersForTest(o);
		return errors.size() != 0;
	}

}
