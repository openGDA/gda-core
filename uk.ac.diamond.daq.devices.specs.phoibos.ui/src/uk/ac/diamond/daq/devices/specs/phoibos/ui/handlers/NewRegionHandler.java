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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class NewRegionHandler {
	protected ISpecsPhoibosAnalyser analyser;
	private static final Logger logger = LoggerFactory.getLogger(HandlerBase.class);

	@Execute
	public void execute(MPart part) {
		Map<String, Object> partData = part.getTransientData();
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) partData.get(SpecsUiConstants.OPEN_SEQUENCE);

		if (sequence == null) {
			return;
		}

		// Create a new region with the default settings
		SpecsPhoibosRegion newRegion = new SpecsPhoibosRegion();
		// Set slices from analyser spring xml via RMI - easily can add more region parameters here!
		try {
			List<ISpecsPhoibosAnalyser> analysers = Finder.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
			if (analysers.size() == 1) {
				analyser = analysers.get(0);
				newRegion.setSlices(analyser.getDefaultRegionUi().getSlices());
				logger.debug("Connected to analyser {} and read default number of slices: {}", analyser, analyser.getDefaultRegionUi().getSlices());
			}
		} catch (Exception e) {
			logger.warn("Failed to get defalt slices over RMI - setting number of slices to the default class parameter value", e);
		}

		// If the sequence already contains a region with the same name add a number
		final String newRegionName = newRegion.getName();
		int i = 2;
		while (sequence.getRegionNames().contains(newRegion.getName())) {
			newRegion.setName(newRegionName + i);
			i++; // Increment i
		}
		sequence.addRegion(newRegion);
	}

	@CanExecute
	public boolean canExecute(MPart part) {
		Map<String, Object> partData = part.getTransientData();
		// Get the sequence
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) partData.get(SpecsUiConstants.OPEN_SEQUENCE);

		// Can't execute if there is no sequence
		if (sequence == null) {
			return false;
		}
		return true;
	}

}