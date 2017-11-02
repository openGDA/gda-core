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

/**
 *
 */
package gda.device.scannable;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.component.UnitsComponent;

/**
 * A base implementation for a {@link ScannableMotionUnits} {@link Device}.
 * <p>
 * The position passed to asynchronousMoveTo and returned by getPosition will be in the external (user) representation.
 * getPosition will return the amount of the the Scannable's position Quantity in external (user) units as a Double (or
 * array of Doubles). See {@link UnitsComponent#internalTowardExternal(Object)}. getPositionQuantity will return the
 * position as a quantity in external (user) units. asynchronousMoveTo asynchronousMoveTo will accept a Quantity that is
 * compatible with the internal (hardware) unit. A String will be parsed into quantity. Otherwise it will take will take
 * anything that {@link ScannableMotionBase}'s will take (except an arbitrary String). See
 * {@link UnitsComponent#externalTowardInternal(Object)}
 * <p>
 * As units are important, then the objects probably are representing hardware, so the isPositionValid method should be
 * implemented.
 * <p>
 * By default the hardware and user units are the dimensionless ONE. The hardware and user unit cannot be configured to
 * be incompatible.
 */
public abstract class ScannableMotionUnitsBase extends ScannableMotionBase implements ScannableMotionUnits {

	private static final Logger logger = LoggerFactory.getLogger(ScannableMotionUnitsBase.class);

	// handles user-unit to motor-unit conversion
	protected UnitsComponent unitsComponent = new UnitsComponent();

	private String initialUserUnits = null;

	@Override
	public Quantity[] getPositionAsQuantityArray() throws DeviceException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Object internalToExternal(Object internalPosition) {
		// 1. Apply offset (which is kept in internal units)
		Object internalPositionwithOffsetAppled = super.internalToExternal(internalPosition);

		// 2. Return amounts that result from changing to external (user) units
		return unitsComponent.internalTowardExternal(internalPositionwithOffsetAppled);
	}

	@Override
	public Object externalToInternal(Object externalPosition) {
		// 1. Calculate amounts that result from changing to internal (user) units
		Object amountsInInternalUnits = unitsComponent.externalTowardInternal(externalPosition);

		// 2. Remove the offset (now that we are internal units) and return
		return super.externalToInternal(amountsInInternalUnits);
	}

	@Override
	public boolean isAt(Object externalPosition) throws DeviceException {
		Object amountsInExternalUnits = unitsComponent.toExternalAmount(externalPosition);
		return super.isAt(amountsInExternalUnits);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals(USERUNITS)) {
			return this.getUserUnits();
		} else if (attributeName.equals(HARDWAREUNITS)) {
			return this.getHardwareUnitString();
		}
		return super.getAttribute(attributeName);
	}



	@Override
	public String getUserUnits() {
		return this.unitsComponent.getUserUnitString();
	}

	@Override
	public void setUserUnits(String userUnitsString) throws DeviceException {
		this.unitsComponent.setUserUnits(userUnitsString);
	}

	@Override
	public String getHardwareUnitString() {
		return this.unitsComponent.getHardwareUnitString();
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		this.unitsComponent.setHardwareUnitString(hardwareUnitString);
	}

	@Override
	public String[] getAcceptableUnits() {
		return this.unitsComponent.getAcceptableUnits();
	}

	@Override
	public void addAcceptableUnit(String newUnit) throws DeviceException {
		this.unitsComponent.addAcceptableUnit(newUnit);
	}

	/**
	 * @return Returns the initialUserUnits.
	 */
	public String getInitialUserUnits() {
		return initialUserUnits;
	}

	/**
	 * Sets the initial user units.
	 *
	 * @throws DeviceException
	 */
	public void setInitialUserUnits(String initialUserUnits) throws DeviceException {
		this.initialUserUnits = initialUserUnits;
		setUserUnits(initialUserUnits);
	}

	@Override
	public String toFormattedString() {
		// We need to extend so that this is passed as SMB rather then SM! TODO: Spaghetti
		String report;
		try {
			report = ScannableUtils.getFormattedCurrentPosition(this);

		} catch (Exception e) {
			logger.warn("Exception getting formatted string for {}", getName(), e);
			report = valueUnavailableString();
		}

		return report + generateScannableLimitsReport();
	}

	@Override
	public Double[] getOffset() {
		Double[] offsetAmountsInInternalUnits = super.getOffset();
		Object offsetAmountsInExternalUnits = unitsComponent.internalTowardExternal(offsetAmountsInInternalUnits);
		return PositionConvertorFunctions.toDoubleArray(offsetAmountsInExternalUnits);
	}

	@Override
	public void setOffset(Double... offsetAmountsInExternalUnits) {
		Object offsetAmountsInInternalUnits = unitsComponent.externalTowardInternal(offsetAmountsInExternalUnits);
		super.setOffset(PositionConvertorFunctions.toDoubleArray(offsetAmountsInInternalUnits));
	}

	@Override
	public void setOffset(Object offsetPositionInExternalUnits) {
		Object offsetAmountsInInternalUnits = unitsComponent.externalTowardInternal(offsetPositionInExternalUnits);
		super.setOffset(PositionConvertorFunctions.toDoubleArray(offsetAmountsInInternalUnits));
	}

}
