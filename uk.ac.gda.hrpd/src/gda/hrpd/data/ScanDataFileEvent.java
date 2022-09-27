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

import java.util.Collection;
import java.util.EventObject;
import java.util.List;

/**
 *
 */
public class ScanDataFileEvent extends EventObject {

	private Collection<ScanDataFile> 	files;
	private ScanDataFile       currentFile;

	/**
	 * @param files 
	 * @param currentFile 
	 */
	public ScanDataFileEvent(Collection<ScanDataFile> files, ScanDataFile currentFile) {
		super(currentFile!=null?currentFile:files);
		this.files   = files;
		this.currentFile = currentFile;
	}

	/**
	 * 
	 * @param files
	 */
	public ScanDataFileEvent(List<ScanDataFile> files) {
		this(files,null);
	}

	/**
	 * @return Returns the dataFiles.
	 */
	public Collection<ScanDataFile> getDataFiles() {
		return files;
	}

	/**
	 * @param dataFiles The dataFiles to set.
	 */
	public void setDataFiles(Collection<ScanDataFile> dataFiles) {
		this.files = dataFiles;
	}

	/**
	 * @return Returns the currentFile.
	 */
	public ScanDataFile getCurrentFile() {
		return currentFile;
	}

	/**
	 * @param currentFile The currentFile to set.
	 */
	public void setCurrentFile(ScanDataFile currentFile) {
		this.currentFile = currentFile;
	}

}
