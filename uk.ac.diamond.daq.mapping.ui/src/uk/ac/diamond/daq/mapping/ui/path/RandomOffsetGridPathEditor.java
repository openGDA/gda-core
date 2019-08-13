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

public class RandomOffsetGridPathEditor extends AbstractPathEditor {

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);

		new Label(composite, SWT.NONE).setText("Offset (%)");
		Text offset = new Text(composite, SWT.BORDER);
		grabHorizontalSpace.applyTo(offset);
		binder.bind(offset, "offset", getModel());

		new Label(composite, SWT.NONE).setText("Seed");
		Text seed = new Text(composite, SWT.BORDER);
		grabHorizontalSpace.applyTo(seed);
		binder.bind(seed, "seed", getModel());

		new Label(composite, SWT.NONE).setText(getFastAxisName() + " Points");
		Spinner fastPoints = new Spinner(composite, SWT.BORDER);
		fastPoints.setMinimum(1);
		grabHorizontalSpace.applyTo(fastPoints);
		binder.bind(fastPoints, "fastAxisPoints", getModel());

		new Label(composite, SWT.NONE).setText(getSlowAxisName() + " Points");
		Spinner slowPoints = new Spinner(composite, SWT.BORDER);
		slowPoints.setMinimum(1);
		grabHorizontalSpace.applyTo(slowPoints);
		binder.bind(slowPoints, "slowAxisPoints", getModel());

		makeSnakeControl(composite, getModel());
		makeContinuousControl(composite, getModel());

		return composite;
	}

}
