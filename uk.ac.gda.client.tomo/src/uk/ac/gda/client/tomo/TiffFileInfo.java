/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo;

/**
 *
 */
public class TiffFileInfo {

	private String filePath;

	private String fileName;

	private String fileFormat;

	public TiffFileInfo(String filePath, String fileName, String fileFormat) {
		this.filePath = filePath;
		this.fileName = fileName;
		this.fileFormat = fileFormat;
	}

	/**
	 * @return Returns the filePath.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath
	 *            The filePath to set.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return Returns the fileFormat.
	 */
	public String getFileFormat() {
		return fileFormat;
	}

	/**
	 * @param fileFormat
	 *            The fileFormat to set.
	 */
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

}
