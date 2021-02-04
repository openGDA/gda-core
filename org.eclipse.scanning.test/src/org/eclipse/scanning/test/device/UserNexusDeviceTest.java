/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.scanning.device.UserNexusDevice;

import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

public class UserNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXuser> {

	private static final String USER_GROUP_NAME = "user01";
	private static final String USER_ID = "abc12345";
	private static final String USER_NAME = "Ted Jones";

	@Override
	protected void setupTestFixtures() throws Exception {
		final ClientDetails userDetails = new ClientDetails(0, USER_ID, USER_NAME, "ws001", 0, true, "visit1");
		final IBatonStateProvider batonStateProvider = mock(IBatonStateProvider.class);
		when(batonStateProvider.getBatonHolder()).thenReturn(userDetails);
		InterfaceProvider.setBatonStateProviderForTesting(batonStateProvider);
	}

	@Override
	protected INexusDevice<NXuser> setupNexusDevice() throws Exception {
		final UserNexusDevice userNexusDevice = new UserNexusDevice();
		userNexusDevice.setName(USER_GROUP_NAME);
		return userNexusDevice;
	}

	@Override
	protected void checkNexusObject(NXuser user) throws Exception {
		assertThat(user.getFacility_user_idScalar(), is(equalTo(USER_ID)));
		assertThat(user.getNameScalar(), is(equalTo(USER_NAME)));
	}

}
