/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Holds angular calibration parameters for a group of Mythen modules.
 */
public class AngularCalibrationParametersFile implements AngularCalibrationParameters {
	
	private List<AngularCalibrationModuleParameters> parameters;
	
	/**
	 * Reads the angular calibration parameters in the specified file.
	 * 
	 * @param file the angular calibration file
	 */
	public AngularCalibrationParametersFile(File file) {
		parameters = new Vector<AngularCalibrationModuleParameters>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] bits = line.trim().split(" ");
				int module = Integer.parseInt(bits[1]);
				double center = Double.parseDouble(bits[3]);
				double conversion = Double.parseDouble(bits[7]);
				double offset = Double.parseDouble(bits[11]);
				AngularCalibrationModuleParameters params = new AngularCalibrationModuleParameters(module, center, conversion, offset);
				parameters.add(params);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load angular calibration parameters from " + file, e);
		}
	}
	
	/**
	 * Creates an {@link AngularCalibrationParametersFile} object using the given
	 * module parameters.
	 * 
	 * @param parameters the module parameters
	 */
	public AngularCalibrationParametersFile(List<AngularCalibrationModuleParameters> parameters) {
		this.parameters = Collections.unmodifiableList(parameters);
	}
	
	/**
	 * Returns angular calibration parameters for the specified module.
	 * 
	 * @param module the module number
	 * 
	 * @return the module's angular calibration parameters
	 */
	@Override
	public AngularCalibrationModuleParameters getParametersForModule(int module) {
		return parameters.get(module);
	}

}
