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

import uk.ac.diamond.daq.experiment.structure.URLFactory;
import uk.ac.gda.common.exception.GDAException;

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
	public URL getContextFile(T contextFile) {
		init(contextFile);
		URL value = contextFiles.get(contextFile);
		return URLFactory.urlExists(value) ? value : null;
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

	protected URL initializeDirectory(URL rootDir, String value, T contextFile) {
		URL url = null;
		try {
			url = getCustomDirectory(rootDir, value);
		} catch (GDAException e) {
			getLogger().error("Cannot initialize {} directory", value, e);
		}
		bindContextFile(url, contextFile);
		return url;
	}

	protected URL initializeDirectoryInConfigDir(String value, T contextFile) {
		URL rootDir = null;
		try {
			rootDir = AcquisitionFileContextHelper.getConfigDir();
		} catch (GDAException e) {
			logger.error("Cannot initialize the directory in ConfigDir");
		}
		return initializeDirectory(rootDir, value, contextFile);
	}

	protected URL initializeDirectoryInVisitDir(String value, T contextFile) {
		URL rootDir = null;
		try {
			rootDir = AcquisitionFileContextHelper.getVisitDir();
		} catch (GDAException e) {
			logger.error("Cannot initialize the directory in VisitDir");
		}
		return initializeDirectory(rootDir, value, contextFile);
	}

	/**
	 * This method cannot use the more natural @PostConstruct because the inner {@code SpringApplicationContextProxy}
	 * would be not initialised at the time of the call. Consequently this method is called once, the first time
	 * {@link #getContextFile(Object)} is called
	 *
	 */
	private void init(T contextFile) {
		if (done || contextFiles.containsKey(contextFile)) {
			return;
		}
		initializeFolderStructure();
		done = true;
	}

	private void putInContext(T contextFile, URL resource) {
		contextFiles.put(contextFile, resource);
	}

	/**
	 * Set the selection for a specified context.
	 * @param contextFile the context to associate
	 * @param resource the resource to associate with the context
	 * @return {@code true} if successful, {@code false} otherwise
	 * @see #removeFileFromContext(Object)
	 */
	public boolean putFileInContext(T contextFile, URL resource) {
		if (URLFactory.urlExists(resource)) {
			putInContext(contextFile, resource);
			return true;
		}
		return false;
	}

	/**
	 * Removes the selection from a specified context
	 * @param contextFile the context from where remove the selection
	 * @return the previous value associated with key, or null if there was no mapping for key.
	 * @see #putFileInContext(Object, URL)
	 */
	public URL removeFileFromContext(T contextFile) {
		return contextFiles.remove(contextFile);
	}

	protected static Logger getLogger() {
		return logger;
	}
}
