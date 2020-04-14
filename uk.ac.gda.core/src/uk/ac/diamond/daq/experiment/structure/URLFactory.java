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

package uk.ac.diamond.daq.experiment.structure;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class URLFactory {

	private NameHelper nameHelper = new NameHelper();

	/**
	 * Generates a unique file URL based on the given root
	 */
	public URL generateUniqueFile(URL root, String name, String defaultName, String fileExtension) throws MalformedURLException {
		String safeName = nameHelper.makeUnique(nameHelper.makeUrlSafe(name, defaultName));
		return generateUrl(root, safeName, formatFileName(safeName, fileExtension));
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

	private String formatFileName(String name, String extension) {
		extension = extension.startsWith(".") ? extension : "." + extension;
		return name + extension;
	}


	private static class NameHelper {

		private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_]");
		private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

		public String makeUrlSafe(String rawName, String defaultName) {
			String value = StringUtils.isNotBlank(rawName) ? rawName : defaultName;
			String alphaNumericOnly = INVALID_CHARACTERS_PATTERN.matcher(value).replaceAll(" ");
			return Arrays.stream(alphaNumericOnly.split(" "))
				.map(String::trim)
				.filter(word -> !word.isEmpty())
				.map(this::capitalise)
				.collect(Collectors.joining());
		}

		public String makeUnique(String name) {
			return name + "_" + timestampFormat.format(new Date());
		}

		private String capitalise(String word) {
			String initial = word.substring(0, 1);
			return word.replaceFirst(initial, initial.toUpperCase());
		}

	}

}
