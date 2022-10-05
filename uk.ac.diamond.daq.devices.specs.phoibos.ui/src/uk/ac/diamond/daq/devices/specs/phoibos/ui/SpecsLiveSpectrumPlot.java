/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveIterationSpectraUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;

public class SpecsLiveSpectrumPlot extends SpecsLivePlot implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveSpectrumPlot.class);
	private double[] summedSpectrum = null;
	private int currentPointIteration;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			actionBars.getToolBarManager().add(new KeBeSwich());
			plottingSystem.createPlotPart(parent, "Spectrum", actionBars, PlotType.XY, this);
			plottingSystem.setTitle("Analyser Spectrum");
			plottingSystem.setShowLegend(false);
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
			throw e;
		}
	}


	@Override
	void updatePlot(SpecsPhoibosLiveUpdate update) {

		if (update instanceof SpecsPhoibosLiveDataUpdate) {
			SpecsPhoibosLiveDataUpdate dataUpdate = (SpecsPhoibosLiveDataUpdate) update;

			// Cache the update in case we want to switch KE and BE
			lastUpdate = dataUpdate;

			// Energy axis
			final double[] energyAxis;
			if (displayInBindingEnergy) {
				energyAxis = dataUpdate.getBeEnergyAxis();
			}
			else {
				energyAxis = dataUpdate.getKeEnergyAxis();
			}
			final IDataset energyAxisDataset = DatasetFactory.createFromObject(energyAxis);

			double[] latestSpectrum = null;
			try {
				latestSpectrum = epicsController.cagetDoubleArray(spectrumChannel, 0);
			} catch (TimeoutException | CAException | InterruptedException e1) {
				logger.error("Could not get spectrum from channel", e1);
			}

			if (dataUpdate instanceof SpecsPhoibosLiveIterationSpectraUpdate) {
				int latestCurrentPointIteration = ((SpecsPhoibosLiveIterationSpectraUpdate)dataUpdate).getcurrentPointInIteration();
				if(currentPointIteration != latestCurrentPointIteration) {
					currentPointIteration = latestCurrentPointIteration;
					int currentIteration = ((SpecsPhoibosLiveIterationSpectraUpdate)dataUpdate).getIterationNumber();
					if(currentIteration > 1) {
						summedSpectrum[currentPointIteration-1] += latestSpectrum[currentPointIteration-1];
					} else {
						summedSpectrum = latestSpectrum;
					}
				}
			}

			// Data
			IDataset data;
			if(dataUpdate instanceof SpecsPhoibosLiveIterationSpectraUpdate) {
				data = DatasetFactory.createFromObject(summedSpectrum);
			}else {
				data = DatasetFactory.createFromObject(latestSpectrum);
			}
			data.setName("spectrum");

			// Something in the plotting system here isn't thread safe so do in UI thread
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				// Thread safe so don't need to be in the UI thread
				plottingSystem.updatePlot1D(energyAxisDataset, Arrays.asList(data), null);
				plottingSystem.getSelectedYAxis().setTitle("Intensity (arb. units)");

				if (displayInBindingEnergy) {
					plottingSystem.getSelectedXAxis().setTitle("Binding Energy (eV)");
					plottingSystem.getSelectedXAxis().setInverted(true);
				} else {
					plottingSystem.getSelectedXAxis().setTitle("Kinetic Energy (eV)");
					plottingSystem.getSelectedXAxis().setInverted(false);
				}

				plottingSystem.repaint();
			});

			logger.trace("Updated plotting system");
		}

	}
}
