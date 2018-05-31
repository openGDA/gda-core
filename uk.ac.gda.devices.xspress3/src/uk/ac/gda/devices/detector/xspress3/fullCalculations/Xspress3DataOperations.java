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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.factory.Finder;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class Xspress3DataOperations {

	private static final String MCA_LABEL = "MCAs";
	private static final String SUM_LABEL = "FF";
	private static final String UNITS_LABEL = "counts";
	private static final String ALL_ELEMENT_SUM_LABEL = "AllElementSum";

	private Xspress3Controller controller;
	private int firstChannelToRead;
	private int framesRead;
	private String configFileName;
	private DetectorROI[] rois;
	private boolean[] isChannelEnabled;
	private Xspress3FileReader reader;
	private boolean readDataFromFile;
	private static final Logger logger = LoggerFactory.getLogger(Xspress3DataOperations.class);

	protected Xspress3DataOperations(Xspress3Controller controller, int firstChannelToRead) {
		this.controller = controller;
		this.firstChannelToRead = firstChannelToRead;
		this.readDataFromFile = false;
	}

	protected String[] getOutputFormat() {
		int numNames = getExtraNames().length + 1;  // the + 1 for the inputName
													// which every detector has
		String[] outputFormat = new String[numNames];
		for (int i = 0; i < numNames; i++) {
			outputFormat[i] = "%.3f";
		}
		return outputFormat;
	}

	// maybe change the method name to prepareForCollection()
	// atScanStart()
	protected void atScanStart(boolean readDataFromFile) throws DeviceException {
		this.readDataFromFile = readDataFromFile;
		if (!readDataFromFile) {
			enableEpicsMcaStorage();
		} else {
			// we are in a Continuous / Fly scan, so data will be readback from
			// the HDF file at the end of each scan line
		}
		framesRead = 0;
	}

	protected void atScanLineStart() {
		if (readDataFromFile)
			framesRead = 0;
		reader = null;
	}

	protected void atPointEnd() {
		framesRead++;
	}

	protected String getConfigFileName() {
		return configFileName;
	}

	protected void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	protected void loadConfigurationFromFile() throws Exception {
		if (getConfigFileName() == null)
			return;

		Xspress3Parameters vortexParameters = (Xspress3Parameters) XMLHelpers.createFromXML(Xspress3Parameters.mappingURL, Xspress3Parameters.class,
				Xspress3Parameters.schemaURL, getConfigFileName());

		applyConfigurationParameters(vortexParameters);
	}

	protected void applyConfigurationParameters(FluorescenceDetectorParameters parameters) {
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

	/*
	 * Enable the visualisation of all the EPICS channels, but ensure that all the ROIs are empty, so only the latest MCAs are kept and visualisation is at a
	 * minimum.
	 */
	private void enableEpicsMcaStorage() throws DeviceException {
		int numChannels = controller.getNumberOfChannels();
		for (int channel = 0; channel < numChannels; channel++) {
			controller.enableChannel(channel, true);
		}
	}

	protected NexusTreeProvider readoutLatest(String detectorName) throws DeviceException {
		int numPointAvailableInArrays = 0;
		// the numPointAvailableInArrays is the array index so framesRead - 1
		numPointAvailableInArrays = controller.monitorUpdateArraysAvailableFrame(framesRead);
		logger.debug("framesRead={}, numPointAvailableInArrays={}", framesRead, numPointAvailableInArrays);
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
				if (Double.isNaN(value)) value = 0.0;
				filtered[i][j] = value;
			}
		}
		return filtered;
	}

	private NXDetectorData createNexusTreeForFrame(double[][] mcasFromFile, String detectorName) {

		double[][] filteredMCAs = removeNaNs(mcasFromFile);

		int numChannels = filteredMCAs.length;
		int numRois = rois.length;

		double[][] roiValues = new double[numRois][numChannels];
		double theFF = 0;
		for (int chan = 0; chan < numChannels; chan++) {
			if (isChannelEnabled[chan]) { // excluded channels do not have a value for ROIs or do they contribute to the FF
				for (int roi = 0; roi < numRois; roi++) {
					double thisRoiSum = extractRoi(filteredMCAs[chan], rois[roi].getRoiStart(), rois[roi].getRoiEnd());
					roiValues[roi][chan] = thisRoiSum;
					theFF += thisRoiSum;
				}
			}
		}

		int numMcaElements = filteredMCAs[0].length;
		double[] allElementSum = new double[numMcaElements];
		for (int chan = 0; chan < numChannels; chan++) {
			if (isChannelEnabled[chan]) { // excluded channels do not contribute to the allElementSum
				for (int element = 0; element < numMcaElements; element++) {
					allElementSum[element] += filteredMCAs[chan][element];
				}
			}
		}

		NXDetectorData thisFrame = new NXDetectorData(getExtraNames(), getOutputFormat(), detectorName);
		INexusTree detTree = thisFrame.getDetTree(detectorName);

		// add the FF (sum over all rois, over all channels)
		NXDetectorData.addData(detTree, SUM_LABEL, new NexusGroupData(theFF), UNITS_LABEL, 1);

		// add rois
		for (int roi = 0; roi < numRois; roi++) {
			NXDetectorData.addData(detTree, rois[roi].getRoiName(), new NexusGroupData(roiValues[roi]), UNITS_LABEL, 2);
		}

		// add MCAs
		NXDetectorData.addData(detTree, MCA_LABEL, new NexusGroupData(filteredMCAs), UNITS_LABEL, 2);

		// add all element sum
		NXDetectorData.addData(detTree, ALL_ELEMENT_SUM_LABEL, new NexusGroupData(allElementSum), UNITS_LABEL, 2);

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

	protected NXDetectorData[] readoutFrames(int firstFrame, int lastFrame, String detectorName) throws DeviceException {
		int numFramesAvailable;
		if (readDataFromFile) {
			numFramesAvailable = controller.getTotalHDFFramesAvailable();
		} else
			numFramesAvailable = controller.getTotalFramesAvailable();
		if (lastFrame > numFramesAvailable) {
			throw new DeviceException("Only " + numFramesAvailable + " frames available, cannot return frames " + firstFrame + " to " + lastFrame);
		}

		try {
			extractMCAsFromFile(controller.getFullFileName());
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

	private void extractMCAsFromFile(String filename) throws ScanFileHolderException {
		if (reader == null) {
			reader = new Xspress3FileReader(filename, controller.getNumberOfChannels(), controller.getMcaSize());
			reader.readFile();
		}
	}

	protected double readoutFF() throws DeviceException {
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

	protected String[] getExtraNames() {
		if (rois == null || controller == null) {
			return new String[] { "FF" };
		}

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
	protected int[][] getMCData(double time) throws DeviceException {
		getMCAData(time);

		double[][] mcaData = getMCAData(time);
		return getIntDataFromDoubles(mcaData);
	}

	protected double[][] getMCAData(double time) throws DeviceException {
		controller.doErase();
		controller.setTriggerMode(TRIGGER_MODE.TTl_Veto_Only);
		controller.doStart();

		Timer tfg = Finder.getInstance().find("tfg");
		tfg.clearFrameSets(); // we only want to collect a frame at a time
		tfg.countAsync(time); // run tfg for time
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		} while (tfg.getStatus() == Timer.ACTIVE);

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

	protected DetectorROI[] getRegionsOfInterest() {
		return rois;
	}

	protected void setRegionsOfInterest(DetectorROI[] regionList) {
		rois = regionList;
	}

	protected Xspress3Parameters getConfigurationParameters() {
		DetectorROI[] regions = getRegionsOfInterest();
		if (regions == null) {
			regions = new DetectorROI[0];
		}

		List<DetectorElement> detectorList = new ArrayList<>();

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

	protected void atScanEnd() throws DeviceException {
		enableEpicsMcaStorage();
	}

}

