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

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FluoDetectorROIComposite extends Composite {

	private ScaleBox roiStart;
	private ScaleBox roiEnd;
	private LabelWrapper counts;
	private TextWrapper roiName;
	private FluoDetectorCompositeController controller;

	public FluoDetectorROIComposite(Composite parent, int style, FluoDetectorCompositeController controller) {
		super(parent, style);
		this.controller = controller;
		setLayout(new GridLayout(2, false));

		GridDataFactory horizontalGrabGridData = GridDataFactory.fillDefaults().grab(true, false);

		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name");

		roiName = new TextWrapper(this, SWT.BORDER);
		horizontalGrabGridData.applyTo(roiName);

		Label windowStartLabel = new Label(this, SWT.NONE);
		windowStartLabel.setText("Start");

		roiStart = new ScaleBox(this, SWT.NONE);
		horizontalGrabGridData.applyTo(roiStart);
		roiStart.setIntegerBox(true);
		roiStart.setButtonVisible(true);
		roiStart.setDecimalPlaces(0);
		roiStart.setEditable(true);

		Label windowEndLabel = new Label(this, SWT.NONE);
		windowEndLabel.setText("End");

		roiEnd = new ScaleBox(this, SWT.NONE);
		horizontalGrabGridData.applyTo(roiEnd);
		roiEnd.setIntegerBox(true);
		roiEnd.setMaximum(controller.getDetector().getMCASize());
		roiEnd.setButtonVisible(true);
		roiEnd.setDecimalPlaces(0);
		roiEnd.setEditable(true);

		roiStart.setMaximum(roiEnd);
		roiEnd.setMinimum(roiStart);

		Label countsLabel = new Label(this, SWT.NONE);
		countsLabel.setText("Counts in this region:");

		counts = new LabelWrapper(this, SWT.NONE);
		horizontalGrabGridData.applyTo(counts);

		roiStart.addValueListener(new ValueAdapter("roiStartListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateUIAfterDetectorElementCompositeChange();
			}
		});

		roiEnd.addValueListener(new ValueAdapter("roiEndListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateUIAfterDetectorElementCompositeChange();
			}
		});
	}

	void updateUIAfterDetectorElementCompositeChange() {
		// need to avoid this causing an infinite loop updating roi <-> plot & ui, but still propagate changes to the
		// bean correctly
		controller.updatePlottedRegion(roiStart.getIntegerValue(), roiEnd.getIntegerValue());
	}

	public TextWrapper getRoiName() {
		return roiName;
	}

	public ScaleBox getRoiStart() {
		return roiStart;
	}

	public ScaleBox getRoiEnd() {
		return roiEnd;
	}

	public LabelWrapper getCountsLabel() {
		return counts;
	}
}
