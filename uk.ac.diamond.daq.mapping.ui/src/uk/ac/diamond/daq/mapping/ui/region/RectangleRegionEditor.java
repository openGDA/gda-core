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

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_STOP;

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

public class RectangleRegionEditor extends AbstractRegionEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getXAxisLabel() + " Start");
		final NumberAndUnitsComposite<Length> xStart = createNumberAndUnitsComposite(composite, getXAxisScannableName(), X_START);
		grabHorizontalSpace.applyTo(xStart);

		new Label(composite, SWT.NONE).setText(getXAxisLabel() + " Stop");
		final NumberAndUnitsComposite<Length> xStop = createNumberAndUnitsComposite(composite, getXAxisScannableName(), X_STOP);
		grabHorizontalSpace.applyTo(xStop);

		new Label(composite, SWT.NONE).setText(getYAxisLabel() + " Start");
		final NumberAndUnitsComposite<Length> yStart = createNumberAndUnitsComposite(composite, getYAxisScannableName(), Y_START);
		grabHorizontalSpace.applyTo(yStart);

		new Label(composite, SWT.NONE).setText(getYAxisLabel() + " Stop");
		final NumberAndUnitsComposite<Length> yStop = createNumberAndUnitsComposite(composite, getYAxisScannableName(), Y_STOP);
		grabHorizontalSpace.applyTo(yStop);

		bind(getXAxisScannableName(), xStart, X_START, xStop, X_STOP);
		bind(getYAxisScannableName(), yStart, Y_START, yStop, Y_STOP);

		bindUnitsCombo(xStart, X_START);
		bindUnitsCombo(xStop, X_STOP);
		bindUnitsCombo(yStart, Y_START);
		bindUnitsCombo(yStop, Y_STOP);

		return composite;
	}

	private void bind(String scannableName,
					  NumberAndUnitsComposite<Length> firstWidget,
					  String firstProperty,
					  NumberAndUnitsComposite<Length> secondWidget,
					  String secondProperty) {

		final IObservableValue<Double> targetStart = binder.getObservableValue(firstWidget);
		final IObservableValue<Double> targetStop  = binder.getObservableValue(secondWidget);

		final IObservableValue<Double> modelStart  = binder.getObservableValue(firstProperty, getModel());
		final IObservableValue<Double> modelStop	 = binder.getObservableValue(secondProperty, getModel());

		// Binding

		binder.bind(targetStart, modelStart);
		binder.bind(targetStop, modelStop);

		// Validation decorators

		final MultiValidator lengthValidator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				final double start = targetStart.getValue();
				final double stop = targetStop.getValue();
				return (Math.abs(start-stop) > 0.0) ?
						ValidationStatus.ok() : ValidationStatus.error("Length must be greater than zero!");
			}
		};

		ControlDecorationSupport.create(lengthValidator, SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(scannableName, firstWidget), SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(scannableName, secondWidget), SWT.LEFT);
	}

}
