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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;

/**
 * Helper to edit the {@link ScanningConfiguration#getProcessingRequest()} element of an Acquisition
 *
 * @author Maurizio Nagni
 */
public class ProcessingRequestHelper extends ConfigurationHelperBase {

	public ProcessingRequestHelper(Supplier<ScanningConfiguration> scanningConfigurationSupplier) {
		super(scanningConfigurationSupplier);
	}

	public final void addProcessingRequest(ProcessingRequestPair<?> processingPair) {
		boolean exists = getProcessingRequest().stream()
				.filter(p ->
					 p.getKey().equals(processingPair.getKey())
							&& p.getValue().containsAll(processingPair.getValue()))
				.count() > 0;

		if (!exists)
			getProcessingRequest().add(processingPair);
	}

	public final <T> void removeProcessingRequest(ProcessingRequestPair<T> processingPair) {
		getProcessingRequest().remove(processingPair);
	}

	private List<ProcessingRequestPair<?>> getProcessingRequest() {
		return Optional.ofNullable(getScanningParameters().getProcessingRequest())
			.orElseGet(this::handleNullProcessingRequest);
	}

	private List<ProcessingRequestPair<?>> handleNullProcessingRequest() {
		List<ProcessingRequestPair<?>> requestPairs = new ArrayList<>();
		Optional.ofNullable(getScanningParameters())
			.ifPresent(c -> c.setProcessingRequest(requestPairs));
		return requestPairs;
	}
}
