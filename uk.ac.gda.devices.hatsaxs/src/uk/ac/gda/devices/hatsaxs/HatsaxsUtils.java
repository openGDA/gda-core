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

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;

import java.io.File;

public class HatsaxsUtils {

	private static final String VISIT_DIRECTORY_PROPERTY = "gda.data.visitdirectory";
	
	private static final String DEFAULT_FILE_NAME = "default";
	public static final String BIOSAXS_EXTENSION = "biosaxs";
	public static final String HPLC_EXTENSION = "hplc";
	
	private HatsaxsUtils() {}

	public static String getXmlDirectory() {
		return PathConstructor.createFromTemplate(LocalProperties.get(VISIT_DIRECTORY_PROPERTY) + "/xml/");
	}
	
	public static File getBioSaxsFileFromName(String name) {
		return new File(String.format("%s/%s.%s", getXmlDirectory(), name, BIOSAXS_EXTENSION));
	}
	
	public static File getHplcFileFromName(String name) {
		return new File(String.format("%s/%s.%s", getXmlDirectory(), name, HPLC_EXTENSION));
	}

	public static File getDefaultBioSaxsFile() {
		return getBioSaxsFileFromName(DEFAULT_FILE_NAME);
	}
	
	public static File getDefaultHplcFile() {
		return getHplcFileFromName(DEFAULT_FILE_NAME);
	}
}
