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

package gda.device.detector.areadetector.v17;


/**
 *
 */
public interface NDFileNexus {

	NDFile getFile();

	/**
	 *
	 */
	String getTemplateFilePath() throws Exception;

	/**
	 *
	 */
	void setTemplateFilePath(String templatefilepath) throws Exception;

	/**
	 *
	 */
	String getTemplateFilePath_RBV() throws Exception;

	/**
	 *
	 */
	String getTemplateFileName() throws Exception;

	/**
	 *
	 */
	void setTemplateFileName(String templatefilename) throws Exception;

	/**
	 *
	 */
	String getTemplateFileName_RBV() throws Exception;

	/**
	 *
	 */
	short getFileTemplateValid() throws Exception;

	/**
	 *
	 */
	void setFileTemplateValid(int filetemplatevalid) throws Exception;

	void reset() throws Exception;

}