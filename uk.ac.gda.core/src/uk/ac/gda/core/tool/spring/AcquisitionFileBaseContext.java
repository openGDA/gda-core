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

package uk.ac.gda.core.tool.spring;

import static uk.ac.gda.core.tool.spring.AcquisitionFileContextHelper.getCustomDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.exception.GDAException;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext.ContextFile;

/**
 * Encapsulates the application state so can be shared throughout the application.
 *
 * At the moment contains only references to essential URLs. In future will contain reference to the whole beamline
 * configuration (stages, cameras, other)
 *
 * @author Maurizio Nagni
 */
abstract class AcquisitionFileBaseContext<T> {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionFileBaseContext.class);

	private Map<T, URL> contextFiles = new HashMap<>();

	private boolean done = false;

	/**
	 * Returns the location associated with the {@code contextFile}.
	 *
	 * @param contextFile
	 * @return the resource URL, otherwise {@code null} if nothing is found.
	 */
	public final URL getContextFile(T contextFile) {
		init(contextFile);
		URL value = contextFiles.get(contextFile);
		return urlExists(value) ? value : null;
	}

	private void bindContextFile(URL url, T contextFile) {
		contextFiles.putIfAbsent(contextFile, url);
		if (url == null) {
			return;
		}
		String msg = Optional.ofNullable(url)
				.map(URL::getPath)
				.orElseGet(() -> "null URL");
		getLogger().info("Binding {} to {}", contextFile, msg);
		try {
			AcquisitionFileContextHelper.createDirectory(url);
		} catch (GDAException e) {
			getLogger().error("Cannot create directory {}", url, e);
		}
	}

	static Path changeDirectoryPermissions(String permissions, URL url) {
		Set<PosixFilePermission> permissionSet = PosixFilePermissions.fromString(permissions);
		File dir;
		try {
			dir = new File(url.toURI());
			return Files.setPosixFilePermissions(dir.toPath(), permissionSet);
		} catch (URISyntaxException | IOException e) {
			logger.error("Cannot set directory permissions", e);
		}
		return null;
	}

	abstract void initializeFolderStructure();

	protected URL initializeDirectory(URL rootDir, String propertyKey, String defaultValue, T contextFile) {
		URL url = null;
		try {
			url = getCustomDirectory(rootDir, propertyKey, defaultValue);
		} catch (GDAException e) {
			getLogger().error("Cannot initialize {} directory", propertyKey, e);
		}
		bindContextFile(url, contextFile);
		return url;
	}

	protected URL initializeDirectoryInConfigDir(String propertyKey, String defaultValue, T contextFile) {
		URL rootDir = null;
		try {
			rootDir = AcquisitionFileContextHelper.getConfigDir();
		} catch (GDAException e) {
			logger.error("Cannot initialize the directory in ConfigDir");
		}
		return initializeDirectory(rootDir, propertyKey, defaultValue, contextFile);
	}

	protected URL initializeDirectoryInVisitDir(String propertyKey, String defaultValue, T contextFile) {
		URL rootDir = null;
		try {
			rootDir = AcquisitionFileContextHelper.getVisitDir();
		} catch (GDAException e) {
			logger.error("Cannot initialize the directory in VisitDir");
		}
		return initializeDirectory(rootDir, propertyKey, defaultValue, contextFile);
	}

	protected URL initializeDirectoryInProcessingDir(String propertyKey, String defaultValue, T contextFile) {
		URL rootDir = null;
		try {
			rootDir = AcquisitionFileContextHelper.getProcessingDir();
			return initializeDirectory(rootDir, propertyKey, defaultValue, contextFile);
		} catch (GDAException e) {
			logger.error("Cannot initialize the directory in ProcessingDir");
		}
		return initializeDirectory(rootDir, propertyKey, defaultValue, contextFile);
	}

	protected boolean urlExists(URL url) {
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
	private void init(T contextFile) {
		if (done || contextFiles.containsKey(contextFile)) {
			return;
		}
		initializeFolderStructure();
		done = true;
	}

	private void putInContext(T contextFile, URL url) {
		contextFiles.put(contextFile, url);
	}

	public boolean putFileInContext(T contextFile, URL calibrationUrl) {
		if (urlExists(calibrationUrl)) {
			putInContext(contextFile, calibrationUrl);
			return true;
		}
		return false;
	}

	protected static Logger getLogger() {
		return logger;
	}
}
