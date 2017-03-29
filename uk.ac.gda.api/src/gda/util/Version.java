/*-
 * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version allows a code release string and number to be queried, which should be set in the private field 'RELEASE_VER'.
 */
public class Version {

	private static final String RELEASE_VER = "9.4.0";

	/**
	 * Returns the release version as a String
	 *
	 * @return string containing the release version
	 */
	public static String getRelease() {
		return RELEASE_VER;
	}

	/**
	 * Returns just the numeric Release as a double
	 *
	 * @return release number as a double.
	 */
	public static double getReleaseNumber() {
		return Double.parseDouble(RELEASE_VER.substring(0, RELEASE_VER.lastIndexOf(".")) + RELEASE_VER.substring(RELEASE_VER.lastIndexOf(".") + 1));
	}

	/**
	 * Test Main method.
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		final Logger logger = LoggerFactory.getLogger(Version.class);
		logger.info("GDA version: {} ", getRelease());
		// Also get as numeric to check
		logger.info("GDA numeric version: {}", getReleaseNumber());
	}
}
