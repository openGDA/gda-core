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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDPluginBase.DataType;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;

/*
 * class that returns data from a file rather than EPICS.
 */
public class NDArraySimulator extends NDBaseImpl implements NDArray {

	GaussianController gC;

	public NDArraySimulator() throws FactoryException {
		super();
		gC = new GaussianController();
		gC.configure();
	}

	@Override
	public byte[] getByteArrayData(int numberOfElements) throws Exception {

		return getByteArrayData();
	}

	@Override
	public float[] getFloatArrayData() throws Exception {

		return null;
	}

	@Override
	public void reset() throws Exception {
	}

	@Override
	public byte[] getByteArrayData() throws Exception {

		int width = getPluginBase().getArraySize0_RBV();
		int height = getPluginBase().getArraySize1_RBV();

		short dataType = getPluginBase().getDataType_RBV();
		double maxHeightForType = getMaxHeightForType(dataType);
		Gaussian g = new Gaussian(gC.heightFrac * maxHeightForType, gC.centreXFrac * width, gC.centreYFrac * height,
				gC.widthXFrac * width, gC.widthYFrac * height);

		byte[] bytes;
		switch (dataType) {
		case NDPluginBase.UInt8: {
			bytes = new byte[width * height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					bytes[j * width + i] = (byte) (((short) Math.min(maxHeightForType, g.getVal(i, j))) & 0xff);
				}
			}
		}
			break;
		case NDPluginBase.Int8: {
			bytes = new byte[width * height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					bytes[j * width + i] = (byte) Math.min(maxHeightForType, g.getVal(i, j));
				}
			}
		}
			break;
		case NDPluginBase.UInt16: {
			bytes = new byte[width * height * 2];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int k = (j * width + i) * 2;
					int v = (int) Math.min(maxHeightForType, g.getVal(i, j));
					bytes[k + 1] = (byte) (v & 255);
					bytes[k] = (byte) ((v >> 8) & 255);
				}
			}
		}
			break;
		case NDPluginBase.Int16: {
			bytes = new byte[width * height * 2];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int k = (j * width + i) * 2;
					short v = (short) Math.min(maxHeightForType, g.getVal(i, j));
					bytes[k + 1] = (byte) (v & 255);
					bytes[k] = (byte) ((v >> 8) & 255);
				}
			}
		}
			break;
		case NDPluginBase.UInt32: {
			bytes = new byte[width * height * 4];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int k = (i * height + j) * 4;
					long v = (long) Math.min(maxHeightForType, g.getVal(i, j));
					bytes[k] = (byte) ((v >> 24) & 0xFF);
					bytes[k + 1] = (byte) ((v >> 16) & 0xFF);
					bytes[k + 2] = (byte) ((v >> 8) & 0xFF);
					bytes[k + 3] = (byte) ((v) & 0xFF);
				}
			}
		}
			break;
		case NDPluginBase.Int32: {
			bytes = new byte[width * height * 4];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int k = (i * height + j) * 4;
					int v = (int) Math.min(maxHeightForType, g.getVal(i, j));
					bytes[k] = (byte) ((v >> 24) & 0xFF);
					bytes[k + 1] = (byte) ((v >> 16) & 0xFF);
					bytes[k + 2] = (byte) ((v >> 8) & 0xFF);
					bytes[k + 3] = (byte) v;
				}
			}
		}
			break;
		default:
			throw new Exception("Does not support data type:" + dataType);
		}
		return bytes;
	}

	//currently we only return signed values so return max val that can fit into these
	private double getMaxHeightForType(short dataType) throws Exception {
		double maxHeightForType;
		switch (dataType) {
		case NDPluginBase.UInt8:
			maxHeightForType = getMaxHeightForType(NDPluginBase.Int8);
			break;
		case NDPluginBase.Int8:
			maxHeightForType = Byte.MAX_VALUE* (1.0 - Math.random()*0.1);//10% randomness;
			break;
		case NDPluginBase.UInt16:
			maxHeightForType = getMaxHeightForType(NDPluginBase.Int16);
			break;
		case NDPluginBase.Int16:
			maxHeightForType = Short.MAX_VALUE* (1.0 - Math.random()*0.1);//10% randomness
			break;
		case NDPluginBase.UInt32:
			maxHeightForType = getMaxHeightForType(NDPluginBase.Int32);
			break;
		case NDPluginBase.Int32:
			maxHeightForType = Integer.MAX_VALUE * (1.0 - Math.random()*0.1);//10% randomness;;
			break;
		default:
			throw new Exception("Does not support data type:" + dataType);
		}
		return maxHeightForType;
	}

	public Scannable getGaussianController() {
		return gC;
	}

	@Override
	public short[] getShortArrayData(int numberOfElements) throws Exception {
		int width = getPluginBase().getArraySize0_RBV();
		int height = getPluginBase().getArraySize1_RBV();

		double maxHeightForType = getMaxHeightForType(getPluginBase().getDataType_RBV());
		Gaussian g = new Gaussian(gC.heightFrac * maxHeightForType, gC.centreXFrac * width, gC.centreYFrac * height,
				gC.widthXFrac * width, gC.widthYFrac * height);

		short[] bytes = new short[width * height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				bytes[j * width + i] = (short) Math.min(maxHeightForType, g.getVal(i, j));
			}
		}
		return bytes;
	}

	@Override
	public int[] getIntArrayData(int numberOfElements) throws Exception {
		double maxHeightForType = getMaxHeightForType(getPluginBase().getDataType_RBV());
		int width = getPluginBase().getArraySize0_RBV();
		int height = getPluginBase().getArraySize1_RBV();
		Gaussian g = new Gaussian(gC.heightFrac * maxHeightForType, gC.centreXFrac * width, gC.centreYFrac * height,
				gC.widthXFrac * width, gC.widthYFrac * height);

		int[] bytes = new int[width * height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double val = g.getVal(i, j);
				bytes[j * width + i] = (int)Math.min(maxHeightForType, val);
			}
		}
		return bytes;
	}

	@Override
	public float[] getFloatArrayData(int numberOfElements) throws Exception {
		double maxHeightForType = getMaxHeightForType(getPluginBase().getDataType_RBV());
		int width = getPluginBase().getArraySize0_RBV();
		int height = getPluginBase().getArraySize1_RBV();
		Gaussian g = new Gaussian(gC.heightFrac * maxHeightForType, gC.centreXFrac * width, gC.centreYFrac * height,
				gC.widthXFrac * width, gC.widthYFrac * height);

		float[] bytes = new float[width * height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				bytes[j * width + i] =  (float)Math.min(maxHeightForType, g.getVal(i, j));
			}
		}
		return bytes;
	}

	@Override
	public double[] getDoubleArrayData() throws Exception {
		double maxHeightForType = getMaxHeightForType(getPluginBase().getDataType_RBV());
		int width = getPluginBase().getArraySize0_RBV();
		int height = getPluginBase().getArraySize1_RBV();
		Gaussian g = new Gaussian(gC.heightFrac * maxHeightForType, gC.centreXFrac * width, gC.centreYFrac * height, gC.widthXFrac * width, gC.widthYFrac
				* height);

		double[] bytes = new double[width * height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				bytes[j * width + i] = g.getVal(i, j);
			}
		}
		return bytes;
	}

	@Override
	public double[] getDoubleArrayData(int numberOfElements) throws Exception {
		return getDoubleArrayData();
	}

	@Override
	public Object getImageData(int expectedNumPixels) throws Exception {
		return null;
	}

	@Override
	public DataType getDataType() throws Exception {
		return DataType.FLOAT64;
	}

}

class Gaussian {
	private double height;
	private double centreX;
	private double centreY;
	private double widthX;
	private double widthY;

	double getVal(double x, double y) {
		double arg = -1. * (Math.pow((x - centreX) / widthX, 2) + Math.pow((y - centreY) / widthY, 2));
		return height * Math.exp(arg);
	}

	public Gaussian(double height, double centreX, double centreY, double widthX, double widthY) {
		super();
		this.height = height;
		this.centreX = centreX;
		this.centreY = centreY;
		this.widthX = widthX;
		this.widthY = widthY;
	}

}

class GaussianController extends ScannableBase {

	NDArraySimulator ndArray;
	static final private String[] names = new String[] { "centreXFrac", "centreYFrac", "heightFrac", "widthXFrac",
			"widthYFrac" };

	double widthXFrac = 1; // max 1 = gaussian widthX = image_width
	double widthYFrac = 1; // max 1 = gaussian widthY = image_height
	double heightFrac = 0.9; // max 1 = gaussian height = max value for datatype
	double centreXFrac = 0.5; // max 1 = gaussian centreX = image_width
	double centreYFrac = 0.5; // max 1 = gaussian centreY = image_height

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		Double[] doubles = ScannableUtils.objectToArray(externalPosition);
		if (doubles.length != names.length) {
			throw new DeviceException("Position must contain 4 parts");
		}
		centreXFrac = doubles[0];
		centreYFrac = doubles[1];
		heightFrac = doubles[2];
		widthXFrac = doubles[3];
		widthYFrac = doubles[4];
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		setName("GaussianController");
		setInputNames(names);
	}

	@Override
	public Object getPosition() throws DeviceException {
		return new double[] { centreXFrac, centreYFrac, heightFrac, widthXFrac, widthYFrac };
	}

}