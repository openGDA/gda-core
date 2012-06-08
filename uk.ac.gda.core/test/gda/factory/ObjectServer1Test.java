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

import gda.util.ObjectServer;

import java.util.List;

import junit.framework.TestCase;
import gda.testlib.GDASetup;
import gda.testlib.GeneralSetup;

/**
 * JUnit case to test GDA EventServer starts successfully The name server is required and is started from here. The name
 * and event server are not left running when the test finishes.
 */
public class ObjectServer1Test extends TestCase {

	private ObjectServer objectServer;

	private GDASetup gdaSetup;

	/**
	 * Constructor needed if only one test method needs running
	 * 
	 * @param testName
	 *            specific test to run
	 */
	public ObjectServer1Test(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// use complete general Junit test environment and GDA name & event
		// server
		GeneralSetup.getInstance().setUpAll();
		gdaSetup = GDASetup.getInstance();
		gdaSetup.setUpSimProperties();
		gdaSetup.setUpNameServer();
		gdaSetup.setUpEventServer();

		// launch the object server and save the object reference
		objectServer = ObjectServer.createServerImpl();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		gdaSetup.tearDownAll();
	}

	/**
	 * A dummy test method to refactor out
	 */
	public void testDumbo() {
		List<String> findableNames = objectServer.getFindableNames();
		assertNotNull(findableNames);
		// TODO lots more tests
	}

}
