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

import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;

/**
 * Utility class to read {@link AcquisitionEngineDocument} documents.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionEngineReader extends AcquisitionReaderBase<AcquisitionEngineDocument>  {

	public AcquisitionEngineReader(Supplier<AcquisitionEngineDocument> supplier) {
		super(supplier);
	}

	/**
	 * @return the engine type, otherwise {@code null}
	 */
	public AcquisitionEngineType getType() {
		return Optional.ofNullable(getData())
			.map(AcquisitionEngineDocument::getType)
			.orElseGet(() -> null);
	}

	/**
	 * @return the engine id, otherwise {@code null}
	 */
	public String getId() {
		return Optional.ofNullable(getData())
			.map(AcquisitionEngineDocument::getId)
			.orElseGet(() -> null);
	}
}
