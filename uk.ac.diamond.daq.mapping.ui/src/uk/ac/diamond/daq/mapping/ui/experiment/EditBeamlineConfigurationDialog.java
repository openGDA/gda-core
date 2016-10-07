/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static org.eclipse.scanning.device.ui.device.scannable.ControlViewerMode.INDIRECT_NO_SET_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.device.scannable.ControlTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog for editing beamline configuration.
 */
public class EditBeamlineConfigurationDialog extends Dialog {

	private static final String SCANNABLES_GROUP_NAME = "Scannables";

	private static final Logger logger = LoggerFactory.getLogger(EditBeamlineConfigurationDialog.class);

	private final IScannableDeviceService scannableDeviceService;

	/**
	 *  The initial beamline configuration, should be set by calling
	 *  {@link #setInitialBeamlineConfiguration(IPosition)} before the dialog is opened.
	 */
	private Map<String, Object> initialBeamlineConfiguration = null;

	/**
	 * The final beamline configuration as returned by #get
	 */
	private Map<String, Object> newBeamlineConfiguration = null;

	private ControlTreeViewer viewer;

	protected EditBeamlineConfigurationDialog(Shell parentShell,
			IScannableDeviceService scannableDeviceService) {
		super(parentShell);
		this.scannableDeviceService = scannableDeviceService;
	}

	public void setInitialBeamlineConfiguration(Map<String, Object> beamlineConfiguration) {
		this.initialBeamlineConfiguration = beamlineConfiguration;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Creates the control tree from the existing beamline configuration IPosition object.
	 * @return control tree
	 */
	private ControlTree createControlTree() {
		// TODO group by scannable group?
		ControlTree controlTree = new ControlTree();

		// TODO: do we have to have a parent group? why?
		ControlGroup scannablesGroup = new ControlGroup();
		scannablesGroup.setName(SCANNABLES_GROUP_NAME);
		controlTree.add(scannablesGroup);

		List<INamedNode> controlNodes = new ArrayList<>();
		if (initialBeamlineConfiguration != null) {
			for (String scannableName : initialBeamlineConfiguration.keySet()) {
				// TODO: check the scannable with the given name exists?
				ControlNode controlNode = new ControlNode();
				controlNode.setName(scannableName);
				controlNode.setValue(initialBeamlineConfiguration.get(scannableName));
				controlTree.add(controlNode);
				controlNodes.add(controlNode);
			}
		}

		scannablesGroup.setControls(controlNodes);
		controlTree.build();

		return controlTree;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit Beamline Configuration");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}

	@Override
	public Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		final ControlTree controlTree = createControlTree();
		viewer = new ControlTreeViewer(scannableDeviceService, INDIRECT_NO_SET_VALUE);
		viewer.setDefaultGroupName(SCANNABLES_GROUP_NAME);
		ToolBarManager toolbarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolBar = toolbarManager.createControl(composite);
		toolBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

		try {
			viewer.createPartControl(composite, controlTree, toolbarManager);
			toolbarManager.update(true);
		} catch (Exception e) {
			logger.error("Could not create control tree viewer", e);
		}

		return composite;
	}

	@Override
	public void okPressed() {
		newBeamlineConfiguration = viewer.getControlTree().toPosition().getValues();
		super.okPressed();
	}

	/**
	 * Returns the new, modified beamline configuration.
	 * @return new beamline configuration
	 */
	public Map<String, Object> getModifiedBeamlineConfiguration() {
		return newBeamlineConfiguration;
	}


}
