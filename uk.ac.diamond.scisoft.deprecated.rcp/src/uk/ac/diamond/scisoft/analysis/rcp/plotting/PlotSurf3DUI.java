/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;

import gda.observable.IObserver;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.core.ScaleType;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.impl.SurfPlotStyles;
import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.ColorMappingUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.preference.DeprecatedPreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.DataWindowView;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 * A very general UI for 2D surface plotting using SWT / Eclipse RCP
 */
@Deprecated
public class PlotSurf3DUI extends AbstractPlotUI implements IObserver {

	private IWorkbenchPage page;
	private AxisValues xAxis = null;
	private AxisValues yAxis = null;
	private AxisValues zAxis = null;
	private AbstractPlotWindow plotWindow = null;
	private DataSetPlotter mainPlotter;
	private Composite parent;
	private HistogramView histogramView;
	private DataWindowView dataWindowView;
	private List<IObserver> observers = 
		Collections.synchronizedList(new LinkedList<IObserver>());
	private Action boundingBox;
	private Action xCoordGrid;
	private Action yCoordGrid;
	private Action zCoordGrid;
	private Action saveGraph;
	private Action copyGraph;
	private Action printGraph;
	private Action displayFilled;
	private Action displayWireframe;
	private Action displayLinegraph;
	private Action displayPoint;
	private Action resetView;
	private Action zAxisScaleLinear;
	private Action zAxisScaleLog;

	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");
	private String id;
	
	private static final Logger logger = LoggerFactory
	.getLogger(PlotSurf3DUI.class);
	
	/**
	 * @param window 
	 * @param plotter
	 * @param parent
	 * @param page 
	 * @param id 
	 */
	public PlotSurf3DUI(AbstractPlotWindow window,
			 final DataSetPlotter plotter,
			 Composite parent, 
			 IWorkbenchPage page, 
			 IActionBars bars,
			 String id) {

		this.parent = parent;
		this.page = page;
		this.plotWindow = window;
		this.id = id;
		xAxis = new AxisValues();
		yAxis = new AxisValues();
		zAxis = new AxisValues();
		this.mainPlotter = plotter;
		buildMenuActions(bars.getMenuManager(), plotter); 
		buildToolActions(bars.getToolBarManager(), 
				         plotter, parent.getShell());								 
		
		if(getDefaultPlottingSystemChoice()==DeprecatedPreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
			try {
				 histogramView = (HistogramView) page.showView("uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView",
						this.id, IWorkbenchPage.VIEW_CREATE);
				plotWindow.addIObserver(histogramView);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			try {
				dataWindowView = (DataWindowView) page.showView("uk.ac.diamond.scisoft.analysis.rcp.views.DataWindowView",
						this.id, IWorkbenchPage.VIEW_CREATE);
				if (histogramView != null)
					histogramView.addIObserver(dataWindowView);
				dataWindowView.addIObserver(this);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}else if (getDefaultPlottingSystemChoice()==DeprecatedPreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
			try {
				dataWindowView = (DataWindowView) page.showView("uk.ac.diamond.scisoft.analysis.rcp.views.DataWindowView",
						this.id, IWorkbenchPage.VIEW_CREATE);
				dataWindowView.setFocus();
				plotWindow.addIObserver(dataWindowView);
				dataWindowView.addIObserver(this);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		if (dataWindowView == null) {
			logger.warn("Cannot find data window");
		}	
	}	

	
	private void buildToolActions(IToolBarManager manager, final DataSetPlotter plotter,
			  final Shell shell)
	{
		resetView = new Action() {
			@Override
			public void run()
			{
				mainPlotter.resetView();
				mainPlotter.refresh(false);
			}
		};
		resetView.setText("Reset view");
		resetView.setToolTipText("Reset panning and zooming");
		resetView.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/house_go.png"));
		
		boundingBox = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run()
			{
				plotter.enableBoundingBox(boundingBox.isChecked());
				plotter.refresh(false);
			}
		};
		boundingBox.setText("Bounding box on/off");
		boundingBox.setToolTipText("Bounding box on/off");
		boundingBox.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/box.png"));		
		boundingBox.setChecked(true);
		xCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), 
										 yCoordGrid.isChecked(),zCoordGrid.isChecked());
				plotter.refresh(false);
			}
		};
		xCoordGrid.setChecked(true);
		xCoordGrid.setText("X grid lines ON/OFF");
		xCoordGrid.setToolTipText("Toggle x axis grid lines on/off");
		xCoordGrid.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/text_align_justify_rot.png"));
		yCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), 
										 yCoordGrid.isChecked(),zCoordGrid.isChecked());
				plotter.refresh(false);
				
			}
		};
		yCoordGrid.setChecked(true);
		yCoordGrid.setText("Y grid lines ON/OFF");
		yCoordGrid.setToolTipText("Toggle y axis grid lines on/off");
		yCoordGrid.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/text_align_justify.png"));		

		zCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), 
										 yCoordGrid.isChecked(),zCoordGrid.isChecked());
				plotter.refresh(false);
				
			}
		};
		zCoordGrid.setChecked(true);
		zCoordGrid.setText("Z grid lines ON/OFF");
		zCoordGrid.setToolTipText("Toggle z axis grid lines on/off");
		zCoordGrid.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/text_align_justify.png"));
		
		saveGraph = new Action() {
			
			// Cache file name otherwise they have to keep
			// choosing the folder.
			private String filename;
			
			@Override
			public void run() {
				
				FileDialog dialog = new FileDialog (shell, SWT.SAVE);
				
				String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
				if (filename!=null) {
					dialog.setFilterPath((new File(filename)).getParent());
				} else {
					String filterPath = "/";
					String platform = SWT.getPlatform();
					if (platform.equals("win32") || platform.equals("wpf")) {
						filterPath = "c:\\";
					}
					dialog.setFilterPath (filterPath);
				}
				dialog.setFilterNames (PlotExportUtil.FILE_TYPES);
				dialog.setFilterExtensions (filterExtensions);
				filename = dialog.open();
				if (filename == null)
					return;

				plotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveGraph.setText(saveButtonText);
		saveGraph.setToolTipText(saveToolTipText);
		saveGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(saveImagePath));

		copyGraph = new Action() {
			@Override
			public void run() {
				plotter.copyGraph();
			}
		};
		copyGraph.setText(copyButtonText);
		copyGraph.setToolTipText(copyToolTipText);
		copyGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(copyImagePath));

		printGraph = new Action() {
			@Override
			public void run() {
				plotter.printGraph();
			}
		};
		printGraph.setText(printButtonText);
		printGraph.setToolTipText(printToolTipText);
		printGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(printImagePath));

		manager.add(resetView);
		manager.add(boundingBox);
		manager.add(xCoordGrid);
		manager.add(yCoordGrid);
		manager.add(zCoordGrid);
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		
		// Needed when toolbar is attached to an editor
		// or else the bar looks empty.
		manager.update(true);

	}
	
	private void buildMenuActions(IMenuManager manager, final DataSetPlotter plotter)
	{
		displayFilled = new Action() {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.FILLED);
				plotter.refresh(false);
			}
		};
		displayFilled.setText("Filled mode");
		displayFilled.setDescription("Render the graph in filled mode");
		displayWireframe = new Action() {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.WIREFRAME);
				plotter.refresh(false);
			}
		};
		displayWireframe.setText("Wireframe mode");
		displayWireframe.setDescription("Render the graph in wireframe mode");
		displayLinegraph = new Action() {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.LINEGRAPH);
				plotter.refresh(true);
			}
		};
		displayLinegraph.setText("Linegraph mode");
		displayLinegraph.setDescription("Render the graph in linegraph mode");
		displayPoint = new Action() {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.POINTS);
				plotter.refresh(false);	
			}
		};
		displayPoint.setText("Point mode");
		displayPoint.setDescription("Render the graph in dot mode");
		zAxisScaleLinear = new Action()
		{
			@Override
			public void run()
			{
				plotter.setYAxisScaling(ScaleType.LINEAR);
			}
		};
		zAxisScaleLinear.setText("Z-Axis scale linear");
		zAxisScaleLinear.setToolTipText("Change the Z-Axis scaling to be linear");
		
		zAxisScaleLog = new Action()
		{
			@Override
			public void run()
			{
				plotter.setYAxisScaling(ScaleType.LN);
			}
		};
		zAxisScaleLog.setText("Z-Axis scale logarithmic");
		zAxisScaleLog.setToolTipText("Change the Z-Axis scaling to be logarithmic (natural)");
		MenuManager zAxis = new MenuManager("Z-Axis");
		zAxis.add(zAxisScaleLinear);
		zAxis.add(zAxisScaleLog);			
		manager.add(zAxis);
		manager.add(displayFilled);
		manager.add(displayWireframe);
		manager.add(displayLinegraph);
		manager.add(displayPoint);
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		if (plotData != null) {
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<Dataset> datasets = Collections.synchronizedList(new LinkedList<Dataset>());
	
			Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
			Dataset zAxisValues = dbPlot.getAxis(AxisMapBean.ZAXIS);
			xAxis.clear();
			yAxis.clear();
			zAxis.clear();
			mainPlotter.setAxisModes((xAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM),
					                 (yAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM),
					                 (zAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM));
			if (xAxisValues != null) {
				if (xAxisValues.getName() != null && xAxisValues.getName().length() > 0)
					mainPlotter.setXAxisLabel(xAxisValues.getName());
				else
					mainPlotter.setXAxisLabel("X-Axis");
				xAxis.setValues(xAxisValues);
				mainPlotter.setXAxisValues(xAxis, 1);
			} else
				mainPlotter.setXAxisLabel("X-Axis");
			if (yAxisValues != null) {
				if (yAxisValues.getName() != null && yAxisValues.getName().length() > 0)
					mainPlotter.setYAxisLabel(yAxisValues.getName());
				else
					mainPlotter.setYAxisLabel("Y-Axis");
				yAxis.setValues(yAxisValues);
				mainPlotter.setYAxisValues(yAxis);
			} else
				mainPlotter.setYAxisLabel("Y-Axis");
			if (zAxisValues != null) {
				if (zAxisValues.getName() != null && zAxisValues.getName().length() > 0)
					mainPlotter.setZAxisLabel(zAxisValues.getName());
				else
					mainPlotter.setZAxisLabel("Z-Axis");
				zAxis.setValues(zAxisValues);
				mainPlotter.setZAxisValues(zAxis);
			} else
				mainPlotter.setZAxisLabel("Z-Axis");
			mainPlotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
			mainPlotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
			while (iter.hasNext()) {
				DatasetWithAxisInformation dataSetAxis = iter.next();
				Dataset data = dataSetAxis.getData();
				datasets.add(data);
			}
			if (datasets.get(0) instanceof CompoundDataset) {
				logger.warn("Surface plotting of CompoundDatasets is currently not supported!");
				plotWindow.notifyUpdateFinished();
			} else {
				final HistogramDataUpdate histoUpdate = new HistogramDataUpdate(datasets.get(0));
				try {
					mainPlotter.replaceAllPlots(datasets);
				} catch (PlotException e) {
					e.printStackTrace();
				}
				//set the title/filename of plot
				String title = "";
				if (page.getActiveEditor()!=null)
					title = page.getActiveEditor().getTitle();
				mainPlotter.setTitle(title);
				
				dataWindowView.setData(datasets.get(0),xAxis,yAxis);
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						mainPlotter.refresh(true);
//						if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM)
//							plotWindow.notifyHistogramChange(histoUpdate); // plotwindow no longer takes care of histogram changes
						plotWindow.notifyUpdateFinished();
					}
				});
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
		if(getDefaultPlottingSystemChoice()==DeprecatedPreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
			histogramView.deleteIObserver(dataWindowView);
			IWorkbenchPage aPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (aPage != null)
				aPage.hideView(dataWindowView);
			plotWindow.deleteIObserver(histogramView);
		} else if (getDefaultPlottingSystemChoice()==DeprecatedPreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
			
		}
	
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.clear();
	}

	@Override
	public void update(Object theObserved, final Object changeCode) {
		if (changeCode instanceof SurfacePlotROI) {
			final SurfacePlotROI roi = (SurfacePlotROI)changeCode;
			try{
				mainPlotter.setDataWindowPosition(roi);
				mainPlotter.refresh(false);
			}catch(ArrayIndexOutOfBoundsException e){
				//logger.debug("Surface plot ROI is out of the image bounds:"+e);
				//do nothing
			}catch(NullPointerException e){
				logger.debug("The Surface plot has been closed and is null:"+e);
			}catch(IllegalArgumentException e){
				logger.debug("Error: "+e);
			}
		}
		if(changeCode instanceof ColorMappingUpdate){
			ColorMappingUpdate update = (ColorMappingUpdate)changeCode;
			mainPlotter.updateColorMapping(update);
		}
	}


	private int getDefaultPlottingSystemChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(DeprecatedPreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? 
				preferenceStore.getDefaultInt(DeprecatedPreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM)
				: preferenceStore.getInt(DeprecatedPreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}
}
