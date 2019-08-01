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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.model.ActionLog;
import uk.ac.gda.tomography.model.TomographyAcquisition;
import uk.ac.gda.tomography.model.TomographyConfiguration;
import uk.ac.gda.tomography.scan.editor.TomographyAcquisitionController;
import uk.ac.gda.tomography.scan.editor.TomographyAcquisitionTabsDialog;
import uk.ac.gda.tomography.scan.editor.TomographyBindingElements;
import uk.ac.gda.tomography.scan.editor.TomographyResourceManager;
import uk.ac.gda.tomography.scan.editor.TomographySWTElements;
import uk.ac.gda.tomography.scan.editor.TomographyScanParameterDialog;
import uk.ac.gda.tomography.scan.editor.TomographySelectionDialog;

/**
 * Allows editing of TomographyAcquisition objects.
 *
 * @author Maurizio Nagni
 */
public class TomographyAcquisitionComposite extends CompositeTemplate<TomographyAcquisition> {

	private static final Logger logger = LoggerFactory.getLogger(TomographyAcquisitionComposite.class);

	// Acquisition UI
	private Text name;
	private Text configuration;
	private Label createConfiguration;
	private Label editConfiguration;
	private Label changeConfiguration;
	private Text script;
	private Label changeScript;
	private ItemsViewer<ActionLog> acquistionLogs;

	private Label saveAcquisition;
	private Label deleteAcquisition;
	private Label undoAcquisition;
	private Label runAcquisition;

	private final TomographyAcquisitionController controller;

	public TomographyAcquisitionComposite(final Composite parent, final TomographyAcquisitionController controller) {
		super(parent, SWT.NONE, controller.getAcquisition());
		this.controller = controller;
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		Composite conf = TomographySWTElements.createComposite(this, labelStyle, 5);
		TomographySWTElements.createLabel(conf, SWT.NONE, TomographyMessages.ACQUISITION, null,
				FontDescriptor.createFrom(TomographyResourceManager.getDefaultFont(), 14, SWT.BOLD));

		saveAcquisition = TomographySWTElements.createLabel(conf, labelStyle);
		saveAcquisition.setImage(TomographySWTElements.getImage(getPluginId(), "icons/disk-16.png"));
		TomographySWTElements.setTooltip(saveAcquisition, TomographyMessages.SAVE_ACQUISITION_TP);
		TomographySWTElements.changeVAlignement(saveAcquisition, SWT.CENTER);
		TomographySWTElements.changeHIndent(saveAcquisition, 50);

		deleteAcquisition = TomographySWTElements.createLabel(conf, labelStyle);
		deleteAcquisition.setImage(TomographySWTElements.getImage(getPluginId(), "icons/cross.png"));
		TomographySWTElements.setTooltip(deleteAcquisition, TomographyMessages.DELETE_ACQUISITION_TP);

		undoAcquisition = TomographySWTElements.createLabel(conf, labelStyle);
		undoAcquisition.setImage(TomographySWTElements.getImage(getPluginId(), "icons/undo-16.png"));
		TomographySWTElements.setTooltip(undoAcquisition, TomographyMessages.UNDO_ACQUISITION_TP);

		runAcquisition = TomographySWTElements.createLabel(conf, labelStyle);
		runAcquisition.setImage(TomographySWTElements.getImage(getPluginId(), "icons/run-16.png"));
		TomographySWTElements.setTooltip(runAcquisition, TomographyMessages.RUN_ACQUISITION_TP);
		TomographySWTElements.changeHIndent(runAcquisition, 50);

		createAcquisitionContent(this, labelStyle, textStyle);
	}

	private void createAcquisitionContent(Composite parent, int labelStyle, int textStyle) {

		// --- Name field ---//
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NAME);
		name = TomographySWTElements.createText(parent, textStyle, null, null, TomographyMessages.ACQUISITION_NAME_TP, new Point(500, SWT.DEFAULT));

		// --- Configuration field ---//
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.CONFIGURATIONS);
		Composite conf = TomographySWTElements.createComposite(parent, labelStyle, 5);
		configuration = TomographySWTElements.createText(conf, textStyle, null, null, TomographyMessages.EMPTY_MESSAGE);
		configuration.setEnabled(false);

		createConfiguration = TomographySWTElements.createLabel(conf, labelStyle);
		createConfiguration.setImage(TomographySWTElements.getImage(getPluginId(), "icons/plus.png"));
		TomographySWTElements.setTooltip(createConfiguration, TomographyMessages.CREATE_CONFIGURATION_TP);
		TomographySWTElements.changeVAlignement(createConfiguration, SWT.CENTER);
		TomographySWTElements.changeHIndent(createConfiguration, 50);

		editConfiguration = TomographySWTElements.createLabel(conf, labelStyle);
		editConfiguration.setImage(TomographySWTElements.getImage(getPluginId(), "icons/pencil.png"));
		TomographySWTElements.setTooltip(editConfiguration, TomographyMessages.EDIT_CONFIGURATION_TP);
		TomographySWTElements.changeVAlignement(editConfiguration, SWT.CENTER);

		changeConfiguration = TomographySWTElements.createLabel(conf, labelStyle);
		changeConfiguration.setImage(TomographySWTElements.getImage(getPluginId(), "icons/change-16.png"));
		TomographySWTElements.setTooltip(changeConfiguration, TomographyMessages.CHANGE_CONFIGURATION_TP);
		TomographySWTElements.changeVAlignement(changeConfiguration, SWT.CENTER);

		// --- Script field ---//
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.SCRIPTS);
		Composite scriptComp = TomographySWTElements.createComposite(parent, labelStyle, 4);
		script = TomographySWTElements.createText(scriptComp, textStyle, null, null, TomographyMessages.EMPTY_MESSAGE, new Point(500, SWT.DEFAULT));
		script.setEnabled(false);

		changeScript = TomographySWTElements.createLabel(scriptComp, labelStyle);
		changeScript.setImage(TomographySWTElements.getImage(getPluginId(), "icons/change-16.png"));
		TomographySWTElements.setTooltip(changeScript, TomographyMessages.CHANGE_SCRIPT_TP);
		TomographySWTElements.changeVAlignement(changeScript, SWT.CENTER);

		ExpandBarBuilder customBarHelper = new ExpandBarBuilder(parent, TomographyMessages.NOTES);
		acquistionLogs = LogsViewer.createLogsViewer(customBarHelper.getInternalArea(), textStyle, new ArrayList<ActionLog>(),
				getController().getAcquistionLogsController());
		customBarHelper.buildExpBar();
	}

	@Override
	protected void bindElements() {
		bindFields();

		editConfiguration.addListener(SWT.Selection, this::getaddOrEditConfigurationListener);
		createConfiguration.addListener(SWT.Selection, this::getaddOrEditConfigurationListener);
		changeConfiguration.addListener(SWT.Selection, this::changeConfigurationListener);

		changeScript.addListener(SWT.Selection, this::changeScriptListener);

		saveAcquisition.addListener(SWT.Selection, this::saveAcquisitionListener);
		deleteAcquisition.addListener(SWT.Selection, this::deleteAcquisitionListener);
		undoAcquisition.addListener(SWT.Selection, this::undoAcquisitionListener);
		runAcquisition.addListener(SWT.Selection, this::runAcquisitionListener);
	}

	private void saveAcquisitionListener(Event event) {
		try {
			controller.saveAcquisition();
		} catch (AcquisitionControllerException e) {
			logger.error("TODO put description of error here", e);
		}
		this.dispose();
	}

	private void deleteAcquisitionListener(Event event) {
		try {
			controller.deleteAcquisition();
		} catch (AcquisitionControllerException e) {
			logger.error("TODO put description of error here", e);
		}
		this.dispose();
	}

	private void undoAcquisitionListener(Event event) {
		controller.undoAcquisitionState();
		updateComposite();
	}

	private void runAcquisitionListener(Event event) {
		controller.runAcquisition();
	}

	private void getaddOrEditConfigurationListener(Event event) {
		TomographyAcquisition acquisition = TomographyAcquisitionController.createNewAcquisition();
		try {
			if (event.widget.equals(editConfiguration)) {
				acquisition = new TomographyAcquisition(getTemplateData());
			}
			Dialog dialog = new TomographyAcquisitionTabsDialog(Display.getDefault().getActiveShell(), acquisition);
			dialog.open();
			if (dialog.getReturnCode() == TomographyScanParameterDialog.SAVE) {
				updateAcquisition(acquisition.getConfiguration());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("TODO put description of error here", e);
		}
	}

	private void changeConfigurationListener(Event event) {
		Map<String, TomographyConfiguration> toSelect = getConfigurations();
		SelectionDialog sd = new TomographySelectionDialog(getShell(), toSelect.keySet().toArray(new String[0]), TomographyMessages.SELECT_CONFIGURATION);
		try {
			sd.open();
			if (sd.getReturnCode() != Window.OK || Objects.isNull(sd.getResult()) || sd.getResult().length == 0) {
				return;
			}
			Arrays.stream(sd.getResult()).findFirst().map(toSelect::get).ifPresent(this::updateAcquisition);
		} catch (Exception e) {
			logger.error("TODO put description of error here", e);
		}
	}

	private void changeScriptListener(Event event) {
		Map<String, URL> toSelect = getScripts();
		SelectionDialog sd = new TomographySelectionDialog(getShell(), toSelect.keySet().toArray(new String[0]), TomographyMessages.SELECT_CONFIGURATION);
		try {
			sd.open();
			if (sd.getReturnCode() != Window.OK || Objects.isNull(sd.getResult()) || sd.getResult().length == 0) {
				return;
			}
			Arrays.stream(sd.getResult()).findFirst().map(toSelect::get).ifPresent(this::updateAcquisition);
		} catch (Exception e) {
			logger.error("TODO put description of error here", e);
		}
	}

	private void updateAcquisition(TomographyConfiguration configuration) {
		saveState();
		getTemplateData().setConfiguration(configuration);
		updateComposite();
	}

	private void updateAcquisition(URL script) {
		saveState();
		getTemplateData().setScript(script);
		updateComposite();
	}

	private void updateComposite() {
		bindFields();
		layout(true);
	}

	private void saveState() {
		controller.saveAcquisitionState(getTemplateData());
	}

	@Override
	protected void initialiseElements() {
		// if (Objects.nonNull(getTemplateData().getConfiguration())) {
		// configuration.setText(getTemplateData().getConfiguration().getName());
		// }
	}

	private void bindFields() {
		DataBindingContext dbc = new DataBindingContext();

		TomographyBindingElements.bindText(dbc, name, String.class, "name", getTemplateData());
		TomographyBindingElements.bindText(dbc, configuration, String.class, "name", getTemplateData().getConfiguration());
		if (Objects.nonNull(getTemplateData().getScript())) {
			TomographyBindingElements.bindText(dbc, script, String.class, "path", getTemplateData().getScript());
		}

	}

	private String getPluginId() {
		return "uk.ac.diamond.daq.beamline.k11";
	}

	private Map<String, TomographyConfiguration> getConfigurations() {
		Map<String, TomographyConfiguration> configurationsMap = new HashMap<>();
		IntStream.rangeClosed(0, 2).forEach(i -> {
			TomographyAcquisition acq = TomographyAcquisitionController.createNewAcquisition();
			TomographyConfiguration item = acq.getConfiguration();
			item.setName("Configuration " + Integer.toString(i));
			configurationsMap.put(item.getName(), item);
		});
		return configurationsMap;
	}

	private Map<String, URL> getScripts() {
		Map<String, URL> scriptsMap = new HashMap<>();
		IntStream.rangeClosed(0, 2).forEach(i -> {
			try {
				URL item = new URL("file://somePath/myFile" + Integer.toString(i));
				scriptsMap.put(item.getPath(), item);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
		});
		return scriptsMap;
	}

	private TomographyAcquisitionController getController() {
		return this.controller;
	}
}
