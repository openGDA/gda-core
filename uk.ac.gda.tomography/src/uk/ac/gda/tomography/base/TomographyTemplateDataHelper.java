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

package uk.ac.gda.tomography.base;

import java.util.List;
import java.util.function.Supplier;

import gda.mscan.element.Mutator;

public class TomographyTemplateDataHelper extends TemplateHelperBase {

	public TomographyTemplateDataHelper(Supplier<TomographyParameters> templateDataSupplier) {
		super(templateDataSupplier);
	}

	public void addMutators(Mutator mutator, List<Number> value) {
		updateTemplate(getBuilder().addMutator(mutator, value));
	}

	public void removeMutators(Mutator mutator) {
		updateTemplate(getBuilder().removeMutator(mutator));
	}

	public void updatePoints(int points) {
		updateTemplate(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withPoints(points))));
	}

	public void updateStep(double step) {
		updateTemplate(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStep(step))));
	}

	public void updateStartAngle(double start) {
		updateTemplate(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStart(start))));
	}

	public void updateStopAngle(double stop) {
		updateTemplate(getBuilder().withScannableTrackDocuments(assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStop(stop))));
	}
}
