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

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

/**
 * <p>
 * <b>Title: </b>Wrapper for <code>org.nexusformat.NexusFile</code>.
 * </p>
 * <p>
 * <b>Description: </b>Wrapper for <code>org.nexusformat.NexusFile</code>.
 * </p>
 * 
 * @see NexusFile
 */

public class GdaNexusFile extends NexusFile {

	/**
	 * Defines the filename of the data file.
	 */
	public String filename;

	/**
	 * Constructor.
	 * 
	 * @param arg0
	 * @param arg1
	 * @throws NexusException
	 * @see NexusException
	 */
	public GdaNexusFile(String arg0, int arg1) throws NexusException {
		super(arg0, arg1);
		filename = arg0;
	}

	@Override
	public void makegroup(String name, String nxclass) throws NexusException {
		// if we don't throw that here the JVM crashed (NeXus upstream bug #190)
		if (name == null || nxclass == null) {
			throw new NullPointerException();
		}
		super.makegroup(name, nxclass);
		super.opengroup(name, nxclass);
		super.closegroup();
	}
}