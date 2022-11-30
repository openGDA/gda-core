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

import org.junit.Test;

import gda.epics.connection.EpicsController.MonitorType;

public class ReturnTypeTest {

	@Test
	public void testToString() {
		assertEquals("NATIVE", ReturnType.DBR_NATIVE.toString());
		assertEquals("STS", ReturnType.DBR_STS.toString());
		assertEquals("TIME", ReturnType.DBR_TIME.toString());
		assertEquals("CTRL", ReturnType.DBR_CTRL.toString());
		assertEquals("GR", ReturnType.DBR_GR.toString());
	}

	@Test
	public void testGetTrueReturnType() {
		assertEquals(MonitorType.NATIVE, ReturnType.DBR_NATIVE.getTrueReturnType());
		assertEquals(MonitorType.STS, ReturnType.DBR_STS.getTrueReturnType());
		assertEquals(MonitorType.TIME, ReturnType.DBR_TIME.getTrueReturnType());
		assertEquals(MonitorType.CTRL, ReturnType.DBR_CTRL.getTrueReturnType());
		assertEquals(MonitorType.GR, ReturnType.DBR_GR.getTrueReturnType());
	}
}
