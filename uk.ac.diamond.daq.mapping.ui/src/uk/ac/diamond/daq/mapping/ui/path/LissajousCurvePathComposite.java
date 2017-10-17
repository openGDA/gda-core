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

import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class LissajousCurvePathComposite extends AbstractPathComposite {

	public LissajousCurvePathComposite(Composite parent, LissajousModel path) {
		super(parent, SWT.NONE);

		(new Label(this, SWT.NONE)).setText("A:");
		Text aTxt = new Text(this, SWT.BORDER);
		gdControls.applyTo(aTxt);
		bindTextBox(aTxt, "a", path);


		(new Label(this, SWT.NONE)).setText("B:");
		Text bTxt = new Text(this, SWT.BORDER);
		gdControls.applyTo(bTxt);
		bindTextBox(bTxt, "b", path);

		(new Label(this, SWT.NONE)).setText("Delta:");
		Text deltaTxt = new Text(this, SWT.BORDER);
		gdControls.applyTo(deltaTxt);
		bindTextBox(deltaTxt, "delta", path);

		(new Label(this, SWT.NONE)).setText("Points:");
		Spinner pointsSpinner = new Spinner(this, SWT.BORDER);
		gdControls.applyTo(pointsSpinner);
		bindSelection(pointsSpinner, "points", path);

		(new Label(this, SWT.NONE)).setText("Theta step:");
		Text thetaTxt = new Text(this, SWT.BORDER);
		gdControls.applyTo(thetaTxt);
		bindTextBox(thetaTxt, "thetaStep", path);

		makeContinuousControl(this, path);
	}

}
