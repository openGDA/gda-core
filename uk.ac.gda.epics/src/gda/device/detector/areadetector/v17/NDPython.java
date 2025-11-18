/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.util.Map;

import gov.aps.jca.dbr.DBRType;

public interface NDPython extends GetPluginBaseAvailable {

	static final String Filename = "Filename";
	static final String Filename_RBV = "Filename_RBV";
	static final String Classname = "Classname";
	static final String Classname_RBV = "Classname_RBV";
	static final String ReadFile = "ReadFile";
	static final String ReadFile_RBV = "ReadFile_RBV";
	static final String Time_RBV = "Time_RBV";
	static final String PluginState_RBV = "PluginState_RBV";

	Map<String, String> getPythonParameters();

	void setPythonParameters(Map<String, String> pythonParameters);

	String getFilename_RBV() throws Exception;

	String getFilename() throws Exception;

	void setFilename(String filename) throws Exception;

	String getClassname_RBV() throws Exception;

	String getClassname() throws Exception;

	void setClassname(String classname) throws Exception;

	void readFile() throws Exception;

	double getRunTime() throws Exception;

	int getStatus() throws Exception;

	void putParam(String parameter, Object value) throws Exception;

	/**
	 * Returns the parameter as a String, regardless of the actual type
	 *
	 * @param parameter
	 * @return String value for parameter
	 * @throws Exception
	 */
	String readParam(String parameter) throws Exception;

	/**
	 * Returns the parameter as an array of the specified type
	 *
	 * @param parameter
	 *            Name of parameter, as set by {@link #setPythonParameters(Map)}
	 * @param type
	 *            Type of array to return
	 * @return value of the specified parameter
	 * @throws Exception
	 */
	Object readParam(String parameter, DBRType type) throws Exception;
}
