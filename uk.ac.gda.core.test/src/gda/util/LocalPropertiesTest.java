/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import gda.configuration.properties.LocalProperties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test that all the local properties are available (from java.properties)
 */
public class LocalPropertiesTest extends TestCase {
	/**
	 * @return suite
	 */
	public static Test suite() {
		return new TestSuite(LocalPropertiesTest.class);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	String testProperty = new String();

	@Override
	protected void setUp() {
	}

	@Override
	protected void tearDown() {
	}

	/**
	 * Tests that the properties are available through the LocalProperties object.
	 */
	public void testCorePropertiesExist() {
		testProperty = LocalProperties.get("user.home");
		assertTrue("Should contain user.home", testProperty != null);

		testProperty = LocalProperties.get("file.separator");
		assertTrue("Should contain file.separator", testProperty != null);

		testProperty = LocalProperties.get("gda.objectserver.mapping");
		assertTrue("Should contain gda.objectserver.mapping", testProperty != null);

		testProperty = LocalProperties.get("gda.factory.factoryName");
		assertTrue("Should contain gda.factory.factoryName", testProperty != null);

		testProperty = LocalProperties.get("gda.objectDelimiter");
		assertTrue("Should contain gda.objectDelimiter", testProperty != null);

		testProperty = LocalProperties.get("gda.gui.xml");
		assertTrue("Should contain gda.gui.xml", testProperty != null);

		// testProperty = LocalProperties.get("gda.progs.expt.guiTitle");
		// assertTrue("Should contain gda.progs.expt.guiTitle", testProperty !=
		// null);

		// testProperty = LocalProperties.get("gda.runNumberFile");
		// assertTrue("Should contain gda.runNumberFile", testProperty != null);

		testProperty = LocalProperties.get("gda.objectserver.xml");
		assertTrue("Should contain gda.objectserver.xml", testProperty != null);

		// testProperty = LocalProperties.get("gda.params");
		// assertTrue("Should contain gda.params", testProperty != null);

		testProperty = LocalProperties.get("gda.motordir");
		assertTrue("Should contain gda.motordir", testProperty != null);

		// testProperty = LocalProperties.get("gda.limitsdir");
		// assertTrue("Should contain gda.limitsdir", testProperty != null);

		testProperty = LocalProperties.get("gda.util.SplashScreen.splashImage");
		assertTrue("Should contain gda.util.SplashScreen.splashImage", testProperty != null);
	}

	/**
	 * Test Castor JDO properties exist.
	 */
	public void testCastorJDOPropertiesExist() {
		testProperty = LocalProperties.get("gda.objectserver.databaseConnectionXML");
		assertTrue("Should contain gda.objectserver.databaseConnectionXML", testProperty != null);

	}

	/**
	 * Test Scripting and Scanning properties exist.
	 */
	public void testScriptingScanningPropertiesExist() {
		testProperty = LocalProperties.get("gda.jython.translator.class");
		assertTrue("Should contain gda.jython.translator.class", testProperty != null);

		testProperty = LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR);
		assertTrue("Should contain gda.data.scan.datawriter.datadir", testProperty != null);

		testProperty = LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
		assertTrue("Should contain gda.data.scan.datawriter.dataFormat", testProperty != null);

		testProperty = LocalProperties.get("gda.data.scan.datawriter.srsStation");
		assertTrue("Should contain gda.data.scan.datawriter.srsStation", testProperty != null);

		testProperty = LocalProperties.get("gda.data.scan.datawriter.srsProject");
		assertTrue("Should contain gda.data.scan.datawriter.srsProject", testProperty != null);

		testProperty = LocalProperties.get("gda.data.scan.datawriter.srsExperiment");
		assertTrue("Should contain gda.data.scan.datawriter.srsExperiment", testProperty != null);

		testProperty = LocalProperties.get("gda.jython.socket");
		assertTrue("gda.jython.socket: should give a port number", testProperty != null);
	}

	/**
	 * Test Exafs properties exist.
	 */
	public void testExafsPropertiesExist() {
		testProperty = LocalProperties.get("gda.exafs.channels");
		assertTrue("Should contain gda.exafs.channels", testProperty != null);

		testProperty = LocalProperties.get("gda.exafs.minEnergy");
		assertTrue("Should contain gda.exafs.minEnergy", testProperty != null);

		testProperty = LocalProperties.get("gda.exafs.maxEnergy");
		assertTrue("Should contain gda.exafs.maxEnergy", testProperty != null);

		testProperty = LocalProperties.get("gda.exafs.twoD");
		assertTrue("Should contain gda.exafs.twoD", testProperty != null);

		testProperty = LocalProperties.get("gda.exafs.buttonFontSize");
		assertTrue("Should contain gda.exafs.buttonFontSize", testProperty != null);
	}

	/**
	 * Test VUV properties exist.
	 */
	public void testVuvPropertiesExist() {
		testProperty = LocalProperties.get("gda.vuv.secondaryMono");
		assertTrue("Should contain gda.vuv.secondaryMono", testProperty != null);

		testProperty = LocalProperties.get("gda.vuv.resolutionFile");
		assertTrue("Should contain resolutionFileName", testProperty != null);

		testProperty = LocalProperties.get("gda.data.path");
		assertTrue("Should contain gda.data.path", testProperty != null);
	}

	/**
	 * Test NCD properties exist.
	 */
	public void testNCDPropertiesExist() {
		testProperty = LocalProperties.get("gda.ncd.imagedir");
		assertTrue("Should contain gda.ncd.imagedir", testProperty != null);

		testProperty = LocalProperties.get("gda.ncd.initialDirectory");
		assertTrue("Should contain gda.ncd.initialDirectory", testProperty != null);

		testProperty = LocalProperties.get("gda.device.xspress.configFileName");
		assertTrue("Should contain gda.device.xspress.configFileName", testProperty != null);
	}

	/**
	 * Test images properties exist.
	 */
	public void testImagesPropertiesExist() {
		testProperty = LocalProperties.get("gda.images.cameraManConfigFile");
		assertTrue("Should contain gda.images.cameraManConfigFile", testProperty != null);
	}

	/**
	 * Test PX properties exist.
	 */
	public void testPxPropertiesExist() {
		testProperty = LocalProperties.get("gda.px.phiSetProtectionLevel");
		assertTrue("Should contain gda.px.phiSetProtectionLevel", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenShowingFile");
		assertTrue("Should contain gda.px.pxgenShowingFile", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenTitle");
		assertTrue("Should contain gda.px.pxgenTitle", testProperty != null);

		testProperty = LocalProperties.get("gda.px.javaSwitches");
		assertTrue("Should contain gda.px.javaSwitches", testProperty != null);

		testProperty = LocalProperties.get("gda.px.detector");
		assertTrue("Should contain gda.px.detector", testProperty != null);

		testProperty = LocalProperties.get("gda.px.exposure");
		assertTrue("Should contain gda.px.exposure", testProperty != null);

		testProperty = LocalProperties.get("gda.px.options");
		assertTrue("Should contain gda.px.options", testProperty != null);

		testProperty = LocalProperties.get("gda.px.monofile");
		assertTrue("Should contain gda.px.monofile", testProperty != null);

		testProperty = LocalProperties.get("gda.px.twoD");
		assertTrue("Should contain gda.px.twoD", testProperty != null);

		testProperty = LocalProperties.get("gda.px.detector.safeDistance");
		assertTrue("Should contain gda.px.detector.safeDistance", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgen.pxgenLogFile");
		assertTrue("Should contain gda.px.pxgen.pxgenLogFile", testProperty != null);

		testProperty = LocalProperties.get("gda.px.controller.commandIP");
		assertTrue("Should contain gda.px.controller.commandIP", testProperty != null);

		testProperty = LocalProperties.get("gda.px.controller.statusIP");
		assertTrue("Should contain gda.px.controller.statusIP", testProperty != null);

		testProperty = LocalProperties.get("gda.px.mono.lowerlimit");
		assertTrue("Should contain gda.px.mono.lowerlimit", testProperty != null);

		testProperty = LocalProperties.get("gda.px.mono.upperlimit");
		assertTrue("Should contain gda.px.mono.upperlimit", testProperty != null);

		testProperty = LocalProperties.get("gda.px.fluorescence");
		assertTrue("Should contain gda.px.fluorescence", testProperty != null);

		testProperty = LocalProperties.get("gda.px.gripCommand");
		assertTrue("Should contain gda.px.gripCommand", testProperty != null);

		testProperty = LocalProperties.get("gda.px.gripStopString");
		assertTrue("Should contain gda.px.gripStopString", testProperty != null);

		testProperty = LocalProperties.get("gda.px.scan345Command");
		assertTrue("Should contain gda.px.scan345Command", testProperty != null);

		testProperty = LocalProperties.get("gda.px.scan345StopString");
		assertTrue("Should contain gda.px.scan345StopString", testProperty != null);

		testProperty = LocalProperties.get("gda.px.detector.CCDDir");
		assertTrue("Should contain gda.px.detector.CCDDir", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgen.tidyup");
		assertTrue("Should contain gda.px.pxgen.tidyup", testProperty != null);

		testProperty = LocalProperties.get("gda.px.factoryname");
		assertTrue("Should contain gda.px.factoryname", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenShowingFile");
		assertTrue("Should contain gda.px.pxgenShowingFile", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenFraction");
		assertTrue("Should contain gda.px.pxgenFraction", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenWidth");
		assertTrue("Should contain gda.px.pxgenWidth", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenHeight");
		assertTrue("Should contain gda.px.pxgenHeight", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenDir");
		assertTrue("Should contain gda.px.pxgenDir", testProperty != null);

		testProperty = LocalProperties.get("gda.px.detector.safeDistance");
		assertTrue("Should contain gda.px.detector.safeDistance", testProperty != null);

		testProperty = LocalProperties.get("gda.px.pxgenTitle");
		assertTrue("Should contain gda.px.pxgenTitle", testProperty != null);

		testProperty = LocalProperties.get("gda.px.mono.limits");
		assertTrue("Should contain gda.px.mono.limits", testProperty != null);

		testProperty = LocalProperties.get("gda.px.detectorDistanceProtectionLevel");
		assertTrue("Should contain gda.px.detectorDistanceProtectionLevel", testProperty != null);
	}
}
