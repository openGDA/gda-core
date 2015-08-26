/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector;

/**
 * This class allows non area detector NXFilewriter plugins to implement the same interface for
 * setting file templates as is used by gda.device.detector.addetector.filewriter.FileWriterBase.
 */
public interface NXFileWriterWithTemplate {
	/**
	 * File template to use.
	 *
	 * AreaDetector's NDFilePlugin will always apply arguments to the template in the order filepath, filename, filenumber,
	 * so classes which implement this interface should do likewise. Unused trailing arguments should be ignored. For instance
	 * "%s%s" should concatenate filePathTemplate and fileNameTemplate) while "%s%s_03%d" should concatenate filePathTemplate,
	 * fileNameTemplate and fileNumber.
	 *
	 * @param fileTemplate
	 */
	public void setFileTemplate(String fileTemplate);

	/**
	 * The file path to use.
	 *
	 * If present, the string "$scan$" should be replaced with the currently running scan number and "$datadir$" should be replaced
	 * with the current scan file's directory.
	 *
	 * @param filePathTemplate
	 */
	public void setFilePathTemplate(String filePathTemplate);

	/**
	 * The file name to use.
	 *
	 * If present, the string "$scan$" should be replaced with the currently running scan number and "$datadir$" should be replaced
	 * with the current scan file's directory.
	 *
	 * @param fileNameTemplate
	 */
	public void setFileNameTemplate(String fileNameTemplate);
}
