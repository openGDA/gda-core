/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.widgets;

import static org.eclipse.scanning.device.ui.device.scannable.ControlViewerMode.INDIRECT_NO_SET_VALUE;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.device.scannable.ControlTreeViewer;
import org.eclipse.scanning.device.ui.util.ViewUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.GDAClientActivator;

/**
 * A widget to edit a beamline configuration, that is, a state defined in terms of relevant scannables and their positions
 */
public class BeamlineConfigurationControls {

	private static final String SCANNABLES_GROUP_NAME = "Scannables";
	private static final String[] FILE_FILTER_NAMES = new String[] { "Beamline configurations", "All Files (*.*)" };
	private static final String[] FILE_FILTER_EXTENSIONS = new String[] { "*.pos", "*.*" };

	/**
	 * Default directory where beamline configurations would be saved will be
	 * IFilePathService.getPersistenceDir() + DEFAULT_DIRECTORY
	 */
	private static final String DEFAULT_DIRECTORY = "blconfigs";

	private static final Logger logger = LoggerFactory.getLogger(BeamlineConfigurationControls.class);

	private final IScannableDeviceService scannableDeviceService;
	private Text statusMessage; // Text rather than Label so that the message can be selected and copied

	/**
	 * The initial beamline configuration,
	 */
	private final Map<String, Object> initialBeamlineConfiguration;

	private ControlTreeViewer viewer;
	private Optional<Runnable> listener = Optional.empty();

	public BeamlineConfigurationControls(IScannableDeviceService service, Map<String, Object> initialConfiguration) {
		scannableDeviceService = service;
		initialBeamlineConfiguration = initialConfiguration;
	}

	/**
	 * Add a callback invoked when the configuration changes (optional)
	 *
	 * @param listener non-null
	 */
	public void addDataChangedCallback(Runnable listener) {
		this.listener = Optional.of(listener);
	}

	public void draw(Composite parent) {
		var composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(composite);
		GridLayoutFactory.fillDefaults().applyTo(composite);

		var controlTree = createControlTree(initialBeamlineConfiguration);
		viewer = new ControlTreeViewer(scannableDeviceService, INDIRECT_NO_SET_VALUE);
		viewer.setDefaultGroupName(SCANNABLES_GROUP_NAME);
		listener.ifPresent(viewer::addDataChangedCallback);
		var toolbarManager = new ToolBarManager(SWT.FLAT);
		var toolBar = toolbarManager.createControl(composite);

		final IAction loadAction = new Action("Load a configuration", getImageDescriptor("icons/open.png")) {
			@Override
			public void run() {
				loadConfiguration();
				listener.ifPresent(Runnable::run);
			}
		};

		final IAction saveAction = new Action("Save this configuration", getImageDescriptor("icons/save.png")) {
			@Override
			public void run() {
				saveConfiguration();
			}
		};

		ViewUtil.addGroup("beamlineConfiguration", toolbarManager, loadAction, saveAction);

		toolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

		try {
			viewer.createPartControl(composite, controlTree, toolbarManager);
			toolbarManager.remove(ControlTreeViewer.ACTION_ID_ADD_GROUP);
			toolbarManager.update(true);
		} catch (Exception e) {
			logger.error("Could not create control tree viewer", e);
		}

		statusMessage = new Text(composite, SWT.READ_ONLY);
		statusMessage.setBackground(composite.getBackground());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusMessage);

		viewer.setFocus();
	}

	private void saveConfiguration() {
		String fileName = chooseFileName(SWT.SAVE);
		if (fileName == null) return;

		try {
			IMarshallerService marshaller = ServiceHolder.getMarshallerService();
			String json = marshaller.marshal(viewer.getControlTree().toPosition().getValues());
			Files.write(Paths.get(fileName), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			setStatusMessage("Saved configuration to file: " + fileName);
		} catch (Exception e) {
			String errorMessage = "Could not save the beamline configuration to file: " + fileName;
			logger.error(errorMessage, e);
			setStatusMessage(errorMessage);
		}
	}

	private void loadConfiguration() {
		String fileName = chooseFileName(SWT.OPEN);
		if (fileName == null) return;

		try {
			byte[] bytes = Files.readAllBytes(Paths.get(fileName));
			var json = new String(bytes, StandardCharsets.UTF_8);

			final IMarshallerService marshaller = ServiceHolder.getMarshallerService();
			@SuppressWarnings("unchecked")
			Map<String, Object> beamlineConf = marshaller.unmarshal(json, Map.class);
			viewer.setControlTree(createControlTree(beamlineConf));
			viewer.refresh();
			setStatusMessage("Loaded configuration from " + fileName);
		} catch (Exception e) {
			String errorMessage = "Could not load a beamline configuration from file: " + fileName;
			logger.error(errorMessage, e);
			setStatusMessage(errorMessage);
		}
	}

	private String chooseFileName(int fileDialogStyle) {
		var dialog = new FileDialog(viewer.getControl().getShell(), fileDialogStyle);
		dialog.setFilterNames(FILE_FILTER_NAMES);
		dialog.setFilterExtensions(FILE_FILTER_EXTENSIONS);
		dialog.setFilterPath(createDirectory());
		dialog.setOverwrite(true);
		return dialog.open();
	}

	private String createDirectory() {
		String persistenceDir = ServiceHolder.getFilePathService().getPersistenceDir();
		var dir = new File(persistenceDir, DEFAULT_DIRECTORY);
		dir.mkdir();
		return dir.getAbsolutePath();
	}

	private void setStatusMessage(String message) {
		statusMessage.setText(message);
	}

	/**
	 * Creates the control tree from the existing beamline configuration IPosition object.
	 * @return control tree
	 */
	private ControlTree createControlTree(Map<String, Object> configuration) {
		var controlTree = new ControlTree();

		// We need only one group of controls, so create one called "Scannables" here
		// and disable the "Add group" button in createDialogArea().
		var scannablesGroup = new ControlGroup();
		scannablesGroup.setName(SCANNABLES_GROUP_NAME);
		controlTree.add(scannablesGroup);

		final List<INamedNode> controlNodes = new ArrayList<>();
		if (configuration != null) {
			for (Entry<String, Object> scannable : configuration.entrySet()) {
				var controlNode = new ControlNode();
				controlNode.setName(scannable.getKey());
				controlNode.setValue(scannable.getValue());
				controlTree.add(controlNode);
				controlNodes.add(controlNode);
			}
		}

		scannablesGroup.setControls(controlNodes);
		controlTree.build();

		return controlTree;
	}

	private ImageDescriptor getImageDescriptor(String path) {
		return GDAClientActivator.getImageDescriptor(path);
	}

	public Map<String, Object> getBeamlineConfiguration() {
		return viewer.getControlTree().toPosition().getValues();
	}

}
