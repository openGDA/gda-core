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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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

		MenuManager menuMgr = new MenuManager();

		menuMgr.add(new Action("Remove") {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void run() {
				ISelection s = viewer.getSelection();
				if (s instanceof StructuredSelection) {
					List<AutoProcessingBean> w = new ArrayList<>();
					Iterator iterator = ((StructuredSelection)s).iterator();
					while (iterator.hasNext()) {
						Object next = iterator.next();
						if (next instanceof AutoProcessingBean) {
							w.add((AutoProcessingBean)next);
						}
					}

					Object input = viewer.getInput();
					if (input instanceof List<?>) {
						((List) input).removeAll(w);
						viewer.refresh();
					}
				}
			}
		});

		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getTable().setMenu(menu);

		tableComposite.setLayout(columnLayout);

		setTableSize(tableComposite);
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

			getViewer().refresh();
		}

	}

}
