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

	public final static int NXACC_CREATE5 = NexusFile.NXACC_CREATE5;
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