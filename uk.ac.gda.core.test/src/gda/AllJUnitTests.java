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

package gda;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all JUnit tests in gda*
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
//	gda.analysis.datavector.AllJUnitTests.class,
//	gda.analysis.io.NexusLoaderTest.class,
//	gda.analysis.FitterTest.class,
	gda.data.AllJUnitTests.class,
	gda.device.AllJUnitTests.class,
	//gda.epics.AllJUnitTests.class,
	// NOTE gda.exafs.AllJUnitTests moved to exafs plugin. Presumably there
	// is an OSGi mechanism or other which allows plugins with
	// different tests to be run.
	//gda.exafs.AllJUnitTests.class,
	gda.jython.AllJUnitTests.class,
	gda.jscience.AllJUnitTests.class,
	gda.scan.AllJUnitTests.class,
	gda.util.AllJUnitTests.class
})

public class AllJUnitTests {
}
