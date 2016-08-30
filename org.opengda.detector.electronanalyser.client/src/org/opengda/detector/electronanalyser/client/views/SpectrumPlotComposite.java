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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SpectrumPlotComposite extends EpicsArrayPlotComposite {

	private FontRegistry fontRegistry;
	static final Logger logger = LoggerFactory.getLogger(SpectrumPlotComposite.class);
	private Label lblProfileIntensityValue;

	private static final String SPECTRUM_PLOT = "Spectrum plot";

	private static final String BOLD_TEXT_11 = "bold-text_11";

	private static final String BOLD_TEXT_9 = "bold-text_9";

	private Composite statsComposite;

	private Text txtPosition;
	private Text txtHeight;
	private Text txtFWHM;
	private Text txtArea;
	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public SpectrumPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
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
		plotComposite.setLayout(new FillLayout());
		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "Spectrum", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars() : null,
				PlotType.XY_STACKED, part);
		plottingSystem.setTitle(SPECTRUM_PLOT);
		plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
		plottingSystem.getSelectedXAxis().setAxisAutoscaleTight(true);
		plottingSystem.getSelectedYAxis().setTitle("Counts (a.u.)");
		plottingSystem.setShowLegend(false);

		statsComposite = new Composite(this, SWT.None);
		statsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statsComposite.setBackground(ColorConstants.lightBlue);
		GridLayout layout3 = new GridLayout(8, true);
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;
		layout3.horizontalSpacing = 0;
		layout3.verticalSpacing = 0;
		statsComposite.setLayout(layout3);

		Label lblPosition = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblPosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPosition.setBackground(ColorConstants.lightBlue);
		lblPosition.setText("Postion:");

		txtPosition = new Text(statsComposite, SWT.None | SWT.LEFT | SWT.READ_ONLY);
		txtPosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtPosition.setBackground(ColorConstants.lightBlue);
		txtPosition.setText("0.00000");

		Label lblHeight = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblHeight.setBackground(ColorConstants.lightBlue);
		lblHeight.setText("Height:");

		txtHeight = new Text(statsComposite, SWT.None | SWT.LEFT | SWT.READ_ONLY);
		txtHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtHeight.setBackground(ColorConstants.lightBlue);
		txtHeight.setText("0000");

		Label lblFWHM = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblFWHM.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblFWHM.setBackground(ColorConstants.lightBlue);
		lblFWHM.setText("FWHM:");

		txtFWHM = new Text(statsComposite, SWT.None | SWT.LEFT | SWT.READ_ONLY);
		txtFWHM.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFWHM.setBackground(ColorConstants.lightBlue);
		txtFWHM.setText("N/A");

		Label lblArea = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblArea.setBackground(ColorConstants.lightBlue);
		lblArea.setText("Area:");

		txtArea = new Text(statsComposite, SWT.None | SWT.LEFT | SWT.READ_ONLY);
		txtArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtArea.setBackground(ColorConstants.lightBlue);
		txtArea.setText("0000");
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
						txtFWHM.setText(String.format("%.3f", fwhmValue));
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
					txtArea.setText(String.format("%.3f", areaValue));
				}
			});
		}
	}

	final ArrayList<IDataset> plotDataSets = new ArrayList<>();

	@Override
	protected synchronized void updatePlot(final IProgressMonitor monitor) {
		// Call super to setup x axis
		super.updatePlot();
		try {
			// Get the spectrum from analyser
			double[] data = analyser.getSpectrum(xdata.length);
			// Make dataset
			dataset = new DoubleDataset(data, new int[] { data.length });
			dataset.setName("Intensity (counts)");

		} catch (Exception e) {
			logger.error("Error getting spectrum from analyser", e);
		}
		updatePlot();
	}

	@Override
	public synchronized void updatePlot() {
		super.updatePlot();
		plotDataSets.clear();
		plotDataSets.add(dataset);

		plottingSystem.reset();
		plottingSystem.getSelectedYAxis().setTitle("Counts (a.u.)");
		plottingSystem.createPlot1D(xAxis, plotDataSets, new NullProgressMonitor());

		if (plottingSystem.isRescale() && isDisplayInBindingEnergy()) {
			reverseXAxis();
		}
		plottingSystem.repaint(false);
	}

	public synchronized void updateStat() {
		if (dataset==null) return;
		if (xdata!=null) setPositionValue(xdata[dataset.argMax()]);
		setHeightValue(dataset.max().intValue());
		setFWHMValue(fwhm(dataset));
		setAreaValue(Double.valueOf(dataset.sum().toString()));
	}

	private double fwhm(Dataset dataset) {
		List<Double> crossings = DatasetUtils.crossings(dataset, (dataset.max().doubleValue() + dataset.min().doubleValue() / 2));
		double fwhm = Double.NaN;
		if (crossings.size() == 2) {
			// single peak
			fwhm = crossings.get(1) - crossings.get(0);
		} else {
			// TODO multiple peaks
			fwhm = Double.NaN;
		}
		return fwhm;
	}
}
