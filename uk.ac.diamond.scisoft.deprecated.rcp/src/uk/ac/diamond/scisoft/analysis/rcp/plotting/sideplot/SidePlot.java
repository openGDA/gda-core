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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.ac.diamond.scisoft.analysis.plotserver.IGuiInfoManager;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IMainPlot;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;

/**
 * A side plot control to populate the tab folder
 */
@Deprecated
abstract public class SidePlot implements ISidePlot {
	
	protected IGuiInfoManager guiUpdateManager;
	protected IMainPlot       mainPlotter;
	protected IPlotUI         mainPlotUI;
	
	protected long            updateInterval = 0;
	protected long            nextTime = 0;
	protected Composite       container;
	protected boolean         isDisposed = true;

	/**
	 */
	public SidePlot() {
	}
	
	@Override
	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
		nextTime = System.currentTimeMillis() + updateInterval;
	}

	@Override
	public void setGuiInfoManager(IGuiInfoManager guiInfoManager) {
		this.guiUpdateManager = guiInfoManager;
	}

	@Override
	public void setMainPlotter(IMainPlot mainPlotter) {
		this.mainPlotter = mainPlotter;
	}

	@Override
	public IMainPlot getMainPlotter() {
		return mainPlotter;
	}

	@Override
	public void processHistogramUpdate(HistogramUpdate update) {
	}

	/**
	 * By default, this implementation creates an action box. You need to override and call the superclass
	 * method and then decorate the returned action with text, tool-tip text and icon
	 */
	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotUI.getSidePlotView().switchSidePlot(plotUI, index);
				if (mainPlotter instanceof DataSetPlotter) // TODO decide whether this is necessary
					((DataSetPlotter) mainPlotter).getComposite().setFocus();
			}
		};
		return action;
	}

	@Override
	public Control getControl() {
		return container;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	@Override
	public void setDisposed(boolean isDisposed) {
		this.isDisposed = isDisposed;
	}

	/**
	 * If this method is overridden in a subclass, that subclass method <b>must</b> call
	 * its super class method for side plots to operate correctly.
	 * @see ISidePlot#dispose()
	 */
	@Override
	public void dispose() {
		guiUpdateManager = null;
		mainPlotter      = null;
		mainPlotUI       = null;
		if (container != null && !container.isDisposed()) {
			container.dispose();
		}
		container = null;
		isDisposed = true;
	}

	@Override
	public void setMainPlotUI(IPlotUI plotUI) {
		this.mainPlotUI = plotUI;
	}

	protected IPlotUI getMainPlotUI() {
		return mainPlotUI;
	}
}
