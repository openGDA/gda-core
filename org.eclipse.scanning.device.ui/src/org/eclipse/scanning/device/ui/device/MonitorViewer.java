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

import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.api.stashing.IStashing;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.util.SortNatural;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A viewer for a list of available scan monitors that allows them to be
 * enabled and disabled for inclusion in the next scan, as well as to
 * select their scan role between per point and per scan (see {@link MonitorScanRole}).
 *
 * @author Matthew Gerring
 * @author Matthew Dickie
 *
 */
public class MonitorViewer {

	private static final String STASH_ID_MONITORS_STATE = "org.eclipse.scanning.device.ui.scan.monitors.json";

	private static final String STASH_ID_SHOW_ENABLED_ONLY = "org.eclipse.scanning.device.ui.scan.monitors.showEnabledOnly";

	private static final Logger logger = LoggerFactory.getLogger(MonitorViewer.class);

	private TableViewer viewer;

	private IScannableDeviceService scannableDeviceService;

	private Image enabledIcon;
	private Image disabledIcon;

	public MonitorViewer() {
		IEventService eservice = ServiceHolder.getEventService();
		try {
			this.scannableDeviceService = eservice.createRemoteService(new URI(CommandConstants.getScanningBrokerUri()),
					IScannableDeviceService.class);
			this.enabledIcon = Activator.getImageDescriptor("icons/ticked.png").createImage();
			this.disabledIcon = Activator.getImageDescriptor("icons/unticked.gif").createImage();
		} catch (EventException | URISyntaxException e) {
			logger.error("Problem getting remote " + IScannableDeviceService.class.getSimpleName(), e);
		}
	}

	/**
	 * Create the contents of the viewer.
	 *
	 * @param parent
	 */
	public void createControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns(viewer);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(createMonitorItems());

		restoreState();
	}

	private List<MonitorScanUIElement> createMonitorItems() {
		try {
			List<String> monitorNames = new ArrayList<>(scannableDeviceService.getScannableNames());
			monitorNames.sort(new SortNatural<>(false));

			return monitorNames.stream()
				.map(MonitorScanUIElement::new)
				.collect(Collectors.toList());
		} catch (ScanningException e) {
			logger.error("Could not get available monitors", e);
			return Collections.emptyList();
		}
	}

	public void saveState() {
		try {
			final IStashing monitorsStash = ServiceHolder.getStashingService().createStash(STASH_ID_MONITORS_STATE);
			@SuppressWarnings("unchecked")
			final List<MonitorScanUIElement> viewItems = (List<MonitorScanUIElement>) viewer.getInput();
			monitorsStash.stash(viewItems);
		} catch (Exception e) {
			logger.error("Could not save monitors setup", e);
		}

		try {
			final IStashing showEnabledOnlyStash = ServiceHolder.getStashingService().createStash(STASH_ID_SHOW_ENABLED_ONLY);
			showEnabledOnlyStash.stash(isShowEnabledOnly());
		} catch (Exception e) {
			logger.error("Could not save show enabled only filter state", e);
		}
	}

	public void restoreState() {
		try {
			final IStashing monitorsStash = ServiceHolder.getStashingService().createStash(STASH_ID_MONITORS_STATE);
			@SuppressWarnings("unchecked")
			final List<MonitorScanUIElement> oldMonitorItems = monitorsStash.unstash(List.class);
			if (oldMonitorItems != null) {
				final Map<String, MonitorScanUIElement> oldItemMap = oldMonitorItems.stream().collect(toMap(MonitorScanUIElement::getName, Function.identity()));
				@SuppressWarnings("unchecked")
				final List<MonitorScanUIElement> newItems = (List<MonitorScanUIElement>) viewer.getInput();
				for (MonitorScanUIElement newItem : newItems) {
					final MonitorScanUIElement oldItem = oldItemMap.get(newItem.getName());
					if (oldItem != null) {
						newItem.setEnabled(oldItem.isEnabled());
						newItem.setMonitorScanRole(oldItem.getMonitorScanRole());
					}
				}
				viewer.refresh();
			}
		} catch (Exception e) {
			logger.error("Could not restore previous monitors state", e);
		}

		try {
			final IStashing showEnabledOnlyStash = ServiceHolder.getStashingService().createStash(STASH_ID_SHOW_ENABLED_ONLY);
			final Boolean showEnabledOnly = showEnabledOnlyStash.unstash(Boolean.class); // can be null
			setShowEnabledOnly(showEnabledOnly == Boolean.TRUE);
		} catch (Exception e) {
			logger.error("Could not restore show enabled only filter state", e);
		}
	}

	private void createColumns(TableViewer tableViewer) {
		// a column wih a tickbox to indicate if the monitor is enabled or disabled
		TableViewerColumn enablementColumn = new TableViewerColumn(tableViewer, SWT.CENTER, 0);
		enablementColumn.getColumn().setWidth(24);
		enablementColumn.getColumn().setMoveable(false);
		enablementColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public Image getImage(Object element) {
				MonitorScanUIElement wrapper = (MonitorScanUIElement) element;
				return wrapper.isEnabled() ? enabledIcon : disabledIcon;
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});

		MouseAdapter mouseClick = new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
				TableItem item = viewer.getTable().getItem(pt);
				if (item == null)
					return;
				Rectangle rect = item.getBounds(0);
				if (rect.contains(pt)) {
					final MonitorScanUIElement wrapper = (MonitorScanUIElement) item.getData();
					wrapper.setEnabled(!wrapper.isEnabled());
					tableViewer.refresh(wrapper);
				}
			}
		};
		tableViewer.getTable().addMouseListener(mouseClick);

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.LEFT, 1);
		nameColumn.getColumn().setWidth(100);
		nameColumn.getColumn().setMoveable(false);
		nameColumn.getColumn().setText("Name");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MonitorScanUIElement) element).getName();

			}
		});

		TableViewerColumn monitorScanRoleColumn = new TableViewerColumn(tableViewer, SWT.LEFT, 2);
		monitorScanRoleColumn.getColumn().setWidth(100);
		monitorScanRoleColumn.getColumn().setMoveable(false);
		monitorScanRoleColumn.getColumn().setText("Type");
		monitorScanRoleColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MonitorScanUIElement wrapper = (MonitorScanUIElement) element;
				return wrapper.getMonitorScanRole().getLabel();
			}
		});
		monitorScanRoleColumn.setEditingSupport(new MonitorScanRoleEditingSupport(tableViewer));
	}

	public MonitorScanUIElement getSelection() {
		final ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			return (MonitorScanUIElement) ssel.getFirstElement();
		}
		return null;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void dispose() {
		if (enabledIcon != null)
			enabledIcon.dispose();
		if (disabledIcon != null)
			disabledIcon.dispose();
	}

	public ISelectionProvider getSelectionProvider() {
		return viewer;
	}

	public Control getControl() {
		return viewer.getControl();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	private final ViewerFilter enabledOnlyViewerFilter = new ViewerFilter() {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return ((MonitorScanUIElement) element).isEnabled();
		}
	};

	public boolean isShowEnabledOnly() {
		// we assume this is the only filter
		return viewer.getFilters().length > 0;
	}

	public void setShowEnabledOnly(boolean showEnabledOnly) {
		if (showEnabledOnly != isShowEnabledOnly()) {
			if (showEnabledOnly) viewer.addFilter(enabledOnlyViewerFilter);
			else viewer.removeFilter(enabledOnlyViewerFilter);
			viewer.refresh();
		}
	}

	public Map<String, MonitorScanRole> getEnabledMonitors() {
		@SuppressWarnings("unchecked")
		List<MonitorScanUIElement> viewItems = (List<MonitorScanUIElement>) viewer.getInput();
		return viewItems.stream()
				.filter(MonitorScanUIElement::isEnabled)
				.collect(toMap(MonitorScanUIElement::getName, MonitorScanUIElement::getMonitorScanRole));
	}

	public List<MonitorScanUIElement> getEnabledMonitorItems() {
		@SuppressWarnings("unchecked")
		List<MonitorScanUIElement> viewItems = (List<MonitorScanUIElement>) viewer.getInput();
		return viewItems.stream()
				.filter(MonitorScanUIElement::isEnabled)
				.collect(Collectors.toList());
	}

}
