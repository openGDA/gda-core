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

import javax.swing.tree.TreeNode;

/**
 * represents a collection of lines from the same scan
 */
public class ScanTreeItem extends ScanTreeM {
	private String currentFilename;
	ScanTreeItem(String scanIdentifier,String s) {
		super(s);
		this.currentFilename = scanIdentifier;
	}

	@SuppressWarnings("unchecked")
	ScanPair addScanLine(String []subGroups, ScanLine scanLine) {
		// find group
		ScanPair scanPair = null;
		boolean add = false;
		if (subGroups.length == 0) {
			add(scanPair = new ScanPair(scanLine));
		} else {
			/*
			 * get ScanPairGroup amongst children - if not present create it and set flag to get it added later iterate
			 * over all names in subgroups ( but for the first item ) looking for the bottomMost subgroup if the group
			 * is not found at a level that create a new group and add to current bottomMost group finally add the
			 * scanline
			 */
			ScanPairGroup scanPairGroup = null;
			{
				Enumeration<TreeNode> e = children();
				while (e.hasMoreElements()) {
					TreeNode n = e.nextElement();
					if (n instanceof ScanPairGroup) {
						if (((ScanPairGroup) n).name.equals(subGroups[0])) {
							scanPairGroup = (ScanPairGroup) n;
						}
					}
				}
			}
			if (scanPairGroup == null) {
				scanPairGroup = new ScanPairGroup(getCurrentFilename(), subGroups[0]);
				add = true;
			}
			ScanPairGroup bottomMost = scanPairGroup;
			int index = 0;
			for (String s : subGroups) {
				index++;
				if (index == 1)
					continue;
				ScanPairGroup child = null;
				Enumeration<TreeNode> e = bottomMost.children();

				while (e.hasMoreElements()) {
					TreeNode n = e.nextElement();
					if (n instanceof ScanPairGroup) {
						if (((ScanPairGroup) n).name.equals(s)) {
							child = (ScanPairGroup) n;
						}
					}
				}
				if (child == null) {
					bottomMost.add(child = new ScanPairGroup(null,s));
				}
				bottomMost = child;
			}
			bottomMost.add(scanPair = new ScanPair(scanLine));
			if (add)
				add(scanPairGroup);
		}
		return scanPair;
	}

	/**
	 * @return id of scan from datawriter
	 */
	public String getCurrentFilename() {
		return currentFilename;
	}

	public String getName() {
		return name;
	}
}

