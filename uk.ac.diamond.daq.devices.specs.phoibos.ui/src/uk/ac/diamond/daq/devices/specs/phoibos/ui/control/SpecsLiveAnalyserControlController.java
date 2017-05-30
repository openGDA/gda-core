/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.control;

import java.util.List;

import gda.factory.Finder;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsFixedRegionWrapper;

public class SpecsLiveAnalyserControlController {

	private final SpecsFixedRegionWrapper model;
	private final ISpecsPhoibosAnalyser analyser;

	public SpecsLiveAnalyserControlController() {
		// Get an analyser
		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		analyser = analysers.get(0);

		// Make a model
		model = new SpecsFixedRegionWrapper(new SpecsPhoibosRegion(), analyser.getDetectorEnergyWidth());

		// Setup the model with the current analyser settings
		model.setPsuMode(analyser.getPsuMode());
		model.setLensMode(analyser.getLensMode());
		// Do PE before centre as its used to calculate start and end energy
		model.setPassEnergy(analyser.getPassEnergy());
		model.setCentreEnergy(analyser.getCenterEnergy());
		model.setExposureTime(analyser.getDwellTime());

	}

	public SpecsFixedRegionWrapper getModel() {
		return model;
	}

	public String[] getPsuModes() {
		return analyser.getPsuModes().toArray(new String[0]);
	}

	public String[] getLensModes() {
		return analyser.getLensModes().toArray(new String[0]);
	}

	public void start() {
		// Setup the currently chosen settings
		analyser.setRegion(model.getRegion());
		// Start continuous acquisition
		analyser.startContinuous();

	}

	public void stop() {
		analyser.stopAcquiring();
	}

}
