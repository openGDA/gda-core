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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.springframework.util.StringUtils;

/**
 * Reads a simpler angular calibration file than AngularCalibrationParametersFile. 
 * <p>
 * The file simply contains two columns, modules and angle. Format: integer-tab-double.
 */
public class SimpleAngularCalibrationParameters implements AngularCalibrationParameters {

	protected HashMap<Integer,Double> parameters;
	
	/**
	 * Reads the angular calibration parameters in the specified file.
	 * 
	 * @param file the angular calibration file
	 */
	public SimpleAngularCalibrationParameters(File file) {
		parameters = new HashMap<Integer,Double>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] bits = line.trim().split("\t");
				int module = Integer.parseInt(bits[0]);
				double centre = Double.parseDouble(bits[1]);
				parameters.put(module, centre);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load angular calibration parameters from " + StringUtils.quote(file.getAbsolutePath()), e);
		}
	}

	
	@Override
	public AngularCalibrationModuleParameters getParametersForModule(int module) {
		Double centre = parameters.get(new Integer(module));
		return new AngularCalibrationModuleParameters(module,centre,0.0,0.0);
	}

}
