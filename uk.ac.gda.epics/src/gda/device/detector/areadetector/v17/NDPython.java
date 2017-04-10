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

public interface NDPython extends GetPluginBaseAvailable {

	static final String Filename = "Filename";
	static final String Filename_RBV = "Filename_RBV";
	static final String Classname = "Classname";
	static final String Classname_RBV = "Classname_RBV";
	static final String ReadFile = "ReadFile";
	static final String ReadFile_RBV = "ReadFile_RBV";
	static final String Time_RBV = "Time_RBV";
	static final String PluginState_RBV = "PluginState_RBV";

	public Map<String, String> getPythonParameters();

	public void setPythonParameters(Map<String, String> pythonParameters);

	public String getFilename_RBV() throws Exception;

	public String getFilename() throws Exception;

	public void setFilename(String filename) throws Exception;

	public String getClassname_RBV() throws Exception;

	public String getClassname() throws Exception;

	public void setClassname(String classname) throws Exception;

	public void readFile() throws Exception;

	public double getRunTime() throws Exception;

	public int getStatus() throws Exception;

	public void putParam(String parameter, Object value) throws Exception;

	/**
	 * Returns the parameter as a String, regardless of the actual type
	 *
	 * @param paramter
	 * @return String value for parameter
	 * @throws Exception
	 */
	public String readParam(String paramter) throws Exception;
}
