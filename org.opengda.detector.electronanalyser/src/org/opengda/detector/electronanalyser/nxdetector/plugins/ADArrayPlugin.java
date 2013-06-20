/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.nxdetector.plugins;

import static gda.device.detector.addetector.ADDetector.determineDataDimensions;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ScanInformation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.nexusformat.NexusFile;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADArrayPlugin implements NXPlugin {
	
	final private NDArray ndArray;

	private boolean enabled = true;

	private boolean firstReadoutInScan = true;
	private VGScientaAnalyser analyser;
	private String regionName;

	public ADArrayPlugin(NDArray ndArray) {
		this.ndArray = ndArray;
	}

	@Override
	public String getName() {
		return "array";
	}

	@Override
	public boolean willRequireCallbacks() {
		return isEnabled();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (isEnabled()) {
			ndArray.getPluginBase().enableCallbacks();
			ndArray.getPluginBase().setBlockingCallbacks((short) 1);
		} else {
			ndArray.getPluginBase().disableCallbacks();
			ndArray.getPluginBase().setBlockingCallbacks((short) 0);
		}
		firstReadoutInScan = true;
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}
	
	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		int xsize=0;
		try {
			xsize = ndArray.getPluginBase().getArraySize0_RBV();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int ysize=0;
		try {
			ysize = ndArray.getPluginBase().getArraySize1_RBV();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		if (isEnabled()) {
			appenders.add(new NXDetectorDataArrayAppender(ndArray, firstReadoutInScan,getRegionName(), xsize, ysize ));
		} else {
			appenders.add(new NXDetectorDataNullAppender());
		}
		firstReadoutInScan = false;
		// disabled
		return appenders;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public NDArray getNdArray() {
		return ndArray;
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
}
class NXDetectorDataArrayAppender implements NXDetectorDataAppender {

	private static final Logger logger=LoggerFactory.getLogger(NXDetectorDataArrayAppender.class);
	private boolean firstReadoutInScan;
	private NDArray ndArray;
	private String regionName;
	private int xsize;
	private int ysize;

	NXDetectorDataArrayAppender(NDArray ndArray, boolean firstReadoutInScan, String regionName, int xsize, int ysize) {
		this.ndArray = ndArray;
		this.firstReadoutInScan = firstReadoutInScan;
		this.regionName=regionName;
		this.xsize=xsize;
		this.ysize=ysize;
	}
	
	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException{
		try {
			if (regionName != null) {
				readoutArrayIntoNXDetectorData(data, ndArray, detectorName, regionName);
			} else {
				readoutArrayIntoNXDetectorData(data, ndArray, detectorName, xsize+"x"+ysize);
			}
			if (firstReadoutInScan) {
				// TODO add sensible axes
//				data.addAxis(detectorName+"_"+regionName, "energies", dimensions, type, axisValues, axisValue, primaryValue, units, isPointDependent)
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	private void readoutArrayIntoNXDetectorData(NXDetectorData data, NDArray ndArray, String detectorName, String regionName)
			throws Exception, DeviceException {
		int[] dims = determineDataDimensions(ndArray);

		if (dims.length == 0) {
			logger.warn("Dimensions of data from " + detectorName + " are zero length");
			return;
		}

		int expectedNumPixels = dims[0];
		for (int i = 1; i < dims.length; i++) {
			expectedNumPixels = expectedNumPixels * dims[i];
		}
		Serializable dataVals;
		// TODO do only once per scan
		short dataType = ndArray.getPluginBase().getDataType_RBV();
		int nexusType;
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
				dataVals = cd;
				nexusType = NexusFile.NX_INT16;
			}
		}
			break;
		case NDPluginBase.Int8: {
			byte[] b = ndArray.getByteArrayData(expectedNumPixels);
			if (expectedNumPixels > b.length)
				throw new DeviceException("Data size is not valid");
			dataVals = b;
			nexusType = NexusFile.NX_INT8;
			break;
		}
		case NDPluginBase.Int16: {
			short[] s = ndArray.getShortArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			dataVals = s;
			nexusType = NexusFile.NX_INT16;
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
			dataVals = cd;
			nexusType = NexusFile.NX_INT32;
		}
			break;
		case NDPluginBase.UInt32: // TODO should convert to INT64 if any numbers are negative
		case NDPluginBase.Int32: {
			int[] s = ndArray.getIntArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			dataVals = s;
			nexusType = NexusFile.NX_INT32;
		}
			break;
		case NDPluginBase.Float32:
		case NDPluginBase.Float64: {
			float[] s = ndArray.getFloatArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			dataVals = s;
			nexusType = NexusFile.NX_FLOAT32;
		}
			break;
		default:
			throw new DeviceException("Type of data is not understood :" + dataType);
		}

		data.addData(detectorName, regionName+"_image", dims, nexusType, dataVals, "counts", 1);
	}

}