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

package gda.epics.util;

import gda.epics.util.EpicsPVs;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * EpicsPVsTest Class
 */
public class EpicsPVsTest extends TestCase {
	private EpicsPVs epv;

	/**
	 * @param name
	 */
	public EpicsPVsTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.tearDown();
		epv = new EpicsPVs();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		epv = null;
	}

	/**
	 * 
	 */
	public void testAddPV() {
		int i;
		i = epv.addPV("name1", "pv1");
		System.out.println("Number of nodes:" + i);
		i = epv.addPV("name2", "pv2");
		System.out.println("Number of nodes:" + i);

		// assertEquals(true, true);

	}

	/**
	 * 
	 */
	public void testPreScanSave() {
		this.testAddPV();
		String fn = epv.preScanSave();
		System.out.println(fn);
		// this.assertEquals("what is it?", fn);
	}

	/**
	 * 
	 */
	public void testAfterScanSave() {
		this.testAddPV();
		String fn = epv.preScanSave();
		System.out.println(fn);
	}

	/**
	 * @return suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		// suite.addTest(new EpicsPVsTest("testAddPV"));
		// @Ignore("2010/01/20 testPreScanSave ignored since not passing in Hudson GDA-2768")
		// suite.addTest(new EpicsPVsTest("testPreScanSave"));
		// suite.addTest(new EpicsPVsTest("testAfterScanSave"));

		return suite;
	}

}
