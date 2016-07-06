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
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.client.IEnergyAxis;
import org.opengda.detector.electronanalyser.client.IPlotCompositeInitialiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.InitializationListener;
import gov.aps.jca.event.MonitorListener;

/**
 * monitor and display external IO data update in the plot.
 */
public class ExtIOPlotComposite extends EpicsArrayPlotComposite implements InitializationListener, MonitorListener, IEnergyAxis, IPlotCompositeInitialiser  {

	private static final Logger logger = LoggerFactory.getLogger(ExtIOPlotComposite.class);

	private static final String EXTIO_PLOT = "External IO plot";

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ExtIOPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
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
		plottingSystem.createPlotPart(plotComposite, "ExtIO", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars() : null,
				PlotType.XY_STACKED, part);
		plottingSystem.setTitle(EXTIO_PLOT);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
	}

	@Override
	protected void updatePlot(final IProgressMonitor monitor, double[] value) {
		super.updatePlot(monitor, value);

		ArrayList<Dataset> plotDataSets = new ArrayList<Dataset>();
		double[] data = ArrayUtils.subarray(value, 0, xdata.length);
		dataset = DatasetFactory.createFromObject(data);
		dataset.setName("Intensity (counts)");
		plotDataSets.add(dataset);
		final List<ITrace> profileLineTraces = plottingSystem.createPlot1D(xAxis, plotDataSets, monitor);
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {

					if (isNewRegion() && !profileLineTraces.isEmpty()) {
						plottingSystem.setShowLegend(false);
						// plottingSystem.getSelectedYAxis().setTitle(dataset.getName());
						plottingSystem.setTitle("");
						profileLineTrace = (ILineTrace) profileLineTraces.get(0);
						profileLineTrace.setTraceColor(ColorConstants.blue);
						setNewRegion(false);
					}
				}
			});
		}
	}

	@Override
	public void updatePlot() {
		if (xdata==null) return;
		super.updatePlot();
		ArrayList<Dataset> plotDataSets = new ArrayList<Dataset>();
		plotDataSets.add(dataset);
		plottingSystem.createPlot1D(xAxis, plotDataSets, new NullProgressMonitor());
	}

}
