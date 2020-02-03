/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.tomography.ui;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_TOMO_PARAMS_JSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.TomographyParams;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Constants and functions for use by tomography GUI
 */
public class TomographyUtils {
	private static final Logger logger = LoggerFactory.getLogger(TomographyUtils.class);

	// File to store calibration configuration
	public static final String CONFIG_DIRECTORY = "gda.tomography.config.directory";
	public static final String CALIBRATION_FILE = "gda.tomography.calibration.file";

	public static final Path CALIBRATION_DIRECTORY_PATH = Paths.get(LocalProperties.get(CONFIG_DIRECTORY));
	public static final Path CALIBRATION_FILE_PATH = Paths.get(CALIBRATION_DIRECTORY_PATH.toString(), LocalProperties.get(CALIBRATION_FILE));

	private TomographyUtils() {
		// prevent instantiation
	}
	public static String getClientMessage(ClientMessages message) {
		return ClientMessagesUtility.getMessage(message);
	}

	public static String[] getProcessingFilesAs(IMappingExperimentBean mappingBean) {
		final Map<String, Collection<Object>> processingRequest = mappingBean.getProcessingRequest();
		final List<String> processingFiles = new ArrayList<>();

		for (Map.Entry<String, Collection<Object>> entry : processingRequest.entrySet()) {
			if (entry.getKey().equals("dawn")) {
				final Collection<Object> jsonFiles = entry.getValue();
				for (Object jsonFilePath : jsonFiles) {
					try {
						// Get the path of the processing file and the tracking lines it contains
						final String json = new String(Files.readAllBytes(Paths.get((String)jsonFilePath)));
						final JsonObject jObject = new JsonParser().parse(json).getAsJsonObject();
						final String processingFilePath = jObject.get("processingFile").getAsString();
						processingFiles.add(processingFilePath);
					} catch (IOException e) {
						logger.error("Error opening JSON file {}", jsonFilePath, e);
					}
				}
			}
		}

		Collections.sort(processingFiles);
		return processingFiles.stream().toArray(String[]::new);
	}

	// Put here to ensure dry run and ordinary scan set the same parameters
	public static void populateScriptService(IScriptService scriptService, IMarshallerService marshallerService, ScanRequest scanRequest, TomographyParams tomoParams) throws Exception {
		scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
		scriptService.setNamedValue(VAR_NAME_TOMO_PARAMS_JSON, marshallerService.marshal(tomoParams));
	}
}