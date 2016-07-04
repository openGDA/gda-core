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

package uk.ac.diamond.scisoft.analysis.rcp.views.nexus;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.dawb.common.util.io.PropUtils;
import org.dawb.common.util.io.SortingUtils;
import org.dawb.common.util.list.SortNatural;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.ResourceManager;

import uk.ac.diamond.scisoft.analysis.deprecated.rcp.Activator;
import uk.ac.diamond.scisoft.analysis.io.DataSetProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.monitor.ProgressMonitorWrapper;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.preference.DeprecatedPreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotUtils;
import uk.ac.diamond.scisoft.analysis.rcp.util.SDAUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.gda.ui.modifiers.DoubleClickModifier;

/**
 * This view can view and plot any file. It is most efficient if the Loader that LoaderFactory
 * uses for this file type is an IMetaLoader. 
 * 
 * TODO fix or delete - this is causing lots of errors
 */
public class DataSetPlotView extends ViewPart implements ICheckStateListener, DataSetProvider {
		
	private static final Logger logger = LoggerFactory.getLogger(DataSetPlotView.class);

	// NOTE Old ID before this class was convert to display files without knowing the 
	// underlying file type.
	public static final String ID = "uk.ac.gda.views.nexus.NexusPlotView"; //$NON-NLS-1$

	// Use table as it might get extended to do more later.
	protected CheckboxTableViewer dataViewer;
	protected List<Object>        data;
	protected String              filePath;
	protected String              fileName;

	protected IMetadata           metaData;

	protected final boolean isDialogMode;
	protected final boolean isPlottingRequired;
	protected final boolean requireToolbar;
	protected final DataSetProvider providerDeligate;
	
	/**
	 * @wbp.parser.constructor
	 */
	public DataSetPlotView() {
		this(false, true, false, null);
	}

	public DataSetPlotView(final boolean         isDialogMode, 
			               final boolean         isPlottingRequired,
			               final boolean         requireToolbar,
			               final DataSetProvider providerDeligate) {
				
		this.data = new ArrayList<Object>(7);
		this.isDialogMode       = isDialogMode;
		this.isPlottingRequired = isPlottingRequired;
		this.requireToolbar     = requireToolbar;
		this.providerDeligate   = providerDeligate;
		
		// If they change the ignore filters activity, recompute the available data sets.
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(DeprecatedPreferenceConstants.IGNORE_DATASET_FILTERS)) {
					
					if (filePath==null) return;
					IProgressService service = (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);
					try {
						// Changed to cancellable as sometimes loading the tree takes ages and you
						// did not mean such to choose the file.
						service.run(true, true, new IRunnableWithProgress() {
							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
									setFile(filePath, monitor);
								} catch (Exception e) {
									throw new InvocationTargetException(e);
								}
							}
						});
					} catch (Exception ne) {
						logger.error("Unable to refresh data set list", ne);
					}
				} else if (event.getProperty().equals(DeprecatedPreferenceConstants.SHOW_XY_COLUMN)) {
					setColumnVisible(1, 24, (Boolean)event.getNewValue());
				} else if (event.getProperty().equals(DeprecatedPreferenceConstants.SHOW_DATA_SIZE)) {
					setColumnVisible(2, 100, (Boolean)event.getNewValue());
				} else if (event.getProperty().equals(DeprecatedPreferenceConstants.SHOW_DIMS)) {
					setColumnVisible(3, 100, (Boolean)event.getNewValue());
				} else if (event.getProperty().equals(DeprecatedPreferenceConstants.SHOW_SHAPE)) {
					setColumnVisible(4, 100, (Boolean)event.getNewValue());
				}
			}
		});
	}


	protected void setColumnVisible(final int col, final int width, boolean isVis) {
		if (this.dataViewer==null || this.dataViewer.getControl().isDisposed()) return;
		dataViewer.getTable().getColumn(col).setWidth(isVis?width:0);
	}
	
	@Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site);
	}
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) container.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gl_container = new GridLayout(1, false);
		if (requireToolbar) {
			final ToolBarManager man = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
			createDimensionalActions(man, true);
			man.createControl(container);
			man.getControl().setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		} else {
			gl_container.verticalSpacing = 0;
			gl_container.marginWidth = 0;
			gl_container.marginHeight = 0;
			gl_container.horizontalSpacing = 0;
		}
		container.setLayout(gl_container);
		
		this.dataViewer = CheckboxTableViewer.newCheckList(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		dataViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		dataViewer.addCheckStateListener(this);
		dataViewer.getTable().setLinesVisible(true);
		dataViewer.getTable().setHeaderVisible(true);
		
		createColumns();
		dataViewer.setUseHashlookup(true);
        dataViewer.setColumnProperties(new String[]{"Data","Length"});
        
        dataViewer.setCellEditors(createCellEditors(dataViewer));
        dataViewer.setCellModifier(createModifier(dataViewer));
		dataViewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				return data.toArray(new Object[data.size()]);
			}
		});		
		
		// Maybe being the selection provider cause the left mouse problem
        //if (getSite()!=null) getSite().setSelectionProvider(dataViewer);
		dataViewer.setInput(new String());
				
		createRightClickMenu();
		initializeToolBar();
		
		setColumnVisible(1, 24,  Activator.getDefault().getPreferenceStore().getBoolean(DeprecatedPreferenceConstants.SHOW_XY_COLUMN));
		setColumnVisible(2, 100, Activator.getDefault().getPreferenceStore().getBoolean(DeprecatedPreferenceConstants.SHOW_DATA_SIZE));
		setColumnVisible(3, 100, Activator.getDefault().getPreferenceStore().getBoolean(DeprecatedPreferenceConstants.SHOW_DIMS));
		setColumnVisible(4, 100, Activator.getDefault().getPreferenceStore().getBoolean(DeprecatedPreferenceConstants.SHOW_SHAPE));
	}
	
	private void saveExpressions() {
		try {
			final Properties props = new Properties();
			for (Iterator<Object> iterator = data.iterator(); iterator.hasNext();) {
				Object d = iterator.next();
				if (d instanceof ExpressionObject) {
					ExpressionObject o = (ExpressionObject)d;
					props.setProperty(o.getMementoKey(), o.getExpression());
				}
			}
			
			// Save properties to workspace.
			final String cachePath = SDAUtils.getSdaHome()+getFileName()+".properties";
			PropUtils.storeProperties(props, cachePath);
			
		} catch (Exception e) {
			logger.error("Cannot save expression", e);
		}
	}
	
	private void readExpressions() throws Exception {
		
		final String cachePath = SDAUtils.getSdaHome()+getFileName()+".properties";
		Properties props = PropUtils.loadProperties(cachePath);
		if (props!=null) {
			try {
				for (Object name : props.keySet()) {
					final String key = name.toString();
					if (ExpressionObject.isExpressionKey(key)) {
						data.add(new ExpressionObject(this, props.getProperty(key), key));
					}
				}
			} catch (Exception ne) {
				throw new PartInitException(ne.getMessage());
			}
		}
	}

	private void createRightClickMenu() {	
	    final MenuManager menuManager = new MenuManager();
	    dataViewer.getControl().setMenu (menuManager.createContextMenu(dataViewer.getControl()));
		if (getSite()!=null) getSite().registerContextMenu(menuManager, dataViewer);
		createDimensionalActions(menuManager, false);
		menuManager.add(new Separator(getClass().getName()+"sep1"));
		menuManager.add(new Action("Preferences...") {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "uk.ac.diamond.scisoft.analysis.rcp.preferencePage", null, null);
				if (pref != null) pref.open();
			}
		});
	}
	
	private void createColumns() {
		
		ColumnViewerToolTipSupport.enableFor(dataViewer,ToolTip.NO_RECREATE);
		
		final TableViewerColumn name   = new TableViewerColumn(dataViewer, SWT.LEFT, 0);
		name.getColumn().setText("Data");
		name.getColumn().setWidth(150);
		name.setLabelProvider(new DataSetColumnLabelProvider(0));
		
		final TableViewerColumn axis   = new TableViewerColumn(dataViewer, SWT.LEFT, 1);
		axis.getColumn().setText(" ");
		axis.getColumn().setWidth(24);
		axis.setLabelProvider(new DataSetColumnLabelProvider(1));

		final TableViewerColumn size   = new TableViewerColumn(dataViewer, SWT.LEFT, 2);
		size.getColumn().setText("Size");
		size.getColumn().setWidth(requireToolbar?0:150);
		size.getColumn().setResizable(requireToolbar?false:true);
		size.setLabelProvider(new DataSetColumnLabelProvider(2));
			
		final TableViewerColumn dims   = new TableViewerColumn(dataViewer, SWT.LEFT, 3);
		dims.getColumn().setText("Dimensions");
		dims.getColumn().setWidth(requireToolbar?0:150);
		dims.getColumn().setResizable(requireToolbar?false:true);
		dims.setLabelProvider(new DataSetColumnLabelProvider(3));
		
		final TableViewerColumn shape   = new TableViewerColumn(dataViewer, SWT.LEFT, 4);
		shape.getColumn().setText("Shape");
		shape.getColumn().setWidth(requireToolbar?0:150);
		shape.getColumn().setResizable(requireToolbar?false:true);
		shape.setLabelProvider(new DataSetColumnLabelProvider(4));

	}
	
	private CellEditor[] createCellEditors(final TableViewer tableViewer) {
		CellEditor[] editors  = new CellEditor[1];
		TextCellEditor nameEd = new TextCellEditor(tableViewer.getTable());
		((Text)nameEd.getControl()).setTextLimit(60);
		// NOTE Must not add verify listener - it breaks things.
		editors[0] = nameEd;
		
		return editors;
	}
	
	private ICellModifier createModifier(final TableViewer tableViewer) {
		return new DoubleClickModifier(tableViewer) {
			@Override
			public boolean canModify(Object element, String property) {
				if (!enabled) return false;
				return (element instanceof ExpressionObject) && "Data".equalsIgnoreCase(property);
			}

			@Override
			public Object getValue(Object element, String property) {
				// NOTE: Only works for scannables right now which have one name
				final String expr = ((ExpressionObject)element).getExpression();
				return expr!=null ? expr : "";
			}
			@Override
			public void modify(Object item, String property, Object value) {
				
				try {
				    final ExpressionObject ob = (ExpressionObject)((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
					ob.setExpression((String)value);
					dataViewer.setChecked(ob, true);
					dataViewer.refresh();
					
					if (isDialogMode) {
						DataSetPlotView curView = (DataSetPlotView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DataSetPlotView.ID);
						if (curView!=null) {
							curView.addExpression(new ExpressionObject(curView, ob.getExpression(), ob.getMementoKey()));
						}
					}
					saveExpressions();
					
				} catch (Exception e) {
					logger.error("Cannot set "+property, e);
	
				} finally {
					setEnabled(false);
				}
			}
	    };
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		if (getViewSite()!=null) {
			IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
			createDimensionalActions(toolbarManager, true);
		}
	}

	private void createDimensionalActions(IContributionManager manager, boolean isToolbar) {
				
		// Warning this is horrible:
		final ActionContributionItem xyAction = new ActionContributionItem(new Action("XY Plot", SWT.TOGGLE) {
			@Override
			public void run() {
				setPlotMode(PlotMode.PM1D);
			}
		});
		xyAction.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(getClass(), "/icons/chart_curve.png"));
		xyAction.getAction().setToolTipText("XY Graph of Data, overlayed for multiple data.");
		
		final ActionContributionItem staggeredAction = new ActionContributionItem(new Action("XY Staggered in Z",  SWT.TOGGLE) {
			@Override
			public void run() {
				setPlotMode(PlotMode.PMSTACKED);
			}
		});		
		staggeredAction.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(getClass(), "/icons/chart_curve_staggered.png"));
		staggeredAction.getAction().setToolTipText("XY Graph of Data, staggered in Z for multiple data.");

		final ActionContributionItem xyzAction = new ActionContributionItem(new Action("XYZ",  SWT.TOGGLE) {
			@Override
			public void run() {
				setPlotMode(PlotMode.PM3D);
			}
		});		
		xyzAction.getAction().setImageDescriptor(ResourceManager.getImageDescriptor(getClass(), "/icons/chart_curve_3D.png"));
		xyzAction.getAction().setToolTipText("XYZ, X is the first chosen data and Z the last.");

		manager.add(new Separator());
		manager.add(xyAction);
		manager.add(staggeredAction);
		manager.add(xyzAction);
		manager.add(new Separator());
		
		if (getSite()==null) {
			manager.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), 
					null, 
					"uk.ac.diamond.scisoft.analysis.rcp.addExpression", 
					null, ResourceManager.getImageDescriptor(getClass(), "/icons/calculator_add.png"), null, null, "Add Expression", 
					null, "Adds an expression which can be plotted. Must be function of other data sets.",
					SWT.PUSH, null, false)));
			
			manager.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), 
                    null, 
                    "uk.ac.diamond.scisoft.analysis.rcp.deleteExpression", 
                    null, ResourceManager.getImageDescriptor(getClass(), "/icons/calculator_delete.png"), null, null, "Delete Expression", 
                    null, "Deletes an expression.",
                    SWT.PUSH, null, false)));
		}
		
		// Removed when part disposed.
		addPlotModeListener(new PlotModeListener() {			
			@Override
			public void plotChangePerformed(PlotMode plotMode) {
				updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, plotMode);
				updateSelection();
			}
		});
		
		if (isDialogMode) {
			DataSetPlotView curView = (DataSetPlotView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DataSetPlotView.ID);
			if (curView!=null) {
				setPlotMode(curView.getPlotMode());
			}
		}

		final Display dis = getSite()!=null ? getSite().getShell().getDisplay() : PlatformUI.getWorkbench().getDisplay();
		if (isToolbar) {
			dis.asyncExec(new Runnable() {
				@Override
				public void run() {
					updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, getPlotMode());
				}
			});
		} else {
			final MenuManager man = (MenuManager)manager;
			man.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					dis.asyncExec(new Runnable() {
						@Override
						public void run() {
					        updatePlotDimenionsSelected(xyAction, staggeredAction, xyzAction, getPlotMode());
						}
					});
				}
			});
		}
	}
	
	protected void updatePlotDimenionsSelected(ActionContributionItem xyAction, ActionContributionItem staggeredAction, ActionContributionItem xyzAction, PlotMode plotMode) {

		setActionSelected(xyAction, PlotMode.PM1D.equals(plotMode));
		setActionSelected(staggeredAction, PlotMode.PMSTACKED.equals(plotMode));
		setActionSelected(xyzAction, PlotMode.PM3D.equals(plotMode));
	}

	protected void setActionSelected(ActionContributionItem action, boolean selected) {
		if(action.getWidget() instanceof ToolItem) {
			((ToolItem)action.getWidget()).setSelection(selected);
		} else if (action.getWidget() instanceof MenuItem) {
			((MenuItem)action.getWidget()).setSelection(selected);
		}
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	/**
	 * Call to load a data file and display it.
	 * @param path
	 * @throws ScanFileHolderException 
	 */
	public void setFile(final String path,final IProgressMonitor monitor) throws Exception {
		
		monitor.beginTask("Opening file " + path, 10);
		monitor.worked(1);
		if (monitor.isCanceled()) return;

		final IMetadata          meta = LoaderFactory.getMetadata(path, new ProgressMonitorWrapper(monitor));
		final List<String>       sets = new ArrayList<String>(meta.getDataNames()); // Will be small list			 
		SortingUtils.removeIgnoredNames(sets, getIgnored());
		Collections.sort(sets, new SortNatural<String>(true));
		
		this.metaData = meta;
		if (monitor.isCanceled()) return;
		
		for (Iterator<Object> it = data.iterator(); it.hasNext();) {
			final Object ob = it.next();
			if (ob instanceof String) {
				it.remove();
			} else {
				((ExpressionObject)ob).clear();
			}
		}
		
		int pos = 0;
		for (String name : sets) {
			data.add(pos, name);
			pos++;
		}
		this.filePath = path;
		try {
		    readExpressions();
		} catch (Exception ne ) {
			logger.error("Cannot read expressions for file.", ne);
		}
		
		final Display dis = getSite()!=null ? getSite().getShell().getDisplay() : PlatformUI.getWorkbench().getDisplay();
		dis.asyncExec(new Runnable() {
			@Override
			public void run() {
				setPartName((new File(path)).getName());
				dataViewer.refresh();
			}
		});
	}

	private List<Object> selections = new ArrayList<Object>(7);
		
	/**
	 * @return Returns the selections.
	 */
	public List<Object> getSelections() {
		return selections;
	}

	private boolean checkStateLocked = false;
	
	@Override
	public void checkStateChanged(final CheckStateChangedEvent event) {
		
		if (checkStateLocked) return;
		try {
			checkStateLocked = true;
			if (selections==null) selections = new ArrayList<Object>(7);
			
		    
    		if (event!=null) {
    			final Object element = event.getElement();
				if (!event.getChecked()) {
					selections.remove(element);
				} else {
					// We only allow selection of one set not 1D
					final int    dims = getDimensionCount(element);
					if (dims!=1) { // Nothing else gets selected
						this.dataViewer.setAllChecked(false);
						this.dataViewer.setChecked(element, true);
						this.selections.clear();
					}
					if (!selections.contains(element)) {
						selections.add(element);
					}

				}
				
				// 1D takes precidence
				boolean is1D = false;
	    		// We check selections to ensure that only n*1D or 1*2D+ are selected
	    		for (Object set : selections) if (getDimensionCount(set)==1)	is1D=true;
				
	    		if (is1D) for (Iterator<Object> it = selections.iterator(); it.hasNext();) {
					Object set = it.next();

	    			if (getDimensionCount(set)!=1) {
	    				this.dataViewer.setChecked(set, false);
	    				it.remove();
	    			}
	    		}

			} else {
				selections.clear();
			}
    		

			updateSelection();
			this.dataViewer.refresh();
			
		} finally {
			checkStateLocked = false;
		}
	}

	private void updateSelection() {

		if (selections==null) return;

		fireSelectionListeners(selections);
		
		IProgressService service =  getSite()!=null 
		                         ? (IProgressService)getSite().getService(IProgressService.class)
		                         : (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);
		

		if (isPlottingRequired) try {
			
		   if (selections.size()==1 && getDimensionCount(selections.get(0))!=1) {
//			   SliceDialog.showSliceDialog(selections.get(0).toString(), 
//					                       this.filePath, 
//					                       metaData.getDataShapes().get(selections.get(0).toString()), 
//					                       this.plotWindow);
		       return;
		   }
			
		   service.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					monitor.beginTask("Updating selected DataSets", 100);

					if (monitor.isCanceled()) return;
					plot1D(monitor);
 
					monitor.done();
				}


			});
		} catch (Exception e) {
			logger.error("Cannot create plot required.", e);
		} 

	}

	/**
	 * Designed to be called on a thread to plot the current 
	 * selected data in 1D.
	 * @param monitor
	 */
	protected void plot1D(final IProgressMonitor monitor) {
		
		if (selections==null || selections.isEmpty()) return;
		Dataset x = getDataSet(selections.get(0), monitor);
		if (x==null) return;
		monitor.worked(1);

		final List<Dataset> ys = new ArrayList<Dataset>(3);
		for (int i = 1; i < selections.size(); i++) {
			ys.add(getDataSet(selections.get(i), monitor));
			if (monitor.isCanceled()) return;
			monitor.worked(1);
		}

		if (ys.isEmpty()) {
			ys.add(x);
			x = DatasetFactory.createRange(DoubleDataset.class, ys.get(0).getSize());
			x.setName("Index");
		}

		PlotUtils.create1DPlot(x,ys,getPlotMode(),getPlotWindow(),monitor);
	}

	private List<ISelectionChangedListener> listeners;
	
	/**
	 * Call to be notified of data set collections being made.
	 * The selections returned are a StructuredSelection with a list
	 * of objects some are Strings for the data set name and
	 * others are ExpressionObject if the user created expressions.
	 * 
	 * NOTE: The listener is NOT called on the GUI thread.
	 * 
	 * @param l
	 */
	public void addSelectionListener(final ISelectionChangedListener l){
		if (listeners==null) listeners = new ArrayList<ISelectionChangedListener>(7);
		listeners.add(l);
	}
	
	public void removeSelectionListener(final ISelectionChangedListener l){
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	private void fireSelectionListeners(List<Object> selections) {
		if (listeners==null) return;
		final SelectionChangedEvent event = new SelectionChangedEvent(this.dataViewer, new StructuredSelection(selections));
		for (ISelectionChangedListener l : listeners) l.selectionChanged(event);
	}

	
	private PlotMode plotMode = PlotMode.PM1D;

	public GuiPlotMode getGuiPlotMode() {
		return plotMode.getGuiPlotMode();
	}

	public PlotMode getPlotMode() {
		return plotMode;
	}

	/**
	 * @param pm The plotMode to set.
	 */
	public void setPlotMode(PlotMode pm) {
		plotMode = pm;
		if (plotModeListeners!=null) {
			for (PlotModeListener l : plotModeListeners) {
				l.plotChangePerformed(pm);
			}
		}
	}
	
	private List<PlotModeListener> plotModeListeners;

	private AbstractPlotWindow plotWindow;
	protected void addPlotModeListener(PlotModeListener l) {
		if (plotModeListeners==null) plotModeListeners = new ArrayList<PlotModeListener>(7);
		plotModeListeners.add(l);
	}
	
	protected void removePlotModeListener(PlotModeListener l) {
		if (plotModeListeners==null) return;
		plotModeListeners.remove(l);
	}
	
	protected interface PlotModeListener extends EventListener {
		void plotChangePerformed(PlotMode plotMode);
	}

	private List<String> getStringSelections(List<Object> selections) {
		
		final List<String> ret = new ArrayList<String>(selections.size());
		for (Object sel : selections) {
			if (sel instanceof String) ret.add((String)sel);
		}
		return ret;
	}

	
	@Override
	public Dataset getDataSet(String name, final IMonitor monitor) {
		
		try {
			if (providerDeligate!=null) {
				return (Dataset)providerDeligate.getDataSet(name, monitor);
			}
			if (this.filePath==null) return null;
			return (Dataset)LoaderFactory.getDataSet(this.filePath, name, monitor);
		} catch (IllegalArgumentException ie) {
			return null;
		} catch (Exception e) {
			logger.error("Cannot get data set "+name+" from "+filePath+". Currently expressions can only contain existing Data Sets.", e);
			return null;
		}
	}

	@Override
	public boolean isDataSetName(String name, IMonitor monitor) {
		final List<String> allNames = getStringSelections(data);
		return allNames.contains(name);
	}

	private Dataset getDataSet(Object object, final IProgressMonitor monitor) {
		if (object instanceof ExpressionObject) {
			try {
				return ((ExpressionObject)object).getDataSet(monitor);
			} catch (Exception e) {
				// valid, user can enter an invalid expression. In this case
				// it colours red but does not stop them from using the view.
				return DatasetFactory.zeros(DoubleDataset.class, null);
			}
		}
		return getDataSet(object, monitor);
	}

	private class DataSetColumnLabelProvider extends ColumnLabelProvider {
		
		private Color RED   = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
		private Color BLUE  = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);
		private Color BLACK = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
		private int columnIndex;
		DataSetColumnLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}
		
		@Override
		public String getText(Object element) {
			switch (columnIndex) {
			case 0:
				return element.toString();
			case 1:
				if (selections!=null&&!selections.isEmpty()) {
					if (selections.size()>1) {
						if (selections.contains(element)) {
							if (selections.indexOf(element)==0) {
								return "X";
							}
							if (selections.size()>2) {
								return "Y"+selections.indexOf(element);
							}
							return "Y";
						}
					} if (selections.size()==1 && selections.contains(element)) {
						return "Y";
					}
				}
                return "";
			case 2:
				if (element instanceof String || requireToolbar) {
					final String name = element.toString();
					if (metaData.getDataSizes()==null) {
						final IDataset set = getDataSet(name, (IMonitor)null);
						if (set!=null) {
							return set.getSize()+"";
						}
					    return "Unknown";
						
					}
					return metaData.getDataSizes().get(name)+"";
				} 
				return ((ExpressionObject)element).getSize(new NullProgressMonitor())+"";
			case 3:
				return getDimensionCount(element)+"";
			case 4:
				if (element instanceof String || requireToolbar) {
					final String name = element.toString();
					if (metaData.getDataShapes()==null) {
						final IDataset set = getDataSet(name, (IMonitor)null);
						if (set!=null) {
							return Arrays.toString(set.getShape());
						}
					    return "Unknown";
						
					}
					return Arrays.toString(metaData.getDataShapes().get(name));
				} 
				return "["+((ExpressionObject)element).getSize(new NullProgressMonitor())+"]";
			default:
				return element.toString();
			}
		}
		
	    @Override
		public String getToolTipText(Object element) {
            return "With one data set selected it is plotted versus index of data with two or more the first set selected is the x-axis.";
	    }
	    
	    @Override
	    public Color getForeground(Object element) {
	    		    	
			switch (columnIndex) {
			case 0:
				if (element instanceof ExpressionObject) {
					final ExpressionObject o = (ExpressionObject)element;
					return o.isValid(new NullProgressMonitor()) ? BLUE : RED;
				}
				return BLACK;
			default:
				return BLACK;
			}
	    }
	}
	
	@Override
	public void dispose() {
		
		datasetSelection = null;
		if (listeners!=null) listeners.clear();
		if (data != null)       this.data.clear();
		if (plotModeListeners!=null) plotModeListeners.clear();
		dataViewer.removeCheckStateListener(this);
		super.dispose();		
	}

	public int getDimensionCount(Object element) {
		
		if (element instanceof String || requireToolbar) {
			final String name = element.toString();
			if (metaData.getDataShapes()==null || metaData.getDataShapes().get(name) == null) {
				final IDataset set = getDataSet(name, (IMonitor)null);
				if (set!=null) {
					return set.getShape().length;
				}
			    return 1;
				
			}
			final int[] shape = metaData.getDataShapes().get(name);
			return shape.length;
		} 
		return 1;
	}

	public void addExpression() {
		final ExpressionObject expression = new ExpressionObject(this);
		data.add(expression);
		dataViewer.refresh();
		
		((DoubleClickModifier)dataViewer.getCellModifier()).setEnabled(true);		
		dataViewer.editElement(expression, 0);
		
	}

	protected void addExpression(ExpressionObject expressionObject) {
		data.add(expressionObject);
		dataViewer.refresh();
	}

	public void deleteExpression() {
		final Object sel = ((IStructuredSelection)dataViewer.getSelection()).getFirstElement();
		if (sel==null|| !(sel instanceof ExpressionObject)) return;
		
		data.remove(sel);
		dataViewer.refresh();
		saveExpressions();

	}
	
	
	public static List<Pattern> getIgnored() {
		
		if (Activator.getDefault().getPreferenceStore().getBoolean(DeprecatedPreferenceConstants.IGNORE_DATASET_FILTERS)) return null;
		
		final List<Pattern> patterns    = new ArrayList<Pattern>(5);

		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.data.set.filter");
		
		for (IConfigurationElement e : config) {
			final String pattern     = e.getAttribute("regularExpression");
			patterns.add(Pattern.compile(pattern));
		}
		
		return patterns;
	}
	
	/**
	 * Used when the view is being controlled from a Dialog.
	 * @param meta
	 */
	public void setMetaData(final IMetadata meta) {
		
		this.data.clear();
		this.data.addAll(meta.getDataNames());
		if (meta.getUserObjects()!=null) this.data.addAll(meta.getUserObjects());
		this.metaData = meta;

		try {
		    readExpressions();
		} catch (Exception ne ) {
			logger.error("Cannot read expressions for file.", ne);
		}
	}

	public void refresh() {
		this.dataViewer.refresh();
	}

	/**
	 * @return expression can be null or empty
	 */
	public List<Object> getExpressions(DataSetProvider prov) {
		if (data == null) return null;
		final List<Object> exprs = new ArrayList<Object>(3);
		for (Object o : data) {
			if (o instanceof ExpressionObject) {
				ExpressionObject e = (ExpressionObject)o;
				e = new ExpressionObject(prov, e.getExpression(), e.getMementoKey());
				exprs.add(e);
			}
		}
		return exprs;
	}

	/**
	 * If you call this method the plots will go to the plotWindow specified
	 * instead of the default which is plot1
	 * 
	 * @param plotWindow
	 */
	public void setPlotWindow(AbstractPlotWindow plotWindow) {
		this.plotWindow = plotWindow;
	}

	protected AbstractPlotWindow getPlotWindow() {
		
		if (plotWindow!=null) return plotWindow;
		
		try {
			PlotView plotView = (PlotView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("uk.ac.diamond.scisoft.analysis.rcp.plotViewNT1");
			return plotView.getPlotWindow();
		} catch (PartInitException e) {
			logger.error("Cannot open uk.ac.diamond.scisoft.analysis.rcp.plotViewNT1", e);
			return null;
		}

	}
	
	public String getFileName() {
		if (filePath!=null) return (new File(filePath)).getName();
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private Dataset datasetSelection = null;
	/**
	 * Thread safe
	 * @param name
	 */
	public Dataset setDatasetSelected(final String name, final boolean clearOthers) {
		
		datasetSelection = null;
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if (clearOthers) {
					DataSetPlotView.this.dataViewer.setAllChecked(false);
					DataSetPlotView.this.checkStateChanged(null);
				}
				DataSetPlotView.this.dataViewer.setCheckedElements(new Object[]{name});
				DataSetPlotView.this.checkStateChanged(new CheckStateChangedEvent(DataSetPlotView.this.dataViewer, name, true));
				datasetSelection = DataSetPlotView.this.getDataSet(name, (IMonitor)null);
			}
		});
		
		return datasetSelection;
	}

	public IMetadata getMetaData() {
		return metaData;
	}

}
