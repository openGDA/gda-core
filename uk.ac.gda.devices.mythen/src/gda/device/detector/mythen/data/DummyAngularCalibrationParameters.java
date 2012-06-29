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
import java.util.Vector;

/**
 * Implementation of {@link AngularCalibrationParameters} that returns dummy values.
 */
public class DummyAngularCalibrationParameters implements AngularCalibrationParameters {
	
	private int numModules;
	
	private List<AngularCalibrationModuleParameters> parameters;
	
	/**
	 * Creates parameters for the specified number of modules.
	 */
	public DummyAngularCalibrationParameters(int numModules) {
		this.numModules = numModules;
		parameters = new Vector<AngularCalibrationModuleParameters>();
		for (int m=0; m<numModules; m++) {
			double center = 640; // each module has 1280 channels, so centre is channel 640
			double conversion = 6.5559e-5; // arbitrary value (taken from I11's file)
			double offset = 5 * m; // each module covers approx. 5°
			AngularCalibrationModuleParameters params = new AngularCalibrationModuleParameters(m, center, conversion, offset);
			parameters.add(params);
		}
	}

	@Override
	public AngularCalibrationModuleParameters getParametersForModule(int module) {
		if (module < 0 || module >= numModules) {
			throw new IllegalArgumentException("Invalid module number: " + module);
		}
		return parameters.get(module);
	}

}
