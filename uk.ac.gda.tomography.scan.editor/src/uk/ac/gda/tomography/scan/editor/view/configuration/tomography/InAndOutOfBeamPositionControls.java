/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view.configuration.tomography;

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.innerComposite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.ClientSWTElements.numericTextBox;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.ui.controller.PositionManager;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue.PositionType;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.tomography.scan.editor.view.configuration.TomographyConfiguration;
import uk.ac.gda.ui.tool.Reloadable;

public class InAndOutOfBeamPositionControls implements Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(InAndOutOfBeamPositionControls.class);

	private Scannable scannable;

	private PositionManager positionManager;

	private double inBeamPosition;
	private double outOfBeamPosition;

	private Label summary;

	public InAndOutOfBeamPositionControls(TomographyConfiguration configuration) {
		scannable = Finder.find(configuration.getLateralAxis());

		inBeamPosition = getPositionManager().getConfiguredPosition(AcquisitionPropertyType.TOMOGRAPHY).stream()
				.filter(val -> val.getScannableName().equals(scannable.getName()))
				.findFirst()
				.map(ScannablePropertiesValue::getPosition)
				.map(Double.class::cast)
				.orElseGet(this::positionFromScannable);

		outOfBeamPosition = getPositionManager().getConfiguredPosition(AcquisitionTemplateType.FLAT).stream()
				.filter(val -> val.getScannableName().equals(scannable.getName()))
				.findFirst()
				.map(ScannablePropertiesValue::getPosition)
				.map(Double.class::cast)
				.map(absolute -> absolute - inBeamPosition)
				.orElse(configuration.getFlatFieldDisplacement());

		updateConfiguredPositions();
	}

	private double positionFromScannable() {
		try {
			return (double) scannable.getPosition();
		} catch (DeviceException e) {
			logger.error("Error reading {} position", scannable.getName(), e);
			return Double.NaN;
		}
	}

	private void updateConfiguredPositions() {
		var inBeam = new ScannablePropertiesValue();
		inBeam.setScannableName(scannable.getName());
		inBeam.setPositionType(PositionType.ABSOLUTE);
		inBeam.setPosition(inBeamPosition);
		getPositionManager().configurePosition(AcquisitionPropertyType.TOMOGRAPHY, List.of(inBeam));

		var outOfBeam = new ScannablePropertiesValue();
		outOfBeam.setScannableName(scannable.getName());
		outOfBeam.setPositionType(PositionType.ABSOLUTE);
		outOfBeam.setPosition(inBeamPosition + outOfBeamPosition);
		getPositionManager().configurePosition(AcquisitionTemplateType.FLAT, List.of(outOfBeam));
	}

	public void createControls(Composite parent) {
		var composite = composite(parent, 2);

		var button = new Button(composite, SWT.PUSH);
		STRETCH.applyTo(button);
		button.setText("Configure positions");
		button.addListener(SWT.Selection, event -> openDialog());

		summary = new Label(composite, SWT.NONE);
		updateSummary();
	}

	@Override
	public void reload() {

		ScanningConfiguration configuration = SpringApplicationContextFacade.getBean(ScanningAcquisitionController.class).getAcquisition().getAcquisitionConfiguration();
		var startPosition = configuration.getAcquisitionParameters().getStartPosition();
		if (startPosition == null) return; // nothing to reload

		startPosition.stream()
			.filter(doc -> doc.getDevice().equals(scannable.getName()))
			.findFirst().ifPresent(doc -> inBeamPosition = Double.parseDouble(doc.getPosition().toString()));

		var flatPosition = configuration.getImageCalibration().getFlatCalibration().getPosition();
		if (flatPosition != null) {
			flatPosition.stream()
				.filter(doc -> doc.getDevice().equals(scannable.getName()))
				.findFirst().ifPresent(doc -> outOfBeamPosition = Double.parseDouble(doc.getPosition().toString()) - inBeamPosition);
		}

		updateConfiguredPositions();

		updateSummary();
	}

	private void updateSummary() {
		summary.setText(String.format("In beam: %.2f%nOut of beam: %.2f", inBeamPosition, outOfBeamPosition));
	}

	private void openDialog() {
		var dialog = new PositionDialog(Display.getCurrent().getActiveShell());
		if (dialog.open() == Window.OK) {
			updateConfiguredPositions();
			updateSummary();
		}
	}

	private class PositionDialog extends Dialog {

		private Text inBeamPosition;
		private Text outOfBeamPosition;

		protected PositionDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			var composite = composite(parent, 2);

			label(composite, "In-beam position (Base X)");

			var inBeam = innerComposite(composite, 2, false);

			inBeamPosition = numericTextBox(inBeam);
			inBeamPosition.setToolTipText("Absolute position for base x (mm)");

			var reset = new Button(inBeam, SWT.PUSH);
			reset.setText("Reset");
			reset.setToolTipText("Reset to current motor position");
			reset.addListener(SWT.Selection, event -> inBeamPosition.setText(String.valueOf(positionFromScannable())));

			label(composite, "Out-of-beam displacement");
			outOfBeamPosition = numericTextBox(composite);
			outOfBeamPosition.setToolTipText("Position relative to in-beam position (mm)");

			inBeamPosition.setText(String.valueOf(InAndOutOfBeamPositionControls.this.inBeamPosition));
			outOfBeamPosition.setText(String.valueOf(InAndOutOfBeamPositionControls.this.outOfBeamPosition));

			var testInBeamPosition = new Button(composite, SWT.PUSH);
			testInBeamPosition.setText("Move in beam");
			STRETCH.applyTo(testInBeamPosition);
			testInBeamPosition.addListener(SWT.Selection, e -> moveTo(Double.parseDouble(inBeamPosition.getText())));

			var testOutOfBeamPosition = new Button(composite, SWT.PUSH);
			testOutOfBeamPosition.setText("Test out-of-beam");
			STRETCH.applyTo(testOutOfBeamPosition);
			testOutOfBeamPosition.addListener(SWT.Selection, e -> moveTo(Double.parseDouble(inBeamPosition.getText()) + Double.parseDouble(outOfBeamPosition.getText())));

			return composite;
		}

		private void moveTo(double position) {
			try {
				scannable.asynchronousMoveTo(position);
			} catch (DeviceException e) {
				logger.error("Failed to move {} to {}", scannable.getName(), position, e);
			}
		}

		@Override
		protected void okPressed() {
			InAndOutOfBeamPositionControls.this.inBeamPosition = Double.parseDouble(inBeamPosition.getText());
			InAndOutOfBeamPositionControls.this.outOfBeamPosition = Double.parseDouble(outOfBeamPosition.getText());
			super.okPressed();
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("Configure in- and out-of-beam positions");
		}

	}

	private PositionManager getPositionManager() {
		if (positionManager == null) {
			positionManager = SpringApplicationContextFacade.getBean(PositionManager.class);
		}
		return positionManager;
	}

}
