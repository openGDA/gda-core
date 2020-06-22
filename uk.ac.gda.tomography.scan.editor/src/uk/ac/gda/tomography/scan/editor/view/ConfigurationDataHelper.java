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

package uk.ac.gda.tomography.scan.editor.view;

import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;

/**
 * Helper methods for editing {@link MultipleScans} objects
 *
 * @author Maurizio Nagni
 */
public class ConfigurationDataHelper {

	public static final MultipleScans updateMultipleScanType(MultipleScans multipleScan, MultipleScansType multipleScanType) {
		return MultipleScans.Builder.cloneMultipleScansDocument(multipleScan).withMultipleScansType(multipleScanType).build();
	}

	public static final MultipleScans updateNumberRepetitions(MultipleScans multipleScan, int numberRepetitions) {
		return MultipleScans.Builder.cloneMultipleScansDocument(multipleScan).withNumberRepetitions(numberRepetitions).build();
	}

	public static final MultipleScans updateWaitingTime(MultipleScans multipleScan, int waitingTime) {
		return MultipleScans.Builder.cloneMultipleScansDocument(multipleScan).withWaitingTime(waitingTime).build();
	}
}
