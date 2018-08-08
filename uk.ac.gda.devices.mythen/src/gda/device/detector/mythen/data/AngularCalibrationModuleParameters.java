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

/**
 * Holds a Mythen module's angular calibration parameters.
 */
public class AngularCalibrationModuleParameters {

	private int module;
	
	private double center;
	
	private double conversion;
	
	private double offset;
	
	/**
	 * Creates an angular calibration parameter object using the specified values.
	 * 
	 * @param module the module number
	 * @param center the module's centre
	 * @param conversion the module's conversion factor
	 * @param offset the module's offset
	 */
	public AngularCalibrationModuleParameters(int module, double center, double conversion, double offset) {
		this.module = module;
		this.center = center;
		this.conversion = conversion;
		this.offset = offset;
	}

	/**
	 * Returns the module number.
	 * 
	 * @return the module number
	 */
	public int getModule() {
		return module;
	}

	/**
	 * Returns the module's centre.
	 * 
	 * @return the centre
	 */
	public double getCenter() {
		return center;
	}

	/**
	 * Returns the module's conversion factor.
	 * 
	 * @return the conversion factor
	 */
	public double getConversion() {
		return conversion;
	}

	/**
	 * Returns the module's offset.
	 * 
	 * @return the offset
	 */
	public double getOffset() {
		return offset;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(module=" + module + ", center=" + center + ", conversion=" + conversion + ", offset=" + offset + ")";
	}

}
