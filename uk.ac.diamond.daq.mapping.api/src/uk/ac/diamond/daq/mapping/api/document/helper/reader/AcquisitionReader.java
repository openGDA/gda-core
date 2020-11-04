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

import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.gda.api.acquisition.Acquisition;

/**
 * Utility class to read {@link Acquisition} documents with the guarantee to not face a {@code NullPointerException} while drilling down the properties
 *
 * @author Maurizio Nagni
 */
public class AcquisitionReader extends AcquisitionReaderBase<AcquisitionBase<?>>  {

	public AcquisitionReader(Supplier<AcquisitionBase<?>> supplier) {
		super(supplier);
	}

	/**
	 * Get the {@code Acquisition.getAcquisitionLocation()}
	 * @return the acquisition {@code URL}  location, otherwise {@code null}
	 */
	public URL getAcquisitionLocation() {
		return Optional.ofNullable(getData())
			.map(AcquisitionBase::getAcquisitionLocation)
			.orElseGet(() -> null);
	}

	/**
	 * Get a {@code AcquisitionConfigurationReader} as wrapper for the underlying AcquisitionConfigurationBase
	 * @return the acquisition {@code AcquisitionConfigurationBase}, otherwise an empty instance
	 */
	public AcquisitionConfigurationReader getAcquisitionConfiguration() {
		return Optional.ofNullable(getData())
				.map(AcquisitionBase::getAcquisitionConfiguration)
				.map(e -> new AcquisitionConfigurationReader(() -> e))
				.orElseGet(() -> new AcquisitionConfigurationReader(null));
	}

	/**
	 * Get an {@code AcquisitionEngineReader} if AcquisitionEngineReader.getAcquisitionEngine()} is not {@code null}
	 * @return an {@code AcquisitionEngineReader}, otherwise an empty instance
	 */
	public AcquisitionEngineReader getAcquisitionEngine() {
		return Optional.ofNullable(getData())
				.map(AcquisitionBase::getAcquisitionEngine)
				.map(e -> new AcquisitionEngineReader(() -> e))
				.orElseGet(() -> new AcquisitionEngineReader(null));
	}
}
