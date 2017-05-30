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
import java.util.List;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;

public class SpecsLiveImagePlot extends SpecsLivePlot {
	private static final Logger logger = LoggerFactory.getLogger(SpecsLiveImagePlot.class);

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		try {
			plottingSystem.createPlotPart(parent, "Image", actionBars, PlotType.IMAGE, this);
			plottingSystem.setTitle("Analyser Image");
			plottingSystem.setShowLegend(false);
			plottingSystem.getSelectedYAxis().setInverted(true);
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
		if (displayInBindingEnergy) {
			energyAxisDataset.setName("Binding Energy (eV)");
		} else {
			energyAxisDataset.setName("Kinetic Energy (eV)");
		}

		// Y axis
		final IDataset yAxis = DatasetFactory.createFromObject(analyser.getYAxis());
		String units = analyser.getYUnits();
		yAxis.setName("Y scale (" + units + ")");
		List<IDataset> axis = Arrays.asList(energyAxisDataset, yAxis);

		// Get the image data
		IDataset image = DatasetFactory.createFromObject(analyser.getImage());

		// Thread safe so don't need to be in the UI thread
		plottingSystem.updatePlot2D(image, axis, null);
		plottingSystem.repaint();

		logger.trace("Updated plotting system");
	}

}
