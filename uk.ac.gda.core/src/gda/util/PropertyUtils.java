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

package gda.util;

import gda.configuration.properties.LocalProperties;

import java.io.File;

/**
 * PropertyUtils Class
 */
public class PropertyUtils {
	static private void CheckDirExists(String dir, String name) throws IllegalArgumentException {
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			throw new IllegalArgumentException("GDAJythonInterpreter.CheckDirExists: " + name + " - " + dir
					+ " does not exist");
		}
	}

	/**
	 * @param key
	 * @return directory
	 * @throws IllegalArgumentException
	 */
	static public String getDirFromLocalProperties(String key) throws IllegalArgumentException {
		String dir = LocalProperties.get(key);
		if (dir == null || dir.isEmpty()) {
			throw new IllegalArgumentException("PropertyUtils.getDirFromLocalProperties: property " + key
					+ " does not exist or is empty");
		}
		if (!(dir.endsWith("\\") || dir.endsWith("/"))) {
			dir += System.getProperty("file.separator");
		}
		return dir;
	}

	/**
	 * @param key
	 * @return directory
	 * @throws IllegalArgumentException
	 */
	static public String getExistingDirFromLocalProperties(String key) throws IllegalArgumentException {
		String dir = getDirFromLocalProperties(key);
		CheckDirExists(dir, key);
		return dir;
	}
}
