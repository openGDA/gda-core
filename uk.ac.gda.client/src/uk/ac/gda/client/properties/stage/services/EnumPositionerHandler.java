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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
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
	DevicePositionDocument devicePositionAsDocument(Scannable device) throws GDAException {
		return devicePositionAsDocument(device, null);
	}

	@Override
	DevicePositionDocument devicePositionAsDocument(Scannable device, ScannablePropertiesValue scannablePropertiesValue)
			throws GDAException {
		if (device instanceof EnumPositioner) {
			var builder = createDocumentBuilder(device);
			builder.withLabelledPosition(getPosition(device, scannablePropertiesValue));
			return builder.build();
		}
		return null;
	}

	private DevicePositionDocument.Builder createDocumentBuilder(Scannable device) {
		return new DevicePositionDocument.Builder()
			.withDevice(device.getName())
			.withValueType(ValueType.LABELLED);
	}

	private String getPosition(Scannable device, ScannablePropertiesValue scannablePropertiesValue) throws GDAException {
		if (scannablePropertiesValue == null)
			return getPosition(device);

		var scannableProperties = scannablesPropertiesHelper
				.getScannablePropertiesDocument(scannablePropertiesValue.getScannableKeys());

		return scannableProperties.getEnumsMap().getOrDefault(scannablePropertiesValue.getLabelledPosition(),
				scannablePropertiesValue.getLabelledPosition());
	}

	private String getPosition(Scannable device) throws GDAException {
		Object position = null;
		try {
			position = device.getPosition();
		} catch (DeviceException e) {
			throw new GDAException("Cannot get position for device " + device.getName(), e);
		}
		return Optional.ofNullable(position)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.orElseThrow(() -> new GDAException("Cannot get position for device " + device.getName()));
	}
}
