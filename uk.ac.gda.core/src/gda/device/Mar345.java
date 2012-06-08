/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

/**
 * Interface to control MAR 345.
 */
public interface Mar345 extends Device, Detector {
	/**
	 * Append keyowrds to the current keyword buffer
	 * 
	 * @param keywords
	 *            the keywords to append
	 */
	public void appendToKeywordList(String keywords);

	/**
	 * Clear current keyword buffer
	 */
	public void clearKeywordList();

	/**
	 * Send the keyword list to the mar controller - ee mar documentation
	 */
	public void sendKeywordList();

	/**
	 * Send a string to the mar controller
	 * 
	 * @param keywords
	 *            the string to send
	 */
	public void sendKeywords(String keywords);

	/**
	 * Set the format for the mar files - see the mar documentation
	 * 
	 * @param format
	 *            the format
	 */
	public void setFormat(String format);

	/**
	 * Set the scan mode, values from 0 - 7 see the mar documentation
	 * 
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(int mode);

	/**
	 * Set the directory into which the mar files will be stored
	 * 
	 * @param directory
	 *            the directory name
	 */
	public void setDirectory(String directory);

	/**
	 * Set the root name of the mar files
	 * 
	 * @param rootName
	 *            the root name
	 */
	public void setRootName(String rootName);

	/**
	 * Gets the current format
	 * 
	 * @return the format
	 */
	public String getFormat();

	/**
	 * Gets the current mode.
	 * 
	 * @return the mode
	 */
	public int getMode();

	/**
	 * Gets the current data directory.
	 * 
	 * @return the directory name
	 */
	public String getDirectory();

	/**
	 * Gets the current root name.
	 * 
	 * @return the root name
	 */
	public String getRootName();

	/**
	 * Erases.
	 */
	public void erase();

	/**
	 * Scans.
	 */
	public void scan();

}