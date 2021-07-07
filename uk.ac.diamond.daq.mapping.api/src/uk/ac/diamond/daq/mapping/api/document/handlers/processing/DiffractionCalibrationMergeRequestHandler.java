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
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.DiffractionContextFile;
import uk.ac.gda.core.tool.spring.ServerSpringProperties;

/**
 * Handler for {@link DiffractionCalibrationMergeRequest} devices.
 *
 * <p>
 * This handler delegates the {@link NexusDeviceService} to merge into the acquired file
 * the file referenced by {@link DiffractionCalibrationMergeRequest#getValue()}. The process creates, on-demand, a python script,
 * in the {@link DiffractionContextFile#DIFFRACTION_CONFIGURATION_DIRECTORY} which name is composed as
 * <i>update_calibration_NameFileToMerge.py</i>
 * </p>
 *
 * <p>
 * This handler depends on
 * </p>
 *
 * @author Douglas Winter
 * @author Maurizio Nagni
 */
@Component
class DiffractionCalibrationMergeRequestHandler implements ProcessingRequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(DiffractionCalibrationMergeRequestHandler.class);

	@Autowired
	private AcquisitionFileContext acquisitionFileContext;

	@Autowired
	private ServerSpringProperties serverSpringProperties;

	private Collection<Object> translateValue(DiffractionCalibrationMergeRequest request) {
		return request.getValue().stream()
			.map(URL::toString)
			.collect(Collectors.toList());
	}

	private ScriptRequest generateScriptRequest(DiffractionCalibrationMergeRequest request) {
		URL calibrationFile = request.getValue().iterator().next();
		String scriptBody = generateScript(calibrationFile);
		try {
			var scriptfile = createFile(createScriptName(calibrationFile));
			writeScript(scriptBody, scriptfile);
			return new ScriptRequest(scriptfile.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Diffraction configuration directory not correctly configured", e);
			return null;
		}
	}

	private File createFile(String scriptName) {
		return Paths.get(acquisitionFileContext.getDiffractionContext()
				.getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY).getFile(),
				scriptName).toFile();
	}

	private String createScriptName(URL calibrationFile) {
		return "update_calibration_" + FilenameUtils.getBaseName(calibrationFile.getFile()) + ".py";
	}

	private void writeScript(String contents, File script) throws IOException {
		try (Writer writer = new FileWriter(script)) {
			writer.append(contents);
		}
	}

	private String generateScript(URL calibrationFile) {
		return new StringBuilder()
				.append("nds = gda.data.ServiceHolder.getNexusDeviceService()\n")
				.append("calibration_merger = nds.getDecorator('")
				.append(getDatasetName())
				.append("')\n")
				.append("calibration_merger.setExternalFilePath('")
				.append(calibrationFile.getFile())
				.append("')\n")
				// now clean up the environment:
				.append("del nds\n")
				.append("del calibration_merger")
				.toString();
	}

	@Override
	public boolean handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		if (!(requestingPair instanceof DiffractionCalibrationMergeRequest)) {
			return false;
		}

		internalHandling(requestingPair, scanRequest);

		return true;
	}

	private void internalHandling(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		Optional.ofNullable(requestingPair)
			.map(DiffractionCalibrationMergeRequest.class::cast)
			.filter(s -> scanRequest.getBeforeScript() == null)
			.ifPresentOrElse(d -> {
				scanRequest.getProcessingRequest().getRequest().putIfAbsent(requestingPair.getKey(), translateValue(d));
				scanRequest.setBeforeScript(generateScriptRequest(d));
			}, () ->  logger.warn("Cannot set ScanRequest.BeforeScript because not empty."));
	}

	private String getDatasetName() {
		return serverSpringProperties.getProcessingRequests().getDiffractionCalibrationMerge().getDatasetName();
	}
}