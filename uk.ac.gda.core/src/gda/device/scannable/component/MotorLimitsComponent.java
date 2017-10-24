/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable.component;

import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.scannable.PositionConvertorFunctions;

import java.util.Arrays;

public class MotorLimitsComponent implements LimitsComponent {

	private final Motor motor;

	public MotorLimitsComponent(Motor motor) {
		this.motor = motor;
	}

	/**
	 * {@inheritDoc}<P>Returns null indicating no limit set of the motor returns NaN.
	 */
	@Override
	public Double[] getInternalLower() throws DeviceException {
		try {
			double min = motor.getMinPosition();
			return (Double.isNaN(min)) ? null : new Double[] { min };
		} catch (MotorException e) {
			throw new DeviceException("Problem getting motor " + motor.getName() + "'s lower limit", e);
		}
	}

	/**
	 * {@inheritDoc}<P>Returns null indicating no limit set of the motor returns NaN.
	 */
	@Override
	public Double[] getInternalUpper() throws DeviceException {
		try {
			double max = motor.getMaxPosition();
			return (Double.isNaN(max)) ? null : new Double[] { max };
		} catch (MotorException e) {
			throw new DeviceException("Problem getting motor " + motor.getName() + "'s upper limit", e);
		}
	}

	@Override
	public String checkInternalPosition(Object[] internalPosition) throws DeviceException {

		final Double[] lowerLim = getInternalLower();
		final Double[] upperLim = getInternalUpper();

		// If neither limits are set, return null indicating okay.
		if ((lowerLim == null) & (upperLim == null)) {
			return null;
		}

		if (internalPosition.length != 1) {
			throw new IllegalArgumentException("Problem checking motor " + motor.getName()
					+ "'s limits. The position '" + Arrays.toString(internalPosition)
					+ "' should have only one element");
		}

		Double pos = PositionConvertorFunctions.toDoubleArray(internalPosition)[0];

		// Check lower limits if set
		if (lowerLim != null) {
			if (lowerLim[0] != null) {
				if (pos < lowerLim[0]) {
					return String.format("Motor limit violation on motor %s: %f < %f (internal/hardware/dial values).",
							motor.getName(), pos, lowerLim[0]);
				}
			}
		}

		// Check upper limits if set
		if (upperLim != null) {
			if (upperLim[0] != null) {
				if (pos > upperLim[0]) {
					return String.format("Motor limit violation on motor %s: %f > %f (internal/hardware/dial values).",
							motor.getName(), pos, upperLim[0]);
				}
			}
		}

		// Position okay
		return null;
	}

	@Override
	public void setInternalLower(Double[] internalLowerLim) throws DeviceException {
		checkLimitsSettable();
		if (internalLowerLim == null) {
			setMotorLimits(null, (getInternalUpper()==null) ? null : getInternalUpper()[0]);
			return;
		}
		if (internalLowerLim.length != 1) {
			throw new DeviceException("Could not set motor " + motor.getName() + "'s lower limit to '"
					+ Arrays.toString(internalLowerLim) + " as this has more then one element.");
		}
		setMotorLimits(internalLowerLim[0], (getInternalUpper()==null) ? null : getInternalUpper()[0]);
	}

	@Override
	public void setInternalUpper(Double[] internalUpperLim) throws DeviceException {
		checkLimitsSettable();
		if (internalUpperLim == null) {
			setMotorLimits((getInternalLower()==null) ? null : getInternalLower()[0], null);
			return;
		}
		if (internalUpperLim.length != 1) {
			throw new DeviceException("Could not set motor " + motor.getName() + "'s upper limit to '"
					+ Arrays.toString(internalUpperLim) + " as this has more then one element.");
		}
		setMotorLimits((getInternalLower()==null) ? null : getInternalLower()[0], internalUpperLim[0]);
	}

	private void checkLimitsSettable() throws DeviceException {
		try {
			if (!motor.isLimitsSettable()) {
				throw new DeviceException("The motor " + motor.getName() + "'s limits are not settable.");
			}
		} catch (MotorException e) {
			throw new DeviceException("Problem checking if motor " + motor.getName() + "'s limits are settable", e);
		}
	}

	/**
	 * @param min
	 *            null will clear the limit
	 * @param max
	 *            null will clear the limit
	 * @throws DeviceException
	 */
	private void setMotorLimits(Double min, Double max) throws DeviceException {
		if (min == null) {
			min = Double.NaN;
		}
		if (max == null) {
			max = Double.NaN;
		}
		try {
			motor.setSoftLimits(min, max);
		} catch (MotorException e) {
			throw new DeviceException("Problem setting motor " + motor.getName() + "'s limits:", e);
		}
	}

}
