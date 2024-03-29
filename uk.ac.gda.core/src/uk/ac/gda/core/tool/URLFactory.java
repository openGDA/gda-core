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

package uk.ac.gda.core.tool;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class URLFactory {

	/**
	 * Generates a file URL based on the given root
	 */
	public URL generateFileUrl(URL root, String name, String fileExtension) throws MalformedURLException {
		return generateUrl(root, name, appendExtension(name, fileExtension));
	}

	/** With absolute path */
	public URL generateUrl(String path) throws MalformedURLException {
		return Paths.get(path).toUri().toURL();
	}

	/** With path relative to root */
	public URL generateUrl(URL root, String... paths) throws MalformedURLException {
		return Paths.get(root.getPath(), paths).toUri().toURL();
	}

	public URL getParent(URL file) throws MalformedURLException {
		return Paths.get(file.getPath()).getParent().toUri().toURL();
	}

	public static final boolean urlExists(URL url) {
		if (url == null) {
			return false;
		}
		try {
			return new File(url.toURI()).exists();
		} catch (URISyntaxException e) {
			return false;
		}
	}

	private String appendExtension(String name, String extension) {
		extension = extension.startsWith(".") ? extension : "." + extension;
		return name + extension;
	}

}
