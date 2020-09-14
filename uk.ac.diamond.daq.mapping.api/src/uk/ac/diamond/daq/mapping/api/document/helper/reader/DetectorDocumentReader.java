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

import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;

/**
 * Utility class to read {@link Acquisition} documents with the guarantee to not face a {@code NullPointerException} while drilling down the properties
 *
 * @author Maurizio Nagni
 */
public class DetectorDocumentReader extends AcquisitionReaderBase<DetectorDocument>  {

	public DetectorDocumentReader(Supplier<DetectorDocument> supplier) {
		super(supplier);
	}

	/**
	 *
	 * @return the existing value, otherwise {@code 0}
	 */
	public double getExposure() {
		return Optional.ofNullable(getData())
			.map(DetectorDocument::getExposure)
			.orElseGet(() -> 0d);
	}

	/**
	 *
	 * @return the existing value, otherwise {@code null}
	 */
	public String getName() {
		return Optional.ofNullable(getData())
			.map(DetectorDocument::getName)
			.orElseGet(() -> null);
	}
}
