/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.addetector;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase;

/**
 * Class to hold data extracted from NDArray plugin in form to add to NXDetectorData
 */
public class ArrayData {

	public static int[] determineDataDimensions(NDArray ndArray) throws Exception {
		// only called if configured to readArrays (and hence ndArray is set)
		NDPluginBase pluginBase = ndArray.getPluginBase();
		int nDimensions = pluginBase.getNDimensions_RBV();
		int[] dimFromEpics = new int[3];
		dimFromEpics[0] = pluginBase.getArraySize2_RBV();
		dimFromEpics[1] = pluginBase.getArraySize1_RBV();
		dimFromEpics[2] = pluginBase.getArraySize0_RBV();

		int[] dims = java.util.Arrays.copyOfRange(dimFromEpics, 3 - nDimensions, 3);
		return dims;
	}

	public static NexusGroupData readArrayData(NDArray ndArray) throws Exception {
		int[] dims = determineDataDimensions(ndArray);

		if (dims.length == 0) {
			throw new Exception("Dimensions of data from ndArray are zero length");
		}

		int expectedNumPixels = dims[0];
		for (int i = 1; i < dims.length; i++) {
			expectedNumPixels = expectedNumPixels * dims[i];
		}
		NexusGroupData dataVals;
		// TODO do only once per scan
		short dataType = ndArray.getPluginBase().getDataType_RBV();
		switch (dataType) {
		case NDPluginBase.UInt8: {
			byte[] b = new byte[] {};
			b = ndArray.getByteArrayData(expectedNumPixels);
			if (expectedNumPixels > b.length)
				throw new DeviceException("Data size is not valid");
			{
				short cd[] = new short[expectedNumPixels];
				for (int i = 0; i < expectedNumPixels; i++) {
					cd[i] = (short) (b[i] & 0xff);
				}
				dataVals = new NexusGroupData(dims, cd);
			}
		}
			break;
		case NDPluginBase.Int8: {
			byte[] b = ndArray.getByteArrayData(expectedNumPixels);
			if (expectedNumPixels > b.length)
				throw new DeviceException("Data size is not valid");
			dataVals = new NexusGroupData(dims, b);
			break;
		}
		case NDPluginBase.Int16: {
			short[] s = ndArray.getShortArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
		}
			break;
		case NDPluginBase.UInt16: {
			short[] s = ndArray.getShortArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			int cd[] = new int[expectedNumPixels];
			for (int i = 0; i < expectedNumPixels; i++) {
				cd[i] = (s[i] & 0xffff);
			}
			dataVals = new NexusGroupData(dims, cd);
		}
			break;
		case NDPluginBase.UInt32: // TODO should convert to INT64 if any numbers are negative
		case NDPluginBase.Int32: {
			int[] s = ndArray.getIntArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
		}
			break;
		case NDPluginBase.Float32: {
			float[] s = ndArray.getFloatArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
		}
			break;
		case NDPluginBase.Float64: {
			double[] s = ndArray.getDoubleArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:" + expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
		}
			break;
		default:
			throw new DeviceException("Type of data is not understood :" + dataType);
		}
		return dataVals;
	}
}
