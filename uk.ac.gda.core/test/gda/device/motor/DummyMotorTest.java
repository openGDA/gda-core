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

//
// GenericOETest uses its own local properties and xml files. VM args
// "gda.propertiesFile" & "jacorb.config.dir" SHOULD NOT BE SET
//

package gda.device.motor;

import gda.device.motor.DummyMotor;
import gda.util.ObjectServer;

import java.io.File;

import junit.framework.TestCase;

/**
 *
 */
public class DummyMotorTest extends TestCase {
	ObjectServer os;

	// set up test environment, perhaps shouldn't be here but awaiting
	// review of
	// generic test environment setup procedure

	static String gdaRoot = System.getProperty("gda.src.java");

	static String oeDir = gdaRoot + "/tests/gda/oe";

	// simple xml file with Dummy motor Test01Dummy
	static String xmlFile = gdaRoot + "/tests/gda/stnSimulator/params/xml/simpleGenericOE.xml";

	static String propertiesFile = gdaRoot + "/tests/gda/stnSimulator/params/properties/java.properties";

	static String Test01Dummy = gdaRoot + "/tests/gda/stnSimulator/params/var/motorPositions/Test01Dummy";

	static String empty = gdaRoot + "/tests/gda/stnSimulator/params/var/motorPositions/empty";

	DummyMotor motor = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		System.setProperty("gda.propertiesFile", propertiesFile);
		os = ObjectServer.createLocalImpl(xmlFile.replace('\\', '/'));

		motor = new DummyMotor();
		motor.setName("Test01Dummy");
		new File(Test01Dummy).delete();
		new File(Test01Dummy).createNewFile();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testSetUp() {
		assertNotNull(gdaRoot);
		assertTrue(new File(xmlFile).exists());
		assertTrue(new File(Test01Dummy).exists());
		assertNotNull(motor);
		assertEquals(motor.getName(), "Test01Dummy");
	}

	/**
	 * 
	 */
	public void testGetPosn() {
		assertEquals(motor.getPosition(), 0.0, 0.0);
	}
}