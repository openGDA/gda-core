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

import java.util.Objects;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.exception.GDAException;

public class ModelDocumentFactory {

	// Default constants to reference the available parameters
	protected static final int OFFSET_INDEX = 0;
	protected static final int SEED_INDEX = 1;

	private ModelDocumentFactory() {
	}

	public static final AcquisitionTemplate buildModelDocument(ScanpathDocument scanpathDocument) throws GDAException {
		Objects.requireNonNull(scanpathDocument);
		switch (scanpathDocument.getModelDocument()) {
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
}
