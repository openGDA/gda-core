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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.connection.EpicsController.MonitorType;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public class SlicesPlotComposite extends EpicsArrayPlotComposite {

	private static final Logger logger = LoggerFactory.getLogger(SlicesPlotComposite.class);

	private static final String SLICE_PLOT = "Slice plot";

	final Spinner sliceControl;
	protected DataListener dataListener;

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

		sliceControl = new Spinner(composite, SWT.BORDER
				| SWT.RIGHT);
		sliceControl.setBounds(20, 17, 44, 22);
		sliceControl.setBackground(ColorConstants.white);
		sliceControl.setMinimum(1);
		sliceControl.setMaximum(100);
		sliceControl.setEnabled(false);
		sliceControl.setToolTipText("display this slice data.");
		sliceControl.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedSlice = sliceControl.getSelection();
				updatePlot(new NullProgressMonitor());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				selectedSlice = sliceControl.getSelection();
				updatePlot(new NullProgressMonitor());
			}
		});

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "Slices",
				part instanceof IViewPart ? ((IViewPart) part).getViewSite()
						.getActionBars() : null, PlotType.XY_STACKED, part);

		plottingSystem.setTitle(SLICE_PLOT);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");

	}

	private double[] value;
	private int selectedSlice=1;

	private boolean channelCreatedForSlices=false;

	/**
	 * need to override base class method as it uses a different data listener implementation.
	 */
	@Override
	public void initialise() {
		if (getAnalyser() == null || getUpdatePV() == null) {
			throw new IllegalStateException("required parameters for 'analyser' and/or 'arrayPV' are missing.");
		}
		dataListener = new DataListener();
		createChannels();
	}

	private void createChannels() {
		if (!channelCreatedForSlices) {
			first = true;
			try {
				updateChannel = controller.createChannel(updatePV,dataListener, MonitorType.NATIVE, false);
				String[] split = getUpdatePV().split(":");
				startChannel = controller.createChannel(split[0] + ":" + split[1] + ":" + ADBase.Acquire, this, MonitorType.NATIVE, false);
				controller.creationPhaseCompleted();
				controller.tryInitialize(100);
				logger.debug("Data channel {} is created", updateChannel.getName());
				channelCreatedForSlices = true;
			} catch (CAException e) {
				logger.error("Failed to create channel {}", updateChannel.getName());
			}
		}
	}
	@Override
	public boolean setFocus() {
		if (!channelCreatedForSlices) {
			createChannels();
		}
		return super.setFocus();
	}

	protected class DataListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first = false;
				logger.debug("Monitor listener is added to channel {}.", ((Channel)arg0.getSource()).getName());
				return;
			}

			if (!getDisplay().isDisposed()) {
				getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
//						boolean visible = SlicesPlotComposite.this.isVisible();
//						if (visible) {
							DBR dbr = arg0.getDBR();

							if (dbr.isDOUBLE()) {
								value = ((DBR_Double) dbr).getDoubleValue();
								dataslices.clear();
							}
						updatePlot(new NullProgressMonitor());
						}
//					}
				});
			}
		}
	}
	ArrayList<Dataset> dataslices=new ArrayList<Dataset>();
	@Override
	protected void updatePlot(IProgressMonitor monitor) {
		super.updatePlot(monitor);
		try {


			int	slices = getAnalyser().getSlices();
			sliceControl.setMaximum(slices);
			sliceControl.setEnabled(true);
			int length = xdata.clone().length;
			int[] dims = new int[] { slices, length };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			// Get the image data from the analyser
			value = analyser.getController().getImage(arraysize);
			double[] values = Arrays.copyOf(value, arraysize);
			final Dataset ds = new DoubleDataset(values, dims);

			ArrayList<Dataset> plotDataSets = new ArrayList<Dataset>();
			if (selectedSlice>sliceControl.getMaximum()) selectedSlice=1;
			for (int i=0; i<slices; i++) {
				Dataset slice = ds.getSlice(new int[] {i, 0 },new int[] { i+1, dims[1]-1 }, new int[] {1,1});
				Dataset dataset1=slice.flatten();
				dataslices.add(dataset1);
			}
			dataset = dataslices.get(selectedSlice - 1);
			dataset.setName("Intensity (counts)");
			plotDataSets.add(dataset);
			final List<ITrace> profileLineTraces = plottingSystem.createPlot1D(xAxis, plotDataSets, monitor);
			if (!getDisplay().isDisposed()) {
				getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {

						if (isNewRegion()&&!profileLineTraces.isEmpty()) {
							plottingSystem.setShowLegend(false);
							plottingSystem.setTitle("");
							profileLineTrace = (ILineTrace) profileLineTraces.get(0);
							profileLineTrace.setTraceColor(ColorConstants.blue);
							setNewRegion(false);
						}
					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live slices plot",	e);
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
