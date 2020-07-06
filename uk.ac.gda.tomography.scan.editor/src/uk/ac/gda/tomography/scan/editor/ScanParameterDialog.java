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

package uk.ac.gda.tomography.scan.editor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.NumberFormat;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.tomography.scan.TomoScanParameters;
import uk.ac.gda.tomography.scan.presentation.ParametersComposite;

public class ScanParameterDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ScanParameterDialog.class);
	private static final String DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL = "tomographyScanModel";
	private static final String DATADIR_SUFFIX_LIVE_PROPERTY = "gda.data.scan.datawriter.datadir.subdir.live";
	private static final String DATADIR_SUFFIX_TEMP = "tmp";

	/**
	 * Used for converter in bindings to doubles
	 */
	private static final int 	DECIMAL_PLACES = 4;

	private TomoScanParameters model;
	private DataBindingContext ctx;
	//private TomographyParametersAcquisitionController controller;

	/**
	 * This constructor assumes 1) no controller 2) the model is stored in IDialogSettings
	 * @param parentShell
	 */
	public ScanParameterDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Tomography Scan Parameters");
	}

	@Override
	public Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		if (Objects.isNull(getModel())) {
			logger.error("No data model available: cannot create dialog");
			return comp;
		}

		GridDataFactory.fillDefaults().applyTo(comp);
		comp.setLayout(new GridLayout());

		ParametersComposite editor = new ParametersComposite(comp, SWT.NONE);
		editor.setLayoutData(new GridData());
		createBindings(editor);

		editor.getSendDataToTempDirectory().addListener(SWT.Selection, event ->	updateOutputPath(editor));
		updateOutputPath(editor);

		return comp;
	}

	private void updateOutputPath(ParametersComposite editor) {
		String outputDirectoryPath = InterfaceProvider.getCommandRunner().evaluateCommand("InterfaceProvider.getPathConstructor().createFromDefaultProperty()");
		final boolean tmpSelected = editor.getSendDataToTempDirectory().getSelection();
		final String outputPathSuffix = LocalProperties.get(DATADIR_SUFFIX_LIVE_PROPERTY, "");

		// We have noted cases where the "tmp" suffix has been left on the path from a previous run, so remove any suffix first
		outputDirectoryPath = outputDirectoryPath.replaceAll(DATADIR_SUFFIX_TEMP + "[/]{0,1}$", "");
		outputDirectoryPath = outputDirectoryPath.replaceAll(outputPathSuffix + "[/]{0,1}$", "");

		final String suffix = tmpSelected ? DATADIR_SUFFIX_TEMP : outputPathSuffix;
		outputDirectoryPath = Paths.get(outputDirectoryPath, suffix).toString();
		editor.getOutputDirectory().setText(outputDirectoryPath);
	}

	private void createBindings(ParametersComposite editor) {
		ctx = new DataBindingContext();

		bindCombo(editor.getRotationStage(), "rotationStage");
		bindCombo(editor.getLinearStage(), "linearStage");
		bindButton(editor.getSendDataToTempDirectory(), "sendDataToTempDirectory");
		bindSimpleText(editor.getTitle(), "title");
		bindText(editor.getExposure(), "exposureTime");
		bindText(editor.getMinI(), "minI");
		bindText(editor.getInBeamPosition(), "inBeamPosition");
		bindText(editor.getOutBeamPosition(), "outOfBeamPosition");
		bindText(editor.getStart(), "start");
		bindText(editor.getStop(), "stop");
		bindText(editor.getStep(), "step");
		bindSimpleText(editor.getImagesPerDark(), "imagesPerDark");
		bindSimpleText(editor.getDarkFieldInterval(), "darkFieldInterval");
		bindSimpleText(editor.getImagesPerFlat(), "imagesPerFlat");
		bindSimpleText(editor.getFlatFieldInterval(), "flatFieldInterval");
		bindButton(editor.getFlyScan(), "flyScan");
		bindButton(editor.getExtraFlatsAtEnd(), "extraFlatsAtEnd");
		bindSimpleText(editor.getNumFlyScans(), "numFlyScans");
		bindText(editor.getFlyScanDelay(), "flyScanDelay");
		bindButton(editor.getCloseShutterAfterLastScan(), "closeShutterAfterLastScan");
		bindSimpleText(editor.getDetectorToSampleDistance(), "detectorToSampleDistance");
		bindCombo(editor.getDetectorToSampleDistanceUnits(),"detectorToSampleDistanceUnits");
		bindSimpleText(editor.getxPixelSize(), "xPixelSize");
		bindCombo(editor.getxPixelSizeUnits(), "xPixelSizeUnits");
		bindSimpleText(editor.getyPixelSize(), "yPixelSize");
		bindCombo(editor.getyPixelSizeUnits(), "yPixelSizeUnits");
		bindSimpleText(editor.getApproxCentreOfRotation(), "approxCentreOfRotation");
	}

	private void bindCombo(Combo combo, String property) {
		ctx.bindValue(WidgetProperties.selection().observe(combo),
				PojoProperties.value(property).observe(getModel()));
	}

	private void bindButton(Button button, String property) {
		ctx.bindValue(WidgetProperties.selection().observe(button),
				PojoProperties.value(property).observe(getModel()));
	}

	private void bindText(Text text, String property) {
		NumberFormat fourDecimalPlacesFormat = NumberFormat.getNumberInstance();
		fourDecimalPlacesFormat.setMaximumFractionDigits(DECIMAL_PLACES);
		IConverter targetToModelConverter = StringToNumberConverter.toDouble(fourDecimalPlacesFormat, true);
		IConverter modelToTargetConverter = NumberToStringConverter.fromDouble(fourDecimalPlacesFormat, true);
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(text),
				PojoProperties.value(property).observe(getModel()),
				new UpdateValueStrategy().setConverter(targetToModelConverter),
				new UpdateValueStrategy().setConverter(modelToTargetConverter));
	}

	/**
	 * Use for binding Text widget to String / int (no NumberFormat needed)
	 * @param text
	 * @param property
	 */
	private void bindSimpleText(Text text, String property) {
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(text),
				PojoProperties.value(property).observe(getModel())
				);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Run",
				false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	private String getModelFilePath() {
		String gdaVar = InterfaceProvider.getPathConstructor().createFromProperty(LocalProperties.GDA_VAR_DIR);
		return gdaVar + "/" + DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL + ".json";
	}

	@Override
	protected void okPressed() {
		final IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		final String modelFilePath = getModelFilePath();
		try {
			final String modelJson = ServiceHolder.getMarshallerService().marshal(getModel());
			dialogSettings.put(DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL, modelJson);
			Files.write(Paths.get(getModelFilePath()), modelJson.getBytes());
		} catch (Exception e) {
			logger.error("Error saving parameters", e);
			return;
		}

		final String command = "tomographyScan.parameters_from_json('" + modelFilePath + "')";
		final String jobLabel = "TomoScan Scan: " + model.getTitle();

		try {
			CommandQueueViewFactory.getQueue().addToTail(new JythonCommandCommandProvider(command, jobLabel, modelFilePath));
			CommandQueueViewFactory.showView();
		} catch (Exception e) {
			logger.error("Error submitting tomoscan to queue", e);
		}

		super.okPressed();
	}

	private TomoScanParameters getModel() {
		if (Objects.isNull(this.model)) {
			try {
				this.model = getIDialogModel();
			} catch (Exception e) {
				logger.info(String.format("No model in %s", DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL), e);
			}
//			if (Objects.nonNull(getController()) && Objects.nonNull(getController().getAcquisition())) {
//				// --TBD --//
//				//this.model = getController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
//			}
		}
		return this.model;
	}

	/**
	 * This approach is used typically by I13
	 * @return
	 * @throws Exception
	 */
	private TomoScanParameters getIDialogModel() throws Exception {
		final IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		final String modelJson = dialogSettings.get(DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL);
		if (modelJson != null) {
			try {
				return ServiceHolder.getMarshallerService().unmarshal(modelJson, TomoScanParameters.class);
			} catch (Exception e) {
				logger.warn("Cannot retrieve saved parameters; using defaults", e);
			}
		}
		return new TomoScanParameters();
	}
}
