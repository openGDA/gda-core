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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.core.ScaleType;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlotView;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;
import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;

/**
 *
 */
@Deprecated
public class Plot2DUI extends AbstractPlotUI {

	public class Plot2DUIUpdater implements Runnable {
		public boolean inqueue=true;

		@Override
		public void run() {
			inqueue=false;
			getSidePlotView().processPlotUpdate();
			plotWindow.notifyUpdateFinished();
		}
	}
	/**
	 * Status item ID
	 */
	public final static String STATUSITEMID = "uk.ac.dimaond.scisoft.analysis.rcp.plotting.Plot2DUI";
	private static HashMap<String, Integer> logScaleSettings = new HashMap<String, Integer>();
	
	private DataSetPlotter mainPlotter;
	private Composite compParent;
	private IWorkbenchPage page;
	private String plotViewID;

	private List<IObserver> observers = 
		Collections.synchronizedList(new LinkedList<IObserver>());

	private AbstractPlotWindow plotWindow;
	private HistogramView histogramView;
	private Action resetView;
	private Action printGraph;
	private Action monitorValues;
	private Action saveGraph;
	private Action copyGraph;
	private Action canvasAspect;
	private AxisValues xAxis = null;
	private AxisValues yAxis = null;
	private Action xLabelTypeRound = null;
	private Action xLabelTypeFloat = null;
	private Action xLabelTypeExponent = null;
	private Action xLabelTypeSI = null;
	private Action yLabelTypeRound = null;
	private Action yLabelTypeFloat = null;
	private Action yLabelTypeExponent = null;
	private Action yLabelTypeSI = null;
	private Action colourCastLinear = null;
	private Action colourCastLog = null;
	private Action gradientMode = null;
	private Action axisVisibility = null;
	private IBeanScriptingManager manager = null;
	private List<Action> switchToTabs;
	private HistogramDataUpdate histoUpdate = null;
	private static final Logger logger = LoggerFactory.getLogger(Plot2DUI.class);
	
	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");
	private Plot2DUIUpdater lastestPlot2duiUpdater;
	
	/**
	 * @param window 
	 * @param plotter
	 * @param parent
	 * @param page 
	 * @param id 
	 */
	public Plot2DUI(AbstractPlotWindow window, 
					final DataSetPlotter plotter,
					final IBeanScriptingManager manager,
					Composite parent, IWorkbenchPage page, 
					IActionBars bars,
					String id)
	{
		this.mainPlotter = plotter;
		this.compParent = parent;
		this.plotWindow = window;
		this.page = page;
		plotViewID = id;
		this.manager = manager;
		initHistogramView(plotViewID);
		mainPlotter.registerUI(this);
		mainPlotter.enableImageScrollBars(getPreferenceScrollBars());
		initSidePlotView();
		buildMenuActions(bars.getMenuManager(), plotter); 
		buildToolActions(bars.getToolBarManager(), plotter, parent.getShell());										 
		xAxis = new AxisValues();
		yAxis = new AxisValues();		
		bars.updateActionBars();
	}

	public void initHistogramView(String id) {
		try {
			 histogramView = (HistogramView) page.showView("uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView",
					id, IWorkbenchPage.VIEW_CREATE);
			 
//			 SidePlotUtils.bringToTop(page, histogramView);		 
			 
			 plotWindow.addIObserver(histogramView);
		} catch (PartInitException e) {
			logger.error("Failed to initialized histogram View");
		}		
	}
	
	@Override
	public ISidePlotView initSidePlotView() {
		try {
			SidePlotView spv = getSidePlotView();
			logger.error("The plotwindow cannot provide any DatasetPlotter anymore");
//			spv.setPlotView(plotWindow.getMainPlotter(),manager); 
			spv.setSwitchActions(switchToTabs);
			
			//			SidePlotUtils.bringToTop(page, spv);		 
            return spv;
		} catch (IllegalStateException ex) {
			logger.debug("Cannot initiate side plot view", ex);
		}
		return null;
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate)
	{
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		if (plotData != null) {
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<Dataset> datasets = Collections.synchronizedList(new LinkedList<Dataset>());
			Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
			xAxis.clear();
			yAxis.clear();
			mainPlotter.setAxisModes((xAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM),
					                 (yAxisValues == null ? AxisMode.LINEAR : AxisMode.CUSTOM),
					                 AxisMode.LINEAR);
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
			mainPlotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
			mainPlotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
			while (iter.hasNext()) {
				DatasetWithAxisInformation dataSetAxis = iter.next();
				Dataset data = dataSetAxis.getData();
				datasets.add(data);
			}
			histoUpdate = new
			  HistogramDataUpdate(datasets.get(0));
					

			try {
				mainPlotter.replaceAllPlots(datasets);
			} catch (PlotException e) {
				e.printStackTrace();
			}

			mainPlotter.setTitle(datasets.get(0).getName());

			if( lastestPlot2duiUpdater ==null || !lastestPlot2duiUpdater.inqueue){
				lastestPlot2duiUpdater = new Plot2DUIUpdater();
				compParent.getDisplay().asyncExec(lastestPlot2duiUpdater);	
			}

//			Dataset data = datasets.get(0);
//			boolean useRGB = 
//				(data instanceof RGBDataset) ||
// 	  	  	    (data instanceof CompoundDataset &&
//				(((CompoundDataset)data).getElementsPerItem() == 3 ||
//		 		 ((CompoundDataset)data).getElementsPerItem() == 4));
//
//			if (!useRGB)
//				plotWindow.notifyHistogramChange(histoUpdate); // plotwindow no longer takes care of histogram changes
//			else
				mainPlotter.refresh(true);
		}
	}

	/**
	 * @return the histogram view
	 */
	public HistogramView getHistogramView() {
		return histogramView;
	}


	private void buildMenuActions(IMenuManager manager, final DataSetPlotter plotter)
	{
		xLabelTypeRound = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
				plotter.refresh(true);
			}
		};
		xLabelTypeRound.setText("X-Axis labels integer");
		xLabelTypeRound.setToolTipText("Change the labelling on the x-axis to integer numbers");
		xLabelTypeRound.setChecked(true);
		xLabelTypeFloat = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.plainMode);
				plotter.refresh(true);
			}
		};
		xLabelTypeFloat.setText("X-Axis labels real");
		xLabelTypeFloat.setToolTipText("Change the labelling on the x-axis to real numbers");

		xLabelTypeExponent = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.useExponent);
				plotter.refresh(true);
			}
		};
		xLabelTypeExponent.setText("X-Axis labels exponents");
		xLabelTypeExponent.setToolTipText("Change the labelling on the x-axis to using exponents");

		xLabelTypeSI = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.useSIunits);	
				plotter.refresh(true);
			}
		};
		xLabelTypeSI.setText("X-Axis labels SI units");
		xLabelTypeSI.setToolTipText("Change the labelling on the x-axis to using SI units");

		yLabelTypeRound = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
				plotter.refresh(true);
			}
		};
		yLabelTypeRound.setText("Y-Axis labels integer");
		yLabelTypeRound.setToolTipText("Change the labelling on the y-axis to integer numbers");
		yLabelTypeRound.setChecked(true);
		yLabelTypeFloat = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYTickLabelFormat(TickFormatting.plainMode);
				plotter.refresh(true);
			}
		};
		yLabelTypeFloat.setText("Y-Axis labels real");
		yLabelTypeFloat.setToolTipText("Change the labelling on the y-axis to real numbers");

		yLabelTypeExponent = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYTickLabelFormat(TickFormatting.useExponent);
				plotter.refresh(true);
			}
		};
		yLabelTypeExponent.setText("Y-Axis labels exponents");
		yLabelTypeExponent.setToolTipText("Change the labelling on the y-axis to using exponents");

		yLabelTypeSI = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYTickLabelFormat(TickFormatting.useSIunits);	
				plotter.refresh(true);
			}
		};
		yLabelTypeSI.setText("Y-Axis labels SI units");
		yLabelTypeSI.setToolTipText("Change the labelling on the y-axis to using SI units");

		colourCastLinear = new Action("",IAction.AS_RADIO_BUTTON) 
		{
			@Override
			public void run() 
			{
				plotter.setZAxisScaling(ScaleType.LINEAR);
				plotter.refresh(true);
				histogramView.setScaling(ScaleType.LINEAR);
				logScaleSettings.put(histogramView.getPartName(), 0);
			}
		};
		colourCastLinear.setChecked(getPreferenceColourScaleChoice() == 0);
		colourCastLinear.setText("Linear mapping colours");
		colourCastLinear.setToolTipText("Apply linear colour mapping to image");

		colourCastLog = new Action("",IAction.AS_RADIO_BUTTON) 
		{
			@Override
			public void run() 
			{
				plotter.setZAxisScaling(ScaleType.LN);
				plotter.refresh(true);
				histogramView.setScaling(ScaleType.LN);
				logScaleSettings.put(histogramView.getPartName(), 1);
			}
		};
		colourCastLog.setText("Logarithmic mapping colours");
		colourCastLog.setToolTipText("Apply logarithmic colour mapping to image");
		colourCastLog.setChecked(getPreferenceColourScaleChoice() != 0);
		
		if (colourCastLog.isChecked()) {
			plotter.setZAxisScaling(ScaleType.LN);
			histogramView.setScaling(ScaleType.LN);
		}
		
		gradientMode = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setGradientImageMode(gradientMode.isChecked());
			}
		};
		gradientMode.setText("Switch to gradient mode");
		gradientMode.setToolTipText("Switches display to gradient mode shows magnitude of gradients");

		axisVisibility = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setAxisVisibility(axisVisibility.isChecked());
			}
		};
		axisVisibility.setText("Show Axis");
		axisVisibility.setToolTipText("Switches on/off Axis visibility");
		axisVisibility.setChecked(true);

		MenuManager xAxisMenu = new MenuManager("X-Axis");
		MenuManager yAxisMenu = new MenuManager("Y-Axis");
		manager.add(xAxisMenu);
		manager.add(yAxisMenu);
		xAxisMenu.add(xLabelTypeFloat);
		xAxisMenu.add(xLabelTypeRound);
		xAxisMenu.add(xLabelTypeExponent);
		xAxisMenu.add(xLabelTypeSI);
		yAxisMenu.add(yLabelTypeFloat);
		yAxisMenu.add(yLabelTypeRound);
		yAxisMenu.add(yLabelTypeExponent);
		yAxisMenu.add(yLabelTypeSI);
		manager.add(colourCastLinear);
		manager.add(colourCastLog);
		manager.add(gradientMode);
		manager.add(axisVisibility);
		manager.update(true);
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
		
		monitorValues = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.setPlotActionEnabled(monitorValues.isChecked());
			}
		};
		monitorValues.setText("Monitor position");
		monitorValues.setToolTipText("Monitor the position in the image");
		monitorValues.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/monitor.png"));

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
		
		canvasAspect = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.imagePlotSetCanvasAspectRatio(canvasAspect.isChecked());
				plotter.refresh(false);
			}
		};
		
		canvasAspect.setText("Canvas");
		canvasAspect.setToolTipText("Switch between canvas/data aspect ratio");
		canvasAspect.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/arrow_out.png"));

		manager.add(new Separator());
		try {
			switchToTabs = getSidePlotView().createSwitchActions(this);

			for (Action action: switchToTabs) {
				manager.add(action);
			}
		} catch (IllegalStateException ex) {}
		manager.add(new Separator());
		
		manager.add(resetView);
		manager.add(monitorValues);
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		manager.add(new Separator(getClass().getName()+"Canvas"));
		manager.add(canvasAspect);
		
		// Needed when toolbar is attached to an editor
		// or else the bar looks empty.
		manager.update(true);
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
	public void disposeOverlays() {
		getSidePlotView().disposeOverlays();
	}

	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
		histoUpdate = null;
		plotWindow.deleteIObserver(histogramView);
		try {
			getSidePlotView().deactivate(leaveSidePlotOpen);
		} catch (IllegalStateException ex) {}
		try {
			page.hideView(histogramView);
		} catch (IllegalArgumentException e) {
		}
	}

	@Override
	public void processGUIUpdate(GuiBean guiBean) {
		getSidePlotView().updateGUI(guiBean);
	}

	@Override
	public SidePlotView getSidePlotView() {
		return SidePlotUtils.getSidePlotView(page, plotViewID);
	}

	private boolean getPreferenceScrollBars() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR) ? 
				preferenceStore.getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR)
				: preferenceStore.getBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_SHOWSCROLLBAR);		
	}
	
	private int getPreferenceColourScaleChoice() {
		if (histogramView != null &&
			logScaleSettings.get(histogramView.getPartName()) != null)
			return logScaleSettings.get(histogramView.getPartName());
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		if (histogramView != null) {
			String pName =  histogramView.getPartName();
			if (!preferenceStore.isDefault(pName+"."+PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING))
				return preferenceStore.getInt(pName+"."+PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING); 	
		} 
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING) ? 
			   preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING)
			   : preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOT2D_SCALING);
	}	
}
