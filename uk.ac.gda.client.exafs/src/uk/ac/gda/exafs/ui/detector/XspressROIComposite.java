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

import gda.configuration.properties.LocalProperties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.FieldComposite.NOTIFY_TYPE;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

public class XspressROIComposite extends DetectorROIComposite {
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");
	private FieldWidgetsForDetectorElementsComposite widgets;
	private Label lblName;
	private Label windowStartLabel;
	private Label windowEndLabel;
	private Label lblCounts;
	private ScaleBox roiStart;
	private ScaleBox roiEnd;
	private LabelWrapper counts;
	private TextWrapper roiName;
	
	public XspressROIComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		lblName = new Label(this, SWT.NONE);
		lblName.setText("Name");
		roiName = new TextWrapper(this, SWT.BORDER);
		roiName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		windowStartLabel = new Label(this, SWT.NONE);
		windowStartLabel.setText("Region start");
		roiStart = new ScaleBox(this, SWT.NONE);
		roiStart.setIntegerBox(true);
		roiStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		roiStart.setButtonVisible(true);
		roiStart.setDecimalPlaces(0);
		windowEndLabel = new Label(this, SWT.NONE);
		windowEndLabel.setText("Region end");
		roiEnd = new ScaleBox(this, SWT.NONE);
		roiEnd.setIntegerBox(true);
		roiEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		roiEnd.setButtonVisible(true);
		roiEnd.setDecimalPlaces(0);
		lblCounts = new Label(this, SWT.NONE);
		lblCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblCounts.setText("In window counts");
		counts = new LabelWrapper(this, SWT.NONE);
		counts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		counts.setNotifyType(NOTIFY_TYPE.VALUE_CHANGED);
		roiStart.setMaximum(roiEnd);
		roiEnd.setMinimum(roiStart);
	}
	
	@Override
	public void setVisible(boolean visible){
		lblName.setVisible(visible);
		windowStartLabel.setVisible(visible);
		windowEndLabel.setVisible(visible);
		lblCounts.setVisible(visible);
		roiStart.setVisible(visible);
		roiEnd.setVisible(visible);
		counts.setVisible(visible);
		roiName.setVisible(visible);
	}
	
	public void setFitTypeVisibility() {
		if(modeOverride)
			return;
		GridUtils.startMultiLayout(this);
		GridUtils.endMultiLayout();
	}	

	@Override
	public FieldWidgetsForDetectorElementsComposite getFieldWidgetsForDetectorElementsComposite() {
		if (widgets == null)
			widgets = new FieldWidgetsForDetectorElementsComposite(getRoiStart(), getRoiEnd(), getCounts());
		return widgets;
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
	
	public ScaleBox getRegionEnd() {
		return roiEnd;
	}

	public ScaleBox getRegionStart() {
		return roiStart;
	}
	
	public ScaleBox getWindowEnd() {
		return roiEnd;
	}

	public ScaleBox getWindowStart() {
		return roiStart;
	}
	
}