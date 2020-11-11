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

import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeProcess;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.exception.GDAException;

/**
 * Handler for {@link DiffractionCalibrationMergeProcess} devices
 *
 * @author Maurizio Nagni
 */
@Component
class DiffractionCalibrationMergeProcessHandler extends ProcessingRequestHandler {
	@Override
	Collection<Object> translateToCollection(ProcessingRequestPair<?> processingRequest) throws GDAException {
		return Optional.ofNullable(processingRequest)
			.filter(DiffractionCalibrationMergeProcess.class::isInstance)
			.map(DiffractionCalibrationMergeProcess.class::cast)
			.map(this::translateValue)
			.orElse(Collections.emptyList());
	}

	private Collection<Object> translateValue(DiffractionCalibrationMergeProcess request) {
		return request.getValue().stream()
			.map(URL::toString)
			.collect(Collectors.toList());
	}

	@Override
	ScriptRequest createScriptRequest(ProcessingRequestPair<?> processingRequest) throws GDAException {
		// To Be Done
		return null;
	}
}
