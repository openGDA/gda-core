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

package uk.ac.gda.client.properties.stage.services;

import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.common.exception.GDAException;

/**
 * Handler for {@link IScannableMotor} devices
 *
 * @author Maurizio Nagni
 */
@Component
class IScannableMotorHandler extends DeviceHandler {

	@Override
	DevicePositionDocument devicePositionAsDocument(Scannable device, ScannablePropertiesValue scannablePropertyValue)
			throws GDAException {
		if (device instanceof IScannableMotor) {
			var builder = createDocumentBuilder(device);
			builder.withPosition(getPosition(device, scannablePropertyValue));
			return builder.build();
		}
		return null;
	}

	private DevicePositionDocument.Builder createDocumentBuilder(Scannable device) {
		return new DevicePositionDocument.Builder()
			.withDevice(device.getName());
	}

	private double getPosition(Scannable device, ScannablePropertiesValue config) throws GDAException {
		switch (config.getPositionType()) {
		case ABSOLUTE:
			return getConfiguredPosition(config);
		case CURRENT:
			return getDevicePosition(device);
		case RELATIVE:
			return getConfiguredPosition(config) + getDevicePosition(device);
		default:
			throw new GDAException("Unsupported position type: " + config.getPosition().toString());
		}
	}

	private double getConfiguredPosition(ScannablePropertiesValue config) throws GDAException {
		var position = config.getPosition();
		if (position == null) {
			throw new GDAException("Position not configured");
		}
		return Double.parseDouble(position.toString());
	}

	private double getDevicePosition(Scannable device) throws GDAException {
		try {
			return (double) device.getPosition();
		} catch (DeviceException e) {
			throw new GDAException("Cannot get position for device " + device.getName(), e);
		}
	}
}
