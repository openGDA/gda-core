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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.stashing.IStashing;
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

import gda.commandqueue.JythonCommandCommandProvider;
import gda.jython.InterfaceProvider;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.tomography.scan.TomoScanParameters;
import uk.ac.gda.tomography.scan.presentation.ParametersComposite;

public class ScanParameterDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ScanParameterDialog.class);
	private static final String STASH_NAME = "uk.ac.gda.tomography.scan.editor.tomographyscanmodel.json";

	private TomoScanParameters model;
	private DataBindingContext ctx;

	public ScanParameterDialog(Shell parentShell) {
		super(parentShell);
		model = getModel();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Tomography Scan Parameters");
	}

	@Override
	public Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
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
		String outputDirectoryPath = getVisitPath();
		if (editor.getSendDataToTempDirectory().getSelection()) {
			outputDirectoryPath+="/tmp/";
		} else {
			outputDirectoryPath+="/raw/";
		}
		editor.getOutputDirectory().setText(outputDirectoryPath);
	}

	private String getVisitPath() {
		String dataPath = InterfaceProvider.getCommandRunner().evaluateCommand("PathConstructor.createFromDefaultProperty()");
		String[] dataPathSplit = dataPath.split("/");
		StringBuilder bld = new StringBuilder();
		for (int i = 1; i < 6; ++i) {
			bld.append('/');
			bld.append(dataPathSplit[i]);
		}
		return bld.toString();
	}

	private void createBindings(ParametersComposite editor) {
		ctx = new DataBindingContext();

		bindCombo(editor.getRotationStage(), "rotationStage");
		bindCombo(editor.getLinearStage(), "linearStage");
		bindButton(editor.getSendDataToTempDirectory(), "sendDataToTempDirectory");
		bindText(editor.getTitle(), "title");
		bindText(editor.getExposure(), "exposureTime");
		bindText(editor.getMinI(), "minI");
		bindText(editor.getInBeamPosition(), "inBeamPosition");
		bindText(editor.getOutBeamPosition(), "outOfBeamPosition");
		bindText(editor.getStart(), "start");
		bindText(editor.getStop(), "stop");
		bindText(editor.getStep(), "step");
		bindText(editor.getImagesPerDark(), "imagesPerDark");
		bindText(editor.getDarkFieldInterval(), "darkFieldInterval");
		bindText(editor.getImagesPerFlat(), "imagesPerFlat");
		bindText(editor.getFlatFieldInterval(), "flatFieldInterval");
		bindButton(editor.getFlyScan(), "flyScan");
		bindButton(editor.getExtraFlatsAtEnd(), "extraFlatsAtEnd");
		bindText(editor.getNumFlyScans(), "numFlyScans");
		bindText(editor.getFlyScanDelay(), "flyScanDelay");
		bindButton(editor.getCloseShutterAfterLastScan(), "closeShutterAfterLastScan");
		bindText(editor.getDetectorToSampleDistance(), "detectorToSampleDistance");
		bindCombo(editor.getDetectorToSampleDistanceUnits(),"detectorToSampleDistanceUnits");
		bindText(editor.getxPixelSize(), "xPixelSize");
		bindCombo(editor.getxPixelSizeUnits(), "xPixelSizeUnits");
		bindText(editor.getyPixelSize(), "yPixelSize");
		bindCombo(editor.getyPixelSizeUnits(), "yPixelSizeUnits");
		bindText(editor.getApproxCentreOfRotation(), "approxCentreOfRotation");
	}

	private void bindCombo(Combo combo, String property) {
		ctx.bindValue(WidgetProperties.selection().observe(combo),
				PojoProperties.value(property).observe(model));
	}

	private void bindButton(Button button, String property) {
		ctx.bindValue(WidgetProperties.selection().observe(button),
				PojoProperties.value(property).observe(model));
	}

	private void bindText(Text text, String property) {
		ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				PojoProperties.value(property).observe(model));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Run",
				false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		final IStashing stash = ServiceHolder.getStashingService().createStash(STASH_NAME);
		try {
			stash.stash(model);
		} catch (Exception e) {
			logger.error("Error saving parameters", e);
		}

		final String command = "tomographyScan.parameters_from_json('" + stash.getFile().getAbsolutePath() + "')";
		final String jobLabel = "TomoScan Scan: " + model.getTitle();

		try {
			CommandQueueViewFactory.getQueue().addToTail(new JythonCommandCommandProvider(command, jobLabel, stash.getFile().getAbsolutePath()));
			CommandQueueViewFactory.showView();
		} catch (Exception e) {
			logger.error("Error submitting tomoscan to queue", e);
		}

		super.okPressed();
	}

	private TomoScanParameters getModel() {
		final IStashing stash = ServiceHolder.getStashingService().createStash(STASH_NAME);
		if (stash.isStashed()) {
			try {
				return stash.unstash(TomoScanParameters.class);
			} catch (Exception e) {
				logger.warn("Cannot retrieve saved parameters; using defaults", e);
			}
		}
		return new TomoScanParameters();
	}

}
