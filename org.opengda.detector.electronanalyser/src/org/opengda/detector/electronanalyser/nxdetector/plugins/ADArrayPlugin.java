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

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ScanInformation;

public class ADArrayPlugin implements NXPlugin {

	final private NDArray ndArray;

	private boolean enabled = true;

	private boolean firstReadoutInScan = true;
	private VGScientaAnalyser analyser;
	private String regionName;

	private String energyMode;

	public String getEnergyMode() {
		return energyMode;
	}

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
			ndArray.getPluginBase().setNDArrayPort(getAnalyser().getAdBase().getPortName_RBV());
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
	public Vector<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		if (isEnabled()) {
			appenders.add(new NXDetectorDataAnalyserArrayAppender(ndArray,firstReadoutInScan, getRegionName(), getAnalyser(), getEnergyMode()));
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

	public void setEnergyMode(String literal) {
		this.energyMode=literal;
	}
}

class NXDetectorDataAnalyserArrayAppender implements NXDetectorDataAppender {

	private static final Logger logger = LoggerFactory
			.getLogger(NXDetectorDataAnalyserArrayAppender.class);
	private boolean firstReadoutInScan;
	private NDArray ndArray;
	private String regionName;
	private VGScientaAnalyser analyser;
	private String energyMode = "Kinetic";

	NXDetectorDataAnalyserArrayAppender(NDArray ndArray, boolean firstReadoutInScan,String regionName, VGScientaAnalyser analyser, String energyMode) {
		this.ndArray = ndArray;
		this.firstReadoutInScan = firstReadoutInScan;
		this.regionName = regionName;
		this.analyser = analyser;
		this.energyMode =energyMode;
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
				//throw new DeviceException("analyser in error state during readout");
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
			if (energyMode.equalsIgnoreCase("Binding")) {
				double excitationEnergy = analyser.getExcitationEnergy();
				for (int j=0; j<axis.length; j++) {
					axis[j]=excitationEnergy-axis[j];
				}
			}

			data.addAxis(detectorName, aname, new NexusGroupData(axis), i + 1, 1, aunit, false);

			i = 0;
			if ("Transmission".equals(analyser.getLensMode())) {
				aname = "location";
				aunit = "pixel";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			axis = analyser.getAngleAxis();

			data.addAxis(detectorName, aname, new NexusGroupData(axis), i + 1, 1, aunit, false);

			data.addData(detectorName, "reagion_name", new NexusGroupData(regionName));

			data.addData(detectorName, "lens_mode", new NexusGroupData(analyser.getLensMode()));
			data.addData(detectorName, "acquisition_mode", new NexusGroupData(analyser.getAcquisitionMode()));
			data.addData(detectorName, "detector_mode", new NexusGroupData(analyser.getDetectorMode()));

			data.addData(detectorName, "pass_energy", new NexusGroupData(analyser.getPassEnergy()));
			if (energyMode.equalsIgnoreCase("Binding")) {
				data.addData(detectorName, "energy_mode", new NexusGroupData(energyMode));
				data.addData(detectorName, "low_energy", new NexusGroupData(analyser.getExcitationEnergy()- analyser.getEndEnergy()), "eV");
				data.addData(detectorName, "high_energy", new NexusGroupData(analyser.getExcitationEnergy()- analyser.getStartEnergy()), "eV");
				data.addData(detectorName, "fixed_energy", new NexusGroupData(analyser.getExcitationEnergy()- analyser.getCentreEnergy()), "eV");
			}
			else {
				data.addData(detectorName, "energy_mode", new NexusGroupData(analyser.getEnergyMode()));
				data.addData(detectorName, "low_energy", new NexusGroupData(analyser.getStartEnergy()), "eV");
				data.addData(detectorName, "high_energy", new NexusGroupData(analyser.getEndEnergy()), "eV");
				data.addData(detectorName, "fixed_energy", new NexusGroupData(analyser.getCentreEnergy()), "eV");
			}
			data.addData(detectorName, "excitation_energy", new NexusGroupData(analyser.getExcitationEnergy()), "eV");
			data.addData(detectorName, "energy_step", new NexusGroupData(analyser.getEnergyStep()), "eV");
			double stepTime = analyser.getStepTime();
			data.addData(detectorName, "step_time", new NexusGroupData(stepTime), "s");
			data.addData(detectorName, "number_of_slices", new NexusGroupData(analyser.getSlices()));
			data.addData(detectorName, "number_of_iterations", new NexusGroupData(analyser.getNumberIterations()));
			int totalSteps = analyser.getTotalSteps().intValue();
			data.addData(detectorName, "total_steps", new NexusGroupData(totalSteps));
			data.addData(detectorName, "total_time", new NexusGroupData(totalSteps*stepTime));

			int cameraMinX = analyser.getCameraMinX();
			data.addData(detectorName, "detector_x_from", new NexusGroupData(cameraMinX));
			int cameraMinY = analyser.getCameraMinY();
			data.addData(detectorName, "detector_y_from", new NexusGroupData(cameraMinY));
			data.addData(detectorName, "detector_x_to", new NexusGroupData(analyser.getCameraSizeX()-cameraMinX));
			data.addData(detectorName, "detector_y_to", new NexusGroupData(analyser.getCameraSizeY()-cameraMinY));
//
//			data.addData(detectorName, "region_size", new NexusGroupData(analyser.getAdBase().getSizeX_RBV(), analyser.getAdBase().getSizeY_RBV()));
		}
	private int[] determineDataDimensions(NDArray ndArray) throws Exception {
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
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
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
			dataVals = new NexusGroupData(dims, cd);
		}
			break;
		case NDPluginBase.UInt32: // TODO should convert to INT64 if any numbers
									// are negative
		case NDPluginBase.Int32: {
			int[] s = ndArray.getIntArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
		}
			break;
		case NDPluginBase.Float32:
		case NDPluginBase.Float64: {
			float[] s = ndArray.getFloatArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:"
						+ s.length + " expected:" + expectedNumPixels);

			dataVals = new NexusGroupData(dims, s);
		}
			break;
		default:
			throw new DeviceException("Type of data is not understood :"
					+ dataType);
		}

		data.addData(detectorName, regionName + "_image", dataVals, "counts", 1);
	}

}