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

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawnsci.plotting.api.PlotType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Monitor and plotting live image data from the electron analyser.
 */
public class ImagePlotComposite extends Composite implements InitializationListener, MonitorListener {

	private static final Logger logger = LoggerFactory.getLogger(ImagePlotComposite.class);

	private static final String IMAGE_PLOT = "Image plot";

	private IVGScientaAnalyser analyser;
	private String arrayPV;
	private EpicsChannelManager controller;

	private AbstractPlottingSystem plottingSystem;

	private ImageDataListener dataListener;
	private Channel dataChannel;

	private boolean first = false;

	private boolean newRegion = true;

	private Channel startChannel;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ImagePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		controller = new EpicsChannelManager(this);
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

	public void initialise() {
		if (getAnalyser() == null || getArrayPV() == null) {
			throw new IllegalStateException("required parameters for 'analyser' and/or 'arrayPV' are missing.");
		}
		dataListener = new ImageDataListener();
		try {
			createChannels();
		} catch (CAException | TimeoutException e1) {
			logger.error("failed to create required spectrum channels", e1);
		}
	}

	public void createChannels() throws CAException, TimeoutException {
		first = true;
		dataChannel = controller.createChannel(arrayPV, dataListener, MonitorType.NATIVE, false);
		String[] split = getArrayPV().split(":");
		startChannel = controller.createChannel(split[0]+":"+split[1]+":"+ADBase.Acquire, this, MonitorType.NATIVE, false);
		controller.creationPhaseCompleted();
		logger.debug("Image channel is created");
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
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.setTitle("");
				plottingSystem.reset();
			}
		});
	}

	private class ImageDataListener implements MonitorListener {

		@Override
		public void monitorChanged(final MonitorEvent arg0) {
			if (first) {
				first = false;
				logger.debug("Image lietener connected.");
				return;
			}
//			logger.debug("receiving image data from " + arg0.toString() + " to plot on " + plottingSystem.getPlotName() + " with axes from "
//					+ getAnalyser().getName());
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						if (ImagePlotComposite.this.isVisible()) {
							DBR dbr = arg0.getDBR();
							double[] value = null;
							if (dbr.isDOUBLE()) {
								value = ((DBR_Double) dbr).getDoubleValue();
							}
							IProgressMonitor monitor = new NullProgressMonitor();
							try {
								updateImagePlot(monitor, value);
							} catch (Exception e) {
								logger.error("exception caught preparing analyser live plot", e);
							}
						}
					}
				});
			}
		}
	}

	double[] xdata = null;
	double[] ydata = null;

	private void updateImagePlot(final IProgressMonitor monitor, final double[] value) {
		if (isNewRegion()) {
			// analyser region
			try {
				xdata = getAnalyser().getEnergyAxis();
			} catch (Exception e) {
				logger.error("cannot get enegery axis from the analyser", e);
			}
			try {
				ydata = getAnalyser().getAngleAxis();
			} catch (Exception e) {
				logger.error("cannot get angle axis from the analyser", e);
			}
			setNewRegion(false);
		}
		DoubleDataset xAxis = new DoubleDataset(xdata, new int[] { xdata.length });
		xAxis.setName("energies (eV)");
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

		ArrayList<AbstractDataset> axes = new ArrayList<AbstractDataset>();
		axes.add(xAxis);
		axes.add(yAxis);
		try {
			int[] dims = new int[] { getAnalyser().getSlices(), xdata.clone().length };
			int arraysize = dims[0] * dims[1];
//			logger.warn("arraysize = {}", arraysize);
			if (arraysize < 1) {
				return;
			}
			double[] values = Arrays.copyOf(value, arraysize);
//			logger.warn("image size = {}", values.length);
			final AbstractDataset ds = new DoubleDataset(values, dims).getSlice(null, null, new int[] { -1, 1 });
			ds.setName("");
			plottingSystem.clear();
			plottingSystem.createPlot2D(ds, axes, monitor);
//			if (!getDisplay().isDisposed()) {
//				getDisplay().asyncExec(new Runnable() {
//
//					@Override
//					public void run() {
//						plottingSystem.autoscaleAxes();
//					}
//				});
//			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live image plot", e);
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
		logger.info("Image EPICS Channel initialisation completed!");

	}

	public void setNewRegion(boolean b) {
		this.newRegion = b;

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

}
