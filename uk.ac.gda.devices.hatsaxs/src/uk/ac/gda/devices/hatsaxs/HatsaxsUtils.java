/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hatsaxs;

import gda.jython.InterfaceProvider;

import java.io.File;

public class HatsaxsUtils {

	private static final String FILENAME_FORMAT = "%s/%s.%s";

	private static final String DEFAULT_FILE_NAME = "default";
	public static final String BIOSAXS_EXTENSION = "biosaxs";
	public static final String HPLC_EXTENSION = "hplc";
	public static final String MANUAL_EXTENSION = "mps";

	private HatsaxsUtils() {
	}

	public static String getXmlDirectory() {
		return InterfaceProvider.getPathConstructor().getClientVisitSubdirectory("xml");
	}

	public static File getBioSaxsFileFromName(String name) {
		return new File(String.format(FILENAME_FORMAT, getXmlDirectory(), name, BIOSAXS_EXTENSION));
	}

	public static File getHplcFileFromName(String name) {
		return new File(String.format(FILENAME_FORMAT, getXmlDirectory(), name, HPLC_EXTENSION));
	}

	public static File getManualFileFromName(String name) {
		return new File(String.format(FILENAME_FORMAT, getXmlDirectory(), name, MANUAL_EXTENSION));
	}

	public static File getDefaultBioSaxsFile() {
		return getBioSaxsFileFromName(DEFAULT_FILE_NAME);
	}

	public static File getDefaultHplcFile() {
		return getHplcFileFromName(DEFAULT_FILE_NAME);
	}

	public static File getDefaultManualFile() {
		return getManualFileFromName(DEFAULT_FILE_NAME);
	}
}
