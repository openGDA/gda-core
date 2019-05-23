/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.utils;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beamline.synoptics.api.DetectorFileHandler;

/** Base class for DetectorFileHandler to filter files by name */
public abstract class FileNameFilteringFileHandler implements DetectorFileHandler {
	private static final Logger logger = LoggerFactory.getLogger(FileNameFilteringFileHandler.class);

	/** Predicate made up of combinations of regex to match filenames against */
	private Predicate<String> filter = s -> true;

	@Override
	public boolean canHandle(String filename) {
		logger.trace("Checking file '{}'", filename);
		return filter.test(filename);
	}

	/** Set regex file filters. If a file matches <em>any</em> of the patterns, it is accepted */
	public void setFilter(String... filters) {
		// If any filter matches, accept the file
		filter = Arrays.stream(filters)
				.map(Pattern::compile)
				.map(Pattern::asPredicate)
				.reduce((a, b) -> a.or(b))
				.orElse(s -> true);
	}
}
