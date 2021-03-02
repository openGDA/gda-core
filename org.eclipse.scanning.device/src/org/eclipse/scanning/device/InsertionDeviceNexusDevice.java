/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NexusBaseClass;

/**
 * An {@link INexusDevice} implementation that adds an {@link NXinsertion_device} to the nexus tree.
 */
public class InsertionDeviceNexusDevice extends AbstractNexusMetadataDevice<NXinsertion_device> {

	public enum InsertionDeviceType {
		UNDULATOR, WIGGLER;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		public static InsertionDeviceType getValue(String str) {
			return Enum.valueOf(InsertionDeviceType.class, str.toUpperCase());
		}
	}

	public InsertionDeviceNexusDevice() {
		super(NexusBaseClass.NX_INSERTION_DEVICE);
	}

	public void setType(String typeStr) {
		final InsertionDeviceType type = InsertionDeviceType.getValue(typeStr);// throws IllegalArgumentException if not valid value
		addScalarField(NXinsertion_device.NX_TYPE, type.toString());
	}

	public void setGapScannableName(String gapScannableName) {
		addScannableField(NXinsertion_device.NX_GAP, gapScannableName);
	}

	public void setTaperScannableName(String taperScannableName) {
		addScannableField(NXinsertion_device.NX_TAPER, taperScannableName);
	}

	public void setHarmonicScannableName(String harmonicScannableName) {
		addScannableField(NXinsertion_device.NX_HARMONIC, harmonicScannableName);
	}

}
