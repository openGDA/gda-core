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
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.data.ServiceHolder;

public abstract class AbstractNexusMetadataDevice<N extends NXobject> implements INexusDevice<N> {

	private String name;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	protected void writeScannableValueIfSet(NXobject object, String fieldName, String scannableName) throws NexusException {
		writeScannableValue(object, fieldName, scannableName, false);
	}

	protected void writeScannableValue(NXobject object, String fieldName, String scannableName) throws NexusException {
		writeScannableValue(object, fieldName, scannableName, true);
	}

	protected <T> IScannable<T> getScannable(String scannableName) throws NexusException {
		try {
			return ServiceHolder.getScannableDeviceService().getScannable(scannableName);
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getScannableValue(String scannableName) throws ScanningException, NexusException {
		return (T) getScannable(scannableName).getPosition();
	}

	protected void writeScannableValue(NXobject object, String fieldName, String scannableName, boolean required) throws NexusException {
		if (required && scannableName == null) {
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