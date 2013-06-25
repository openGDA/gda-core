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
import gda.data.nexus.extractor.NexusGroupData;
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
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
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

	double[] energies;
	double[] angles;
	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		if (isEnabled()) {
			appenders.add(new NXDetectorDataArrayAppender(ndArray,firstReadoutInScan, getRegionName(), getAnalyser()));
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

	private static final Logger logger = LoggerFactory
			.getLogger(NXDetectorDataArrayAppender.class);
	private boolean firstReadoutInScan;
	private NDArray ndArray;
	private String regionName;
	private VGScientaAnalyser analyser;

	NXDetectorDataArrayAppender(NDArray ndArray, boolean firstReadoutInScan,String regionName, VGScientaAnalyser analyser) {
		this.ndArray = ndArray;
		this.firstReadoutInScan = firstReadoutInScan;
		this.regionName = regionName;
		this.analyser = analyser;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName)
			throws DeviceException {
		try {
			readoutArrayIntoNXDetectorData(data, ndArray, detectorName,regionName);
			if (firstReadoutInScan) {
				appendRegionParametersAndDataAxes(data, detectorName);
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	private void appendRegionParametersAndDataAxes(NXDetectorData data, String detectorName) throws Exception {
		short state = analyser.getAdBase().getDetectorState_RBV();
		switch (state) {
			case 6:
				throw new DeviceException("analyser in error state during readout");
			case 1:
				// The IOC can report acquiring for quite a while after being stopped
				logger.debug("analyser status is acquiring during readout although we think it has stopped");
				break;
			case 10:
				logger.warn("analyser in aborted state during readout");
				break;
			default:
				break;
		}
			int i = 1;
			String aname = "energies";
			String aunit = "eV";
			double[] axis = analyser.getEnergyAxis();

			data.addAxis(detectorName, aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit, false);

			i = 0;
			if ("Transmission".equals(analyser.getLensMode())) {
				aname = "location";
				aunit = "mm";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			axis = analyser.getAngleAxis();

			data.addAxis(detectorName, aname, new int[] { axis.length }, NexusFile.NX_FLOAT64, axis, i + 1, 1, aunit, false);
			
			data.addData(detectorName, "reagion_name", new NexusGroupData(regionName), null, null);

			data.addData(detectorName, "lens_mode", new NexusGroupData(analyser.getLensMode()), null, null);
			data.addData(detectorName, "acquisition_mode", new NexusGroupData(analyser.getAcquisitionMode()), null, null);
			data.addData(detectorName, "energy_mode", new NexusGroupData( analyser.getEnergysMode() ), null, null);
			data.addData(detectorName, "detector_mode", new NexusGroupData( analyser.getDetectorMode() ), null, null);
			
			data.addData(detectorName, "pass_energy", new int[] {1}, NexusFile.NX_INT32, new int[] { analyser.getPassEnergy()}, null, null);
			data.addData(detectorName, "low_energy", new int[] {1}, NexusFile.NX_FLOAT64, new double[] { analyser.getStartEnergy()}, "eV", null);
			data.addData(detectorName, "high_energy", new int[] {1}, NexusFile.NX_FLOAT64, new double[] { analyser.getEndEnergy()}, "eV", null);
			data.addData(detectorName, "fixed_energy", new int[] {1}, NexusFile.NX_FLOAT64, new double[] { analyser.getCentreEnergy()}, "eV", null);
			data.addData(detectorName, "excitation_energy", new int[] {1}, NexusFile.NX_FLOAT64, new double[] { analyser.getExcitationEnergy()}, "eV", null);
			data.addData(detectorName, "energy_step", new int[] {1}, NexusFile.NX_FLOAT64, new double[] { analyser.getEnergyStep()}, "eV", null);
			double stepTime = analyser.getStepTime();
			data.addData(detectorName, "step_time", new int[] {1}, NexusFile.NX_FLOAT64, new double[] { stepTime}, "s", null);
			data.addData(detectorName, "number_of_slices", new int[] {1}, NexusFile.NX_INT32, new int[] { analyser.getSlices() }, null, null);
			data.addData(detectorName, "number_of_iterations", new int[] {1}, NexusFile.NX_INT32, new int[] { analyser.getNumberIterations()}, null, null);
			int totalSteps = analyser.getTotalSteps().intValue();
			data.addData(detectorName, "total_steps", new int[] {1}, NexusFile.NX_INT32, new int[] { totalSteps }, null, null);
			data.addData(detectorName, "total_time", new int[] {1}, NexusFile.NX_FLOAT64, new double[] {totalSteps*stepTime }, null, null);

			int cameraMinX = analyser.getCameraMinX();
			data.addData(detectorName, "detector_x_from", new int[] {}, NexusFile.NX_INT32, new int[] { cameraMinX}, null, null);
			int cameraMinY = analyser.getCameraMinY();
			data.addData(detectorName, "detector_y_from", new int[] {}, NexusFile.NX_INT32, new int[] { cameraMinY}, null, null);
			data.addData(detectorName, "detector_x_to", new int[] {}, NexusFile.NX_INT32, new int[] { analyser.getCameraSizeX()-cameraMinX}, null, null);
			data.addData(detectorName, "detector_y_to", new int[] {}, NexusFile.NX_INT32, new int[] { analyser.getCameraSizeY()-cameraMinY}, null, null);
//
//			data.addData(detectorName, "region_size", new int[] {2}, NexusFile.NX_INT32, new int[] { analyser.getAdBase().getSizeX_RBV(), analyser.getAdBase().getSizeY_RBV() }, null, null);
		}
	
	private void readoutArrayIntoNXDetectorData(NXDetectorData data,
			NDArray ndArray, String detectorName, String regionName)
			throws Exception, DeviceException {
		int[] dims = determineDataDimensions(ndArray);

		if (dims.length == 0) {
			logger.warn("Dimensions of data from " + detectorName
					+ " are zero length");
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
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			dataVals = s;
			nexusType = NexusFile.NX_INT16;
		}
			break;
		case NDPluginBase.UInt16: {
			short[] s = ndArray.getShortArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			int cd[] = new int[expectedNumPixels];
			for (int i = 0; i < expectedNumPixels; i++) {
				cd[i] = (s[i] & 0xffff);
			}
			dataVals = cd;
			nexusType = NexusFile.NX_INT32;
		}
			break;
		case NDPluginBase.UInt32: // TODO should convert to INT64 if any numbers
									// are negative
		case NDPluginBase.Int32: {
			int[] s = ndArray.getIntArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			dataVals = s;
			nexusType = NexusFile.NX_INT32;
		}
			break;
		case NDPluginBase.Float32:
		case NDPluginBase.Float64: {
			float[] s = ndArray.getFloatArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			dataVals = s;
			nexusType = NexusFile.NX_FLOAT32;
		}
			break;
		default:
			throw new DeviceException("Type of data is not understood :"
					+ dataType);
		}

		data.addData(detectorName, regionName + "_image", dims, nexusType,	dataVals, "counts", 1);
	}

}