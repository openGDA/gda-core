/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.device.ui.device;


import java.net.URI;

import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.device.scannable.ControlTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(ControlView.class);

	public static final String ID = "org.eclipse.scanning.device.ui.device.ControlView"; //$NON-NLS-1$

	private static final String MEMENTO_KEY_CONTROL_TREE = "controlTree";

	// UI
	private ControlTreeViewer viewer;

	private ControlTree initialControlTree;

	public ControlView() {
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, true);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		if (memento != null) {
			final String controlTreeJson = memento.getString(MEMENTO_KEY_CONTROL_TREE);
			if (controlTreeJson != null) {
				try {
					initialControlTree = ServiceHolder.getMarshallerService().unmarshal(controlTreeJson, ControlTree.class);
				} catch (Exception e) {
					logger.error("Could not retreive control tree", e);
				}
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
	super.saveState(memento);
		try {
			final String controlTreeJson = ServiceHolder.getMarshallerService().marshal(viewer.getControlTree());
			memento.putString(MEMENTO_KEY_CONTROL_TREE, controlTreeJson);
		} catch (Exception e) {
			logger.error("Problem stashing control tree!", e);
		}
	}
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		try {
			IScannableDeviceService cservice = ServiceHolder.getEventService().createRemoteService(new URI(CommandConstants.getScanningBrokerUri()), IScannableDeviceService.class);

			ControlTree defaultTree = ControlTreeUtils.parseDefaultXML();
			if (defaultTree==null) {
				defaultTree = new ControlTree();
				defaultTree.globalize();
			}
			viewer = new ControlTreeViewer(defaultTree, cservice); // Widget linked to hardware, use ControlViewerMode.INDIRECT_NO_SET_VALUE to edit without setting hardware.

			if (initialControlTree != null) initialControlTree.build();
			viewer.createPartControl(parent, initialControlTree, getViewSite().getActionBars().getMenuManager(),
					getViewSite().getActionBars().getToolBarManager());
			getSite().setSelectionProvider(viewer.getSelectionProvider());
		} catch (Exception e) {
			logger.error("Cannot build ControlTreeViewer!", e);
		}

	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer.dispose();
	}

}
