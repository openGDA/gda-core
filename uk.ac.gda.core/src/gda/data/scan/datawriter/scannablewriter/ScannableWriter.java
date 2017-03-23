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

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;

import gda.data.scan.datawriter.SelfCreatingLink;
import gda.device.Scannable;

/**
 * This interface is used by the NexusDataWriterMetadataTree to place the "position" of a scannable in the correct place
 * in the NeXus hierarchy.
 */
public interface ScannableWriter {

	/**
	 * Retrieve a list of other Scannables that need to have their position recorded in order for this Scannable
	 * position to be valid. This is mostly the case for motion dependencies, like in diffractometers where the location
	 * of an axis depends on other prior motors.
	 *
	 * @return list of Scannable names
	 */
	public Collection<String> getPrerequisiteScannableNames();

	/**
	 * This is the call to generate the structure in the file If only
	 *
	 * @param file
	 *            reference to the NeXusfile with the pointer being in the current NXentry
	 * @param s
	 *            the scannable to write
	 * @param position
	 *            the scannable data to write (do NOT call s.getPosition() to get it!)
	 * @param dim
	 *            number of dimensions in the scan (or {1} for metadata only)
	 * @param primary
	 *            Is this scannable primary, will add primary=1 attribute to the scannables dataset
	 */
	public Collection<? extends SelfCreatingLink> makeScannable(NexusFile file, GroupNode group, Scannable s, Object position,
			int[] dim, boolean primary) throws NexusException;

	/**
	 * @param file
	 * @param s
	 * @param position
	 */
	public void writeScannable(NexusFile file, GroupNode group, Scannable s, Object position, int[] dimloc)
			throws NexusException;
}