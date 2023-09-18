/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.autoprocessing.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import gda.autoprocessing.AutoProcessingBean;


public class AutoProcessingListViewer extends Composite {

	private TableViewer viewer;
	private Image ticked;
	private Image unticked;
	private List<Action> menuActions = new ArrayList<>();
	private URI uri;

	private List<Runnable> listeners = new ArrayList<>();

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}

	private void notifyListeners() {
		listeners.forEach(Runnable::run);
	}


	public AutoProcessingListViewer(Composite parent) {
		super(parent, SWT.NONE);
		createViewer();
	}

	private void createViewer() {

		ticked = Activator.getImageDescriptor("icons/ticked.png").createImage();
		unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();

		this.setLayout(new FillLayout());


		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		TableColumnLayout columnLayout = new TableColumnLayout();

		viewer = new TableViewer(tableComposite, SWT.MULTI |SWT.FULL_SELECTION | SWT.BORDER);
		viewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(viewer);

		viewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn check   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		check.setEditingSupport(new CheckBoxEditSupport(viewer));
		check.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				return ((AutoProcessingBean)element).isActive() ? ticked : unticked;
			}

		});

		columnLayout.setColumnData(check.getColumn(), new ColumnPixelData(25));

		TableViewerColumn app = new TableViewerColumn(viewer, SWT.LEFT);

		app.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((AutoProcessingBean)element).getAppName();
			}
		});

		app.getColumn().setText("App");

		columnLayout.setColumnData(app.getColumn(), new ColumnWeightData(20, 20));

		TableViewerColumn name = new TableViewerColumn(viewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getDisplayName(element);
			}

			@Override
			public String getToolTipText(Object element) {
				return getDisplayName(element);
			}
		});

		name.getColumn().setText("Config");
		columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(80, 20));

		setupDefaultMenuActions();
		setupContextMenu();

		tableComposite.setLayout(columnLayout);

		setTableSize(tableComposite);
	}

	/** Call this after creating the composite to ensure any menu actions
	 * added using {@link #addMenuAction(String, Consumer)} are shown in the context menu
	 */
	public void setupContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuActions.forEach(menuMgr::add);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getTable().setMenu(menu);
	}

	/**
	 * Setup menu actions for adding, editing, removing items from the table
	 */
	public void setupDefaultMenuActions() {
		menuActions.clear();
		addMenuAction("Add", list -> addNewItem());
		addMenuAction("Edit", list -> editCurrentItem());
		addMenuAction("Remove", list -> removeSelectedItems());
	}

	/**
	 * Add an action to be shown in the context menu.
	 * A list of the selected AutoProcessingBeans is passed to the Consumer object
	 * when the Action is run.
	 *
	 * @param name displayed in the context menu for the item
	 * @param consumer
	 */
	public void addMenuAction(String name, Consumer<List<AutoProcessingBean>> consumer) {
		var action =  new Action(name) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void run() {
				List<AutoProcessingBean> currentSelection = getCurrentSelection();
				consumer.accept(currentSelection);
			}
		};
		menuActions.add(action);
	}

	/**
	 * @return List of the currently selected AutoProcessingBeans (may be an empty list)
	 */
	private List<AutoProcessingBean> getCurrentSelection() {
		if (viewer.getSelection() instanceof StructuredSelection selection) {
			Iterator<AutoProcessingBean> iterator = selection.iterator();
			List<AutoProcessingBean> beanlist = new ArrayList<>();
			iterator.forEachRemaining(beanlist::add);
			return beanlist;
		}
		return Collections.emptyList();
	}

	public String getDisplayName(Object element) {
		String displayName = ((AutoProcessingBean)element).getDisplayName();
		return displayName == null ? "" : displayName;
	}

	public void setInput(List<AutoProcessingBean> wrapperList) {
		viewer.setInput(wrapperList);
	}

	public void refresh() {
		viewer.refresh();
		notifyListeners();
	}

	/**
	 * Resize the table to fit the data, subject to a maximum size: table will scroll to view all items
	 */
	private void setTableSize(Composite tableComposite) {
		final int maxItems = 5;
		if (tableComposite.getLayoutData() instanceof GridData) {
			final int itemCount = Math.min(viewer.getTable().getItemCount(), maxItems);
			final int itemHeight = viewer.getTable().getItemHeight();
			final int headerHeight = viewer.getTable().getHeaderHeight();

			final GridData gd = (GridData) tableComposite.getLayoutData();
			final int h = (1 + itemCount) * itemHeight + headerHeight;
			gd.minimumHeight = h;
			gd.heightHint = h;
		}
	}

	@Override
	public void dispose() {
		if (ticked != null)
			ticked.dispose();
		if (unticked != null)
			unticked.dispose();
		super.dispose();
	}

	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			CheckboxCellEditor edit = new CheckboxCellEditor(viewer.getTable());
			edit.setValue(((AutoProcessingBean)element).isActive());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof AutoProcessingBean) return ((AutoProcessingBean)element).isActive();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof AutoProcessingBean && value instanceof Boolean){
				((AutoProcessingBean)element).setActive((Boolean)value);
			}

			refresh();
		}
	}

	public void removeSelectedItems() {
		List<AutoProcessingBean> currentSelection = getCurrentSelection();
		Object input = viewer.getInput();
		if (input instanceof List<?>) {
			((List) input).removeAll(currentSelection);
			refresh();
		}
	}

	public void editCurrentItem() {
		var selectedConfigs = getCurrentSelection();
		if (selectedConfigs.isEmpty()) {
			return;
		}

		List<AutoProcessingBean> beanList = (List<AutoProcessingBean>) viewer.getInput();
		var selectedConfig = selectedConfigs.get(0);
		int indexInList = beanList.indexOf(selectedConfig);
		AutoProcessingConfigDialog configDialog = createConfigDialong();
		configDialog.setConfigToShow(selectedConfig);

		if (Window.OK == configDialog.open()) {
			beanList.set(indexInList, configDialog.getConfig());
			refresh();
		}
	}

	public void addNewItem() {
		AutoProcessingConfigDialog configDialog = createConfigDialong();
		if (Window.OK == configDialog.open()) {
			AutoProcessingBean config = configDialog.getConfig();
			((List<AutoProcessingBean>) viewer.getInput()).add(config);
			refresh();
		}
	}

	private AutoProcessingConfigDialog createConfigDialong() {
		if (uri == null) {
			throw new IllegalArgumentException("Cannot create AutoProcessingConfigDialog - URI of GDA-Zocalo-Connector has not been set");
		}
		IMarshallerService service = Activator.getService(IMarshallerService.class);
		return new AutoProcessingConfigDialog(this.getShell(), uri, service);
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	@SuppressWarnings("unchecked")
	public List<AutoProcessingBean> getProcessingList() {
		return (List<AutoProcessingBean>) viewer.getInput();
	}

}
