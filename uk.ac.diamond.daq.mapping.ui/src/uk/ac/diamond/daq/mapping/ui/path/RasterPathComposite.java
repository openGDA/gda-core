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

import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.ui.NumberAndUnitsComposite;

public class RasterPathComposite extends AbstractPathComposite {

	public RasterPathComposite(Composite parent, RasterModel path) {
		super(parent, SWT.NONE);

		// X Step
		Label xStepLabel = new Label(this, SWT.NONE);
		xStepLabel.setText(getFastAxisName() + " Step");
		NumberAndUnitsComposite xStep = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(xStep);

		bind(xStep, "fastAxisStep", path, GREATER_THAN_ZERO);

		// Y Step
		Label yStepLabel = new Label(this, SWT.NONE);
		yStepLabel.setText(getSlowAxisName() + " Step");
		NumberAndUnitsComposite yStep = new NumberAndUnitsComposite(this, SWT.NONE);
		gdControls.applyTo(yStep);

		bind(yStep, "slowAxisStep", path, GREATER_THAN_ZERO);

		makeSnakeControl(this, path);
		makeContinuousControl(this, path);
	}

}
