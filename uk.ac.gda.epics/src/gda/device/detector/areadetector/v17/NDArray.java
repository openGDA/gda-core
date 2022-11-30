/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.device.detector.areadetector.v17.NDPluginBase.DataType;

/**
 * Interface that corresponds to the 'arr' plugin on the edm screen.
 */
public interface NDArray extends GetPluginBaseAvailable {

	public static final String ARRAY_DATA = "ArrayData";
	public static final String DATA_TYPE_RBV = "DataType_RBV";

	short[] getShortArrayData(int numberOfElements) throws Exception;

	byte[] getByteArrayData(int numberOfElements) throws Exception;

	int[] getIntArrayData(int numberOfElements) throws Exception;

	float[] getFloatArrayData(int numberOfElements) throws Exception;

	float[] getFloatArrayData() throws Exception;

	double[] getDoubleArrayData(int numberOfElements) throws Exception;

	double[] getDoubleArrayData() throws Exception;

	void reset() throws Exception;

	byte[] getByteArrayData() throws Exception;

	Object getImageData(int expectedNumPixels) throws Exception;

	/**
	 * Get the data type of the last frame which reached the array plugin.
	 *
	 * @return the data type
	 * @throws Exception
	 */
	DataType getDataType() throws Exception;
}
