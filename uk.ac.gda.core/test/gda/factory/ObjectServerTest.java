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

package gda.factory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.amplifier.Keithley;
import gda.device.gpib.DummyGpib;
import gda.device.memory.Gdhist;
import gda.device.modulator.PEM90;
import gda.device.motor.DummyMotor;
import gda.device.motor.McLennan600Motor;
import gda.device.motor.McLennanController;
import gda.device.motor.McLennanServoMotor;
import gda.device.motor.McLennanStepperMotor;
import gda.device.motor.NewportMotor;
import gda.device.motor.Parker6kControllerEnet;
import gda.device.motor.Parker6kMotor;
import gda.device.serial.SerialComm;
import gda.device.temperature.Eurotherm2000;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the creation of an ObjectServer using Castor XML, and that the objects in the server have been filled properly.
 * <P>
 * Note that these tests are performed without configuring the ObjectServer which is created!
 * <P>
 * This uses the file test.xml. It does not require a event or channel server process to be running.
 */

public class ObjectServerTest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(ObjectServerTest.class);

	static String gdaRoot = LocalProperties.getRoot();

	static String factoryDir = gdaRoot + File.separator + "src" + File.separator + "java" + File.separator + "gda"
			+ File.separator + "factory";

	static String testFactoryDir = gdaRoot + File.separator + "src" + File.separator + "java" + File.separator
			+ File.separator + "tests" + File.separator + "gda" + File.separator + "factory";

	static String xmlFile = (testFactoryDir + File.separator + "test.xml").replace('\\', '/');

	static String mappingFile = (factoryDir + File.separator + "mapping.xml").replace('\\', '/');

	static String jacorbDir = (gdaRoot + File.separator + "params" + File.separator + "properties").replace('\\', '/');

	static String propertiesFile = (gdaRoot + File.separator + "params" + File.separator + "properties"
			+ File.separator + "java.properties").replace('\\', '/');

	/**
	 * @return test suite
	 */
	public static Test suite() {
		return new TestSuite(ObjectServerTest.class);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	// fixtures to test on
	List<Findable> findables = null;

	Factory factory = null;

	Finder finder = null;

	int expectedNoFindables = 29;

	@Override
	protected void setUp() throws FactoryException, MalformedURLException {
		logger.debug("gdaRoot = " + gdaRoot);
		System.setProperty("gda.propertiesFile", propertiesFile);
		System.setProperty("jacorb.config.dir", jacorbDir);
		XmlObjectCreator oc = new XmlObjectCreator();
		oc.setXmlFile(xmlFile);
		oc.setMappingFile(mappingFile);
		factory = oc.getFactory();
		findables = factory.getFindables();
		// add the object factory the the finder
		finder = Finder.getInstance();
		finder.addFactory(factory);
	}

	@Override
	protected void tearDown() {

	}

	/**
	 * 
	 */
	public void testGdaRoot() {
		assertNotNull(gdaRoot);
	}

	/**
	 * 
	 */
	public void testFiles() {
		assertNotNull(xmlFile);
		assertNotNull(mappingFile);
		assertNotNull(propertiesFile);
		assertNotNull(jacorbDir);
	}

	/**
	 * 
	 */
	public void testFactoryName() {
		assertEquals("TestFactory", factory.getName());
	}

	/**
	 * 
	 */
	public void testExpecteNumberFindables() {
		assertEquals(expectedNoFindables, findables.size());
	}

	/**
	 * 
	 */
	public void testDummyMotor() {
		// get object from finder
		DummyMotor d = (DummyMotor) finder.find("Test01Dummy");
		// test it is of correct class
		assertEquals("gda.device.motor.DummyMotor", d.getClass().getName());
		// test its name
		assertEquals("Test01Dummy", d.getName());
		// test its other attributes
		assertEquals(2.0, d.getBacklashSteps(), 0.0);
		assertEquals(50, d.getFastSpeed(), 0);
		assertEquals(25, d.getMediumSpeed(), 0);
		assertEquals(5, d.getSlowSpeed(), 0);
	}

	/**
	 * 
	 */
	public void testParkerController() {
		Parker6kControllerEnet pce = (Parker6kControllerEnet) finder.find("Test01Controller");
		assertEquals("gda.device.motor.Parker6kControllerEnet", pce.getClass().getName());
		assertEquals("Test01Controller", pce.getName());
		assertEquals("testHostname", pce.getHost());
		assertEquals(5002, pce.getPort(), 0);
	}

	/**
	 * 
	 */
	public void testParkerMotor() {
		Parker6kMotor pm = (Parker6kMotor) finder.find("Test01Parker");
		assertEquals("gda.device.motor.Parker6kMotor", pm.getClass().getName());
		assertEquals("Test01Parker", pm.getName());
		assertEquals(50, pm.getFastSpeed(), 0);
		assertEquals(25, pm.getMediumSpeed(), 0);
		assertEquals(5, pm.getSlowSpeed(), 0);
		assertEquals(1, pm.getAxisNo(), 0);
		assertTrue(pm.isStepper());
		assertEquals(-1000000.0, pm.getMinPosition(), 0);
		assertEquals(1000000.0, pm.getMaxPosition(), 0);
		assertEquals(0, pm.getMinSpeed(), 0);
		assertEquals(2000, pm.getMaxSpeed(), 0);
	}

	/**
	 * 
	 */
	public void testKeithley() {
		Keithley k = (Keithley) finder.find("Test01Keithley");
		assertEquals("gda.device.amplifier.Keithley", k.getClass().getName());
		assertEquals("Test01Keithley", k.getName());
		assertEquals("Test01Gpib", k.getGpibInterfaceName());
		assertEquals("dev1", k.getDeviceName());
	}

	/**
	 * 
	 */
	public void testGdhist() {
		Gdhist gd = (Gdhist) finder.find("TestMemory");
		assertEquals("gda.device.memory.Gdhist", gd.getClass().getName());
		assertEquals("TestMemory", gd.getName());
		assertEquals("TestdaServer", gd.getDaServerName());
		int[] dims = { 512, 1 };
		assertEquals(dims, gd.getDimension());
		assertEquals("test startup", gd.getStartupScript());
		assertEquals("test size", gd.getSizeCommand());
		assertEquals("test open", gd.getOpenCommand());
	}

	/**
	 * 
	 */
	public void testEuroTherm() {
		Eurotherm2000 eu = (Eurotherm2000) finder.find("TestEurotherm");
		assertEquals("gda.device.temperature.Eurotherm2000", eu.getClass().getName());
		assertEquals("TestEurotherm", eu.getName());
		assertEquals("testPort", eu.getSerialDeviceName());
		assertEquals(2.0, eu.getAccuracy(), 0);
		assertEquals(5000, eu.getPolltime(), 0);
		assertEquals(0, eu.getGid(), 0);
		assertEquals(1, eu.getUid(), 0);
		try {
			ArrayList<String> probes = eu.getProbeNames();
			assertEquals("Internal", probes.get(0));
			assertEquals("External", probes.get(1));
		} catch (DeviceException ex) {
			logger.debug(ex.getStackTrace().toString());
		}
	}

	/**
	 * 
	 */
	public void testSerialComm() {
		SerialComm com = (SerialComm) finder.find("COM4");
		assertEquals("gda.device.serial.SerialComm", com.getClass().getName());
		assertEquals("COM4", com.getName());
		assertEquals(9600, com.getBaudRate());
		assertEquals(7, com.getByteSize());
		assertEquals(1, com.getStopBits(), 0);
		assertEquals("space", com.getParity());
	}

	/**
	 * 
	 */
	public void testMclennanController() {
		McLennanController control = (McLennanController) finder.find("w0_Controller");
		assertEquals("gda.device.motor.McLennanController", control.getClass().getName());
		assertEquals("w0_Controller", control.getName());
		assertEquals("w0", control.getSerialDeviceName());
	}

	/**
	 * 
	 */
	public void testMclennanStepper() {
		McLennanStepperMotor step = (McLennanStepperMotor) finder.find("EOB_YMotor");
		assertEquals("gda.device.motor.McLennanStepperMotor", step.getClass().getName());
		assertEquals("EOB_YMotor", step.getName());
		assertEquals(4, step.getAxis());
		assertEquals(800.0, step.getBacklashSteps(), 0);
	}

	/**
	 * 
	 */
	public void testMclennanServo() {
		McLennanServoMotor servo = (McLennanServoMotor) finder.find("AlphaMotor");
		assertEquals("gda.device.motor.McLennanServoMotor", servo.getClass().getName());
		assertEquals("AlphaMotor", servo.getName());
		assertEquals(6, servo.getAxis());
		assertEquals(260.0, servo.getBacklashSteps(), 0);
	}

	/**
	 * 
	 */
	public void testMclennan600() {
		McLennan600Motor sixHundred = (McLennan600Motor) finder.find("PhiMotor");
		assertEquals("gda.device.motor.McLennan600Motor", sixHundred.getClass().getName());
		assertEquals("PhiMotor", sixHundred.getName());
		assertEquals(4, sixHundred.getAxis());
		assertEquals(800.0, sixHundred.getBacklashSteps(), 0);
	}

	/**
	 * 
	 */
	public void testPem90() {
		PEM90 pem90 = (PEM90) finder.find("PRS232");
		assertEquals("gda.device.modulator.PEM90", pem90.getClass().getName());
		assertEquals("PRS232", pem90.getName());
		assertEquals("w0", pem90.getSerialDeviceName());
	}

//	/**
//	 * 
//	 */
//	public void testTriaxController() {
//		TriaxControllerGPIB triControl = (TriaxControllerGPIB) finder.find("TCGPIB");
//		assertEquals("gda.vuv.spectrometer.TriaxControllerGPIB", triControl.getClass().getName());
//		assertEquals("TCGPIB", triControl.getName());
//		assertEquals("dev1", triControl.getDeviceName());
//		assertEquals("gpib", triControl.getGpibInterfaceName());
//	}

//	/**
//	 * 
//	 */
//	public void testTriaxMotor() {
//		TriaxMotor triMotor = (TriaxMotor) finder.find("gmotor");
//		assertEquals("gda.device.motor.TriaxMotor", triMotor.getClass().getName());
//		assertEquals("gmotor", triMotor.getName());
//		assertEquals("TCGPIB", triMotor.getTriaxControllerName());
//		assertEquals("MONO", triMotor.getIdentifier());
//		assertEquals(1, triMotor.getSlitNumber());
//	}

	/**
	 * 
	 */
	public void testDummyGpib() {
		DummyGpib testObj = (DummyGpib) finder.find("Test01Gpib");
		assertEquals("gda.device.gpib.DummyGpib", testObj.getClass().getName());
		assertEquals("Test01Gpib", testObj.getName());
	}

//	/**
//	 * 
//	 */
//	public void testEpicsMotor() {
//		EpicsMotor testObj = (EpicsMotor) finder.find("TSXXX-MO-HSLIT-01:NEGB:MOT");
//		assertEquals("gda.device.motor.EpicsMotor", testObj.getClass().getName());
//		assertEquals("TSXXX-MO-HSLIT-01:NEGB:MOT", testObj.getName());
//		assertEquals(50, testObj.getFastSpeed());
//		assertEquals(25, testObj.getMediumSpeed());
//		assertEquals(5, testObj.getSlowSpeed());
//	}




	/**
	 * 
	 */
	public void testNewPortMotor() {
		NewportMotor testObj = (NewportMotor) finder.find("newportmoptor01");
		assertEquals("gda.device.motor.NewportMotor", testObj.getClass().getName());
		assertEquals("newportmoptor01", testObj.getName());
		assertEquals("newportcontroller01", testObj.getNewportControllerName());
	}
}
