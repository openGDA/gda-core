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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.common.exception.GDAException;

/**
 * Handler for {@code EpicsEnumPositioner} devices
 *
 * @author Maurizio Nagni
 */
@Component
class EnumPositionerHandler extends DeviceHandler {

	@Autowired
	private ScannablesPropertiesHelper scannablesPropertiesHelper;

	@Override
	DevicePositionDocument devicePositionAsDocument(Scannable device, ScannablePropertiesValue scannablePropertiesValue)
			throws GDAException {
		if (device instanceof EnumPositioner) {
			var builder = createDocumentBuilder(device);
			builder.withPosition(getPosition(device, scannablePropertiesValue));
			return builder.build();
		}
		return null;
	}

	private DevicePositionDocument.Builder createDocumentBuilder(Scannable device) {
		return new DevicePositionDocument.Builder()
			.withDevice(device.getName());
	}

	private Object getPosition(Scannable device, ScannablePropertiesValue config) throws GDAException {
		switch (config.getPositionType()) {
		case ABSOLUTE:
			String position = getConfiguredPosition(config).toString();
			var scannableProperties = scannablesPropertiesHelper.getScannablePropertiesDocument(config.getScannableKeys());
			return scannableProperties.getEnumsMap().getOrDefault(position, position);
		case CURRENT:
			return getDevicePosition(device);
		case RELATIVE:
			throw new GDAException("Cannot handle relative position for enumurated positioner device");
		default:
			throw new GDAException("Unsupported position type: " + config.getPosition().toString());
		}
	}

	private Object getConfiguredPosition(ScannablePropertiesValue config) throws GDAException {
		var position = config.getPosition();
		if (position == null) {
			throw new GDAException("Position not configured");
		}
		return position;
	}

	private Object getDevicePosition(Scannable device) throws GDAException {
		try {
			return device.getPosition();
		} catch (DeviceException e) {
			throw new GDAException("Cannot get position for device " + device.getName(), e);
		}
	}
}
