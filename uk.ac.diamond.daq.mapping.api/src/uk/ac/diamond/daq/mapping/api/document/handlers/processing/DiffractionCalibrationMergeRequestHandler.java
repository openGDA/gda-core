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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.script.ScriptRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.DiffractionContextFile;

/**
 * Handler for {@link DiffractionCalibrationMergeRequest} devices
 *
 * @author Maurizio Nagni
 */
@Component
class DiffractionCalibrationMergeRequestHandler extends ProcessingRequestHandler {

	@Autowired
	private AcquisitionFileContext acquisitionFileContext;

	@Override
	Collection<Object> translateToCollection(ProcessingRequestPair<?> processingRequest) throws GDAException {
		return Optional.ofNullable(processingRequest)
			.filter(DiffractionCalibrationMergeRequest.class::isInstance)
			.map(DiffractionCalibrationMergeRequest.class::cast)
			.map(this::translateValue)
			.orElse(Collections.emptyList());
	}

	private Collection<Object> translateValue(DiffractionCalibrationMergeRequest request) {
		return request.getValue().stream()
			.map(URL::toString)
			.collect(Collectors.toList());
	}

	@Override
	ScriptRequest createScriptRequest(ProcessingRequestPair<?> processingRequest) throws GDAException {
		return Optional.ofNullable(processingRequest)
					.filter(DiffractionCalibrationMergeRequest.class::isInstance)
					.map(DiffractionCalibrationMergeRequest.class::cast)
					.map(this::generateScriptRequest)
					.orElse(null);
	}

	private ScriptRequest generateScriptRequest(DiffractionCalibrationMergeRequest request) {
		URL calibrationFile = request.getValue().iterator().next();
		String deviceName = request.getDeviceName();
		String scriptBody = generateScript(calibrationFile, deviceName);
		try {
			File scriptfile = createFile();
			writeScript(scriptBody, scriptfile);
			return new ScriptRequest(scriptfile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Diffraction configuration directory not correctly configured", e);
			return null;
		}
	}

	private File createFile() throws IOException {
		File file = Paths.get(acquisitionFileContext.getDiffractionContext()
				.getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY).getFile(),
				"update_calibration.py").toFile();
		file.createNewFile();
		return file;
	}

	private void writeScript(String contents, File script) throws IOException {
		try (Writer writer = new FileWriter(script)) {
			writer.append(contents);
		}
	}

	private String generateScript(URL calibrationFile, String deviceName) {
		return new StringBuilder()
				.append("nds = gda.data.ServiceHolder.getNexusDeviceService()\n")
				.append("calibration_merger = nds.getDecorator('")
				.append(deviceName)
				.append("')\n")
				.append("calibration_merger.setExternalFilePath('")
				.append(calibrationFile.getFile())
				.append("')\n")
				// now clean up the environment:
				.append("del nds\n")
				.append("del calibration_merger")
				.toString();
	}
}
