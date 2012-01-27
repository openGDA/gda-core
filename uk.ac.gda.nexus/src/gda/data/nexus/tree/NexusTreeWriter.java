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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

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
	public static void writeHere(NexusFile file, INexusTree tree) throws NexusException{
		String name = tree.getName();
		String nxClass = tree.getNxClass();
		if( nxClass.equals(NexusExtractor.AttrClassName)){
			NexusGroupData data = tree.getData();
			if( data != null && data.getBuffer() != null){
				file.putattr(name, data.getBuffer(), data.type);
			}
			return;
		}
		if(! name.isEmpty() && ! nxClass.isEmpty()){
			if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
				file.makegroup(name, nxClass);
			}
			file.opengroup(name, nxClass);
		}
		try{
			for( INexusTree branch : tree ){
				writeHere(file,branch);
			}
		} finally {
			if(! name.isEmpty() && ! nxClass.isEmpty()){
				file.closegroup();
			}
		}
	}
}
