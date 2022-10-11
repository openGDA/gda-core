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

/**
 * Class to splice NexusTrees
 */
public class NexusTreeSplicer {
	/**
	 * @param tree1
	 * @param tree2
	 */
	public void Splice(INexusTree tree1, INexusTree tree2){
		INexusTree tree2Parent = tree2.getParentNode();
		if( tree2Parent != null){
			tree2Parent.removeChildNode(tree2);
		}
		tree1.addChildNode(tree2);
		tree2.setParentNode(tree1);
	}
	/**
	 * @param tree1
	 * @param tree2
	 */
	public void MergeTwo(INexusTree tree1, INexusTree tree2){
		while( tree2.getNumberOfChildNodes() > 0){
			Splice(tree1, tree2.getChildNode(0));
		}
	}
}
