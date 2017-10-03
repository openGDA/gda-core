/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Passive detector which sets up and then reads out from the Xspress3 readout chain via EPICS.
 * <p>
 * By passive I mean that the Xspress3 is an externally triggered system and this class does not cover how the triggers are generated. This class sets up the
 * Xspress3 time frames and reads out the data.
 *
 * @see uk.ac.gda.devices.detector.xspress#Xspress3System
 * @author rjw82
 */
public class Xspress3Detector extends DetectorBase implements Xspress3 {
	private static final int MCA_SIZE = 4096;
	public static final String ALL_ELEMENT_SUM_LABEL = "AllElementSum_";
	public static int SUM_ALL_ROI = 0;
	public static int SUM_FIRST_ROI = 1;
	public static int MAX_ROI_PER_CHANNEL = 4;

	protected Xspress3Controller controller;
	private String channelLabelPrefix = "FF channel ";
	private String sumLabel = "FF";
	private String unitsLabel = "counts";
	private int framesRead = 0;
	private int firstChannelToRead = 0;
	private int numberOfChannelsToRead = 1;
	private int summingMethod = SUM_ALL_ROI;

	private boolean writeHDF5Files = false;
	private String filePath = "";
	private String filePrefix = "";
	private String numTrackerExtension = "nxs";
	private NumTracker numTracker;
	private int currentScanNumber = -1;

	private String configFileName;

	public Xspress3Detector() {
		super();
	}

	@Override
	public void configure() throws FactoryException {
		controller.configure();
		super.configure();
		if (numTracker == null) {
			createNumTracker();
		}
		if (filePrefix.isEmpty() && !getName().isEmpty()) {
			filePrefix = getName();
		}
		inputNames = new String[] {};
	}

	private void createNumTracker() {
		if (numTrackerExtension != null && !numTrackerExtension.isEmpty()) {
			try {
				numTracker = new NumTracker(numTrackerExtension);
			} catch (IOException e) {
				throw new IllegalArgumentException("NumTracker with extension '" + numTrackerExtension + "' could not be created.", e);
			}
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation currentscan = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		currentScanNumber = currentscan.getScanNumber();
		int numDimensions = currentscan.getDimensions().length;
		int lengthOfEachScanLine = currentscan.getDimensions()[numDimensions - 1];
		setNumberOfFramesToCollect(lengthOfEachScanLine);
		stop();
		prepareFileWriting();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		framesRead = 0;
		startRunningXspress3FrameSet();
	}

	public int getFramesRead() {
		return framesRead;
	}

	public void setFramesRead(int framesRead) {
		this.framesRead = framesRead;
	}

	protected void startRunningXspress3FrameSet() throws DeviceException {
		// if (writeHDF5Files) {
		// do not do this if writeHDF5Files is false as may cause errors in
		// epics
		controller.setSavingFiles(writeHDF5Files);
		// }
		controller.doErase();
		controller.doStart();

	}

	@Override
	public void atScanEnd() throws DeviceException {
		currentScanNumber = -1;
	}

	@Override
	public void atPointEnd() throws DeviceException {
		if (controller.getNumFramesToAcquire() > 1) {
			framesRead++;
		}
	}

	private void prepareFileWriting() throws DeviceException {
		if (writeHDF5Files) {
			// set file path name, number here if known or set
			if (filePath != null && !filePath.isEmpty()) {
				controller.setFilePath(filePath);
			} else {
				controller.setFilePath(PathConstructor.createFromDefaultProperty());
			}

			if (filePrefix != null && !filePrefix.isEmpty()) {
				String scanNumber = getScanNumber();
				if (!scanNumber.isEmpty()) {
					scanNumber = "_" + scanNumber;
				}
				controller.setFilePrefix(filePrefix + scanNumber + "_");
			} else {
				controller.setFilePrefix("xspress3_");
			}

			controller.setNextFileNumber(0);
		}
	}

	private String getScanNumber() {
		return Long.toString(currentScanNumber);
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing here as the detector is passive: it sets up time frames at
		// the start of the scan and then is triggered by a TFG (or another
		// detector driving the TFG) which must be
		// included in the same scan
	}

	@Override
	public void stop() throws DeviceException {
		stopXspress3();
	}

	protected void stopXspress3() throws DeviceException {
		controller.doStop();
	}

	@Override
	public int getStatus() throws DeviceException {
		int controllerStatus = controller.getStatus();
		if (controllerStatus == Detector.FAULT || controllerStatus == Detector.STANDBY) {
			return controllerStatus;
		}
		// This detector class is completely passive and is dependent on being
		// triggered by the TFG, so always return idle and let whatever other
		// Detector object driving the TFG to control the status
		return Detector.IDLE;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // false as this will return data for the GDA to write
						// itself.
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {

		// this method is for step scan readout and so it assumed that the data
		// has been collected by the time this method is called

		try {
			// yuck, but unavoidable. The Xspress3 is driven by the TFG, but we
			// are
			// looking at the EPICS layer which will be a bit behind the gate
			// signals sent to the xspress3 electronics, so need a delay until
			// the available frames PV (and therefore all the data PVs) has been
			// updated
			Thread.sleep(150);
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException during readout.");
		}

		if (framesRead == controller.getTotalFramesAvailable()) {
			// we have run out of data! This method should not have been called.
			// Problem in the logic somewhere.
			throw new DeviceException("Cannot readout - no more data in buffer");
		}

		// get all various info and add to a NexusTreeProvider
		NexusTreeProvider tree = readFrames(framesRead, framesRead)[0];

		return tree;
	}

	/**
	 * Currently only looks at ROI, not the scaler windows.
	 * <p>
	 * Returns the FF (sum of ROI) in the plottable values. May want the option in the future to return the individual ROI values instead.
	 * <p>
	 * TODO add a link to the HDF5 file created by underlying controller instead of adding more data to this Nexus tree.
	 *
	 * @param firstFrame
	 * @param lastFrame
	 * @return NexusTreeProvider array for every frame
	 * @throws DeviceException
	 */

	@Override
	public NXDetectorData[] readFrames(int firstFrame, int lastFrame) throws DeviceException {
		int numFramesAvailable = controller.getTotalFramesAvailable();
		if (lastFrame > numFramesAvailable) {
			throw new DeviceException("Only " + numFramesAvailable + " frames available, cannot return frames " + firstFrame + " to " + lastFrame);
		}

		// readout ROI in format [frame][detector channel][ROIs]
		int finalChannelToRead = numberOfChannelsToRead + firstChannelToRead - 1;
		int numFramesRead = lastFrame - firstFrame + 1;

		// derive FFs from ROIs on MCAs
		// Double[][][] data = controller.readoutDTCorrectedROI(firstFrame,
		// lastFrame, firstChannelToRead, finalChannelToRead);
		// calc FF from ROI
		// Double[][] FFs = calculateFFs(data, numFramesRead);

		// read out FFs from sca5
		Double[][] FFs_sca5 = controller.readoutDTCorrectedSCA1(firstFrame, lastFrame, firstChannelToRead, finalChannelToRead);
		Double[][] FFs_sca6 = controller.readoutDTCorrectedSCA2(firstFrame, lastFrame, firstChannelToRead, finalChannelToRead);
		double[][] FFs = new double[numFramesRead][numberOfChannelsToRead];
		for (int frame = 0; frame < numFramesRead; frame++) {
			for (int channel = 0; channel < numberOfChannelsToRead; channel++) {
				// TODO summing all ROIs here - should check current value of summingMethod!
				// Check on b18 beamline and found that the right calculation is FFs[frame][channel] = FFs_sca5[frame][channel];
				// and not FFs[frame][channel] = FFs_sca5[frame][channel] + FFs_sca6[frame][channel];
				FFs[frame][channel] = FFs_sca5[frame][channel];
			}
		}

		// create trees
		NXDetectorData[] results = new NXDetectorData[numFramesRead];

		for (int frame = 0; frame < numFramesRead; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(this);
			// INexusTree detTree = thisFrame.getDetTree(getName());
			// thisFrame.addData(detTree, sumLabel, new int[] { numberOfChannelsToRead }, Dataset.FLOAT64, FFs[frame], unitsLabel, 1);
			thisFrame.addData(getName(), sumLabel, new NexusGroupData(FFs[frame]), unitsLabel, 1);
			for (int chan = 0; chan < numberOfChannelsToRead; chan++) {
				thisFrame.setPlottableValue(getExtraNames()[chan], FFs[frame][chan]);
			}

			double totalFF = 0;
			for (Double ff : FFs[frame]) {
				totalFF += ff;
			}
			thisFrame.setPlottableValue(getExtraNames()[numberOfChannelsToRead], totalFF);

			// TODO this needs fixing at some point - currently writes an absolute path when better to use relative in Nexus
			if (writeHDF5Files) {
				thisFrame.addScanFileLink(getName(), "nxfile://" + controller.getFullFileName() + "#entry/instrument/detector/data");
			}

			results[frame] = thisFrame;
		}
		return results;
	}

	/**
	 * For use by Xspress3FFOverI0Detector only. Not intended for use in continuous scans. Largely duplicates code used in
	 * Xspress3FFoverI0BufferedDetector.getFF().
	 */
	public double readoutFFTotal() throws DeviceException {

		// inefficient to call this whole method just to get the FF, but easiest option for now
		NXDetectorData xspressFrame = (NXDetectorData) readout();

		Double[] xspressOutput = xspressFrame.getDoubleVals();
		String[] names = getExtraNames();

		double ffTotal = 0;
		for (int index = 0; index < names.length; index++) {
			if (names[index].equals("FF")) {
				ffTotal = xspressOutput[index];
			}
		}
		return ffTotal;
	}

	@SuppressWarnings("unused") // still used in commented code in readoutFrames()
	private Double[][] calculateFFs(Double[][][] data, int numFramesRead) {
		Double[][] FFs = new Double[numFramesRead][numberOfChannelsToRead]; // [frame][detector channel]
		for (int frame = 0; frame < numFramesRead; frame++) {
			for (int chan = 0; chan < numberOfChannelsToRead; chan++) {
				if (summingMethod == SUM_FIRST_ROI) {
					FFs[frame][chan] = data[frame][chan][0];
				} else {
					FFs[frame][chan] = sumArray(data[frame][chan]);
				}
			}
		}
		return FFs;
	}

	@Override
	public String[] getExtraNames() {
		// these are the plottable values. For this detector it is the FF for
		// each channel
		String[] extraNames = new String[numberOfChannelsToRead + 1];
		for (int i = 0; i < numberOfChannelsToRead; i++) {
			extraNames[i] = "Chan" + (firstChannelToRead + i);
		}
		extraNames[numberOfChannelsToRead] = "FF";
		return extraNames;
	}

	private Double sumArray(Double[] doubles) {
		Double sum = 0.0;
		for (Double element : doubles) {
			sum += element;
		}
		return sum;
	}

	@Override
	public void setRegionsOfInterest(DetectorROI[] regionList) throws DeviceException {
		if (regionList.length > MAX_ROI_PER_CHANNEL) {
			throw new DeviceException("Too many regions! Only " + MAX_ROI_PER_CHANNEL + " allowed.");
		}
		for (int chan = firstChannelToRead; chan < numberOfChannelsToRead + firstChannelToRead; chan++) {
			for (int roiNum = 0; roiNum < MAX_ROI_PER_CHANNEL; roiNum++) {
				// 'soft' ROIs on MCAs
				if (roiNum < regionList.length) {
					controller.setROILimits(chan, roiNum, new int[] { regionList[roiNum].getRoiStart(), regionList[roiNum].getRoiEnd() });
				} else {
					controller.setROILimits(chan, roiNum, new int[] { 0, 0 });
				}
				if (roiNum < 2 && roiNum < regionList.length) {
					controller.setWindows(chan, roiNum, new int[] { regionList[roiNum].getRoiStart(), regionList[roiNum].getRoiEnd() });
				} else if (roiNum < 2) {
					controller.setWindows(chan, roiNum, new int[] { 0, 0 });
				}
			}
		}
		controller.setNumberROIToRead(regionList.length);
	}

	/**
	 * For the moment, all ROI on all channels are the same, and assumed by this class to be the same.
	 *
	 * @return ROI[]
	 * @throws DeviceException
	 */
	@Override
	public DetectorROI[] getRegionsOfInterest() throws DeviceException {
		DetectorROI[] rois = new DetectorROI[controller.getNumberROIToRead()];

		for (int roiNum = 0; roiNum < rois.length; roiNum++) {
			rois[roiNum] = new DetectorROI();
			rois[roiNum].setRoiName("ROI" + roiNum);
			Integer[] limits = controller.getROILimits(0, roiNum);
			rois[roiNum].setRoiStart(limits[0]);
			rois[roiNum].setRoiEnd(limits[1]);
		}
		return rois;
	}

	@Override
	public void clearAndStart() throws DeviceException {
		controller.doErase();
		controller.doStart();
	}

	public int[][] getData() throws DeviceException {

		double[][] deadTimeCorrectedData = controller.readoutDTCorrectedLatestMCA(firstChannelToRead, getNumberOfChannelsToRead() - 1);
		int[][] deadTimeCorrectedDataInt = new int[deadTimeCorrectedData.length][deadTimeCorrectedData[0].length];
		for (int i = 0; i < deadTimeCorrectedData.length; i++) {
			for (int j = 0; j < deadTimeCorrectedData[0].length; j++) {
				deadTimeCorrectedDataInt[i][j] = (int) Math.round(deadTimeCorrectedData[i][j]);
			}
		}
		return deadTimeCorrectedDataInt;
	}

	/**
	 * @param time
	 *            - milliseconds
	 * @return
	 * @throws DeviceException
	 */
	@Override
	@Deprecated
	public int[][] getMCData(double time) throws DeviceException {
		getMCAData(time);

		double[][] mcaData = getMCAData(time);
		return getIntDataFromDoubles(mcaData);
	}

	/**
	 * @param time
	 *            - milliseconds
	 * @return
	 * @throws DeviceException
	 */
	@Override
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

		return controller.readoutDTCorrectedLatestMCA(firstChannelToRead, getNumberOfChannelsToRead() - 1);
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

	public int getFirstChannelToRead() {
		return firstChannelToRead;
	}

	public void setFirstChannelToRead(int firstChannelToRead) {
		this.firstChannelToRead = firstChannelToRead;
	}

	public int getNumberOfChannelsToRead() {
		return numberOfChannelsToRead;
	}

	public void setNumberOfChannelsToRead(int numberOfChannelsToRead) {
		this.numberOfChannelsToRead = numberOfChannelsToRead;
		// this defines the number of extraNames as currently extraNames = FF
		// per channel
		String[] newExtraNames = new String[numberOfChannelsToRead];
		String[] newoutputFormat = new String[numberOfChannelsToRead];
		newoutputFormat[0] = this.outputFormat[0];

		for (int chan = 0; chan < numberOfChannelsToRead; chan++) {
			String label = channelLabelPrefix + (chan + firstChannelToRead);
			newExtraNames[chan] = label;
			newoutputFormat[chan] = this.outputFormat[0];
		}
		this.extraNames = newExtraNames;
		this.outputFormat = newoutputFormat;
	}

	public int getNumberOfFramesToCollect() throws DeviceException {
		return controller.getNumFramesToAcquire();
	}

	public void setNumberOfFramesToCollect(int numberOfFramesToCollect) throws DeviceException {
		controller.setNumFramesToAcquire(numberOfFramesToCollect);
	}

	public int getSummingMethod() {
		return summingMethod;
	}

	public void setSummingMethod(int summingMethod) {
		this.summingMethod = summingMethod;
	}

	public String getSumLabel() {
		return sumLabel;
	}

	public void setSumLabel(String sumLabel) {
		this.sumLabel = sumLabel;
	}

	public String getUnitsLabel() {
		return unitsLabel;
	}

	public void setUnitsLabel(String unitsLabel) {
		this.unitsLabel = unitsLabel;
	}

	@Override
	public Xspress3Controller getController() {
		return controller;
	}

	public void setController(Xspress3Controller controller) {
		this.controller = controller;
	}

	public boolean isWriteHDF5Files() {
		return writeHDF5Files;
	}

	public void setWriteHDF5Files(boolean writeHDF5Files) {
		this.writeHDF5Files = writeHDF5Files;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public String getNumTrackerExtension() {
		return numTrackerExtension;
	}

	public void setNumTrackerExtension(String numTrackerExtension) {
		this.numTrackerExtension = numTrackerExtension;
		createNumTracker();
	}

	/*
	 * @Override public Object getCountRates() throws DeviceException { return null; }
	 */
	@Override
	public String getConfigFileName() {
		return configFileName;
	}

	@Override
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	public void loadConfigurationFromFile() throws Exception {
		if (getConfigFileName() == null)
			return;

		Xspress3Parameters vortexParameters = (Xspress3Parameters) XMLHelpers.createFromXML(Xspress3Parameters.mappingURL, Xspress3Parameters.class,
				Xspress3Parameters.schemaURL, getConfigFileName());

		List<DetectorROI> vortexRois = vortexParameters.getDetector(0).getRegionList();
		DetectorROI[] rois = new DetectorROI[vortexRois.size()];
		for (int index = 0; index < vortexRois.size(); index++) {
			rois[index] = new DetectorROI(vortexRois.get(index).getRoiName(), vortexRois.get(index).getRoiStart(), vortexRois.get(index).getRoiEnd());
		}

		setRegionsOfInterest(rois);
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		if (parameters instanceof Xspress3Parameters) {
			applyXspress3ConfigurationParameters((Xspress3Parameters) parameters);
		} else {
			throw new IllegalArgumentException("An Xspress3Parameters object must be provided to configure the Xspress3 detector");
		}
	}

	private void applyXspress3ConfigurationParameters(Xspress3Parameters parameters) throws DeviceException {
		List<DetectorROI> roisList = parameters.getDetector(0).getRegionList();
		DetectorROI[] roisArray = new DetectorROI[roisList.size()];
		for (int index = 0; index < roisList.size(); index++) {
			DetectorROI roi = roisList.get(index);
			roisArray[index] = new DetectorROI(roi.getRoiName(), roi.getRoiStart(), roi.getRoiEnd());
		}

		for (int channel = 0; channel < parameters.getDetectorList().size(); channel++) {
			boolean channelDisabled = parameters.getDetectorList().get(channel).isExcluded();
			controller.enableChannel(channel, !channelDisabled);
		}

		setRegionsOfInterest(roisArray);
	}

	@Override
	public int getNumberOfElements() {
		return controller.getNumberOfChannels();
	}

	@Override
	public int getMCASize() {
		return MCA_SIZE;
	}

	@Override
	public int getMaxNumberOfRois() {
		return MAX_ROI_PER_CHANNEL;
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		DetectorROI[] regions;
		try {
			regions = getRegionsOfInterest();
		} catch (DeviceException e) {
			Xspress3Parameters parameters = new Xspress3Parameters();
			parameters.setDetectorName(getName());
			return parameters;
		}

		List<DetectorElement> detectorList = new ArrayList<DetectorElement>();

		for (int i = 0; i < getNumberOfElements(); i++){
			DetectorElement thisElement = new DetectorElement();
			for (DetectorROI region : regions){
				thisElement.addRegion(region);
			}
			detectorList.add(thisElement);
		}

		Xspress3Parameters parameters = new Xspress3Parameters();
		parameters.setDetectorName(getName());
		parameters.setDetectorList(detectorList);

		return parameters;
	}

	/**
	 * For a given row in a multiDimensional scan , this returns the name of the subentry in the Nexus file which contains all the MCAs for that row.
	 * <p>
	 * This will become redundant when SWMR is available and all Mca data can be placed in the same HDF5 file by the Area Detector EPICS plugin, so there will
	 * be no need to put MCA data in different files and Nexus nodes.
	 *
	 * @param rowNumber
	 * @return
	 */
	public static String getNameOfRowSubNode(int rowNumber) {
		return "mcas_row_" + String.format("%04d", rowNumber);
	}

	public static String getNameOfAllElementSumRowSubNode(int rowNumber) {
		return ALL_ELEMENT_SUM_LABEL + "row_" + String.format("%04d", rowNumber);
	}

	@Override
	public double readoutFF() throws DeviceException {
		// TODO Auto-generated method stub
		return readoutFFTotal();
	}

}
