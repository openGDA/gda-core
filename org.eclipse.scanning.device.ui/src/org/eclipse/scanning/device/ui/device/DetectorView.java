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

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.ui.DetectorScanUIElement;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.points.ValidateResults;
import org.eclipse.scanning.device.ui.points.ValidateResultsView;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.device.ui.util.ViewUtil;
import org.eclipse.scanning.event.ui.view.DelegatingSelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a list of available detectors to the user.
 * They may click on one and configure it.
 *
 * @author Matthew Gerring
 * @author Matthew Dickie
 *
 */
public class DetectorView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(DetectorView.class);
	public  static final String ID     = "org.eclipse.scanning.device.ui.device.detectorView";

	public static final String MEMENTO_KEY_ENABLED_DETECTOR_NAMES = "enabledDetectorNames";
	// UI
	private TableViewer viewer;
	private Image ticked;
	private Image unticked;
	private Image defaultIcon;
	private Map<String,Image> iconMap;
	private DelegatingSelectionProvider selectionProvider;
	private DetectorViewerFilter filter;
	private IRunnableDeviceService runnableDeviceService;

	private List<DetectorScanUIElement<?>> detectorUIElements;

	public DetectorView() {
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_HARDWARE, true);
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_MALCOLM, true);
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_PROCESSING, true);
		try {
			this.defaultIcon = Activator.getImageDescriptor("icons/camera-lens.png").createImage();
			this.ticked = Activator.getImageDescriptor("icons/ticked.png").createImage();
			this.unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
		} catch (Exception e) {
			logger.error("Could not create images", e);
		}
		this.iconMap = new HashMap<>(7);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		try {
			// get the available detectors
			runnableDeviceService = ServiceHolder.getEventService().createRemoteService(
					new URI(CommandConstants.getScanningBrokerUri()), IRunnableDeviceService.class);
			detectorUIElements = createDetectorItems();
		} catch (Exception e) {
			logger.error("Cannot get available detectors", e);
			detectorUIElements = Collections.emptyList();
		}

		// restore the enablement state of the detectors
		if (memento != null && !detectorUIElements.isEmpty()) {
			String detectorsJson = memento.getString(MEMENTO_KEY_ENABLED_DETECTOR_NAMES);
			try {
				@SuppressWarnings("unchecked")
				Set<String> previouslyEnabledDetectorNames =
						ServiceHolder.getMarshallerService().unmarshal(detectorsJson, Set.class);
				for (DetectorScanUIElement<?> element : detectorUIElements) {
					if (previouslyEnabledDetectorNames.contains(element.getName())) {
						element.setEnabled(true);
					}
				}
			} catch (Exception e) {
				logger.error("Could not restore previously enabled detectors", e);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class clazz) {
		if (clazz == DetectorScanUIElement.class) {
			return getEnabledItems();
		}

		return super.getAdapter(clazz);
	}

	private List<DetectorScanUIElement<?>> getEnabledItems() {
		return detectorUIElements.stream()
				.filter(DetectorScanUIElement::isEnabled)
				.collect(toList());
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(false);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns(viewer, "Name");

		viewer.setContentProvider(new ArrayContentProvider());
		filter = new DetectorViewerFilter();
		viewer.addFilter(filter);
		viewer.setInput(detectorUIElements);

		selectionProvider = new DelegatingSelectionProvider(viewer);
		getSite().setSelectionProvider(selectionProvider);

		createActions();
	}

	private List<DetectorScanUIElement<?>> createDetectorItems() {
		try {
			final List<DeviceInformation<?>> infos = new ArrayList<>(runnableDeviceService.getDeviceInformation());
			// Since infos will be iterated through in an arbitrary order (from a user perspective), return a sorted
			// list for display
			infos.sort(Comparator.comparing(DeviceInformation::getLabel));
			return infos.stream()
//				.map(DetectorScanUIElement::new) // TODO reinstate this line when Jenkins builds with Oxygen
				.map(info -> new DetectorScanUIElement<>(info)) // the Eclipse Mars compiler has a bug which makes it not compile
				.collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("Could not get detector information", e);
			return Collections.emptyList();
		}
	}

	private static final class DetectorViewerFilter extends ViewerFilter {

		private final Map<DeviceRole, Boolean> rolesToShowMap = new EnumMap<>(DeviceRole.class);

		public DetectorViewerFilter() {
			update();
		}

		public void update() {
			final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
			rolesToShowMap.put(DeviceRole.HARDWARE, prefs.getBoolean(DevicePreferenceConstants.SHOW_HARDWARE));
			rolesToShowMap.put(DeviceRole.MALCOLM, prefs.getBoolean(DevicePreferenceConstants.SHOW_MALCOLM));
			rolesToShowMap.put(DeviceRole.PROCESSING, prefs.getBoolean(DevicePreferenceConstants.SHOW_PROCESSING));
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			final DeviceRole deviceRole = ((DetectorScanUIElement<?>) element).getDeviceRole();
			return rolesToShowMap.get(deviceRole);
		}
	}

	private void createColumns(TableViewer tableViewer, String name) {

		TableViewerColumn tickedColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
		tickedColumn.getColumn().setWidth(24);
		tickedColumn.getColumn().setMoveable(false);
		tickedColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				DetectorScanUIElement<?> info = (DetectorScanUIElement<?>) element;
				return info.isEnabled() ? ticked : unticked;
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
				if (item == null) return;
				Rectangle rect = item.getBounds(0);
				if (rect.contains(pt)) {
					final DetectorScanUIElement<?> element = (DetectorScanUIElement<?>)item.getData();
					element.setEnabled(!element.isEnabled());
					tableViewer.refresh(element);
				}
			}
		};
		tableViewer.getTable().addMouseListener(mouseClick);

		TableViewerColumn iconColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
		iconColumn.getColumn().setWidth(24);
		iconColumn.getColumn().setMoveable(false);
		iconColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				DetectorScanUIElement<?> item = (DetectorScanUIElement<?>) element;
				if (item.getIconPath()==null) return defaultIcon;
				return getIcon(item.getIconPath());
			}
			@Override
			public String getText(Object element) {
				return null;
			}
		});

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		nameColumn.getColumn().setWidth(300);
		nameColumn.getColumn().setMoveable(false);
		nameColumn.getColumn().setText(name);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DetectorScanUIElement<?> item = (DetectorScanUIElement<?>) element;
				String label = item.getLabel();
				if (label==null) label = item.getName();
				if (label==null) label = item.getId();
				if (label==null) label = "Unamed Device";
				return label;
			}
		});

	}

	protected Image getIcon(String fullPath) {
		if (fullPath == null || fullPath.equals("")) return defaultIcon;

		try {
			if (iconMap.containsKey(fullPath)) return iconMap.get(fullPath);
			final String[] sa = fullPath.split("/");
			final Bundle bundle = Platform.getBundle(sa[0]);
			if (bundle==null) return defaultIcon;
			Image image = new Image(null, bundle.getResource(sa[1]+"/"+sa[2]).openStream());
			iconMap.put(fullPath, image);
			return iconMap.get(fullPath);
		} catch (Exception e) {
			logger.debug("Cannot get icon for "+fullPath, e);
			return defaultIcon;
		}
	}

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {

		IMenuManager popup = new MenuManager();
		List<IContributionManager> mans = Arrays.asList(getViewSite().getActionBars().getToolBarManager(), getViewSite().getActionBars().getMenuManager(), popup);

		IAction showHardware = createPreferenceAction("Show Devices", DevicePreferenceConstants.SHOW_HARDWARE, "icons/camera-lens.png");
		IAction showMalcolm = createPreferenceAction("Show Malcolm Devices", DevicePreferenceConstants.SHOW_MALCOLM, "icons/alarm-clock-select.png");
		IAction showProcessing = createPreferenceAction("Show Processing", DevicePreferenceConstants.SHOW_PROCESSING, "icons/processing.png");
		ViewUtil.addGroups("visibility", mans, showHardware, showMalcolm, showProcessing);

		IAction refresh = new Action("Refresh", Activator.getImageDescriptor("icons/recycle.png")) {
			@Override
			public void run() {
				refresh();
			}
		};

		ViewUtil.addGroups("refresh", mans, refresh);

		IAction configure = new Action("Configure", Activator.getImageDescriptor("icons/configure.png")) {
			@Override
			public void run() {
				configure();
			}
		};
		ViewUtil.addGroups("configure", mans, configure);
	}

	private IAction createPreferenceAction(String label, String preference, String icon) {
		IAction ret = new Action(label, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(preference, isChecked());
				filter.update();
				viewer.refresh();
			}
		};
		ret.setImageDescriptor(Activator.getImageDescriptor(icon));
		ret.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(preference));
		return ret;
	}


	protected void refresh() {
		boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm Refresh",
						"This action will go to the devices and re-read their models.\n"+
						"It will mean that if you have made local edits, they could be lost.\n\n"+
						"Are you sure you want continue?\n\n"+
						"(If not the 'Configure' action can be used to send your local edits to a device.)");
		if (ok) {
			viewer.refresh();
		}
	}

	protected <M extends IDetectorModel> void configure() {
		final DetectorScanUIElement<M> element = getSelection();
		if (element==null) return; // Nothing to configure
		final String detectorName = element.getName();

		boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm Configure",
				"Are you sure you want to configure '"+detectorName+"' now?\n\n"+
				"If the device is active or being used this will change its behaviour.");
		if (!ok) return;

		try {
			final IRunnableDevice<M> device = runnableDeviceService.getRunnableDevice(detectorName);
			final M model = element.getModel();

			final String label = runnableDeviceService.getDeviceInformation(model.getName()).getLabel();
			final Dialog editModelDialog = new EditDetectorModelDialog(getViewSite().getShell(),
					runnableDeviceService, model, label);
			editModelDialog.open();

			// Pass null to 'clear' the validation results view
			selectionProvider.fireSelection(new StructuredSelection(new ValidateResults(detectorName, null)));
			Object validateReturn = device.validateWithReturn(model);
			ValidateResults validateResults = new ValidateResults(detectorName, validateReturn);
			showValidationResultsView(validateResults);
			selectionProvider.fireSelection(new StructuredSelection(validateResults));
			device.configure(model);
		} catch (ScanningException|ValidationException ne) {
			ErrorDialog.openError(getViewSite().getShell(), "Configure Failed", "The configure of '"+detectorName+"' failed",
					new Status(IStatus.ERROR, "org.eclipse.scanning.device.ui", ne.getMessage(), ne));
			logger.error("Cannot configure '"+detectorName+"'", ne);
		}
	}

	private void showValidationResultsView(ValidateResults validateResults) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			try {
				if (PageUtil.getPage().findView(ValidateResultsView.ID) == null) {
					IViewPart viewPart = PageUtil.getPage().showView(ValidateResultsView.ID);
					if (viewPart instanceof ValidateResultsView) {
						ValidateResultsView validateResultsView = (ValidateResultsView)viewPart;
						validateResultsView.update(validateResults);
					}
				} else {
					PageUtil.getPage().showView(ValidateResultsView.ID);
				}
			} catch (PartInitException e) {
				logger.warn("Unable to show validate results view " + e);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <M> DetectorScanUIElement<M> getSelection() {
		if (viewer.getSelection() == null || viewer.getSelection().isEmpty()) return null;
		return (DetectorScanUIElement<M>) ((IStructuredSelection)viewer.getSelection()).getFirstElement();
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		final Set<String> enabledDetectorNames = getEnabledItems().stream()
				.map(DetectorScanUIElement::getName)
				.collect(Collectors.toSet());
		try {
			final String detectorsJson = ServiceHolder.getMarshallerService().marshal(enabledDetectorNames);
			memento.putString(MEMENTO_KEY_ENABLED_DETECTOR_NAMES, detectorsJson);
		} catch (Exception e) {
			logger.error("Could not save selected detectors", e);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (ticked!=null)   ticked.dispose();
		if (unticked!=null) unticked.dispose();
		if (defaultIcon!=null) defaultIcon.dispose();
		for (String path : iconMap.keySet()) iconMap.get(path).dispose();
		iconMap.clear();
	}
}
