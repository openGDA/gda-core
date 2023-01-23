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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.wrappers.ComboAndNumberWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.common.rcp.util.GridUtils;

/**
 * Composite with combo boxes to allow selection of readout mode type and resolution grade
 * @since 13/6/2016
 */
public class FluoDetectorReadoutModeComposite extends Composite {
	private ComboWrapper readoutMode;
	private int lastComboIndex;
	private ComboAndNumberWrapper resolutionGradeCombo;
	private ComboWrapper regionType;
	private Label resGradeLabel;
	private Label regionTypeLabel;
	private Group readoutModeGroup;

	public FluoDetectorReadoutModeComposite(Composite parentComposite, int style) {
		super(parentComposite, style);

		this.setLayout(new FillLayout());

		readoutModeGroup = new Group(this, SWT.NONE);
		readoutModeGroup.setText("Readout mode");

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(readoutModeGroup);
		Label label = new Label(readoutModeGroup, SWT.NONE);
		label.setText("Read out mode");
		label.setToolTipText("The type of data which will be written to file");

		readoutMode = new ComboWrapper(readoutModeGroup, SWT.READ_ONLY);
		readoutMode.setItems(new String[] { XspressDetector.READOUT_SCALERONLY, XspressDetector.READOUT_MCA, XspressDetector.READOUT_ROIS });
		readoutMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lastComboIndex = 0;
		readoutMode.select(lastComboIndex);

		resGradeLabel = new Label(readoutModeGroup, SWT.NONE);
		resGradeLabel.setText("Resolution grade");
		resGradeLabel.setToolTipText("The resolution setting during calibration and XAS scans");
		resGradeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Setup Resolution Grade combo - also stores the value of resolution threshold
		resolutionGradeCombo = new ComboAndNumberWrapper(readoutModeGroup, SWT.READ_ONLY,
				Arrays.asList(new String[] { ResGrades.THRESHOLD }));

		Map<String, Object> resGrades;
		resGrades = new HashMap<String, Object>(3);
		resGrades.put("Sum all grades", ResGrades.NONE);
		resGrades.put("Individual grades", ResGrades.ALLGRADES);
		resGrades.put("Threshold", ResGrades.THRESHOLD);

		resolutionGradeCombo.setItems(resGrades);
		resolutionGradeCombo.getValueField().setMaximum(15.99);
		resolutionGradeCombo.getValueField().setMinimum(0.0);
		resolutionGradeCombo.getValueField().setDecimalPlaces(1);
		resolutionGradeCombo.getValueField().setNumericValue(1d);
		resolutionGradeCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		resolutionGradeCombo.addValueListener(new ValueAdapter("resGrade") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
			}
		});

		// Add listener to text value field that fires the combo box listeners so the richbeans (and UI) knows a change has occurred.
		resolutionGradeCombo.getValueField().addValueListener(new ValueAdapter("resGradeValue") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				resolutionGradeCombo.fireValueListeners();
			}
		});

		regionTypeLabel = new Label(readoutModeGroup, SWT.NONE);
		regionTypeLabel.setText("Region type");

		regionType = new ComboWrapper(readoutModeGroup, SWT.READ_ONLY);
		regionType.setItems(new String[]{XspressParameters.VIRTUALSCALER, XspressROI.MCA});
		regionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		regionType.select(0);

		readoutMode.addValueListener( new ValueAdapter("listenerForGradeComboUpdate") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateControlVisibility();
			}
		} );

	}

	public void updateControlVisibility() {
		String currentReadoutMode = getReadoutMode();
		if ( currentReadoutMode != null )
			showResGradeRegionControls( currentReadoutMode.equals( XspressDetector.READOUT_ROIS ) );
	}

	// Hide, show resolution grade and region type controls, and adjust layout to fit.
	public void showResGradeRegionControls( boolean showControls ) {
		GridUtils.startMultiLayout(this);
		try {
			GridUtils.setVisibleAndLayout(resGradeLabel, showControls);
			GridUtils.setVisibleAndLayout(resolutionGradeCombo, showControls);
			GridUtils.setVisibleAndLayout(regionTypeLabel, showControls);
			GridUtils.setVisibleAndLayout(regionType, showControls);
		} finally {
			GridUtils.endMultiLayout();
		}
	}

	// Get readout mode
	public String getReadoutMode() {
		int currentIndex = readoutMode.getSelectionIndex();
		return readoutMode.getItem(currentIndex);
	}

	// Set selected item in combo box corresponding to given readout mode
	public void setReadoutMode(String newReadoutMode) {
		if ( readoutMode != null ) {
			readoutMode.setValue(newReadoutMode);
			updateControlVisibility();
		}
	}

	public ComboWrapper getReadoutCombo() {
		return readoutMode;
	}

	// Get resolution mode
	public String getResGrade() {
		int currentIndex = resolutionGradeCombo.getSelectionIndex();
		return resolutionGradeCombo.getItem(currentIndex);
	}

	// Set resolution grade
	public void setResGrade(String newResGrade) {
		resolutionGradeCombo.setValue(newResGrade);
	}

	// Get resolution grade combo
	public ComboWrapper getResolutionGradeCombo() {
		return resolutionGradeCombo;
	}

	// Set region type
	public void setRegionType(String newResGrade) {
		regionType.setValue(newResGrade);
	}

	// Get region type
	public void getRegionType() {
		int currentIndex = regionType.getSelectionIndex();
		regionType.getItem(currentIndex);
	}

	// Get region type combo
	public ComboWrapper getRegionTypeCombo() {
		return regionType;
	}

}
