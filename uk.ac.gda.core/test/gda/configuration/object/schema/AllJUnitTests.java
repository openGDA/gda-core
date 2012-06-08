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

package gda.configuration.object.schema;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all JUnit tests in the {@code gda.configuration.object.schema} package.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
// ObjectFactoryValidationTest is not part of AllJUnitTests - it tests
// configurations, and is run from the configuration test suite, not the core
// test suite.
// gda.configuration.object.schema.ObjectFactoryValidationTest.class
})
public class AllJUnitTests {
}