/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.eclipse.core.runtime.FileLocator;

/**
 * Eclipse utilities
 *
 */
public class EclipseUtils {

	public static final String URI_SEPARATOR = "/";
	public static final String PLATFORM_BUNDLE_PREFIX = "platform:/plugin/%s";

	/**
	 * Retrieves the file corresponding to an Eclipse OSGi Bundle Entry URL referencing a file within the bundle,
	 * such as is returned by the FileLocator.find method.
	 *
	 * @param fileURL			An Eclipse OSGi format Bundle Entry URL
	 * @return					The corresponding File object if it exists
	 * @throws IOException		If the resolved path or supplied URL is invalid
	 */
	public static File resolveFileFromPlatformURL(final URL fileURL) throws IOException {
		if (!fileURL.toExternalForm().contains("bundleentry://")) {
			throw new IOException(String.format("The supplied URL %s is invalid", fileURL));
		}
		final String filePath = FileLocator.toFileURL(fileURL).getPath(); // if no corresponding file URL can be found, no conversion will
		final File file = Paths.get(filePath).toFile(); // happen meaning file will not exist resulting in an exception
		if( !(file).exists()) {
			throw new IOException(String.format("Resolved bundle file path %s does not exist for %s.", file.getAbsolutePath(), filePath));
		}
		return file;
	}

	/**
	 * Retrieves a File object embodying the absolute path of a file within a bundle from the active Equinox context
	 * based on a partial path identifying the bundle and the relative path of the file within it. For instance to get
	 * the target.txt file within the org.example bundle,  the path would be of the form org.example/target.txt. This
	 * allows correct paths to be retrieved regardless of whether the application was started from eclipse or an exported
	 * product build.
	 *
	 * @param bundleFilePath	The path that identifies the bundle and the required file within it
	 * @return					The corresponding File object resolved from the active Equinox context
	 * @throws IOException		If the resolved file does not exist or cannot be resolved in the first place
	 */
	public static File resolveBundleFile(String bundleFilePath) throws IOException {
		bundleFilePath = bundleFilePath.replace("\\", URI_SEPARATOR);
		final URL fileURL = FileLocator.find(new URL(String.format(PLATFORM_BUNDLE_PREFIX, bundleFilePath)));
		if (fileURL != null) {
			return resolveFileFromPlatformURL(fileURL);
		}
		throw new IOException(String.format("Bundle file path %s not found", bundleFilePath));
	}

	/**
	 * Retrieves a File object embodying the absolute path of a folder within a bundle from the active Equinox context
	 * based on a partial path identifying the bundle and the relative path of the folder within it. For instance to get
	 * the target folder within the org.example bundle, the path would be of the form org.example/target. This allows
	 * correct paths to be retrieved regardless of whether the application was started from eclipse or an exported
	 * product build.
	 *
	 * @param bundleFolderPath	The path that identifies the bundle and the required folder within it
	 * @return					The corresponding File object resolved from the active Equinox context
	 * @throws IOException		If the resolved folder is not actually a folder (i.e. it is some other type of file)
	 * 							or does not exist or cannot be resolved in the first place
	 */
	public static File resolveBundleFolderFile(final String bundleFolderPath) throws IOException {
		final File folder = resolveBundleFile(bundleFolderPath);
		if (!folder.isDirectory()) {
			throw new IOException(String.format("Resolved bundle folder path %s for %s is not a folder.", folder.getAbsolutePath(), bundleFolderPath));
		}
		return folder;
	}
}


