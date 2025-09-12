/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.util;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class CompensatedUnixToWindowsFilePathConverter extends UnixToWindowsFilePathConverter {

	private static final String WINDOWS_PATH_REGEX_SPEC = "([a-zA-Z]:[\\\\\\/][^:])[\\\\\\/\\S]+";
	private static final Pattern WINDOWS_PATH_PATTERN = Pattern.compile(WINDOWS_PATH_REGEX_SPEC);

	@Override
	public void setWindowsSubString(String windowsSubString) {
		var compensated = compensateWindowsSubPath(windowsSubString);
		super.setWindowsSubString(compensated);
	}

	/**
	 * Converts windows like path strings into standardised format needed for accurate matching.
	 * Applies agnostic coercion so that each of the following examples
	 * x:\\data/visit, x:/data\\visit, X:/data/visit, X:\\data\\visit
	 * are compensated into the same standard windows path form x:\\data\\visit
	 * Rejects paths not recognised as windows paths, lacking drive letter or slashes
	 * @param windowsSubString
	 * @return compensated path string
	 */
	static String compensateWindowsSubPath(String windowsSubString) {
		verifyHasCanonicalWindowsPathFormat(windowsSubString);

		var elements = windowsSubString.split(":", 2);
		var drivePrefix = standardisedDriveLetterPrefix(elements[0]);
		return rebuildStandardisedPath(drivePrefix, elements[1]);
	}

	private static String standardisedDriveLetterPrefix(String driveLetter) {
		var lowerCaseDriveLetter = driveLetter.toLowerCase();
		return "%s:".formatted(lowerCaseDriveLetter);
	}

	private static String rebuildStandardisedPath(String drivePrefix, String tail) {
		var tailElements = streamStandardisedTailElements(tail);
		BinaryOperator<String> accumulator = "%s\\%s"::formatted;
		return tailElements.filter(Predicate.not(String::isEmpty))
												.reduce(drivePrefix, accumulator);
	}

	private static Stream<String> streamStandardisedTailElements(String tail) {
		var standardisedTail = tail.replace("/", "\\");
		var tailElements = standardisedTail.split("\\\\");
		return Arrays.stream(tailElements);
	}

	/**
	 * Checks substring for the format letter-colon followed by content containing slashes of either flavour
	 * @param subString path which is expected to follow the windows format e.g. x:\\data
	 */
	private static void verifyHasCanonicalWindowsPathFormat(String subString) {
		var pathMatching = WINDOWS_PATH_PATTERN.matcher(subString);
		if (!pathMatching.matches()) {
			var msg = "windowsSubString >> %s << is not of recognised format".formatted(subString);
			throw new IllegalArgumentException(msg);
		}
	}
}
