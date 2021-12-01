/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import java.util.stream.Stream;

import javax.measure.Quantity;

import gda.device.DeviceException;

/**
 * Dummy scannable that can be configured with input names, extra names and units.
 *
 * @param <Q> type of quantity of units
 */
public class DummyMultiFieldUnitsScannable<Q extends Quantity<Q>> extends ScannableMotionUnitsBase {

	private Object[] inputPosition;
	private Object[] extraPosition;

	public DummyMultiFieldUnitsScannable(String name) throws DeviceException {
		this(name, null);
	}

	public DummyMultiFieldUnitsScannable(String name, String units) throws DeviceException {
		setName(name);
		setInputNames(new String[] { name });
		setExtraNames(new String[0]);
		inputPosition = new Double[] { 0.0 };
		extraPosition = new Double[0];
		setConfigured(true);

		if (units != null) {
			setHardwareUnitString(units);
			setInitialUserUnits(units);
		}
	}

	@Override
	public void setInputNames(String[] inputNames) {
		super.setInputNames(inputNames);
		inputPosition = new Double[inputNames.length];
		Arrays.fill(inputPosition, 0.0);
	}

	@Override
	public void setExtraNames(String[] extraNames) {
		super.setExtraNames(extraNames);
		extraPosition = new Double[extraNames.length];
		Arrays.fill(extraPosition, 0.0);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return Stream.of(inputPosition, extraPosition).flatMap(Stream::of).toArray(Object[]::new);
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		final Double[] externalPositionArr = PositionConvertorFunctions.toDoubleArray(externalPosition);
		if (externalPositionArr.length != getInputNames().length) {
			throw new IllegalArgumentException("Position array must have size " + getInputNames().length + ", was " + externalPositionArr.length);
		}

		inputPosition = externalPositionArr; // note: we don't bother to do any conversion between hardware and user units
	}

	@Override
	public Object getPosition() throws DeviceException {
		return rawGetPosition();
	}

	public void setCurrentPosition(Object... position) {
		this.inputPosition = position;
	}

	public void setExtraFieldsPosition(Object... extraPosition) {
		this.extraPosition = extraPosition;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

}