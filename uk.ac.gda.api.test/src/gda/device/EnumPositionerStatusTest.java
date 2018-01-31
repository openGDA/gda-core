/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EnumPositionerStatusTest {

	@Test
	public void testValue() {
		assertEquals(0, EnumPositionerStatus.IDLE.value());
		assertEquals(1, EnumPositionerStatus.MOVING.value());
		assertEquals(2, EnumPositionerStatus.ERROR.value());
	}

	@Test
	public void testFromInt() {
		assertEquals(EnumPositionerStatus.IDLE, EnumPositionerStatus.from_int(0));
		assertEquals(EnumPositionerStatus.MOVING, EnumPositionerStatus.from_int(1));
		assertEquals(EnumPositionerStatus.ERROR, EnumPositionerStatus.from_int(2));
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testFromIntTooHigh() {
		EnumPositionerStatus.from_int(3);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testFromIntTooLow() {
		EnumPositionerStatus.from_int(-1);
	}

	@Test
	public void testToString() {
		assertEquals("idle", EnumPositionerStatus.IDLE.toString());
		assertEquals("moving", EnumPositionerStatus.MOVING.toString());
		assertEquals("error", EnumPositionerStatus.ERROR.toString());
	}
}
