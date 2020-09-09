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

package gda.rcp.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceDeleteEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * A builder for a {@link TreeViewer} which can be used as base for other GDA components
 *
 * @param <T>
 *
 * @author Maurizio Nagni
 */
public abstract class TreeViewerBuilder<T> {

	private static final int FOUR_ROWS = 165;

	private static final Logger logger = LoggerFactory.getLogger(TreeViewerBuilder.class);

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
		columns.stream().forEachOrdered(this::addColumn);
		viewer.setInput(getInputElements(true));
		viewer.refresh();

		contextMenu.setRemoveAllWhenShown(true);
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(tree, saveResourcesListener);
			SpringApplicationContextProxy.addDisposableApplicationListener(tree, deleteResourcesListener);
		} catch (GDAClientException e) {
			logger.error("Could not add application listener(s)", e);
		}
		return viewer;
	}

	private ApplicationListener<AcquisitionConfigurationResourceSaveEvent> saveResourcesListener = new ApplicationListener<AcquisitionConfigurationResourceSaveEvent>() {
		@Override
		public void onApplicationEvent(AcquisitionConfigurationResourceSaveEvent event) {
			save(event.getUrl());
		}
	};

	private ApplicationListener<AcquisitionConfigurationResourceDeleteEvent> deleteResourcesListener = new ApplicationListener<AcquisitionConfigurationResourceDeleteEvent>() {
		@Override
		public void onApplicationEvent(AcquisitionConfigurationResourceDeleteEvent event) {
			delete(event.getUrl());
		}
	};

	/**
	 * Default implementation updates viewer input according to {@link #getInputElements(boolean)}
	 *
	 * @param configuration URL of saved configuration
	 */
	protected void save(@SuppressWarnings("unused") URL configuration) {
		refreshResources();
	}

	/**
	 * Default implementation updates viewer input according to {@link #getInputElements(boolean)}
	 *
	 * @param configuration URL of deleted configuration
	 */
	protected void delete(@SuppressWarnings("unused") URL configuration) {
		refreshResources();
	}

	/**
	 * Replace the viewer input with the given array and refresh
	 */
	protected void updateContents(T[] updated) {
		viewer.setInput(updated);
		viewer.refresh();
	}

	private void refreshResources() {
		updateContents(getInputElements(true));
	}

	public abstract T[] getInputElements(boolean reload);

	/**
	 * Adds a column to the main {@link TreeViewer} using the supplied {@link ComparableStyledLabelProvider} and
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
				if (ComparableStyledLabelProvider.class.isInstance(intColumn.getProvider())) {
					viewer.setComparator(
							ComparableStyledLabelProvider.class.cast(intColumn.getProvider()).getComparator());
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
