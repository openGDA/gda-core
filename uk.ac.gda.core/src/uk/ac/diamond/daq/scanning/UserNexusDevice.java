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

package uk.ac.diamond.daq.scanning;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

/**
 * A simple {@link INexusDevice} that creates a {@link NXuser} object for the current user.
 */
public class UserNexusDevice extends AbstractNexusMetadataDevice<NXuser> {

	public static final String DEFAULT_USER_GROUP_NAME = "user01";

	private String userGroupName = DEFAULT_USER_GROUP_NAME;

	public String getUserGroupName() {
		return userGroupName;
	}

	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
	}

	@Override
	public NexusObjectProvider<NXuser> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXuser userGroup = createUserGroup();
		return new NexusObjectWrapper<>(userGroupName, userGroup);
	}

	private NXuser createUserGroup() {
		final ClientDetails userDetails = InterfaceProvider.getBatonStateProvider().getBatonHolder();

		final NXuser userGroup = NexusNodeFactory.createNXuser();
		writeFieldValue(userGroup, NXuser.NX_FACILITY_USER_ID, userDetails.getUserID());
		writeFieldValue(userGroup, NXuser.NX_NAME, userDetails.getFullName());

		return userGroup;
	}


}