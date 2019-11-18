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
	 * Creates controls for options common to grid paths:<ul>
	 * <li>alternating - whether subsequent scans change direction in the innermost axis;</li>
	 * <li>continuous - whether to scan the innermost axis continuously (for malcolm scans only);</li>
	 * <li>vertical orientation - whether to treat the 2nd axis as the fast axis</li>
	 *
	 * @param parent composite to draw the controls on
	 */

	protected void makeCommonGridOptionsControls(Composite parent) {
		makeCommonOptionsControls(parent);
		makeVerticalOrientationControl(parent);
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
