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

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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

import com.cosylab.epics.caj.CAJChannel;

public class SlicesPlotComposite extends Composite implements InitializationListener, MonitorListener, IPropertyChangeListener {

	private static final Logger logger = LoggerFactory
			.getLogger(SlicesPlotComposite.class);

	private IVGScientaAnalyser analyser;
	private String arrayPV;
	private EpicsChannelManager controller;

	private static final String SLICE_PLOT = "Slice plot";

	private AbstractPlottingSystem plottingSystem;

	private ILineTrace profileLineTrace;

	private SlicesDataListener dataListener;

	private Channel dataChannel;

	private boolean first;
	final Spinner sliceControl;

	private Channel startChannel;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public SlicesPlotComposite(IWorkbenchPart part, Composite parent, int style)
			throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		controller=new EpicsChannelManager(this);
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

		plottingSystem.setTitle(SLICE_PLOT);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");

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
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create required data channel", e1);
		}
	}
	public void createChannels() throws CAException, TimeoutException {
		first=true;
		dataChannel = controller.createChannel(arrayPV, dataListener, MonitorType.NATIVE,false);
		String[] split = getArrayPV().split(":");
		startChannel = controller.createChannel(split[0]+":"+split[1]+":"+ADBase.Acquire, this, MonitorType.NATIVE, false);
		controller.creationPhaseCompleted();
		logger.debug("Slices channel is created");
	}

	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		dataChannel.dispose();
		startChannel.dispose();
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

	private boolean newRegion=true;

	private class SlicesDataListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first=false;
				logger.debug("Slices Data Listener connected.");
				return;
			}
//			logger.debug("receiving slices data from " + arg0.toString()
//					+ " to plot on " + plottingSystem.getPlotName()
//					+ " with axes from " + getAnalyser().getName());
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
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
				});
			}
		}
	}

	double[] xdata;
	private void updateSlicesPlot(final IProgressMonitor monitor,final double[] value, final int slice) {
		if (isNewRegion()) {
			try {
				xdata = getAnalyser().getEnergyAxis();
			} catch (Exception e) {
				logger.error("cannot get enegery axis from the analyser", e);
			}
			try {
				sliceControl.setMaximum(getAnalyser().getNdarrayYsize());
			} catch (Exception e) {
				logger.error("cannot get Y diamonsion from the analyser", e);
			}
		}
		DoubleDataset xAxis = new DoubleDataset(xdata,new int[] { xdata.length });
		xAxis.setName("energies (eV)");
		
		try {
			int[] dims = new int[] { getAnalyser().getNdarrayYsize(),getAnalyser().getNdarrayXsize() };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			double[] values = Arrays.copyOf(value, arraysize);
			final AbstractDataset ds = new DoubleDataset(values, dims);

			final ArrayList<AbstractDataset> yaxes = new ArrayList<AbstractDataset>();

			for (int i = 0; i < dims[0]; i++) {
				AbstractDataset slice2 = ds.getSlice(new int[] { 0, i },new int[] { dims[1], i+1 }, null);
				slice2.setName("Intensity (counts");
				yaxes.add(slice2);
			}
			plottingSystem.clear();
			final List<ITrace> profileLineTraces = plottingSystem.createPlot1D(xAxis, yaxes, monitor);
			if (!getDisplay().isDisposed()) {
				getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {

						if (isNewRegion()&&!profileLineTraces.isEmpty()) {
							plottingSystem.setTitle("");
							setNewRegion(false);
						}
						// Highlight selected slice in blue color
						profileLineTrace = (ILineTrace) profileLineTraces.get(slice);
						profileLineTrace.setTraceColor(ColorConstants.blue);
						//plottingSystem.autoscaleAxes();
					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live slices plot",	e);
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
	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.info("Slices EPICS Channel initialisation completed!");
		
	}
	public void setNewRegion(boolean b) {
		this.newRegion=b;
		
	}
	public boolean isNewRegion() {
		return newRegion;
	}
	@Override
	public void monitorChanged(MonitorEvent arg0) {
		if (((CAJChannel) arg0.getSource()).getName().endsWith(ADBase.Acquire)) {
//			logger.debug("been informed of some sort of change to acquire status");
//			DBR_Enum en = (DBR_Enum) arg0.getDBR();
//			short[] no = (short[]) en.getValue();
//			if (no[0] == 0) {
//				logger.info("been informed of a stop");
//			} else {
//				logger.info("been informed of a start");
//			}
			setNewRegion(true);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		try {
			double excitationEnergy = getAnalyser().getExcitationEnergy();
			xdata = getAnalyser().getEnergyAxis();
			for (int i = 0; i < xdata.length; i++) {
				xdata[i] = excitationEnergy - xdata[i];
			}
		} catch (Exception e) {
			logger.error("cannot get enegery axis fron the analyser", e);
		}
	}

}
