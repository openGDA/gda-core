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

import junit.extensions.RepeatedTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Class to exercise stnSimulator name, event and object servers using repeated tests.
 */
public class FactoryTests {
	private static int totalRuns = 10;

	/**
	 * Use this constructure to set specific numbers of tests
	 * 
	 * @param runs
	 *            numbers of test runs to complete
	 */
	public FactoryTests(int runs) {
		totalRuns = runs;
	}

	/**
	 * @return test suite
	 */
	public static Test suite() {
		// build a suite containing all 3 server test cases
		TestSuite suite = new TestSuite("Mike tests for tests.gda.factory");
		suite.addTestSuite(EventServerTest.class);
		suite.addTestSuite(ObjectServer1Test.class);

		// use a container so whole suite is run repeatedly
		TestSuite containerSuite = new TestSuite("Container for Mike tests");
		containerSuite.addTest(new RepeatedTest(suite, totalRuns));
		return containerSuite;
	}
}
