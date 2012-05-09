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

import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.FieldComposite.NOTIFY_TYPE;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 *
 */
public class XspressROIComposite extends DetectorROIComposite {

	private ScaleBox regionStart;
	private ScaleBox regionEnd;
	
	private LabelWrapper counts;
	private TextWrapper roiName;
	
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");

	/**
	 * @param parent
	 * @param style
	 */
	public XspressROIComposite(Composite parent, int style) {

		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label lblName = new Label(this, SWT.NONE);
		lblName.setText("Name");
		
		roiName = new TextWrapper(this, SWT.BORDER);
		roiName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final Label windowStartLabel = new Label(this, SWT.NONE);
		windowStartLabel.setText("Region start");

		regionStart = new ScaleBox(this, SWT.NONE);
		regionStart.setIntegerBox(true);
		regionStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		regionStart.setButtonVisible(true);
		regionStart.setDecimalPlaces(0);

		final Label windowEndLabel = new Label(this, SWT.NONE);
		windowEndLabel.setText("Region end");

		regionEnd = new ScaleBox(this, SWT.NONE);
		regionEnd.setIntegerBox(true);
		regionEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		regionEnd.setButtonVisible(true);
		regionEnd.setDecimalPlaces(0);

		Label lblCounts = new Label(this, SWT.NONE);
		lblCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblCounts.setText("In window counts");
		
		counts = new LabelWrapper(this, SWT.NONE);
		counts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		counts.setNotifyType(NOTIFY_TYPE.VALUE_CHANGED);
		
		regionStart.setMaximum(regionEnd);
		regionEnd.setMinimum(regionStart);

	}
	
	public void setFitTypeVisibility() {
		if(modeOverride)
			return;
		GridUtils.startMultiLayout(this);
		GridUtils.endMultiLayout();
	}	
	/**
	 * @return d
	 */
	public ScaleBox getRegionEnd() {
		return regionEnd;
	}

	/**
	 * @return d
	 */
	public ScaleBox getRegionStart() {
		return regionStart;
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
			widgets = new FieldWidgetsForDetectorElementsComposite(getRegionStart(), getRegionEnd(), getCounts());
		}
		return widgets;
	}	
}
