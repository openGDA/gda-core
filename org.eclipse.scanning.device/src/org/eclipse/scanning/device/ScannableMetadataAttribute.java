/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class ScannableMetadataAttribute extends AbstractMetadataAttribute {

	private String scannableName;

	public ScannableMetadataAttribute() {
		// no-arg constructor for spring instantiation
	}

	public ScannableMetadataAttribute(String fieldName, String scannableName) {
		super(fieldName);
		this.scannableName = scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public Object getValue() throws NexusException {
		return getScannableValue();
	}

	private IScannable<?> getScannable() throws NexusException {
		try {
			return ServiceProvider.getService(IScannableDeviceService.class).getScannable(scannableName);
		} catch (ScanningException e) {
			throw new NexusException("Could not find scannable with name: " + scannableName);
		}
	}

	private Object getScannableValue() throws NexusException {
		try {
			return getScannable().getPosition();
		} catch (ScanningException e) {
			throw new NexusException("Could not get position for scannable with name: " + scannableName);
		}
	}

}
