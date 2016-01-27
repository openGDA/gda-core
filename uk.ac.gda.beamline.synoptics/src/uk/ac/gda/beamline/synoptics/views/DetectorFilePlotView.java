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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	ArrayList<IDataset> plotDataSets = new ArrayList<IDataset>();
	IDataset xAxisDataset;
	PlotType currentPlotType=PlotType.XY;
	private boolean first;

	public void updatePlot(final IProgressMonitor monitor, final IDataset xData, final IDataset yData, final String title,
			final String xAxisName, final String yAxisName, boolean newPlot, PlotType plotType) {
		if (newPlot) {
			plottingSystem.reset();//clear();
			first=true;
		} 
		if (currentPlotType!=plotType) {
//			plottingSystem.clear();
			currentPlotType=plotType;
			plottingSystem.setPlotType(plotType);
		}

		if (plotType == PlotType.XY || plotType == PlotType.XY_STACKED || plotType == PlotType.XY_STACKED_3D) {
			
			if (!Display.getDefault().isDisposed()) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						final ILineTrace lineTrace = plottingSystem.createLineTrace(yData.getName());
						lineTrace.setData(xData, yData);
						plottingSystem.addTrace(lineTrace);
						if (first) {
							plottingSystem.setShowLegend(true);
							plottingSystem.setTitle(title);
							lineTrace.setTraceColor(ColorConstants.blue);
							plottingSystem.getSelectedYAxis().setTitle(xAxisName);
							plottingSystem.getSelectedYAxis().setTitle(yAxisName);
							first=false;
						}
//						plottingSystem.repaint(true);
						plottingSystem.repaint();
					}
				});
			}
		} else {
			if (!Display.getDefault().isDisposed()) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						IImageTrace imageTrace = plottingSystem.createImageTrace(yData.getName());
						imageTrace.setMax(yData.max(null));
						imageTrace.setMin(yData.min(null));
						imageTrace.setMask(null);
						imageTrace.setData(xData, null, false);
						plottingSystem.addTrace(imageTrace);
						
						if (first) {
							// plottingSystem.setShowLegend(true);
							plottingSystem.setTitle(title);
							first=false;
						}
//						 plottingSystem.repaint(true);
						 plottingSystem.repaint();
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
