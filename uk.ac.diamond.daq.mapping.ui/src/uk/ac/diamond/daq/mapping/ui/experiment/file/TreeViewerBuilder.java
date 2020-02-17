/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public abstract class TreeViewerBuilder<T> {

	private static final int FOUR_ROWS = 165;

	private ObservableList<T> observableList;
	private List<IntColumn> columns = new ArrayList<>();
	private TreeViewer viewer;
	private IContentProvider contentProvider;
	private MenuManager contextMenu;

	private ISelectionChangedListener selectionChangeListener;
	private IDoubleClickListener doubleClickListener;

	public TreeViewerBuilder<T> addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		this.doubleClickListener = doubleClickListener;
		return this;
	}

	public TreeViewerBuilder<T> addObservableList(ObservableList<T> observableList) {
		this.observableList = observableList;
		return this;
	}

	public TreeViewerBuilder<T> addColumn(String name, int width, IStyledLabelProvider provider) {
		columns.add(new IntColumn(name, width, provider));
		return this;
	}

	public TreeViewerBuilder<T> addContentProvider(IContentProvider contentProvider) {
		this.contentProvider = contentProvider;
		return this;
	}

	public TreeViewerBuilder<T> addSelectionListener(ISelectionChangedListener selectionChangeListener) {
		this.selectionChangeListener = selectionChangeListener;
		return this;

	}

	public TreeViewerBuilder<T> addMenuManager(MenuManager contextMenu) {
		this.contextMenu = contextMenu;
		return this;
	}

	public TreeViewer build(Composite parent) {
		FilteredTree tree = new FilteredTree(parent, SWT.V_SCROLL, new PatternFilter(), true);
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, FOUR_ROWS).applyTo(tree);
		viewer = tree.getViewer();
		viewer.getTree().setHeaderVisible(true);
		viewer.setContentProvider(contentProvider);

		viewer.addDoubleClickListener(doubleClickListener);
		viewer.addSelectionChangedListener(selectionChangeListener);
		observableList.addListChangeListener(getListChangeListener(viewer));
		columns.stream().forEachOrdered(this::addColumn);
		viewer.setInput(getInputElements());
		viewer.refresh();

		contextMenu.setRemoveAllWhenShown(true);
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		return viewer;
	}

	private IListChangeListener<T> getListChangeListener(TreeViewer viewer) {
		return e -> {
			viewer.setInput(getInputElements());
			viewer.refresh();
		};
	}

	public abstract T[] getInputElements();

	/**
	 * Adds a column to the main {@link TreeViewer} using the supplied {@link IComparableStyledLabelProvider} and
	 * settings.
	 *
	 * @param name
	 *            The displayed name of the column
	 * @param width
	 *            The preferred width of the column
	 * @param provider
	 *            The provider of formatted content for cells in the column
	 */
	private void addColumn(IntColumn intColumn) {
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(intColumn.getName());
		column.getColumn().setWidth(intColumn.getWidth());
		column.setLabelProvider(new DelegatingStyledCellLabelProvider(intColumn.getProvider()));
		column.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (IComparableStyledLabelProvider.class.isInstance(intColumn.getProvider())) {
					viewer.setComparator(
							IComparableStyledLabelProvider.class.cast(intColumn.getProvider()).getComparator());
					sorter(column.getColumn());
				}
			}
		});
	}

	/**
	 * Set the currently sorted column and its sort direction
	 *
	 * @param sortColumn
	 *            The column selected as the currently sorted column
	 */
	private void sorter(TreeColumn sortColumn) {
		Tree tree = viewer.getTree();
		int direction = tree.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
		tree.setSortDirection(direction);
		tree.setSortColumn(sortColumn);
		viewer.refresh();
	}

	class IntColumn {
		private final String name;
		private final int width;
		private final IStyledLabelProvider provider;

		/**
		 * @param name
		 *            The displayed name of the column
		 * @param width
		 *            The preferred width of the column
		 * @param provider
		 *            The provider of formatted content for cells in the column
		 */
		public IntColumn(String name, int width, IStyledLabelProvider provider) {
			super();
			this.name = name;
			this.width = width;
			this.provider = provider;
		}

		public String getName() {
			return name;
		}

		public int getWidth() {
			return width;
		}

		public IStyledLabelProvider getProvider() {
			return provider;
		}
	}
}
