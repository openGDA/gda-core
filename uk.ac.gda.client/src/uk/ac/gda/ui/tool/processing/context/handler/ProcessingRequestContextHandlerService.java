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

package uk.ac.gda.ui.tool.processing.context.handler;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;

/**
 * Services which loop through the various  {@link ProcessRequestContextHandler} instances.
 *
 * @author Maurizio Nagni
 */
@Service
public class ProcessingRequestContextHandlerService {
	/**
	 * All the classes extending {@link ProcessRequestContextHandler} are annotated with Spring {@link Component}
	 * @Autowired does the rest of the magic...
	 */
	@Autowired
	private List<ProcessRequestContextHandler> handlers;

	public final ProcessingRequestPair<?> handle(ProcessingRequestContext<?> processingContext) {
		return handlers.stream()
				.filter(h -> h.canHandle(processingContext))
				.findFirst()
				.map(h -> h.handle(processingContext))
				.orElse(null);
	}

	public final ProcessingRequestPair<?> handle(Shell shell, ProcessingRequestContext<?> processingContext) {
		return handlers.stream()
				.filter(h -> h.canHandle(processingContext))
				.findFirst()
				.map(h -> h.handle(shell, processingContext))
				.orElse(null);
	}

	public final String assembleTooltip(ProcessingRequestPair<?> processingPair) {
		return handlers.stream()
				.filter(h -> h.canHandle(processingPair))
				.findFirst()
				.map(h -> h.assembleTooltip(processingPair))
				.orElse(null);
	}

	public final void removeProcessingRequest(ProcessingRequestPair<?> processingPair) {
		handlers.stream()
				.filter(h -> h.canHandle(processingPair))
				.findFirst()
				.ifPresent(h -> h.removeProcessingRequest(processingPair));
	}
}
