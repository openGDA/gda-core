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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.util.ViewUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A view for adding and removing monitors. The monitors produced are
 * provided by the adaptable pattern as a list of strings. This list of
 * strings will be added to the ScanRequest as monitor names when the
 * scan is generated.
 *
 * @author Matthew Gerring
 * @author Matthew Dickie
 */
public class MonitorView extends ViewPart {

	public static final String ID = "org.eclipse.scanning.device.ui.device.MonitorView"; //$NON-NLS-1$

	public static final String SHOW_ENABLED_MONITORS_ONLY = "showEnabledMonitorsOnly";

	private static final Logger logger = LoggerFactory.getLogger(MonitorView.class);

	private MonitorViewer viewer;

	// TODO: convert this view to be an e4 view

	public MonitorView() {
		this.viewer = new MonitorViewer();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer.createControl(parent); // note: viewer automatically restores its previous state
		getSite().setSelectionProvider(viewer.getSelectionProvider());
		createActions(getViewSite().getActionBars().getToolBarManager(), getViewSite().getActionBars().getMenuManager());
	}

	/**
	 * Create the actions.
	 */
	private void createActions(IContributionManager... managers) {
		List<IContributionManager> mans = new ArrayList<>(Arrays.asList(managers));
		MenuManager rightClick = new MenuManager();
		mans.add(rightClick);

		final IAction toggleShowEnabledOnlyAction = new Action("Show enabled only", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				try {
					viewer.setShowEnabledOnly(!viewer.isShowEnabledOnly());
				} catch (Exception e) {
					logger.error("Cannot refresh scannable viewer!", e);
				}
			}
		};

		toggleShowEnabledOnlyAction.setImageDescriptor(Activator.getImageDescriptor("icons/funnel--minus.png"));
		ViewUtil.addGroups("view", mans, toggleShowEnabledOnlyAction);

		viewer.getControl().setMenu(rightClick.createContextMenu(viewer.getControl()));
		toggleShowEnabledOnlyAction.setChecked(viewer.isShowEnabledOnly());
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		viewer.saveState();
	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}

	@Override
	public void dispose() {
		viewer.dispose();
	}

	public Map<String, MonitorScanRole> getEnabledMonitors() {
		return viewer.getEnabledMonitors();
	}

	@Override
	public Object getAdapter(Class clazz) {
		if (clazz == MonitorScanUIElement.class || clazz == MonitorScanUIElement[].class) {
			return viewer.getEnabledMonitorItems();
		}
		return super.getAdapter(clazz);
	}

}
