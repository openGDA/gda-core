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

import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;
import static tec.units.indriya.AbstractUnit.ONE;
import static tec.units.indriya.unit.MetricPrefix.KILO;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.MetricPrefix.NANO;
import static tec.units.indriya.unit.Units.JOULE;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.RADIAN;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.QuantityFactory;
import uk.ac.diamond.daq.mapping.api.MappingRegionUnits;

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

	private static final Set<Unit<Dimensionless>> DIMENSIONLESS_OR_UNKNOWN_UNITS = Set.of(ONE);
	private static final Set<Unit<Length>> LENGTH_UNITS = Set.of(MILLI(METRE), MICRO(METRE), NANO(METRE));
	private static final Set<Unit<Angle>> ANGLE_UNITS = Set.of(MILLI(DEGREE_ANGLE), DEGREE_ANGLE);
	private static final Set<Unit<Energy>> ENERGY_UNITS = Set.of(ELECTRON_VOLT, KILO(ELECTRON_VOLT));

	private static final String DEFAULT_LENGTH_UNITS_STRING = "mm";

	private IScannableDeviceService scannableService;
	private MappingRegionUnits lengthUnitsService;
	private Map<String, String> defaultUnits;

	public UnitsProvider() {
		this(null);
	}

	public UnitsProvider(Map<String, String> defaultUnits) {
		this.defaultUnits = defaultUnits != null ? defaultUnits : Collections.emptyMap();
	}

	/**
	 * Returns the base {@link Unit} of the scannable with the given name
	 */
	public <Q extends Quantity<Q>> Unit<Q> getScannableUnit(String scannableName) {
		return QuantityFactory.createUnitFromString(getScannableUnitString(scannableName));
	}

	/**
	 * Returns 'sensible' (subjective) alternative units for the scannable with the given name
	 */
	public <Q extends Quantity<Q>> Set<Unit<Q>> getCompatibleUnits(String scannableName) {
		@SuppressWarnings("unchecked")
		final Unit<Q> scannableUnit = (Unit<Q>) getScannableUnit(scannableName);
		return getCompatibleUnits(scannableUnit);
	}

	/**
	 * Returns all units deemed compatible with the given input unit
	 *
	 * @param scannableUnit
	 *            the unit for which we want to find compatible units
	 * @return set of compatible units
	 */
	@SuppressWarnings("unchecked")
	public static <Q extends Quantity<Q>> Set<Unit<Q>> getCompatibleUnits(Unit<Q> scannableUnit) {
		// We cannot use Set<Unit<Q>> as, e.g. Set<Unit<Length>> cannot be assigned to it (TODO: is there a better way of doing this)
		Set<?> result;
		if (ONE.equals(scannableUnit)) {
			result = DIMENSIONLESS_OR_UNKNOWN_UNITS;
		} else if (scannableUnit.isCompatible(METRE)) {
			result = LENGTH_UNITS;
		} else if (scannableUnit.isCompatible(RADIAN)) {
			result = ANGLE_UNITS;
		} else if (scannableUnit.isCompatible(JOULE)) {
			result = ENERGY_UNITS;
		} else {
			logger.warn("Unsupported dimension {}. Returning standard length units", scannableUnit.getDimension());
			result = LENGTH_UNITS;
		}
		return (Set<Unit<Q>>) result;
	}

	/**
	 * Returns the initial unit for the scannable with the given name for the given property of a model.
	 * This is the same as returned by {@link #getScannableUnit(String)} unless the scannable represents
	 * a length and the unit is overriden by:
	 * <ul>
	 * <li>the property LocalProperties.GDA_INITIAL_LENGTH_UNITS, or
	 * <li>{@link MappingRegionUnits} configured as an OSGi service
	 * </ul>
	 */
	public <Q extends Quantity<Q>> Unit<Q> getInitialUnit(String scannableName, String propertyName) {
		final Unit<Q> scannableUnit = getScannableUnit(scannableName);
		if (scannableUnit.isCompatible(METRE)) {
			final String initialUnitString = getLengthUnitsService().getUnits(propertyName, getScannableUnitString(scannableName));
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
		try {
			final IScannable<?> scannable = scannableService.getScannable(scannableName);
			final String unitStr = scannable.getUnit();
			return StringUtils.isNotEmpty(unitStr) ? unitStr : defaultUnits.getOrDefault(scannableName, "");
		} catch (ScanningException e) {
			logger.error("Could not retrieve scannable with name '{}'. Returning default length units", scannableName, e);
			return DEFAULT_LENGTH_UNITS_STRING;
		}
	}

	private MappingRegionUnits getLengthUnitsService() {
		if (lengthUnitsService == null) {
			lengthUnitsService = PlatformUI.getWorkbench().getService(MappingRegionUnits.class);
			if (lengthUnitsService == null) {
				lengthUnitsService = new MappingRegionUnits(Collections.emptyMap());
			}
		}
		return lengthUnitsService;
	}

}
