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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.data.ServiceHolder;

/**
 * TODO javadoc
 *
 * TODO is this the right package for this class?
 */
public class InsertionDeviceNexusDevice implements INexusDevice<NXinsertion_device> {

	// TODO should this be a scannable or just an INexusDevice

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

	private String name;

	private InsertionDeviceType type;

	private String gapScannableName;

	private String taperScannableName;

	private String harmonicScannableName;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public NexusObjectProvider<NXinsertion_device> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXinsertion_device insertionDevice = NexusNodeFactory.createNXinsertion_device();
		insertionDevice.setTypeScalar(getType().toString());
		writeScannableValue(insertionDevice, gapScannableName, NXinsertion_device.NX_GAP);
		writeScannableValue(insertionDevice, taperScannableName, NXinsertion_device.NX_TAPER);
		writeScannableValue(insertionDevice, harmonicScannableName, NXinsertion_device.NX_HARMONIC);

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

	public void writeScannableValue(NXobject object, String scannableName, String fieldName) throws NexusException {
		// TODO move to abstract superclass?

		if (scannableName == null) {
			// TODO all fields required at present, this may change
			throw new NexusException(String.format("No scannable set for field %s of %s", fieldName, object.getClass().getSimpleName()));
		}

		try {
			final IScannable<?> scannable = ServiceHolder.getScannableDeviceService().getScannable(scannableName);
			final Object scannableValue = scannable.getPosition();
			object.setField(fieldName, scannableValue);
			// TODO write units?
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

}
