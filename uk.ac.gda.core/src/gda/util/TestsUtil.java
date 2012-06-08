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

/**
 * A class containing utility methods for unit tests.
 */
public class TestsUtil {
	/**
	 * Construct a path to a file used as part of a unit test. This path will in part be based on the package of a
	 * passed in object. The starting point for the directory will be either determined from the 'Java property
	 * gda.tests' or if not set the 'Java property user.home + /gda/src'. The latter is a best guess at the default
	 * location of the GDA tests directory. The class of the object is then added to the path, of course replacing the
	 * dots with separators. Finally the filename is added to complete the path. Examples based on the object
	 * tests.gda.ddh.DDHTest and the filename java.properties would be as follows.
	 * <p>
	 * No gda.tests property but user.home returns /home/shk results in
	 * /home/shk/gda/src/tests/gda/ddh/DDHTest/java.properties
	 * <p>
	 * gda.tests set to /home/shk/gda_6_12_branch/src results in
	 * /home/shk/gda_6_12_branch/src/tests/gda/ddh/DDHTest/java.properties
	 * 
	 * @param filename
	 *            The file's name.
	 * @param object
	 *            An object whose class is to be used for forming the end of the directory path.
	 * @return A complete path to the file.
	 */
	public static String constructTestPath(String filename, Object object) {
		return constructTestPath(filename,object.getClass());
	}
	
	/**
	 * @param filename
	 * @param classunderTest
	 * @return A complete path to the file.
	 * 
	 * @see TestsUtil#constructTestPath
	 */
	public static String constructTestPath(String filename, Class<? extends Object> classunderTest) {
		String separator = System.getProperty("file.separator");
		String path = System.getProperty("gda.tests");

		if (path == null) {
			path = System.getProperty("user.home");
			path += separator;
			path += "gda";
			path += separator;
			path += "src";
		}

		if (!path.endsWith(separator)) {
			path += separator;
		}

		path += classunderTest.getPackage().getName().replaceAll("\\.", separator);
		path += separator;
		path += filename;

		return path;
	}

	
	
}
