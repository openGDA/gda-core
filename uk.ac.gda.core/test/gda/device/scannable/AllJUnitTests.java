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

package gda.device.scannable;

import gda.device.scannable.component.MotorLimitsComponentTest;
import gda.device.scannable.scannablegroup.CoordinatedScannableElementTest;
import gda.device.scannable.scannablegroup.CoordinatedScannableGroupTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all JUnit tests in gda.device and sub-packages
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ConvertorScannableTest.class,
	ScannablePosTest.class,
	ScannableMotorTest.class,
	ScannableUtilsTest.class,
	TwoJawSlitsTest.class,
	ScannableGroupTest.class,
	CoordinatedScannableGroupTest.class,
	CoordinatedScannableElementTest.class,
	JEPScannableTest.class,
	ScannableBaseTest.class,
	ScannableMotionBaseTest.class,
	ScannableMotionUnitsBaseTest.class,
	PositionConvertorFunctionsTest.class,
	MotorLimitsComponentTest.class,
	ScannableMotionWithScannableFieldsBaseTest.class
	})

public class AllJUnitTests {
}