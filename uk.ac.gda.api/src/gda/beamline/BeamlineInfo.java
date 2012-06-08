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

import gda.factory.Findable;
import gda.observable.IObservable;

import java.io.Serializable;

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
	public abstract String getDataDir();

	/**
	 * sets or changes the current data directory to the value specified. It provides a way to change gda output data
	 * directory after GDA has started.
	 * 
	 * @param dataDir
	 */
	public abstract void setDataDir(String dataDir);

	/**
	 * gets the prefix for the data file name.
	 * 
	 * @return file prefix
	 */
	public abstract String getFilePrefix();

	/**
	 * sets or changes the prefix for the data file name to the specified value.
	 * 
	 * @param filePrefix
	 */
	public abstract void setFilePrefix(String filePrefix);

	/**
	 * gets the suffix for the data file name
	 * 
	 * @return file suffix
	 */
	public abstract String getFileSuffix();

	/**
	 * sets or changes the suffix for data file name to the specified value.
	 * 
	 * @param fileSuffix
	 */
	public abstract void setFileSuffix(String fileSuffix);

	/**
	 * gets the extension name for the data file name
	 * 
	 * @return file extension
	 */
	public abstract String getFileExtension();

	/**
	 * sets or change the extension name to the specified value for data file name.
	 * 
	 * @param fileExtension
	 */
	public abstract void setFileExtension(String fileExtension);

	/**
	 * returns current file number
	 * 
	 * @return the current file number
	 */
	public abstract long getFileNumber();

	/**
	 * returns the next file number. This causes the NumTracker to increment the file number for the system.
	 * 
	 * @return the next file number
	 */
	public abstract long getNextFileNumber();

	/**
	 * gets the extension name for the data file name
	 * 
	 * @return file extension
	 */
	public abstract String getProjectName();

	/**
	 * sets or change the extension name to the specified value for data file name.
	 * 
	 * @param project
	 */
	public abstract void setProjectName(String project);

	/**
	 * gets the extension name for the data file name
	 * 
	 * @return file extension
	 */
	public abstract String getExperimentName();

	/**
	 * sets or change the extension name to the specified value for data file name.
	 * 
	 * @param experiment
	 */
	public abstract void setExperimentName(String experiment);

	/**
	 * gets the header string for this beamline
	 * 
	 * @return header
	 */
	public abstract String getHeader();

	/**
	 * sets the header string for this beamline
	 * 
	 * @param header
	 */
	public abstract void setHeader(String header);

	/**
	 * gets the sub-header string for the beamline
	 * 
	 * @return sub-header
	 */
	public abstract String getSubHeader();

	/**
	 * sets the sub-header string for the beamline
	 * 
	 * @param subHeader
	 */
	public abstract void setSubHeader(String subHeader);

}