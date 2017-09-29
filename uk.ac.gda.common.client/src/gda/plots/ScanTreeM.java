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

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

// TODO what does the M mean in ScanTreeM?
public class ScanTreeM extends SelectableNode {
	private boolean visibleFLag;
	protected String name;

	public ScanTreeM() {
		this("");
	}

	ScanTreeM(String name) {
		this.name = name;
	}

	DefaultMutableTreeNode addSingleScanLine(String currentFilename, String topGrouping, ScanLine scanLine) {
		DefaultMutableTreeNode node = new SingleScanLine(currentFilename, topGrouping, scanLine);
		insert(node, 0);
		return node;
	}

	@SuppressWarnings("unchecked")
	DefaultMutableTreeNode addGroupedScanLine(String currentFilename, String topGrouping, String []subGrouping, ScanLine scanLine) {
		// find group
		ScanTreeItem scanTreeItem = null;
		Enumeration<TreeNode> e = children();
		while (e.hasMoreElements()) {
			TreeNode n = e.nextElement();
			if (n instanceof ScanTreeItem) {
				if (((ScanTreeItem) n).getName().equals(topGrouping)) {
					scanTreeItem = (ScanTreeItem) n;
					break;
				}
			}
		}
		if (scanTreeItem == null) {
			this.insert(scanTreeItem = new ScanTreeItem(currentFilename, topGrouping), 0);
		}

		return scanTreeItem.addScanLine(subGrouping, scanLine);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Selected getSelected() {
		Enumeration e = children();
		int numVisible = 0;
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof ISelectableNode) {
				Selected sel = ((ISelectableNode) obj).getSelected();
				if (sel == Selected.Some)
					return Selected.Some;
				if (sel == Selected.All) {
					numVisible++;
				}
			}
		}
		if (numVisible == 0) {
			return Selected.None;
		} else if (numVisible == getChildCount()) {
			return Selected.All;
		}
		return Selected.Some;
	}

	@Override
	public void setSelectedFlag(boolean visibleFLag) {
		this.visibleFLag = visibleFLag;

	}

	@Override
	public boolean getSelectedFlag() {
		return visibleFLag;
	}

	@Override
	public String toLabelString(int maxlength) {
		String text = toString();
		if (text.length() > maxlength) {
			text = "." + text.substring(text.length() - maxlength + 1);
		}
		return text;
	}

	public String getName() {
		return name;
	}

}
