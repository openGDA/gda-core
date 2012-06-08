/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import gda.configuration.properties.LocalProperties;

/**
 * A utility class which allows general classes to get and concatonate directories for general purpose files.
 */
public class GenFiles {
	private static String userHome = System.getProperty("user.home");

	private static String dlParams = LocalProperties.get("gda.params");

	private static String separator = System.getProperty("file.separator");

	private static String passwordFile = null;

	static {
		if (dlParams == null)
			passwordFile = userHome + separator + "passwords";
		else
			passwordFile = dlParams + separator + "passwords";
	}

	/**
	 * @return passwordFile
	 */
	public static String getPasswordFile() {
		return passwordFile;
	}
}
