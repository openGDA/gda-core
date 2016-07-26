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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase.DataType;

/*
 * class that returns data from a file rather than EPICS.
 */
public class DummyNDArray extends NDBaseImpl implements NDArray {


	public DummyNDArray()  {
		super();
	}

	private float[] floatArrayData;
	private int[] intArrayData;
	private short[] shortArrayData;
	private byte[] byteArrayData;
	private double[] doubleArrayData;

	@Override
	public byte[] getByteArrayData(int numberOfElements) throws Exception {

		return byteArrayData;
	}

	@Override
	public float[] getFloatArrayData() throws Exception {

		return floatArrayData;
	}

	@Override
	public void reset() throws Exception {
	}

	@Override
	public byte[] getByteArrayData() throws Exception {

/*		int width = pluginBase.getArraySize0_RBV();
		int height = pluginBase.getArraySize1_RBV();*/
		return byteArrayData;

	}

	@Override
	public short[] getShortArrayData(int numberOfElements) throws Exception {
		return shortArrayData;
	}

	@Override
	public int[] getIntArrayData(int numberOfElements) throws Exception {
		return intArrayData;
	}

	@Override
	public float[] getFloatArrayData(int numberOfElements) throws Exception {
		return floatArrayData;
	}

	@Override
	public Object getImageData(int expectedNumPixels) throws Exception {
		return null;
	}

	@Override
	public double[] getDoubleArrayData(int numberOfElements) throws Exception {
		return doubleArrayData;
	}

	@Override
	public double[] getDoubleArrayData() throws Exception {
		return doubleArrayData;
	}

	@Override
	public DataType getDataType() throws Exception {
		// Dummy returns float 64
		return DataType.FLOAT64;
	}

}

