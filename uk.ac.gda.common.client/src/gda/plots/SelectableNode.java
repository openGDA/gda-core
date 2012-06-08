/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;



import java.awt.Color;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 */
abstract class SelectableNode extends DefaultMutableTreeNode implements ISelectableNode {
	List<String> getParents() {
		Vector<String> parentsReverse = new Vector<String>();
		TreeNode p = getParent();
		while (p != null) {
			parentsReverse.add(p.toString());
			p = p.getParent();
		}
		Vector<String> parents = new Vector<String>();
		for (int i = parentsReverse.size() - 2; i >= 0; i--) {
			parents.add(parentsReverse.get(i));
		}
		return parents;
	}
	
	@Override
	public Color getColor() {
		return Color.BLACK;
	}
}
