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

import java.util.Collections;

import gda.device.DeviceException;
import gda.device.MotorStatus;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableStatus;
import uk.ac.diamond.daq.jms.positioner.Positioner;
import uk.ac.diamond.daq.jms.positioner.PositionerStatus;
import uk.ac.diamond.daq.server.services.positioner.PositionerFactoryException;
import uk.ac.diamond.daq.server.services.positioner.PositionerFactoryPlugin;

public class ScannableMotorPositionerFactoryPlugin implements PositionerFactoryPlugin {
	@Override
	public boolean matches(Scannable scannable) {
		return scannable instanceof ScannableMotor;
	}

	private void checkScannable(Scannable scannable) throws PositionerFactoryException {
		if (!(scannable instanceof ScannableMotor)) {
			String error = String.format("This plugin requires a scannble of type %s", ScannableMotor.class.getName());
			throw new PositionerFactoryException(error);
		}
	}

	@Override
	public Positioner createPositioner(Scannable scannable) throws PositionerFactoryException {
		checkScannable(scannable);

		ScannableMotor scannableMotor = (ScannableMotor) scannable;

		String lowerLimit;
		String upperLimit;
		try {
			lowerLimit = Double.toString(scannableMotor.getLowerInnerLimit());
			upperLimit = Double.toString(scannableMotor.getUpperInnerLimit());
		} catch (DeviceException e) {
			throw new PositionerFactoryException("Cannot get motor limits");
		}

		return new Positioner(scannableMotor.getName(), lowerLimit, upperLimit, scannableMotor.getUserUnits(),
				Collections.emptyList(), getPosition(scannable), getStatus(scannableMotor));
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

		double value;
		try {
			value = Double.parseDouble(position);
		} catch (NumberFormatException e) {
			String error = String.format("Unable to move scannable %s to %s as it cannot be parsed as a double",
					scannable.getName(), position);
			throw new PositionerFactoryException(error);
		}
		double currentPosition = 0.0;
		try {
			ScannableMotor scannableMotor = (ScannableMotor) scannable;
			currentPosition = (Double) scannableMotor.getPosition();
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
		checkScannable(scannable);

		if (event instanceof MotorStatus) {
			MotorStatus status = (MotorStatus) event;
			if (status == MotorStatus.READY || status == MotorStatus.LOWER_LIMIT || status == MotorStatus.UPPER_LIMIT
					|| status == MotorStatus.SOFT_LIMIT_VIOLATION) {
				return PositionerStatus.STOPPED;
			} else if (status == MotorStatus.BUSY) {
				return PositionerStatus.MOVING;
			} else if (status == MotorStatus.FAULT) {
				return PositionerStatus.ERROR;
			}
		} else if (event instanceof ScannableStatus) {
			ScannableStatus status = (ScannableStatus) event;
			if (status == ScannableStatus.BUSY) {
				return PositionerStatus.MOVING;
			} else if (status == ScannableStatus.IDLE) {
				return PositionerStatus.STOPPED;
			} else if (status == ScannableStatus.FAULT) {
				return PositionerStatus.ERROR;
			}
		}
		return PositionerStatus.UNKNOWN;
	}

	@Override
	public PositionerStatus getStatus(Scannable scannable) throws PositionerFactoryException {
		checkScannable(scannable);

		try {
			return convertStatus(scannable, ((ScannableMotor) scannable).getMotor().getStatus());
		} catch (DeviceException e) {
			return PositionerStatus.ERROR;
		}
	}
}
