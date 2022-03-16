/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.region;

import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.QuantityFactory;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionPathModelEditor;
import uk.ac.gda.client.NumberAndUnitsComposite;

/**
 * Base class for all region editors in RegionAndPathSection.
 */
public abstract class AbstractRegionEditor extends AbstractRegionPathModelEditor<IMappingScanRegionShape> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRegionEditor.class);

	private Map<String, String> regionUnits;

	/**
	 * @param scannableName
	 * @return the scannable's lower limit, or {@code -Double.MAX_VALUE} if something has gone wrong
	 */
	protected double getLowerLimit(String scannableName) {
		try {
			final Object minimum = getScannableDeviceService().getScannable(scannableName).getMinimum();
			if (minimum == null) {
				logger.warn("Lower limit not configured for scannable {}", scannableName);
			} else if (minimum instanceof Number) {
				return ((Number) minimum).doubleValue();
			}
		} catch (EventException | ScanningException e) {
			logger.error("Could not read lower limit for scannable {}", scannableName, e);
		}
		return -Double.MAX_VALUE;
	}

	/**
	 * @param scannableName
	 * @return the scannable's upper limit, or {@code Double.MAX_VALUE} if something has gone wrong
	 */
	protected double getUpperLimit(String scannableName) {
		try {
			final Object maximum = getScannableDeviceService().getScannable(scannableName).getMaximum();
			if (maximum == null) {
				logger.warn("Upper limit not configured for scannable {}", scannableName);
			} else if (maximum instanceof Number) {
				return ((Number) maximum).doubleValue();
			}
		} catch (EventException | ScanningException e) {
			logger.error("Could not read upper limit for scannable {}", scannableName, e);
		}
		return Double.MAX_VALUE;
	}

	/**
	 * @param scannableName
	 * @param widget - must be supported by DataBinder::getObservableValue
	 * @return a {@link MultiValidator} that checks whether positions given by single widget are within scannable limits.
	 */
	protected MultiValidator createLimitsValidator(String scannableName, Widget widget) {

		return new MultiValidator() {
			final IObservableValue<Double> observable = binder.getObservableValue(widget);

			final double lowerLimit = getLowerLimit(scannableName);
			final double upperLimit = getUpperLimit(scannableName);

			@Override
			protected IStatus validate() {
				final double start = observable.getValue();
				return (start < lowerLimit || start > upperLimit) ?
						getLimitsError(lowerLimit, upperLimit) : ValidationStatus.ok();
			}
		};
	}

	/**
	 * @param scannableName
	 * @param centre - widget must be supported by DataBinder::getObservableValue
	 * @param radius - widget must be supported by DataBinder::getObservableValue
	 * @return a {@link MultiValidator} that checks whether positions given by centre + radius are within scannable limits.
	 */
	protected MultiValidator createLimitsValidator(String scannableName, Widget centre, Widget radius) {
		return new MultiValidator() {
			final double lowerLimit = getLowerLimit(scannableName);
			final double upperLimit = getUpperLimit(scannableName);

			final IObservableValue<Double> centreObservable = binder.getObservableValue(centre);
			final IObservableValue<Double> radiusObservable  = binder.getObservableValue(radius);

			@Override
			protected IStatus validate() {
				final double c = centreObservable.getValue();
				final double r = radiusObservable.getValue();
				return (c-r < lowerLimit || c+r > upperLimit) ?
						getLimitsError(lowerLimit, upperLimit) : ValidationStatus.ok();
			}

		};
	}

	private static final String POSITION_OUT_OF_LIMITS_MSG = "Position cannot be outside scannable limits";
	protected IStatus getLimitsError(double lowerLimit, double upperLimit) {
		return ValidationStatus.error(POSITION_OUT_OF_LIMITS_MSG +
				" ("+String.format("%.3g", lowerLimit)+":"+String.format("%.3g", upperLimit)+")");
	}

	protected MultiValidator createGreaterThanZeroValidator(Widget widget) {
		return new MultiValidator() {
			final IObservableValue<Double> observable = binder.getObservableValue(widget);

			@Override
			protected IStatus validate() {
				double range = observable.getValue();
				return range > 0 ? ValidationStatus.ok() :
					ValidationStatus.error("Range must be greater than zero!");
			}
		};
	}

	/**
	 * Handle units in region elements that may have be/have been changed from their default values.
	 * <ul>
	 * <li>if units are specified in the {@link MappingExperimentBean}, change the combo box now</li>
	 * <li>set a listener to handle any changes the user makes</li>
	 * </ul>
	 *
	 * @param widget
	 *            the {@link NumberAndUnitsComposite} whose combo box we are tracking
	 * @param comboName
	 *            the name of the widget (used as key to the units map)
	 */
	protected void bindUnitsCombo(NumberAndUnitsComposite<Length> widget, String comboName) {
		final ComboViewer unitsCombo = widget.getUnitsCombo();

		// If units have been changed from the default, select the appropriate units in the combo box
		if (regionUnits != null && regionUnits.containsKey(comboName)) {
			final Unit<Length> unit = QuantityFactory.createUnitFromString(regionUnits.get(comboName));
			final ISelection selection = new StructuredSelection(unit);
			unitsCombo.setSelection(selection);
		}

		// Add a listener for future changes
		unitsCombo.addSelectionChangedListener(event -> {
			final String newUnit = ((StructuredSelection) unitsCombo.getSelection()).getFirstElement().toString();
			regionUnits.put(comboName, newUnit);
		});
	}

	public void setRegionUnits(Map<String, String> mappingRegionUnits) {
		this.regionUnits = mappingRegionUnits;
	}
}
