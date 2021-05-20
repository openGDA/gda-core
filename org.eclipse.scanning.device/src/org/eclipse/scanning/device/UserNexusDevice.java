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

package org.eclipse.scanning.device;

import java.util.Optional;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

/**
 * A simple {@link INexusDevice} that creates a {@link NXuser} object for the current user.
 */
public class UserNexusDevice extends AbstractNexusMetadataDevice<NXuser> {

	public UserNexusDevice() {
		super(NexusBaseClass.NX_USER);
	}

	public static final String DEFAULT_USER_GROUP_NAME = "user01";

	private String userGroupName = DEFAULT_USER_GROUP_NAME;

	public String getUserGroupName() {
		return userGroupName;
	}

	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
	}

	@Override
	protected NexusObjectWrapper<NXuser> createAndConfigureNexusWrapper(NXuser nexusObject) {
		return new NexusObjectWrapper<>(userGroupName, nexusObject);
	}

	private static final String UNKNOWN_USER_NAME = "Unknown user";
	private static final String UNKNOWN_USER_ID = "unknown";

	@Override
	protected NXuser createNexusObject(NexusScanInfo info) throws NexusException {
		final NXuser userGroup = super.createNexusObject(info);

		final IBatonStateProvider batonProvider = InterfaceProvider.getBatonStateProvider();
		// If the baton is not held by anyone then getBatonHolder returns null
		final Optional<ClientDetails> userDetails = Optional.ofNullable(batonProvider.getBatonHolder());
		final String userId = userDetails.map(ClientDetails::getUserID).orElse(UNKNOWN_USER_ID);
		final String userFullName = userDetails.map(ClientDetails::getFullName).orElse(UNKNOWN_USER_NAME);

		userGroup.setFacility_user_idScalar(userId);
		userGroup.setNameScalar(userFullName);

		return userGroup;
	}

}
