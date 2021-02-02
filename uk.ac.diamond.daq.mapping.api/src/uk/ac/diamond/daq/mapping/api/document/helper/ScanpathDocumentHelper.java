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

package uk.ac.diamond.daq.mapping.api.document.helper;

import java.util.List;
import java.util.function.Supplier;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;

/**
 * Utilities methods to update a {@link ScanpathDocument}. As the {@link ScanpathDocument} is implemented as immutable,
 * these methods wrap all the machinery to clone and update the document supplied in the constructor
 *
 * @author Maurizio Nagni
 */
public class ScanpathDocumentHelper extends ScanningParametersHelperBase {

	/**
	 * Creates a new helper to update a {@link ScanpathDocument}
	 *
	 * @param scanningParametersSupplier the supplier for a {@link ScanningParameters} document
	 */
	public ScanpathDocumentHelper(Supplier<ScanningParameters> scanningParametersSupplier) {
		super(scanningParametersSupplier);
	}

	/**
	 * Add one mutator to the list returned from {@link ScanpathDocument#getMutators()}.
	 *
	 * <p>
	 * If the {@link Mutator} does not exist is added otherwise replaces the associated values
	 * </p>
	 *
	 * @param mutator the mutator to add
	 * @param value the mutator associated values
	 */
	public void addMutators(Mutator mutator, List<Number> value) {
		updateScanPathDocument(getScanpathDocumentBuilder().addMutator(mutator, value));
	}

	/**
	 * Remove one mutator from the list returned from {@link ScanpathDocument#getMutators()}.
	 *
	 * <p>
	 * If the {@link Mutator} exist is removed added otherwise silently return
	 * </p>
	 *
	 * @param mutator the mutator to remove
	 */
	public void removeMutators(Mutator mutator) {
		updateScanPathDocument(getScanpathDocumentBuilder().removeMutator(mutator));
	}

	/**
	 * Updates the {@link AcquisitionTemplateType} associated with this document
	 * @param modelDocument the new {@link AcquisitionTemplateType}
	 */
	public void updateModelDocument(AcquisitionTemplateType modelDocument) {
		updateScanPathDocument(getScanpathDocumentBuilder().withModelDocument(modelDocument));
	}

	/**
	 * Updates the points for a single {@code ScannableTrackDocument}
	 *
	 * @param points
	 *            the new number of points for the existing scannableTrackDocument
	 *
	 * @deprecated use {@link ScannableTrackDocumentHelper#updateScannableTrackDocumentsPoints(int...)}
	 */
	@Deprecated
	public void updatePoints(int points) {
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(
				assembleScannableTracks(getScannableTrackDocumentBuilder(0).withPoints(points))));
	}

	/**
	 * Updates the step length for a single {@code ScannableTrackDocument}
	 *
	 * @param step
	 *            the new length for the existing scannableTrackDocument
	 *
	 * @deprecated use {@link ScannableTrackDocumentHelper#updateScannableTrackDocumentsSteps(int...)}
	 */
	@Deprecated
	public void updateStep(double step) {
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(
				assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStep(step))));
	}

	/**
	 * Updates the start position for a single {@code ScannableTrackDocument}
	 *
	 * @param start
	 *            the new start position for the existing scannableTrackDocument
	 *
	 * @deprecated use {@link ScannableTrackDocumentHelper#updateScannableTrackDocumentsStarts(double...)}
	 */
	@Deprecated
	public void updateStartAngle(double start) {
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(
				assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStart(start))));
	}


	/**
	 * Updates the start position for a single {@code ScannableTrackDocument}
	 *
	 * @param stop
	 *            the new stop position for the existing scannableTrackDocument
	 *
	 * @deprecated use {@link ScannableTrackDocumentHelper#updateScannableTrackDocumentsStops(double...)}
	 */
	@Deprecated
	public void updateStopAngle(double stop) {
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(
				assembleScannableTracks(getScannableTrackDocumentBuilder(0).withStop(stop))));
	}
}
