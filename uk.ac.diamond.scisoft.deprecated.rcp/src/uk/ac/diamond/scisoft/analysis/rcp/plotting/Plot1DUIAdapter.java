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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.dawnsci.plotting.jreality.tick.TickFormatting;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;
import org.eclipse.dawnsci.plotting.api.jreality.core.ScaleType;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;

import com.swtdesigner.ResourceManager;


/**
 * Class which can be extended to create custom toolbars.
 */
@Deprecated
public class Plot1DUIAdapter extends AbstractPlotUI {

	/**
	 * String placeholder for the History plots
	 */
	public final static String HISTORYSTRING = "History";

	protected List<IObserver> observers = Collections.synchronizedList(new LinkedList<IObserver>());
	protected static DataSetPlotter plotter;
	protected Composite      parent;
	
	// Actions could be in a map with a protected getter method.
	protected Action zoomAction;
	protected Action resetZoomAction;
	protected Action activateRegionZoom;
	protected Action activateAreaZoom;
	protected Action displayPlotPos;
	protected Action addToHistory;
	protected Action removeFromHistory;
	protected Action rightClickOnGraphAction;
	protected Action changeColour;
	protected Action activateXgrid;
	protected Action activateYgrid;
	protected Action xLabelTypeRound;
	protected Action xLabelTypeFloat;
	protected Action xLabelTypeExponent;
	protected Action xLabelTypeSI;
	protected Action yLabelTypeRound;
	protected Action yLabelTypeFloat;
	protected Action yLabelTypeExponent;
	protected Action yLabelTypeSI;
	protected Action yAxisScaleLog;
	protected Action yAxisScaleLinear;
	protected Action saveGraph;
	protected Action copyGraph;
	protected Action printGraph;
	protected Action toggleXAxisErrorBars;
	protected Action toggleYAxisErrorBars;
	protected Action toggleZAxisErrorBars;
	protected String viewName;
	protected String xAxisModePref;
	protected String yAxisModePref;
	protected String xGridLinePref;
	protected String yGridLinePref;
	protected String numAppsPref;
	protected static final Logger logger = LoggerFactory.getLogger(Plot1DUIAdapter.class);

	protected String[] listPrintScaleText = { ResourceProperties.getResourceString("PRINT_LISTSCALE_0"),
			ResourceProperties.getResourceString("PRINT_LISTSCALE_1"), ResourceProperties.getResourceString("PRINT_LISTSCALE_2"),
			ResourceProperties.getResourceString("PRINT_LISTSCALE_3"), ResourceProperties.getResourceString("PRINT_LISTSCALE_4"),
			ResourceProperties.getResourceString("PRINT_LISTSCALE_5"), ResourceProperties.getResourceString("PRINT_LISTSCALE_6") };
	protected String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	protected String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	protected String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	protected String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	protected String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	protected String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	protected String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	protected String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	protected String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

	/**
	 * Constructor of a AbstractPlot1DUI 
	 * @param plotter Plotter object for parsing information back to the plotter
	 * @param parent parent composite 
	 * @param viewName name of the view this UI is associated to
	 */
	public Plot1DUIAdapter(final DataSetPlotter plotter,
							final Composite parent,
							String viewName) {
	
		this.parent = parent;
		Plot1DUIAdapter.plotter = plotter;
		this.viewName = viewName;
		createAllActions();
		readAndSetPreferences();
		Plot1DUIAdapter.plotter.registerUI(this);
	}

	/**
	 * Constructor of a Plot1DUI 
	 * @param bars ActionBars from the parent view
	 * @param plotter Plotter object for parsing information back to the plotter
	 * @param parent parent composite 
	 * @param viewName name of the view this UI is associated to
	 */
	public Plot1DUIAdapter(IActionBars bars, 
							final DataSetPlotter plotter,
							Composite parent,
							String viewName) {
	
		this(plotter,parent,viewName);
		buildToolActions(bars.getToolBarManager());
		buildMenuActions(bars.getMenuManager());
		buildStatusLineItems(bars.getStatusLineManager());
	}

	protected IAction createShowLegend() {
		return createShowLegend(plotter);
	}
	
	public static IAction createShowLegend(final DataSetPlotter plotter) {
		Action action = new Action("Show legend", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (plotter==null) return;
				plotter.setUseLegend(isChecked());
			}
		};
		action.setImageDescriptor(ResourceManager.getImageDescriptor(Plot1DUIAdapter.class, "/icons/application_tile_vertical.png"));
		return action;
	}

	/**
	 * Constructor of a Plot1DUI for just the toolbar.
	 * @param toolBar to use with the graph
	 * @param plotter Plotter object for parsing information back to the plotter
	 * @param parent parent composite 
	 * @param viewName name of the view this UI is associated to
	 */
	
	public Plot1DUIAdapter(IToolBarManager toolBar, 
							final DataSetPlotter plotter,
							Composite parent,
							String viewName) {
	
		this(plotter,parent,viewName);
		buildToolActions(toolBar);
	}	
	
	/**
	 * Please implement to setup the actions, default nothing. @see Plot1DUIComplete
	 * @param manager
	 */
	public void buildMenuActions(@SuppressWarnings("unused") IMenuManager manager) {
	}
	
	/**
	 * Please overrider, default nothing. @see Plot1DUIComplete
	 * @param manager
	 */
	public void buildStatusLineItems(@SuppressWarnings("unused") IStatusLineManager manager) {
		
	}
	
	/**
	 * Please implement to setup the actions. @see Plot1DUIComplete
	 * @param manager
	 */
	public void buildToolActions(IToolBarManager manager) {
		manager.add(activateRegionZoom);
		manager.add(activateAreaZoom);
		manager.add(zoomAction);
		manager.add(changeColour);
		manager.add(activateXgrid);
		manager.add(activateYgrid);
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
	}

	/**
	 * 
	 */
	protected void createAllActions() {
		
		xLabelTypeRound = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(xAxisModePref,1);
				plotter.refresh(true);
			}
		};
		xLabelTypeRound.setText("X-Axis labels integer");
		xLabelTypeRound.setToolTipText("Change the labelling on the x-axis to integer numbers");

		xLabelTypeFloat = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setXTickLabelFormat(TickFormatting.plainMode);
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(xAxisModePref,0);
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
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(xAxisModePref,2);
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
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(xAxisModePref,3);
				plotter.refresh(true);
			}
		};
		xLabelTypeSI.setText("X-Axis labels SI units");
		xLabelTypeSI.setToolTipText("Change the labelling on the x-axis to using SI units");
		// y axis 
		yLabelTypeRound = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(yAxisModePref,1);
				plotter.refresh(true);
			}
		};
		yLabelTypeRound.setText("Y-Axis labels integer");
		yLabelTypeRound.setToolTipText("Change the labelling on the x-axis to integer numbers");

		yLabelTypeFloat = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYTickLabelFormat(TickFormatting.plainMode);
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(yAxisModePref,0);
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
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(yAxisModePref,2);
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
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(yAxisModePref,3);
				plotter.refresh(true);
			}
		};
		yLabelTypeSI.setText("Y-Axis labels SI units");
		yLabelTypeSI.setToolTipText("Change the labelling on the y-axis to using SI units");
		yAxisScaleLinear = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYAxisScaling(ScaleType.LINEAR);
			}
		};
		yAxisScaleLinear.setText("Y-Axis scale linear");
		yAxisScaleLinear.setToolTipText("Change the Y-Axis scaling to be linear");
		
		yAxisScaleLog = new Action("",IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				plotter.setYAxisScaling(ScaleType.LN);
			}
		};
		yAxisScaleLog.setText("Y-Axis scale logarithmic");
		yAxisScaleLog.setToolTipText("Change the Y-Axis scaling to be logarithmic (natural)");
	
		addToHistory = new Action() {
			@Override
			public void run()
			{
				Plot1DAppearance plotApp = 
					new Plot1DAppearance(PlotColorUtility.getDefaultColour(plotter.getColourTable().getLegendSize()),
							             Plot1DStyles.SOLID,HISTORYSTRING+" "+(plotter.getNumHistory()+1));
				plotter.getColourTable().addEntryOnLegend(plotApp);
				plotter.pushGraphOntoHistory();
				notifyObservers();
				plotter.refresh(false);
			}
		};
		
		addToHistory.setText("Add current line to history");
		addToHistory.setToolTipText("Adds the current line to the plot history");
		addToHistory.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/basket_put.png"));
		removeFromHistory = new Action() {
			@Override
			public void run()
			{
				
				if (plotter.getNumHistory() > 0) {
					plotter.getColourTable().deleteLegendEntry(plotter.getColourTable().getLegendSize()-1);					
				    plotter.popGraphFromHistory();
				    plotter.refresh(true);
				}
			}
		};
		removeFromHistory.setText("Remove last line from history");
		removeFromHistory.setToolTipText("Remove the last line from the plot history");
		removeFromHistory.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/basket_remove.png"));
		
		zoomAction = new Action()
		{
			@Override
			public void run()
			{
				plotter.undoZoom();
			}
		};
		zoomAction.setText("Undo zoom");
		zoomAction.setToolTipText("Undo a zoom level");
		zoomAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/minify.png"));
		resetZoomAction = new Action()
		{
			@Override
			public void run()
			{
				plotter.resetZoom();
			}
		};
		resetZoomAction.setText("Reset zoom");
		resetZoomAction.setToolTipText("Reset to no zoom");
		resetZoomAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/magifier_zoom_reset.png"));
		changeColour = new Action()
		{
			@Override
			public void run()
			{
				PlotAppearanceDialog pad = new PlotAppearanceDialog(parent.getShell(),plotter.getColourTable());
				boolean success = pad.open();
				if (success)
				{
					plotter.updateAllAppearance();
					plotter.refresh(true);
					updatePrefFromAppearance(plotter.getColourTable());
				}
			}
		};
		
		changeColour.setText("Change Plot appearance");
		changeColour.setToolTipText("Change the appearance of a plot");		
		changeColour.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/color_wheel.png"));
		activateRegionZoom = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				activateAreaZoom.setChecked(false);
				plotter.setZoomEnabled(activateRegionZoom.isChecked());
				plotter.setZoomMode(false);
				plotter.setPlotUpdateOperation(activateRegionZoom.isChecked());
			}
		};

		activateRegionZoom.setText("Region Zoom");
		activateRegionZoom.setToolTipText("Region zoom mode");
		activateRegionZoom.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/magnify.png"));
		activateAreaZoom = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				activateRegionZoom.setChecked(false);
				plotter.setZoomEnabled(activateAreaZoom.isChecked());
				plotter.setZoomMode(true);
				plotter.setPlotUpdateOperation(activateAreaZoom.isChecked());
			}
		};

		activateAreaZoom.setText("Area Zoom");
		activateAreaZoom.setToolTipText("Area zoom mode");
		activateAreaZoom.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/zoom_in.png"));

		displayPlotPos = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setPlotActionEnabled(displayPlotPos.isChecked());
			}
		};
		displayPlotPos.setText("Display graph position");
		displayPlotPos.setToolTipText("Display the position on the graph");
		displayPlotPos.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Cross-Hairs.png"));

		activateXgrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(activateXgrid.isChecked(), 
										 activateYgrid.isChecked(),false);
				plotter.refresh(false);
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(xGridLinePref,!activateXgrid.isChecked());
			}
		};
		activateXgrid.setChecked(true);
		activateXgrid.setText("X grid lines ON/OFF");
		activateXgrid.setToolTipText("Toggle x axis grid lines on/off");
		activateXgrid.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/text_align_justify_rot.png"));
		activateYgrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(activateXgrid.isChecked(), 
						 activateYgrid.isChecked(),false);
				plotter.refresh(false);
				AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(yGridLinePref,!activateYgrid.isChecked());
			}
		};
		activateYgrid.setChecked(true);
		activateYgrid.setText("Y grid lines ON/OFF");
		activateYgrid.setToolTipText("Toggle y axis grid lines on/off");
		activateYgrid.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/text_align_justify.png"));		

		saveGraph = new Action() {
			
			// Cache file name otherwise they have to keep
			// choosing the folder.
			private String filename;
			
			@Override
			public void run() {
				
				FileDialog dialog = new FileDialog (parent.getShell(), SWT.SAVE);
				
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

		rightClickOnGraphAction = new Action("Activate right click action.\nWith this action enabled you can right click on the graph to show the data within the current zoom area.\n\nPlease click precisely on the data you require.",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run() {
				plotter.setPlotRightClickActionEnabled(rightClickOnGraphAction.isChecked());
				if (rightClickOnGraphAction.isChecked()) {
					displayPlotPos.setChecked(false);
					plotter.setPlotActionEnabled(false);
				}
			}
		};
		rightClickOnGraphAction.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/Data-Analyse.png"));
		
		
		// Error Bar actions
		toggleXAxisErrorBars = new Action("Toggle x-Axis Error Bars",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run() {
				plotter.toggleErrorBars(toggleXAxisErrorBars.isChecked(),
						toggleYAxisErrorBars.isChecked(),
						toggleZAxisErrorBars.isChecked());				
			}
		};
		toggleXAxisErrorBars.setChecked(true);
		toggleXAxisErrorBars.setText("Toggle x-Axis Error Bars");
		toggleXAxisErrorBars.setToolTipText("Toggle x-Axis Error Bars");
		
		toggleYAxisErrorBars = new Action("Toggle y-Axis Error Bars",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run() {
				plotter.toggleErrorBars(toggleXAxisErrorBars.isChecked(),
						toggleYAxisErrorBars.isChecked(),
						toggleZAxisErrorBars.isChecked());	
				
			}
		};
		toggleYAxisErrorBars.setChecked(true);
		toggleYAxisErrorBars.setText("Toggle y-Axis Error Bars");
		toggleYAxisErrorBars.setToolTipText("Toggle y-Axis Error Bars");	
		
		toggleZAxisErrorBars = new Action("Toggle z-Axis Error Bars",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run() {
				plotter.toggleErrorBars(toggleXAxisErrorBars.isChecked(),
						toggleYAxisErrorBars.isChecked(),
						toggleZAxisErrorBars.isChecked());
				
			}
		};
		toggleYAxisErrorBars.setChecked(true);
		toggleZAxisErrorBars.setText("Toggle z-Axis Error Bars");
		toggleZAxisErrorBars.setToolTipText("Toggle z-Axis Error Bars");
		
	}
	
	public void addZoomListener(final IPropertyChangeListener ifZoom) {
		this.activateAreaZoom.addPropertyChangeListener(ifZoom);
		this.activateRegionZoom.addPropertyChangeListener(ifZoom);
	}
	public void removeZoomListener(final IPropertyChangeListener ifZoom) {
		this.activateAreaZoom.removePropertyChangeListener(ifZoom);
		this.activateRegionZoom.removePropertyChangeListener(ifZoom);
	}

	public void addPositionSwitchListener(final IPropertyChangeListener ifPositionMonitor) {
		this.displayPlotPos.addPropertyChangeListener(ifPositionMonitor);
	}
	public void removePositionSwitchListener(final IPropertyChangeListener ifPositionMonitor) {
		this.displayPlotPos.removePropertyChangeListener(ifPositionMonitor);
	}

	protected void notifyObservers()
	{
		Iterator<IObserver> iter = observers.iterator();
		while (iter.hasNext())
		{
			IObserver ob = iter.next();
			ob.update(this, null);
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
		observers.removeAll(observers);
	}
	
	private Set<PlotActionEventListener> plotListeners;
	/**
	 * Call to listen to PlotActionEvents outside the PlotUI mechanism
	 * @param l
	 */
	public void addPlotActionEventListener(final PlotActionEventListener l) {
		if (plotListeners==null) plotListeners = new LinkedHashSet<PlotActionEventListener>(3);
		plotListeners.add(l);
	}
	
	@Override
	public void plotActionPerformed(PlotActionEvent event) {		
	    if (plotListeners!=null) {
	    	for (PlotActionEventListener l : plotListeners) l.plotActionPerformed(event);
	    }
	}

	@Override
	public void dispose() {
		if (plotListeners!=null) plotListeners.clear();
		plotListeners = null;
	}
	
	private String convertColourToString(java.awt.Color color) {
		return ""+color.getRed()+";"+color.getGreen()+";"+color.getBlue();
	}
	
	private java.awt.Color convertStringToColour(String colourName) {
		String [] colourParts = colourName.split(";");
		try {
			return new java.awt.Color(Integer.parseInt(colourParts[0]),
									  Integer.parseInt(colourParts[1]),
									  Integer.parseInt(colourParts[2]));
		} catch (NumberFormatException ex) {
			return java.awt.Color.black;
		}
	}
	
	private void updatePrefFromAppearance(Plot1DGraphTable table) {
		AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(numAppsPref,table.getLegendSize());
		for (int i = 0; i < table.getLegendSize(); i++) {
			Plot1DAppearance plotApp = table.getLegendEntry(i);
			String colourPrefName = viewName+".app.Colour"+i;
			String lineWidthPrefName = viewName+".app.LineWidth"+i;
			String stylePrefName = viewName+".app.Style"+i;
			AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(colourPrefName,
																			convertColourToString(plotApp.getColour()));
			AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(lineWidthPrefName, plotApp.getLineWidth());
			int styleNr = 0;
			switch (plotApp.getStyle()) {
				case SOLID:
					styleNr = 0;
				break;
				case DASHED:
					styleNr = 1;
				break;
				case POINT:
					styleNr = 2;
				break;
				case SOLID_POINT:
					styleNr = 3;
				break;
				case DASHED_POINT:
					styleNr = 4;
				break;
			}
			AnalysisRCPActivator.getDefault().getPreferenceStore().setValue(stylePrefName, styleNr);
		}
	}
	
	private void updateAppearanceFromPref(int numApps) {
		for (int i = 0; i < numApps; i++) {
			String colourPrefName = viewName+".app.Colour"+i;
			String lineWidthPrefName = viewName+".app.LineWidth"+i;
			String stylePrefName = viewName+".app.Style"+i;
			String colourStr = 
				AnalysisRCPActivator.getDefault().getPreferenceStore().getString(colourPrefName);
			int lineWidth = 
				AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(lineWidthPrefName);
			int style = 
				AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(stylePrefName);
			Plot1DStyles plotStyle = Plot1DStyles.SOLID;
			switch (style) {
				case 1:
					plotStyle = Plot1DStyles.DASHED;
				break;
				case 2:
					plotStyle = Plot1DStyles.POINT;
				break;
				case 3:
					plotStyle = Plot1DStyles.SOLID_POINT;
				break;
				case 4:
					plotStyle = Plot1DStyles.DASHED_POINT;
				break;
				default:
					plotStyle = Plot1DStyles.SOLID;
			}
			PlotColorUtility.setDefaultColour(i, convertStringToColour(colourStr));
			PlotColorUtility.setDefaultStyle(i, plotStyle);
			PlotColorUtility.setDefaultLineWidth(i, lineWidth);
		}
	}
	
	protected void readAndSetPreferences() {
		xAxisModePref = viewName+".xAxisLabelMode";
		yAxisModePref = viewName+".yAxisLabelMode";
		numAppsPref = viewName+".numAppearance";
		xGridLinePref = viewName+".showXgrid";
		yGridLinePref = viewName+".showYgrid";
		int xLabelMode = AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(xAxisModePref);
		int yLabelMode = AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(yAxisModePref);
		int numApps = AnalysisRCPActivator.getDefault().getPreferenceStore().getInt(numAppsPref);
		boolean showXgrid = !AnalysisRCPActivator.getDefault().getPreferenceStore().getBoolean(xGridLinePref);
		boolean showYgrid = !AnalysisRCPActivator.getDefault().getPreferenceStore().getBoolean(yGridLinePref);
		switch (xLabelMode) {
			case 0:
				plotter.setXTickLabelFormat(TickFormatting.plainMode);
				xLabelTypeFloat.setChecked(true);
			break;
			case 1:
				plotter.setXTickLabelFormat(TickFormatting.roundAndChopMode);
				xLabelTypeRound.setChecked(true);
			break;
			case 2:
				plotter.setXTickLabelFormat(TickFormatting.useExponent);
				xLabelTypeExponent.setChecked(true);
			break;
			case 3:
				plotter.setXTickLabelFormat(TickFormatting.useSIunits);
				xLabelTypeSI.setChecked(true);
			break;
			default:
				plotter.setXTickLabelFormat(TickFormatting.plainMode);
				xLabelTypeFloat.setChecked(true);
		}
		switch (yLabelMode) {
			case 0:
				plotter.setYTickLabelFormat(TickFormatting.plainMode);
				yLabelTypeFloat.setChecked(true);
			break;
			case 1:
				plotter.setYTickLabelFormat(TickFormatting.roundAndChopMode);
				yLabelTypeRound.setChecked(true);
			break;
			case 2:
				plotter.setYTickLabelFormat(TickFormatting.useExponent);
				yLabelTypeExponent.setChecked(true);
			break;
			case 3:
				plotter.setYTickLabelFormat(TickFormatting.useSIunits);
				yLabelTypeSI.setChecked(true);
			break;
			default:
				plotter.setYTickLabelFormat(TickFormatting.plainMode);
				yLabelTypeFloat.setChecked(true);
		}
		plotter.setTickGridLines(showXgrid, showYgrid, false);
		activateXgrid.setChecked(showXgrid);
		activateYgrid.setChecked(showYgrid);
		updateAppearanceFromPref(numApps);
	}

}
