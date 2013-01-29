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

package gda.device;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all JUnit tests in gda.device and sub-packages
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	gda.device.adc.AllJUnitTests.class,
	gda.device.detector.AllJUnitTests.class,
	gda.device.memory.AllJUnitTests.class,
	gda.device.timer.AllJUnitTests.class,
	gda.device.scannable.AllJUnitTests.class,
	DeviceBaseTest.class
})

public class AllJUnitTests {
}