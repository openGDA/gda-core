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

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;

public class RectangleRegionEditor extends AbstractRegionEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		Composite composite = super.createEditorPart(parent);


		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Start");
		NumberAndUnitsComposite xStart = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(xStart);

		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Stop");
		NumberAndUnitsComposite xStop = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(xStop);

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Start");
		NumberAndUnitsComposite yStart = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(yStart);

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Stop");
		NumberAndUnitsComposite yStop = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(yStop);

		bind(getFastAxisName(), xStart, "xStart", xStop, "xStop");
		bind(getSlowAxisName(), yStart, "yStart", yStop, "yStop");

		return composite;
	}

	private void bind(String scannableName,
					  NumberAndUnitsComposite firstWidget,
					  String firstProperty,
					  NumberAndUnitsComposite secondWidget,
					  String secondProperty) {

		IObservableValue targetStart = binder.getObservableValue(firstWidget);
		IObservableValue targetStop  = binder.getObservableValue(secondWidget);

		IObservableValue modelStart  = binder.getObservableValue(firstProperty, getModel());
		IObservableValue modelStop	 = binder.getObservableValue(secondProperty, getModel());

		// Binding

		binder.bind(targetStart, modelStart);
		binder.bind(targetStop, modelStop);

		// Validation decorators

		MultiValidator lengthValidator = new MultiValidator() {
			@Override
			protected IStatus validate() {
				double start = (double) targetStart.getValue();
				double stop = (double) targetStop.getValue();
				if (Math.abs(start-stop) > 0.0) return ValidationStatus.ok();
				return ValidationStatus.error("Length must be greater than zero!");
			}
		};

		ControlDecorationSupport.create(lengthValidator, SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(scannableName, firstWidget), SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(scannableName, secondWidget), SWT.LEFT);
	}

}
