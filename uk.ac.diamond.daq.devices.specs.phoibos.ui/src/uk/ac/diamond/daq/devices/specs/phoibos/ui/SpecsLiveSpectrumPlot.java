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
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;

public class SpecsLiveSpectrumPlot extends SpecsLivePlot implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveSpectrumPlot.class);

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			plottingSystem.createPlotPart(parent, "Spectrum", actionBars, PlotType.XY, this);
			plottingSystem.setTitle("Analyser Spectrum");
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
		}
	}

	@Override
	void updatePlot(SpecsPhoibosLiveDataUpdate update) {
		if (update == null) {
			return; // Can't do anything n this case - should only happen before any updates are received and if KE BE is pressed.
		}

		// Cache the update in case we want to switch KE and BE
		lastUpdate = update;

		// Energy axis
		final double[] energyAxis;
		if (displayInBindingEnergy) {
			energyAxis = update.getBeEnergyAxis();
		}
		else {
			energyAxis = update.getKeEnergyAxis();
		}
		final IDataset energyAxisDataset = DatasetFactory.createFromObject(energyAxis);

		// Data
		IDataset spectrum = DatasetFactory.createFromObject(update.getSpectrum());
		spectrum.setName("spectrum");

		// Something in the plotting system here isn't thread safe so do in UI thread
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
			// Thread safe so don't need to be in the UI thread
			plottingSystem.updatePlot1D(energyAxisDataset, Arrays.asList(spectrum), null);
			plottingSystem.getSelectedYAxis().setTitle("Intesnity (arb. units)");

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
