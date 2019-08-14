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

package uk.ac.diamond.daq.mapping.ui.path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class GridPathEditor extends AbstractGridPathEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		(new Label(composite, SWT.NONE)).setText(getXAxisName() + " Points");
		Spinner fastPoints = new Spinner(composite, SWT.BORDER);
		fastPoints.setMinimum(1);
		fastPoints.setMaximum(Integer.MAX_VALUE);
		grabHorizontalSpace.applyTo(fastPoints);
		binder.bind(fastPoints, "xAxisPoints", getModel());

		(new Label(composite, SWT.NONE)).setText(getYAxisName() + " Points");
		Spinner slowPoints = new Spinner(composite, SWT.BORDER);
		slowPoints.setMinimum(1);
		slowPoints.setMaximum(Integer.MAX_VALUE);
		grabHorizontalSpace.applyTo(slowPoints);
		binder.bind(slowPoints, "yAxisPoints", getModel());

		makeCommonGridOptionsControls(composite);

		return composite;
	}

}
