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

import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class OneDEqualSpacingPathComposite extends AbstractPathComposite {

	public OneDEqualSpacingPathComposite(Composite parent, OneDEqualSpacingModel path) {
		super(parent, SWT.NONE);

		(new Label(this, SWT.NONE)).setText("Points:");
		Spinner points = new Spinner(this, SWT.BORDER);
		points.setMinimum(1);
		gdControls.applyTo(points);
		bindSelection(points, "points", path);

		makeContinuousControl(this, path);
	}

}
