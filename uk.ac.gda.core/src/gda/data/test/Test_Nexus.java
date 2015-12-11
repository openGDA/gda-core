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

package gda.data.test;

import gda.data.nexus.NexusFileFactory;

import org.eclipse.dawnsci.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>Test program for creating Nexus files.
 * </p>
 */

public class Test_Nexus {
	private static final Logger logger = LoggerFactory.getLogger(Test_Nexus.class);

	/**
	 * main()
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		logger.debug("Test_Nexus starting...");
		String filename = null;
		try {
			filename = new String("file://C:/GDA/nexus_files/my_first_file.nxs");
		} catch (Exception e) {
			logger.debug("ERROR: Caught Exception in main().");
		}

		// Create a new NEXUS file
		try {
			NexusFile nf = NexusFileFactory.createFile(filename, false);
			// nf.makegroup("entry1","NXentry");
			nf.close();
		} catch (Throwable e) {
			logger.debug("ERROR: Caught Throwable in main().");
			e.printStackTrace();
		}

		logger.debug("Finished.");
	}

}
