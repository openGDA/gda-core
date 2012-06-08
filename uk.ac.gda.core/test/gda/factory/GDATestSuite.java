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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runs all the JUnit test cases.
 * <P>
 * Make sure to reference all the correct test xml files and test databases from java.properties:
 * <P>
 * 1. the database must be referenced from the gda.objectserver.xml and gda.objectserver.mapping values
 * <P>
 * 2. the test xml files for xml castor testing must be referenced in references called:
 * tests.gda.objectserver.xml.mapping and tests.gda.objectserver.xml.
 * <P>
 * so the java.properties file has line such as:
 * <P>
 * tests.gda.objectserver.xml.mapping = D:/GDA/stationXX/src/java/tests/gda/factory/mapping.xml
 * <P>
 * tests.gda.objectserver.xml = D:/GDA/stationXX/src/java/tests/gda/factory/test.xml
 * <P>
 * gda.objectserver.xml = database
 * <P>
 * gda.objectserver.mapping = D:/GDA/stationXX/src/java/tests/gda/factory/db_mapping.xml
 * <P>
 * gda.objectserver.databaseConnectionXML = D:/GDA/stationXX/src/java/tests/gda/factory/database_test.xml
 */
public class GDATestSuite extends TestCase {
	/**
	 * @return test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("All Test Cases");
		// add all tests here
		suite.addTest(ObjectServerTest.suite());
		// suite.addTest(GenericOETest.suite());
		// suite.addTest(JDOObjectServerTest.suite());
		// suite.addTest(NetServiceTest.suite());
		return suite;
	}

	/**
	 * Main method to run test suite.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
