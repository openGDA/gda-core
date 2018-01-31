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

import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class RectangleRegionComposite extends AbstractRegionAndPathComposite {

	public RectangleRegionComposite(Composite parent, RectangularMappingRegion region) {
		super(parent, SWT.NONE);

		// X Start
		Label xStartLabel = new Label(this, SWT.NONE);
		xStartLabel.setText(getFastAxisName() + " Start");
		NumberAndUnitsComposite xStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStart);

		// X Stop
		Label xStopLabel = new Label(this, SWT.NONE);
		xStopLabel.setText(getFastAxisName() + " Stop");
		NumberAndUnitsComposite xStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStop);

		// Y Start
		Label yStartLabel = new Label(this, SWT.NONE);
		yStartLabel.setText(getSlowAxisName() + " Start");
		NumberAndUnitsComposite yStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStart);

		// Y Stop
		Label yStopLabel = new Label(this, SWT.NONE);
		yStopLabel.setText(getSlowAxisName() + " Stop");
		NumberAndUnitsComposite yStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStop);

		bindAndValidate(xStart, "xStart", xStop, "xStop", region);
		bindAndValidate(yStart, "yStart", yStop, "yStop", region);

	}

	private void bindAndValidate(NumberAndUnitsComposite firstWidget,
										 String firstProperty,
										 NumberAndUnitsComposite secondWidget,
										 String secondProperty,
										 Object bean) {


		IObservableValue targetStart = getObservableValue(firstWidget);
		IObservableValue targetStop  = getObservableValue(secondWidget);

		IObservableValue modelStart = getObservableValue(firstProperty, bean);
		IObservableValue modelStop  = getObservableValue(secondProperty, bean);

		MultiValidator validator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				double start = (double) targetStart.getValue();
				double stop  = (double) targetStop.getValue();
				if (Math.abs(start-stop) > 0.0) return ValidationStatus.ok();
				return ValidationStatus.error("Length must be greater than zero!");
			}
		};

		bind(validator.observeValidatedValue(targetStart), modelStart);
		bind(validator.observeValidatedValue(targetStop),  modelStop);

		ControlDecorationSupport.create(validator, SWT.LEFT);
	}
}
