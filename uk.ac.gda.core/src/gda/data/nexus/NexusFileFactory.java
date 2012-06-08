/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

package gda.data.nexus;

import java.io.File;

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>Factory class for Nexus files.
 * </p>
 * <p>
 * <b>Description: </b> Factory class for NeXus files. NeXus files of different types can be created (new, read_only and
 * read-write). The NeXus file itself is configured according to NexusFileConfig. This class constructs Nexus files
 * ready for use elsewhere in the GDA.
 * </p>
 */

public class NexusFileFactory {
	private static final Logger logger = LoggerFactory.getLogger(NexusFileFactory.class);

	/**
	 * Create a Nexus file.
	 * 
	 * @param fileUrl
	 *            String The full filename of the file to create.
	 * @param type
	 *            String The format of NeXus file to create (HDF4, HDF5, XML).
	 * @param instrumentFileApi  - if true the nexus api is instrumented
	 * @return GdaNexusFile
	 * @throws Exception
	 */
	public static NeXusFileInterface createFile(String fileUrl, String type, boolean instrumentFileApi) throws Exception {
		NeXusFileInterface nf = null;

		// If the type of file is null, then create a file of the default type (i.e. HDF5)
		if (type == null) {
			logger.debug("GdaNeXusFile: A null file type was requested - using default NeXus format ("
					+ NexusGlobals.GDA_NX_DEFAULT + ")");
			type = NexusGlobals.GDA_NX_DEFAULT;
		}

		File f = new File(fileUrl);
		File fparent = new File(f.getParent());
		if (!fparent.exists()) {
			fparent.mkdirs();
		}
		if (type.equalsIgnoreCase("HDF5")) {
			// Create HDF5 based NeXus file.
			logger.debug("Creating HDF5 format NeXus file.");
			nf = new GdaNexusFile(fileUrl, NexusFile.NXACC_CREATE5);
		} else if (type.equalsIgnoreCase("HDF4")) {
			// Create HDF4 based NeXus file.
			logger.debug("Creating HDF4 format NeXus file.");
			nf = new GdaNexusFile(fileUrl, NexusFile.NXACC_CREATE4);
		} else if (type.equalsIgnoreCase("XML")) {
			// Create XML based NeXus file.
			logger.debug("Creating XML format NeXus file (" + fileUrl + ").");
			nf = new GdaNexusFile(fileUrl, NexusFile.NXACC_CREATEXML);
		} else {
			throw new NexusException("No matching GdaNexusFile opening mode in NexusFileFactory.");
		}
		if(instrumentFileApi)
			nf = new NexusFileWrapper(nf);
		
		// For now we will assume that all the NeXus files have a single
		// NXentry called "entry1". This is because of the way that the
		// metadata classes have been written (i.e. they expect an entry called entry1!)

		String entryName = "entry1";

		nf.makegroup(entryName, "NXentry");

		// add XES definition to file.
		NeXusUtils.writeXESraw(nf, entryName);

		// All files will conform to the XESraw and Archive definitions...

		// Return a reference to the file which is ready for use.
		return nf;
	}
}