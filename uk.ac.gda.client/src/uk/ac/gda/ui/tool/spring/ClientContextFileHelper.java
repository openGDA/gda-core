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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.ui.PlatformUI;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.structure.URLFactory;
import uk.ac.gda.client.exception.GDAClientException;

/**
 * Hides the client from the external file service and is primarily dedicated to help the {@link ClientContext}
 *
 * @author Maurizio Nagni
 */
public class ClientContextFileHelper {

	private static final URLFactory urlFactory = new URLFactory();

	private ClientContextFileHelper() {
	}

	/**
	 * Generates a URL from a {@code String}
	 *
	 * @param path
	 *            the location to convert
	 * @return the converted string to URL, otherwise {@code null}
	 */
	private static URL generateURL(String path) {
		try {
			return urlFactory.generateUrl(path);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	static boolean createDirectory(URL url) throws GDAClientException {
		try {
			return new File(url.toURI()).mkdirs();
		} catch (URISyntaxException e) {
			throw gdaException(url.toExternalForm()).get();
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

	static URL getTempDir() throws GDAClientException {
		return getFilePathService()
				.map(IFilePathService::getTempDir)
				.map(ClientContextFileHelper::generateURL)
				.orElseThrow(gdaException("tmp"));
	}

	static URL getConfigDir() throws GDAClientException {
		return getFilePathService()
				.map(IFilePathService::getVisitConfigDir)
				.map(ClientContextFileHelper::generateURL)
				.orElseThrow(gdaException("Configuration"));
	}

	static URL getProcessingDir() throws GDAClientException {
		return getFilePathService()
				.map(IFilePathService::getProcessingDir)
				.map(ClientContextFileHelper::generateURL)
				.orElseThrow(gdaException("Processing"));
	}

	private static Supplier<GDAClientException> gdaException(String dirName) {
		return () -> new GDAClientException(String.format("Cannot get %s directory", dirName));
	}

	/**
	 * Returns a URL based on a path defined by an application property.
	 *
	 * <p>
	 * This methods combines several cases:
	 * <ul>
	 * <li>if {@code rootDir} is not {@code null} and the property defines a relative path (does not start with
	 * backslash), the last is appended to the previous</li>
	 * <li>if {@code rootDir} is not {@code null} and the property defines an absolute path (starts with backslash),
	 * returns the last</li>
	 * <li>if {@code rootDir} is {@code null} and the property defines an absolute path (starts with backslash), returns
	 * the last</li>
	 * <li>if {@code rootDir} is not {@code null} and the property is {@code null}, returns the first</li>
	 * <li>if {@code rootDir} is {@code null} and the property defines a relative path throws a MalformedURLException
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param rootDir
	 *            the root directory if the {@code propertyKey} defines a relative path
	 * @param propertyKey
	 *            the property key to retrieve.
	 * @param defaultValue
	 *            the value if the property is absent
	 * @return a new URL based on the parameters combination
	 * @throws GDAClientException
	 *             if both the parameters are {@code null} or their value is malformed
	 */
	static URL getCustomDirectory(URL rootDir, String propertyKey, String defaultValue) throws GDAClientException {
		if (rootDir == null && propertyKey == null) {
			throw new GDAClientException("Cannot getDirectory with null parameters");
		}

		String propertyValue = LocalProperties.get(propertyKey, defaultValue);
		if (rootDir != null && propertyValue == null) {
			return rootDir;
		}

		if (propertyValue.startsWith("/")) {
			// property describes an absolute path
			return generateURL(propertyValue);
		} else {
			// property describes a relative path
			return generateURL(rootDir, propertyValue);
		}
	}

	private static Optional<IFilePathService> getFilePathService() {
		return Optional.ofNullable(PlatformUI.getWorkbench().getService(IFilePathService.class));
	}

}