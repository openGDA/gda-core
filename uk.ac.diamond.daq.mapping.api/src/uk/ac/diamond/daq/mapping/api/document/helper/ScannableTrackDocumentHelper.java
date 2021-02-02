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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;

/**
 * Utilities methods to update a {@link ScannableTrackDocument}. As the {@link ScannableTrackDocument} is implemented
 * as immutable, these methods wrap all the machinery to clone and update the document supplied in the constructor.
 *
 * @author Maurizio Nagni
 */
public class ScannableTrackDocumentHelper extends ScanningParametersHelperBase {

	public ScannableTrackDocumentHelper(Supplier<ScanningParameters> scanningParametersSupplier) {
		super(scanningParametersSupplier);
	}

	/**
	 * Updates the number of points for all the {@code ScannableTrackDocument}s with the same order in
	 * {@link ScanpathDocument#getScannableTrackDocuments()}
	 *
	 * The method silently return without updating the document if
	 * <p>
	 * {@code starts.length != getScanpathDocument().getScannableTrackDocuments().size()}
	 * </p>
	 *
	 * @param starts
	 *            the start per each scannableTrackDocument
	 */
	public void updateScannableTrackDocumentsStarts(double... starts) {
		if (starts.length != getScanningParameters().getScanpathDocument().getScannableTrackDocuments().size()) {
			return;
		}
		List<String> axes = getAxes();
		List<ScannableTrackDocument> trackDocuments = ScanningParametersHelperBase
				.assembleScannableTracks(IntStream.range(0, starts.length)
					.mapToObj(index -> getScannableTrackDocumentBuilder(axes.get(index)).withStart(starts[index]))
					.toArray(ScannableTrackDocument.Builder[]::new));
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(trackDocuments));
	}

	/**
	 * Updates the number of points for all the {@code ScannableTrackDocument}s with the same order in
	 * {@link ScanpathDocument#getScannableTrackDocuments()}
	 *
	 * The method silently return without updating the document if
	 * <p>
	 * {@code stops.length != getScanpathDocument().getScannableTrackDocuments().size()}
	 * </p>
	 *
	 * @param stops
	 *            the stop per each scannableTrackDocument
	 */
	public void updateScannableTrackDocumentsStops(double... stops) {
		if (stops.length != getScanningParameters().getScanpathDocument().getScannableTrackDocuments().size()) {
			return;
		}
		List<String> axes = getAxes();
		List<ScannableTrackDocument> trackDocuments = ScanningParametersHelperBase
				.assembleScannableTracks(IntStream.range(0, stops.length)
					.mapToObj(index -> getScannableTrackDocumentBuilder(axes.get(index)).withStop(stops[index]))
					.toArray(ScannableTrackDocument.Builder[]::new));
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(trackDocuments));
	}

	/**
	 * Updates the number of points for all the {@code ScannableTrackDocument}s with the same order in
	 * {@link ScanpathDocument#getScannableTrackDocuments()}
	 *
	 * The method silently return without updating the document if
	 * <p>
	 * {@code points.length != getScanpathDocument().getScannableTrackDocuments().size()}
	 * </p>
	 *
	 * @param points
	 *            the the points per each scannableTrackDocument
	 */
	public void updateScannableTrackDocumentsPoints(int... points) {
		if (points.length != getScanningParameters().getScanpathDocument().getScannableTrackDocuments().size()) {
			return;
		}
		List<String> axes = getAxes();
		List<ScannableTrackDocument> trackDocuments = ScanningParametersHelperBase.assembleScannableTracks(IntStream.range(0, points.length)
						.mapToObj(index -> getScannableTrackDocumentBuilder(axes.get(index)).withPoints(points[index]))
						.toArray(ScannableTrackDocument.Builder[]::new));
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(trackDocuments));
	}

	/**
	 * Updates the step length for all the {@code ScannableTrackDocument}s with the same order in
	 * {@link ScanpathDocument#getScannableTrackDocuments()}
	 *
	 * The method silently return without updating the document if
	 * <p>
	 * {@code steps.length != getScanpathDocument().getScannableTrackDocuments().size()}
	 * </p>
	 *
	 * @param steps
	 *            the step per each scannableTrackDocument
	 */
	public void updateScannableTrackDocumentsSteps(int... steps) {
		if (steps.length != getScanningParameters().getScanpathDocument().getScannableTrackDocuments().size()) {
			return;
		}
		List<String> axes = getAxes();
		List<ScannableTrackDocument> trackDocuments = ScanningParametersHelperBase
				.assembleScannableTracks(IntStream.range(0, steps.length)
					.mapToObj(index -> getScannableTrackDocumentBuilder(axes.get(index)).withStep(steps[index]))
					.toArray(ScannableTrackDocument.Builder[]::new));
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(trackDocuments));
	}

	private List<String> getAxes() {
		return getScanningParameters().getScanpathDocument().getScannableTrackDocuments().stream()
				.map(ScannableTrackDocument::getAxis)
				.collect(Collectors.toList());
	}
}
