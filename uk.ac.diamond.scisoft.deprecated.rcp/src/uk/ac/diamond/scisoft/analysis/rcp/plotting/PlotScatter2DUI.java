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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.january.dataset.Dataset;
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

/**
 *
 */
@Deprecated
public class PlotScatter2DUI extends Plot1DUIAdapter {

	private IWorkbenchPage page;
	private final static String STATUSSTRING = "Pos: ";
	public final static String STATUSITEMID = "uk.ac.dimaond.scisoft.analysis.rcp.plotting.PlotScatter2DUI";
	private DataSetPlotter mainPlotter;
	private StatusLineContributionItem statusLine;	
	private AbstractPlotWindow plotWindow;
	
	public PlotScatter2DUI(final AbstractPlotWindow window,
							IActionBars bars, 
							final DataSetPlotter plotter,
							Composite parent, IWorkbenchPage page, String viewName)
	{
		super(null, parent, viewName);
		logger.error("the plotwindow cannot provide anymore a DatasetPlotter");
//		super(window.getMainPlotter(), parent, viewName);

		this.mainPlotter = plotter;
		this.parent = parent;		
		this.page = page;
		this.plotWindow = window;
		buildToolActions(bars.getToolBarManager());
		buildMenuActions(bars.getMenuManager());
		buildStatusLineItems(bars.getStatusLineManager());
		mainPlotter.registerUI(this);
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
		
		manager.add(toggleXAxisErrorBars);
		manager.add(toggleYAxisErrorBars);
		
	}
	
	/**
	 * @param manager 
	 * 
	 */
	@Override
	public void buildToolActions(IToolBarManager manager)
	{		
		manager.add(new Separator(getClass().getName()+"Data"));
		manager.add(displayPlotPos);
	//	manager.add(rightClickOnGraphAction);
		manager.add(new Separator(getClass().getName()+"History"));
//		manager.add(addToHistory);
//		manager.add(removeFromHistory);
		manager.add(new Separator(getClass().getName()+"Zoom"));
		manager.add(activateRegionZoom);
		manager.add(activateAreaZoom);
		manager.add(zoomAction);
		manager.add(resetZoomAction);
		manager.add(new Separator(getClass().getName()+"Appearance"));
		manager.add(changeColour);
//		manager.add(activateXgrid);
//		manager.add(activateYgrid);
		manager.add(new Separator(getClass().getName()+"Print"));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		
		// Needed when toolbar is attached to an editor
		// or else the bar looks empty.
		manager.update(true);

	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		
		if (plotData != null) {
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<Dataset> datasets = Collections.synchronizedList(new LinkedList<Dataset>());
			final List<AxisValues> xAxes = Collections.synchronizedList(new LinkedList<AxisValues>());
			final List<AxisValues> yAxes = Collections.synchronizedList(new LinkedList<AxisValues>());
			if (!isUpdate) {
				int counter = 0;
				Plot1DGraphTable colourTable = mainPlotter.getColourTable();
				colourTable.clearLegend();			
				mainPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.CUSTOM, AxisMode.LINEAR);
				while (iter.hasNext()) {
					Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS+Integer.toString(counter));
					Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS+Integer.toString(counter));
					String xName = xAxisValues.getName();
					if (xName != null && xName.length() > 0)
						mainPlotter.setXAxisLabel(xName);
					else
						mainPlotter.setXAxisLabel("X-Axis");
					String yName = yAxisValues.getName();
					if (yName != null && yName.length() > 0)
						mainPlotter.setYAxisLabel(yName);
					else
						mainPlotter.setYAxisLabel("Y-Axis");
					AxisValues newXAxis = new AxisValues();
					newXAxis.setValues(xAxisValues);
					AxisValues newYAxis = new AxisValues();
					newYAxis.setValues(yAxisValues);
					xAxes.add(newXAxis);
					yAxes.add(newYAxis);
					DatasetWithAxisInformation dataSetAxis = iter.next();
					Dataset data = dataSetAxis.getData();
					datasets.add(data);
					counter++;
				}
				try {
					mainPlotter.replaceAllPlots(datasets,xAxes,yAxes);
				} catch (PlotException e) {
					e.printStackTrace();
				}
			} else {
				while (iter.hasNext()) {
					Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS+"0");
					Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS+"0");
					AxisValues newXAxis = new AxisValues();
					AxisValues newYAxis = new AxisValues();
					newXAxis.setValues(xAxisValues);
					newYAxis.setValues(yAxisValues);
					DatasetWithAxisInformation dataSetAxis = iter.next();
					Dataset data = dataSetAxis.getData();
					try {
						mainPlotter.addToCurrentPlot(data,newXAxis,newYAxis);
					} catch (PlotException e) {
						e.printStackTrace();
					}
				}					
			}
			
			//we set the plot/file name
			String title = "";
			if(page.getActiveEditor()!=null)
				title = page.getActiveEditor().getTitle();
			mainPlotter.setTitle(title);
			
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {						
					mainPlotter.refresh(true);
					mainPlotter.updateAllAppearance();
					plotWindow.notifyUpdateFinished();
				}
			});
		} 
	}
	

	@Override
	public void plotActionPerformed(final PlotActionEvent event) {
		parent.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run()
			{
				String pos = String.format("%s %g:%g", STATUSSTRING, event.getPosition()[0], event.getPosition()[1]);
				statusLine.setText(pos);
			}
		});
	}

}
