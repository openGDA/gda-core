/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.microfocus.beans;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import gda.util.TestUtils;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class MicroFocusScanParametersTest {
	final static String testScratchDirectoryName =
		TestUtils.generateDirectorynameFromClassname(MicroFocusScanParametersTest.class.getCanonicalName());

	@BeforeClass
	public static void beforeClass() throws Exception{
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}
	@Test
	public void testCreateFromXMLWithClass() {
		try {
			final MicroFocusScanParameters s = (MicroFocusScanParameters)XMLHelpers.createFromXML(MicroFocusScanParameters.mappingURL,
					MicroFocusScanParameters.class,
					MicroFocusScanParameters.schemaUrl,
					new File("testfiles/uk/ac/gda/microfocus/beans/MicroFocusScanParametersTest/MicroFocus_Parameters.xml"));
			System.out.println(s);
		} catch (Exception ex) {
			fail("Invalid exception thrown - " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testCreateFromXML()  throws Exception{
		MicroFocusScanParameters mfp = new MicroFocusScanParameters();
		mfp.setXStart(2.0);
		mfp.setXEnd(8.0);
		mfp.setXStepSize(1.0);
		mfp.setYStart(0.0);
		mfp.setYEnd(4.0);
		mfp.setYStepSize(1.0);
		mfp.setCollectionTime(1.0);
		mfp.setEnergy(3500.0);
		mfp.setZValue(2.5);
		mfp.setRowTime(10.0);
		MicroFocusScanParameters s = MicroFocusScanParameters.createFromXML("testfiles/uk/ac/gda/microfocus/beans/MicroFocusScanParametersTest/MFParameters_Valid.xml");
		if (!mfp.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	@Test
	public void testWriteToXML() throws Exception
	{
		MicroFocusScanParameters mfp = new MicroFocusScanParameters();
		mfp.setEnergy(1000.0);
		mfp.setXStart(2.0);
		mfp.setXEnd(8.0);
		mfp.setXStepSize(1.0);
		mfp.setYStart(0.0);
		mfp.setYEnd(4.0);
		mfp.setYStepSize(1.0);
		mfp.setCollectionTime(1.0);
		mfp.setEnergy(3500.0);
		mfp.setZValue(2.5);
		mfp.setRowTime(10.0);
		MicroFocusScanParameters.writeToXML(mfp, testScratchDirectoryName + "MFParameters_written.xml");
		MicroFocusScanParameters mfp2 = MicroFocusScanParameters.createFromXML(testScratchDirectoryName + "MFParameters_written.xml");
		if (!mfp.equals(mfp2)) {
			fail("Values read are incorrect - " + mfp2.toString());
		}
	}
}
