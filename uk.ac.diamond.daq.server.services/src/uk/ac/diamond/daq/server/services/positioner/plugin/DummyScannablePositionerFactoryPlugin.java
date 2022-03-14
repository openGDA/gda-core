/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.positioner.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.component.ScannableLimitsComponent;
import uk.ac.diamond.daq.jms.positioner.Positioner;
import uk.ac.diamond.daq.jms.positioner.PositionerStatus;
import uk.ac.diamond.daq.server.services.positioner.PositionerFactoryException;
import uk.ac.diamond.daq.server.services.positioner.PositionerFactoryPlugin;

public class DummyScannablePositionerFactoryPlugin implements PositionerFactoryPlugin {
	private static final Logger log = LoggerFactory.getLogger(DummyScannablePositionerFactoryPlugin.class);

	@Override
	public boolean matches(Scannable scannable) {
		return scannable instanceof DummyScannable;
	}

	private void checkScannable(Scannable scannable) throws PositionerFactoryException {
		if (!(scannable instanceof DummyScannable)) {
			String error = String.format("This plugin requires a scannble of type %s", DummyScannable.class.getName());
			throw new PositionerFactoryException(error);
		}
	}

	@Override
	public Positioner createPositioner(Scannable scannable) throws PositionerFactoryException {
		checkScannable(scannable);

		DummyScannable dummyScannable = (DummyScannable) scannable;

		String lowerLimit = "N/A";
		String upperLimit = "N/A";
		ScannableLimitsComponent limits = dummyScannable.getLimitsComponent();
		if (limits != null) {
			if (limits.getInternalLower().length > 0) {
				lowerLimit = Double.toString(limits.getInternalLower()[0]);
			}
			if (limits.getInternalUpper().length > 0) {
				upperLimit = Double.toString(limits.getInternalUpper()[0]);
			}
		}

		Positioner positioner = new Positioner(dummyScannable.getName(), lowerLimit, upperLimit, "N/A");
		positioner.setPosition(getPosition(dummyScannable));
		positioner.setStatus(getStatus(dummyScannable));
		return positioner;
	}

	@Override
	public String getPosition(Scannable scannable) throws PositionerFactoryException {
		checkScannable(scannable);

		Object position;
		try {
			position = scannable.getPosition();
		} catch (DeviceException e) {
			String error = String.format("Unable to get position of %s due to internal error %s", scannable.getName(),
					e.getMessage());
			throw new PositionerFactoryException(error, e);
		}
		try {
			return Double.toString((Double) position);
		} catch (ClassCastException | NullPointerException e) {
			String error = String.format("Unable to get position of %s, %s cannot be converted", scannable.getName(),
					position);
			throw new PositionerFactoryException(error);
		}
	}

	@Override
	public String moveTo(Scannable scannable, String position) throws PositionerFactoryException {
		checkScannable(scannable);

		if (position == null) {
			String error = String.format("Unable to set position of %s to null", scannable.getName());
			throw new PositionerFactoryException(error);
		}

		Object currentPosition;
		try {
			currentPosition = scannable.getPosition();
		} catch (DeviceException e) {
			String error = String.format("Unable to get position of %s due to internal error %s", scannable.getName(),
					e.getMessage());
			throw new PositionerFactoryException(error, e);
		}

		Object value = position;
		if (currentPosition == null) {
			try {
				value = Double.parseDouble(position);
			} catch (NumberFormatException e) {
				String message = String.format(
						"Current position of scannable %s is null to attempted to parse value %s", scannable.getName(),
						position);
				log.debug(message);
			}
		} else if (currentPosition instanceof Double) {
			try {
				value = Double.parseDouble(position);
			} catch (NumberFormatException e) {
				String error = String.format("Unable to move scannable %s to %s as it cannot be parsed as a double",
						scannable.getName(), position);
				throw new PositionerFactoryException(error);
			}
		}

		try {
			DummyScannable scannableMotor = (DummyScannable) scannable;
			scannableMotor.asynchronousMoveTo(value);
		} catch (DeviceException e) {
			String error = String.format("Unable to move scannable %s to %s dur to internal error %s",
					scannable.getName(), position, e.getMessage());
			throw new PositionerFactoryException(error);
		}
		return String.format("Motor %s moving from %f to %f", scannable.getName(), currentPosition, value);
	}

	@Override
	public String stop(Scannable scannable) throws PositionerFactoryException {
		checkScannable(scannable);

		try {
			scannable.stop();
			return String.format("Stopped scannable %s", scannable.getName());
		} catch (DeviceException e) {
			String error = String.format("Failed to stop %s due to internal error: %s", scannable.getName(),
					e.getMessage());
			throw new PositionerFactoryException(error);
		}
	}

	@Override
	public PositionerStatus convertStatus(Scannable scannable, Object event) throws PositionerFactoryException {
		return PositionerStatus.STOPPED;
	}

	@Override
	public PositionerStatus getStatus(Scannable scannable) throws PositionerFactoryException {
		return PositionerStatus.STOPPED;
	}

}
