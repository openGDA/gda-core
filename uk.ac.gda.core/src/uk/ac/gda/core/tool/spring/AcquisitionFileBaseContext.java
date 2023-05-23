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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.core.tool.URLFactory;

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

	private Map<T, Supplier<URL>> contextFiles = new HashMap<>();

	private boolean done = false;

	/**
	 * Returns the location associated with the {@code contextFile}.
	 *
	 * @param contextFile
	 * @return the resource URL, otherwise {@code null} if nothing is found.
	 */
	public URL getContextFile(T contextFile) {
		init(contextFile);
		var urlSupplier = contextFiles.get(contextFile);
		if (urlSupplier == null) return null;
		var url = urlSupplier.get();
		if (!URLFactory.urlExists(url)) {
			createDirectory(url);
		}
		return url;
	}

	private void bindContextFile(Supplier<URL> url, T contextFile) {
		contextFiles.putIfAbsent(contextFile, url);
		createDirectory(url.get());
	}

	private void createDirectory(URL url) {
		if (url == null) return;
		try {
			AcquisitionFileContextHelper.createDirectory(url);
		} catch (GDAException e) {
			logger.error("Cannot create directory {}", url, e);
		}
	}

	abstract void initializeFolderStructure();

	protected URL initializeDirectory(Supplier<URL> rootDir, String value, T contextFile) {
		Supplier<URL> urlSupplier = () -> {
			try {
				return getCustomDirectory(rootDir.get(), value);
			} catch (GDAException e) {
				logger.error("Cannot initialize {} directory", value, e);
				return null;
			}};
		bindContextFile(urlSupplier, contextFile);
		return urlSupplier.get();
	}

	protected URL initializeDirectoryInConfigDir(String value, T contextFile) {
		Supplier<URL> configDir = AcquisitionFileContextHelper::getConfigDir;
		return initializeDirectory(configDir, value, contextFile);

	}

	protected URL initializeDirectoryInVisitDir(String value, T contextFile) {
		Supplier<URL> visitDir = AcquisitionFileContextHelper::getVisitDir;
		return initializeDirectory(visitDir, value, contextFile);
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
}
