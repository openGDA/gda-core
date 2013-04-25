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

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

public class SlicesPlotComposite extends Composite {

	private static final Logger logger = LoggerFactory
			.getLogger(SlicesPlotComposite.class);

	private IVGScientaAnalyser analyser;
	private String arrayPV;
	private EpicsController controller = EpicsController.getInstance();

	private static final String SLICE_PLOT = "Slice plot";

	private AbstractPlottingSystem plottingSystem;

	private ILineTrace profileLineTrace;

	private SlicesDataListener dataListener;

	private Channel dataChannel;

	private Monitor dataMonitor;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public SlicesPlotComposite(IWorkbenchPart part, Composite parent, int style)
			throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		Composite controlComposite = new Composite(this, SWT.None);
		controlComposite.setLayout(new FormLayout());
		controlComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite composite = new Composite(controlComposite, SWT.NONE
				| SWT.RIGHT);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0);
		fd_composite.right = new FormAttachment(100, -10);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new GridLayout(2, false));
		composite.setBackground(ColorConstants.yellow);

		Label lblSlice = new Label(composite, SWT.None | SWT.RIGHT);
		lblSlice.setBounds(10, 20, 27, 15);
		lblSlice.setText("Slice:");
		lblSlice.setBackground(ColorConstants.yellow);

		final Spinner sliceControl = new Spinner(composite, SWT.BORDER
				| SWT.RIGHT);
		sliceControl.setBounds(20, 17, 44, 22);
		sliceControl.setBackground(ColorConstants.white);
		sliceControl.setMinimum(1);
		sliceControl.setMaximum(100);
		sliceControl.setToolTipText("display this slice data.");
		sliceControl.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedSlice = sliceControl.getSelection();
				updateSlicesPlot(new NullProgressMonitor(), value,
						selectedSlice);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				selectedSlice = sliceControl.getSelection();
				updateSlicesPlot(new NullProgressMonitor(), value,
						selectedSlice);
			}
		});

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "Slices",
				part instanceof IViewPart ? ((IViewPart) part).getViewSite()
						.getActionBars() : null, PlotType.XY_STACKED, part);
	}
	/**
	 * initialise object
	 * 1. add monitor to data channel and 
	 * 2. create and add a monitor listener to handle Monitor changed event.
	 * This method must be called after the analyser object and data PV are set.
	 */
	public void initialise() {
		if (getAnalyser() == null || getArrayPV()==null) {
			throw new IllegalStateException("required parameters for 'analyser' and/or 'arrayPV' are missing.");
		}
		dataListener = new SlicesDataListener();
		try {
			addMonitors();
		} catch (Exception e) {
			logger.error("Slice Plot Composite cannot add monitor to its data channel.",e);
		}
		addMonitorListeners();
	}

	public void addMonitors() throws Exception {
		dataChannel = controller.createChannel(arrayPV);
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
			logger.error("Failed to remove monitors on Spectrum Plot dispose.",
					e);
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

	private double[] value;
	private int selectedSlice;

	private class SlicesDataListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving image data from " + arg0.toString()
					+ " to plot on " + plottingSystem.getPlotName()
					+ " with axes from " + getAnalyser().getName());
			if (SlicesPlotComposite.this.isVisible()) {
				DBR dbr = arg0.getDBR();
				if (dbr.isDOUBLE()) {
					value = ((DBR_Double) dbr).getDoubleValue();
				}
				IProgressMonitor monitor = new NullProgressMonitor();
				try {
					updateSlicesPlot(monitor, value, selectedSlice);
				} catch (Exception e) {
					logger.error(
							"exception caught preparing analyser live plot", e);
				}
			}
		}
	}

	private void updateSlicesPlot(final IProgressMonitor monitor,
			final double[] value, final int slice) {
		try {
			int[] dims = new int[] { getAnalyser().getNdarrayYsize(),
					getAnalyser().getNdarrayXsize() };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			double[] values = Arrays.copyOf(value, arraysize);
			final AbstractDataset ds = new DoubleDataset(values, dims);

			double[] xdata = getAnalyser().getEnergyAxis(); // TODO do this once
															// per analyser
															// region
			final DoubleDataset xAxis = new DoubleDataset(xdata,
					new int[] { xdata.length });
			xAxis.setName("energies (eV)");
			final ArrayList<AbstractDataset> yaxes = new ArrayList<AbstractDataset>();

			for (int i = 0; i < dims[0]; i++) {
				AbstractDataset slice2 = ds.getSlice(new int[] { 0, i },
						new int[] { dims[1], i }, new int[] { 1, 1 });
				slice2.setName("Intensity (counts");
				yaxes.add(slice2);
			}

			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						final List<ITrace> profileLineTraces = plottingSystem
								.updatePlot1D(xAxis, yaxes, monitor);

						if (!profileLineTraces.isEmpty()
								&& profileLineTraces.size() > slice) {
							// Highlight selected slice in blue color
							profileLineTrace = (ILineTrace) profileLineTraces
									.get(slice);
							profileLineTrace.setTraceColor(ColorConstants.blue);
						}

						plottingSystem.setTitle(SLICE_PLOT);
						plottingSystem.getSelectedYAxis().setFormatPattern(
								"######.#");
						plottingSystem.getSelectedXAxis().setFormatPattern(
								"#####.#");
					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live image plot",
					e);
		}
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}
}
