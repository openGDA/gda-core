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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Utility class to read {@link AcquisitionParametersBase} documents.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionParametersReader extends AcquisitionReaderBase<AcquisitionParametersBase> {

	public AcquisitionParametersReader(Supplier<AcquisitionParametersBase> supplier) {
		super(supplier);
	}

	/**
	 * Get the {@code AcquisitionParametersBase.getScanpathDocument()}
	 * @return the scanpath, otherwise {@code null}
	 */
	public ScanpathDocumentReader getScanpathDocument() {
		return Optional.ofNullable(getData())
				.map(AcquisitionParametersBase::getScanpathDocument)
				.map(e -> new ScanpathDocumentReader(() -> e))
				.orElseGet(() -> new ScanpathDocumentReader(null));
	}

	/**
	 * Get the {@code AcquisitionParametersBase.getDetectorDocument()}
	 * @return the detectorDocument, otherwise {@code null}
	 */
	public DetectorDocumentReader getDetector() {
		return Optional.ofNullable(getData())
			.map(AcquisitionParametersBase::getDetector)
			.map(e -> new DetectorDocumentReader(() -> e))
			.orElseGet(() -> new DetectorDocumentReader(null));
	}

	/**
	 * Get the {@code AcquisitionParametersBase.getPosition()}
	 * @return the start position, otherwise empty {@code Set}
	 */
	public Set<DevicePositionDocument> getPosition() {
		return Optional.ofNullable(getData())
			.map(AcquisitionParametersBase::getPosition)
			.map(Collections::unmodifiableSet)
			.orElseGet(() -> Collections.unmodifiableSet(new HashSet<>()));
	}
}
