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

import java.util.List;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;

/**
 * Processes any {@link ProcessingRequestPair} against their related {@link ScanRequest}
 *
 *  @author Maurizio Nagni
 *
 *  @see ProcessingRequestHandler
 */
@Service
public class ProcessingRequestHandlerService {

	/**
	 * All the classes extending {@link DeviceHandler} are annotated with Spring {@link Component}
	 * @Autowired does the rest of the magic...
	 */
	@Autowired
	private List<ProcessingRequestHandler> handlers;

	public final void handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		for (ProcessingRequestHandler handler : handlers) {
			if (handler.handle(requestingPair, scanRequest)) {
				break;
			}
		}
	}
}
