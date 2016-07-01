/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper.BOOLEAN_MODE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Composite for adjusting the window values when using Scaler mode readout.
 * @since 30/6/2016
 */
public class FluoDetectorWindowComposite extends Composite {

	private BooleanWrapper applyToAllCheckbox;
	private NumberBox windowStart;
	private NumberBox windowEnd;

	public FluoDetectorWindowComposite(Composite parent, int style ) {
		super(parent, style);
		this.setLayout( new FillLayout() );

		Group windowGroup = new Group(this, SWT.NONE);
		windowGroup.setText("Window range for scaler");
		GridLayoutFactory.swtDefaults().numColumns(2).margins(15,15).applyTo(windowGroup);


		applyToAllCheckbox = new BooleanWrapper(windowGroup, SWT.NONE);
		applyToAllCheckbox.setValue(true);
		applyToAllCheckbox.setBooleanMode(BOOLEAN_MODE.REVERSE); // because the XspressParameters object field isEditIndividualElements has the opposite sense
		applyToAllCheckbox.setText("Apply to all");
		applyToAllCheckbox.setToolTipText("Apply the same ROIs to all detector elements");
		GridDataFactory.fillDefaults().grab(true, false).span(2,1).applyTo(applyToAllCheckbox);

		Label windowStartLabel = new Label(windowGroup, SWT.NONE);
		windowStartLabel.setText("Start ");

		windowStart = new ScaleBox(windowGroup, SWT.NONE);
		windowStart.setIntegerBox(true);
		windowStart.setButtonVisible(true);
		windowStart.setDecimalPlaces(0);
		windowStart.setEditable(true);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(windowStart);

		Label windowEndLabel = new Label(windowGroup, SWT.NONE);
		windowEndLabel.setText("End ");

		windowEnd = new ScaleBox(windowGroup, SWT.NONE);
		windowEnd.setIntegerBox(true);
		windowEnd.setButtonVisible(true);
		windowEnd.setDecimalPlaces(0);
		windowEnd.setEditable(true);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(windowEnd);

		windowStart.setMaximum( windowEnd );
		windowEnd.setMinimum( windowStart );
	}

	public NumberBox getWindowStart() {
		return windowStart;
	}

	public NumberBox getWindowEnd() {
		return windowEnd;
	}

	public BooleanWrapper getApplyToAllCheckbox() {
		return applyToAllCheckbox;
	}
}
