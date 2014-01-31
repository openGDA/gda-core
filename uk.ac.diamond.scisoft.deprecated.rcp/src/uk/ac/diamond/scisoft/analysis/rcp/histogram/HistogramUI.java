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

package uk.ac.diamond.scisoft.analysis.rcp.histogram;

import gda.observable.IObserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IActionBars;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.AbstractPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView;

/**
 *
 */
@Deprecated
public class HistogramUI extends AbstractPlotUI {

	private List<IObserver> observers = 
		Collections.synchronizedList(new LinkedList<IObserver>());
	
	private static HashMap<String, Boolean> autoScaleSettings = new HashMap<String,Boolean>();
	
	private Action zoomAction;
	private Action activateZoom;
	private Action lockScale;
	private Action autoScale;
	private Action showGraphLines;
	private HistogramView histoView;
	/**
	 * @param view
	 * @param bars
	 * @param plotter
	 * 
	 */
	
	public HistogramUI(HistogramView view,
					   IActionBars bars, 
			           final DataSetPlotter plotter)
	{
		this.histoView = view;
		buildToolActions(bars.getToolBarManager(), plotter);
	}	
	

	private void buildToolActions(IToolBarManager manager, final DataSetPlotter plotter)
	{
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
		activateZoom = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setZoomEnabled(activateZoom.isChecked());
			}
		};

		activateZoom.setText("Zoom");
		activateZoom.setToolTipText("Zoom mode");
		activateZoom.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/magnify.png"));
		autoScale = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				if (!lockScale.isChecked()) {
					histoView.setAutoContrastScaling(autoScale.isChecked());
					autoScaleSettings.put(histoView.getPartName(),autoScale.isChecked());
					histoView.createInitialHistogram();
				} else 
					autoScale.setChecked(false);
			}
		};
		autoScale.setText("Autoscale contrast");
		autoScale.setToolTipText("Automatically scale contrast");
		autoScale.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/contrast_out.png"));
		autoScale.setChecked(getPreferenceAutoConstrastChoice());
		histoView.setAutoContrastScaling(autoScale.isChecked());
		
		lockScale = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				histoView.setHistogramLock(lockScale.isChecked());
				if (lockScale.isChecked()) {
					histoView.setAutoContrastScaling(false);
					autoScale.setChecked(false);
				}
			}
		};
		lockScale.setText("Lock range");
		lockScale.setToolTipText("Lock mapping range for subsequent images");
		lockScale.setChecked(false);
		lockScale.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/lock.png"));
		
		showGraphLines = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				histoView.setGraphLines(showGraphLines.isChecked());
			}
		};
		showGraphLines.setText("Show channel graphs");
		showGraphLines.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/chart_curve.png"));
		showGraphLines.setToolTipText("Active/deactivate colour channel graphs");
		showGraphLines.setChecked(true);
		
		manager.add(activateZoom);
		manager.add(zoomAction);
		manager.add(autoScale);
		manager.add(lockScale);
		manager.add(showGraphLines);
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
	public void plotActionPerformed(PlotActionEvent event) {
		// Nothing to do

	}
	
	private void notifyObservers(Object event)
	{
		Iterator<IObserver> iter = observers.iterator();
		while (iter.hasNext())
		{
			IObserver ob = iter.next();
			ob.update(this, event);
		}		
	}
	

	@Override
	public void areaSelected(AreaSelectEvent event) {
		// pass on the AreaSelectEvent via the Object observed 
		// mechanism, yes repacking the event object is a bit evil
		
		notifyObservers(event);        
	}


	private boolean getPreferenceAutoConstrastChoice() {
		if (histoView != null &&
			autoScaleSettings.get(histoView.getPartName()) != null) {
			return autoScaleSettings.get(histoView.getPartName());
		}
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST) ? 
				preferenceStore.getDefaultBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST)
				: preferenceStore.getBoolean(PreferenceConstants.PLOT_VIEW_PLOT2D_AUTOCONTRAST);
	}
	
}
