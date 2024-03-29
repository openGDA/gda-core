/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.commandinfo.ui;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.ICommandThreadObserver;
import uk.ac.gda.client.commandinfo.CommandInfoModel;

public class CommandInfoComposite extends Composite implements ICommandThreadObserver {

	static public final String CLASS_NAME = "CommandInfoComposite";

	static public enum CiColumn {
		_id (0,"ID",60,""),
		_threadType (1,"Type",90,""),
		_state (2,"State",100,""),
		_priority (3,"Priority",50,""),
		_command(4,"Command",250,"");
		static public CiColumn getColumnByIndex(int index) {
			CiColumn result = null;
			for (CiColumn column : CiColumn.values()) {
				if (index == column.index) {
					result = column;
					break;
				}
			}
			return result;
		}
		public int index;
		public String title;
		public int width;
		public String tooltip;
		CiColumn(int index, String title, int width, String tooltip) {
			this.index = index;
			this.title = title;
			this.width = width;
			this.tooltip = tooltip;
		}
	}

	private CommandInfoContentProvider contentProvider= new CommandInfoContentProvider();
	private CommandInfoLabelProvider labelProvider= new CommandInfoLabelProvider();
	private TableViewer viewer = null;

	public CommandInfoComposite(Composite parent, int style) {
		super(parent, style);
		this.createLayout(parent);
	}

	private void addInternalListeners() { }

	public void createLayout(Composite parent) {
		GridData   masterData = new GridData(SWT.FILL,SWT.FILL,true,true);
		GridLayout masterLayout = new GridLayout(1,false);
		masterLayout.marginWidth = 0;
		masterLayout.marginHeight = 1;
		parent.setLayout(masterLayout);
		this.setLayout(masterLayout);
		this.setLayoutData(masterData);

		this.createTablePanel(this,SWT.FILL);
		this.addInternalListeners();
	}

	private void createTableColumns(TableViewer viewer) {
		for (CiColumn column : CiColumn.values()) {
			this.createTableViewerColumn(viewer,column);
		}
	}

	private void createTablePanel(Composite parent, int tableStyle) {
		GridLayout layoutTable = new GridLayout(1,false);
		layoutTable.marginWidth = 0;
		layoutTable.marginHeight = 0;
		layoutTable.horizontalSpacing = 1;
		layoutTable.verticalSpacing = 1;

		Composite comTable = new Composite(parent,tableStyle);
		comTable.setLayout(layoutTable);
		comTable.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

		// int style = SWT.VIRTUAL | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER; //SWT.FULL_SELECTION | SWT.SINGLE
		viewer = new TableViewer(comTable,tableStyle);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		createTableColumns(viewer);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider); // after columns set

		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setVisible(true);
		table.setHeaderVisible(true);
	}

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, CiColumn cic) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(cic.title);
		column.setWidth(cic.width);
		column.setToolTipText(cic.tooltip);
		return viewerColumn;
	}

	@Override
	public void dispose() {
		this.contentProvider = null;
		this.labelProvider = null;
		super.dispose();
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void refresh() {
		viewer.refresh();
	}

	public void setInput(Object input) {
		if(null!=viewer) {
			if (input instanceof CommandInfoModel) {
				viewer.setInput(input);
			}
		}
	}

	// Respond to Command Info events
	@Override
	public void update(Object source, Object arg) {
		@SuppressWarnings("unused") // can extend behaviour for different events
		CommandThreadEvent event = (CommandThreadEvent) arg;
		if (arg instanceof CommandThreadEvent) {
			event = (CommandThreadEvent) arg;
			viewer.refresh();
			@SuppressWarnings("unused")
			int debug = 0;
		}
	}

}
