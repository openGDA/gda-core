/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;

public class ExternalNXlink extends SelfCreatingLink {
	String name, url;

	public ExternalNXlink(String name, String url) {
		super(null);
		this.name = name;
		this.url = url;
	}

	@Override
	public void create(NexusFile file, GroupNode g) throws NexusException {
		try {
			file.linkExternal(new URI(url), NexusUtils.addToAugmentPath(file.getPath(g), name, null), false);
		} catch (URISyntaxException e) {
			throw new NexusException("Problem creating a valid URI", e);
		}
	}
}
