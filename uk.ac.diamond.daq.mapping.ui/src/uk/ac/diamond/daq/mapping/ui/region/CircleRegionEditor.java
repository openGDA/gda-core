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

import javax.measure.quantity.Length;

import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.client.NumberAndUnitsComposite;

public class CircleRegionEditor extends AbstractRegionEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getXAxisName() + " Centre");
		final NumberAndUnitsComposite<Length> xCentre = createNumberAndUnitsComposite(composite, getXAxisName(), X_CENTRE);
		grabHorizontalSpace.applyTo(xCentre);

		new Label(composite, SWT.NONE).setText(getYAxisName() + " Centre");
		final NumberAndUnitsComposite<Length> yCentre = createNumberAndUnitsComposite(composite, getYAxisName(), Y_CENTRE);
		grabHorizontalSpace.applyTo(yCentre);

		new Label(composite, SWT.NONE).setText("Radius");
		final NumberAndUnitsComposite<Length> radius = createNumberAndUnitsComposite(composite, getXAxisName(), RADIUS);
		grabHorizontalSpace.applyTo(radius);

		binder.bind(xCentre, X_CENTRE, getModel());
		binder.bind(yCentre, Y_CENTRE, getModel());
		binder.bind(radius, RADIUS, getModel());

		ControlDecorationSupport.create(createGreaterThanZeroValidator(radius), SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(getXAxisName(), xCentre, radius), SWT.LEFT);
		ControlDecorationSupport.create(createLimitsValidator(getYAxisName(), yCentre, radius), SWT.LEFT);

		return composite;
	}

}