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

import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.api.exception.GDAException;

/**
 * Handler for {@code EpicsEnumPositioner} devices
 *
 * @author Maurizio Nagni
 */
@Component
class EnumPositionerHandler extends DeviceHandler {
	@Override
	DevicePositionDocument devicePositionAsDocument(Scannable device) throws GDAException {
		if (device instanceof EnumPositioner) {
			return createDocument(device);
		}
		return null;
	}

	private DevicePositionDocument createDocument(Scannable positioner) throws GDAException {
		return new DevicePositionDocument.Builder()
			.withDevice(positioner.getName())
			.withValueType(ValueType.LABELLED)
			.withLabelledPosition(getPosition(positioner))
			.build();
	}

	private String getPosition(Scannable positioner) throws GDAException {
		Object position = null;
		try {
			position = positioner.getPosition();
		} catch (DeviceException e) {
			throw new GDAException("Cannot get position for device " + positioner.getName(), e);
		}
		return Optional.ofNullable(position)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.orElseThrow(() -> new GDAException("Cannot get position for device " + positioner.getName()));
	}

}
