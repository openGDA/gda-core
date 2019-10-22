/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static javax.measure.unit.NonSI.DEGREE_ANGLE;
import static javax.measure.unit.NonSI.ELECTRON_VOLT;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.MICRO;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.NANO;

import java.util.Collections;
import java.util.Set;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import gda.util.QuantityFactory;

/**
 * This class can tell us of a scannable:
 * <ul>
 * <li>its unit
 * <li>other compatible units
 * <li>the initial unit to use for a particular model property
 * </ul>
 * In order to do this properly, <strong>the scannable service must be set</strong>
 */
public class UnitsProvider {

	private static final Logger logger = LoggerFactory.getLogger(UnitsProvider.class);

	private static final Set<Unit<? extends Quantity>> LENGTH_UNITS = ImmutableSet.of(MILLI(METER), MICRO(METER), NANO(METER));
	private static final Set<Unit<? extends Quantity>> ANGLE_UNITS = ImmutableSet.of(MILLI(DEGREE_ANGLE), DEGREE_ANGLE);
	private static final Set<Unit<? extends Quantity>> ENERGY_UNITS = ImmutableSet.of(ELECTRON_VOLT, KILO(ELECTRON_VOLT));

	private static final String DEFAULT_LENGTH_UNITS_STRING = "mm";

	private IScannableDeviceService scannableService;
	private InitialLengthUnits lengthUnitsService;

	/**
	 * Returns the base {@link Unit} of the scannable with the given name
	 */
	public Unit<? extends Quantity> getScannableUnit(String scannableName) {
		return QuantityFactory.createUnitFromString(getScannableUnitString(scannableName));
	}

	/**
	 * Returns 'sensible' (subjective) alternative units for the scannable with the given name
	 */
	public Set<Unit<? extends Quantity>> getCompatibleUnits(String scannableName) {
		Unit<? extends Quantity> scannableUnit = getScannableUnit(scannableName);
		if (scannableUnit.isCompatible(Length.UNIT)) {
			return LENGTH_UNITS;
		} else if (scannableUnit.isCompatible(Angle.UNIT)) {
			return ANGLE_UNITS;
		} else if (scannableUnit.isCompatible(Energy.UNIT)) {
			return ENERGY_UNITS;
		}

		logger.warn("Unsupported dimension {}. Returning standard length units", scannableUnit.getDimension());
		return LENGTH_UNITS;
	}

	/**
	 * Returns the initial unit for the scannable with the given name for the given property of a model.
	 * This is the same as returned by {@link #getScannableUnit(String)} unless the scannable represents
	 * a length and the unit is overriden by:
	 * <ul>
	 * <li>the property LocalProperties.GDA_INITIAL_LENGTH_UNITS, or
	 * <li>{@link InitialLengthUnits} configured as an OSGi service
	 * </ul>
	 */
	public Unit<? extends Quantity> getInitialUnit(String scannableName, String propertyName) {
		Unit<? extends Quantity> scannableUnit = getScannableUnit(scannableName);
		if (scannableUnit.isCompatible(Length.UNIT)) {
			String initialUnitString = getLengthUnitsService().getDefaultUnit(propertyName, getScannableUnitString(scannableName));
			return QuantityFactory.createUnitFromString(initialUnitString);
		}
		return scannableUnit;
	}

	/**
	 * Set this to give me knowledge of scannables so I can be useful.
	 * If not set, I will always respond with default units.
	 */
	public void setScannableService(IScannableDeviceService scannableService) {
		this.scannableService = scannableService;
	}

	private String getScannableUnitString(String scannableName) {
		if (scannableService == null) {
			logger.warn("Scannable service is has not been set); using defaults!");
			return DEFAULT_LENGTH_UNITS_STRING;
		}
		IScannable<?> scannable;
		try {
			scannable = scannableService.getScannable(scannableName);
			return scannable.getUnit();
		} catch (ScanningException e) {
			logger.error("Could not retrieve scannable with name '{}'. Returning default length units", scannableName, e);
			return DEFAULT_LENGTH_UNITS_STRING;
		}
	}

	private InitialLengthUnits getLengthUnitsService() {
		if (lengthUnitsService == null) {
			lengthUnitsService = PlatformUI.getWorkbench().getService(InitialLengthUnits.class);
			if (lengthUnitsService == null) {
				lengthUnitsService = new InitialLengthUnits(Collections.emptyMap());
			}
		}
		return lengthUnitsService;
	}

}
