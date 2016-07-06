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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramDataUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 *
 */
@Deprecated
public class PlotScatter3DUI extends AbstractPlotUI {

	private Composite parent;
	private AbstractPlotWindow plotWindow;
	private AxisValues xAxis;
	private AxisValues yAxis;
	private AxisValues zAxis;
	private DataSetPlotter mainPlotter;
	private Action boundingBox;
	private Action saveGraph;
	private Action copyGraph;
	private Action printGraph;
	private Action useTransparency;
	private Action renderEdgeOnly;
	private Action uniformSize;
	private Action xLabelTypeRound = null;
	private Action xLabelTypeFloat = null;
	private Action xLabelTypeExponent = null;
	private Action xLabelTypeSI = null;
	private Action yLabelTypeRound = null;
	private Action yLabelTypeFloat = null;
	private Action yLabelTypeExponent = null;
	private Action yLabelTypeSI = null;	
	private Action zLabelTypeRound = null;
	private Action zLabelTypeFloat = null;
	private Action zLabelTypeExponent = null;
	private Action zLabelTypeSI = null;		
	private HistogramView histogramView;
	private Action resetView;
	private IWorkbenchPage page;
	
	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

	public PlotScatter3DUI(AbstractPlotWindow window, 
			 			   final DataSetPlotter plotter,
			 			   Composite parent, 
			 			   IWorkbenchPage page, 
			 			   IActionBars bars,
			 			   String id) {
		
		this.parent = parent;
		this.plotWindow = window;
		xAxis = new AxisValues();
		yAxis = new AxisValues();
		zAxis = new AxisValues();
		this.mainPlotter = plotter;
		this.page = page;
		buildMenuActions(bars.getMenuManager(), plotter); 
		buildToolActions(bars.getToolBarManager(), 
		         plotter, parent.getShell());								 

		try {
			 histogramView = (HistogramView) page.showView("uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView",
					id, IWorkbenchPage.VIEW_CREATE);
			plotWindow.addIObserver(histogramView);
		} catch (PartInitException e) {
			e.printStackTrace();
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
		manager.add(new Separator(getClass().getName()+"Print"));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		
		// Needed when toolbar is attached to an editor
		// or else the bar looks empty.
		manager.update(true);

	}
	
	private void buildMenuActions(IMenuManager manager, final DataSetPlotter plotter)
	{	
		useTransparency = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run()
			{
				plotter.useTransparency(useTransparency.isChecked());
				plotter.refresh(false);
			}
		};
		useTransparency.setText("Use transparency");
		useTransparency.setToolTipText("Switch on/off transparency");
		renderEdgeOnly = new Action("",IAction.AS_CHECK_BOX) {
			@Override
			public void run() 
			{
				if (renderEdgeOnly.isChecked())
					plotter.useTransparency(true);
				else
					plotter.useTransparency(useTransparency.isChecked());
				plotter.useDrawOutlinesOnly(renderEdgeOnly.isChecked());
			}
		};
		xLabelTypeRound = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
				plotter.refresh(false);
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
				plotter.refresh(false);
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
				plotter.refresh(false);
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
				plotter.refresh(false);
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
				plotter.refresh(false);
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
				plotter.refresh(false);
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
				plotter.refresh(false);
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
				plotter.refresh(false);
			}
		};
		yLabelTypeSI.setText("Y-Axis labels SI units");
		yLabelTypeSI.setToolTipText("Change the labelling on the y-axis to using SI units");
		zLabelTypeRound = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setZTickLabelFormat(TickFormatting.roundAndChopMode);
				plotter.refresh(false);
			}
		};
		zLabelTypeRound.setText("Z-Axis labels integer");
		zLabelTypeRound.setToolTipText("Change the labelling on the z-axis to integer numbers");
		zLabelTypeFloat = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.plainMode);
				plotter.refresh(false);
			}
		};
		zLabelTypeFloat.setText("Z-Axis labels real");
		zLabelTypeFloat.setToolTipText("Change the labelling on the z-axis to real numbers");
		zLabelTypeFloat.setChecked(true);

		zLabelTypeExponent = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.useExponent);
				plotter.refresh(false);
			}
		};
		zLabelTypeExponent.setText("Z-Axis labels exponents");
		zLabelTypeExponent.setToolTipText("Change the labelling on the z-axis to using exponents");

		zLabelTypeSI = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setZTickLabelFormat(TickFormatting.useSIunits);	
				plotter.refresh(false);
			}
		};
		zLabelTypeSI.setText("Z-Axis labels SI units");
		zLabelTypeSI.setToolTipText("Change the labelling on the z-axis to using SI units");
		
		renderEdgeOnly.setText("Draw outlines only");
		renderEdgeOnly.setToolTipText("Switch on/off drawing outlines only");
		
		uniformSize = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run() {
				plotter.useUniformSize(uniformSize.isChecked());
				plotter.refresh(false);
			}
		};
		uniformSize.setText("Uniform size");
		uniformSize.setToolTipText("Switch on/off uniform point size");
		manager.add(useTransparency);
		manager.add(renderEdgeOnly);
		manager.add(uniformSize);
		MenuManager xAxisMenu = new MenuManager("X-Axis");
		MenuManager yAxisMenu = new MenuManager("Y-Axis");
		MenuManager zAxisMenu = new MenuManager("Z-Axis");
		manager.add(xAxisMenu);
		manager.add(yAxisMenu);		
		manager.add(zAxisMenu);
		xAxisMenu.add(xLabelTypeFloat);
		xAxisMenu.add(xLabelTypeRound);
		xAxisMenu.add(xLabelTypeExponent);
		xAxisMenu.add(xLabelTypeSI);
		yAxisMenu.add(yLabelTypeFloat);
		yAxisMenu.add(yLabelTypeRound);
		yAxisMenu.add(yLabelTypeExponent);
		yAxisMenu.add(yLabelTypeSI);
		zAxisMenu.add(zLabelTypeFloat);
		zAxisMenu.add(zLabelTypeRound);
		zAxisMenu.add(zLabelTypeExponent);
		zAxisMenu.add(zLabelTypeSI);		
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		if (plotData != null) {
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<IDataset> datasets = Collections.synchronizedList(new LinkedList<IDataset>());
	
			Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			Dataset yAxisValues = dbPlot.getAxis(AxisMapBean.YAXIS);
			Dataset zAxisValues = dbPlot.getAxis(AxisMapBean.ZAXIS);
			if (xAxisValues != null && yAxisValues != null && zAxisValues != null) {
				if (!isUpdate) {
					xAxis.clear();
					yAxis.clear();
					zAxis.clear();
					if (xAxisValues.getName() != null && xAxisValues.getName().length() > 0)
						mainPlotter.setXAxisLabel(xAxisValues.getName());
					else
						mainPlotter.setXAxisLabel("X-Axis");
	
					if (yAxisValues.getName() != null && yAxisValues.getName().length() > 0)
						mainPlotter.setYAxisLabel(yAxisValues.getName());
					else
						mainPlotter.setYAxisLabel("Y-Axis");

					if (zAxisValues.getName() != null && zAxisValues.getName().length() > 0)
						mainPlotter.setZAxisLabel(zAxisValues.getName());
					else
						mainPlotter.setZAxisLabel("Z-Axis");

				}
	
				xAxis.setValues(xAxisValues);
				mainPlotter.setXAxisValues(xAxis, 1);
				
				yAxis.setValues(yAxisValues);
				mainPlotter.setYAxisValues(yAxis);

				zAxis.setValues(zAxisValues);
				mainPlotter.setZAxisValues(zAxis);

				mainPlotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
				mainPlotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
				while (iter.hasNext()) {
					DatasetWithAxisInformation dataSetAxis = iter.next();
					Dataset data = dataSetAxis.getData();
					datasets.add(data);
				}
				if (!isUpdate) {
					try {
						mainPlotter.replaceAllPlots(datasets);
					} catch (PlotException e) {
						e.printStackTrace();
					}
				} else {
					IDataset data = datasets.get(0);
					IDataset currentData = mainPlotter.getCurrentDataSet();
					final int addLength = data.getSize();
					int n = currentData.getSize();
					currentData.resize(n + addLength);
					for (int i = 0; i < addLength; i++)
						currentData.set(data.getObject(i), n++);
					datasets.set(0, currentData);
					try {
						mainPlotter.replaceAllPlots(datasets);
					} catch (PlotException e) {
						e.printStackTrace();
					}					
				}
				//set the title/filename of plot
				String title = "";
				if (page.getActiveEditor()!=null)
					title = page.getActiveEditor().getTitle();
				mainPlotter.setTitle(title);
				
				final HistogramDataUpdate histoUpdate = new
				  HistogramDataUpdate(datasets.get(0));
				
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						mainPlotter.refresh(true);
//						plotWindow.notifyHistogramChange(histoUpdate); // Abstract PlotWindow no longer takes care of histogram update
						plotWindow.notifyUpdateFinished();
					}
				});	
				
			}
/*			dataWindowView.setData(datasets.get(0),xAxis,yAxis);*/
		}

	}
	
	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
		plotWindow.deleteIObserver(histogramView);
		page.hideView(histogramView);
	}	

}
