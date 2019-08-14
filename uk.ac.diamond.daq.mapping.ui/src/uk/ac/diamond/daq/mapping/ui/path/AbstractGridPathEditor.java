/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.points.models.AbstractGridModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Abstract superclass for grid-like paths (subclasses of {@link AbstractGridModel}),
 *
 */
public class AbstractGridPathEditor extends AbstractPathEditor {

	/**
	 * Creates controls for options common to all grid paths:<ul>
	 * <li>snake - whether subsequent rows change scan direction in the fast axis;</li>
	 * <li>continuous - whether to scan the fast axis continuously (for malcolm scans only);</li>
	 *
	 * @param parent composite to draw the controls on
	 */
	protected void makeCommonGridOptionsControls(Composite parent) {
		makeSnakeControl(parent);
		makeContinuousControl(parent);
		makeVerticalOrientationControl(parent);
	}

	/**
	 * If the path edited by this editor is snakeable, this method will draw the controls for consistency.
	 * @param parent composite to draw control on
	 */
	private void makeSnakeControl(Composite parent) {
		Label snakeLabel = new Label(parent, SWT.NONE);
		snakeLabel.setText("Snake");
		Button snake = new Button(parent, SWT.CHECK);
		binder.bind(snake, "snake", getModel());
	}

	/**
	 * If the path edited by this editor can have vertical or horizontal orientation, this method will draw the
	 * control to toggle this property.
	 * @param parent composite to draw control on
	 */
	private void makeVerticalOrientationControl(Composite parent) {
		Label verticalOrientationLabel = new Label(parent, SWT.NONE);
		verticalOrientationLabel.setText("Vertical Orientation");
		Button verticalOrientation = new Button(parent, SWT.CHECK);
		binder.bind(verticalOrientation, "verticalOrientation", getModel());
	}

}
