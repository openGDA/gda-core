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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Utility class to read {@link AcquisitionConfiguration} documents with the guarantee to not face a {@code NullPointerException} while drilling down the properties
 *
 * @author Maurizio Nagni
 */
public class AcquisitionConfigurationReader extends AcquisitionReaderBase<AcquisitionConfigurationBase<?>> {


	public AcquisitionConfigurationReader(Supplier<AcquisitionConfigurationBase<?>> supplier) {
		super(supplier);
	}

	/**
	 * Get an {@code ImageCalibrationReader} if AcquisitionConfiguration.getImageCalibration()} is not {@code null}
	 * @return an {@code ImageCalibrationReader}, otherwise an empty instance
	 */
	public ImageCalibrationReader getImageCalibration() {
		return Optional.ofNullable(getData())
				.map(e -> new ImageCalibrationReader(e::getImageCalibration))
				.orElseGet(() -> new ImageCalibrationReader(null));
	}

	public AcquisitionParametersReader getAcquisitionParameters() {
		return Optional.ofNullable(getData())
				.map(e -> new AcquisitionParametersReader(e::getAcquisitionParameters))
				.orElseGet(() -> new AcquisitionParametersReader(null));
	}

	/**
	 * Get an unmodifiable map representing the acquisition processing request.
	 * @return an unmodifiable map
	 */
	public List<ProcessingRequestPair<?>> getProcessingRequest() {
		return Optional.ofNullable(getData())
				.map(AcquisitionConfiguration::getProcessingRequest)
				.map(Collections::unmodifiableList)
				.orElseGet(Collections::emptyList);
	}

	/**
	 * Get an unmodifiable set representing the acquisition end configuration.
	 * @return an unmodifiable set
	 */
	public Set<DevicePositionDocument> getEndPosition() {
		return Optional.ofNullable(getData())
				.map(AcquisitionConfiguration::getEndPosition)
				.map(Collections::unmodifiableSet)
				.orElseGet(Collections::emptySet);
	}
}
