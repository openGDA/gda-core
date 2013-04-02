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
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.ArrayList;
import java.util.Arrays;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.server.VGScientaController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 * Monitor and plotting live image data from the electron analyser.
 */
public class ImagePlotComposite extends Composite {

	private FontRegistry fontRegistry;
	private static final Logger logger = LoggerFactory.getLogger(ImagePlotComposite.class);

	private VGScientaAnalyser analyser;
	private EpicsController controller = EpicsController.getInstance();

	private static final String BOLD_TEXT_11 = "bold-text_11";

	private static final String BOLD_TEXT_9 = "bold-text_9";

	private AbstractPlottingSystem plottingSystem;

	private ImageDataListener dataListener;
	private Channel dataChannel;
	private Monitor dataMonitor;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ImagePlotComposite(IWorkbenchPart part, Composite parent, int style)
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
//		plottingSystem.createPlotPart(plotComposite, "Image", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars()
//				: null, PlotType.XY_STACKED, part);
		plottingSystem.createPlotPart(plotComposite, "Image", null, PlotType.XY_STACKED, part);

		initialise();
	}

	private void initialise() {
		if (getAnalyser() == null) {
			// Analyser must be called 'analyser' in Spring configuration
			setAnalyser((VGScientaAnalyser) (Finder.getInstance().find("analyser")));
		}
		dataListener = new ImageDataListener();
		try {
			addMonitors();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addMonitorListeners();
	}

	public void addMonitors() throws Exception {
		dataChannel = getAnalyser().getController().getChannel(VGScientaController.IMAGEDATA);
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
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving image data from " + arg0.toString()
					+ " to plot on " + plottingSystem.getPlotName()
					+ " with axes from " + getAnalyser().getName());
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
	}

	private void updateImagePlot(final IProgressMonitor monitor,
			final double[] value) {
		try {
			int[] dims = new int[] {
					getAnalyser().getNdArray().getPluginBase().getArraySize1_RBV(),
					getAnalyser().getNdArray().getPluginBase().getArraySize0_RBV() };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1) {
				return;
			}
			double[] values = Arrays.copyOf(value, arraysize);
			final AbstractDataset ds = new DoubleDataset(values, dims);

			double[] xdata = getAnalyser().getEnergyAxis(); // TODO do this once
															// per
			// analyser region
			double[] ydata = getAnalyser().getAngleAxis();
			DoubleDataset xAxis = new DoubleDataset(xdata, new int[] { xdata.length });
			DoubleDataset yAxis = new DoubleDataset(ydata, new int[] { ydata.length });
			xAxis.setName("energies (eV)");
			if ("Transmission".equalsIgnoreCase(getAnalyser().getLensMode())) {
				yAxis.setName("location (mm)");
			} else {
				yAxis.setName("angles (deg)");
			}
			final ArrayList<AbstractDataset> axes = new ArrayList<AbstractDataset>();
			axes.add(xAxis);
			axes.add(yAxis);
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						plottingSystem.updatePlot2D(ds, axes, monitor);
					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live image plot", e);
		}
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
}
