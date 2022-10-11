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

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;

/**
 * An interface to be implemented by a device (normally) if it is able to write additional information about itself to
 * the current position in the NeXus file.
 */
public interface INeXusInfoWriteable {

	/**
	 * @param file
	 *            nexus file handle
	 * @param node
	 * @throws NexusException 
	 */
	public void writeNeXusInformation(NexusFile file, Node node) throws NexusException;

}
