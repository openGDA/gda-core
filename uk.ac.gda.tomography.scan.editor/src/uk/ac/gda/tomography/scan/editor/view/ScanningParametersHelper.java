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

import java.util.List;
import java.util.function.Supplier;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;

public class ScanningParametersHelper extends ParametersHelperBase {

	public ScanningParametersHelper(Supplier<ScanningParameters> scanningParametersSupplier) {
		super(scanningParametersSupplier);
	}

	public void addMutators(Mutator mutator, List<Number> value) {
		updateScanningParameters(getBuilder().addMutator(mutator, value));
	}

	public void removeMutators(Mutator mutator) {
		updateScanningParameters(getBuilder().removeMutator(mutator));
	}

	public void updatePoints(int points) {
		updateScanningParameters(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withPoints(points))));
	}

	public void updateStep(double step) {
		updateScanningParameters(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStep(step))));
	}

	public void updateStartAngle(double start) {
		updateScanningParameters(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStart(start))));
	}

	public void updateStopAngle(double stop) {
		updateScanningParameters(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStop(stop))));
	}
}
