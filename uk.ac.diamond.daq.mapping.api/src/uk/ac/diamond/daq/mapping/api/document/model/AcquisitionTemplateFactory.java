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

package uk.ac.diamond.daq.mapping.api.document.model;

import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.common.exception.GDAException;

/**
 * Builds an {@link AcquisitionTemplate} based on a {@link AcquisitionTemplateType}.
 *
 * The Client GUI can use the {@link #isValidModelDocument(ScanpathDocument)} before further submit the document,
 * however should not use the {@link #buildModelDocument(ScanpathDocument)} as it may be restricted in future.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionTemplateFactory {

	// Default constants to reference the available parameters
	protected static final int OFFSET_INDEX = 0;
	protected static final int SEED_INDEX = 1;

	private AcquisitionTemplateFactory() {
	}

	/**
	 * Builds and validate an {@link AcquisitionTemplate} based on a {@link ScanpathDocument#getModelDocument()}.
	 *
	 * @param scanpathDocument
	 *            the acquisition geometry description
	 * @return a valid template
	 * @throws GDAException
	 *             if the {@code scanpathDocument} is {@code null} or {@link AcquisitionTemplate#validate()} fails.
	 */
	public static final AcquisitionTemplate buildModelDocument(final ScanpathDocument scanpathDocument)
			throws GDAException {
		return instantiateAndValidate(Optional.ofNullable(scanpathDocument).orElseThrow(supplyNullDocument()));
	}

	/**
	 * Validates an {@link AcquisitionTemplate} based on a {@link ScanpathDocument#getModelDocument()}.
	 *
	 * @param scanpathDocument
	 * @return {@code true} is a valid template, {@code false} otherwise.
	 */
	public static boolean isValidModelDocument(final ScanpathDocument scanpathDocument) {
		try {
			instantiateAndValidate(scanpathDocument);
			return true;
		} catch (GDAException e) {
			return false;
		}
	}

	private static AcquisitionTemplate instantiateAndValidate(final ScanpathDocument scanpathDocument)
			throws GDAException {
		AcquisitionTemplate acquisitionTemplate = instantiateAcquisitionTemplate(scanpathDocument);
		acquisitionTemplate.validate();
		return acquisitionTemplate;
	}

	private static final AcquisitionTemplate instantiateAcquisitionTemplate(ScanpathDocument scanpathDocument)
			throws GDAException {
		switch (scanpathDocument.getModelDocument()) {
		case STATIC_POINT:
			return new StaticPointModelDocument(scanpathDocument);
		case ONE_DIMENSION_LINE:
			return new AxialStepModelDocument(scanpathDocument);
		case TWO_DIMENSION_POINT:
			return new TwoAxisPointSingleModelDocument(scanpathDocument);
		case TWO_DIMENSION_LINE:
			return new TwoAxisLinePointsModelDocument(scanpathDocument);
		case TWO_DIMENSION_GRID:
			return new TwoAxisGridPointsModelDocument(scanpathDocument);
		default:
			throw new GDAException(String.format("No ModelDocument implementation available for %s",
					scanpathDocument.getModelDocument()));
		}
	}

	private static Supplier<GDAException> supplyNullDocument() {
		return () -> new GDAException("ScanpathDucoment null. Cannot create ModelDocument");
	}
}
