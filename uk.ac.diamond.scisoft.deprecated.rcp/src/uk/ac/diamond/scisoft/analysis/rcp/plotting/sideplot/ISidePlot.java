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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import org.dawnsci.plotting.jreality.overlay.OverlayConsumer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IMainPlot;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;

/**
 * An ISidePlot is the interface that a side plot needs to implement to provide
 * information and user interaction with the contents of a plot view.
 * <p>
 * Side plots are held in a side plot view associated with a plot view. A set of action
 * buttons in the plot view allow side plots to be selected and displayed.
 * <p>
 * Each side plot is expected to create its GUI and provide a switch action to populate
 * the tool bar of a plot view. It is important to note that the GUI creation is delayed
 * until the first time its corresponding switch action is used.
 * <p>
 * On activation, a side plot should get GUI state information from the GUI information
 * manager and get data from the main plot.
 * <p>
 * It should respond to updates from histogram changes, main plot data changes and GUI
 * information changes. Note these changes are <b>only</b> sent to the current sideplot.
 * It should manipulate its plots histories as commanded.
 * <p>
 * It should send back updates to GUI information at time intervals greater than or equal to
 * the set update interval.
 */
@Deprecated
public interface ISidePlot extends OverlayConsumer {

	/**
	 * Set the interval at which updates will be pushed to the server
	 * 
	 * @param updateInterval The update interval in milliseconds to set.
	 */
	public void setUpdateInterval(long updateInterval);

	/**
	 * Set the GUI info manager associated with this ISidePlot.
	 * The GUI info manager is used to get updates to and from the server
	 * 
	 * @param plotView The plotView to set
	 */
	public void setGuiInfoManager(IGuiInfoManager plotView);

	/**
	 * Set the main plot associated with this ISidePlot
	 * 
	 * @param mainPlotter The mainPlotter to set
	 */
	public void setMainPlotter(IMainPlot mainPlotter);
	
	/**
	 * This is called to set the plotUI being used by the main plot. This can
	 * be used to find out if the UI is zooming and dimension specific
	 * things.
	 * 
	 * @param plotUI
	 */
	public void setMainPlotUI(IPlotUI plotUI);

	/**
	 * Returns the DataSetPlotter associated with this ISidePlot
	 * <p>
	 * May return <code>null</code> if the DataSetPlotter
	 * has not been created yet.
	 * </p>
	 * @return the top level DataSetPlotter or <code>null</code>
	 */
	public IMainPlot getMainPlotter();

	/**
	 * Create contents of the side plot
	 * 
	 * @param parent
	 */
	public void createPartControl(Composite parent);

	/**
	 * The last method call on the side plot, when it is to perform required cleanup. 
	 * Clients should only call dispose after the side plot has been created with 
	 * <code>createPartControl</code>
	 */
	public void dispose();

	/**
	 * @return true if side plot's GUI has been disposed
	 */
	public boolean isDisposed();

	/**
	 * Set state of GUI disposal
	 * @param isDisposed
	 */
	public void setDisposed(boolean isDisposed);

	/**
	 * Notify that main plot has updated.
	 *
	 * NB this is called in an asynchronous block
	 * 
	 * All primitives have been removed and the consumer unregistered prior to this call!
	 * So do not use any overlays.
	 */
	public void processPlotUpdate();

	/**
	 * @param index 
	 * @param plotUI A plot UI that implements the IPlotUI interface
	 * 
	 * @return toolbar action for switching control to this side plot 
	 */
	public Action createSwitchAction(final int index, final IPlotUI plotUI);

	/**
	 * Add current plots in a side plot to the respective storage (its history) of those plot
	 */
	public void addToHistory();

	/**
	 * Remove last stored plots from history of the plots in a side plot
	 */
	public void removeFromHistory();

	/**
	 * Service colour map change from histogram view
	 *
	 * @param update
	 */
	public void processHistogramUpdate(HistogramUpdate update);

	/**
	 * Returns the top level control for this side plot 
	 * <p>
	 * May return <code>null</code> if the control
	 * has not been created yet.
	 * </p>
	 * @return the top level control or <code>null</code>
	 */
	public Control getControl();

	/**
	 * This is called when the side plot view is switched to this side plot
	 */
	public void showSidePlot();

	/**
	 * Update GUI based on information from bean
	 * @param bean
	 * @return state defined by flag with following bit masks: ROI, ROILIST, PREFS
	 */
	public int updateGUI(GuiBean bean);

	/**
	 * Generate tool actions and add to manager
	 * @param manager
	 */
	public void generateToolActions(IToolBarManager manager);

	/**
	 * Generate menu actions and add to manager
	 * @param manager
	 * @param site
	 */
	public void generateMenuActions(IMenuManager manager, IWorkbenchPartSite site);
	
	/**
	 * Indicates that a ROI was updated
	 */
	int ROI = 1;

	/**
	 * Indicates that a ROI list was updated
	 */
	int ROILIST = 2;

	/**
	 * Indicates that a preference was updated
	 */
	int PREFS = 4;

}
