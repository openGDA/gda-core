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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.findableHashtable.Hashtable;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A Class for performing junit tests on GDA FindableHashtable. Separate Java properties and object server XML files are
 * associated with and used by this test. Their location is constructed within the test. By default the tests are
 * assumed to be under the users home directory under ${user.home}/gda/src... unless the Java property gda.tests is set
 * AS A VM ARGUMENT when running the test. In the latter case the test's Java property and XML files are assumed to be
 * under ${gda.tests}/gda/src...
 */
public class FindableHashtableTest {
	private static Hashtable hashtable;
	private String key;
	private boolean b = true;
	private int i = 1234;
	private int iMax = Integer.MAX_VALUE;
	private int iMin = Integer.MIN_VALUE;
	private int l = 1234;
	private long lMax = Long.MAX_VALUE;
	private long lMin = Long.MIN_VALUE;
	private float f = 123.456f;
	private float fMax = Float.MAX_VALUE;
	private float fMin = Float.MIN_VALUE;
	private double d = 123.456;
	private double dMax = Double.MAX_VALUE;
	private double dMin = Double.MIN_VALUE;
	String hello = "Hello World!";

	/**
	 * Test setup to be run once at the start.
	 * @throws FactoryException
	 */
	@BeforeClass()
	public static void setUpBeforeClass() throws Exception {
		/*
		 * The following line is required to ensure that the default LocalProperties are obtained from the test's Java
		 * properties file. The property gda.propertiesFile must be set BEFORE LocalProperties is used and thus it's
		 * static block is invoked.
		 */
		System.setProperty("gda.propertiesFile", TestUtils.getResourceAsFile(FindableHashtableTest.class, "java_findableHashtable.properties")
				.getAbsolutePath());

		ObjectServer.createLocalImpl(TestUtils.getResourceAsFile(FindableHashtableTest.class, "server_findableHashtable.xml").getAbsolutePath());
		hashtable = (Hashtable) Finder.getInstance().find("GDAHashtable");
	}

	/**
	 * Test storing and retrieving data from the hashtable.
	 *
	 * @throws DeviceException
	 *             the device exception
	 */
	@Test
	public void testPutAndGet() throws DeviceException {
		if (hashtable == null) {
			fail("Hashtable could not be found");
		}
		key = "booleanTest";
		hashtable.putBoolean(key, b);
		assertEquals("boolean test failed", true, hashtable.getBoolean(key));

		key = "intTest";
		hashtable.putInt(key, i);
		assertEquals("int test failed", hashtable.getInt(key), i);
		hashtable.putInt(key, iMin);
		assertEquals("int test failed", hashtable.getInt(key), iMin);
		hashtable.putInt(key, iMax);
		assertEquals("int test failed", hashtable.getInt(key), iMax);

		key = "longTest";
		hashtable.putLong(key, l);
		assertEquals("long test failed", hashtable.getLong(key), l);
		hashtable.putLong(key, lMin);
		assertEquals("long test failed", hashtable.getLong(key), lMin);
		hashtable.putLong(key, lMax);
		assertEquals("long test failed", hashtable.getLong(key), lMax);

		key = "floatTest";
		hashtable.putFloat(key, f);
		assertEquals("float test failed", hashtable.getFloat(key), f, Double.MIN_VALUE);
		hashtable.putFloat(key, fMin);
		assertEquals("float test failed", hashtable.getFloat(key), fMin, Double.MIN_VALUE);
		hashtable.putFloat(key, fMax);
		assertEquals("float test failed", hashtable.getFloat(key), fMax, Double.MIN_VALUE);

		key = "doubleTest";
		hashtable.putDouble(key, d);
		assertEquals("double test failed", hashtable.getDouble(key), d, Double.MIN_VALUE);
		hashtable.putDouble(key, dMin);
		assertEquals("double test failed", hashtable.getDouble(key), dMin, Double.MIN_VALUE);
		hashtable.putDouble(key, dMax);
		assertEquals("double test failed", hashtable.getDouble(key), dMax, Double.MIN_VALUE);

		key = "StringTest";
		hashtable.putString(key, hello);
		assertEquals("String test failed", hashtable.getString(key), hello);

		TestClass tc = (TestClass) hashtable.get("ObjectTest1");
		assertNull("Hashtable test failed, returned object that shouldn't exist", tc);
		hashtable.put("ObjectTest", new TestClass(i, d));
		tc = (TestClass) hashtable.get("ObjectTest");
		assertNotNull("Hashtable test failed to return object", tc);
		assertEquals("Object int compare test failed", tc.getInt(), i);
		assertEquals("Object double compare test failed", tc.getDouble(), d, Double.MIN_VALUE);
	}
}
