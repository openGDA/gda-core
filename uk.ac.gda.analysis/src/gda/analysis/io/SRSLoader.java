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

package gda.analysis.io;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * This class should be used to load SRS datafiles
 * <p>
 * <b>Note</b>: the metadata from this loader is left as strings
 * @deprecated use {@link uk.ac.diamond.scisoft.analysis.io.SRSLoader}
 */
@Deprecated(since="at least 2012")
public class SRSLoader extends uk.ac.diamond.scisoft.analysis.io.SRSLoader {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(SRSLoader.class);

	public SRSLoader() {
		logger.deprecatedClass(null, "uk.ac.diamond.scisoft.analysis.io.SRSLoader");
	}

	/**
	 * @param fileName
	 */
	public SRSLoader(String fileName) {
		super(fileName);
		logger.deprecatedClass(null, "uk.ac.diamond.scisoft.analysis.io.SRSLoader");
	}

}
