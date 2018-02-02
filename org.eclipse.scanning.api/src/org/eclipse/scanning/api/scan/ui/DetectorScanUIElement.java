/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.event.scan.DeviceInformation;

/**
 * An instance of this class represents a detector in the UI.
 * An instance of this class wraps a {@link DeviceInformation}. It is not intended to be serialized.
 *
 * @param <M> the type of the model of the detector
 */
public class DetectorScanUIElement<M> extends ScanUIElement implements IModelProvider<M> {

	private final DeviceInformation<M> deviceInfo;

	public DetectorScanUIElement(DeviceInformation<M> deviceInfo) {
		super(deviceInfo.getName(), deviceInfo.getIcon());
		this.deviceInfo = deviceInfo;
	}

	@Override
	public M getModel() {
		return deviceInfo.getModel();
	}

	@Override
	public void setModel(M detectorModel) {
		this.deviceInfo.setModel(detectorModel);
	}

	public DeviceRole getDeviceRole() {
		return deviceInfo.getDeviceRole();
	}

	public String getLabel() {
		return deviceInfo.getLabel();
	}

	public String getId() {
		return deviceInfo.getId();
	}

	public DeviceInformation<?> getDeviceInfo() {
		return deviceInfo;
	}

}
