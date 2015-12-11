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

package gda.data.scan.datawriter.scannablewriter;

import gda.data.scan.datawriter.SelfCreatingLink;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;

/**
 * This interface allows writing of a single input or extra element of a scannable
 */
public interface ComponentWriter {

	public Collection<SelfCreatingLink> makeComponent(NexusFile file, GroupNode group, int[] dim, String path,
			String scannableName, String componentName, Object pos, String unit) throws NexusException;

	public void writeComponent(NexusFile file, GroupNode group, int[] start, String path, String scannableName,
			String componentName, Object pos) throws NexusException;
}
