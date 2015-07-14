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

import org.dawnsci.common.richbeans.components.FieldBeanComposite;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.dawnsci.common.richbeans.components.wrappers.TextWrapper;
import org.dawnsci.common.richbeans.event.ValueAdapter;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import gda.observable.IObserver;

public class FluoDetectorROIComposite extends FieldBeanComposite implements IObserver {

	private ScaleBox roiStart;
	private ScaleBox roiEnd;
	private LabelWrapper counts;
	private TextWrapper roiName;
	private FluorescenceDetectorCompositeController controller;
	protected boolean updatingFromUI;

	public FluoDetectorROIComposite(Composite parent, int style, FluorescenceDetectorCompositeController controller) {
		super(parent, style);
		this.controller = controller;
		setLayout(new GridLayout(2, false));

		Label lblName = new Label(this, SWT.NONE);
		lblName.setText("Name");

		roiName = new TextWrapper(this, SWT.BORDER);
		roiName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label windowStartLabel = new Label(this, SWT.NONE);
		windowStartLabel.setText("Start");

		roiStart = new ScaleBox(this, SWT.NONE);
		roiStart.setIntegerBox(true);
		roiStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		roiStart.setButtonVisible(true);
		roiStart.setDecimalPlaces(0);
		roiStart.setEditable(true);

		final Label windowEndLabel = new Label(this, SWT.NONE);
		windowEndLabel.setText("End");

		roiEnd = new ScaleBox(this, SWT.NONE);
		roiEnd.setIntegerBox(true);
		roiEnd.setMaximum(controller.getDetector().getMCASize());
		roiEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		roiEnd.setButtonVisible(true);
		roiEnd.setDecimalPlaces(0);
		roiEnd.setEditable(true);

		roiStart.setMaximum(roiEnd);
		roiEnd.setMinimum(roiStart);

		Label lblCounts = new Label(this, SWT.NONE);
		lblCounts.setText("In window counts");

		counts = new LabelWrapper(this, SWT.NONE);
		counts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		counts.setNotifyType(NOTIFY_TYPE.VALUE_CHANGED);

		roiStart.addValueListener(new ValueAdapter("roiStartListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateUIAfterDetectorElementCompositeChange();
			}

		});
		roiEnd.addValueListener(new ValueAdapter("windowEndListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateUIAfterDetectorElementCompositeChange();
			}
		});

		controller.addRoiObserver(this);
	}

	void updateUIAfterDetectorElementCompositeChange() {
		controller.updatePlottedRegion(roiStart.getIntegerValue(), roiEnd.getIntegerValue());
	}

	public ScaleBox getRoiEnd() {
		return roiEnd;
	}

	public ScaleBox getRoiStart() {
		return roiStart;
	}

	public LabelWrapper getCounts() {
		return counts;
	}

	public TextWrapper getRoiName() {
		return roiName;
	}

	@Override
	public void update(Object source, Object arg) {
		if (source.equals(controller) && arg instanceof ROIEvent) {
			IROI newRoiFromUI = ((ROIEvent) arg).getROI();
			final int start = (int) Math.round(newRoiFromUI.getPointX());
			final int end = (int) (start + Math.round(newRoiFromUI.getBounds().getLength(0)));
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					roiStart.off();
					roiStart.setIntegerValue(start);
					roiStart.on();
					roiEnd.off();
					roiEnd.setIntegerValue(end);
					roiEnd.on();
					controller.updateBeanFromUI();
				}
			});
		}

	}
}
