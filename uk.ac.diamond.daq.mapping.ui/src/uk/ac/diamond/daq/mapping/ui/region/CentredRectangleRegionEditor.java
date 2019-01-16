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

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jscience.physics.quantities.Length;

import uk.ac.gda.client.NumberAndUnitsComposite;

public class CentredRectangleRegionEditor extends AbstractRegionEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Centre");
		NumberAndUnitsComposite<Length> xCentre = createNumberAndUnitsLengthComposite(composite, X_CENTRE);
		grabHorizontalSpace.applyTo(xCentre);

		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Range");
		NumberAndUnitsComposite<Length> xRange = createNumberAndUnitsLengthComposite(composite, X_RANGE);
		grabHorizontalSpace.applyTo(xRange);

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Centre");
		NumberAndUnitsComposite<Length> yCentre = createNumberAndUnitsLengthComposite(composite, Y_CENTRE);
		grabHorizontalSpace.applyTo(yCentre);

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Range");
		NumberAndUnitsComposite<Length> yRange = createNumberAndUnitsLengthComposite(composite, Y_RANGE);
		grabHorizontalSpace.applyTo(yRange);

		createValidatedBindings(getFastAxisName(), xCentre, X_CENTRE, xRange, X_RANGE);
		createValidatedBindings(getSlowAxisName(), yCentre, Y_CENTRE, yRange, Y_RANGE);

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

		MultiValidator limitsValidator = new MultiValidator() {

			double lowerLimit = getLowerLimit(scannableName);
			double upperLimit = getUpperLimit(scannableName);

			IObservableValue centreValue = binder.getObservableValue(centreWidget);
			IObservableValue rangeValue  = binder.getObservableValue(rangeWidget);

			@Override
			protected IStatus validate() {
				double centre = (double) centreValue.getValue();
				double range  = (double) rangeValue.getValue();

				if (centre - range/2 < lowerLimit || centre + range/2 > upperLimit) return getLimitsError(lowerLimit, upperLimit);
				return ValidationStatus.ok();
			}
		};
		ControlDecorationSupport.create(limitsValidator, SWT.LEFT);

	}

}
