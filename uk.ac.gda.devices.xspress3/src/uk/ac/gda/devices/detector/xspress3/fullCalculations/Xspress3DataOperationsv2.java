/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class Xspress3DataOperationsv2 {

	private static final String mcaLabel = "MCAs";
	private final String sumLabel = "FF";
	private final String unitsLabel = "counts";
	private final String allElementSumLabel = "AllElementSum";

	private Xspress3Controller controller;
	private int firstChannelToRead;
	private int framesRead;
	private String configFileName;
	private DetectorROI[] rois;
	private boolean[] isChannelEnabled;
	private Xspress3FileReaderv2 reader;
	private boolean readDataFromFile;
	private int lineNumber;
	private static final Logger logger = LoggerFactory.getLogger(Xspress3DataOperationsv2.class);

	public Xspress3DataOperationsv2(Xspress3Controller controller, int firstChannelToRead) {
		this.controller = controller;
		this.firstChannelToRead = firstChannelToRead;
		this.lineNumber = 0;
		this.readDataFromFile = false;
	}

	public String[] getOutputFormat() {
		int numNames = getExtraNames().length + 1; // the + 1 for the inputName
		// which every detector has
		String[] outputFormat = new String[numNames];
		for (int i = 0; i < numNames; i++) {
			outputFormat[i] = "%.3f";
		}
		return outputFormat;
	}

	// maybe change the method name to prepareForCollection()
	// atScanStart()
	public void atScanStart(boolean readDataFromFile) throws DeviceException {
		// remove for testing Xspress3 v.2 as this plugin is not yet available
		// controller.setPerformROICalculations(false);
		this.readDataFromFile = readDataFromFile;
		if (readDataFromFile) {
			// we are in a Continuous / Fly scan, so data will be readback from
			// the HDF file at the end of each scan line
			disableAllEPICSCalculations();
		} else {
			enableEpicsMcaStorage();
		}
		lineNumber = 0;
	}

	public void atScanLineStart() throws DeviceException {
		lineNumber++;
		framesRead = 0;
		reader = null;
	}

	public void atPointEnd() throws DeviceException {
		framesRead++;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public void loadConfigurationFromFile() throws Exception {
		if (getConfigFileName() == null)
			return;

		Xspress3Parameters vortexParameters = (Xspress3Parameters) XMLHelpers.createFromXML(Xspress3Parameters.mappingURL, Xspress3Parameters.class,
				Xspress3Parameters.schemaURL, getConfigFileName());

		applyConfigurationParameters(vortexParameters);
	}

	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) {
		List<DetectorROI> vortexRois = parameters.getDetector(0).getRegionList();
		rois = new DetectorROI[vortexRois.size()];
		for (int index = 0; index < vortexRois.size(); index++) {
			rois[index] = new DetectorROI(vortexRois.get(index).getRoiName(), vortexRois.get(index).getRoiStart(), vortexRois.get(index).getRoiEnd());
		}

		isChannelEnabled = new boolean[controller.getNumberOfChannels()];
		for (int detector = 0; detector < controller.getNumberOfChannels(); detector++) {
			isChannelEnabled[detector] = !parameters.getDetector(detector).isExcluded();
		}
	}

	private void disableAllEPICSCalculations() throws DeviceException {
		int numChannels = controller.getNumberOfChannels();
		for (int channel = 0; channel < numChannels; channel++) {
			controller.enableChannel(channel, false);
		}
	}

	/*
	 * Enable the visualisation of all the EPICS channels, but ensure that all the ROIs are empty, so only the latest MCAs are kept and visualisation is at a
	 * minimum.
	 */
	private void enableEpicsMcaStorage() throws DeviceException {
		int numChannels = controller.getNumberOfChannels();
		for (int channel = 0; channel < numChannels; channel++) {
			controller.enableChannel(channel, true);
			// controller.setWindows(channel, 1, new int[] { 0, 0 });
			// controller.setWindows(channel, 2, new int[] { 0, 0 });
			// for (int roi = 0; roi < EpicsXspress3ControllerPvProvider.NUMBER_ROIs; roi++) {
			// controller.setROILimits(numChannels, roi, new int[] { 0, 0 });
			// }
		}
	}

	public NexusTreeProvider readoutLatest(String detectorName) throws DeviceException {
		int numPointAvailableInArrays = 0;
		// the numPointAvailableInArrays is the array index so framesRead - 1
		numPointAvailableInArrays = controller.monitorUpdateArraysAvailableFrame(framesRead);
		logger.info("framesRead=" + framesRead + " and UpdateArraysAvailableFrame: " + numPointAvailableInArrays);
		if (framesRead != numPointAvailableInArrays) {
			throw new DeviceException("Xspress3 arrays are not updated correctly!");
		}

		return readoutLatestFrame(detectorName);
	}

	private NexusTreeProvider readoutLatestFrame(String detectorName) throws DeviceException {
		double[][] data = controller.readoutDTCorrectedLatestMCA(0, controller.getNumberOfChannels() - 1);
		return createNexusTreeForFrame(data, detectorName);
	}

	private double[][] removeNaNs(double[][] original) {
		// because we might get NaNs from EPICS, which will mess up our totals
		double[][] filtered = new double[original.length][original[0].length];
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < original[0].length; j++) {
				double value = original[i][j];
				if (Double.toString(value).compareTo("NaN") == 0) {
					value = 0.0;
				}
				filtered[i][j] = value;
			}
		}
		original = filtered;
		return original;
	}

	private NXDetectorData createNexusTreeForFrame(double[][] mcasFromFile, String detectorName) {

		mcasFromFile = removeNaNs(mcasFromFile);

		int numChannels = mcasFromFile.length;
		int numRois = rois.length;

		double[][] roiValues = new double[numRois][numChannels];
		double theFF = 0;
		for (int chan = 0; chan < numChannels; chan++) {
			if (isChannelEnabled[chan]) { // excluded channels do not have a value for ROIs or do they contribute to the FF
				for (int roi = 0; roi < numRois; roi++) {
					double thisRoiSum = extractRoi(mcasFromFile[chan], rois[roi].getRoiStart(), rois[roi].getRoiEnd());
					roiValues[roi][chan] = thisRoiSum;
					theFF += thisRoiSum;
				}
			}
		}

		int numMcaElements = mcasFromFile[0].length;
		double[] allElementSum = new double[numMcaElements];
		for (int chan = 0; chan < numChannels; chan++) {
			if (isChannelEnabled[chan]) { // excluded channels do not contribute to the allElementSum
				for (int element = 0; element < numMcaElements; element++) {
					allElementSum[element] += mcasFromFile[chan][element];
				}
			}
		}

		NXDetectorData thisFrame = new NXDetectorData(getExtraNames(), getOutputFormat(), detectorName);
		INexusTree detTree = thisFrame.getDetTree(detectorName);

		// add the FF (sum over all rois, over all channels)
		NXDetectorData.addData(detTree, sumLabel, new NexusGroupData(theFF), unitsLabel, 1);

		// add rois
		for (int roi = 0; roi < numRois; roi++) {
			NexusGroupData roiData = new NexusGroupData(roiValues[roi]);
			roiData.chunkDimensions = new int[] { roiValues[roi].length };
			NXDetectorData.addData(detTree, rois[roi].getRoiName(), roiData, unitsLabel, 2);
		}

		// add MCAs
		NexusGroupData mcaData = new NexusGroupData(mcasFromFile);
		mcaData.chunkDimensions = new int[] { mcasFromFile.length, mcasFromFile[0].length };
		NXDetectorData.addData(detTree, mcaLabel, mcaData, unitsLabel, 2);

		// add all element sum
		NexusGroupData sumData = new NexusGroupData(allElementSum);
		sumData.chunkDimensions = new int[] { allElementSum.length };
		NXDetectorData.addData(detTree, allElementSumLabel, sumData, unitsLabel, 2);

		// add plottable values
		int index = 0;
		String[] extraNames = getExtraNames(); // num channels * num rois + FF
		for (int chan = 0; chan < numChannels; chan++) {
			for (int roi = 0; roi < numRois; roi++) {
				thisFrame.setPlottableValue(extraNames[index], roiValues[roi][chan]);
				index++;
			}
		}
		thisFrame.setPlottableValue(extraNames[index], theFF);

		return thisFrame;
	}

	private double extractRoi(double[] mca, int start, int end) {
		double sum = 0;
		for (int element = start; element < end; element++) {
			sum += mca[element];
		}
		return sum;
	}

	public NXDetectorData[] readoutFrames(int firstFrame, int lastFrame, String detectorName) throws DeviceException {
		// SR get the number of frames from HDFfile writer with reading from HDF5 file
		int numFramesAvailable;
		int driverNumFramesAvailable = 0;
		if (readDataFromFile) {
			numFramesAvailable = controller.getTotalHDFFramesAvailable();
			driverNumFramesAvailable = controller.getTotalFramesAvailable();
			logger.info("controller.getTotalHDFFramesAvailable():" + controller.getTotalHDFFramesAvailable());
			logger.info("controller.getTotalFramesAvailable():" + controller.getTotalFramesAvailable());
			// to speed up scan need to configure the driver at the start of the scan, here a check if no frame is dropped
			// here the driver is waiting for more pulses and the file writer should be stopped, ContinuousScan is sequential
			if (numFramesAvailable != (driverNumFramesAvailable / lineNumber)) {
				throw new DeviceException("Pulses between EPICs HDF file writer and main driver do not match.");
			}
		} else
			numFramesAvailable = controller.getTotalFramesAvailable();
		if (lastFrame > numFramesAvailable) {
			throw new DeviceException("Only " + numFramesAvailable + " frames available, cannot return frames " + firstFrame + " to " + lastFrame);
		}

		try {
			extractMCAsFromFile(controller.getFullFileName(), firstFrame, lastFrame);
			int numFrames = lastFrame - firstFrame + 1;
			NXDetectorData[] frames = new NXDetectorData[numFrames];
			for (int frame = 0; frame < numFrames; frame++) {
				int absoluteFrameNumber = frame + firstFrame;
				frames[frame] = createNexusTreeForFrame(reader.getFrame(absoluteFrameNumber), detectorName);
			}
			return frames;
		} catch (Exception e) {
			reader = null;
			throw new DeviceException(e.getMessage());
		}
	}

	private void extractMCAsFromFile(String filename, int firstFrame, int lastFrame) throws ScanFileHolderException {
		if (reader == null) {
			reader = new Xspress3FileReaderv2(filename, controller.getNumberOfChannels(), controller.getMcaSize());
			reader.readFile();
		}
	}

	public double readoutFF() throws DeviceException {
		int numRois = rois.length;
		int numChannels = controller.getNumberOfChannels();
		double[][] data = controller.readoutDTCorrectedLatestMCA(0, controller.getNumberOfChannels() - 1);
		double[][] dataWithoutNaNs = removeNaNs(data);

		double theFF = 0;
		for (int chan = 0; chan < numChannels; chan++) {
			if (isChannelEnabled[chan]) {
				for (int roi = 0; roi < numRois; roi++) {
					double thisRoiSum = extractRoi(dataWithoutNaNs[chan], rois[roi].getRoiStart(), rois[roi].getRoiEnd());
					theFF += thisRoiSum;
				}
			}
		}
		return theFF;
	}

	public String[] getExtraNames() {
		int numExtraNames = (rois.length * controller.getNumberOfChannels()) + 1;
		String[] extraNames = new String[numExtraNames];
		int index = 0;
		for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
			for (int roi = 0; roi < rois.length; roi++) {
				String valueName = "Chan" + chan + "_" + rois[roi].getRoiName();
				extraNames[index] = valueName;
				index++;
			}
		}
		extraNames[numExtraNames - 1] = "FF";

		return extraNames;
	}

	/**
	 * @param time
	 *            - milliseconds
	 * @return
	 * @throws DeviceException
	 */
	@Deprecated
	public int[][] getMCData(double time) throws DeviceException {
		getMCAData(time);

		double[][] mcaData = getMCAData(time);
		return getIntDataFromDoubles(mcaData);
	}

	public double[][] getMCAData(double time) throws DeviceException {
		controller.doErase();
		controller.doStart();
		((Timer) Finder.getInstance().find("tfg")).clearFrameSets(); // we only want to collect a frame at a time
		((Timer) Finder.getInstance().find("tfg")).countAsync(time); // run tfg for time
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		} while (((Timer) Finder.getInstance().find("tfg")).getStatus() == Timer.ACTIVE);

		controller.doStop();

		return controller.readoutDTCorrectedLatestMCA(firstChannelToRead, controller.getNumberOfChannels() - 1);
	}

	private int[][] getIntDataFromDoubles(double[][] mcaData) {
		int[][] mcaIntData = new int[mcaData.length][mcaData[0].length];
		for (int i = 0; i < mcaData.length; i++) {
			for (int j = 0; j < mcaData[0].length; j++) {
				mcaIntData[i][j] = (int) Math.round(mcaData[i][j]);
			}
		}
		return mcaIntData;
	}

	public DetectorROI[] getRegionsOfInterest() {
		return rois;
	}

	public void setRegionsOfInterest(DetectorROI[] regionList) {
		rois = regionList;
	}

	public Xspress3Parameters getConfigurationParameters() {
		DetectorROI[] regions = getRegionsOfInterest();
		if (regions == null) {
			regions = new DetectorROI[0];
		}

		List<DetectorElement> detectorList = new ArrayList<DetectorElement>();

		for (int i = 0; i < controller.getNumberOfChannels(); i++) {
			DetectorElement thisElement = new DetectorElement();
			if (isChannelEnabled != null) {
				thisElement.setExcluded(!isChannelEnabled[i]);
			}
			for (DetectorROI region : regions) {
				thisElement.addRegion(region);
			}
			detectorList.add(thisElement);
		}

		Xspress3Parameters parameters = new Xspress3Parameters();
		parameters.setDetectorList(detectorList);

		return parameters;
	}

	public void atScanEnd() throws DeviceException {
		enableEpicsMcaStorage();
	}

}

