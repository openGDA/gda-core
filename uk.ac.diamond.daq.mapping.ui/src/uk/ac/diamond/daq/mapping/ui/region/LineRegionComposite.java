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

import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class LineRegionComposite extends AbstractRegionAndPathComposite {

	private static final String VALIDATION_ERROR_MESSAGE = "Line cannot have zero length!";

	public LineRegionComposite(Composite parent, LineMappingRegion region) {
		super(parent, SWT.NONE);

		new Label(this, SWT.NONE).setText(getFastAxisName() + " Start");
		NumberAndUnitsComposite xStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStart);

		new Label(this, SWT.NONE).setText(getSlowAxisName() + " Start");
		NumberAndUnitsComposite yStart = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStart);

		new Label(this, SWT.NONE).setText(getFastAxisName() + " Stop");
		NumberAndUnitsComposite xStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStop);

		new Label(this, SWT.NONE).setText(getSlowAxisName() + " Stop");
		NumberAndUnitsComposite yStop = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStop);

		/*Validation and binding*/
		IObservableValue targetXStart = getObservableValue(xStart);
		IObservableValue targetXStop  = getObservableValue(xStop);
		IObservableValue targetYStart = getObservableValue(yStart);
		IObservableValue targetYStop  = getObservableValue(yStop);

		IObservableValue modelXStart  = getObservableValue("xStart", region);
		IObservableValue modelXStop   = getObservableValue("xStop",  region);
		IObservableValue modelYStart  = getObservableValue("yStart", region);
		IObservableValue modelYStop   = getObservableValue("yStop",  region);

		MultiValidator lengthValidator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				double deltaX = Math.abs((double) targetXStart.getValue() - (double) targetXStop.getValue());
				double deltaY = Math.abs((double) targetYStart.getValue() - (double) targetYStop.getValue());
				if (deltaX > 0 || deltaY > 0) return ValidationStatus.ok();
				return ValidationStatus.error(VALIDATION_ERROR_MESSAGE);
			}
		};

		bind(lengthValidator.observeValidatedValue(targetXStart), modelXStart);
		bind(lengthValidator.observeValidatedValue(targetXStop ), modelXStop );
		bind(lengthValidator.observeValidatedValue(targetYStart), modelYStart);
		bind(lengthValidator.observeValidatedValue(targetYStop ), modelYStop );

		ControlDecorationSupport.create(lengthValidator, SWT.LEFT);
	}

}
