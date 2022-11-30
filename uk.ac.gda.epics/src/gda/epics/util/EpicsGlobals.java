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

package gda.epics.util;

import gda.configuration.properties.LocalProperties;

/**
 * <p>
 * <b>Title: </b>Global constants and functions for integration with EPICS system.
 * </p>
 * <p>
 * <b>Description: </b>Class providing global functions/constants to the gda.epics package. Access to constants is only
 * possible through get methods.
 * </p>
 */

public final class EpicsGlobals {
	// This is the buffer size to use when reading/writing files.
	private final static int BUFFERSIZE = 65536; // 64k

	// private final static int BUFFERSIZE = 131072; //128k

	// Set the debug level for the EPICS package.
	private final static String DEBUG_LEVEL = "d2";

	// set the TIMEOUT for EPICS buffer flush
	private static double TIMEOUT = 5;

	// set the DELAY in millisceonds for EPICS channel calls
	private static final long DELAY = 100;

	// The escape character for URIs
	private final static char EPICS_URI_PATH_SEPARATOR = '/';

	// The character encoding to use when encoding URIs from Strings.
	// This is used at the highlest level in DdhMonitorConfig.
	private final static String CHAR_ENCODING = "UTF-8";

	/**
	 * Size of buffer to use when reading & writing files.
	 *
	 * @return int
	 */
	public static int getBUFFERSIZE() {
		return BUFFERSIZE;
	}

	/**
	 * The debug level to use with <code>DdhMessage</code> (and <code>Message</code>).
	 *
	 * @return String
	 */
	public static String getDEBUG_LEVEL() {
		return DEBUG_LEVEL;
	}

	/**
	 * The path separator for use in URIs.
	 *
	 * @return Character to use for path separators in URIs.
	 */
	public static char getDH_URI_PATH_SEPARATOR() {
		return EPICS_URI_PATH_SEPARATOR;
	}

	/**
	 * The character encoding to use when encoding URIs from Strings. This is used at the highest level in
	 * DdhMonitorConfig.
	 *
	 * @return the char encoding
	 */
	public static String getCHAR_ENCODING() {
		return CHAR_ENCODING;
	}

	/**
	 * @return TIMEOUT
	 */
	public static double getTimeout() {
		String requesttimeout = null;
		if ((requesttimeout = LocalProperties.get("gda.epics.request.timeout")) != null)
			TIMEOUT = Double.parseDouble(requesttimeout);
		return TIMEOUT;
	}

	/**
	 * @param timeout
	 */
	public static void setTimeout(double timeout) {
		TIMEOUT = timeout;
	}

	/**
	 * @return DELAY
	 */
	public static long getDELAY() {
		return DELAY;
	}
}
