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

	// Identifier strings that need to be read from the java.properties
	// file.
	private final static String DEVICES_XML_FILE = "gda.epics.devices.xml";

	private final static String TYPES_XML_FILE = "gda.epics.types.xml";

	private final static String DEVICES_XML_SCHEMA_FILE = "gda.epics.devices.schema";

	private final static String TYPES_XML_SCHEMA_FILE = "gda.epics.types.schema";

	// Name of any classes that are initialized using Castor (via XML
	// configuration).
	private final static String CASTOR_CODE_GENERATOR_CLASS = "org.exolab.castor.builder.SourceGenerator";

	private final static String CASTOR_CODE_GENERATOR_BINDING_FILE = "gda.epics.binding.xml";

	private final static String CASTOR_CODE_GENERATOR_SRC_DEST_DIR = "gda.epics.generated";

	// Set the debug level for the EPICS package.
	private final static String DEBUG_LEVEL = "d2";

	// set the TIMEOUT for EPICS buffer flush
	private static double TIMEOUT = 30.0;

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
	 * Name of the configuration class for the DDH (ie. that used by <code>DdhMonitor</code>, and which is normally
	 * specified in java.properties).
	 * 
	 * @return String
	 */
	public static String getCASTOR_CODE_GENERATOR_CLASS() {
		return CASTOR_CODE_GENERATOR_CLASS;
	}

	/**
	 * Name of the Meta data configuration class for files. (This is normally specified in java.properties file.)
	 * 
	 * @return String
	 */
	public static String getCASTOR_CODE_GENERATOR_BINDING_FILE() {
		return CASTOR_CODE_GENERATOR_BINDING_FILE;
	}

	/**
	 * Name of the Nexus file configuration class. (This is normally specified in java.properties file.)
	 * 
	 * @return String
	 */
	public static String getCASTOR_CODE_GENERATOR_SRC_DEST_DIR() {
		return CASTOR_CODE_GENERATOR_SRC_DEST_DIR;
	}

	/**
	 * Identifier string for EPICS Devices XML file specified in java.properties.
	 * 
	 * @return String
	 */
	public static String getDEVICES_XML_FILE() {
		return DEVICES_XML_FILE;
	}

	/**
	 * Identifier string for EPICS Device Types XML file specified in java.properties.
	 * 
	 * @return String
	 */
	public static String getTYPES_XML_FILE() {
		return TYPES_XML_FILE;
	}

	/**
	 * The minimum time allowed between the creation of a file and any copy operation on that file.
	 * 
	 * @return long
	 */
	public static String getDEVICES_XML_SCHEMA_FILE() {
		return DEVICES_XML_SCHEMA_FILE;
	}

	/**
	 * The minimum frequency allowed for polling.
	 * 
	 * @return long
	 */
	public static String getTYPES_XML_SCHEMA_FILE() {
		return TYPES_XML_SCHEMA_FILE;
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
