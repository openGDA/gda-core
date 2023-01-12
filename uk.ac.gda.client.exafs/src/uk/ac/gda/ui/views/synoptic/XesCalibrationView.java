/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.views.synoptic;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.exafs.xes.IXesOffsets;
import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControlBase;
import uk.ac.gda.client.livecontrol.ScannablePositionerControl;

public class XesCalibrationView extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(XesCalibrationView.class);

	private String offsetObjectName = "";
	private IXesOffsets offsets;
	private Text loadedOffsetFileTextbox;


	private Label addLabel(Composite parent, String labelText) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		return label;
	}

	private Button addButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		return button;
	}

	private void showWarningDialog(String title, String message) {
		MessageDialog.openWarning(Display.getCurrent().getActiveShell(), title, message);

	}
	private void showViewCreationMessageProblem(String message) {
		showWarningDialog("Cannot open XES calibration view", "Cannot open XES calibration view : "+message);
	}

	@Override
	public void createControl(Composite parent) {
		offsets = Finder.find(offsetObjectName);
		if (offsets == null) {
			showViewCreationMessageProblem("required XesOffset object not found on server.");
			return;
		}
		Scannable energyScannable = Finder.find(offsets.getXesEnergyScannableName());

		if (energyScannable == null) {
			showViewCreationMessageProblem("energy scannable "+offsets.getXesEnergyScannableName()+" could not be found.");
			return;
		}

		Composite comp = new Group(parent, SWT.NONE);
		comp.setLayout(new GridLayout(4, false));

		// Set the parent background to match these widgets
		parent.getParent().setBackground(comp.getBackground());

		// First row : XES energy control, expected energy and calibrate button
		ScannablePositionerControl posControl = new ScannablePositionerControl();
		posControl.setHorizontalLayout(true);
		posControl.setScannableName(energyScannable.getName());
		posControl.setDisplayName(energyScannable.getName());

		// XES energy control goes in its own composite
		Composite energyComp = new Composite(comp, SWT.NONE);
		energyComp.setLayout(new FillLayout());
		posControl.createControl(energyComp);

		// Make sure the positioner control has default background colour behaviour
		// (i.e. white background in Text widget rather than grey)
		energyComp.setBackgroundMode(SWT.INHERIT_DEFAULT);

		String[] energyPos = new String[] {"0"};
		try {
			energyPos = ScannableUtils.getFormattedCurrentPositionArray(energyScannable);
		} catch (DeviceException e) {
			logger.error("Problem getting energy from {}", energyScannable.getName(), e);
		}

		addLabel(comp, "Expected energy");

		Text expectedEnergyTextbox = new Text(comp, SWT.NONE);
		expectedEnergyTextbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		expectedEnergyTextbox.setText(energyPos[0]);

		Button calibrateButton = addButton(comp, "Calibrate");
		calibrateButton.addListener(SWT.Selection, event -> applyCalibration(expectedEnergyTextbox.getText()));
		calibrateButton.setToolTipText("Calculate the offsets for the expected energy and apply to the spectrometer motors");

		// Second row : filename, load save offset button
		addLabel(comp, "Filename");

		Text offsetFileTextbox = new Text(comp, SWT.NONE);
		offsetFileTextbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button loadFileButton = addButton(comp, "Load file");
		loadFileButton.setToolTipText("Load offset values from the given file and apply to the spectrometer motors");
		loadFileButton.addListener(SWT.Selection, event -> loadFromFile(offsetFileTextbox.getText()));

		Button saveFile = addButton(comp, "Save file");
		saveFile.setToolTipText("Save the current offset values for all spectrometer motors to file with given name.");
		saveFile.addListener(SWT.Selection, event -> saveToFile(offsetFileTextbox.getText()));

		// 3rd row : Load offset from file, remove offset button
		addLabel(comp, "Loaded offset file");

		loadedOffsetFileTextbox = new Text(comp, SWT.NONE);
		loadedOffsetFileTextbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button removeOffsetsButton = addButton(comp, "Remove offsets");
		removeOffsetsButton.setToolTipText("Set the offsets for all spectrometer motors to zero");
		removeOffsetsButton.addListener(SWT.Selection, event -> removeOffsets());
	}

	private void applyCalibration(String expectedEnergy) {
		try {
			Double energy = Double.parseDouble(expectedEnergy);
			offsets.applyFromLive(energy);
		} catch (NumberFormatException e) {
			String message = "Could not run offset calculation - expected energy " + expectedEnergy	+ " is not recognised as a number";
			showWarningDialog("Problem starting offset calculation", message);
		} catch (DeviceException | IOException e) {
			logger.error("Problem running XES offset calculation", e);
			String message = "Problem running calibration : " + e.getMessage() + ".\nSee log panel for more details";
			showWarningDialog("Problem running offset calculation", message);
		}
	}

	private void loadFromFile(String filename) {
		try {
			offsets.apply(filename);
			loadedOffsetFileTextbox.setText(filename);
		} catch (IOException e) {
			logger.error("Problem loading offsets from file {}", filename, e);
			showWarningDialog("Problem loading offsets from file", "Problem loading offsets from file : "+e.getMessage());
		}
	}

	private void saveToFile(String filename) {
		if (filename == null || filename.isEmpty()) {
			showWarningDialog( "Problem saving offsets to file", "File name to save offsets to is empty.");
			return;
		}
		try {
			offsets.saveAs(filename);
		} catch (IOException e) {
			logger.error("Problem saving offsets to file {}", filename, e);
			showWarningDialog("Problem saving to file", "Problem saving offsets to file : "+e.getMessage()+".\nSee log panel for more information");
		}
	}

	private void removeOffsets() {
		offsets.removeAll();
	}

	public void setOffsetObjectName(String offsetObjectName) {
		this.offsetObjectName = offsetObjectName;
	}
}
