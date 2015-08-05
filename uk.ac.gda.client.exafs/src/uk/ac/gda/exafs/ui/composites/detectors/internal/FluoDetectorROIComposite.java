/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FluoDetectorROIComposite extends Composite {

	private NumberBox roiStart;
	private NumberBox roiEnd;
	private TextWrapper roiName;

	public FluoDetectorROIComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		GridDataFactory horizontalGrabGridData = GridDataFactory.fillDefaults().grab(true, false);

		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name ");

		roiName = new TextWrapper(this, SWT.BORDER);
		horizontalGrabGridData.applyTo(roiName);
		// Override layout specified in TextWrapper to make box size match the ScaleBoxes
		GridLayoutFactory.fillDefaults().applyTo(roiName);

		Label windowStartLabel = new Label(this, SWT.NONE);
		windowStartLabel.setText("Start ");

		roiStart = new ScaleBox(this, SWT.NONE);
		horizontalGrabGridData.applyTo(roiStart);
		roiStart.setIntegerBox(true);
		roiStart.setButtonVisible(true);
		roiStart.setDecimalPlaces(0);
		roiStart.setEditable(true);

		Label windowEndLabel = new Label(this, SWT.NONE);
		windowEndLabel.setText("End ");

		roiEnd = new ScaleBox(this, SWT.NONE);
		horizontalGrabGridData.applyTo(roiEnd);
		roiEnd.setIntegerBox(true);
		roiEnd.setButtonVisible(true);
		roiEnd.setDecimalPlaces(0);
		roiEnd.setEditable(true);

		roiStart.setMaximum(roiEnd);
		roiEnd.setMinimum(roiStart);
	}

	public IFieldWidget getRoiName() {
		return roiName;
	}

	public NumberBox getRoiStart() {
		return roiStart;
	}

	public NumberBox getRoiEnd() {
		return roiEnd;
	}
}
