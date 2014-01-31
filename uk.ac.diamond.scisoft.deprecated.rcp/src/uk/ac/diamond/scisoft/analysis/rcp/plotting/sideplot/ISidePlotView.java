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

import java.util.List;

import org.eclipse.jface.action.Action;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;


/**
 * This interface identifies a view that will contain
 * instances of <code>ISidePlot</code>
 * <p>
 * View clients should implement this interface if they intend
 * to host the SidePlot composites
 */
@Deprecated
public interface ISidePlotView {

	/**
	 * 
	 * @return the active plot or null if none active.
	 */
	public ISidePlot getActivePlot();
	
	/**
	 * Switch the side plot selected by the index to the front
	 * of the view 
	 * 
	 * @param plotUI UI for plotting
	 * @param index index identifying the selected side plot
	 */
	void switchSidePlot(IPlotUI plotUI, int index);

	/**
	 * Create a list of actions for toolbar
	 * @param plotUI
	 * @return list of Actions
	 */
	public List<Action> createSwitchActions(IPlotUI plotUI);

	/**
	 * Set list of actions
	 * @param actions
	 */
	public void setSwitchActions(List<Action> actions);
	
}
