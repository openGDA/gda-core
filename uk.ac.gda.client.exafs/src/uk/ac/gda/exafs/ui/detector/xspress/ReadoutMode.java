/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.configuration.properties.LocalProperties;
import gda.device.detector.xspress.XspressDetector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class ReadoutMode {
	private ComboWrapper readoutMode;
	private Composite parent;
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");
	private ResolutionGrade resolutionGrade;
	private Label readoutModeLabel;
	
	public ReadoutMode(Composite parent, ResolutionGrade resolutionGrade) {
		this.parent = parent;
		this.resolutionGrade = resolutionGrade;
		readoutModeLabel = new Label(parent, SWT.NONE);
		readoutModeLabel.setText("Read out mode");
		readoutModeLabel.setToolTipText("The type of data which will be written to file");
		readoutModeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		if (modeOverride)
			GridUtils.setVisibleAndLayout(readoutModeLabel, false);
		readoutMode = new ComboWrapper(parent, SWT.READ_ONLY);
		readoutMode.setItems(new String[] { XspressDetector.READOUT_SCALERONLY, XspressDetector.READOUT_MCA, XspressDetector.READOUT_ROIS });
		readoutMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		readoutMode.select(0);
	}
	
	public void addListener(){
		readoutMode.addValueListener(new ValueAdapter("readoutMode") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				GridUtils.startMultiLayout(parent.getParent());
				try {
					updateOverrideMode();
					boolean readoutRois = false;
					if(resolutionGrade.getResolutionGradeCombo().getValue().equals(XspressDetector.READOUT_ROIS))
						readoutRois = true;
					resolutionGrade.updateResModeItems(readoutRois);
//					updateRoiVisibility();
//					updateResGradeVisibility(parent);
				} finally {
					GridUtils.endMultiLayout();
				}
			}
		});
	}
	
	public void updateOverrideMode() {
		if (modeOverride)
			this.readoutMode.setValue(XspressDetector.READOUT_ROIS);
	}
	
	public boolean isModeOveride(){
		return modeOverride;
	}
	
	public void setVisibility(boolean visibility){
			readoutModeLabel.setVisible(visibility);
			readoutMode.setVisible(visibility);
	}

	public ComboWrapper getReadoutMode() {
		return readoutMode;
	}

}