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

package gda.beamline;

import java.io.Serializable;

import gda.factory.Findable;
import gda.observable.IObservable;

/**
 * access methods for users to enquire current values about the beamline which are used in data collection. This object
 * is accessible over CORBA so that GUI objects have access to the same data. This interface extends IObservable so that
 * GUI objects can be informed of any changes made.
 */
public interface BeamlineInfo extends Findable, IObservable, Serializable {

	/**
	 * gets the current data directory
	 *
	 * @return path to current data directory
	 */
	String getDataDir();

	/**
	 * sets or changes the current data directory to the value specified. It provides a way to change gda output data
	 * directory after GDA has started.
	 *
	 * @param dataDir
	 */
	void setDataDir(String dataDir);

	/**
	 * gets the prefix for the data file name.
	 *
	 * @return file prefix
	 */
	String getFilePrefix();

	/**
	 * sets or changes the prefix for the data file name to the specified value.
	 *
	 * @param filePrefix
	 */
	void setFilePrefix(String filePrefix);

	/**
	 * gets the suffix for the data file name
	 *
	 * @return file suffix
	 */
	String getFileSuffix();

	/**
	 * sets or changes the suffix for data file name to the specified value.
	 *
	 * @param fileSuffix
	 */
	void setFileSuffix(String fileSuffix);

	/**
	 * gets the extension name for the data file name
	 *
	 * @return file extension
	 */
	String getFileExtension();

	/**
	 * sets or change the extension name to the specified value for data file name.
	 *
	 * @param fileExtension
	 */
	void setFileExtension(String fileExtension);

	/**
	 * returns current file number
	 *
	 * @return the current file number
	 */
	int getFileNumber();

	/**
	 * returns the next file number. This causes the NumTracker to increment the file number for the system.
	 *
	 * @return the next file number
	 */
	int getNextFileNumber();

	/**
	 * gets the extension name for the data file name
	 *
	 * @return file extension
	 */
	String getProjectName();

	/**
	 * sets or change the extension name to the specified value for data file name.
	 *
	 * @param project
	 */
	void setProjectName(String project);

	/**
	 * gets the extension name for the data file name
	 *
	 * @return file extension
	 */
	String getExperimentName();

	/**
	 * sets or change the extension name to the specified value for data file name.
	 *
	 * @param experiment
	 */
	void setExperimentName(String experiment);

	/**
	 * gets the header string for this beamline
	 *
	 * @return header
	 */
	String getHeader();

	/**
	 * sets the header string for this beamline
	 *
	 * @param header
	 */
	void setHeader(String header);

	/**
	 * gets the sub-header string for the beamline
	 *
	 * @return sub-header
	 */
	String getSubHeader();

	/**
	 * sets the sub-header string for the beamline
	 *
	 * @param subHeader
	 */
	void setSubHeader(String subHeader);
}