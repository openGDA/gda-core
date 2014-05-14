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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.device.DeviceException;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the JEPScannable class except for the parts which require calls to evaluate a Jython command rather than simply
 * get an object from the namespace.
 */
public class JEPScannableTest {

	private DummyScannable scannable1;
	private DummyScannable scannable2;
	private int a;
	private int b;
	private MockJythonServerFacade jsf;

	@Before
	public void createJythonNamespace() {
		scannable1 = new DummyScannable("scannable1", 8);
		scannable2 = new DummyScannable("scannable2", 5);
		a = 1;
		b = 7;
		jsf = new MockJythonServerFacade();
		jsf.placeInJythonNamespace("scannable1", scannable1);
		jsf.placeInJythonNamespace("scannable2", scannable2);
		jsf.placeInJythonNamespace("a", a);
		jsf.placeInJythonNamespace("b", b);
		InterfaceProvider.setJythonNamespaceForTesting(jsf);

	}

	@Test
	public void testWithScannable() throws Exception {
		JEPScannable jep = JEPScannable.createJEPScannable("test1", "scannable2", "%6.2f", null, "scannable2");
		try {
			assertEquals(5.0, jep.getPosition());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testWithScannables() throws Exception  {
		JEPScannable jep = JEPScannable.createJEPScannable("test2", null, "%6.2f", null, "scannable1 + scannable2");
		try {
			assertEquals(13.0, jep.getPosition());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testWithScannableAndJythonVariable() throws Exception {
		JEPScannable jep = JEPScannable.createJEPScannable("test3", "scannable2", "%6.2f", null, "scannable2 * b");
		try {
			assertEquals(35.0, jep.getPosition());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testWithJythonVariablesOnly()throws Exception {
		JEPScannable jep = JEPScannable.createJEPScannable("test4", null, "%6.2f", null, "a + b");
		try {
			assertEquals(8.0, jep.getPosition());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}

	}
	
	@Test
	public void testWithJythonVariablesWithConstant()throws Exception {
		JEPScannable jep = JEPScannable.createJEPScannable("test4", null, "%6.2f", null, "a + 10");
		try {
			assertEquals(11.0, jep.getPosition());
		} catch (DeviceException e) {
			fail(e.getMessage());
		}

	}

}
