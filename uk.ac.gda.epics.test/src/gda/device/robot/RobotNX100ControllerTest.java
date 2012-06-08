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

package gda.device.robot;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 */
public class RobotNX100ControllerTest {
	private RobotNX100Controller robot;
	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass(){
	}

	/**
	 */
	@Before
	public void setUp() {
		robot = new RobotNX100Controller();
		robot.setName("testRobot");
		robot.readTheFile();
	}

	/**
	 */
	@After
	public void tearDown() {
	}

	/**
	 * Test method for {@link gda.device.robot.RobotNX100Controller#lookupErrorCode(java.lang.String)}.
	 */
	@Test
	@Ignore("2010/01/20 Test ignored since not passing in Hudson GDA-2767")
	public void testLookupErrorCode() {
		Assert.assertEquals("Command hold, Check Door Interlock Failed", robot.lookupErrorCode("2050"));
	}
	/**
	 * 
	 */
	@Test
	public void printTable() {
		for (Object e : robot.getErrorMap().keySet()) {
			System.out.println(e.toString() + "\t" + robot.getErrorMap().get(e));
		}
	}

	/**
	 * make your JUnit 4 test classes accessible to a TestRunner designed to work with earlier versions of JUnit.
	 * @return @see junit.framework.Test
	 */
	public static junit.framework.Test suite() { 
	    return new JUnit4TestAdapter(RobotNX100ControllerTest.class); 
	}
}
