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

	public final static int NXACC_CREATE4 = NexusFile.NXACC_CREATE4;
	public final static int NXACC_CREATE5 = NexusFile.NXACC_CREATE5;
	public final static int NXACC_CREATEXML = NexusFile.NXACC_CREATEXML;
	public final static int NXACC_READ = NexusFile.NXACC_READ;
	public final static int NXACC_RDWR = NexusFile.NXACC_RDWR;

	public final static int NX_COMP_LZW_LVL1 = NexusFile.NX_COMP_LZW_LVL1;

	public final static int NX_BOOLEAN = NexusFile.NX_BOOLEAN;
	public final static int NX_CHAR = NexusFile.NX_CHAR;

	public final static int NX_FLOAT32  = NexusFile.NX_FLOAT32;
	public final static int NX_FLOAT64  = NexusFile.NX_FLOAT64;

	public final static int NX_INT8  = NexusFile.NX_INT8;
	public final static int NX_INT16 = NexusFile.NX_INT16;
	public final static int NX_INT32 = NexusFile.NX_INT32;
	public final static int NX_INT64 = NexusFile.NX_INT64;
	public final static int NX_UINT8  = NexusFile.NX_UINT8;
	public final static int NX_UINT16 = NexusFile.NX_UINT16;
	public final static int NX_UINT32 = NexusFile.NX_UINT32;
	public final static int NX_UINT64 = NexusFile.NX_UINT64;

	public final static int NX_UNLIMITED = NexusFile.NX_UNLIMITED;
}