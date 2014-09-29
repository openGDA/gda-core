/*-
 * Copyright © 2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.beamline.synoptics.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
/**
 * A generic view to display detector data file(s) content as plot in selected plot type for new plot or plot over existing data
 * 
 * For usage example please see {@link DetectorFileDisplayer}
 */
public class DetectorFilePlotView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(DetectorFilePlotView.class);
	public static final String ID = "uk.ac.gda.beamline.i11.views.DetectorFilePlotView";
	private IPlottingSystem plottingSystem = null;

	@Override
	public void createPartControl(Composite parent) {
		Composite plotComposite = new Composite(parent, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());
		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComposite, "DataDisplayer", getViewSite().getActionBars(),
					PlotType.XY, this);
			plottingSystem.setTitle("View collected data from detector data files");
			plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
			plottingSystem.getSelectedXAxis().setFormatPattern("######.#");

		} catch (Exception e) {
			logger.error("Failed to create a plotting system object", e);
		}
	}

	private ILineTrace lineTrace;
	ArrayList<IDataset> plotDataSets = new ArrayList<IDataset>();
	IDataset xAxisDataset;
	List<ITrace> lineTraces = null;
	ITrace imageTrace;
	PlotType currentPlotType=PlotType.XY;

	public void updatePlot(final IProgressMonitor monitor, IDataset xvalues, IDataset yvalues, final String title,
			final String xAxisName, final String yAxisName, boolean newPlot, PlotType plotType) {
		if (newPlot) {
			plottingSystem.reset();//clear();
			plotDataSets.clear();
		} 
		if (currentPlotType!=plotType) {
			plottingSystem.clear();
			currentPlotType=plotType;
		}
		// To support PlotType changes without data file changes.
		if (xvalues!=null) { 
			xAxisDataset=xvalues;
		}
		if (yvalues != null) {
			plotDataSets.add(yvalues);
		}
		plottingSystem.setPlotType(plotType);
		if (plotType == PlotType.XY || plotType == PlotType.XY_STACKED || plotType == PlotType.XY_STACKED_3D) {
			lineTraces = plottingSystem.updatePlot1D(xAxisDataset, plotDataSets,  monitor);
			if (!Display.getDefault().isDisposed()) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (lineTraces != null && !lineTraces.isEmpty() && lineTraces.size() == 1) {
							plottingSystem.setShowLegend(true);
							plottingSystem.setTitle(title);
							ITrace iTrace = lineTraces.get(0);
							if (iTrace instanceof ILineTrace) {
								lineTrace = (ILineTrace) iTrace;
								lineTrace.setTraceColor(ColorConstants.blue);
							} 
							plottingSystem.getSelectedYAxis().setTitle(xAxisName);
							plottingSystem.getSelectedYAxis().setTitle(yAxisName);
						}
						// plottingSystem.autoscaleAxes();
					}
				});
			}
		} else {
			imageTrace = plottingSystem.updatePlot2D(xvalues, plotDataSets, monitor);
			if (!Display.getDefault().isDisposed()) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {

						if (imageTrace != null) {
							// plottingSystem.setShowLegend(true);
							plottingSystem.setTitle(title);
						}
						// plottingSystem.autoscaleAxes();
					}
				});
			}
		}
	}


	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		super.dispose();
	}

	public void clearPlots() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.setTitle("");
				plottingSystem.reset();
			}
		});
	}

	@Override
	public void setFocus() {
		//plottingSystem.setFocus();
	}
}
