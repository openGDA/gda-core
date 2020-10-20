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

package uk.ac.diamond.daq.mapping.ui.services.position;

import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.api.exception.GDAException;

/**
 * Handler for {@link IScannableMotor} devices
 *
 * @author Maurizio Nagni
 */
@Component
class IScannableMotorHandler extends DeviceHandler {
	@Override
	DevicePositionDocument devicePositionAsDocument(Object device) throws GDAException {
		if (IScannableMotor.class.isInstance(device)) {
			return createDocument(IScannableMotor.class.cast(device));
		}
		return null;
	}

	private DevicePositionDocument createDocument(IScannableMotor device) throws GDAException {
		return new DevicePositionDocument.Builder()
			.withDevice(device.getName())
			.withValueType(ValueType.NUMERIC)
			.withPosition(getPosition(device))
			.build();
	}

	private double getPosition(IScannableMotor device) throws GDAException {
		try {
			return (double) device.getPosition();
		} catch (DeviceException e) {
			throw new GDAException("Cannot get position for device " + device.getName(), e);
		}
	}
}
