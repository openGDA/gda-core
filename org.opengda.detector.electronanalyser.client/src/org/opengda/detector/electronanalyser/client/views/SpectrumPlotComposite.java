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
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.server.VGScientaController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

/**
 *
 */
public class SpectrumPlotComposite extends Composite {

	private FontRegistry fontRegistry;
	private static final Logger logger = LoggerFactory.getLogger(SpectrumPlotComposite.class);

	private VGScientaAnalyser analyser;

	private Label lblProfileIntensityValue;

	private static final String SPECTRUM_PLOT = "Spectrum plot";

	private static final String BOLD_TEXT_11 = "bold-text_11";

	private static final String BOLD_TEXT_9 = "bold-text_9";

	private AbstractPlottingSystem plottingSystem;

	private ILineTrace profileLineTrace;

	private Composite statsComposite;

	private Text txtPosition;
	private Text txtHeight;
	private Text txtFWHM;
	private Text txtArea;
	private SpectrumDataListener spectrumDataListener;
	private Channel spectrumChannel;
	private EpicsController controller = EpicsController.getInstance();
	private Monitor spectrumMonitor;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public SpectrumPlotComposite(IWorkbenchPart part, Composite parent,
			int style) throws Exception {
		super(parent, style);
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
			fontRegistry.put(BOLD_TEXT_9, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
		}
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
		plottingSystem.createPlotPart(plotComposite, "Spectrum", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars()
				: null, PlotType.XY_STACKED, part);

		statsComposite = new Composite(this, SWT.None);
		statsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statsComposite.setBackground(ColorConstants.yellow);
		GridLayout layout3 = new GridLayout(8, true);
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;
		layout3.horizontalSpacing = 0;
		layout3.verticalSpacing = 0;
		statsComposite.setLayout(layout3);

		Label lblPosition = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblPosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPosition.setBackground(ColorConstants.yellow);
		lblPosition.setText("Postion:");

		txtPosition = new Text(statsComposite, SWT.None | SWT.LEFT
				| SWT.READ_ONLY);
		txtPosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtPosition.setBackground(ColorConstants.yellow);
		txtPosition.setText("0.00000");

		Label lblHeight = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblHeight.setBackground(ColorConstants.yellow);
		lblHeight.setText("Height:");

		txtHeight = new Text(statsComposite, SWT.None | SWT.LEFT
				| SWT.READ_ONLY);
		txtHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtHeight.setBackground(ColorConstants.yellow);
		txtHeight.setText("0000");

		Label lblFWHM = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblFWHM.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblFWHM.setBackground(ColorConstants.yellow);
		lblFWHM.setText("FWHM:");

		txtFWHM = new Text(statsComposite, SWT.None | SWT.LEFT | SWT.READ_ONLY);
		txtFWHM.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFWHM.setBackground(ColorConstants.yellow);
		txtFWHM.setText("N/A");

		Label lblArea = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblArea.setBackground(ColorConstants.yellow);
		lblArea.setText("Area:");

		txtArea = new Text(statsComposite, SWT.None | SWT.LEFT | SWT.READ_ONLY);
		txtArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtArea.setBackground(ColorConstants.yellow);
		txtArea.setText("0000");

		initialise();

	}

	private void initialise() {
		if (getAnalyser() == null) {
			// Analyser must be called 'analyser' in Spring configuration
			analyser = (VGScientaAnalyser) (Finder.getInstance().find("analyser"));
		}
		spectrumDataListener = new SpectrumDataListener();
		try {
			addMonitors();
		} catch (Exception e) {
			logger.error("exception caught on adding a monitor to spectrum data channel. ", e);
		}
		addMonitorListeners();
	}

	public void addMonitors() throws Exception {
		spectrumChannel = getAnalyser().getController().getChannel(VGScientaController.SPECTRUMDATA);
		spectrumMonitor = controller.addMonitor(spectrumChannel);
	}

	public void removeMonitors() throws CAException {
		spectrumMonitor.clear();
	}

	public void addMonitorListeners() {
		spectrumMonitor.addMonitorListener(spectrumDataListener);
	}

	public void removeMonitorListeners() {
		spectrumMonitor.removeMonitorListener(spectrumDataListener);
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
		spectrumChannel.dispose();
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

	public void setPositionValue(final double xValue) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					txtPosition.setText(String.format("%.3f", xValue));
				}
			});
		}
	}

	public void setHeightValue(final int heightVal) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					txtHeight.setText(String.format("%d", heightVal));
				}
			});
		}
	}

	public void setFWHMValue(final double fwhmValue) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (fwhmValue == Double.NaN) {
						lblProfileIntensityValue.setText("N/A");
					} else {
						lblProfileIntensityValue.setText(String.format("%.3f", fwhmValue));
					}
				}
			});
		}
	}

	public void setAreaValue(final double areaValue) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					lblProfileIntensityValue.setText(String.format("%.3f", areaValue));
				}
			});
		}
	}

	private class SpectrumDataListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving spectrum data from "
					+ ((Channel) (arg0.getSource())).getName() + " to plot on "
					+ plottingSystem.getPlotName() + " with axes from "
					+ getAnalyser().getName());
			if (SpectrumPlotComposite.this.isVisible()) {
				DBR dbr = arg0.getDBR();
				double[] value = null;
				if (dbr.isDOUBLE()) {
					value = ((DBR_Double) dbr).getDoubleValue();
				}
				IProgressMonitor monitor = new NullProgressMonitor();
				updateSpectrumPlot(monitor, value);
			}
		}
	}

	private void updateSpectrumPlot(final IProgressMonitor monitor,
			double[] value) {
		final ArrayList<AbstractDataset> plotDataSets = new ArrayList<AbstractDataset>();
		DoubleDataset dataset = new DoubleDataset(value, new int[] { value.length });
		dataset.setName("Intensity (counts");
		plotDataSets.add(dataset);
		try {
			double[] xdata = getAnalyser().getEnergyAxis(); // TODO once per
															// analyser region
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

						plottingSystem.setTitle(SPECTRUM_PLOT);
						plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
						plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
						// plottingSystem.getSelectedYAxis().setRange(0, 5);
						// plottingSystem.getSelectedXAxis().setRange(0, 4008);
					}
				});
			}
			setPositionValue(xdata[dataset.argMax()]);
			setHeightValue(dataset.max().intValue());
			setFWHMValue(fwhm(dataset));
			setAreaValue(Double.valueOf(dataset.sum().toString()));
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live spectrum plot", e);
		}
	}

	private double fwhm(DoubleDataset dataset) {
		List<Double> crossings = DatasetUtils.crossings(dataset, (dataset.max().doubleValue()+dataset.min().doubleValue()/2));
		double fwhm=Double.NaN;
		if (crossings.size()==2) {
			// single peak
			fwhm = crossings.get(1)-crossings.get(0);
		} else {
			//TODO multiple peaks
		}
		return fwhm;
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
}
