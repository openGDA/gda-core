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

package uk.ac.diamond.daq.mapping.api.document.handlers.processing;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.exception.GDAException;

/**
 * Base implementation of the logic supporting the <i>Chain of Responsibility</i> to translate a {@link ProcessingRequestPair#getValue()}
 * in a collection understandable by the {@link ScanRequest}
 *
 * @author Maurizio Nagni
 */
abstract class ProcessingRequestHandler {

	static final Logger logger = LoggerFactory.getLogger(ProcessingRequestHandler.class);

	/**
	 * The next handler to call if this one cannot handle the device
	 */
	private ProcessingRequestHandler nextHandler;

	/**
	 * Sets a reference to the next handler.
	 * @param nextHandler
	 */
	void setNextHandler(ProcessingRequestHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	/**
	 * Creates a {@link ProcessingRequestPair} from the passed {@code processingRequest} object
	 * @param processingRequest The processingRequest to handle
	 * @return A collection understandable by the {@link ScanRequest} otherwise {@code null} if cannot be handled
	 */
	public final Collection<Object> handleDevice(ProcessingRequestPair<?> processingRequest) {
		try {
			return doHandleProcessingRequest(processingRequest);
		} catch (GDAException e) {
			logger.error("Cannot handle device {} ", processingRequest, e);
			return null;
		}
	}

	/**
	 * Creates a {@code Collection<Object>} from the {@code processingRequest} object
	 * @param processingRequest The processingRequest to handle
	 * @return A collection understandable by the {@link ScanRequest} otherwise {@code null} if cannot be handled
	 * @throws GDAException If the request has the correct type but an error occurred during the analysis
	 */
	abstract Collection<Object> translateToCollection(ProcessingRequestPair<?> processingRequest) throws GDAException;

	private Collection<Object> doHandleProcessingRequest(ProcessingRequestPair<?> processingRequest) throws GDAException {
		Collection<Object> response = translateToCollection(processingRequest);
		if (response != null)
			return response;
		if (nextHandler != null) {
			return nextHandler.handleDevice(processingRequest);
		}
		logger.error("No suitable handler found for processingRequest {} ", processingRequest);
		return Collections.emptyList();
	}
}
