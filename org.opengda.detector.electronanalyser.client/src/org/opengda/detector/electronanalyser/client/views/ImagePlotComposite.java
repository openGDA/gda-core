/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor and plotting live image data from the electron analyser.
 */
public class ImagePlotComposite extends EpicsArrayPlotComposite {

	private static final Logger logger = LoggerFactory.getLogger(ImagePlotComposite.class);

	private static final String IMAGE_PLOT = "Image plot";
	double[] ydata = null;
	private Dataset yAxis;

	private final ArrayList<IDataset> axes = new ArrayList<>();

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ImagePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "Image", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars() : null,
				PlotType.IMAGE, part);
		plottingSystem.setTitle(IMAGE_PLOT);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("######.#");
	}

	@Override
	protected void updatePlot(IProgressMonitor monitor) {

		super.updatePlot(monitor);
		ydata = getYData();
		yAxis = createYAxis();

		try {
			int length = xdata.clone().length;
			int slices = getAnalyser().getSlices();
			int[] dims = new int[] { slices, length };
			// Get the image data from the analyser
			double[] value = analyser.getImage(dims[0] * dims[1]);
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			dataset = new DoubleDataset(value, dims);
			dataset.setName("");
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live image plot", e);
		}
		updatePlot();
	}

	@Override
	public void updatePlot() {
		if (xdata==null) return;
		super.updatePlot();
		axes.clear();
		axes.add(xAxis);
		axes.add(yAxis);
		plottingSystem.updatePlot2D(dataset, axes, new NullProgressMonitor());
		plottingSystem.repaint(false);
	}

	private Dataset createYAxis() {
		DoubleDataset yAxis = new DoubleDataset(ydata, new int[] { ydata.length });
		try {
			if ("Transmission".equalsIgnoreCase(getAnalyser().getLensMode())) {
				yAxis.setName("pixel");
			} else {
				yAxis.setName("angles (deg)");
			}
		} catch (Exception e1) {
			logger.error("cannot get lens mode from the analyser", e1);
		}
		return yAxis;
	}

	private double[] getYData() {
		double[] ydata=null;
		try {
			ydata = getAnalyser().getAngleAxis();
		} catch (Exception e) {
			logger.error("cannot get angle axis from the analyser", e);
		}
		return ydata;
	}
}
