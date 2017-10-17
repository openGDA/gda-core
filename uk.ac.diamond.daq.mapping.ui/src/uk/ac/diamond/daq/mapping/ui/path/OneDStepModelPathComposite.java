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

import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OneDStepModelPathComposite extends AbstractPathComposite {

	public OneDStepModelPathComposite(Composite parent, OneDStepModel path) {
		super(parent, SWT.NONE);

		(new Label(this, SWT.NONE)).setText("Step:");
		Text step = new Text(this, SWT.BORDER);
		gdControls.applyTo(step);
		bindTextBox(step, "step", path);

		makeContinuousControl(this, path);
	}

}
