/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldComposite.NOTIFY_TYPE;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 *
 */
public class VortexROIComposite extends DetectorROIComposite {

	private ScaleBox windowStart;
	private ScaleBox windowEnd;
	private LabelWrapper counts;
	private TextWrapper roiName;

	/**
	 * @param parent
	 * @param style
	 */
	public VortexROIComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label lblName = new Label(this, SWT.NONE);
		lblName.setText("Name");
		
		roiName = new TextWrapper(this, SWT.BORDER);
		roiName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final Label windowStartLabel = new Label(this, SWT.NONE);
		windowStartLabel.setText("Start");

		windowStart = new ScaleBox(this, SWT.NONE);
		windowStart.setIntegerBox(true);
		windowStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		windowStart.setButtonVisible(true);
		windowStart.setDecimalPlaces(0);

		final Label windowEndLabel = new Label(this, SWT.NONE);
		windowEndLabel.setText("End");

		windowEnd = new ScaleBox(this, SWT.NONE);
		windowEnd.setIntegerBox(true);
		windowEnd.setMaximum(1023);
		windowEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		windowEnd.setButtonVisible(true);
		windowEnd.setDecimalPlaces(0);
		
		windowStart.setMaximum(windowEnd);
		windowEnd.setMinimum(windowStart);
		
		Label lblCounts = new Label(this, SWT.NONE);
		lblCounts.setText("In window counts");
		
		counts = new LabelWrapper(this, SWT.NONE);
		counts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		counts.setNotifyType(NOTIFY_TYPE.VALUE_CHANGED);
	}
	
	/**
	 * @return d
	 */
	public ScaleBox getWindowEnd() {
		return windowEnd;
	}

	/**
	 * @return d
	 */
	public ScaleBox getWindowStart() {
		return windowStart;
	}


	public LabelWrapper getCounts() {
		return counts;
	}
	/**
	 * @return d
	 */
	public TextWrapper getRoiName() {
		return roiName;
	}

	FieldWidgetsForDetectorElementsComposite widgets;
	@Override
	public FieldWidgetsForDetectorElementsComposite getFieldWidgetsForDetectorElementsComposite() {
		if (widgets == null) {
			widgets = new FieldWidgetsForDetectorElementsComposite(getWindowStart(), getWindowEnd(), getCounts());
		}
		return widgets;
	}

}
