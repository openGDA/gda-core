/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.liveplot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IFileLoader;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.plots.ScanTreeItem;
import gda.plots.SingleScanLine;
import gda.rcp.GDAClientActivator;
import gda.scan.AxisSpec;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.NexusLoader;
import uk.ac.diamond.scisoft.analysis.io.SRSLoader;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiUpdate;
import uk.ac.diamond.scisoft.analysis.plotserver.OneDDataFilePlotDefinition;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.gda.preferences.PreferenceConstants;

@SuppressWarnings("deprecation")
public class LivePlotView extends ViewPart implements IScanDataPointObserver {
	private static final String MEMENTO_GROUP = "LivePlotView";

	/**
	 * The primary ID of the view (one per class)
	 */
	public static final String ID = "uk.ac.gda.client.liveplotview";
	protected static int secondaryIdSuffix = 1;
	private static final Logger logger = LoggerFactory.getLogger(LivePlotView.class);
	protected LivePlotComposite xyPlot;
	private FileDialog fileDialog;
	private IAction actionConnect;
	private Boolean connected = false;
	private IPreferenceStore preferenceStore;

	/**
	 * Folder into which the data for this view is to be stored Use getArchiveFolder as __archiveFolder is not
	 * initialised until first call to getArchiveFolder
	 */
	private String __archiveFolder;

	/**
	 * IPath to file that holding the memento that lists the different scans saved in the archive Use
	 * getMementoFileIPath as __mementoFileIPath is not initialised until first call to getArchiveFolder
	 */
	private IPath __mementoFileIPath;

	static {
		// We need to activate the SciSoftRCP bundle as that sets up the PlotServer
		AnalysisRCPActivator.getDefault();
		PlotServerProvider.getPlotServer().addIObserver(new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (source instanceof PlotServer) {
					if (arg instanceof GuiUpdate) {
						GuiUpdate update = (GuiUpdate) arg;
						if (update.getGuiName().equals(ID)) {
							final GuiBean guiData = update.getGuiData();
							if (guiData.containsKey(GuiParameters.ONEDFILE)) {
								Object obj = guiData.get(GuiParameters.ONEDFILE);
								if (obj instanceof OneDDataFilePlotDefinition) {
									final OneDDataFilePlotDefinition data = (OneDDataFilePlotDefinition) obj;
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											try {
												final IWorkbenchPage page = PlatformUI.getWorkbench()
														.getActiveWorkbenchWindow().getActivePage();
												LivePlotView part = (LivePlotView) page.findView(LivePlotView.ID);
												if (part == null) {
													part = (LivePlotView) page.showView(LivePlotView.ID);
												}
												part.openFile(data);
											} catch (Exception e) {
												logger.error("Error responding to IDE_ACTION");
											}
										}
									});
								}
							}
						}
					}
				}
			}
		});

	}

	/**
	 * default constructor providing default scan_plot_config in this bundle.
	 */
	public LivePlotView() {
		super();
		preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
	}

	/**
	 * @param point
	 */
	private void addData(IScanDataPoint point) {
		xyPlot.addData(point);
	}

	private IPath getArchiveFileIPath() {
		if (__mementoFileIPath == null) {
			IPath mementoStoreIPath = GDAClientActivator.getDefault().getStateLocation();
			String Id = getViewSite().getSecondaryId();
			if (Id == null)
				Id = getID();
			__mementoFileIPath = mementoStoreIPath.append(Id);
		}
		return __mementoFileIPath;
	}

	private String getArchiveFilePath() {
		IPath mementoFileIPath = getArchiveFileIPath();
		return mementoFileIPath.toFile().getAbsolutePath();
	}

	private String getArchiveFolder() {
		if (__archiveFolder == null) {
			String archiveFolder = getArchiveFilePath() + "_livearchive";
			File storeFolderFile = new File(archiveFolder);
			storeFolderFile.mkdirs();
			this.__archiveFolder = archiveFolder;
		}
		return __archiveFolder;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		xyPlot = new LivePlotComposite(this, parent, SWT.NONE, getArchiveFolder());
		xyPlot.setAutoHideNewScan(false);
		xyPlot.setAutoHideLastScan(preferenceStore.getBoolean(PreferenceConstants.GDA_CLIENT_PLOT_AUTOHIDE_LAST_SCAN));
		xyPlot.showLegend(true);
		getViewSite().getActionBars().getToolBarManager();

		List<IAction> actions = new Vector<IAction>();
		{
			actionConnect = new Action("", IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					if (isConnected()) {
						disconnect();
					} else {
						connect();
					}
				}
			};
			actions.add(actionConnect);
		}
		{
			IAction action = new Action("", IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					setAutoHideLastScan(!getAutoHideLastScan());
					boolean autoHideLastScan = getAutoHideLastScan();
					preferenceStore.setValue(PreferenceConstants.GDA_CLIENT_PLOT_AUTOHIDE_LAST_SCAN, autoHideLastScan);
					this.setChecked(autoHideLastScan);
				}
			};
			action.setChecked(xyPlot.getAutoHideLastScan());
			action.setToolTipText("Auto hide last scan");
			action.setImageDescriptor(gda.rcp.GDAClientActivator.getImageDescriptor("icons/chart_curve_single.png"));
			actions.add(action);
		}
		{
			IAction action = new Action("", IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					xyPlot.showLegend(!xyPlot.getShowLegend());
					this.setChecked(xyPlot.getShowLegend());
				}
			};
			action.setChecked(xyPlot.getShowLegend());
			action.setToolTipText("Show/Hide legend");
			action.setImageDescriptor(gda.rcp.GDAClientActivator.getImageDescriptor("icons/chart_curve_legend.png"));
			actions.add(action);
		}

		{
			IAction action = new Action("", IAction.AS_PUSH_BUTTON) {
				@Override
				public void run() {
					try {
						openFile(null, null, null);
					} catch (Throwable e) {
						logger.error("Error opening a file", e);
					}
				}
			};
			action.setChecked(true);
			action.setToolTipText("Open File");
			action.setImageDescriptor(GDAClientActivator.getImageDescriptor("icons/folder_page.png"));
			actions.add(action);
		}

		{
			IAction action = new Action("", IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					xyPlot.setLog10(!xyPlot.isLog10());
					this.setChecked(xyPlot.isLog10());
				}
			};
			action.setChecked(xyPlot.isLog10());
			action.setToolTipText("Log10 YAxis");
			action.setText("Log10");
			actions.add(action);
		}

		xyPlot.createAndRegisterPlotActions(parent, getViewSite().getActionBars(), getPartName(), actions);
		xyPlot.createScriptingConnection(getPartName());

		try {
			/**
			 * Attempt to restore the view from the memento stored when view was last disposed
			 */
			File file = getArchiveFileIPath().toFile();
			if (file.exists()) {
				FileReader fileReader = new FileReader(file);
				XMLMemento memento = XMLMemento.createReadRoot(fileReader);
				xyPlot.initFromMemento(memento, getArchiveFolder());
			}
		} catch (Exception e) {
			logger.error("Error restoring view to previous state", e);
		}

		connect();
		xyPlot.initContextMenu(getSite(), getSite().getWorkbenchWindow(), new XYPlotActionGroup(getSite()
				.getWorkbenchWindow(), xyPlot));
		getSite().setSelectionProvider(xyPlot.getTreeViewer());
	}

	private void openFile(OneDDataFilePlotDefinition data) throws Exception {
		Vector<String> xyDatasetNames = new Vector<String>();
		xyDatasetNames.add(data.x_axis);
		for (String y_axis : data.y_axes) {
			xyDatasetNames.add(y_axis);
		}
		openFile(data.url, xyDatasetNames, data.yAxesMap);
	}

	private List<String> getXYDataSetNames(Shell shell, String[] possibleXYDataSetNames) {
		ListDialog ld = new ListDialog(shell);
		ld.setAddCancelButton(true);
		ld.setContentProvider(new ArrayContentProvider());
		ld.setLabelProvider(new LabelProvider());
		ld.setInput(possibleXYDataSetNames);
		ld.setInitialSelections(new Object[] { possibleXYDataSetNames[0] });
		ld.setTitle("Select the dataset to use as the x axis");
		ld.open();
		Object[] xSel = ld.getResult();
		if (xSel == null)
			return null;
		List<String> xyDataSetNames = new Vector<String>();
		xyDataSetNames.add((String) xSel[0]);
		if (possibleXYDataSetNames.length == 1)
			return xyDataSetNames;
		ListSelectionDialog lsd = new ListSelectionDialog(shell, possibleXYDataSetNames, new ArrayContentProvider(),
				new LabelProvider(), "");
		lsd.setInitialSelections(new Object[] { possibleXYDataSetNames[1] });
		lsd.setTitle("Select the datasets to use as the y axes");
		lsd.open();

		Object[] ySels = lsd.getResult();
		if (ySels == null)
			return null;
		for (Object ySel : ySels) {
			xyDataSetNames.add((String) ySel);
		}
		return xyDataSetNames;
	}

	public void openFile(String path, List<String> xyDataSetNames, Map<String, String> yAxesMap) throws Exception {
		if (path == null) {
			if (fileDialog == null) {
				fileDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
				String[] filterNames = new String[] { "All Files (*)" };
				String[] filterExtensions = new String[] { "*" };
				fileDialog.setFilterPath(PathConstructor.createFromDefaultProperty());
				fileDialog.setFilterNames(filterNames);
				fileDialog.setFilterExtensions(filterExtensions);
			}
			path = fileDialog.open();
			if (path == null)
				return;
		}

		// try to load as Srs
		BufferedReader bfr = new BufferedReader(new FileReader(path));
		String line = bfr.readLine();
		bfr.close();
		String hdf = new String(new char[] { '\ufffd', 'H', 'D', 'F' });
		IFileLoader fileLoader = null;
		Shell shell = getSite().getShell();
		if (line.startsWith(hdf)) {
			if (xyDataSetNames == null) {
				List<String> possibleXYDataSetNames = NexusLoader.getDatasetNames(path, null);
				xyDataSetNames = getXYDataSetNames(shell, possibleXYDataSetNames.toArray(new String[] {}));
				if (xyDataSetNames == null)
					return;
			}
			fileLoader = new NexusLoader(path, xyDataSetNames);
		} else {
			try {
				fileLoader = LoaderFactory.getLoader(SRSLoader.class, path);
			} catch (Exception e) {
				// Fall through
			}
		}
		if (fileLoader != null) {
			IDataHolder dataHolder = fileLoader.loadFile();
			if (xyDataSetNames == null) {
				xyDataSetNames = getXYDataSetNames(shell, dataHolder.getNames());
				if (xyDataSetNames == null)
					return;
			}
			DoubleDataset xData = (DoubleDataset) dataHolder.getDataset(xyDataSetNames.get(0));
			for (int i = 1; i < xyDataSetNames.size(); i++) {
				String xyDataSetName = xyDataSetNames.get(i);
				DoubleDataset yData = (DoubleDataset) dataHolder.getDataset(xyDataSetName);
				AxisSpec axisSpec = null;
				if (yAxesMap != null) {
					String yAxisName = yAxesMap.get(xyDataSetName);
					if (yAxisName != null)
						axisSpec = new AxisSpec(yAxisName);
				}
				int scanNumberFromPath = deriveScanNumberFromPath(path);
				xyPlot.addData(scanNumberFromPath, path, xyDataSetName + "/" + xyDataSetNames.get(0), xData, yData,
						true, true, axisSpec);
			}
		} else {
			logger.warn("Unrecognized file type - " + path);
		}
	}

	private int deriveScanNumberFromPath(String path) {
		String filename = FilenameUtils.getName(path);
		Matcher matcher = Pattern.compile("(\\d+)").matcher(filename);
		matcher.find();
		return Integer.parseInt(matcher.group(0));
	}

	public void addData(int scanIdentifier, String fileName, String label, DoubleDataset xData, DoubleDataset yData,
			boolean visible, boolean reload, AxisSpec yAxisName) {
		xyPlot.addData(scanIdentifier, fileName, label, xData, yData, visible, reload, yAxisName);
	}

	/**
	 * Call this method to get a unique secondary ID to use when opening multiple LivePlotViews
	 */
	public static String getUniqueSecondaryId() {
		return String.valueOf(++secondaryIdSuffix);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		disconnect();
		if (site.getSecondaryId() == null) {
			setPartName("Scan Plot 1");
		} else {
			setPartName("Scan Plot " + site.getSecondaryId());
		}
	}

	/**
	 * Writes to a memento and a set of archive files in a folder a copy of the state of the xy lines displayed in the
	 * view
	 *
	 * @param memento
	 * @param archiveFolder
	 */
	private void saveState(IMemento memento, String archiveFolder) {
		boolean wasConnected = isConnected();
		try {
			disconnect();
			Thread.sleep(500); // wait for any processing of scan data points to complete - hack
			if (xyPlot != null)
				xyPlot.saveState(memento, archiveFolder);
		} catch (InterruptedException e) {
			logger.error("saveState interrupted", e);
		} finally {
			if (wasConnected) {
				connect();
			}
		}
	}

	@Override
	public void dispose() {
		try {
			/**
			 * To save the state of the view to a memento we need to do it in the dispose method as the normal memento
			 * system along saves the state of the application when it is closed. If a view is closed at the time the
			 * application closes then the state of the view is not saved. By doing the save in dispose the user can
			 * close and re-open the view without losing the data displayed.
			 */
			XMLMemento memento = XMLMemento.createWriteRoot(MEMENTO_GROUP);
			saveState(memento, getArchiveFolder());
			memento.save(new FileWriter(getArchiveFilePath()));
		} catch (IOException e) {
			logger.error("Error saving state of  view", e);
		}
		super.dispose();

		xyPlot.dispose();
		connect();
	}

	@Override
	public void setFocus() {
		xyPlot.setFocus();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class)
			return this.xyPlot.getPlottingSystem();
		return super.getAdapter(clazz);
	}

	/**
	 * @return Primary ID of view
	 */
	public static String getID() {
		return ID;
	}

	/**
	 * @param value
	 *            True if new scans are to be hidden automatically
	 */
	public void setAutoHideLastScan(Boolean value) {
		xyPlot.setAutoHideLastScan(value);
	}

	/**
	 * @return true if last scans are made invisible
	 */
	public Boolean getAutoHideLastScan() {
		return xyPlot.getAutoHideLastScan();
	}

	/**
	 * Hide all scans
	 */

	public void hideAll() {
		xyPlot.hideAll();
	}

	/**
	 * Clear the graph
	 */
	public void clearGraph() {
		xyPlot.clearGraph();
	}

	public void setHideOldestScan(Boolean value) {
		xyPlot.setHideOldestScan(value);
	}

	public boolean getHideOldestScan() {
		return xyPlot.getHideOldestScan();
	}

	public void connect() {
		setConnect(true);
	}

	public void disconnect() {
		setConnect(false);
	}

	/**
	 * @param connect
	 *            <code>true</code> if this plot should connect and listen for scan data points
	 */
	private void setConnect(boolean connect) {
		try {
			if (connect)
				InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
			// Do not use UIScanDataPointEventService as it can only hold a certain number of points so is unreliable
			// UIScanDataPointEventService.getInstance().addScanPlotListener(this);
			else
				InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
			// UIScanDataPointEventService.getInstance().removeScanPlotListener(this);

			connected = connect;
			if (actionConnect != null) {
				actionConnect.setChecked(connected);
				if (connected) {
					actionConnect.setToolTipText("Disconnect: do not listen for new scans");
					actionConnect.setImageDescriptor(gda.rcp.GDAClientActivator
							.getImageDescriptor("icons/control_pause_blue.png"));
				} else {
					actionConnect.setToolTipText("Connect: listen for data from new scans");
					actionConnect.setImageDescriptor(gda.rcp.GDAClientActivator
							.getImageDescriptor("icons/control_play_blue.png"));
				}
			}
		} catch (Exception e) {
			// logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * @return <code>true</code> if the plot is currently listening for scan data points
	 */
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void update(Object source, Object arg) {
		if (connected && arg instanceof IScanDataPoint)
			addData((IScanDataPoint) arg);
	}
}

class XYPlotActionGroup extends ActionGroup {
	private IWorkbenchWindow window;
	private LivePlotComposite xyplot;

	XYPlotActionGroup(IWorkbenchWindow window, LivePlotComposite xyplot) {
		this.window = window;
		this.xyplot = xyplot;
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean anyResourceSelected = !selection.isEmpty();
		if (anyResourceSelected) {
			addOpenScanPairGroup(menu, selection);
			menu.add(new RemoveSelectedItemsPlot(window, xyplot, selection.toArray()));
		}
	}

	/**
	 * Adds the Open in New Window action to the context menu.
	 *
	 * @param menu
	 *            the context menu
	 * @param selection
	 *            the current selection
	 */
	private void addOpenScanPairGroup(IMenuManager menu, IStructuredSelection selection) {
		// Only supported if exactly one container (i.e open project or folder) is selected.
		if (selection.size() != 1) {
			return;
		}
		Object element = selection.getFirstElement();
		String scanIdentifier = null;
		if (element instanceof ScanTreeItem) {
			scanIdentifier = ((ScanTreeItem) element).getCurrentFilename();
		} else if (element instanceof SingleScanLine) {
			scanIdentifier = ((SingleScanLine) element).getCurrentFilename();
		}
		if (scanIdentifier != null) {
			File f = new File(scanIdentifier);
			if (f.isFile()) {
				menu.add(new OpenScanFile(window, f.getAbsolutePath()));
			}
		}
	}

}

class OpenScanFile extends Action implements ActionFactory.IWorkbenchAction {
	private static final Logger logger = LoggerFactory.getLogger(OpenScanFile.class);
	/**
	 * The workbench window; or <code>null</code> if this action has been <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;
	private String absFilePath;

	public OpenScanFile(IWorkbenchWindow window, String absFilePath) {
		super("Open " + absFilePath);
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		this.absFilePath = absFilePath;
		setToolTipText(absFilePath);
	}

	/**
	 * The implementation of this <code>IAction</code> method opens a new window. The initial perspective for the new
	 * window will be the same type as the active perspective in the window which this action is running in.
	 */
	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		try {
			// (new OpenDataFileAction(true)).openViewForFile(new File(absFilePath));
			// Now that Nexus files have their own editor simple use the editor associated with the file
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(new File(absFilePath).toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditorOnFileStore(page, fileStore);
		} catch (Exception e) {
			logger.warn("Cannot open file", e);
		}
	}

	@Override
	public void dispose() {
		workbenchWindow = null;
	}
}

class RemoveScanFileFromPlot extends Action implements ActionFactory.IWorkbenchAction {
	/**
	 * The workbench window; or <code>null</code> if this action has been <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;
	private String absFilePath;
	private LivePlotComposite xyplot;

	public RemoveScanFileFromPlot(IWorkbenchWindow window, LivePlotComposite xyplot, String absFilePath) {
		super("Remove");
		if (window == null)
			throw new IllegalArgumentException();
		this.workbenchWindow = window;
		this.absFilePath = absFilePath;
		this.xyplot = xyplot;
		setToolTipText(absFilePath);
	}

	/**
	 * The implementation of this <code>IAction</code> method opens a new window. The initial perspective for the new
	 * window will be the same type as the active perspective in the window which this action is running in.
	 */
	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		xyplot.removeScanGroup(absFilePath);
	}

	@Override
	public void dispose() {
		workbenchWindow = null;
	}
}

class RemoveSelectedItemsPlot extends Action implements ActionFactory.IWorkbenchAction {
	/**
	 * The workbench window; or <code>null</code> if this action has been <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;
	private Object[] selectedItems;
	private LivePlotComposite xyplot;

	public RemoveSelectedItemsPlot(IWorkbenchWindow window, LivePlotComposite xyplot, Object[] selectedItems) {
		super("Remove");
		if (window == null)
			throw new IllegalArgumentException();
		this.workbenchWindow = window;
		this.selectedItems = selectedItems;
		this.xyplot = xyplot;
		setToolTipText("Remove items from the plot");
	}

	/**
	 * The implementation of this <code>IAction</code> method opens a new window. The initial perspective for the new
	 * window will be the same type as the active perspective in the window which this action is running in.
	 */
	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		xyplot.removeScanTreeObjects(selectedItems);
	}

	@Override
	public void dispose() {
		workbenchWindow = null;
	}
}
