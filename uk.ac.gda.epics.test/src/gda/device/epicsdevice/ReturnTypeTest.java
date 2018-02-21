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

package gda.device.epicsdevice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import gda.epics.connection.EpicsController.MonitorType;

public class ReturnTypeTest {

	@Test
	public void testValue() {
		assertEquals(0, ReturnType.DBR_NATIVE.value());
		assertEquals(1, ReturnType.DBR_STS.value());
		assertEquals(2, ReturnType.DBR_TIME.value());
		assertEquals(3, ReturnType.DBR_CTRL.value());
		assertEquals(4, ReturnType.DBR_GR.value());
		assertEquals(5, ReturnType.DBR_UNKNOWN.value());
	}

	@Test
	public void testFromInt() {
		assertEquals(ReturnType.DBR_NATIVE, ReturnType.from_int(0));
		assertEquals(ReturnType.DBR_STS, ReturnType.from_int(1));
		assertEquals(ReturnType.DBR_TIME, ReturnType.from_int(2));
		assertEquals(ReturnType.DBR_CTRL, ReturnType.from_int(3));
		assertEquals(ReturnType.DBR_GR, ReturnType.from_int(4));
		assertEquals(ReturnType.DBR_UNKNOWN, ReturnType.from_int(5));
		assertNull(ReturnType.from_int(6));
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testFromIntTooHigh() {
		ReturnType.from_int(7);
	}

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testFromIntTooLow() {
		ReturnType.from_int(-1);
	}

	@Test
	public void testToString() {
		assertEquals("NATIVE", ReturnType.DBR_NATIVE.toString());
		assertEquals("STS", ReturnType.DBR_STS.toString());
		assertEquals("TIME", ReturnType.DBR_TIME.toString());
		assertEquals("CTRL", ReturnType.DBR_CTRL.toString());
		assertEquals("GR", ReturnType.DBR_GR.toString());
		assertEquals("CTRL", ReturnType.DBR_UNKNOWN.toString());
	}

	@Test
	public void testGetTrueReturnType() {
		assertEquals(MonitorType.NATIVE, ReturnType.DBR_NATIVE.getTrueReturnType());
		assertEquals(MonitorType.STS, ReturnType.DBR_STS.getTrueReturnType());
		assertEquals(MonitorType.TIME, ReturnType.DBR_TIME.getTrueReturnType());
		assertEquals(MonitorType.CTRL, ReturnType.DBR_CTRL.getTrueReturnType());
		assertEquals(MonitorType.GR, ReturnType.DBR_GR.getTrueReturnType());
		assertEquals(MonitorType.CTRL, ReturnType.DBR_UNKNOWN.getTrueReturnType());
	}
}
