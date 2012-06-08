/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.util.simpleServlet;

import junit.framework.TestCase;

/**
 * Tests {@link SimpleServlet}.
 */
public class SimpleServletTest extends TestCase {

	/**
	 * Method used in tests.
	 * 
	 * @return a fixed string
	 */
	public static String noArgsMethod() {
		return "noargresult";
	}
	
	/**
	 * Method used in tests.
	 * 
	 * @param str a string
	 * 
	 * @return the string parameter
	 */
	public static String oneStringArgMethod(String str) {
		return str;
	}
	
	static String str;
	
	static Object obj;
	
	/**
	 * Method used in tests.
	 * 
	 * @param o an object
	 */
	public static void oneObjectArgMethod(Integer o) {
		obj = o;
	}
	
	/**
	 * Method used in tests.
	 * 
	 * @param s a string parameter
	 * @param o an object parameter
	 */
	public static void twoArgMethod(String s, Integer o) {
		str = s;
		obj = o;
	}
	
	/**
	 * Tests invoking a method with no arguments.
	 * 
	 * @throws Exception
	 */
	public void testInvokingNoArgsMethod() throws Exception {
		Object o = SimpleServlet.execute(SimpleServletTest.class.getName() + "?noArgsMethod");
		assertEquals("noargresult", o);
	}
	
	/**
	 * Tests invoking a method with a single String parameter.
	 * 
	 * @throws Exception
	 */
	public void testInvokingOneStringArgMethod() throws Exception {
		String arg = "stringarg";
		Object o = SimpleServlet.execute(SimpleServletTest.class.getName() + "?oneStringArgMethod?" + arg);
		assertEquals(arg, o);
	}
	
	/**
	 * Tests invoking a method with a single Object parameter.
	 * 
	 * @throws Exception
	 */
	public void testInvokingOneObjectArgMethod() throws Exception {
		obj = null;
		Integer arg = 1;
		assertNull(obj);
		SimpleServlet.execute(SimpleServletTest.class.getName() + "?oneObjectArgMethod", arg);
		assertEquals(arg, obj);
	}
	
	/**
	 * Tests invoking a method with a String and Object parameter.
	 * 
	 * @throws Exception
	 */
	public void testInvokingTwoObjectArgMethod() throws Exception {
		str = null;
		obj = null;
		String arg1 = "test";
		Integer arg2 = 1;
		assertNull(str);
		assertNull(obj);
		SimpleServlet.execute(SimpleServletTest.class.getName() + "?twoArgMethod?" + arg1, arg2);
		assertEquals(arg1, str);
		assertEquals(arg2, obj);
	}
	
}
