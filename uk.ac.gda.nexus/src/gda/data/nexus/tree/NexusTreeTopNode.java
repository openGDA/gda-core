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

import java.net.URL;

/**
 * class that implements INexusTree and hold the source e.g. filename
 */
public class NexusTreeTopNode extends NexusTreeNode implements INexusTree, INexusSourceProvider {
	
	private static final long serialVersionUID = 1L;

	final URL source;
	INexusTree node;

	@Override
	public URL getSource() {
		return source;
	}
	
	NexusTreeTopNode(INexusTree node, URL source){
		super(node.getName(), node.getNxClass(), node.getParentNode(), node.getData());
		this.source = source;
	}
}