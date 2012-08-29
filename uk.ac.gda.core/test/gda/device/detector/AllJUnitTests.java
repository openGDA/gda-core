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

package gda.device.detector;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all the tests in gda.device.detector and sub-packages
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	gda.device.detector.NexusDetectorWritingTest.class,
	gda.device.detector.odccd.AllJUnitTests.class,
	gda.device.detector.pco4000.AllJUnitTests.class,
	gda.device.detector.countertimer.AllJUnitTests.class,
	gda.device.detector.DummyHardwareTriggerableAreaDetectorTest.class,
	gda.device.detector.DummyHardwareTriggerableDetectorTest.class,
	gda.device.detector.NXDetectorTest.class})
public class AllJUnitTests {
}