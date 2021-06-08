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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.ApplyNexusTemplatesRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;

/**
 * Handler for {@link ApplyNexusTemplatesRequest} instances.
 *
 * @author Maurizio Nagni
 */
@Component
class ApplyNexusTemplateHandler implements ProcessingRequestHandler {
	@Override
	public boolean handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		if (!(requestingPair instanceof ApplyNexusTemplatesRequest)) {
			return false;
		}

		internalHandling(requestingPair, scanRequest);

		return true;
	}

	private void internalHandling(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		Optional.ofNullable(requestingPair)
			.map(ApplyNexusTemplatesRequest.class::cast)
			.map(ApplyNexusTemplatesRequest::getValue)
			.ifPresent(u -> {
				Set<String> urlStrings = u.stream()
					.map(URL::getPath)
					.collect(Collectors.toSet());
				scanRequest.setTemplateFilePaths(urlStrings);
			});
	}
}