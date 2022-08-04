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

package gda.util.logging;

import gda.configuration.properties.LocalProperties;

public class LoggingUtils {

	/**
	 * If the {@code gda.logs.dir} system property is not already set, it is set to the value of
	 * {@code gda.logs.dir} from {@code java.properties}.
	 */
	public static void setLogDirectory() {
		// The loggin config files can only do parameter interpolation from system properties,
		// so check that the standard logs directory property is defined and set from java properties if possible
		String existingProperty = System.getProperty(LocalProperties.GDA_LOGS_DIR);
		if (existingProperty == null || existingProperty.isEmpty()) {
			String fromJavaProperties = LocalProperties.get(LocalProperties.GDA_LOGS_DIR);
			if (fromJavaProperties != null && !fromJavaProperties.isEmpty()) {
				System.setProperty(LocalProperties.GDA_LOGS_DIR, fromJavaProperties);
			}
		}
	}

}
