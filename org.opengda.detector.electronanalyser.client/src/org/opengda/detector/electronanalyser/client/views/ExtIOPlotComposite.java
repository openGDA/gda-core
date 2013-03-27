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

package org.opengda.detector.electronanalyser.client.plot;

import gda.epics.connection.EpicsController;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.server.VGScientaController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 *	monitor and display external IO data update in the plot.
 */
public class ExtIOPlotComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(ExtIOPlotComposite.class);

	private VGScientaAnalyser analyser;
	private EpicsController controller=EpicsController.getInstance();

	private static final String EXTIO_PLOT = "External IO plot";

	private AbstractPlottingSystem plottingSystem;

	private ILineTrace profileLineTrace;
	private ExtIODataListener dataListener;
	private Channel dataChannel;
	private Monitor dataMonitor;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ExtIOPlotComposite(IWorkbenchPart part, Composite parent, int style)
			throws Exception {
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
		plotComposite.setLayout(new GridLayout(1, true));

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "ExtIO", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars()
				: null, PlotType.XY_STACKED, part);
		initialise();
	}

	private void initialise() {
		if (getAnalyser() == null) {
			// Analyser must be called 'analyser' in Spring configuration
			setAnalyser((VGScientaAnalyser) (Finder.getInstance().find("analyser")));
		}
		dataListener = new ExtIODataListener();
		try {
			addMonitors();
		} catch (Exception e) {
			logger.error("Exception on adding a monitor to ExtIO channel. ", e);
		}
		addMonitorListeners();
	}

	public void addMonitors() throws Exception {
		dataChannel = getAnalyser().getController().getChannel(VGScientaController.EXTIODATA);
		dataMonitor = controller.addMonitor(dataChannel);
	}

	public void removeMonitors() throws CAException {
		if (dataMonitor != null) {
			dataMonitor.clear();
		}
	}

	public void addMonitorListeners() {
		dataMonitor.addMonitorListener(dataListener);
	}

	public void removeMonitorListeners() {
		dataMonitor.removeMonitorListener(dataListener);
	}

	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		removeMonitorListeners();
		try {
			removeMonitors();
		} catch (CAException e) {
			logger.error("Failed to remove monitors on Spectrum Plot dispose.", e);
		}
		dataChannel.dispose();
		super.dispose();
	}

	public void clearPlots() {
		if (!getDisplay().isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					plottingSystem.setTitle("");
					plottingSystem.reset();
				}
			});
		}
	}

	private class ExtIODataListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving external IO data from "
					+ ((Channel) (arg0.getSource())).getName() + " to plot on "
					+ plottingSystem.getPlotName() + " with axes from "
					+ getAnalyser().getName());
			if (ExtIOPlotComposite.this.isVisible()) {
				DBR dbr = arg0.getDBR();
				double[] value = null;
				if (dbr.isDOUBLE()) {
					value = ((DBR_Double) dbr).getDoubleValue();
				}
				IProgressMonitor monitor = new NullProgressMonitor();
				updateExtIOPlot(monitor, value);
			}
		}
	}

	private void updateExtIOPlot(final IProgressMonitor monitor, double[] value) {
		final ArrayList<AbstractDataset> plotDataSets = new ArrayList<AbstractDataset>();
		DoubleDataset extiodata = new DoubleDataset(value, new int[] { value.length });
		extiodata.setName("External IO Data");
		plotDataSets.add(extiodata);
		try {
			double[] xdata = getAnalyser().getEnergyAxis(); // TODO once per analyser
														// region
			final DoubleDataset xAxis = new DoubleDataset(xdata, new int[] { xdata.length });
			xAxis.setName("energies (eV)");
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						final List<ITrace> profileLineTraces = plottingSystem.updatePlot1D(xAxis, plotDataSets, monitor);

						if (!profileLineTraces.isEmpty()) {
							profileLineTrace = (ILineTrace) profileLineTraces.get(0);
							profileLineTrace.setTraceColor(ColorConstants.blue);
						}
						plottingSystem.setTitle(EXTIO_PLOT);
						plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
						plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live external IO data plot", e);
		}
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
}
