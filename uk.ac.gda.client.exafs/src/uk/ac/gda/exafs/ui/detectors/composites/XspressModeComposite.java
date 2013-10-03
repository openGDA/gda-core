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

package uk.ac.gda.exafs.ui.detectors.composites;

import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class XspressModeComposite extends Composite {
	private static HashMap<String, Object> RES_ALL;
	private static HashMap<String, Object> RES_NO_16;

	static {
		RES_ALL = new HashMap<String, Object>(3);
		RES_ALL.put("Sum all grades", ResGrades.NONE);
		RES_ALL.put("Individual grades", ResGrades.ALLGRADES);
		RES_ALL.put("Threshold", ResGrades.THRESHOLD);

		RES_NO_16 = new HashMap<String, Object>(3);
		RES_NO_16.put("Sum all grades", ResGrades.NONE);
		RES_NO_16.put("Threshold", ResGrades.THRESHOLD);
	}

	private ComboWrapper readoutMode;
	private ComboAndNumberWrapper resGrade;
	private Composite thisComposite;
	private Label resGradeLabel;
	private Label lblRegionBins;
	private ComboWrapper regionType;

	public XspressModeComposite(Composite parent, int style) {
		super(parent, style);
		thisComposite = new Composite(parent, SWT.NONE);
		thisComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		thisComposite.setLayout(gridLayout_1);

		final Label readoutModeLabel = new Label(thisComposite, SWT.NONE);
		readoutModeLabel.setText("Read out mode");
		readoutModeLabel.setToolTipText("The type of data which will be written to file");
		readoutModeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		this.readoutMode = new ComboWrapper(thisComposite, SWT.READ_ONLY);
		readoutMode.setItems(new String[] { XspressDetector.READOUT_SCALERONLY, XspressDetector.READOUT_MCA,
				XspressDetector.READOUT_ROIS });
		readoutMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		readoutMode.select(0);
		readoutMode.addValueListener(new ValueAdapter("readoutMode") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
//				GridUtils.startMultiLayout(parentComposite);
//				try {
//					updateOverrideMode();
//					updateResModeItems();
//					updateRoiVisibility();
//					updateResGradeVisibility();
//					configureUI();
//				} finally {
//					GridUtils.endMultiLayout();
//				}
			}
		});

		resGradeLabel = new Label(thisComposite, SWT.NONE);
		resGradeLabel.setText("Resolution Grade");
		resGradeLabel.setToolTipText("The resolution setting during calibration and XAS scans");
		resGradeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		this.resGrade = new ComboAndNumberWrapper(thisComposite, SWT.READ_ONLY,
				Arrays.asList(new String[] { ResGrades.THRESHOLD }));
		resGrade.setItems(RES_ALL);
		resGrade.getValueField().setMaximum(15.99);
		resGrade.getValueField().setMinimum(0.0);
		resGrade.getValueField().setDecimalPlaces(1);
		resGrade.getValueField().setNumericValue(1d);
		resGrade.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		resGrade.addValueListener(new ValueAdapter("resGrade") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
//				updateAdditiveMode();
			}
		});
//		if (modeOverride) {
//			GridUtils.setVisibleAndLayout(readoutModeLabel, false);
//			GridUtils.setVisibleAndLayout(readoutMode, false);
//			GridUtils.setVisibleAndLayout(resGradeLabel, false);
//			GridUtils.setVisibleAndLayout(resGrade, false);
//			GridUtils.setVisibleAndLayout(lblRegionBins, false);
//			GridUtils.setVisibleAndLayout(regionType, false);
//		}
		lblRegionBins = new Label(thisComposite, SWT.NONE);
		lblRegionBins.setText("Region type");
		
		regionType = new ComboWrapper(thisComposite, SWT.READ_ONLY);
		regionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		regionType.setItems(new String[]{XspressParameters.VIRTUALSCALER, XspressROI.MCA});
		regionType.select(0);
	}

}
