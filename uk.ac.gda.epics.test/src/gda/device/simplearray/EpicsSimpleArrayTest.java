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

package gda.device.simplearray;

import static org.junit.Assert.*;

import gda.device.DeviceException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // No suitable PV on dasc-epics simulation for this test yet.
public class EpicsSimpleArrayTest {

	EpicsSimpleArray simpleArray;
	String testString;
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	@AfterClass
	public static void tearDownAfterClass() {
	}

	@Before
	public void setUp() throws Exception {
		simpleArray = new EpicsSimpleArray();
		simpleArray.setPvName("CS-CS-MSTAT-01:MESS01"); // read-only PV
		simpleArray.configure();
		testString = "Beamline Sim";
//		testAsynchronousMoveTo();
	}
	@After
	public void tearDown() throws Exception {
		simpleArray.destroy();
	}

	public void testAsynchronousMoveTo() throws DeviceException {
		simpleArray.asynchronousMoveTo(testString);
	}
	@Test
	public void testGetPosition() throws DeviceException {
		String result = String.valueOf(simpleArray.getPosition());
		assertTrue(result.equals(testString));
	}
	@Test
	public void testGetValue() throws DeviceException {
		String result = simpleArray.getValue();
		assertTrue(result.equals(testString));
	}
}
