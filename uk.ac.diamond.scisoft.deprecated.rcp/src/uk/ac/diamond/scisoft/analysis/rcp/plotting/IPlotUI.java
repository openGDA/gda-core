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

package uk.ac.diamond.scisoft.analysis.rcp.plotting;
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
import gda.observable.IObservable;

import org.dawnsci.plotting.jreality.tool.AreaSelectEventListener;
import org.dawnsci.plotting.jreality.tool.PlotActionEventListener;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlotView;

/**
 * Generic interface for Plotting UI attached to different Plotters
 */
public interface IPlotUI extends IObservable, PlotActionEventListener, AreaSelectEventListener {

	/**
	 * Process a plot update
	 * 
	 * @param dbPlot DataBean containing the new plot
	 * @param isUpdate is this an update of an existing plot?
	 */
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate);

	/**
	 * Unregister and dispose all overlays associated to the current plot,
	 * since PlotUI is currently the master on them
	 */
	public void disposeOverlays();

	/**
	 * Deactivate the UI this can be used to do some additional actions before
	 * the UI gets removed
	 */
	public void deactivate(boolean leaveSidePlotOpen);

	/**
	 * Process a GUI update. Implement this synchronously
	 * @param guiBean
	 */
	public void processGUIUpdate(GuiBean guiBean);

	/**
	 * @return a side plot view
	 */
	public ISidePlotView getSidePlotView();

	/**
	 * Initialise side plot view
	 */
	public ISidePlotView initSidePlotView();

	/**
	 * Called when ui is no longer needed
	 */
	public void dispose();

}
