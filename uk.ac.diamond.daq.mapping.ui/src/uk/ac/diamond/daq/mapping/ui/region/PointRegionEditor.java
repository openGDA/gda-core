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

import javax.measure.quantity.Quantity;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.client.NumberAndUnitsComposite;

public class PointRegionEditor extends AbstractRegionEditor {

	@Override
	public Composite createEditorPart(Composite parent) {
		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText(getXAxisName() + " position");
		NumberAndUnitsComposite<Quantity> xPosition = createNumberAndUnitsComposite(composite, getXAxisName(), X_POSITION);
		grabHorizontalSpace.applyTo(xPosition);
		binder.bind(xPosition, X_POSITION, getModel());

		new Label(composite, SWT.NONE).setText(getYAxisName() + " position");
		NumberAndUnitsComposite<Quantity> yPosition = createNumberAndUnitsComposite(composite, getYAxisName(), Y_POSITION);
		grabHorizontalSpace.applyTo(yPosition);
		binder.bind(yPosition, Y_POSITION, getModel());

		return composite;
	}

}
