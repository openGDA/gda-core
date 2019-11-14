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

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SpiralPathEditor extends AbstractPathEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);
		Label scaleLabel = new Label(composite, SWT.NONE);
		scaleLabel.setText("Scale");
		Text scaleText = new Text(composite, SWT.BORDER);
		grabHorizontalSpace.applyTo(scaleText);

		binder.bind(scaleText, "scale", getModel(),
				val -> ((double) val == 0.0) ? ValidationStatus.error("Scale cannot be zero!") : ValidationStatus.ok());

		makeCommonOptionsControls(composite);

		String scaleDescription = "This parameter gives approximately both "
				+ "the distance between arcs and the arclength between consecutive points.";

		scaleLabel.setToolTipText(scaleDescription);
		scaleText.setToolTipText(scaleDescription);

		return composite;
	}

}
