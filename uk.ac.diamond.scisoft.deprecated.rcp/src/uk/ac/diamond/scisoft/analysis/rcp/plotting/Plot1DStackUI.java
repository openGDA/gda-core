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

import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;

/**
 * A very general UI for 1D Stacked Plots using SWT / Eclipse RCP
 */
@Deprecated
public class Plot1DStackUI extends AbstractPlotUI {

	private IWorkbenchPage page;
	private AxisValues xAxis = null;
	private AxisValues zAxis = null;
	private DataSetPlotter mainPlotter;
	private Composite parent;
	private Action xLabelTypeRound;
	private Action xLabelTypeFloat;
	private Action xLabelTypeExponent;
	private Action xLabelTypeSI;
	private Action zoomAction;
	private Action activateRegionZoom;
	private Action activateAreaZoom;
	private Action changeColour;
	private Action colourLegend;
	private Action tooglePerspectiveAction;
	private Action resetView;
	private Action boundingBox;
	private AbstractPlotWindow plotWindow;
	private boolean setInitialPersp = false;
	//private SliderAction expansionAction;
	
	
	/**
	 * Constructor of the Plot1DStackUI
	 * @param bars
	 * @param plotter
	 * @param parent
	 */
	public Plot1DStackUI(final AbstractPlotWindow window,
						 IActionBars bars, 
						 final DataSetPlotter plotter,
						 Composite parent, IWorkbenchPage page) {

		this.parent = parent;
		this.page = page;
		this.plotWindow = window;
		xAxis = new AxisValues();
		zAxis = new AxisValues();
		this.mainPlotter = plotter;
		buildToolActions(bars.getToolBarManager(), plotter, parent.getShell(), plotter.getColourTable());
		buildMenuActions(bars.getMenuManager(), plotter);
		setInitialPersp = false;
	}
	
	@Override
	public void disposeOverlays() {
		// Nothing to do
	}

	@SuppressWarnings("null")
	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
		Collection<DatasetWithAxisInformation> plotData = dbPlot.getData();
		
		if (plotData != null) {
			boolean individualXAxis = false;
			Iterator<DatasetWithAxisInformation> iter = plotData.iterator();
			final List<Dataset> datasets = Collections.synchronizedList(new LinkedList<Dataset>());
			// check for x-axis data
			xAxis.clear();
			AxisMode xAxisMode = AxisMode.LINEAR;
			AxisMode zAxisMode = AxisMode.LINEAR;
			Dataset xAxisValues = dbPlot.getAxis(AxisMapBean.XAXIS);
			
			if (xAxisValues != null) {
				if (xAxisValues.getName() != null && xAxisValues.getName().length() > 0)
					mainPlotter.setXAxisLabel(xAxisValues.getName());
				else
					mainPlotter.setXAxisLabel("X-Axis");
				xAxisMode = AxisMode.CUSTOM;
				xAxis.setValues(xAxisValues);
				mainPlotter.setXAxisValues(xAxis, plotData.size());
			} else {
				Dataset testValues = dbPlot.getAxis(AxisMapBean.XAXIS+"0");
				mainPlotter.setXAxisLabel("X-Axis");
				if (testValues != null)
				{
					xAxisMode = AxisMode.CUSTOM;
					individualXAxis = true;
				}	
			}
			mainPlotter.setYAxisLabel("Y-Axis");			
			Dataset zAxisValues = dbPlot.getAxis(AxisMapBean.ZAXIS);
			if (zAxisValues != null) {
				if (zAxisValues.getName() != null && zAxisValues.getName().length() > 0)
					mainPlotter.setZAxisLabel(zAxisValues.getName());
				else
					mainPlotter.setZAxisLabel("Z-Axis");
				zAxis.clear();
				zAxisMode = AxisMode.CUSTOM;
				zAxis.setValues(zAxisValues);
				mainPlotter.setZAxisValues(zAxis);
			} else {
				mainPlotter.setZAxisLabel("Z-Axis");
				mainPlotter.setZAxisValues(null);
			}
			mainPlotter.setAxisModes(xAxisMode, AxisMode.LINEAR, zAxisMode);
			mainPlotter.setXTickLabelFormat(TickFormatting.plainMode);
			mainPlotter.setYTickLabelFormat(TickFormatting.plainMode);
			mainPlotter.setZTickLabelFormat(TickFormatting.plainMode);
			Plot1DGraphTable colourTable = mainPlotter.getColourTable();
			colourTable.clearLegend();
			int axisCounter = 0;
			LinkedList<AxisValues> xAxisValuesList = null;
			if (individualXAxis)
				xAxisValuesList = new LinkedList<AxisValues>();
			
			while (iter.hasNext()) {
				DatasetWithAxisInformation dataSetAxis = iter.next();
				//AxisMapBean mapBean = dataSetAxis.getAxisMap();
				Dataset data = dataSetAxis.getData();
				if (individualXAxis) 
				{
					String axisStr = AxisMapBean.XAXIS + axisCounter;
					Dataset testValues = dbPlot.getAxis(axisStr);
					if (testValues != null) {
						AxisValues xaxis = new AxisValues(testValues);
						xAxisValuesList.add(xaxis);
					}
					axisCounter++;
				}
				
				Plot1DAppearance newApp = 
					new Plot1DAppearance((colourLegend.isChecked() ? 
							PlotColorUtility.getDefaultColour(colourTable.getLegendSize())
								: java.awt.Color.BLACK), Plot1DStyles.SOLID, data.getName());
				colourTable.addEntryOnLegend(newApp);
				datasets.add(data);
			}
			mainPlotter.setPlotUpdateOperation(isUpdate);
			if (individualXAxis) {
				try {
					mainPlotter.replaceAllPlots(datasets, xAxisValuesList);
				} catch (PlotException e) {
					e.printStackTrace();
				}
			} else {
				try {
					mainPlotter.replaceAllPlots(datasets);
				} catch (PlotException ex) {}
			}
			
			//we set the plot/file name
			String title = "";
			if (page.getActiveEditor()!=null)
				title = page.getActiveEditor().getTitle();
			mainPlotter.setTitle(title);
	
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					//expansionAction.setScale((int)(100 * mainPlotter.stackPlotGetZAxisLengthFactor()));
					if (!setInitialPersp) {
						mainPlotter.setPerspectiveCamera(getCameraPerspectiveChoice() == 1, false);
						setInitialPersp = true;
					}
					mainPlotter.refresh(true);
					mainPlotter.updateAllAppearance();
					plotWindow.notifyUpdateFinished();
				}
			});
		}
	}

	private void buildToolActions(IToolBarManager manager,
			final DataSetPlotter plotter, final Shell shell,
			final Plot1DGraphTable colourTable) {

		zoomAction = new Action() {
			@Override
			public void run() {
				plotter.undoZoom();
			}
		};
		zoomAction.setText("Undo zoom");
		zoomAction.setToolTipText("Undo a zoom level");
		zoomAction.setImageDescriptor(AnalysisRCPActivator
				.getImageDescriptor("icons/minify.png"));
		colourLegend = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				Plot1DGraphTable colourTable = plotter.getColourTable();
				for (int i = 0; i < colourTable.getLegendSize(); i++) {
					Plot1DAppearance app = colourTable.getLegendEntry(i);
					app.setColour((colourLegend.isChecked() ? PlotColorUtility.getDefaultColour(i) : java.awt.Color.BLACK));
				}
				plotter.updateAllAppearance();
				plotter.refresh(true);				
			}
		};
		
		colourLegend.setText("Use colours");
		colourLegend.setToolTipText("Switch between monochrome and colour");
		colourLegend.setChecked(true);
		colourLegend.setImageDescriptor(AnalysisRCPActivator
				.getImageDescriptor("icons/rainbow.png"));

		changeColour = new Action() {
			@Override
			public void run() {
				PlotAppearanceDialog pad = new PlotAppearanceDialog(shell,
						colourTable);
				boolean success = pad.open();
				if (success) {
					plotter.updateAllAppearance();
					plotter.refresh(true);
				}
			}
		};

		changeColour.setText("Change Plot appearance");
		changeColour.setToolTipText("Change the appearance of a plot");
		changeColour.setImageDescriptor(AnalysisRCPActivator
				.getImageDescriptor("icons/color_wheel.png"));
		activateRegionZoom = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				activateAreaZoom.setChecked(false);
				plotter.setZoomEnabled(activateRegionZoom.isChecked());
				plotter.setZoomMode(false);
			}
		};

		activateRegionZoom.setText("Region Zoom");
		activateRegionZoom.setToolTipText("Region zoom mode");
		activateRegionZoom.setImageDescriptor(AnalysisRCPActivator
				.getImageDescriptor("icons/magnify.png"));
		activateAreaZoom = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				activateRegionZoom.setChecked(false);
				plotter.setZoomEnabled(activateAreaZoom.isChecked());
				plotter.setZoomMode(true);
			}
		};

		activateAreaZoom.setText("Area Zoom");
		activateAreaZoom.setToolTipText("Area zoom mode");
		activateAreaZoom.setImageDescriptor(AnalysisRCPActivator
				.getImageDescriptor("icons/zoom_in.png"));

		tooglePerspectiveAction = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.setPerspectiveCamera(tooglePerspectiveAction.isChecked(),true);
			}
		};
		tooglePerspectiveAction.setImageDescriptor(AnalysisRCPActivator.
													getImageDescriptor("icons/camera_add.png"));
		tooglePerspectiveAction.setText("Switch Ortho/Persp");
		tooglePerspectiveAction.setToolTipText("Switch between orthographic and perspective view");
		tooglePerspectiveAction.setChecked(getCameraPerspectiveChoice() == 1);
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
		//expansionAction = new SliderAction(plotter);
		manager.add(activateRegionZoom);
		manager.add(activateAreaZoom);
		manager.add(zoomAction);
		manager.add(colourLegend);
		manager.add(changeColour);
		manager.add(tooglePerspectiveAction);
		manager.add(resetView);
		manager.add(boundingBox);
		//manager.add(expansionAction);
		manager.update(true);
	}
	
	private void buildMenuActions(IMenuManager manager, final DataSetPlotter plotter)
	{
		xLabelTypeRound = new Action()
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

		xLabelTypeFloat = new Action()
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

		xLabelTypeExponent = new Action()
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

		xLabelTypeSI = new Action()
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

		manager.add(xLabelTypeRound);
		manager.add(xLabelTypeFloat);
		manager.add(xLabelTypeExponent);
		manager.add(xLabelTypeSI);
	}
	
	private int getCameraPerspectiveChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ) ? 
				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ)
				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_MULTI1D_CAMERA_PROJ);
	}	
	
}
