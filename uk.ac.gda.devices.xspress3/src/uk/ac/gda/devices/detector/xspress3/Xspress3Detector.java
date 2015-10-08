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

import gda.data.PathConstructor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Passive detector which sets up and then reads out from the Xspress3 readout
 * chain via EPICS.
 * <p>
 * By passive I mean that the Xspress3 is an externally triggered system and
 * this class does not cover how the triggers are generated. This class sets up
 * the Xspress3 time frames and reads out the data.
 *
 * @see uk.ac.gda.devices.detector.xspress#Xspress3System
 * @author rjw82
 * <p>
 *         now replaced with Xspress3WithFullCalculationsDetector. This new
 *         class was developed for I18 after problems were seen in the EPICS
 *         layer. This new class is yet to be tested on B18. But it should work
 *         and should be applicable for B18, the classes should be able to be
 *         swapped with no further changes required in the rest of the code.
 *
 */
@Deprecated
public class Xspress3Detector extends DetectorBase implements Xspress3 {

	private static final int MCA_SIZE = 4096;
	public static final String ALL_ELEMENT_SUM_LABEL = "AllElementSum_";
	public static int SUM_ALL_ROI = 0;
	public static int SUM_FIRST_ROI = 1;
	public static int MAX_ROI_PER_CHANNEL = 10;

	protected Xspress3Controller controller;
	private String channelLabelPrefix = "FF channel ";
	private String sumLabel = "FF";
	private String unitsLabel = "counts";
	private int framesRead = 0;
	private int firstChannelToRead = 0;
	private int summingMethod = SUM_ALL_ROI;

	private boolean writeHDF5Files = false;
	private String filePath = "";
	private String filePrefix = "";
	// private String numTrackerExtension = "nxs";
	// private NumTracker numTracker;
	private int currentScanNumber = -1;

	private String configFileName;
	private String[] regionNames;
	private int rowBeingCollected;
	private int[] currentDimensions;

	/**
	 * For a given row in a multiDimensional scan , this returns the name of the
	 * subentry in the Nexus file which contains all the MCAs for that row.
	 * <p>
	 * This will become redundant when SWMR is available and all Mca data can be
	 * placed in the same HDF5 file by the Area Detector EPICS plugin, so there
	 * will be no need to put MCA data in different files and Nexus nodes.
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

	public Xspress3Detector() {
		super();
	}

	@Override
	public void configure() throws FactoryException {
		controller.configure();
		super.configure();
		// if (numTracker == null) {
		// createNumTracker();
		// }
		if (filePrefix.isEmpty() && !getName().isEmpty()) {
			filePrefix = getName();
		}
		inputNames = new String[] {};

		configureExtraNames();
	}

	// private void createNumTracker() {
	// if (numTrackerExtension != null && !numTrackerExtension.isEmpty()) {
	// try {
	// numTracker = new NumTracker(numTrackerExtension);
	// } catch (IOException e) {
	// throw new IllegalArgumentException(
	// "NumTracker with extension '" + numTrackerExtension
	// + "' could not be created.", e);
	// }
	// }
	// }

	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation currentscan = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		currentScanNumber = currentscan.getScanNumber();
		currentDimensions = currentscan.getDimensions();

		int numDimensions = currentscan.getDimensions().length;
		int lengthOfEachScanLine = currentscan.getDimensions()[numDimensions - 1];
		setNumberOfFramesToCollect(lengthOfEachScanLine);
		stop();
		prepareFileWriting(currentDimensions);

		rowBeingCollected = -1;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		framesRead = 0;
		startRunningXspress3FrameSet();
		rowBeingCollected++;

		// for when we have SWMR and can write to a multi-dimensional HDF5 file
		// only start the xspress3 during the first row of a multi-dimensional
		// scan
		// if (newScan){
		// framesRead = 0;
		// startRunningXspress3FrameSet();
		// newScan = false;
		// }
	}

	protected void startRunningXspress3FrameSet() throws DeviceException {
		controller.doErase();
		controller.doStart();
		if (writeHDF5Files) {
			// do not do this if writeHDF5Files is false as may cause errors in
			// epics
			controller.setSavingFiles(writeHDF5Files);
		}
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

	private void prepareFileWriting(int[] numDimensions) throws DeviceException {
		if (writeHDF5Files) {
			// set file path name, number here if known or set

			String scanNumber = getScanNumber();
			String subFolder = "";
			if (currentDimensions.length > 1) {
				subFolder = scanNumber;
			}

			filePath = PathConstructor.createFromRCPProperties();
			filePath += getName() + File.separator + subFolder;
			File filePathTester = new File(filePath);
			if (!filePathTester.exists()) {
				filePathTester.mkdirs();
			}
			controller.setFilePath(filePath);

			if (!scanNumber.isEmpty()) {
				controller.setFilePrefix(scanNumber + "_");
			} else {
				controller.setFilePrefix(getName() + "_");
			}

			controller.setNextFileNumber(0);
			controller.setHDFFileAutoIncrement(true);
			controller.setHDFNumFramesToAcquire(currentDimensions[currentDimensions.length - 1]);

			// for future use with SWMR
			// controller.setNextFileNumber(Integer.parseInt(getScanNumber()));
			// controller.setHDFFileDimensions(numDimensions);
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
	 * Returns the FF (sum of ROI) in the plottable values. May want the option
	 * in the future to return the individual ROI values instead.
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
			throw new DeviceException("Only " + numFramesAvailable + " frames available, cannot return frames "
					+ firstFrame + " to " + lastFrame);
		}

		// readout ROI in format [frame][detector channel][ROIs]
		Double[][][] data = controller.readoutDTCorrectedROI(firstFrame, lastFrame, firstChannelToRead,
				controller.getNumberOfChannels() + firstChannelToRead - 1);
		// calc FF from ROI
		int numFramesRead = lastFrame - firstFrame + 1;
		double[][] FFs = calculateFFs(data, numFramesRead);

		// create trees
		NXDetectorData[] results = new NXDetectorData[numFramesRead];

		for (int frame = 0; frame < numFramesRead; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(this);
			INexusTree detTree = thisFrame.getDetTree(getName());

			// add FF (all ROI, all channels)
			thisFrame.addData(detTree, sumLabel, new NexusGroupData(FFs[frame]), unitsLabel, 1);

			// add regions of interest for each channel
			Double[][] thisFrameData = data[frame];
			for (int region = 0; region < regionNames.length; region++) {
				double[] countsPerChannel = new double[controller.getNumberOfChannels()];

				for (int channel = 0; channel < controller.getNumberOfChannels(); channel++) {
					countsPerChannel[channel] = thisFrameData[channel][region];
				}
				thisFrame.addData(detTree, regionNames[region], new NexusGroupData(countsPerChannel), unitsLabel, 2);
			}

			// // add the FFs as the plottable values (seen in Jython Terminal
			// and ASCII files)
			// for (int chan = 0; chan < controller.getNumberOfChannels();
			// chan++) {
			// thisFrame.setPlottableValue(getExtraNames()[chan],
			// FFs[frame][chan]);
			// }

			// add all the rois as plottable values, plus a final FF.
			int index = 0;
			double ffSum = 0;
			String[] extraNames = getExtraNames();
			for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
				for (int roi = 0; roi < controller.getNumberROIToRead(); roi++) {
					double roiCounts = data[frame][chan][roi];
					ffSum += roiCounts;
					thisFrame.setPlottableValue(extraNames[index], roiCounts);
					index++;
				}
			}
			thisFrame.setPlottableValue("FF", ffSum);

			// Add link to MCA data files
			// Only need to do this for the very first frame of a file. This
			// assumes a 2D scan.
			if (rowBeingCollected == 0 && (frame + firstFrame) == 0) {
				// There will be a new file per row, so create a new link per
				// row.
				// This must be done first time, as new links in later frames
				// will be ignored when data is appended in the Nexus file.
				int numRows = currentDimensions[0];
				String path = controller.getFilePath();
				String prefix = controller.getFilePrefix();

				String allElementPath = path;
				String allElementPrefix = "AllElementSum_"+prefix;

				for (int row = 0; row < numRows; row++) {
					String hdf5FileName = path + prefix + String.format("%04d", row) + ".hdf5";
					String nodeName = getNameOfRowSubNode(row);
					String fullLink = "nxfile://" + hdf5FileName + "#entry/instrument/detector/data";
					thisFrame.addExternalFileLink(getName(), nodeName, fullLink, false, false);

					String allElementFileName = allElementPath + allElementPrefix + String.format("%04d", row) + ".hdf5";
					String allElementNodeName = getNameOfAllElementSumRowSubNode(row);
					String allElementFullLink = "nxfile://" + allElementFileName + "#entry/instrument/detector/data";
					thisFrame.addExternalFileLink(getName(), allElementNodeName, allElementFullLink, false, false);
				}
			}

			results[frame] = thisFrame;
		}
		return results;
	}

	/**
	 * For use by Xspress3FFOverI0Detector only. Not intended for use in continuous scans. Largely duplicates code
	 * used in Xspress3FFoverI0BufferedDetector.getFF().
	 */
	@Override
	public double readoutFF() throws DeviceException {

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

	private double[][] calculateFFs(Double[][][] data, int numFramesRead) {
		double[][] FFs = new double[numFramesRead][controller.getNumberOfChannels()]; // [frame][detector

		// channel]
		for (int frame = 0; frame < numFramesRead; frame++) {
			for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
				if (summingMethod == 1) {
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
		// // these are the plottable values. For this detector it is the FF for
		// // each channel
		// String[] extraNames = new String[controller.getNumberOfChannels()];
		// for (int i = 0; i < controller.getNumberOfChannels(); i++) {
		// extraNames[i] = "Chan" + (firstChannelToRead + i);
		// }
		// return extraNames;

		int numExtraNames = (controller.getNumberROIToRead() * controller.getNumberOfChannels()) + 1;
		String[] extraNames = new String[numExtraNames];
		int index = 0;
		for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
			for (int roi = 0; roi < controller.getNumberROIToRead(); roi++) {
				String valueName = "Chan" + chan + "_" + regionNames[roi];
				extraNames[index] = valueName;
				index++;
			}
		}
		extraNames[numExtraNames - 1] = "FF";

		return extraNames;
	}

	private Double sumArray(Double[] doubles) {
		Double sum = 0.0;
		for (Double element : doubles) {
			sum += element;
		}
		return sum;
	}

	/**
	 * @deprecated Use applyConfigurationParameters() instead
	 */
	@Override
	@Deprecated
	public void setRegionsOfInterest(DetectorROI[] regionList) throws DeviceException {
		if (regionList.length > MAX_ROI_PER_CHANNEL) {
			throw new DeviceException("Too many regions! Only " + MAX_ROI_PER_CHANNEL + " allowed.");
		}

		defineRegionNames(regionList);

		for (int chan = firstChannelToRead; chan < controller.getNumberOfChannels() + firstChannelToRead; chan++) {
			for (int roiNum = 0; roiNum < MAX_ROI_PER_CHANNEL; roiNum++) {
				if (roiNum < regionList.length) {
					controller.setROILimits(chan, roiNum,
							new int[] { regionList[roiNum].getRoiStart(), regionList[roiNum].getRoiEnd() });

				} else {
					controller.setROILimits(chan, roiNum, new int[] { 0, 0 });
				}
			}
		}
		controller.setNumberROIToRead(regionList.length);

	}

	private void defineRegionNames(DetectorROI[] regionList) {
		regionNames = new String[regionList.length];

		for (int region = 0; region < regionList.length; region++) {
			regionNames[region] = regionList[region].getRoiName();
		}
	}

	/**
	 * For the moment, all ROI on all channels are the same, and assumed by this
	 * class to be the same.
	 *
	 * @return ROI[]
	 * @throws DeviceException
	 * @deprecated Use getConfigurationParameters() instead
	 */
	@Override
	@Deprecated
	public DetectorROI[] getRegionsOfInterest() throws DeviceException {
		// assume that the ROIs were defined via this class and so the
		// regionNames array kept in this class has the same size as the regions
		// defined in the controller
		int numRegions = controller.getNumberROIToRead();
		DetectorROI[] rois = new DetectorROI[numRegions];

		for (int roiNum = 0; roiNum < numRegions; roiNum++) {
			rois[roiNum] = new DetectorROI();
			rois[roiNum].setRoiName(regionNames[roiNum]);
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

	public int getFirstChannelToRead() {
		return firstChannelToRead;
	}

	public void setFirstChannelToRead(int firstChannelToRead) {
		this.firstChannelToRead = firstChannelToRead;
	}

	private void configureExtraNames() {
		// this defines the number of extraNames as currently extraNames = FF
		// per channel
		String[] newExtraNames = new String[controller.getNumberOfChannels()];
		String[] newoutputFormat = new String[controller.getNumberOfChannels()];
		if (this.outputFormat.length == 0){
			newoutputFormat[0] = "%.3f";
		} else {
			newoutputFormat[0] = this.outputFormat[0];
		}

		for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
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

		Xspress3Parameters xspress3Parameters = (Xspress3Parameters) XMLHelpers.createFromXML(
				Xspress3Parameters.mappingURL, Xspress3Parameters.class, Xspress3Parameters.schemaURL,
				getConfigFileName());

		applyXspress3ConfigurationParameters(xspress3Parameters);
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		if (parameters instanceof Xspress3Parameters) {
			applyXspress3ConfigurationParameters((Xspress3Parameters)parameters);
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

		for(int i = 0; i < getNumberOfElements(); i++){
			DetectorElement thisElement = new DetectorElement();
			for(DetectorROI region : regions){
				thisElement.addRegion(region);
			}
			detectorList.add(thisElement);
		}

		Xspress3Parameters parameters = new Xspress3Parameters();
		parameters.setDetectorName(getName());
		parameters.setDetectorList(detectorList);

		return parameters;
	}
}
