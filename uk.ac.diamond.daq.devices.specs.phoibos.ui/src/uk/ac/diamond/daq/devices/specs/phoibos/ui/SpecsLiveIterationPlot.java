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

import java.util.HashMap;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveIterationSpectraUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;

public class SpecsLiveIterationPlot extends SpecsLivePlot implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveIterationPlot.class);
	HashMap <Integer, IDataset> dataStorage = new HashMap <>();

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			actionBars.getToolBarManager().add(new KeBeSwich());
			actionBars.getToolBarManager().add(new StopAfterCurrentIteration());
			plottingSystem.createPlotPart(parent, "Spectrum", actionBars, PlotType.XY, this);
			plottingSystem.setTitle("Iteration Spectrum");
			plottingSystem.setShowLegend(true);
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
			throw e;
		}
	}


	@Override
	void updatePlot(SpecsPhoibosLiveUpdate update) {
		if (update instanceof SpecsPhoibosLiveIterationSpectraUpdate updatePlot) {
			// Cache the update in case we want to switch KE and BE
			if (updatePlot.getCurrentPoint() == 1) {
				plottingSystem.clear();
				dataStorage.clear();
			}
			int iterationNumber = updatePlot.getIterationNumber();
			lastUpdate = updatePlot;

			// Energy axis
			final double[] energyAxis;
			if (displayInBindingEnergy) {
				energyAxis = updatePlot.getBeEnergyAxis();
			}
			else {
				energyAxis = updatePlot.getKeEnergyAxis();
			}
			final IDataset energyAxisDataset = DatasetFactory.createFromObject(energyAxis);


			// Data
			IDataset spectrum = DatasetFactory.createFromObject(updatePlot.getIterationSpectrum());
			spectrum.setName("Iteration " + iterationNumber);
			dataStorage.put(iterationNumber,spectrum);

			// Update plotting system in UI thread
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				plottingSystem.updatePlot1D(energyAxisDataset, dataStorage.values().stream().toList(), null);
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


	class StopAfterCurrentIteration extends Action {

		public StopAfterCurrentIteration() {
			super("Stop After Current Iteration",
				AbstractUIPlugin.imageDescriptorFromPlugin("uk.ac.diamond.daq.devices.specs.phoibos.ui", "icons/stop_sign.png"));
		}

		@Override
		public void run() {
			logger.trace("Stop after current iteration pressed");
			analyser.stopAfterCurrentIteration();
		}
	}
}
