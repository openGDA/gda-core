/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

import java.util.Arrays;

import javax.measure.Quantity;
import javax.measure.Unit;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.util.QuantityFactory;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A dummy scannable for testing / simulations which uses units.
 */
@ServiceInterface(ScannableMotionUnits.class)
public class DummyUnitsScannable extends ScannableMotionUnitsBase {

	private double currentPosition = 0;

	public DummyUnitsScannable() {
		// noargs constructor for use in Spring
	}

	/**
	 * Constructor with minimal required settings.
	 *
	 * @param name
	 * @param initialPosition
	 * @param hardwareUnits
	 * @param userUnits
	 * @throws DeviceException
	 */
	public DummyUnitsScannable(String name, double initialPosition, String hardwareUnits, String userUnits) throws DeviceException {
		setName(name);
		setInputNames(new String[] { name });
		currentPosition = initialPosition;
		setHardwareUnitString(hardwareUnits);
		setInitialUserUnits(userUnits);
		setConfigured(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		final Quantity targetInUserUnits = QuantityFactory.createFromObject(externalPosition, getUserUnits());
		final Unit<? extends Quantity<?>> hardwareUnits = QuantityFactory.createUnitFromString(getHardwareUnitString());
		final double targetInHardwareUnits = targetInUserUnits.to(hardwareUnits).getValue().doubleValue();
		final String report = checkPositionValid(targetInHardwareUnits);
		if (report != null) {
			throw new DeviceException(report);
		}
		currentPosition = targetInHardwareUnits;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getPosition() throws DeviceException {
		final Quantity positionInHardwareUnits = QuantityFactory.createFromObject(currentPosition, getHardwareUnitString());
		final Unit<? extends Quantity<?>> userUnits = QuantityFactory.createUnitFromString(getUserUnits());
		return positionInHardwareUnits.to(userUnits).getValue().doubleValue();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public String toString() {
		return "DummyUnitsScannable [currentPosition=" + currentPosition + ", unitsComponent=" + unitsComponent
				+ ", numberTries=" + numberTries + ", tolerance=" + Arrays.toString(tolerance) + ", level=" + level
				+ ", inputNames=" + Arrays.toString(inputNames) + ", extraNames=" + Arrays.toString(extraNames)
				+ ", outputFormat=" + Arrays.toString(outputFormat) + "]";
	}

	/**
	 * Direct access to get currentPosition for testing purposes
	 *
	 * @return position in internal units
	 */
	public double getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * Direct access to set current position for testing purposes
	 *
	 * @param currentPosition
	 *            position in internal units
	 */
	public void setCurrentPosition(double currentPosition) {
		this.currentPosition = currentPosition;
	}
}
