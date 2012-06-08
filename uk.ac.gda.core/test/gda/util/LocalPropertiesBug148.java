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
import junit.framework.TestCase;

/**
 * LocalPropertiesBug148 Class
 */
public class LocalPropertiesBug148 extends TestCase {

	private final String test_NAME = "test.name", test_VALUE = "test value", test1_NAME = "test.name1",
			test1_VALUE = "test value1";

	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		LocalProperties.get("user.home");
		System.setProperty(test_NAME, test_VALUE);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for LocalPropertiesBug148.
	 * 
	 * @param arg0
	 */
	public LocalPropertiesBug148(String arg0) {
		super(arg0);
	}

	/**
	 * Class under test for String get(String)
	 */
	public void testGet() {
		String testValue = LocalProperties.get(test_NAME);
		assertEquals(test_VALUE, testValue);

		System.setProperty(test1_NAME, test1_VALUE);

		testValue = LocalProperties.get(test_NAME);
		assertEquals(test_VALUE, testValue);
	}

	/**
	 * Class under test for String set(String key, String val)
	 */
	public void testSet() {
		LocalProperties.set(test1_NAME, test1_VALUE);
		String test1Value = LocalProperties.get(test1_NAME);
		assertEquals(test1_VALUE, test1Value);

		test1Value = System.getProperty(test1_NAME);
		assertEquals(test1_VALUE, test1Value);

	}

}
