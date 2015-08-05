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
import gda.util.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.describers.XspressDescriber;
import uk.ac.gda.util.PackageUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 *
 */
public class XspressParametersTest {
	final static String testScratchDirectoryName = TestUtils
			.generateDirectorynameFromClassname(XspressParametersTest.class.getCanonicalName());

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	@Test
	public void testDescriber() {
		try {
			InputStream contents = new FileInputStream(new File(PackageUtils.getTestPath(getClass())
					+ "Xspress_Parameters.xml"));
			XspressDescriber describer = new XspressDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Got broken at one point so have simple test for it.
	 */
	@Test
	public void testZeros() throws Exception {

		final XspressDeadTimeParameters s1 = (XspressDeadTimeParameters) XMLHelpers.createFromXML(XspressDeadTimeParameters.mappingURL,
				XspressDeadTimeParameters.class, XspressDeadTimeParameters.schemaURL, new File(PackageUtils.getTestPath(getClass())
						+ "Xspress_DeadTime_Parameters.xml"));

		if (s1.getDetectorDT(0).getProcessDeadTimeAllEventGradient() == 0)
			throw new Exception("Could not read in processDeadTimeAllEventGradient!");
	}

	/**
	 * Got broken at one point so have simple test for it.
	 */
	@Test
	public void testCreateFromXMLWithClass() {
		try {
			final XspressParameters s1 = (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL,
					XspressParameters.class, XspressParameters.schemaURL, new File(PackageUtils.getTestPath(getClass())
							+ "Xspress_Parameters.xml"));

			XMLHelpers.writeToXML(XspressParameters.mappingURL, s1, testScratchDirectoryName
					+ "XspressParameters_written.xml");

			final XspressParameters s2 = (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL,
					XspressParameters.class, XspressParameters.schemaURL, new File(testScratchDirectoryName
							+ "XspressParameters_written.xml"));

			if (!s1.equals(s2))
				throw new Exception("Written and original files should be the same!");

		} catch (Exception ex) {
			fail("Invalid exception thrown - " + ex.getCause().getMessage());
		}
	}

}
