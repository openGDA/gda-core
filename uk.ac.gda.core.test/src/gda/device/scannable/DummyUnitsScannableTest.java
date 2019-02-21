/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;

public class DummyUnitsScannableTest {
	private static final String SCANNABLE_NAME = "dummyScannable";
	private static final String HARDWARE_UNITS = "mm";
	private static final String USER_UNITS = "µm";

	// Tolerance for imprecision of floating-point calculations
	private static final double FP_TOLERANCE = 0.00001;

	private DummyUnitsScannable scannable;

	@Before
	public void setUp() throws DeviceException {
		scannable = new DummyUnitsScannable(SCANNABLE_NAME, 0, HARDWARE_UNITS, USER_UNITS);
	}

	@Test
	public void testInitialState() throws DeviceException {
		assertEquals(SCANNABLE_NAME, scannable.getName());
		final String[] inputNames = scannable.getInputNames();
		assertEquals(1, inputNames.length);
		assertEquals(SCANNABLE_NAME, inputNames[0]);
		assertEquals(0.0, (double) scannable.getPosition(), FP_TOLERANCE);
		assertEquals("mm", scannable.getHardwareUnitString());
		assertEquals(USER_UNITS, scannable.getUserUnits());
		assertTrue(scannable.isConfigured());
	}

	@Test
	public void testAsynchronousMoveTo() throws DeviceException {
		scannable.asynchronousMoveTo(245.89);
		// Check that user units (microns) have been translated to hardware units (millimetres)
		assertEquals(0.24589, scannable.getCurrentPosition(), FP_TOLERANCE);
	}

	@Test(expected = NullPointerException.class)
	public void testAsynchronousMoveToInvalidPosition() throws DeviceException {
		// This test documents the current behaviour.
		// We may want throw a more specific exception.
		scannable.asynchronousMoveTo("In");
	}

	@Test
	public void testGetPosition() throws DeviceException {
		scannable.setCurrentPosition(7.2134);
		// Check that hardware units are translated to user units
		assertEquals(7213.4, (double) scannable.getPosition(), FP_TOLERANCE);
	}

	@Test
	public void testMoveToAndGetPositionAresymmetrical() throws DeviceException {
		final double position = 9.236;
		scannable.asynchronousMoveTo(position);
		assertEquals(position, (double) scannable.getPosition(), FP_TOLERANCE);
	}
}
