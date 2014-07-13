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

package org.opengda.lde.ui.views;

import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.Arrays;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Monitor and plotting live image data from the electron analyser.
 */
public class ImagePlotComposite extends Composite implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(ImagePlotComposite.class);

	private static final String IMAGE_PLOT = "Image plot";

	private String arrayPV;
	private EpicsChannelManager controller;
	private int xPixelSize;
	private int yPixelSize;
	private IPlottingSystem plottingSystem;

	private ImageDataListener dataListener;
	private Channel dataChannel;

	private boolean first = false;

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
		if (getArrayPV() == null) {
			throw new IllegalStateException("required parameters for 'arrayPV' are missing.");
		}
		if (getXPixelSize() == 0 || getYPixelSize() == 0) {
			throw new IllegalStateException("Area detector diamension cannot be 0.");
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
		controller.creationPhaseCompleted();
		logger.debug("Image channel is created");
	}

	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		dataChannel.dispose();
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

	private void updateImagePlot(final IProgressMonitor monitor, final double[] value) {

		try {
			int[] dims = new int[] { getYPixelSize(), getXPixelSize() };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			double[] values = Arrays.copyOf(value, arraysize);
//			logger.warn("image size = {}", values.length);
			final AbstractDataset ds = new DoubleDataset(values, dims);
			ds.setName("Intensity");
			plottingSystem.clear();
			plottingSystem.createPlot2D(ds, null, monitor);
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

	public int getYPixelSize() {
		return yPixelSize;
	}

	public void setYPixelSize(int yPixelSize) {
		this.yPixelSize = yPixelSize;
	}

	public int getXPixelSize() {
		return xPixelSize;
	}

	public void setXPixelSize(int xPixelSize) {
		this.xPixelSize = xPixelSize;
	}
}
