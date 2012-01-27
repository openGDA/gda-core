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

package gda.data.nexus;

/**
 * Interface that can be implemented by a device (normally a Detector) if it is able to write the data file it has
 * produced to a given place in the NeXus file.
 */
public interface INeXusFileWritableDevice {

	/**
	 * @param nexusFile
	 *            nexus filename
	 * @param nexusPath
	 *            path to the device entry within the nexus file
	 * @param dataFile
	 *            external data file name
	 * @param dimensions
	 */
	public void writeNexusData(String nexusFile, String nexusPath, String dataFile, int[] dimensions);

}
