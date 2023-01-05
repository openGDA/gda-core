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

package uk.ac.diamond.daq.mapping.api.document.helper.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;

/**
 * Utility class to read {@link ScanpathDocument} documents.
 *
 * @author Maurizio Nagni
 */
public class ScanpathDocumentReader extends AcquisitionReaderBase<ScanpathDocument> {

	public ScanpathDocumentReader(Supplier<ScanpathDocument> supplier) {
		super(supplier);
	}

	/**
	 *
	 * @return the existing value, otherwise {@code null}
	 */
	public AcquisitionTemplateType getModelDocument() {
		return Optional.ofNullable(getData())
			.map(ScanpathDocument::getModelDocument)
			.orElseGet(() -> null);
	}

	/**
	 *
	 * @return the existing value, otherwise empty {@code List}
	 */
	public List<ScannableTrackDocument> getScannableTrackDocuments() {
		return Optional.ofNullable(getData())
			.map(e -> Collections.unmodifiableList(e.getScannableTrackDocuments()))
			.orElseGet(() -> Collections.unmodifiableList(new ArrayList<>()));
	}
}
