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

import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FermatSpiralPathComposite extends AbstractPathComposite {

	public FermatSpiralPathComposite(Composite parent, SpiralModel path) {
		super(parent, SWT.NONE);

		(new Label(this, SWT.NONE)).setText("Scale:");
		Text scaleText = new Text(this, SWT.BORDER);
		gdControls.applyTo(scaleText);

		bindTextBox(scaleText, "scale", path);

		makeContinuousControl(this, path);
	}

}
