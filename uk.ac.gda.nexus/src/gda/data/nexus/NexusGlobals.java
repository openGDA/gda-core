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

package gda.data.nexus;

import org.nexusformat.NexusFile;

/**
 * <p>
 * <b>Title: </b>Global constants for Nexus file configuration.
 * </p>
 * <p>
 * <b>Description: </b>Use this class to hold any constant parameters that are used to construct and configure Nexus
 * files.
 * </p>
 */

public class NexusGlobals {
	/**
	 * constant denoting an unlimited dimension.
	 */
	public final static int GDA_NX_UNLIMITED = NexusFile.NX_UNLIMITED;

	/**
	 * Format is HDF4
	 */
	public final static String NEXUS_FILE_HDF4 = "HDF4";

	/**
	 * Format is HDF5
	 */
	public final static String NEXUS_FILE_HDF5 = "HDF5";

	/**
	 * Format is XML
	 */
	public final static String NEXUS_FILE_XML = "XML";

	/**
	 * Access mode is READ
	 */
	public final static String NEXUS_FILE_READ = "READ";

	/**
	 * Access mode is READWRITE
	 */
	public final static String NEXUS_FILE_READWRITE = "READWRITE";

	/**
	 * Default length of string data
	 */
	public final static int DEFAULT_LENGTH_OF_STRING_DATA = 255;

	/**
	 * Default length of description string
	 */
	public final static int DEFAULT_LENGTH_OF_DESCRIPTION_STRING = 3000;

	/**
	 * Constant defining the default backend format to be used.
	 * 
	 * <p>(We used to default to HDF4; see
	 * <a href="http://trac.nexusformat.org/code/ticket/180">NeXus ticket #180</a>)
	 */
	public final static String GDA_NX_DEFAULT = NEXUS_FILE_HDF5;
}