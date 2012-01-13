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

/**
 *
 */
public class OSUtils {
	/**
	 * @return true if windows
	 */
	static public boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}
	/**
	 * @return true if XP
	 */
	static public boolean isWindowsXP() {
		if (!isWindowsOS()) return false;
		return (System.getProperty("os.name").indexOf("Windows XP") == 0);
	}
	/**
	 * @return true if vista
	 */
	static public boolean isWindowsVista() {
		if (!isWindowsOS()) return false;
		return "Windows Vista".equalsIgnoreCase(System.getProperty("os.name"));
	}
	/**
	 * @return true if linux
	 */
	public static boolean isLinuxOS() {
		String os = System.getProperty("os.name");
		return os != null && os.startsWith("Linux");
	}
	/**
	 * @return true if 32-bit JVM
	 * CAUTION: this does not seem to be particularly portable (the system property is specific to the Sun (now Oracle) JVM)
	 */
	public static boolean is32bitJVM() {
		String os = System.getProperty("sun.arch.data.model");
		return os != null && os.equals("32");
	}
	/**
	 * @return true if 64-bit JVM
	 * CAUTION: this does not seem to be particularly portable (the system property is specific to the Sun (now Oracle) JVM)
	 */
	public static boolean is64bitJVM() {
		String os = System.getProperty("sun.arch.data.model");
		return os != null && os.equals("64");
	}

}
