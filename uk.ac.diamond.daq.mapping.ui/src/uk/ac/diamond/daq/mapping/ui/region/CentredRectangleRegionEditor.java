/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_RANGE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_RANGE;

import javax.measure.quantity.Length;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.client.NumberAndUnitsComposite;

public class CentredRectangleRegionEditor extends AbstractRegionEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getXAxisLabel() + " Centre");
		final NumberAndUnitsComposite<Length> xCentre = createNumberAndUnitsComposite(composite, getXAxisScannableName(), X_CENTRE);
		grabHorizontalSpace.applyTo(xCentre);

		new Label(composite, SWT.NONE).setText(getXAxisLabel() + " Range");
		final NumberAndUnitsComposite<Length> xRange = createNumberAndUnitsComposite(composite, getXAxisScannableName(), X_RANGE);
		grabHorizontalSpace.applyTo(xRange);

		new Label(composite, SWT.NONE).setText(getYAxisScannableName() + " Centre");
		final NumberAndUnitsComposite<Length> yCentre = createNumberAndUnitsComposite(composite, getYAxisScannableName(), Y_CENTRE);
		grabHorizontalSpace.applyTo(yCentre);

		new Label(composite, SWT.NONE).setText(getYAxisScannableName() + " Range");
		final NumberAndUnitsComposite<Length> yRange = createNumberAndUnitsComposite(composite, getYAxisScannableName(), Y_RANGE);
		grabHorizontalSpace.applyTo(yRange);

		createValidatedBindings(getXAxisScannableName(), xCentre, X_CENTRE, xRange, X_RANGE);
		createValidatedBindings(getYAxisScannableName(), yCentre, Y_CENTRE, yRange, Y_RANGE);

		bindUnitsCombo(xCentre, X_CENTRE);
		bindUnitsCombo(xRange, X_RANGE);
		bindUnitsCombo(yCentre, Y_CENTRE);
		bindUnitsCombo(yRange, Y_RANGE);

		return composite;
	}

	private void createValidatedBindings(String scannableName,
										 NumberAndUnitsComposite<Length> centreWidget,
										 String centreProperty,
										 NumberAndUnitsComposite<Length> rangeWidget,
										 String rangeProperty) {

		binder.bind(centreWidget, centreProperty, getModel());
		binder.bind(rangeWidget, rangeProperty, getModel());

		ControlDecorationSupport.create(createGreaterThanZeroValidator(rangeWidget), SWT.LEFT);

		final MultiValidator limitsValidator = new MultiValidator() {
			final double lowerLimit = getLowerLimit(scannableName);
			final double upperLimit = getUpperLimit(scannableName);

			final IObservableValue<Double> centreValue = binder.getObservableValue(centreWidget);
			final IObservableValue<Double> rangeValue  = binder.getObservableValue(rangeWidget);

			@Override
			protected IStatus validate() {
				final double centre = centreValue.getValue();
				final double range  = rangeValue.getValue();

				return (centre - range/2 < lowerLimit || centre + range/2 > upperLimit) ?
						getLimitsError(lowerLimit, upperLimit) : ValidationStatus.ok();
			}
		};
		ControlDecorationSupport.create(limitsValidator, SWT.LEFT);
	}
}
