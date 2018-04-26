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

import java.util.Objects;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractModelEditor;

/**
 * Base class for all region editors in RegionAndPathSection.
 */
public abstract class AbstractRegionEditor extends AbstractModelEditor<IMappingScanRegionShape> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRegionEditor.class);

	/**
	 * @param scannableName
	 * @return the scannable's lower limit, or {@code -Double.MAX_VALUE} if something has gone wrong
	 */
	protected double getLowerLimit(String scannableName) {
		try {
			return (double) Objects.requireNonNull(getScannableDeviceService().getScannable(scannableName).getMinimum());
		} catch (EventException | ScanningException e) {
			logger.error("Could not read lower limit for scannable {}", scannableName, e);
		} catch (NullPointerException npe) {
			logger.warn("Lower limit not configured for scannable {}", scannableName, npe);
		}
		return -Double.MAX_VALUE;
	}

	/**
	 * @param scannableName
	 * @return the scannable's upper limit, or {@code Double.MAX_VALUE} if something has gone wrong
	 */
	protected double getUpperLimit(String scannableName) {
		try {
			return (double) Objects.requireNonNull(getScannableDeviceService().getScannable(scannableName).getMaximum());
		} catch (EventException | ScanningException e) {
			logger.error("Could not read upper limit for scannable {}", scannableName, e);
		} catch (NullPointerException npe) {
			logger.warn("Upper limit not configured for scannable {}", scannableName, npe);
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

			IObservableValue observable = binder.getObservableValue(widget);

			double lowerLimit = getLowerLimit(scannableName);
			double upperLimit = getUpperLimit(scannableName);

			@Override
			protected IStatus validate() {
				double start = (double) observable.getValue();
				if (start < lowerLimit || start > upperLimit) return getLimitsError(lowerLimit, upperLimit);
				return ValidationStatus.ok();
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

			double lowerLimit = getLowerLimit(scannableName);
			double upperLimit = getUpperLimit(scannableName);

			IObservableValue centreObservable = binder.getObservableValue(centre);
			IObservableValue radiusObservable  = binder.getObservableValue(radius);

			@Override
			protected IStatus validate() {
				double c = (double) centreObservable.getValue();
				double r = (double) radiusObservable.getValue();
				if (c-r < lowerLimit || c+r > upperLimit) return getLimitsError(lowerLimit, upperLimit);
				return ValidationStatus.ok();
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

			IObservableValue observable = binder.getObservableValue(widget);

			@Override
			protected IStatus validate() {
				double range = (double) observable.getValue();
				return range > 0 ? ValidationStatus.ok() :
					ValidationStatus.error("Range must be greater than zero!");
			}
		};
	}

}
