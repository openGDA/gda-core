/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.jyunit.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convenience methods to build paths for Mx and Mx beamline configuration
 */
public final class MxPathsUtils {

	public static final String CONFIG_PARENT = "configurations";
	public static final String CONFIG_SUFFIX = "-config";
	public static final String MX_SPECIFIER = "mx";
	public static final String MX_REPO = "gda-mx.git";

	public static final String SCRIPTS = "scripts";
	public static final String UNIT_TESTING = "unit_testing";
	public static final String SCRIPT_NAME = "testing.py";

	/**
	 * Convenience provider of config directory names based on a specifier
	 * @param specifier mx or mx-beamline related element, examples: "mx", "i04-1", <b>"i19-shared"</b>
	 * @return the name of the configuration directory e.g. <b>i19-shared-config</b>
	 */
	public static String configDirNameOf(String specifier) {
		return specifier + CONFIG_SUFFIX;
	}

	/**
	 * Concatenates mx repository configuration path elements
	 * @param specifier mx or mx-beamline related element, examples: "mx", "i03" or "i02-2"
	 * @param furtherPathElements (non-mandatory) subdirectories and/or a file name
	 * @return path from gda-mx repo down to last element (inclusive)
	 */
	public static Path mxRepoConfigPathOf(String specifier, String... furtherPathElements) {
		var configElement = configDirNameOf(specifier);
		Path base = Paths.get(MX_REPO, CONFIG_PARENT, configElement);
		return Arrays.stream(furtherPathElements)
					.map(Paths::get)
					.reduce(base, Path::resolve);
	}

	/**
	 * mx beamline repository configuration script path
	 * @param specifier mx or mx-beamline related element, examples: "mx", "i03" or "i02-2"
	 * @return specified configuration's script directory path from gda-mx repo
	 */
	public static Path mxRepoScriptPathOf(String specifier) {
		return mxRepoConfigPathOf(specifier, SCRIPTS);
	}

	/**
	 * mx common repository configuration script path
	 * @return mx configuration script path
	 */
	public static Path mxRepoScriptPath() {
		return mxRepoScriptPathOf(MX_SPECIFIER);
	}

	/**
	 * Provides gda-mx repo standard path to unit test script
	 * @param beamlineConfigSpecifier mx or mx-beamline related configuration specifier, examples: "mx", "i03" or "i02-2"
	 * @return standard path to unit test script
	 */
	public static String unitTestingScriptPath(String beamlineConfigSpecifier) {
		return mxRepoConfigPathOf(beamlineConfigSpecifier, SCRIPTS, UNIT_TESTING, SCRIPT_NAME).toString();
	}

	/**
	 * Listing of (unique) path string combines established list with further path instances
	 * @param establishedPathList prior path listing
	 * @param furtherPaths to be added to combined list
	 * @return combined path listing excluding duplicates
	 */
	public static List<String> collectUniquePathList(List<String> establishedPathList, Path...furtherPaths) {
		var mainStream = establishedPathList.stream();
		var tributaries = Arrays.stream(furtherPaths).map(Path::toString);
		return Stream.concat(mainStream, tributaries)
					.distinct()
					.collect(Collectors.toUnmodifiableList());
	}
}
