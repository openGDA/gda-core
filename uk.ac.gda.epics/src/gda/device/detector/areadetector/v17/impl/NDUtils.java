/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.NDPluginBase;


public class NDUtils {

	/**
	 *
	 * @return Max pixel value given the dataType of the camera e.g. for UINT8 returns 256
	 */
	static public double getImageMaxFromDataType(gda.device.detector.areadetector.v17.NDPluginBase.DataType dataType) throws Exception{
		switch (dataType.ordinal()) {
		case NDPluginBase.Int8:
			return 127;
		case NDPluginBase.UInt8:
			return 255;
		case NDPluginBase.Int16:
			return 65536/2-1;
		case NDPluginBase.UInt16:
			return 65535;
		case NDPluginBase.Int32:
			return Integer.MAX_VALUE;
		case NDPluginBase.UInt32:
			return Integer.MAX_VALUE*2-1;
		case NDPluginBase.Float32:
			return Float.MAX_VALUE;
		case NDPluginBase.Float64:
			return Double.MAX_VALUE;
		default:
			throw new Exception("Invalid dataType");
		}
	}

}
