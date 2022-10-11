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

/** Implementation of a PerScanDataLeaf
 * 
 */
public class NexusTreeScanDataLeaf extends NexusTreeNode implements PerScanDataLeaf {
	/**
	 * 
	 * @param name
	 * @param nxClass
	 * @param parentNode
	 * @param groupData
	 */
	 	public NexusTreeScanDataLeaf(String name, @SuppressWarnings("unused") String nxClass, INexusTree parentNode, NexusGroupData groupData) {
	
		super(name, NexusExtractor.SDSClassName, parentNode, groupData);
	}
	 	
	 	/**
	 	 * 
	 	 * @param name
	 	 * @param parentNode
	 	 * @param groupData
	 	 */
	public NexusTreeScanDataLeaf(String name, INexusTree parentNode, NexusGroupData groupData) {
		super(name, NexusExtractor.SDSClassName, parentNode, groupData);
	}
}
