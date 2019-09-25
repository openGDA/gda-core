/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.scan.editor.Activator;
import uk.ac.gda.tomography.scan.editor.StagesComposite;
import uk.ac.gda.tomography.scan.editor.TomographyAcquisitionTabsDialog;
import uk.ac.gda.tomography.scan.editor.TomographyScanParameterDialog;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientResourceManager;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Allows editing of TomographyAcquisition objects.
 *
 * @author Maurizio Nagni
 */
public class TomographyAcquisitionComposite extends CompositeTemplate<TomographyParametersAcquisitionController> {

	private static final String CAMERA_CONFIGURATION_BEAN = "imaging.camera.name";
	private static final String DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL = "tomographyScanModel";
	private static final Logger logger = LoggerFactory.getLogger(TomographyAcquisitionComposite.class);

	private Group source;
	private Label energyIcon;
	private Label energy;
	private Label energyValue;
	private Button shutter;
	private Label shutterLabel;
	private Label shutterValue;

	private Label configuration;

	public TomographyAcquisitionComposite(final Composite parent, final TomographyParametersAcquisitionController controller) {
		super(parent, SWT.NONE, controller);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		headerElements(ClientSWTElements.createComposite(this, SWT.NONE, 3), labelStyle, textStyle);
		stageCompose(ClientSWTElements.createComposite(this, SWT.NONE, 1), labelStyle, textStyle);
	}

	private void headerElements(Composite parent, int labelStyle, int textStyle) {
		createSource(ClientSWTElements.createGroup(parent, 3, ClientMessages.SOURCE), labelStyle, textStyle);

		configuration = ClientSWTElements.createLabel(parent, labelStyle);
		configuration.setImage(ClientSWTElements.getImage(getPluginId(), "icons/sinogram-50.png"));
		configuration.setToolTipText(ClientMessagesUtility.getMessage(ClientMessages.EDIT_CONFIGURATION_TP));
		ClientSWTElements.changeHIndent(configuration, 50);
	}

	private void stageCompose(Composite parent, int labelStyle, int textStyle) {
		StagesComposite stagesComposite = StagesComposite.buildModeComposite(parent, controller);
		controller.setTomographyMode(stagesComposite.getStageType().getStage());
	}

	private void createSource(Composite parent, int labelStyle, int textStyle) {
		energyIcon = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ENERGY_KEV);
		energyIcon.setImage(ClientSWTElements.getImage(getPluginId(), "icons/beam-16.png"));
		energy = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ENERGY_KEV);
		energyValue = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NOT_AVAILABLE, null,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));

		shutter = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.EMPTY_MESSAGE, ClientMessages.SHUTTER_TP);
		shutterLabel = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.SHUTTER);
		shutterValue = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NOT_AVAILABLE, null,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));
	}

	@Override
	protected void bindElements() {
		configuration.addListener(SWT.FOCUSED, this::getaddOrEditConfigurationListener);
	}

	private void getaddOrEditConfigurationListener(Event event) {
		try {
			Dialog dialog = new TomographyAcquisitionTabsDialog(Display.getDefault().getActiveShell(), controller);
			dialog.open();
			if (dialog.getReturnCode() == TomographyScanParameterDialog.SAVE) {
				controller.saveAcquisitionAsIDialogSettings(getController().getAcquisition(), Activator.getDefault().getDialogSettings(),
						DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL);
			}
			if (dialog.getReturnCode() == TomographyScanParameterDialog.RUN) {
				controller.saveAcquisitionAsIDialogSettings(getController().getAcquisition(), Activator.getDefault().getDialogSettings(),
						DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL);
				try {
					controller.runAcquisition(getController().getAcquisition());
				} catch (AcquisitionControllerException e) {
					MessageDialog.openError(getShell(), "Run Acquisition", e.getMessage());
				}

			}
		} catch (Exception e) {
			logger.error("Error handling configuration Dialog", e);
		}
	}

	@Override
	protected void initialiseElements() {
	}

	private String getPluginId() {
		return "uk.ac.diamond.daq.beamline.k11";
	}
}
