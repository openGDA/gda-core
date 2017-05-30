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

import java.util.Map;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class RemoveRegionHandler {

	@Execute
	public void execute(MPart part) {
		Map<String, Object> partData = part.getTransientData();
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) partData.get(SpecsUiConstants.OPEN_SEQUENCE);
		// Get the selected region
		SpecsPhoibosRegion selectedRegion = (SpecsPhoibosRegion) partData.get(SpecsUiConstants.SELECTED_REGION);

		// Prevent NPEs
		if (sequence == null || selectedRegion == null) {
			return;
		}

		// Remove the region will fire PCLs and update the GUI
		sequence.removeRegion(selectedRegion);
	}

	@CanExecute
	public boolean canExecute(MPart part) {
		Map<String, Object> partData = part.getTransientData();
		// Get the sequence
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) partData.get(SpecsUiConstants.OPEN_SEQUENCE);
		// Get the selected region
		SpecsPhoibosRegion region = (SpecsPhoibosRegion) partData.get(SpecsUiConstants.SELECTED_REGION);

		// Can't execute if there is no sequence or selected region
		if (sequence == null || region == null) {
			return false;
		}
		return true;
	}

}