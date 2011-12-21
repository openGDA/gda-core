/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.editors;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.CSVUtils;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.common.rcp.util.GridUtils;


/**
 * An editor which shows the current data sets plotted in a table.
 * 
 * This may be needed if the scientist has manipulated the data and would like to view it or export it.
 */
public class DataEditor extends EditorPart implements IReusableEditor, IPageChangedListener {
	
	private static Logger logger = LoggerFactory.getLogger(DataEditor.class);
	
	// This view is a composite of two other views.
	private TableViewer tableViewer;
	private CLabel      errorLabel;
	private Composite   main;
	private PlotWindow  plotWindow;
	
	public DataEditor() {
	    
	}
	
	/**
	 * You must call this method with the plotWindow which you would like to 
	 * interrogate and show the window of.
	 * @param plotWindow
	 */
	public void setPlotter(PlotWindow plotWindow) {
		this.plotWindow = plotWindow;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		
	}

	@Override
	public boolean isDirty() {
		return false;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		this.main       = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		main.setLayout(gridLayout);
		
		// We use a local toolbar to make it clear to the user the tools
		// that they can use, also because the toolbar actions are 
		// hard coded.
	    final ToolBarManager   toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar          toolBar = toolMan.createControl(main);
	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
	    createActions(toolMan);
	    
		errorLabel = new CLabel(main, SWT.NONE);
		errorLabel.setImage(AnalysisRCPActivator.getImage("icons/error.png"));
		errorLabel.setText("This editor shows the data plotted. Please plot at least one data set and view the data here.");
		errorLabel.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED));
		errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		toolMan.update(false);
		main.layout();
		
        AnalysisRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.DATA_FORMAT)) {
					if (tableViewer!=null) tableViewer.refresh();
				}
			}
		});
 	}
	
	private void createActions(final ContributionManager toolMan) {
		
		final Action exportCsv = new Action("Export current plotted data to csv file", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				CSVUtils.createCSV(EclipseUtils.getIFile(getEditorInput()), data, "_plot");
			}
		};
		exportCsv.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/page_white_excel.png"));
		toolMan.add(exportCsv);
		
		toolMan.add(new Separator(getClass().getName()+"Sep1"));
		
		final Action format = new Action("Preferences...", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "uk.ac.diamond.scisoft.analysis.rcp.preferencePage", null, null);
				if (pref != null) pref.open();
			}
		};
		format.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/application_view_list.png"));
		toolMan.add(format);
	}

	private Map<String,AbstractDataset> data;
	/**
	 * This method to creates the table columns required to hold the
	 * 1D data currently in the plotter passed in.
	 * @param plotter
	 */
	private void update(final DataSetPlotter plotter) {
		
		if (tableViewer!=null) {
			GridUtils.setVisible(tableViewer.getTable(), false);
			tableViewer.getTable().dispose();
		}
		
		final List<IDataset> sets = plotter.getCurrentDataSets();
		if (sets==null || sets.isEmpty()) {
			GridUtils.setVisible(errorLabel, true);
			main.layout();
			return;
		}
		
		// We put the x values as the first set
		final List<AxisValues> axes = plotter.getXAxisValues();
		final AbstractDataset x =  axes.get(0).toDataset();
		x.setName(plotter.getXAxisLabel());
		sets.add(0,x);
		
		final Table table = new Table(main, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.tableViewer  = new TableViewer(table);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);		
		GridUtils.setVisible(errorLabel, false);
		main.layout();

		if (data==null) data = new LinkedHashMap<String, AbstractDataset>();
		data.clear();
		
		final String[] names = new String[sets.size()];
		for (int i = 0; i < sets.size(); i++) {

			final AbstractDataset set = DatasetUtils.convertToAbstractDataset(sets.get(i));

			final TableViewerColumn col   = new TableViewerColumn(tableViewer, SWT.RIGHT, i);
			names[i] = set.getName()!=null?set.getName():"";
			data.put(names[i], set);
			col.getColumn().setText(names[i]);
			col.getColumn().setWidth(150);
			col.setLabelProvider(new ValueColumnProvider(i));
		}
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(names);
	    final MenuManager menuManager = new MenuManager();
	    tableViewer.getControl().setMenu (menuManager.createContextMenu(tableViewer.getControl()));
	    createActions(menuManager);

		final Object[] data = getRowData(sets);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			@Override
			public Object[] getElements(Object inputElement) {
				return data;
			}
		});		
		tableViewer.setInput(data);
		main.layout();
	}
	
	/**
	 * The data sets are the columns, we need an array of the rows to work
	 * best with TableViewer.
	 * @param sets
	 * @return an array of RowObjects
	 */
	protected Object[] getRowData(final List<IDataset> sets) {
		
		// Find max
		int max = Integer.MIN_VALUE;
		for (IDataset set : sets) max = Math.max(max, set.getSize());
		
		final List<RowObject> rows = new ArrayList<RowObject>(max);
		for (int row = 0; row < max; row++) {
			final RowObject rowOb = new RowObject(row);
			rows.add(rowOb);
			for (IDataset set : sets) {
				try {
				    rowOb.add(set.getDouble(row));
				} catch (Exception ne) {
					rowOb.add(Double.NaN);
				}
			}
		}
		
		return rows.toArray(new Object[max]);
	}
	
	private class RowObject {
		private List<Double> rowValues = new ArrayList<Double>(31);
		private int row;
		RowObject(int row) {
			this.row = row;
		}
		public void add(Double value) {
			rowValues.add(value);
		}
		public Double get(int col) {
			return rowValues.get(col);
		}
		public int getRow(){
			return row;
		}
	}
	
	private class ValueColumnProvider extends ColumnLabelProvider {
		private int columnIndex;
		ValueColumnProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}
		@Override
		public String getText(Object element) {
			final RowObject row = (RowObject)element;
			final double    val = row.get(columnIndex);
			if (Double.isNaN(val)) return "";

			return formatValue(val);
		}
		
		private String formatValue(final double val) {
			final String formatString = AnalysisRCPActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.DATA_FORMAT);
		    try {
		    	DecimalFormat format = new DecimalFormat(formatString);
				return format.format(val);
		    } catch (Exception ne) {
		    	logger.debug("Format does not work: "+formatString, ne);
		    	return String.valueOf(val);
		    }
		}

	}

	@Override
	public void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
	}
		

	@Override
	public void setFocus() {
		if (tableViewer!=null) tableViewer.getControl().setFocus();	
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    @Override
    public void dispose() {
     	if (tableViewer!=null) tableViewer.getControl().dispose();
     	if (main!=null)        main.dispose();
     	if (data!=null)        data.clear();
     	if (plotWindow!=null)  plotWindow = null;
    	super.dispose();
    }

	@Override
	public void pageChanged(PageChangedEvent event) {
		if (event.getSelectedPage()==this) { // Just selected this page
			update(plotWindow.getMainPlotter());
		}
	}

}
