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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.DataSetProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.PydevConsoleAction;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotServerConnection;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DataSetPlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.ExpressionObject;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.IDataSetPlotViewProvider;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.SliceComponent;


/**
 * An editor which combines a plot with a graph of data sets.
 * 
 * Currently this is for 1D analyses only so if the data does not contain 1D, this
 * editor will not show.
 * 
 */
public class DataSetPlotEditor extends EditorPart implements IReusableEditor, IDataSetPlotViewProvider, DataSetProvider {
	
	private static Logger logger = LoggerFactory.getLogger(DataSetPlotEditor.class);
	
	// This view is a composite of two other views.
	private DataSetPlotView   dataSetPlotView;
	private SliceComponent    sliceComponent;
	private AbstractPlotWindow        plotWindow;
	private IDataHolder       cachedDataHolder;
	private PlotServerConnection plotServerConnection;
	private Text              point;
	private Composite         pointControls;
	
	public DataSetPlotEditor() {
	
	}
	public DataSetPlotEditor(DataHolder dataHolder) {
		this.cachedDataHolder = dataHolder;
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
	public void createPartControl(final Composite parent) {
		
		final Composite  main       = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		main.setLayout(gridLayout);
		
		final Composite tools = new Composite(main, SWT.NONE);
		tools.setLayout(new GridLayout(4, false));
		GridUtils.removeMargins(tools);
		tools.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		this.pointControls = new Composite(tools, SWT.NONE);
		pointControls.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		pointControls.setLayout(new GridLayout(2, false));
		GridUtils.removeMargins(pointControls);
		
		final CLabel pointLabel = new CLabel(pointControls, SWT.NONE);
		pointLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		pointLabel.setText("Position ");
		
	    this.point = new Text(pointControls, SWT.RIGHT);
	    final GridData pointData = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    pointData.widthHint = 150;
	    point.setLayoutData(pointData);
	    point.setEditable(false);
	    point.setText("0, 0");
	    GridUtils.setVisible(pointControls, false);

	    final ToolBarManager   leftMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar          leftBar = leftMan.createControl(tools);
	    leftBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

	    // We use a local toolbar to make it clear to the user the tools
		// that they can use, also because the toolbar actions are 
		// hard coded.
	    final ToolBarManager   toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar          toolBar = toolMan.createControl(tools);
	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
	    
	    final ToolBarManager   rightMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar          rightBar = rightMan.createControl(tools);
	    rightBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final IActionBars bars = this.getEditorSite().getActionBars();
        final ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,null,null,(IActionBars2)bars);

        final SashForm sashForm = new SashForm(main, SWT.BORDER);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(new FillLayout());

		// NOTE use name of input. This means that although two files of the same
		// name could be opened, the editor name is clearly visible in the GUI and
		// is usually short.
		final String plotName = this.getEditorInput().getName();
		
		// Connect this PlotWindow to the server TODO Lazy initiation with this?
		// no point connecting it to the plot server unless absolutely necessary.
		this.plotServerConnection = new PlotServerConnection(plotName);
		
        this.plotWindow = new PlotWindow(right,
										plotServerConnection,
										wrapper,
										this,
										plotName);	
		plotWindow.setNotifyListener(plotServerConnection);
		plotServerConnection.setPlotWindow(plotWindow);
		plotWindow.setExclusiveToolars(true);
		
		// This block relies on the plotter always starting as 1D which it does currently
		// We intentionally fail if this is not the case so alert the test desks as to the
		// change.
		final Plot1DUIAdapter ui = (Plot1DUIAdapter)plotWindow.getPlotUI();
		ui.addPositionSwitchListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				GridUtils.setVisible(pointControls, (Boolean)event.getNewValue());
				pointControls.getParent().layout(new Control[]{pointControls});
			}
		});
		ui.addPlotActionEventListener(new PlotActionEventListener() {
			@Override
			public void plotActionPerformed(PlotActionEvent event) {
				final String pos = String.format("%g, %g", event.getPosition()[0], event.getPosition()[1]);
				
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						point.setText(pos);
					}	
				});
				
			}
		});
					
		createCustomToolbarActionsLeft(leftMan);
		createCustomToolbarActionsRight(rightMan);
     
		SashForm left = new SashForm(sashForm, SWT.VERTICAL);
		left.setLayout(new FillLayout());
		
		this.dataSetPlotView = new DataSetPlotView(false, false, false, this);
		dataSetPlotView.createPartControl(left);
		dataSetPlotView.addSelectionListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				updatePlot(((StructuredSelection)event.getSelection()).toArray());
			}
		});
		dataSetPlotView.setPlotWindow(plotWindow);
		dataSetPlotView.setFileName(getEditorInput().getName());
		
		this.sliceComponent = new SliceComponent();
		sliceComponent.createPartControl(left);
		sliceComponent.setVisible(false);
		
		left.setWeights(new int[] {30, 70});
		sashForm.setWeights(new int[] {70, 30});
		
		// Finally
		leftMan.update(true);
		toolMan.update(true);
		rightMan.update(true);
	    createData(getEditorInput());	
	    
		EditorUtils.addSidePlotActivator(this, plotWindow, getPartName());

		
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (plotWindow!=null) {
					logger.error("The AbstractPlotWindow does not support side plots anymore");
//					ui.getSidePlotView().switchSidePlot(plotWindow.getPlotUI(), 0); // Start them off with the first one.
				}
			}
		});
 	}
	
	/**
	 * Override to provide extra content.
	 * @param toolMan
	 */
	protected void createCustomToolbarActionsLeft(final ToolBarManager toolMan) {
				
		final Action scipting = new Action("Plot Scripting", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				final PydevConsoleAction p = new PydevConsoleAction();
				try {
					p.execute(null);
				} catch (ExecutionException e) {
					logger.error("Cannot open python console.", e);
				}
			}
		};
		scipting.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/application_osx_terminal.png"));

		toolMan.add(scipting);
		
		toolMan.add(new Separator(getClass().getName()+"Separator0"));
		
	}
	/**
	 * Override to provide extra content.
	 * @param toolMan
	 */
	protected void createCustomToolbarActionsRight(final ToolBarManager toolMan) {
		
		
		toolMan.add(new Separator(getClass().getName()+"Separator1"));

		final Action tableColumns = new Action("Open editor preferences.", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "uk.ac.diamond.scisoft.analysis.rcp.preferencePage", null, null);
				if (pref != null) pref.open();
			}
		};
		tableColumns.setChecked(false);
		tableColumns.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/application_view_columns.png"));

		toolMan.add(tableColumns);
		
	}

	private boolean doingUpdate = false;
	
	protected void updatePlot(final Object[] selections) {
		
		if (doingUpdate) return;
		
		if (selections==null || selections.length<1) {
			if (sliceComponent!=null) sliceComponent.setVisible(false);
		}
		
		try {
			doingUpdate = true;
			if (selections==null||selections.length<1) {
				plotWindow.getPlottingSystem().reset();
//				plotWindow.getMainPlotter().hideAllPlots();
				return;
			}
			
			if (selections.length==1 && dataSetPlotView.getDimensionCount(selections[0])!=1) {
				sliceComponent.setVisible(true);
				sliceComponent.setData(selections[0].toString(),
									   EclipseUtils.getFilePath(getEditorInput()), 
									   dataSetPlotView.getMetaData().getDataShapes().get(selections[0].toString()), 
									   plotWindow);
				return;
			}
			
			IProgressService service =  getEditorSite()!=null 
			                         ? (IProgressService)getSite().getService(IProgressService.class)
					                 : (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);
			try {
				service.run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	
						monitor.beginTask("Updating selected DataSets", 100);
	                    createCachedData(monitor);
			
						final Dataset x  = getDataSet(selections[0], monitor);
						List<Dataset> ys = null;
						if (selections.length>1) {
							ys = new ArrayList<Dataset>(3);
							for (int i = 1; i < selections.length; i++) {
								ys.add(getDataSet(selections[i], monitor));
								if (monitor.isCanceled()) return;
								monitor.worked(1);
							}
						}

						PlotUtils.create1DPlot(x, ys, dataSetPlotView.getPlotMode(), plotWindow, monitor);
	
					}
				});
			} catch (Exception e) {
				logger.error("Cannot create plot required.", e);
			} 
		} finally {
			doingUpdate = false;
		}
	}
	
	protected void createCachedData(final IProgressMonitor mon) {
   
		final IMonitor wrapper = mon!=null ? new ProgressMonitorWrapper(mon) : null;
		createCachedData(wrapper);
	}
	
	protected void createCachedData(final IMonitor mon) {
		if (cachedDataHolder==null) {
			try {
				cachedDataHolder = LoaderFactory.getData(EclipseUtils.getFilePath(getEditorInput()), mon);
			} catch (Exception e) {
				logger.error("Cannot load data from "+getEditorInput().getName(), e);
				return;
			}
		}
	}


	@Override
	public Dataset getDataSet(String name, IMonitor monitor) {
		createCachedData(monitor);
		return DatasetUtils.convertToDataset(cachedDataHolder.getDataset(name));
	}


	@Override
	public boolean isDataSetName(String name, IMonitor monitor) {
        createCachedData(monitor);
		return cachedDataHolder.toLazyMap().keySet().contains(name);
	}
	
	public Dataset getDataSet(final Object object, final IProgressMonitor monitor) {
		if (object instanceof ExpressionObject) {
			try {
				return ((ExpressionObject)object).getDataSet(monitor);
			} catch (Exception e) {
				// valid, user can enter an invalid expression. In this case
				// it colours red but does not stop them from using the view.
				return new DoubleDataset();
			}
		}
        createCachedData(monitor);
		return getDataSet((String)object, new ProgressMonitorWrapper(monitor));
	}


	@Override
	public void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		createData(input);
	}
	
	/**
	 * Reads the data sets from the IEditorInput
	 */
	private void createData(final IEditorInput input) {

		if (dataSetPlotView==null) return;
		try {
			final IMetadata meta = LoaderFactory.getMetadata(EclipseUtils.getFilePath(input), null);
			dataSetPlotView.setMetaData(meta);
			dataSetPlotView.refresh();
		} catch (Exception ne) {
			logger.error("Cannot open nexus", ne);
		}
		
	}

	

	@Override
	public void setFocus() {
		dataSetPlotView.setFocus();
		
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
     	if (cachedDataHolder!=null)     cachedDataHolder.clear();
    	if (plotServerConnection!=null) plotServerConnection.dispose();
    	if (plotWindow!=null)           plotWindow.dispose();
    	if (sliceComponent!=null)       sliceComponent.dispose();
     	super.dispose();
    }

	@Override
	public DataSetPlotView getDataSetPlotView() {
		return dataSetPlotView;
	}
	/**
	 * Thread safe method which can be called to select a data set in the tree.
	 * @param name
	 */
	public Dataset setDatasetSelected(final String name, boolean clearOthers) {
		return dataSetPlotView.setDatasetSelected(name, clearOthers);
	}

	public AbstractPlotWindow getPlotWindow() {
		return plotWindow;
	}

}
