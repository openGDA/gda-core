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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;

/**
 * Collection of methods to update a {@link ScanningParameters} instance. Constructor and methods are protected
 * because this class is not supposed to be used alone.
 *
 * @author Maurizio Nagni
 */
public class ScanningParametersHelperBase {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ScanningParametersHelperBase.class);

	/**
	 * The scanning parameters data
	 */
	private final Supplier<ScanningParameters> scanningParameterSupplier;

	protected ScanningParametersHelperBase(Supplier<ScanningParameters> scanningParameterSupplier) {
		super();
		this.scanningParameterSupplier = scanningParameterSupplier;
	}

	/**
	 * Builds a list of {@link ScannableTrackDocument} from an array of
	 * {@link uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument.Builder}.
	 *
	 * @param scannableTrackBuilders
	 * @return an array of ScannableTrackDocuments
	 */
	public static final List<ScannableTrackDocument> assembleScannableTracks(
			ScannableTrackDocument.Builder... scannableTrackBuilders) {
		List<ScannableTrackDocument> ret = new ArrayList<>();
		Arrays.stream(scannableTrackBuilders).map(ScannableTrackDocument.Builder::build).forEach(ret::add);
		return Collections.unmodifiableList(ret);
	}

	/**
	 * A {@code ScannableTrackDocument.Builder} which can be used to update a {@link ScannableTrackDocument} from the inner {@link ScanpathDocument}
	 * @param index the {@code ScanpathDocument#getScannableTrackDocuments()} index to retrieve.
	 * @return {@code ScannableTrackDocument.Builder}
	 * @deprecated use {@link ScannableTrackDocumentHelper#getScannableTrackDocumentBuilder(String)}
	 */
	@Deprecated(since="GDA 9.19")
	public ScannableTrackDocument.Builder getScannableTrackDocumentBuilder(int index) {
		logger.deprecatedMethod("getScannableTrackDocumentBuilder(int)", null, "getScannableTrackDocumentBuilder(String)");
		return Optional.ofNullable(getScanningParameters().getScanpathDocument())
				.map(scanpath -> findOrCreateScannableTrackDocument(scanpath, index))
				.orElse(new ScannableTrackDocument.Builder());
	}

	/**
	 * Similar to {@link #getScannableTrackDocumentBuilder(int)} but accept a {@code ScannableTrackDocument}.
	 * @param scanpathDocument from where extract the {@code ScannableTrackDocument}
	 * @param index the position of the required {@code ScannableTrackDocument}
	 * @return a builder or null if the index does not exist
	 */
	@Deprecated(since="GDA 9.19")
	private ScannableTrackDocument.Builder findOrCreateScannableTrackDocument(ScanpathDocument scanpathDocument, int index) {
		logger.deprecatedMethod("findOrCreateScannableTrackDocument(ScanpathDocument, int)");
		if (index <= scanpathDocument.getScannableTrackDocuments().size() - 1) {
			return new ScannableTrackDocument.Builder(scanpathDocument.getScannableTrackDocuments().get(index));
		}
		return null;
	}

	/**
	 * A {@code ScannableTrackDocument.Builder} created cloning the underlying {@link ScannableTrackDocument}
	 * with the given {@code axis}.
	 * @param axis the {@code ScanpathDocument#getScannableTrackDocuments()} axis to retrieve.
	 * @return {@code ScannableTrackDocument.Builder}, otherwise {@code null} if the axis odes not already exist
	 */
	public ScannableTrackDocument.Builder getScannableTrackDocumentBuilder(String axis) {
		return Optional.ofNullable(getScanningParameters().getScanpathDocument().getScannableTrackDocuments())
				.map(tracks -> getScannableTrackDocumentPerAxisBuilder(tracks, axis))
				.orElseGet(() -> null);
	}

	/**
	 * Returns the {@link ScannableTrackDocument} associated with the given axis
	 * @param axis
	 * @return a {@code ScannableTrackDocument}, otherwise {@code null} if the axis does not exist
	 */
	public  ScannableTrackDocument getScannableTrackDocumentPerAxis(String axis) {
		List<ScannableTrackDocument> scannableTrackDocuments = Optional.ofNullable(getScanningParameters().getScanpathDocument().getScannableTrackDocuments())
				.orElseGet(() -> null);
		return scannableTrackDocuments.stream()
				.filter(t -> t.getAxis().equals(axis))
				.findFirst()
				.orElseGet(() -> null);
	}

	private ScannableTrackDocument.Builder getScannableTrackDocumentPerAxisBuilder(List<ScannableTrackDocument> scannableTrackDocuments, String axis) {
		// A static point is not expected to have any axis associated. However the scannableTrackDocument still contains information regarding the
		// number of points in an acquisition
		if (axis == null
				&& AcquisitionTemplateType.STATIC_POINT.equals(getScanningParameters().getScanpathDocument().getModelDocument())
				&& !scannableTrackDocuments.isEmpty()) {
			return new ScannableTrackDocument.Builder(scannableTrackDocuments.get(0));
		}

		return scannableTrackDocuments.stream()
				.filter(t -> t.getAxis().equals(axis))
				.filter(Objects::nonNull)
				.findFirst()
				.map(ScannableTrackDocument.Builder::new)
				.orElseGet(() -> null);
	}

	/**
	 * Clones the existing scanpathDocument otherwise creates a new one. A class calling this method, is going to modify
	 * the {@link ScanpathDocument} instance returned by {@code getTemplateData().getScanpathDocument()}, which may
	 * still not exist. Consequently this method return a builder either on the existing {@link ScanpathDocument} or
	 * creates for the request a brand new one.
	 *
	 * @return clones the existing scanpathDocument otherwise creates a new one
	 */
	public ScanpathDocument.Builder getScanpathDocumentBuilder() {
		return ScanpathDocument.Builder.cloneScanpathDocument(getScanningParameters().getScanpathDocument());
	}

	/**
	 * Replaces the inner {@link ScanpathDocument} with the one generated by the {@code builder}
	 * @param builder the new, to build, {@code ScanpathDocument}
	 */
	public void updateScanPathDocument(ScanpathDocument.Builder builder) {
		getScanningParameters().setScanpathDocument(builder.build());
	}

	/**
	 * Replaces the inner {@link ScannableTrackDocument} with the new {@code scannableDocuments}
	 * @param scannableTrackDocuments
	 */
	protected void updateTemplateData(List<ScannableTrackDocument> scannableTrackDocuments) {
		updateScanPathDocument(getScanpathDocumentBuilder().withScannableTrackDocuments(scannableTrackDocuments));
	}

	protected ScanningParameters getScanningParameters() {
		return scanningParameterSupplier.get();
	}

}
