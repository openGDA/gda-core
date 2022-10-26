/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.device;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Set;

import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommonBeamlineDeviceConfigurationTest {

	private CommonBeamlineDevicesConfiguration deviceConfig;

	@BeforeEach
	private void setUp() {
		deviceConfig = new CommonBeamlineDevicesConfiguration();
	}

	@AfterEach
	private void tearDown() {
		deviceConfig = null;
	}

	@Test
	void testGetCommonBeamlineDevices() {
		deviceConfig.setBendingMagnetName("magnet");
		deviceConfig.setBeamName("p45");
		deviceConfig.setInsertionDeviceName("id1");
		deviceConfig.setMonochromatorName("mono");
		deviceConfig.setSourceName("diamond");
		deviceConfig.setUserDeviceName("bob");
		deviceConfig.setAdditionalDeviceNames(Set.of("slit1", "slit2", "mirror1", "mirror2", "mirror3"));

		assertThat(deviceConfig.getCommonDeviceNames(), containsInAnyOrder(
				"magnet", "p45", "id1", "mono", "diamond", "bob", "slit1", "slit2",
				"mirror1", "mirror2", "mirror3"));
	}

	@Test
	void testGetCommonBeamlineDevicesSomeNull() {
		deviceConfig.setInsertionDeviceName("id1");
		deviceConfig.setMonochromatorName("mono");
		deviceConfig.setSourceName("diamond");
		deviceConfig.setAdditionalDeviceNames(Set.of("slit1", "slit2", "mirror1", "mirror2", "mirror3"));

		assertThat(deviceConfig.getCommonDeviceNames(), containsInAnyOrder(
				"id1", "mono", "diamond", "slit1", "slit2", "mirror1", "mirror2", "mirror3"));
	}

	@Test
	void testGetCommonBeamlineDevicesSomeDisabled() {
		deviceConfig.setBendingMagnetName("magnet");
		deviceConfig.setBeamName("p45");
		deviceConfig.setInsertionDeviceName("id1");
		deviceConfig.setMonochromatorName("mono");
		deviceConfig.setSourceName("diamond");
		deviceConfig.setUserDeviceName("bob");
		deviceConfig.setAdditionalDeviceNames(Set.of("slit1", "slit2", "mirror1", "mirror2", "mirror3"));

		deviceConfig.disableDevices("magnet", "bob", "slit2", "mirror1", "mirror3");

		assertThat(deviceConfig.getCommonDeviceNames(), containsInAnyOrder(
				"p45", "id1", "mono", "diamond", "slit1", "mirror2"));
	}

}
