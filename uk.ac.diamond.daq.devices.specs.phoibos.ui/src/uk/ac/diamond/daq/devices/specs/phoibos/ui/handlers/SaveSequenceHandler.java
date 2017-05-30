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

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceHelper;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class SaveSequenceHandler {

	@Execute
	public void execute(MPart part) {
		// Get the sequence open in the editor
		SpecsPhoibosSequence sequence = (SpecsPhoibosSequence) part.getTransientData().get(SpecsUiConstants.OPEN_SEQUENCE);

		// Save the sequence to the existing file path
		SpecsPhoibosSequenceHelper.saveSequence(sequence, part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH));

		// Update the hash of the saved sequence
		part.getTransientData().put(SpecsUiConstants.SAVED_SEQUENCE_HASH, sequence.hashCode());

		// Set dirty false its just been saved.
		part.setDirty(false);
	}

	@CanExecute
	public boolean canExecute(MPart part) {
		// Check if there is a path for the current file if not its unsaved.
		String path = part.getPersistedState().get(SpecsUiConstants.OPEN_SEQUENCE_FILE_PATH);
		if (path == null) {
			return false;
		}
		return part.isDirty();
	}

}