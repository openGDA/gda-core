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

package uk.ac.diamond.daq.scanning;

import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

/**
 * TODO javadoc
 *
 * TODO is this the right package for this class?
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

	private InsertionDeviceType type;

	private String gapScannableName;

	private String taperScannableName;

	private String harmonicScannableName;

	@Override
	public NexusObjectProvider<NXinsertion_device> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXinsertion_device insertionDevice = NexusNodeFactory.createNXinsertion_device();
		insertionDevice.setTypeScalar(getType().toString());
		writeScannableValue(insertionDevice, NXinsertion_device.NX_GAP, gapScannableName);
		writeScannableValue(insertionDevice, NXinsertion_device.NX_TAPER, taperScannableName);
		writeScannableValue(insertionDevice, NXinsertion_device.NX_HARMONIC, harmonicScannableName);

		return new NexusObjectWrapper<>(getName(), insertionDevice);
	}

	public void setType(String type) {
		this.type = InsertionDeviceType.getValue(type);
	}

	public InsertionDeviceType getType() {
		return type;
	}

	public void setGapScannableName(String gapScannableName) {
		this.gapScannableName = gapScannableName;
	}

	public void setTaperScannableName(String taperScannableName) {
		this.taperScannableName = taperScannableName;
	}

	public void setHarmonicScannableName(String harmonicScannableName) {
		this.harmonicScannableName = harmonicScannableName;
	}

}