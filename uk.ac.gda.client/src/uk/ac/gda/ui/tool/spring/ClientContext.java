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

package uk.ac.gda.ui.tool.spring;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.ac.gda.client.exception.GDAClientException;

/**
 * Encapsulates the client state in a perspective-independent way so can be shared throughout the application.
 *
 * At the moment contains only references to essential URLs. In future will contain reference to the whole beamline
 * configuration (stages, cameras, other)
 *
 * @author Maurizio Nagni
 */
@Component
public class ClientContext {

	private static final Logger logger = LoggerFactory.getLogger(ClientContext.class);

	public static final String ACQUISITION_CALIBRATION_DIRECTORY_PROPERTY = "acquisition.diffraction.calibration.directory";
	public static final String ACQUISITION_CALIBRATION_DIRECTORY_PROPERTY_DEFAULT = "calibration";

	public static final String ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY = "acquisition.experiment.directory";
	public static final String ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY_DEFAULT = "experiment";

	public static final String ACQUISITION_CONFIGURATION_DIRECTORY_PROPERTY = "acquisition.configuration.directory";
	public static final String ACQUISITION_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT = "configuration";

	public enum ContextFile {
		ACQUISITION_CONFIGURATION_DIRECTORY, DIFFRACTION_CALIBRATION_DIRECTORY, DIFFRACTION_CALIBRATION, ACQUISITION_EXPERIMENT_DIRECTORY
	}

	private Map<ContextFile, URL> contextFiles = new EnumMap<>(ContextFile.class);

	private boolean done = false;

	private ClientContext() {
	}

	/**
	 * Returns the location associated with the {@code contextFile}.
	 *
	 * @param contextFile
	 * @return the resource URL, otherwise {@code null} if nothing is found.
	 */
	public final URL getContextFile(ContextFile contextFile) {
		init();
		URL value = contextFiles.get(contextFile);
		return urlExists(value) ? value : null;
	}

	private void initializeConfigurationDir() {
		URL url = null;
		try {
			url = ClientContextFileHelper.getCustomDirectory(ClientContextFileHelper.getConfigDir(),
					ACQUISITION_CONFIGURATION_DIRECTORY_PROPERTY, ACQUISITION_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT);
			bindContextFile(url, ContextFile.ACQUISITION_CONFIGURATION_DIRECTORY);
		} catch (GDAClientException e) {
			logger.error("Cannot initialize the custom directory at {}", safeURL(url), e);
		}
	}

	private void initializeExperimentDir() {
		URL url = null;
		try {
			url = ClientContextFileHelper.getCustomDirectory(ClientContextFileHelper.getProcessingDir(),
					ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY, ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY_DEFAULT);
			bindContextFile(url, ContextFile.ACQUISITION_EXPERIMENT_DIRECTORY);
		} catch (GDAClientException e) {
			logger.error("Cannot initialize the experiment directory at {}", safeURL(url), e);
		}
	}

	private void initializeDiffractionCalibrationDir() {
		URL url = null;
		try {
			url = ClientContextFileHelper.getCustomDirectory(ClientContextFileHelper.getConfigDir(),
					ACQUISITION_CALIBRATION_DIRECTORY_PROPERTY, ACQUISITION_CALIBRATION_DIRECTORY_PROPERTY_DEFAULT);
			bindContextFile(url, ContextFile.DIFFRACTION_CALIBRATION_DIRECTORY);
		} catch (GDAClientException e) {
			logger.error("Cannot initialize the calibration directory at {}", safeURL(url), e);
		}
	}

	private void bindContextFile(URL url, ContextFile contextFile) throws GDAClientException {
		String msg = Optional.ofNullable(url).map(URL::getPath).orElseGet(() -> "null URL");
		logger.info("Binding {} to {}", contextFile, msg);
		ClientContextFileHelper.createDirectory(url);
		contextFiles.putIfAbsent(contextFile, url);
	}

	private void initializeFolderStructure() {
		initializeDiffractionCalibrationDir();
		initializeConfigurationDir();
		initializeExperimentDir();
	}

	private boolean urlExists(URL url) {
		if (url == null) {
			return false;
		}
		try {
			return new File(url.toURI()).exists();
		} catch (URISyntaxException e) {
			logger.error(String.format("URL %s does not exists", url.toExternalForm()), e);
		}
		return false;
	}

	/**
	 * This method cannot use the more natural @PostConstruct because the inner {@code SpringApplicationContextProxy}
	 * would be not initialised at the time of the call. Consequently this method is called once, the first time
	 * {@link #getContextFile(ContextFile)} is called
	 *
	 */
	private void init() {
		if (done) {
			return;
		}
		initializeFolderStructure();
		done = true;
	}

	private String safeURL(URL url) {
		return url == null ? "Null URL" : url.toString();
	}

	public boolean putCalibrationFile(URL calibrationUrl) {
		logger.info("Updating {} to {}", ContextFile.DIFFRACTION_CALIBRATION, calibrationUrl);
		if (urlExists(calibrationUrl)) {
			contextFiles.put(ContextFile.DIFFRACTION_CALIBRATION, calibrationUrl);
			return true;
		}
		return false;
	}
}
