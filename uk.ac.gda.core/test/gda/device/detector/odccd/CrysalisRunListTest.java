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

package gda.device.detector.odccd;

import gda.util.TestUtils;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * 
 */
public class CrysalisRunListTest {
	static String testScratchDirectoryName = null;

	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception if setup fails
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(CrysalisRunListTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}	

	/**
	 * Test writing/reading and comparison on Crysalis RunLists
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void test() throws Exception {
		CrysalisRunList outList = new CrysalisRunList();
		outList.name = "outList";
		outList.cexperimentdir = "cexperimentdir";
		outList.cexperimentname = "cexperimentname";
		outList.dwtotalnumofframes = 20;
		outList.inumofreferenceruns = 21;
		outList.inumofruns = 22;
		outList.wisreferenceframes = 23;
		outList.wreferenceframefrequency = 24;
		outList.wversioninfo = 25;
		outList.runFolder = "runFolder";
		outList.runFile = "runFile";
		CrysalisRun run1 = new CrysalisRun();
		run1.name = "run1";
		run1.domegaindeg = 0.5;
		run1.ddetectorindeg = 1.5;
		run1.dexposuretimeinsec = 8.5;
		run1.dkappaindeg = 2.5;
		run1.dphiindeg = Double.MAX_VALUE;
		run1.dscanendindeg = 3.5;
		run1.dscanspeedratio = 7.5;
		run1.dscanstartindeg = 3.1;
		run1.dscanwidthindeg = 6.5;
		run1.dwnumofframes = 10;
		run1.dwnumofframesdone = 11;
		run1.inum = 12;
		run1.irunscantype = 4;
		outList.addRun(run1);
		CrysalisRun run2 = CrysalisRun.newInstance(run1);
		run2.name = "run2";
		run2.inum = 2;
		run2.irunscantype = 0;
		run2.domegaindeg = Double.MAX_VALUE;
		run2.dphiindeg = 3.5;
		run2.dscanendindeg = .9;
		run2.dscanspeedratio = 7.5;
		run2.dscanstartindeg = .1;
		outList.addRun(run2);

		XMLHelpers.writeToXML(CrysalisRunList.mappingURL, outList, testScratchDirectoryName + "CrysalisSimple1.xml");

		CrysalisRunList inList = (CrysalisRunList)XMLHelpers.createFromXML(CrysalisRunList.mappingURL,
														CrysalisRunList.class,
														CrysalisRunList.schemaURL,
														testScratchDirectoryName + "CrysalisSimple1.xml");
		Assert.assertEquals(outList, inList);

		String configPath = testScratchDirectoryName + "CrysalisRunListValidator.xml";
		CrysalisRunListValidator validator1 = new CrysalisRunListValidator(configPath, true);
		validator1.saveToConfig(configPath);
		CrysalisRunListValidator validator2 = new CrysalisRunListValidator(configPath, false);
		Assert.assertEquals(validator1, validator2);

		URL validatorConfigURL = CrysalisRunListTest.class.getResource("TestFiles/CrysalisRunListValidator.xml");
		if (validatorConfigURL == null) {
			 throw new IllegalArgumentException(
					 "Unable to find CrysalisRunListValidator.xml. Ensure it is in the classpath - set output folder for test folder to test");
		}
		configPath = validatorConfigURL.getPath();
		CrysalisRunListValidator validator = new CrysalisRunListValidator(configPath, true);

		validator.checkValidity(inList);
	}
}
