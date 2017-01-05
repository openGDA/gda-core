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

package uk.ac.gda.epics.dxp.client.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.edxd.common.IEdxdAlignment;

public class EDXDChecklistView extends ViewPart {

	private static final String LBL_Q_CALIBRATION_FILE = "Q Calibration file:";
	private static final String LBL_BTN_LOAD = "Load";
	private static final String DEFAULT_VAL_ENERGY_CALIBRATION = "/filepath/plus_name.txt";
	private static final String LBL_ENERGY_CALIBRATION_FILE = "Energy calibration file:";
	private static final String LBL_Q_AXIS_CALIBRATION = "Q-axis calibration";
	private static final String LBL_COLLIMATOR_ANGULAR_ALIGNMENT = "Collimator Angular alignment";
	private static final String LBL_COLLIMATOR_XY_ALIGNMENT = "Collimator XYZ alignment";
	private static final String LBL_COLLIMATOR_IN = "Collimator in";
	private static final String LBL_DET_XY_ALIGNMENT = "Detector XY alignment";
	private static final String LBL_ENERGY_CALIBRATION = "Energy calibration";
	private static final String LBL_BTN_RUN = "Run";
	private static final String LBL_PREAMP_GAIN_CALIB = "Preamp gain calibration";
	private static final String LBL_COLLIMATOR_OUT = "Collimator out";
	private static final String LBL_CALIB_SAMPLE_IN = "Calibration sample in";
	private static final String LBL_BEAM_CENTRED = "Beam centred";
	private static final String LBL_DET_MECHANICAL_ALIGNMENT = "Detector mechanical alignment";
	private static final String FORM_HEADER = "EDXD Alignment Checklist";
	public static final String ID = "uk.ac.gda.epics.dxp.client.checklist";
	private Button btnRunPreampGain;
	private Button btnRunEnergyCalibration;
	private Button btnRunDetXyAlignment;
	private Button btnRunCollimatorXYAlignment;
	private Button btnRunCollimatorAngularAlignment;
	private Button btnRunQAxisCalibration;
	private Button btnLoadEnergyCalibration;
	private Button btnLoadQCalibration;

	private ScrolledForm scrolledForm;
	private Text txtEnergyCalibrationFile;
	private Text txtQCalibrationFile;

	private IEdxdAlignment edxdAlignment;

	public void setEdxdAlignment(IEdxdAlignment edxdAlignment) {
		this.edxdAlignment = edxdAlignment;
	}

	@Override
	public void createPartControl(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(getViewSite().getShell().getDisplay());

		formToolkit.setBackground(ColorConstants.white);

		scrolledForm = formToolkit.createScrolledForm(parent);

		scrolledForm.setText(FORM_HEADER);
		formToolkit.decorateFormHeading(scrolledForm.getForm());

		scrolledForm.getBody().setLayout(new FillLayout());
		Composite formContents = formToolkit.createComposite(scrolledForm.getBody());
		formContents.setLayout(new GridLayout(2, false));

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		formToolkit.createButton(formContents, LBL_DET_MECHANICAL_ALIGNMENT, SWT.CHECK).setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		formToolkit.createButton(formContents, LBL_BEAM_CENTRED, SWT.CHECK).setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		formToolkit.createButton(formContents, LBL_CALIB_SAMPLE_IN, SWT.CHECK).setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		formToolkit.createButton(formContents, LBL_COLLIMATOR_OUT, SWT.CHECK).setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		formToolkit.createButton(formContents, LBL_PREAMP_GAIN_CALIB, SWT.CHECK).setLayoutData(layoutData);
		btnRunPreampGain = formToolkit.createButton(formContents, LBL_BTN_RUN, SWT.PUSH);
		btnRunPreampGain.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunPreampGain.addSelectionListener(buttonSelAdapter);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		formToolkit.createButton(formContents, LBL_ENERGY_CALIBRATION, SWT.CHECK).setLayoutData(layoutData);
		btnRunEnergyCalibration = formToolkit.createButton(formContents, LBL_BTN_RUN, SWT.PUSH);
		btnRunEnergyCalibration.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunEnergyCalibration.addSelectionListener(buttonSelAdapter);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		formToolkit.createButton(formContents, LBL_DET_XY_ALIGNMENT, SWT.CHECK).setLayoutData(layoutData);
		btnRunDetXyAlignment = formToolkit.createButton(formContents, LBL_BTN_RUN, SWT.PUSH);
		btnRunDetXyAlignment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunDetXyAlignment.addSelectionListener(buttonSelAdapter);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		formToolkit.createButton(formContents, LBL_COLLIMATOR_IN, SWT.CHECK).setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		formToolkit.createButton(formContents, LBL_COLLIMATOR_XY_ALIGNMENT, SWT.CHECK).setLayoutData(layoutData);
		btnRunCollimatorXYAlignment = formToolkit.createButton(formContents, LBL_BTN_RUN, SWT.PUSH);
		btnRunCollimatorXYAlignment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunCollimatorXYAlignment.addSelectionListener(buttonSelAdapter);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		formToolkit.createButton(formContents, LBL_COLLIMATOR_ANGULAR_ALIGNMENT, SWT.CHECK).setLayoutData(layoutData);
		btnRunCollimatorAngularAlignment = formToolkit.createButton(formContents, LBL_BTN_RUN, SWT.PUSH);
		btnRunCollimatorAngularAlignment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunCollimatorAngularAlignment.addSelectionListener(buttonSelAdapter);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		formToolkit.createButton(formContents, LBL_Q_AXIS_CALIBRATION, SWT.CHECK).setLayoutData(layoutData);
		btnRunQAxisCalibration = formToolkit.createButton(formContents, LBL_BTN_RUN, SWT.PUSH);
		btnRunQAxisCalibration.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunQAxisCalibration.addSelectionListener(buttonSelAdapter);

		Composite fileLoadComposite = formToolkit.createComposite(formContents);
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		layoutData2.horizontalSpan = 2;
		fileLoadComposite.setLayoutData(layoutData2);

		fileLoadComposite.setLayout(new GridLayout(2, false));

		GridData layoutData3 = new GridData();
		layoutData3.horizontalSpan = 2;
		formToolkit.createLabel(fileLoadComposite, LBL_ENERGY_CALIBRATION_FILE).setLayoutData(layoutData3);

		txtEnergyCalibrationFile = formToolkit
				.createText(fileLoadComposite, DEFAULT_VAL_ENERGY_CALIBRATION, SWT.BORDER);
		txtEnergyCalibrationFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnLoadEnergyCalibration = formToolkit.createButton(fileLoadComposite, LBL_BTN_LOAD, SWT.None);
		btnLoadEnergyCalibration.addSelectionListener(buttonSelAdapter);

		layoutData3 = new GridData();
		layoutData3.horizontalSpan = 2;
		formToolkit.createLabel(fileLoadComposite, LBL_Q_CALIBRATION_FILE).setLayoutData(layoutData3);

		txtQCalibrationFile = formToolkit.createText(fileLoadComposite, DEFAULT_VAL_ENERGY_CALIBRATION, SWT.BORDER);
		txtQCalibrationFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnLoadQCalibration = formToolkit.createButton(fileLoadComposite, LBL_BTN_LOAD, SWT.None);
		btnLoadQCalibration.addSelectionListener(buttonSelAdapter);

		scrolledForm.setMinSize(formContents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledForm.reflow(true);

		initialiseView();
	}

	private void initialiseView() {
		String lastSavedEnergyCalibrationFile = edxdAlignment.getLastSavedEnergyCalibrationFile();
		if (lastSavedEnergyCalibrationFile != null) {
			txtEnergyCalibrationFile.setText(lastSavedEnergyCalibrationFile);
		}
	}

	private SelectionAdapter buttonSelAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
			scrolledForm.setBusy(true);

			if (e.getSource().equals(btnLoadEnergyCalibration)) {
				edxdAlignment.loadEnergyCalibrationFile(txtEnergyCalibrationFile.getText());
				loadDialogMessage("Energy Calibration will be loaded");
			} else if (e.getSource().equals(btnLoadQCalibration)) {
				edxdAlignment.loadQCalibrationFile(txtEnergyCalibrationFile.getText());
				loadDialogMessage("Q Calibration will be loaded");
			} else if (e.getSource().equals(btnRunCollimatorAngularAlignment)) {
				edxdAlignment.runCollimatorAngularAlignment();
				loadDialogMessage("Collimator Angular Alignment will be run");
			} else if (e.getSource().equals(btnRunCollimatorXYAlignment)) {
				edxdAlignment.runCollimatorXYZAlignment();
				loadDialogMessage("Collimator XY Alignment will be run");
			} else if (e.getSource().equals(btnRunDetXyAlignment)) {
				edxdAlignment.runDetectorXYAlignment();
				loadDialogMessage("Detector XY Alignment will be run");
			} else if (e.getSource().equals(btnRunEnergyCalibration)) {
				edxdAlignment.runEnergyCalibration();
				loadDialogMessage("Energy Calibration will be run");
			} else if (e.getSource().equals(btnRunPreampGain)) {
				edxdAlignment.runPreampGain();
				loadDialogMessage("Preamp gain will be run");
			} else if (e.getSource().equals(btnRunQAxisCalibration)) {
				edxdAlignment.runQAxisCalibration();
				loadDialogMessage("Q Axis Calibration will be run");
			}

		}
	};

	private void loadDialogMessage(String message) {
		MessageDialog.openInformation(getViewSite().getShell(), "Info", message);
		scrolledForm.setBusy(false);
	}

	@Override
	public void setFocus() {

	}
	
	@Override
	public String getPartName() {
		return "Edxd Checklist";
	}

}
