/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.beans;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * Run all JUnit tests in this package
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	DetectorParametersTest.class,
//	ExafsDOETest.class,  class deleted as DOE not used and the was a clash in parameter types and getting the UI to work properly
	OutputParametersTest.class,
	XanesScanParametersTest.class,
	XasScanParametersTest.class,
	XspressParametersTest.class
})

public class AllJUnitTests {
}

