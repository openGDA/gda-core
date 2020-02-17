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

import javax.measure.Quantity;

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

		new Label(composite, SWT.NONE).setText(getXAxisName() + " Start");
		NumberAndUnitsComposite<? extends Quantity<?>> xStart = createNumberAndUnitsComposite(composite, getXAxisName(), X_START);
		grabHorizontalSpace.applyTo(xStart);

		new Label(composite, SWT.NONE).setText(getXAxisName() + " Stop");
		NumberAndUnitsComposite<? extends Quantity<?>> xStop = createNumberAndUnitsComposite(composite, getXAxisName(), X_STOP);
		grabHorizontalSpace.applyTo(xStop);

		new Label(composite, SWT.NONE).setText(getYAxisName() + " Start");
		NumberAndUnitsComposite<? extends Quantity<?>> yStart = createNumberAndUnitsComposite(composite, getYAxisName(), Y_START);
		grabHorizontalSpace.applyTo(yStart);

		new Label(composite, SWT.NONE).setText(getYAxisName() + " Stop");
		NumberAndUnitsComposite<? extends Quantity<?>> yStop = createNumberAndUnitsComposite(composite, getYAxisName(), Y_STOP);
		grabHorizontalSpace.applyTo(yStop);

		bind(getXAxisName(), xStart, X_START, xStop, X_STOP);
		bind(getYAxisName(), yStart, Y_START, yStop, Y_STOP);

		return composite;
	}

	@SuppressWarnings("unchecked")
	private void bind(String scannableName,
					  NumberAndUnitsComposite<? extends Quantity<?>> firstWidget,
					  String firstProperty,
					  NumberAndUnitsComposite<? extends Quantity<?>> secondWidget,
					  String secondProperty) {

		IObservableValue<Double> targetStart = binder.getObservableValue(firstWidget);
		IObservableValue<Double> targetStop  = binder.getObservableValue(secondWidget);

		IObservableValue<Double> modelStart  = binder.getObservableValue(firstProperty, getModel());
		IObservableValue<Double> modelStop	 = binder.getObservableValue(secondProperty, getModel());

		// Binding

		binder.bind(targetStart, modelStart);
		binder.bind(targetStop, modelStop);

		// Validation decorators

		MultiValidator lengthValidator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				double start = targetStart.getValue();
				double stop = targetStop.getValue();
				if (Math.abs(start-stop) > 0.0) return ValidationStatus.ok();
				return ValidationStatus.error("Length must be greater than zero!");
			}
		};

		ControlDecorationSupport.create(lengthValidator, SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(scannableName, firstWidget), SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(scannableName, secondWidget), SWT.LEFT);
	}

}
