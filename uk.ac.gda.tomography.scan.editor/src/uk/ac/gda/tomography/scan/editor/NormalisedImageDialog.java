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
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Queue;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.tomography.scan.NormalisedImageParameters;

public class NormalisedImageDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(NormalisedImageDialog.class);
	private static final String DIALOG_SETTINGS_KEY_NORMALIZED_IMAGE_MODEL = "normalizedImageModel";

	private NormalisedImageParameters model;

	public NormalisedImageDialog(Shell shell) {
		super(shell);
		model = getModel();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Get Normalised Image");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cmp = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(cmp);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(cmp);

		// Prepare layouts for labels and text widgets
		GridDataFactory gdLabel = GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory gdText = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).minSize(50, SWT.DEFAULT);

		Label lblOutOfBeam = new Label(cmp, SWT.NONE);
		lblOutOfBeam.setText("Out of beam position (mm)");
		gdLabel.applyTo(lblOutOfBeam);

		Text outBeamX = new Text(cmp, SWT.BORDER);
		gdText.applyTo(outBeamX);

		Label lblExposure = new Label(cmp, SWT.NONE);
		lblExposure.setText("Exposure time (s)");
		gdLabel.applyTo(lblExposure);

		Text exposureTime = new Text(cmp, SWT.BORDER);
		gdText.applyTo(exposureTime);

		// Bind the text widgets to the appropriate fields in the TomoScanParameters model
		DataBindingContext ctx = new DataBindingContext();
		ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(outBeamX),
				PojoProperties.value("outOfBeamPosition").observe(model));

		ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(exposureTime),
				PojoProperties.value("exposureTime").observe(model));

		return cmp;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Run", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {

		saveModel();

		final String cmd = String.format("tomographyScan.showNormalisedImage(%s,exposureTime=%s)", model.getOutOfBeamPosition(), model.getExposureTime());
		try {
			Queue queue = CommandQueueViewFactory.getQueue();
			if (queue != null) {
				queue.addToTail(new JythonCommandCommandProvider(cmd, "Running command '" + cmd + "'", null));
				CommandQueueViewFactory.showView();
			} else {
				throw new Exception("Queue not found");
			}
		} catch (Exception e1) {
			logger.error("Error showing normalised image", e1);
		}
		super.okPressed();
	}

	private NormalisedImageParameters getModel() {
		final IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		String modelJson = dialogSettings.get(DIALOG_SETTINGS_KEY_NORMALIZED_IMAGE_MODEL);
		if (modelJson != null) {
			try {
				return ServiceProvider.getService(IMarshallerService.class).unmarshal(modelJson, NormalisedImageParameters.class);
			} catch (Exception e) {
				logger.warn("Cannot retrieve saved parameters; using defaults", e);
			}
		}
		return new NormalisedImageParameters();
	}

	private void saveModel() {
		final IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		try {
			String modelJson = ServiceProvider.getService(IMarshallerService.class).marshal(model);
			dialogSettings.put(DIALOG_SETTINGS_KEY_NORMALIZED_IMAGE_MODEL, modelJson);
		} catch (Exception e) {
			logger.error("Error saving parameters", e);
		}
	}
}
