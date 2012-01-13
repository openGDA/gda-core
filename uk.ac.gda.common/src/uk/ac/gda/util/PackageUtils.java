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

package uk.ac.gda.util;

public class PackageUtils {

	/**
	 * Returns a sub folder called TestFiles for the use of test decks.
	 * 
	 * @param clazz
	 * @return path
	 */
	public static String getTestPath(final Class<? extends Object> clazz) {
		return getTestPath(clazz, "src");
	}

	/**
	 * @param clazz
	 * @param srcFolderName
	 * @return path
	 */
	public static String getTestPath(Class<? extends Object> clazz, String srcFolderName) {
		String pack = clazz.getPackage().getName();
		pack = pack.replace('.', '/');
		return srcFolderName + "/" + pack + "/TestFiles/";
	}
}
