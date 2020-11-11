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
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Provide a translation of any {@link ProcessingRequestPair#getValue()} into a collection suitable object for {@link ProcessingRequest}.
 *
 * <p>
 * {@link #translateToCollection(ProcessingRequestPair)} ability to identify a device type and produce a {@link DevicePositionDocument}
 * relies on the underlying <i>Chain of Responsibility</i> pattern composed by a list of {@link ProcessingRequestHandler} instances
 * each dedicated to handle a specific device type.
 * </p>
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

	/**
	 * The chain first element. If {@code null} means either no handlers or the class
	 * still has not created the responsibility chain.
	 */
	private ProcessingRequestHandler handler;

	/**
	 * Returns a collection suitable for a processing request element.
	 * @param processingRequest the request to translate
	 * @return the collection otherwise {@code null} if the handler is not available
	 */
	public final Collection<Object> translateToCollection(ProcessingRequestPair<?> processingRequest) {
		return Optional.ofNullable(processingRequest)
				.map(handler::handleProcess)
				.orElse(null);
	}

	/**
	 * Returns the first ScriptRequest.
	 * @param processingRequest the request to translate
	 * @return the script request otherwise {@code null} if the handler is not available
	 * @deprecated The use of script should be avoided as it may execute operations without the required control (on user, resource, other).
	 * However some processes in GDA, i.e. DiffractionCalibrationMergeProcess, are possible only through a script.
	 */
	@Deprecated
	public final ScriptRequest generateScriptRequest(ProcessingRequestPair<?> processingRequest) {
		return Optional.ofNullable(processingRequest)
				.map(handler::generateScriptRequest)
				.orElse(null);
	}

	/**
	 * Creates the responsibility chain with the handlers
	 */
	@PostConstruct
	private void initaliseService() {
		if (handler == null) {
			handler = handlers.get(0);
			handlers.stream()
				.reduce((a, b) -> {
					a.setNextHandler(b);
					return b;
			});
		}
	}
}
