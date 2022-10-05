/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;

public class SpecsLiveAlignmentPlot extends SpecsLivePlot implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveAlignmentPlot.class);

	private static final String PLOT_TITLE = "Alignment Spectrum";
	private static final String XAXIS_TITLE = "Values";
	private static final String YAXIS_TITLE = "Intensity (counts)";

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			plottingSystem.createPlotPart(parent, "Spectrum", actionBars, PlotType.XY, this);
			plottingSystem.setShowLegend(false);
			plottingSystem.getSelectedXAxis().setInverted(false);
			plottingSystem.getSelectedYAxis().setAutoscale(false);
			setPlotTitles();
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
			throw e;
		}
	}


	@Override
	public void updatePlot(SpecsPhoibosLiveUpdate update) {

		if (!(update instanceof SpecsPhoibosLiveDataUpdate)) {

			// Get data
			double[] spectrum = null ;
			try {
				spectrum = epicsController.cagetDoubleArray(spectrumChannel, 0);
			} catch (TimeoutException | CAException | InterruptedException e) {
				logger.error("Could not get spectrum form channel", e);
			}

			IDataset dataset = DatasetFactory.createFromObject(spectrum);
			dataset.setName("Alignment Spectrum");
			plottingSystem.updatePlot1D(null, Arrays.asList(dataset), null);

			// Set plotting system
			double maxY = dataset.max(false).doubleValue();

			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				plottingSystem.getSelectedXAxis().setAutoscale(true);
				plottingSystem.getSelectedYAxis().setRange(0, maxY * 1.05);
				setPlotTitles();
			});
			logger.trace("Updated plotting system");
		}
	}

	private void setPlotTitles() {
		plottingSystem.setTitle(PLOT_TITLE);
		plottingSystem.getSelectedYAxis().setTitle(YAXIS_TITLE);
		plottingSystem.getSelectedXAxis().setTitle(XAXIS_TITLE);
	}

}
