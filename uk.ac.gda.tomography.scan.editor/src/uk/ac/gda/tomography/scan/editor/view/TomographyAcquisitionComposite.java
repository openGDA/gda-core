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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.CameraConfigurationDialog;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.scan.editor.Activator;
import uk.ac.gda.tomography.scan.editor.StagesComposite;
import uk.ac.gda.tomography.scan.editor.TomographyAcquisitionTabsDialog;
import uk.ac.gda.tomography.scan.editor.TomographyResourceManager;
import uk.ac.gda.tomography.scan.editor.TomographyScanParameterDialog;
import uk.ac.gda.tomography.service.message.TomographyMessages;
import uk.ac.gda.tomography.service.message.TomographyMessagesUtility;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.tomography.ui.tool.TomographySWTElements;

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
	private Label camera;

	public TomographyAcquisitionComposite(final Composite parent, final TomographyParametersAcquisitionController controller) {
		super(parent, SWT.NONE, controller);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		headerElements(TomographySWTElements.createComposite(this, SWT.NONE, 3), labelStyle, textStyle);
		stageCompose(TomographySWTElements.createComposite(this, SWT.NONE, 1), labelStyle, textStyle);
	}

	private void headerElements(Composite parent, int labelStyle, int textStyle) {
		createSource(TomographySWTElements.createGroup(parent, 3, TomographyMessages.SOURCE), labelStyle, textStyle);

		configuration = TomographySWTElements.createLabel(parent, labelStyle);
		configuration.setImage(TomographySWTElements.getImage(getPluginId(), "icons/sinogram-50.png"));
		configuration.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.EDIT_CONFIGURATION_TP));
		TomographySWTElements.changeHIndent(configuration, 50);

		camera = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.CAMERA);
		camera.setImage(TomographySWTElements.getImage(getPluginId(), "icons/camera-50.png"));
		camera.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.CAMERA_TP));
		TomographySWTElements.changeHIndent(camera, 50);
	}

	private void stageCompose(Composite parent, int labelStyle, int textStyle) {
		StagesComposite stagesComposite = StagesComposite.buildModeComposite(parent, controller);
		controller.setTomographyMode(stagesComposite.getStageType().getStage());
	}

	private void createSource(Composite parent, int labelStyle, int textStyle) {
		energyIcon = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ENERGY_KEV);
		energyIcon.setImage(TomographySWTElements.getImage(getPluginId(), "icons/beam-16.png"));
		energy = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ENERGY_KEV);
		energyValue = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NOT_AVAILABLE, null,
				FontDescriptor.createFrom(TomographyResourceManager.getInstance().getTextDefaultFont()));

		shutter = TomographySWTElements.createButton(parent, SWT.CHECK, TomographyMessages.EMPTY_MESSAGE, TomographyMessages.SHUTTER_TP);
		shutterLabel = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.SHUTTER);
		shutterValue = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NOT_AVAILABLE, null,
				FontDescriptor.createFrom(TomographyResourceManager.getInstance().getTextDefaultFont()));
	}

	@Override
	protected void bindElements() {
		configuration.addListener(SWT.FOCUSED, this::getaddOrEditConfigurationListener);
		camera.addListener(SWT.FOCUSED, this::cameraListener);
	}

	private void cameraListener(Event event) {
		try {
			CameraConfigurationDialog.show(Display.getDefault(), getLiveStreamConnection());
		} catch (DeviceException e) {
			logger.error("Error handling configuration Dialog", e);
		}
	}

	private LiveStreamConnection getLiveStreamConnection() {
		return new LiveStreamConnection(getCameraConfiguration(), StreamType.EPICS_ARRAY);
	}

	private CameraConfiguration getCameraConfiguration() {
		String cameraName = LocalProperties.get(CAMERA_CONFIGURATION_BEAN);
		return Finder.getInstance().find(cameraName);
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
