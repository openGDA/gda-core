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

package uk.ac.gda.devices.pco;

import gda.configuration.properties.LocalProperties;

/**
 *
 */
public class LiveModeUtil {

	/**
	 * @return true if the server is started in 'live' mode. This is useful because some PVs are not available when the
	 *         simulation IOC is used.
	 */
	public static boolean isLiveMode() {
		String gdaMode = LocalProperties.get("gda.mode");
		if ("live".equals(gdaMode)) {
			return true;
		}
		return false;
	}
}
