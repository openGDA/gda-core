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
 * Loader to allow the XMap files to be loaded in, should be used in Java like this
 * 1. ScanFileHolder sfh = new ScanFileHolder()
 * 2. sfh.load(new XMapLoader("filename"))
 * @deprecated use {@link uk.ac.diamond.scisoft.analysis.io.XMapLoader}
 */
@Deprecated(since="at least 2012")
public class XMapLoader extends uk.ac.diamond.scisoft.analysis.io.XMapLoader {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(XMapLoader.class);

	/**
	 * Constructor which takes a filename
	 *
	 * @param fileName
	 */
	public XMapLoader(String fileName) {
		super(fileName);
		logger.deprecatedClass(null, "uk.ac.diamond.scisoft.analysis.io.XMapLoader");
	}
}
