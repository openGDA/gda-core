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

import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.dawnsci.plotting.jreality.tool.PlotActionEvent;

import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.ISidePlotView;

public abstract class AbstractPlotUI implements IPlotUI {

	@Override
	public void deactivate(boolean leaveSidePlotOpen) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void disposeOverlays() {
	}

	@Override
	public ISidePlotView getSidePlotView() {
		return null;
	}

	@Override
	public ISidePlotView initSidePlotView() {
		return null;
	}

	@Override
	public void processGUIUpdate(GuiBean guiBean) {
	}

	@Override
	public void processPlotUpdate(DataBean dbPlot, boolean isUpdate) {
	}

	@Override
	public void areaSelected(AreaSelectEvent event) {
	}

	@Override
	public void plotActionPerformed(PlotActionEvent event) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public void addIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObserver(IObserver observer) {
	}
}
