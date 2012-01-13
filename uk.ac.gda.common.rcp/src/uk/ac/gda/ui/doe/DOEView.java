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

package uk.ac.gda.ui.doe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.doe.DOEUtils;
import uk.ac.gda.doe.RangeInfo;

public class DOEView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(DOEView.class);
	
	public static final String ID = "uk.ac.gda.ui.doe.DOEView"; //$NON-NLS-1$

	private TableViewer     tableViewer;
	private List<RangeInfo> info;
	private DOEBeanProvider beansProvider;
	
	public DOEView() throws CoreException {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.common.doe.beans");
		for (IConfigurationElement e : config) {	
			this.beansProvider = (DOEBeanProvider)e.createExecutableExtension("class");
            break;
		}
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		final Table table      = new Table(container, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);						
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
	
		this.tableViewer = new TableViewer(table);
	    
        tableViewer.setUseHashlookup(true);
	    tableViewer.setContentProvider(createContentProvider());
	    ColumnViewerToolTipSupport.enableFor(tableViewer,ToolTip.NO_RECREATE);
	    	   	    
		createRightClickMenu();
		
		getSite().setSelectionProvider(tableViewer);
		
	    getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				refresh();
			}
	    });

	}

	private void createTableColumns() {
		
		// Dispose columns not the first.
		for(;tableViewer.getTable().getColumnCount()>1;)
			tableViewer.getTable().getColumns()[1].dispose();
		
		// Create experiment name column if required.
		if (tableViewer.getTable().getColumnCount()<1) {
			final TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE, 0);
			col.getColumn().setText("Experiment");
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public void update(ViewerCell cell) {
					final TableItem item = (TableItem)cell.getViewerRow().getItem();
					final Table     table= tableViewer.getTable();
					int row = table.indexOf(item)+1;
					cell.setText(" "+row);
				}
			});
			col.getColumn().setWidth(120);
		}
		
		if (info == null) return;
		
		final Map<String,Class<?>> columns = new LinkedHashMap<String,Class<?>>();
		for (RangeInfo i : info) columns.putAll(i.getColumnClasses());
				
	    final String[] columnLabels     = getColumnLabels(columns);
	    final String[] columnProperties = getColumnProperties(columns);
		
		for (int icolumn = 1; icolumn < columnLabels.length; icolumn++) {
			
			final TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE, icolumn);
			col.getColumn().setText(columnLabels[icolumn]);
			
			col.setLabelProvider(new FieldColumnProvider(columnProperties));
			col.getColumn().setWidth(100);

		}
		
		
		tableViewer.setColumnProperties(columnProperties);
		tableViewer.setInput(new Object());

	}

	private String[] getColumnLabels(Map<String, Class<?>> columns) {
		
		final List<String> columnLabels = new ArrayList<String>(columns.size()+1);
		columnLabels.add("Run"); // The experiment number column
		
        for (String name : columns.keySet()) {
			String label = beansProvider.getColumnLabel(name, columns.get(name));
			if (label==null) label = name;
			columnLabels.add(label);
		}
		
        return columnLabels.toArray(new String[columnLabels.size()]);
	}
	
	private String[] getColumnProperties(Map<String, Class<?>> columns) {
		
		final List<String> columnNames = new ArrayList<String>(columns.size()+1);
		columnNames.add("Run"); // The experiment number column
		
        for (String name : columns.keySet()) columnNames.add(name);
		
        return columnNames.toArray(new String[columnNames.size()]);
	}


	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {

			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				try {
			        return info.toArray(new RangeInfo[info.size()]);
				} catch (Exception e) {
					logger.error("Cannot get info from list", e);
					return null;
				}
			}
		};
	}

	private void createInfo() throws Exception {
        final List<Object>    obs  = beansProvider.getBeans();
        DOEView.this.info = DOEUtils.getInfoFromList(obs);
	}

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}
	
	private void createRightClickMenu() {	
	    final MenuManager menuManager = new MenuManager();
		tableViewer.getControl().setMenu (menuManager.createContextMenu(tableViewer.getControl()));
		getSite().registerContextMenu(menuManager, tableViewer);
	}

	public void refresh() {
	    try {
			createInfo();
		    createTableColumns();
			tableViewer.refresh();
		} catch (Exception e) {
			logger.error("Cannot read ranges", e);
		}
	}

}
