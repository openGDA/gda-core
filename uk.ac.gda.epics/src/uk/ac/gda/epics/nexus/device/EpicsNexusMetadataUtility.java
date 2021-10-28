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

package uk.ac.gda.epics.nexus.device;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.scanning.device.INexusMetadataDevice;
import org.eclipse.scanning.device.utils.NexusMetadataUtility;

/**
 * a utility class to support dynamic metadata creation and add from a PV.
 *
 * @author Fajin Yuan
 * @since 9.22
 */
public enum EpicsNexusMetadataUtility {

	INSTANCE;

	/**
	 * add the specified field name and field value to the specified device. if the named device is not existed yet a new metadata device will be created.
	 *
	 * @param deviceName
	 *            - the name of metadata device to add
	 * @param fieldName
	 *            - the field name to be added to the device
	 * @param pvName
	 *            - the name of Processing Variable whose value to be added to the device
	 * @param unit
	 *            - the unit for the field
	 */
	public void addPV(String deviceName, String fieldName, String pvName, String unit) {
		final INexusMetadataDevice<NXobject> nxMetadataDevice = NexusMetadataUtility.INSTANCE.getNexusMetadataDeviceOrAppender(deviceName)
				.orElseGet(() -> NexusMetadataUtility.INSTANCE.createNexusMetadataDevice(deviceName, NexusConstants.COLLECTION));
		nxMetadataDevice.addField(new ProcessingVariableField(fieldName, pvName, unit));
		NexusMetadataUtility.INSTANCE.getUserAddedFields().add(new ImmutablePair<>(deviceName, fieldName));
	}
}
