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

package gda.device.scannable.component;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;

public class MotorLimitsComponentTest {

	private Motor motor;
	private MotorLimitsComponent lc;

	@Before
	public void setUp() {
		motor = mock(Motor.class);
		when(motor.getName()).thenReturn("name");
		lc = new MotorLimitsComponent(motor);
	}

	@Test
	public void testGetInternalLower() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(-1.);
		assertArrayEquals(new Double[]{-1.}, lc.getInternalLower());
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		assertEquals(null, (Object) lc.getInternalLower());
	}

	@Test
	public void testGetInternalUpper() throws MotorException, DeviceException {
		when(motor.getMaxPosition()).thenReturn(1.);
		assertArrayEquals(new Double[]{1.}, lc.getInternalUpper());
		when(motor.getMaxPosition()).thenReturn(Double.NaN);
		assertEquals(null, (Object) lc.getInternalUpper());
	}

	@Test
	public void testCheckInternalPositionNoLimits() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.getMaxPosition()).thenReturn(Double.NaN);
		assertEquals(null, lc.checkInternalPosition(new Double[]{1.}));
	}

	@Test
	public void testCheckInternalPositionBothLimits() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(-1.);
		when(motor.getMaxPosition()).thenReturn(1.);
		assertEquals(null, lc.checkInternalPosition(new Double[]{0.}));
		assertEquals("Motor limit violation on motor name: 1.100000 > 1.000000 (internal/hardware/dial values).", lc.checkInternalPosition(new Double[]{1.1}));
		assertEquals("Motor limit violation on motor name: -1.100000 < -1.000000 (internal/hardware/dial values).", lc.checkInternalPosition(new Double[]{-1.1}));
	}

	@Test
	public void testCheckInternalPositionLowLimits() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(-1.);
		when(motor.getMaxPosition()).thenReturn(Double.NaN);
		assertEquals(null, lc.checkInternalPosition(new Double[]{0.}));
		assertEquals("Motor limit violation on motor name: -1.100000 < -1.000000 (internal/hardware/dial values).", lc.checkInternalPosition(new Double[]{-1.1}));
	}

	@Test
	public void testCheckInternalPositionHighLimits() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.getMaxPosition()).thenReturn(1.);
		assertEquals(null, lc.checkInternalPosition(new Double[]{0.}));
		assertEquals("Motor limit violation on motor name: 1.100000 > 1.000000 (internal/hardware/dial values).", lc.checkInternalPosition(new Double[]{1.1}));
	}

	@Test
	public void testSetInternalLower1() throws MotorException, DeviceException {
		when(motor.getMaxPosition()).thenReturn(1.);
		when(motor.isLimitsSettable()).thenReturn(true);
		lc.setInternalLower(new Double[] {-2.});
		verify(motor).setSoftLimits(-2, 1);
	}

	@Test
	public void testSetInternalLower2() throws MotorException, DeviceException {
		when(motor.getMaxPosition()).thenReturn(Double.NaN);
		when(motor.isLimitsSettable()).thenReturn(true);
		lc.setInternalLower(new Double[] {-2.});
		verify(motor).setSoftLimits(-2, Double.NaN);
	}

	@Test
	public void testSetInternalUpper1() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(-1.);
		when(motor.isLimitsSettable()).thenReturn(true);
		lc.setInternalUpper(new Double[] {2.});
		verify(motor).setSoftLimits(-1., 2.);
	}
	@Test
	public void testSetInternalUpper2() throws MotorException, DeviceException {
		when(motor.getMinPosition()).thenReturn(Double.NaN);
		when(motor.isLimitsSettable()).thenReturn(true);
		lc.setInternalUpper(new Double[] {2.});
		verify(motor).setSoftLimits(Double.NaN, 2.);
	}

}
