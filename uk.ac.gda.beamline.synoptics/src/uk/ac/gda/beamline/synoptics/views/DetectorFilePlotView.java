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

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
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
 */
public class DetectorFilePlotView extends ViewPart {
	public static final String ID = "uk.ac.gda.beamline.i11.views.DetectorFilePlotView";

	private static final Logger logger = LoggerFactory.getLogger(DetectorFilePlotView.class);
	private static final String TRACE_NAME = "DetectorPlotTrace";

	private IPlottingSystem<Composite> plottingSystem = null;

	private PlotType currentPlotType = PlotType.XY;
	private boolean first;
	private Composite plotComposite;
	private boolean disposed;

	@Override
	public void createPartControl(Composite parent) {
		plotComposite = new Composite(parent, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());
		plottingSystem = createPlotSystem(plotComposite);
		plotComposite.addDisposeListener(e -> disposed = true);
	}

	private IPlottingSystem<Composite> createPlotSystem(Composite plotComposite) {
		try {
			IPlottingSystem<Composite> system = PlottingFactory.createPlottingSystem();
			system.createPlotPart(plotComposite, "DataDisplayer", getViewSite().getActionBars(),
					PlotType.XY, this);
			system.setTitle("View collected data from detector data files");
			system.getSelectedYAxis().setFormatPattern("######.#");
			system.getSelectedXAxis().setFormatPattern("######.#");
			return system;
		} catch (Exception e) {
			logger.error("Failed to create a plotting system object", e);
			return null;
		}
	}

	public void updatePlot(final IDataset xData, final IDataset yData, final String title,
			final String xAxisName, final String yAxisName, boolean newPlot, PlotType plotType) {
		if (plottingSystem == null || plottingSystem.isDisposed()) {
			plottingSystem = createPlotSystem(plotComposite);
		}
		if (currentPlotType != plotType) {
//			plottingSystem.clear();
			currentPlotType=plotType;
			plottingSystem.setPlotType(plotType);
		}

		if (yData == null || xData == null) {
			return;
		}

		if (plotType == PlotType.XY || plotType == PlotType.XY_STACKED || plotType == PlotType.XY_STACKED_3D) {
			if (!Display.getDefault().isDisposed()) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						if (newPlot) {
							plottingSystem.reset(); // clear
							first=true;
						}
						final ILineTrace lineTrace = plottingSystem.createLineTrace(yData.getName());
						lineTrace.setData(xData, yData);
						plottingSystem.addTrace(lineTrace);
						plottingSystem.setTitle(title);
						if (first) {
							plottingSystem.setShowLegend(true);
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
						if (newPlot) {
							plottingSystem.reset();// clear
							first=true;
						}
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

	public void updateImagePlot(ILazyDataset image, String filename) {
		if (!isDisposed() && !Display.getDefault().isDisposed()) {
			plottingSystem.clear();
			if (image.getShape().length > 2) {
				// If file has multiple frames, only plot first
				try {
					image = image.getSlice(new Slice(0, 1, 1)).squeeze();
				} catch (DatasetException e) {
					logger.error("Could not plot image trace", e);
				}
			}
			ILazyDataset img = image; // image has to be (effectively) final
			Display.getDefault().asyncExec(() -> {
				IImageTrace trace = (IImageTrace) plottingSystem.getTrace(TRACE_NAME);
				boolean needTrace = trace == null;
				if (trace == null) {
					trace = plottingSystem.createImageTrace(TRACE_NAME);
				}
				trace.setData(img, null, false);
				if (needTrace) {
					plottingSystem.addTrace(trace);
				}
				plottingSystem.setTitle(filename);
				plottingSystem.repaint();
			});
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
				plottingSystem.reset();
			}
		});
	}

	@Override
	public void setFocus() {
		//plottingSystem.setFocus();
	}

	public boolean isDisposed() {
		return disposed;
	}
}
