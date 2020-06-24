/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import gda.data.nexus.tree.INexusTree;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;

/**
 * An interface defining methods for data writers which extend {@link DataWriter} to write nexus files.
 */
public interface INexusDataWriter extends DataWriter {

	/**
	 * Sets the template to use for the filename. This should use the string {@link String#format(String, Object...)}
	 * pattern, with a single format specifier <code>%s</code>, e.g. <code>ixx-%d.nxs</code>.
	 * @param string
	 */
	public void setNexusFileNameTemplate(String string);

	/**
	 * The file name of the nexus file being written by this writer.
	 *
	 * @return file name of nexus file
	 */
	public String getNexusFileName();

	/**
	 * Returns the full path of the folder which data files are written to.
	 *
	 * @return the full path of the folder which data files are written
	 */
	public String getDataDir();

	/**
	 * Returns a {@link SwmrStatus} object specifying the state of SWMR mode (single-write multiple-read)
	 * for the nexus file written by this writer. See the javadoc for {@link SwmrStatus} for details.
	 * @return the {@link SwmrStatus}
	 */
	public SwmrStatus getSwmrStatus();

	/**
	 * Add an {@link INexusTree} to be written as before scan metadata.
	 * TODO: do we need this method. It doesn't seem to be used, but may potentially be useful?
	 *
	 * @param beforeScanMetadata
	 */
	public void setBeforeScanMetaData(INexusTree beforeScanMetadata);

}
