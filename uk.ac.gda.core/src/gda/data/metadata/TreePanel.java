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

package gda.data.metadata;

import gda.device.DeviceException;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * A class to create a message output window
 */
public class TreePanel extends JPanel {
	private static JTree tree = null;

	private JScrollPane pane;

	private DefaultMutableTreeNode top = null;

	private DefaultTreeModel model = null;

	private Metadata metadata = null;

	private String fedid = null;

	/**
	 * 
	 */
	public TreePanel() {
		configure();
	}

	private void configure() {
		String topName = "Investigations";
		metadata = GDAMetadataProvider.getInstance();

		if (metadata != null) {
			try {
				fedid = metadata.getMetadataValue("federalid");
				topName += " for federal id " + fedid;
			} catch (DeviceException e) {
			}
		}
		setLayout(new BorderLayout());

		top = new DefaultMutableTreeNode(topName);

		tree = new JTree();
		model = new DefaultTreeModel(top);
		tree.setModel(model);
		pane = new JScrollPane(tree);
		add(pane, BorderLayout.CENTER);
	}

	/**
	 * Clear tree.
	 */
	public void clearTree() {
		top.removeAllChildren();
	}

	/**
	 * Display.
	 */
	public void display() {
		model.reload(top);
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public DefaultTreeModel getModel() {
		return model;
	}

	/**
	 * Gets the top node.
	 * 
	 * @return the top node
	 */
	public DefaultMutableTreeNode getTopNode() {
		return top;
	}

	/**
	 * Gets the tree.
	 * 
	 * @return the tree
	 */
	public JTree getTree() {
		return tree;
	}
}
