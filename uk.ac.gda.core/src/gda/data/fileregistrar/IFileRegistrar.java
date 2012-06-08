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

package gda.data.fileregistrar;

import gda.factory.Findable;

/**
 * File registration service that listens to scans (via DataWriterExtender) 
 * and can be used directly by detectors. Files will be archived and listed in 
 * icat and possibly post-processed. Whatever the pipeline is configured to do. 
 */
public interface IFileRegistrar extends Findable {

	/**
	 * detectors writing files independently of scans.
	 * 
	 * @param fileName <i>absolute</i> Pathname of file to register
	 */
	public void registerFile(String fileName);
	
	/**
	 * detectors writing files independently of scans.
	 * 
	 * @param fileNames An array of <i>absolute</i> Pathnames of files to register
	 */
	public void registerFiles(String[] fileNames);
}