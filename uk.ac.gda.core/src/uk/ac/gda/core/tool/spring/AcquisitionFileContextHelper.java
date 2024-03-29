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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.core.tool.URLFactory;

/**
 * Hides the application from the external file service and is primarily dedicated to help the {@link AcquisitionFileContext}
 *
 * @author Maurizio Nagni
 */
class AcquisitionFileContextHelper {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionFileContextHelper.class);

	private static final URLFactory urlFactory = new URLFactory();

	private AcquisitionFileContextHelper() {
	}

	/**
	 * Generates a URL from a {@code String}
	 *
	 * @param path
	 *            the location to convert. Has to be an absolute path.
	 * @return the converted string to URL, otherwise {@code null}
	 */
	private static URL generateURL(String path) {
		if (path == null) return null;
		if ((!new File(path).isAbsolute())) {
			logger.error("Cannot generateURL with not absolute path \"{}\"", path);
			return null;
		}

		try {
			return urlFactory.generateUrl(path);
		} catch (MalformedURLException e) {
			logger.error("Cannot generateURL from path {}", path);
			return null;
		}
	}

	static boolean createDirectory(URL url) throws GDAException {
		try {
			return new File(url.toURI()).mkdirs();
		} catch (URISyntaxException e) {
			throw new GDAException(String.format("Cannot get %s directory", url.toExternalForm()));
		}
	}

	/**
	 * Appends an array of {@code paths} to a {@code root} path
	 *
	 * @param root
	 *            the root directory
	 * @param paths
	 *            an array of subpaths
	 * @return the concatenated path
	 */
	private static URL generateURL(URL root, String... paths) {
		try {
			return urlFactory.generateUrl(root, paths);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	static URL getTempDir() {
		return generateURL(ServiceProvider.getService(IFilePathService.class).getTempDir());
	}

	static URL getConfigDir() {
		return generateURL(ServiceProvider.getService(IFilePathService.class).getVisitConfigDir());
	}

	static URL getVisitDir() {
		return generateURL(ServiceProvider.getService(IFilePathService.class).getVisitDir());
	}

	static URL getProcessingDir() {
		return generateURL(ServiceProvider.getService(IFilePathService.class).getProcessingDir());
	}

	/**
	 * Returns a URL based on a path defined by an application property.
	 *
	 * <p>
	 * This methods combines several cases:
	 * <ul>
	 * <li>if {@code rootDir} is not {@code null} and the {@code value} defines a relative path (does not start with
	 * backslash), the last is appended to the previous</li>
	 * <li>if {@code rootDir} is not {@code null} and the {@code value} defines an absolute path (starts with backslash),
	 * returns the last</li>
	 * <li>if {@code rootDir} is {@code null} and the property defines an absolute path (starts with backslash), returns
	 * the last</li>
	 * <li>if {@code rootDir} is not {@code null} and the {@code value} is {@code null}, returns the first</li>
	 * <li>if {@code rootDir} is {@code null} and the {@code value} defines a relative path throws a GDAException
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param rootDir
	 *            the root directory if the {@code propertyKey} defines a relative path
	 * @param value
	 *            the file path relative to the rootDir
	 * @return a new URL based on the parameters combination
	 * @throws GDAException
	 *             if both the parameters are {@code null} or their value is malformed
	 */
	static URL getCustomDirectory(URL rootDir, String value) throws GDAException {
		if (value == null) {
			throw new GDAException("Cannot getDirectory with null parameters");
		}

		if (new File(value).isAbsolute()) {
			// property describes an absolute path
			return generateURL(value);
		} else {
			if (rootDir == null) {
				throw new GDAException("Cannot set a relative path for " + value + " without a root path");
			}
			// property describes a relative path
			return generateURL(rootDir, value);
		}
	}
}