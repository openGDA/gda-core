/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.views.dashboard;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.rcp.GDAClientActivator;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.rcp.dashboard.configuration.DashboardScannables;
import uk.ac.gda.ui.modifiers.DoubleClickModifier;

public final class DashboardView extends ViewPart {

	private class TableLabelProvider extends ColumnLabelProvider {
		private int column;

		TableLabelProvider(int col) {
			this.column = col;
		}

		@Override
		public String getText(Object element) {
			final ScannableObject ob = (ScannableObject) element;
			switch (column) {
			case 0:
				return ob.getName();
			case 1:
				return ob.getOutput();
			default:
				return "";
			}
		}

		@Override
		public String getToolTipText(Object element) {
			final ScannableObject serverOb = (ScannableObject) element;
			return serverOb.getToolTip();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(DashboardView.class);

	public static final String ID = "uk.ac.gda.rcp.views.dashboardView"; //$NON-NLS-1$

	public static final String FREQUENCY_LABEL = "update_frequency";

	private TableViewer serverViewer;

	private List<ScannableObject> data;

	private Future<?> updater;

	protected int sleeptime;

	public void addServerObject(ScannableObject sso) {
		try {
			data.add(sso);
			serverViewer.refresh();
			((DoubleClickModifier) serverViewer.getCellModifier()).setEnabled(true);
			String name = sso.getName();
			if (name == null || name == "") {
				serverViewer.editElement(sso, 0);
			}
		} catch (Exception ne) {
			logger.error("Cannot add object", ne);
		}
	}

	public void addNewServerObject() {
		ScannableObject sso = new ScannableObject("", new JythonSnapshotProvider());
		data.add(sso);
		serverViewer.refresh();
		((DoubleClickModifier) serverViewer.getCellModifier()).setEnabled(true);
		serverViewer.editElement(sso, 0);
	}

	public void clearSelectedObjects() {
		final boolean ok = MessageDialog.openConfirm(getSite().getShell(), "Please confirm clear",
				"Would you like to clear all monitored objects?");
		if (!ok)
			return;
		try {
			data.clear();
			serverViewer.refresh();
		} catch (Exception ne) {
			logger.error("Cannot clear objects", ne);
		}
	}

	private CellEditor[] createCellEditors(final TableViewer tableViewer) {
		CellEditor[] editors = new CellEditor[1];
		TextCellEditor nameEd = new TextCellEditor(tableViewer.getTable());
		((Text) nameEd.getControl()).setTextLimit(60);
		// NOTE Must not add verify listener - it breaks things.
		editors[0] = nameEd;

		return editors;
	}

	private void createContentProvider() {
		serverViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return data.toArray(); // Does not happen that often and list not large
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
	}

	private ICellModifier createModifier(final TableViewer tableViewer) {
		return new DoubleClickModifier(tableViewer) {
			@Override
			public boolean canModify(Object element, String property) {
				if (!enabled)
					return false;
				return (element instanceof ScannableObject) && "Object Name".equalsIgnoreCase(property);
			}

			@Override
			public Object getValue(Object element, String property) {
				// NOTE: Only works for scannables right now which have one name
				final String name = ((ScannableObject) element).getName();
				return name != null ? name : "";
			}

			@Override
			public void modify(Object item, String property, Object value) {
				try {
					final ScannableObject ob = (ScannableObject) ((IStructuredSelection) serverViewer
							.getSelection()).getFirstElement();
					if (!ob.getName().equals(value)) {
						int idx = data.indexOf(ob);
						data.set(idx, new ScannableObject((String) value, new JythonSnapshotProvider()));
					}

				} catch (Exception e) {
					logger.error("Cannot set " + property, e);

				} finally {
					setEnabled(false);
				}
				serverViewer.refresh();
			}
		};
	}

	@Override
	public void createPartControl(Composite parent) {

		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new FillLayout());
		scrolledComposite.setContent(container);

		this.serverViewer = new TableViewer(container, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		serverViewer.getTable().setLinesVisible(true);
		serverViewer.getTable().setHeaderVisible(true);

		ColumnViewerToolTipSupport.enableFor(serverViewer, ToolTip.NO_RECREATE);

		final TableViewerColumn name = new TableViewerColumn(serverViewer, SWT.NONE);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(150);
		name.setLabelProvider(new TableLabelProvider(0));

		final TableViewerColumn value = new TableViewerColumn(serverViewer, SWT.NONE);
		value.getColumn().setText("Value");
		value.getColumn().setWidth(150);
		value.setLabelProvider(new TableLabelProvider(1));

		serverViewer.setColumnProperties(new String[] { "Object Name", "Object Value" });
		serverViewer.setCellEditors(createCellEditors(serverViewer));
		serverViewer.setCellModifier(createModifier(serverViewer));
		createContentProvider();
		serverViewer.setInput(new Object());

		getSite().setSelectionProvider(serverViewer);
		createRightClickMenu();

		GDAClientActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FREQUENCY_LABEL)) {
					updateSleepTime();
				}
			}
		});

		updateSleepTime();

		if (updater == null || updater.isDone()) {
			resetUpdateProcessing();
		}
	}

	private void updateSleepTime() {
		int delay = GDAClientActivator.getDefault().getPreferenceStore().getInt(FREQUENCY_LABEL);
		sleeptime = delay == 0 ? 2 : delay;
		resetUpdateProcessing();
	}

	/**
	 * Cancel the currently running update task and restart a new one.
	 * Lets users change the update/refresh rate,
	 */
	private void resetUpdateProcessing() {
		if (updater != null && !updater.isDone()) {
			updater.cancel(false);
		}
		updater = Async.scheduleAtFixedRate(() -> {
			if (serverViewer.getControl().isDisposed()) {
				logger.info("Dashboard disposed. Stopping dashboard update thread");
				throw new IllegalStateException("Dashboard view has been disposed");
			}
			refresh();
		}, 0, sleeptime, SECONDS);
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		serverViewer.getControl().setMenu(menuManager.createContextMenu(serverViewer.getControl()));
		getSite().registerContextMenu(menuManager, serverViewer);
	}

	public void deleteSelectedObject() {
		try {
			final ScannableObject ob = (ScannableObject) ((IStructuredSelection) serverViewer.getSelection())
					.getFirstElement();
			data.remove(ob);
			serverViewer.refresh();
		} catch (Exception ignored) {
			// Might be nothing selected.
		}
	}

	/**
	 * Called to get the default list of things to monitor.
	 */
	protected List<ScannableObject> getDefaultServerObjects() throws Exception {

		final List<ScannableObject> data = new ArrayList<ScannableObject>();

		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.client.dashboard.objects");

		for (IConfigurationElement e : config) {
			final String name = e.getAttribute("name");
			final ScannableObject ob = new ScannableObject(name, new JythonSnapshotProvider());
			ob.setToolTip(e.getAttribute("tooltip"));

			data.add(ob);
		}

		DashboardScannables dashboardScannables = Finder.find("dashboard_motors");
		if (dashboardScannables != null) {
			List<String> scannableNames = dashboardScannables.getDashboardScannableNames();
			scannableNames.stream()
						  .forEach(item -> data.add(new ScannableObject(item, new JythonSnapshotProvider())));
		} else {
			logger.info("No dashboard scannables were defined via Spring xml");
		}

		return data;
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site);

		try {
			if (memento != null) {
				this.data = new ArrayList<ScannableObject>();
				for (String name : memento.getAttributeKeys()) {
					ScannableObject so = new ScannableObject(name, new JythonSnapshotProvider());
					String toolTip = memento.getString(name);
					if (!"".equals(toolTip)) {
						// avoid empty string pop-up when mouse-hovering over dashboard
						so.setToolTip(memento.getString(name));
					}
					this.data.add(so);
				}
			} else {
				this.data = getDefaultServerObjects();
			}
		} catch (Exception ne) {
			throw new PartInitException("Could not create DashboardView", ne);
		}
	}

	/**
	 * Called to refresh all the values in the table.
	 *
	 * @param moveAmount
	 */
	public void move(final int moveAmount) {

		final int sel = serverViewer.getTable().getSelectionIndex();
		final int pos = sel + moveAmount;
		if (pos < 0 || pos > this.data.size() - 1)
			return;

		final ScannableObject o = data.remove(sel);
		data.add(pos, o);

		serverViewer.refresh();
	}

	/**
	 * Called to refresh all the values in the table.
	 */
	public void refresh() {
		for (ScannableObject sso : data) {
			try {
				sso.refresh();
			} catch (Exception e) {
				logger.error("Cannot refresh ScannableObject: {}", sso.getName(), e);
			}
		}
		Display.getDefault().asyncExec(() -> {
			if (serverViewer.getControl().isDisposed()) {
				return;
			}
			if (!serverViewer.isCellEditorActive()) {
				serverViewer.refresh();
			}
		});

	}

	/**
	 * Used when user has too many scannables and would like to reset the view.
	 */
	public void resetSelectedObjects() {
		try {
			data.clear();
			data.addAll(getDefaultServerObjects());
			serverViewer.refresh();
		} catch (Exception ne) {
			logger.error("Cannot reset objects", ne);
		}
	}

	/**
	 * Used to reset the state of the currently monitored scannables
	 * e.g. after a reset_namespace has replaced Jython scannables
	 */
	public void resetCurrentObjects() {
		for (int i = 0; i < data.size(); i++) {
			ScannableObject old = data.get(i);
			ScannableObject so = new ScannableObject(old.getName(), new JythonSnapshotProvider());
			so.setToolTip(old.getToolTip());
			data.set(i, so);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		for (ScannableObject so : data) {
			String toolTip = so.getToolTip();
			// putString(name, value) must have non-null value to store
			if (toolTip == null) toolTip = "";
			memento.putString(so.getName(), toolTip);
		}
	}

	@Override
	public void setFocus() {
		if (this.serverViewer != null)
			serverViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (updater != null) {
			updater.cancel(true);
		}
	}
}
