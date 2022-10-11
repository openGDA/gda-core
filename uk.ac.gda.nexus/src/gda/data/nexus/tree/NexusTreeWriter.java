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

package gda.data.nexus.tree;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;

/**
 * Class that add a NexusTree to a Nexus file
 */
public class NexusTreeWriter {
	
	/**
	 * Adds a Nexus tree to a Nexus file.
	 * 
	 * @param file the Nexus file
	 * @param tree the Nexus tree
	 * 
	 * @throws NexusException
	 */
	public static void writeHere(NexusFile file, GroupNode group, INexusTree tree) throws NexusException {
		String name = tree.getName();
		String nxClass = tree.getNxClass();

		if( nxClass.equals(NexusExtractor.AttrClassName)){
			NexusGroupData data = tree.getData();
			if( data != null && data.getBuffer() != null){
				NexusUtils.writeAttribute(file, group, name, data.toDataset());
			}
			return;
		}
		if (!name.isEmpty() && !nxClass.isEmpty()) {
			group = file.getGroup(group, name, nxClass, true);
		}
		for (INexusTree branch : tree) {
			writeHere(file, group, branch);
		}
	}
}
