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

import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.DawnProcessingRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestHandler;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;

/**
 * Handler for {@link DawnProcessingRequest} instances.
 *
 * @author Maurizio Nagni
 */
@Component
class DawnProcessingRequestHandler implements ProcessingRequestHandler {

	/**
	 * 	At the moment savu process does not handle any {@code URL} syntax as {@code file:/path1} consequently
	 *	the method call {@link URL#getPath()} to strip the URL protocol
	 * @param request the savu processing request object
	 * @return the converted object suitable for a ScanRequest
	 */
	private Collection<Object> translateValue(DawnProcessingRequest request) {
		return request.getValue().stream()
			.map(URL::getPath)
			.collect(Collectors.toList());
	}

	@Override
	public boolean handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		if (!(requestingPair instanceof DawnProcessingRequest)) {
			return false;
		}

		internalHandling(requestingPair, scanRequest);

		return true;
	}

	private void internalHandling(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		Optional.ofNullable(requestingPair)
			.map(DawnProcessingRequest.class::cast)
			.map(this::translateValue)
			.ifPresent(tv -> scanRequest.getProcessingRequest().getRequest().putIfAbsent(requestingPair.getKey(), tv));
	}
}
