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

import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;

public class LineRegionEditor extends AbstractRegionEditor {

	private static final String VALIDATION_ERROR_MESSAGE = "Line cannot have zero length!";

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Start");
		NumberAndUnitsComposite xStart = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(xStart);

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Start");
		NumberAndUnitsComposite yStart = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(yStart);

		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Stop");
		NumberAndUnitsComposite xStop = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(xStop);

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Stop");
		NumberAndUnitsComposite yStop = new NumberAndUnitsComposite(composite, SWT.NONE);
		grabHorizontalSpace.applyTo(yStop);

		/*Validation and binding*/
		IObservableValue targetXStart = binder.getObservableValue(xStart);
		IObservableValue targetXStop  = binder.getObservableValue(xStop);
		IObservableValue targetYStart = binder.getObservableValue(yStart);
		IObservableValue targetYStop  = binder.getObservableValue(yStop);

		IObservableValue modelXStart  = binder.getObservableValue("xStart", getModel());
		IObservableValue modelXStop   = binder.getObservableValue("xStop",  getModel());
		IObservableValue modelYStart  = binder.getObservableValue("yStart", getModel());
		IObservableValue modelYStop   = binder.getObservableValue("yStop",  getModel());

		MultiValidator lengthValidator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				double deltaX = Math.abs((double) targetXStart.getValue() - (double) targetXStop.getValue());
				double deltaY = Math.abs((double) targetYStart.getValue() - (double) targetYStop.getValue());
				if (deltaX > 0 || deltaY > 0) return ValidationStatus.ok();
				return ValidationStatus.error(VALIDATION_ERROR_MESSAGE);
			}
		};

		binder.bind(lengthValidator.observeValidatedValue(targetXStart), modelXStart);
		binder.bind(lengthValidator.observeValidatedValue(targetXStop ), modelXStop );
		binder.bind(lengthValidator.observeValidatedValue(targetYStart), modelYStart);
		binder.bind(lengthValidator.observeValidatedValue(targetYStop ), modelYStop );

		ControlDecorationSupport.create(lengthValidator, SWT.LEFT);

		return composite;
	}

}
