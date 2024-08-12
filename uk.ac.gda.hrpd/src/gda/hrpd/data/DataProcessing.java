/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
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

package gda.hrpd.data;

import java.io.File;

/**
 * Interface for post-collection raw data processing. It takes a raw data file in and output a new processed data file.
 */
public interface DataProcessing {
	/**
	 * processing raw data
	 * @param rawdata - the file hold the raw data
	 * @return the file storing processed data
	 */
	public File processData(File rawdata);
	public void completeProcess();
	
}
