/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gda.device.DeviceException;

import org.junit.Before;
import org.junit.Test;

public class DummyEpicsMonitorDoubleTest {

	private static final double LOWER_LIMIT = -0.6;
	private static final double UPPER_LIMIT = 2.3;
	private static final double INCREMENT = 0.15;
	private static final double INITIAL_POSITION = 0.2;

	private DummyEpicsMonitorDouble monitor;

	@Before
	public void setUp() {
		monitor = new DummyEpicsMonitorDouble();
		monitor.setValue(INITIAL_POSITION);
	}

	private void setUpIncrement() {
		monitor.setLowerLimit(LOWER_LIMIT);
		monitor.setUpperLimit(UPPER_LIMIT);
		monitor.setIncrement(INCREMENT);
	}

	@Test
	public void testNoIncrement() throws DeviceException {
		for (int i = 0; i < 20; i++) {
			assertEquals(INITIAL_POSITION, monitor.getPosition());
		}
	}

	@Test
	public void testIncrementWithinLimits() throws DeviceException {
		setUpIncrement();
		assertEquals(INITIAL_POSITION + INCREMENT, (double) monitor.getPosition(), 0.001);
		assertEquals(INITIAL_POSITION + 2 * INCREMENT, (double) monitor.getPosition(), 0.001);
	}

	@Test
	public void testLimits() throws DeviceException {
		// Test that the value keeps varying but stays within limits
		// subject to inaccuracies in double.
		setUpIncrement();
		double lastPos = INITIAL_POSITION;
		for (int i = 0; i < 100; i++) {
			final double currentPos = (double) monitor.getPosition();
			assertTrue(Math.abs(currentPos - lastPos) > 0.001);
			assertTrue(currentPos - UPPER_LIMIT < 0.001);
			assertTrue(currentPos - LOWER_LIMIT > 0.001);
			lastPos = currentPos;
		}
	}
}
