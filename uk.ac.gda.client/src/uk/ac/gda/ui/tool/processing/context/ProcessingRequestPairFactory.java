/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.processing.context;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.mapping.api.document.helper.ProcessingRequestHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestBuilder;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Addd or removes {@link ProcessingRequestPair} from the {@link ScanningAcquisition} contained in {@link ClientSpringContext#getAcquisitionController()}
 *
 * @author Maurizio Nagni
 */
@Service
public class ProcessingRequestPairFactory {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingRequestPairFactory.class);

	@Autowired
	private ClientSpringContext clientSpringContext;

	public final <T> ProcessingRequestPair<T> insertProcessingRequestPair(ProcessingRequestBuilder<T> processingRequestBuilder, List<T> selections) {
		ProcessingRequestPair<T> processingPair = createProcessingRequestPair(processingRequestBuilder, selections);

		try {
			getHelper().addProcessingRequest(processingPair);
		} catch (GDAClientException e) {
			logger.error("Cannot add the processing pair", e);
		}

		return processingPair;
	}

	public <T> void removeProcessingRequest(ProcessingRequestPair<T> processingPair) {
		try {
			getHelper().removeProcessingRequest(processingPair);
		} catch (GDAClientException e) {
			logger.error("Cannot remove the processing pair", e);
		}
	}

	private final <T> ProcessingRequestPair<T> createProcessingRequestPair(ProcessingRequestBuilder<T> processingRequestBuilder, List<T> selections) {
		try {
			return processingRequestBuilder
				.withValue(selections)
				.build();
		} catch (IllegalArgumentException | SecurityException e) {
			logger.error("Error creating ProcessingRequestPair", e);
		}
		return null;
	}

	private Optional<ScanningConfiguration> getAcquisitionConfiguration() {
		return clientSpringContext.getAcquisitionController()
				.map(AcquisitionController::getAcquisition)
				.map(ScanningAcquisition::getAcquisitionConfiguration);
	}

	private ProcessingRequestHelper getHelper() throws GDAClientException {
		return getAcquisitionConfiguration()
			.map(sc -> new ProcessingRequestHelper(() -> sc))
			.orElseThrow(() -> new GDAClientException("Cannot instantiate helper"));
	}
}
