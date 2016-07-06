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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.tool.PlotActionComplexEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.IBeanScriptingManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;


/**
 * A very general UI for 1D Plots using SWT / Eclipse RCP
 * 
 * With complete action set in the toolbar.
 */
@Deprecated
public class Plot1DUIComplete extends Plot1DUIAdapter {

	/**
	 * Status item ID
	 */
	public final static String STATUSITEMID = "uk.ac.dimaond.scisoft.analysis.rcp.plotting.Plot1DUI";
	private final static String STATUSSTRING = "Pos: ";
	
	
	private StatusLineContributionItem statusLine;	
	private AxisValues xAxis;
	private IWorkbenchPage page;
	private String plotViewID;
	private AbstractPlotWindow plotWindow;
	private List<Action> switchToTabs;
	private IBeanScriptingManager manager;
	
	/**
	 * Constructor of a Plot1DUI 
	 * @param window Plot window
	 * @param bars ActionBars from the parent view
	 * @param parent parent composite 
	 * @param page workbench page
	 * @param viewName name of the view associated to this UI
	 */
	
	public Plot1DUIComplete(final AbstractPlotWindow window, 
							final IBeanScriptingManager manager,
							IActionBars bars, 
					        Composite parent, IWorkbenchPage page,
					        String viewName) {
		super(null, parent, viewName);
//		super(window.getDatasetPlotter(), parent, viewName);
		logger.error("The plotwindow cannot provide any DatasetPlotter anymore");
		this.page = page;
		plotWindow = window;
		plotViewID = viewName;
		this.xAxis = new AxisValues();
		this.manager = manager;
		
		initSidePlotView();
		buildToolActions(bars.getToolBarManager());
		buildMenuActions(bars.getMenuManager());
		buildStatusLineItems(bars.getStatusLineManager());
		
	}

	@Override
	public ISidePlotView initSidePlotView() {
		try {
			SidePlotView spv = getSidePlotView();
			spv.setPlotView(plotter,manager); 
			spv.setSwitchActions(switchToTabs);
			
//			SidePlotUtils.bringToTop(page, spv);
			return spv;
		} catch (IllegalStateException ex) {
			logger.debug("Cannot initiate side plot view", ex);
		}
		return null;
	}

	@Override
	public SidePlotView getSidePlotView() {
		return SidePlotUtils.getSidePlotView(page, plotViewID);
	}

	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
		super.deactivate(leaveSidePlotOpen);
		try {
			getSidePlotView().deactivate(leaveSidePlotOpen);
		} catch (IllegalStateException ex) {
		} catch (NullPointerException ne) {
		}
	}

	/**
	 * 
	 * @param manager
	 */
	@Override
	public void buildStatusLineItems(IStatusLineManager manager)
	{
		statusLine = new StatusLineContributionItem(STATUSITEMID);
		statusLine.setText(STATUSSTRING);
		manager.add(statusLine);
	}
	
	/**
	 * 
	 */
	@Override
	public void buildMenuActions(IMenuManager manager)
	{
		MenuManager xAxis = new MenuManager("X-Axis");
		MenuManager yAxis = new MenuManager("Y-Axis");
		manager.add(xAxis);
		manager.add(yAxis);

		xAxis.add(xLabelTypeRound);
		xAxis.add(xLabelTypeFloat);
		xAxis.add(xLabelTypeExponent);
		xAxis.add(xLabelTypeSI);
		yAxis.add(yLabelTypeRound);
		yAxis.add(yLabelTypeFloat);
		yAxis.add(yLabelTypeExponent);
		yAxis.add(yLabelTypeSI);
		manager.add(yAxisScaleLinear);
		manager.add(yAxisScaleLog);		
		
	}
	
	/**
	 * @param manager 
	 * 
	 */
	@Override
	public void buildToolActions(IToolBarManager manager)
	{
		try {
			switchToTabs = getSidePlotView().createSwitchActions(this);
			for (Action action: switchToTabs) {
				manager.add(action);
			}
		} catch (IllegalStateException ex) {}
			
		manager.add(new Separator(getClass().getName()+"Data"));
		manager.add(displayPlotPos);
		manager.add(rightClickOnGraphAction);
		manager.add(new Separator(getClass().getName()+"History"));
		manager.add(addToHistory);
		manager.add(removeFromHistory);
		manager.add(new Separator(getClass().getName()+"Zoom"));
		manager.add(activateRegionZoom);
		manager.add(activateAreaZoom);
		manager.add(zoomAction);
		manager.add(resetZoomAction);
		manager.add(new Separator(getClass().getName()+"Appearance"));
		manager.add(changeColour);
		manager.add(activateXgrid);
		manager.add(activateYgrid);
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		manager.add(new Separator(getClass().getName()+"Menu"));
		// Needed when toolbar is attached to an editor
		// or else the bar looks empty.
		manager.update(true);

	}
	
	@Override
	public void plotActionPerformed(final PlotActionEvent event) {
		if (event instanceof PlotActionComplexEvent)
		{
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run()
				{
					PlotDataTableDialog dataDialog = 
						new PlotDataTableDialog(parent.getShell(),(PlotActionComplexEvent)event);
					dataDialog.open();								
				}
			});
		} else
		{
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run()
				{
					String pos;
					if (event.getPosition().length > 2) {
						pos = String.format("%s %g (%g):%g", STATUSSTRING, event.getPosition()[0], event.getPosition()[2], event.getPosition()[1]);
					} else {
						pos = String.format("%s %g:%g", STATUSSTRING, event.getPosition()[0], event.getPosition()[1]);
					}
					statusLine.setText(pos);	
				}
			});
		}
		super.plotActionPerformed(event);
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		
		if (plotData != null) {
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<Dataset> datasets = Collections.synchronizedList(new LinkedList<Dataset>());

			// check for x-axis data
			xAxis.clear();
			Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			Dataset xAxisValues2 = dbPlot.getAxis(AxisMapBean.XAXIS2);
			AxisValues xAxes2 = null;
			AxisMode xAxisMode = AxisMode.LINEAR;
			LinkedList<AxisValues> xAxisValuesList = null;
			if (xAxisValues != null) {
				String xName = xAxisValues.getName();
				if (xName != null && xName.length() > 0)
						plotter.setXAxisLabel(xName);
				else
					plotter.setXAxisLabel("X-Axis");
				xAxis.setValues(xAxisValues);
				xAxisMode = AxisMode.CUSTOM;
			} else {
				Dataset testValues = dbPlot.getAxis(AxisMapBean.XAXIS+"0");
				plotter.setXAxisLabel("X-Axis");
				if (testValues != null) {
					xAxisMode = AxisMode.CUSTOM;
					xAxisValuesList = new LinkedList<AxisValues>();
				}
				plotter.setXAxisLabel("X-Axis");
			}

			Plot1DGraphTable colourTable = plotter.getColourTable();
			colourTable.clearLegend();
			plotter.setAxisModes(xAxisMode, AxisMode.LINEAR, AxisMode.LINEAR);
			if (xAxisValues != null)
			{
				plotter.setXAxisValues(xAxis, plotData.size());
			}

			if (xAxisValues2 != null) {
				String secondXAxisName = "X-Axis 2";
				if (xAxisValues2.getName() != null &&
					xAxisValues2.getName().length() > 0)
					secondXAxisName = xAxisValues2.getName();
				xAxes2 = new AxisValues(xAxisValues2);
				plotter.setSecondaryXAxisValues(xAxes2,secondXAxisName);
			} else
				plotter.setSecondaryXAxisValues(null,"");

			int axisCounter = 0;
			while (iter.hasNext()) {
				DatasetWithAxisInformation dataSetAxis = iter.next();
				Dataset data = dataSetAxis.getData();
				if (xAxisValuesList != null) {
					String axisStr = AxisMapBean.XAXIS + axisCounter;
					Dataset testValues = dbPlot.getAxis(axisStr);
					if (testValues != null) {
						AxisValues xaxis = new AxisValues(testValues);
						xAxisValuesList.add(xaxis);
					}
					axisCounter++;
				}

				Plot1DAppearance newApp = 
					new Plot1DAppearance(PlotColorUtility.getDefaultColour(colourTable.getLegendSize()),
							             PlotColorUtility.getDefaultStyle(colourTable.getLegendSize()),
							             PlotColorUtility.getDefaultLineWidth(colourTable.getLegendSize()),
							             data.getName());
				colourTable.addEntryOnLegend(newApp);
				datasets.add(data);
			}
			
			// calculate an nice label for the Y-Axis
			ArrayList<String> AxisNames = new ArrayList<String>();
			
			for(int i = 0; i < datasets.size(); i++) {
				String name = datasets.get(i).getName();
				if(name != null) {
					AxisNames.add(name);
				}
			}
			StringBuilder yLabel = new StringBuilder("Y-Axis");
			if (AxisNames.size() > 0) {
				yLabel.delete(0, yLabel.length());
				yLabel.append(AxisNames.get(0));
				for (int i = 1; i < AxisNames.size(); i++) {
					yLabel.append(", ");
					yLabel.append(AxisNames.get(i));
				}
			}
			if (yLabel.length() > 50) {
				yLabel.delete(47, yLabel.length());
				yLabel.append("...");
			}
			plotter.setYAxisLabel(yLabel.toString());
			
			int numHistory = plotter.getNumHistory();
			for (int i = 0; i < numHistory; i++) {
				Plot1DAppearance newApp =
					new Plot1DAppearance(PlotColorUtility.getDefaultColour(colourTable.getLegendSize()),
										 PlotColorUtility.getDefaultStyle(colourTable.getLegendSize()),
										 PlotColorUtility.getDefaultLineWidth(colourTable.getLegendSize()),
										 Plot1DUIAdapter.HISTORYSTRING + " " + (i + 1));
				colourTable.addEntryOnLegend(newApp);
			}
			plotter.setPlotUpdateOperation(isUpdate);
			try {
				if (xAxisValuesList != null)
					plotter.replaceAllPlots(datasets, xAxisValuesList);
				else
					plotter.replaceAllPlots(datasets);
			} catch (PlotException e) {
				e.printStackTrace();
			}
			
			//we set the plot/file name
			String title = "";
			if (page.getActiveEditor()!=null)
				title = page.getActiveEditor().getTitle();
			plotter.setTitle(title);
			
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					plotter.refresh(true);
					plotter.updateAllAppearance();
					getSidePlotView().processPlotUpdate();
					plotWindow.notifyUpdateFinished();
				}
			});
		}
	}
}
