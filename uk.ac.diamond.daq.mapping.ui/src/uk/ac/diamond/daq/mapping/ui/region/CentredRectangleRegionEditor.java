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

import javax.measure.quantity.Quantity;

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

		new Label(composite, SWT.NONE).setText(getXAxisName() + " Centre");
		NumberAndUnitsComposite<Quantity> xCentre = createNumberAndUnitsComposite(composite, getXAxisName(), X_CENTRE);
		grabHorizontalSpace.applyTo(xCentre);

		new Label(composite, SWT.NONE).setText(getXAxisName() + " Range");
		NumberAndUnitsComposite<Quantity> xRange = createNumberAndUnitsComposite(composite, getXAxisName(), X_RANGE);
		grabHorizontalSpace.applyTo(xRange);

		new Label(composite, SWT.NONE).setText(getYAxisName() + " Centre");
		NumberAndUnitsComposite<Quantity> yCentre = createNumberAndUnitsComposite(composite, getYAxisName(), Y_CENTRE);
		grabHorizontalSpace.applyTo(yCentre);

		new Label(composite, SWT.NONE).setText(getYAxisName() + " Range");
		NumberAndUnitsComposite<Quantity> yRange = createNumberAndUnitsComposite(composite, getYAxisName(), Y_RANGE);
		grabHorizontalSpace.applyTo(yRange);

		createValidatedBindings(getXAxisName(), xCentre, X_CENTRE, xRange, X_RANGE);
		createValidatedBindings(getYAxisName(), yCentre, Y_CENTRE, yRange, Y_RANGE);

		return composite;
	}

	@SuppressWarnings("unchecked")
	private void createValidatedBindings(String scannableName,
										 NumberAndUnitsComposite<Quantity> centreWidget,
										 String centreProperty,
										 NumberAndUnitsComposite<Quantity> rangeWidget,
										 String rangeProperty) {

		binder.bind(centreWidget, centreProperty, getModel());
		binder.bind(rangeWidget, rangeProperty, getModel());

		ControlDecorationSupport.create(createGreaterThanZeroValidator(rangeWidget), SWT.LEFT);

		MultiValidator limitsValidator = new MultiValidator() {

			double lowerLimit = getLowerLimit(scannableName);
			double upperLimit = getUpperLimit(scannableName);

			IObservableValue<Double> centreValue = binder.getObservableValue(centreWidget);
			IObservableValue<Double> rangeValue  = binder.getObservableValue(rangeWidget);

			@Override
			protected IStatus validate() {
				double centre = centreValue.getValue();
				double range  = rangeValue.getValue();

				if (centre - range/2 < lowerLimit || centre + range/2 > upperLimit) return getLimitsError(lowerLimit, upperLimit);
				return ValidationStatus.ok();
			}
		};
		ControlDecorationSupport.create(limitsValidator, SWT.LEFT);

	}

}
