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

import gda.rcp.GDAClientActivator;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.preferences.PreferenceConstants;
import uk.ac.gda.ui.modifiers.DoubleClickModifier;

/**
 * Additions: Use TreeViewer to display the scannables in their scannable groups. An action to add all scannables.
 */
public final class DashboardView extends ViewPart implements ServerObjectListener {

	private static final Logger logger = LoggerFactory.getLogger(DashboardView.class);

	/**
	 *
	 */
	public static final String ID = "uk.ac.gda.exafs.ui.dashboardView"; //$NON-NLS-1$

	private TableViewer serverViewer;

	private TableViewerColumn maxColumn, minColumn, desColumn;

	/**
	 * Create contents of the view part
	 *
	 * @param parent
	 */
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

		this.minColumn = new TableViewerColumn(serverViewer, SWT.NONE);
		minColumn.getColumn().setText("Minimum");
		minColumn.getColumn().setWidth(150);
		minColumn.setLabelProvider(new TableLabelProvider(2));

		this.maxColumn = new TableViewerColumn(serverViewer, SWT.NONE);
		maxColumn.getColumn().setText("Maximum");
		maxColumn.getColumn().setWidth(150);
		maxColumn.setLabelProvider(new TableLabelProvider(3));

		this.desColumn = new TableViewerColumn(serverViewer, SWT.NONE);
		desColumn.getColumn().setText("Description");
		desColumn.getColumn().setWidth(150);
		desColumn.setLabelProvider(new TableLabelProvider(4));

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
				if (event.getProperty().equals(PreferenceConstants.DASHBOARD_FORMAT)) {
					serverViewer.refresh();
				} else if (event.getProperty().equals(PreferenceConstants.DASHBOARD_BOUNDS)) {
					updateBoundsColumns();
				} else if (event.getProperty().equals(PreferenceConstants.DASHBOARD_DESCRIPTION)) {
					updateDummyColumn();
				}
			}
		});

		updateBoundsColumns();
		updateDummyColumn();
	}

	private void updateBoundsColumns() {
		final boolean isVis = GDAClientActivator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.DASHBOARD_BOUNDS);
		if (!isVis) {
			maxColumn.getColumn().setWidth(0);
			maxColumn.getColumn().setResizable(false);
			minColumn.getColumn().setWidth(0);
			minColumn.getColumn().setResizable(false);
		} else {
			maxColumn.getColumn().setWidth(150);
			maxColumn.getColumn().setResizable(true);
			minColumn.getColumn().setWidth(150);
			minColumn.getColumn().setResizable(true);
		}
	}

	private void updateDummyColumn() {
		final boolean isVis = GDAClientActivator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.DASHBOARD_DESCRIPTION);
		if (!isVis) {
			desColumn.getColumn().setWidth(0);
			desColumn.getColumn().setResizable(false);
		} else {
			desColumn.getColumn().setWidth(150);
			desColumn.getColumn().setResizable(true);
		}
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		serverViewer.getControl().setMenu(menuManager.createContextMenu(serverViewer.getControl()));
		getSite().registerContextMenu(menuManager, serverViewer);
	}

	private CellEditor[] createCellEditors(final TableViewer tableViewer) {
		CellEditor[] editors = new CellEditor[1];
		TextCellEditor nameEd = new TextCellEditor(tableViewer.getTable());
		((Text) nameEd.getControl()).setTextLimit(60);
		// NOTE Must not add verify listener - it breaks things.
		editors[0] = nameEd;

		return editors;
	}

	private ICellModifier createModifier(final TableViewer tableViewer) {
		return new DoubleClickModifier(tableViewer) {
			@Override
			public boolean canModify(Object element, String property) {
				if (!enabled)
					return false;
				return (element instanceof SimpleScannableObject) && "Object Name".equalsIgnoreCase(property);
			}

			@Override
			public Object getValue(Object element, String property) {
				// NOTE: Only works for scannables right now which have one name
				final String name = ((SimpleScannableObject) element).getScannableName();
				return name != null ? name : "";
			}

			@Override
			public void modify(Object item, String property, Object value) {
				try {
					final SimpleScannableObject ob = (SimpleScannableObject) ((IStructuredSelection) serverViewer
							.getSelection()).getFirstElement();
					ob.setScannableName((String) value);
					ob.connect();

				} catch (Exception e) {
					logger.error("Cannot set " + property, e);

				} finally {
					setEnabled(false);
				}
				serverViewer.refresh();
			}
		};
	}

	private List<ServerObject> data;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site);

		try {
			if (memento != null)
				this.data = getDataFromXML(memento.getTextData());
			if (data == null)
				this.data = getDefaultServerObjects();
			connect();
		} catch (Exception ne) {
			throw new PartInitException(ne.getMessage());
		}
	}

	private void connect() {
		// connect to objects in a separate thread
		Job job = new Job("Connecting dashboard to objects...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (ServerObject serverObject : data) {
					try {
						serverObject.connect();
					} catch (Exception e) {
						logger.debug("Dashboard view error while trying to connect", e);
					} finally {
						serverObject.addServerObjectListener(DashboardView.this);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}

	private void disconnect() {
		for (ServerObject serverObject : data)
			serverObject.disconnect();
		for (ServerObject o : data) {
			o.removeServerObjectListener(this);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		try {
			memento.putTextData(getXMLFromData(data));
		} catch (Exception e) {
			logger.error("Cannot save plot bean", e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<ServerObject> getDataFromXML(String textData) throws UnsupportedEncodingException {

		if (textData == null)
			return null;
		final ByteArrayInputStream stream = new ByteArrayInputStream(textData.getBytes("UTF-8"));
		XMLDecoder d = new XMLDecoder(new BufferedInputStream(stream));
		final List<ServerObject> data = (List<ServerObject>) d.readObject();
		d.close();
		return data;
	}

	private String getXMLFromData(final List<ServerObject> data) throws Exception {

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLEncoder e = new XMLEncoder(new BufferedOutputStream(stream));
		e.writeObject(data);
		e.close();

		return stream.toString("UTF-8");
	}

	@Override
	public void serverObjectChangePerformed(ServerObjectEvent evt) {
		if (serverViewer.getTable().isDisposed())
			return;// Important can be called from timer thread.
		if (serverViewer.isCellEditorActive())
			return;
		final ServerObject ob = (ServerObject) evt.getSource();
		serverViewer.update(ob, null);
	}

	/**
	 * Add an object to listen to.
	 *
	 * @param toAdd
	 */
	public void addServerObject(final ServerObject toAdd) {
		try {
			toAdd.connect();
			data.add(toAdd);
			toAdd.addServerObjectListener(this);
			serverViewer.refresh();
			((DoubleClickModifier) serverViewer.getCellModifier()).setEnabled(true);
			if (toAdd instanceof SimpleScannableObject) {
				// Do not edit the name in the Dashboard if one is provided
				SimpleScannableObject scannable = (SimpleScannableObject) toAdd;
				String name = scannable.getScannableName();
				if (name != null && name != "") {
					return;
				}
			}
			// Start editing if no name is provided (case if the "Add Scannable" button is pressed)
			serverViewer.editElement(toAdd, 0);

		} catch (Exception ne) {
			logger.error("Cannot add object", ne);
		}
	}

	/**
	 *
	 */
	public void deleteSelectedObject() {
		try {
			final ServerObject ob = (ServerObject) ((IStructuredSelection) serverViewer.getSelection())
					.getFirstElement();
			data.remove(ob); // NOTE the equals method of ServerObject simply looks at the label.
			ob.disconnect();
			serverViewer.refresh();
		} catch (Exception ignored) {
			// Might be nothing selected.
		}
	}

	/**
	 * Used when user has too many scannables and would like to reset the view.
	 */
	public void resetSelectedObjects() {
		try {
			disconnect();
			data.clear();
			data.addAll(getDefaultServerObjects());
			connect();
			serverViewer.refresh();
		} catch (Exception ne) {
			logger.error("Cannot reset objects", ne);
		}
	}

	/**
	 * Called to refresh all the values in the table.
	 */
	public void refresh() {
		try {
			disconnect();
			ServerObject.cancelTimer();
			connect();
			serverViewer.refresh();
		} catch (Exception ne) {
			logger.error("Cannot refresh objects", ne);
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

		final ServerObject o = data.remove(sel);
		data.add(pos, o);

		serverViewer.refresh();
	}

	/**
	 *
	 */
	public void clearSelectedObjects() {
		final boolean ok = MessageDialog.openConfirm(getSite().getShell(), "Please confirm clear",
				"Would you like to clear all monitored objects?");
		if (!ok)
			return;
		try {
			disconnect();
			data.clear();
			serverViewer.refresh();
		} catch (Exception ne) {
			logger.error("Cannot clear objects", ne);
		}
	}

	private class TableLabelProvider extends ColumnLabelProvider {
		private int column;

		TableLabelProvider(int col) {
			this.column = col;
		}

		@Override
		public String getText(Object element) {
			final ServerObject ob = (ServerObject) element;
			switch (column) {
			case 0:
				return ob.getLabel();
			case 1:
				return formatValue(ob.getValue(), ob.getUnit());
			case 2:
				return formatValue(ob.getMinimum(), ob.getUnit());
			case 3:
				return formatValue(ob.getMaximum(), ob.getUnit());
			case 4:
				return ob.getDescription();
			default:
				return "";
			}
		}

		private String formatValue(final Object valueOriginal, final String unit) {
			Object value = valueOriginal;
			if (value != null) {
				try {
					final double dblValue = value instanceof Double ? (Double) value : Double.parseDouble(value
							.toString());
					final String formatString = GDAClientActivator.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.DASHBOARD_FORMAT);
					DecimalFormat format = new DecimalFormat(formatString);
					value = format.format(dblValue);
				} catch (Exception ignored) {
					value = valueOriginal;
				}
			}
			if (value != null && unit != null && !unit.equals("None")){
				return value + " " + unit;
			}
			if (value != null){
				return value + "";
			}
			return "";
		}

		@Override
		public String getToolTipText(Object element) {
			final ServerObject serverOb = (ServerObject) element;
			if (serverOb.isError())
				return "Cannot locate scannable '" + serverOb.getLabel() + "'.";
			if (column == 4) {
				return serverOb.getClassName();
			}
			return serverOb.getTooltip();
		}
	}

	private void createContentProvider() {
		serverViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return data.toArray(); // Does not happen that often and list not large
			}
		});
	}

	@Override
	public void setFocus() {
		if (this.serverViewer != null)
			serverViewer.getControl().setFocus();
	}

	/**
	 * Called to get the default list of things to monitor.
	 */
	protected List<ServerObject> getDefaultServerObjects() throws Exception {

		final List<ServerObject> data = new ArrayList<ServerObject>(5);

		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.gda.client.dashboard.objects");

		for (IConfigurationElement e : config) {
			final String name = e.getAttribute("name");
			final SimpleScannableObject ob = new SimpleScannableObject(name);
			ob.setTooltip(e.getAttribute("tooltip"));

			data.add(ob);
		}

		return data;
	}

}