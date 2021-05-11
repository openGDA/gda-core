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
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.script.ScriptRequest;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.ApplyNexusTemplatesRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.common.exception.GDAException;

/**
 * Handler for {@link ApplyNexusTemplatesRequest} devices
 *
 * @author Maurizio Nagni
 */
@Component
class ApplyNexusTemplateHandler extends ProcessingRequestHandler {
	@Override
	Collection<Object> translateToCollection(ProcessingRequestPair<?> processingRequest) throws GDAException {
		return Optional.ofNullable(processingRequest)
			.filter(ApplyNexusTemplatesRequest.class::isInstance)
			.map(ApplyNexusTemplatesRequest.class::cast)
			.map(this::translateValue)
			.orElse(Collections.emptyList());
	}

	/**
	 * 	At the moment gda process does not handle any {@code URL} syntax as {@code file:/path1} consequently
	 *	the method call {@link URL#getPath()} to strip the URL protocol
	 * @param request the request object
	 * @return the converted object suitable for a ScanRequest
	 */
	private Collection<Object> translateValue(ApplyNexusTemplatesRequest request) {
		return request.getValue().stream()
			.map(URL::getPath)
			.collect(Collectors.toList());
	}

	@Override
	ScriptRequest createScriptRequest(ProcessingRequestPair<?> processingRequest) throws GDAException {
		return null;
	}

}