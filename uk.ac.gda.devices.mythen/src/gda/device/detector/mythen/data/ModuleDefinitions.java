/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.data;

import java.util.List;

/**
 * Holds a list of the module names in order. The names should be in the same format as the calibration files.
 * <p>
 * For use by the classes which merge calibration data before it is used by the DataConvertor class
 * <p>
 * Information about where and which calibration files to use is provided by the mode and calibrationFolder attributes.
 */
public class ModuleDefinitions {

	private String mode = "standard";
	private String calibrationFolder;
	private String flatFilePrefix = "StdFlatCu";
	private List<String> modules;

	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getCalibrationFolder() {
		return calibrationFolder;
	}

	public void setCalibrationFolder(String calibrationFolder) {
		this.calibrationFolder = calibrationFolder;
	}

	public void setFlatFilePrefix(String flatFilePrefix) {
		this.flatFilePrefix = flatFilePrefix;
	}

	public String getFlatFilePrefix() {
		return flatFilePrefix;
	}
	
	public String getCalibrationFilePrefix(){
		
		if (mode.equals("standard")){
			return "Std";
		} else if (mode.equals("fast")){
			return "Fast";
		} else {
			return "Hg"; // highgain
		}
	}

}
