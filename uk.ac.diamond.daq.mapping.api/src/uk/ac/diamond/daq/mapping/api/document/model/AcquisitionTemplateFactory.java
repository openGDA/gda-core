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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.common.exception.GDAException;

/**
 * Builds an {@link AcquisitionTemplate} based on a {@link TrajectoryShape}.
 *
 * The Client GUI can use the {@link #isValidModelDocument(ScanpathDocument)} before further submit the document,
 * however should not use the {@link #buildModelDocument(ScanpathDocument)} as it may be restricted in future.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionTemplateFactory {

	private AcquisitionTemplateFactory() {
	}

	/**
	 * Validates given {@link ScanpathDocument} and builds corresponding {@link AcquisitionTemplate}s.
	 *
	 * @param scanpathDocument
	 *            the acquisition geometry description
	 * @return a valid template
	 * @throws GDAException
	 *             if the {@code scanpathDocument} is {@code null} or {@link AcquisitionTemplate#validate()} fails.
	 */
	public static final List<AcquisitionTemplate> buildModelDocument(final ScanpathDocument scanpathDocument)
			throws GDAException {
		return instantiateAndValidate(Optional.ofNullable(scanpathDocument).orElseThrow(supplyNullDocument()));
	}

	public static boolean isValidModelDocument(final ScanpathDocument scanpathDocument) {
		try {
			instantiateAndValidate(scanpathDocument);
			return true;
		} catch (GDAException e) {
			return false;
		}
	}

	private static List<AcquisitionTemplate> instantiateAndValidate(final ScanpathDocument scanpathDocument) throws GDAException {
		var templates = scanpathDocument.getTrajectories().stream().map(AcquisitionTemplateFactory::getAcquisitionTemplate).toList();
		for (var template : templates) template.validate();
		return templates;
	}

	private static AcquisitionTemplate getAcquisitionTemplate(Trajectory trajectory) {
		return switch (trajectory.getShape()) {
			case STATIC_POINT -> new AxialStepModelDocument(trajectory);
			case ONE_DIMENSION_LINE -> new AxialStepModelDocument(trajectory);
			case TWO_DIMENSION_POINT -> new TwoAxisPointSingleModelDocument(trajectory);
			case TWO_DIMENSION_LINE -> new TwoAxisLinePointsModelDocument(trajectory);
			case TWO_DIMENSION_GRID -> new TwoAxisGridPointsModelDocument(trajectory);
			default -> throw new IllegalStateException(String.format("No AcquisitionTemplate implementation available for %s",
						trajectory.getShape()));
		};
	}

	private static Supplier<GDAException> supplyNullDocument() {
		return () -> new GDAException("ScanpathDucoment null. Cannot create ModelDocument");
	}
}
