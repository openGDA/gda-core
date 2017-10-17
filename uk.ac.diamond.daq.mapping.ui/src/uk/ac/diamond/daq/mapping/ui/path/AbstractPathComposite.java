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

import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class AbstractPathComposite extends AbstractRegionAndPathComposite {

	private Button continuous;
	private Label continuousLabel;

	public AbstractPathComposite(Composite parent, int style) {
		super(parent, style);
	}

	public void setContinuousEnabled(boolean enabled) {
		continuous.setEnabled(enabled);
		continuous.setSelection(enabled);
		continuousLabel.setEnabled(enabled);
	}

	protected void makeSnakeControl(Composite parent, IScanPathModel path) {
		Label snakeLabel = new Label(parent, SWT.NONE);
		snakeLabel.setText("Snake:");
		Button snake = new Button(parent, SWT.CHECK);

		bindSelection(snake, "snake", path);
	}

	protected void makeContinuousControl(Composite parent, IScanPathModel path) {
		continuousLabel = new Label(parent, SWT.NONE);
		continuousLabel.setText("Continuous:");
		continuous = new Button(parent, SWT.CHECK);

		bindSelection(continuous, "continuous", path);
	}

}
