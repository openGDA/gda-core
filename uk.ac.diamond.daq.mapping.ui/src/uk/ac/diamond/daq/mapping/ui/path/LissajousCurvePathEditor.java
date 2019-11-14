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

package uk.ac.diamond.daq.mapping.ui.path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class LissajousCurvePathEditor extends AbstractPathEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText("A");
		Text aTxt = new Text(composite, SWT.BORDER);
		grabHorizontalSpace.applyTo(aTxt);
		binder.bind(aTxt, "a", getModel());

		new Label(composite, SWT.NONE).setText("B");
		Text bTxt = new Text(composite, SWT.BORDER);
		grabHorizontalSpace.applyTo(bTxt);
		binder.bind(bTxt, "b", getModel());

		new Label(composite, SWT.NONE).setText("Points");
		Spinner pointsSpinner = new Spinner(composite, SWT.BORDER);
		grabHorizontalSpace.applyTo(pointsSpinner);
		binder.bind(pointsSpinner, "points", getModel());

		makeCommonOptionsControls(composite);

		return composite;
	}

}
