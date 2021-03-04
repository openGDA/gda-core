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

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * A {@link AbstractMetadataNode} field that is written as the position of a scannable.
 */
public class ScannableField extends AbstractMetadataNode {

	public ScannableField() {
		// no-arg constructor for spring initialization
	}

	public ScannableField(String fieldName, String scannableName) {
		super(fieldName);
		setScannableName(scannableName);
	}

	private String scannableName;

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public void writeNode(NXobject nexusObject) throws NexusException {
		writeScannableValue(nexusObject, getName(), getScannableName());
	}

	protected void writeScannableValue(NXobject nxObject, String fieldName, String scannableName) throws NexusException {
		if (scannableName == null) return; // property not set, ignore

		try {
			final Object scannableValue = getScannable(scannableName).getPosition();
			nxObject.setField(fieldName, scannableValue);
			// TODO write units?
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	protected <T> IScannable<T> getScannable(String scannableName) throws NexusException {
		try {
			return Services.getScannableDeviceService().getScannable(scannableName);
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getScannableValue(String scannableName) throws ScanningException, NexusException {
		return (T) getScannable(scannableName).getPosition();
	}

}
