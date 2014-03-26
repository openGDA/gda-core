package uk.ac.gda.devices.detector.xspress3;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.io.IOException;

import org.nexusformat.NexusFile;

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
 * 
 */
public class Xspress3Detector extends DetectorBase implements NexusDetector {

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
	private Long currentScanNumber = -1L;

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
				throw new IllegalArgumentException(
						"NumTracker with extension '" + numTrackerExtension
								+ "' could not be created.", e);
			}
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation currentscan = InterfaceProvider
				.getCurrentScanInformationHolder().getCurrentScanInformation();
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
		currentScanNumber = -1L;
	}

	private void prepareFileWriting() throws DeviceException {
		if (writeHDF5Files) {
			// set file path name, number here if known or set
			if (filePath != null && !filePath.isEmpty()) {
				controller.setFilePath(filePath);
			} else {
				controller.setFilePath(PathConstructor
						.createFromDefaultProperty());
			}

			if (filePrefix != null && !filePrefix.isEmpty()) {
				String scanNumber = getScanNumber();
				if (!scanNumber.isEmpty()) {
					scanNumber = "_" + scanNumber;
				}
				controller.setFilePrefix(filePrefix + scanNumber);
			} else {
				controller.setFilePrefix("xspress3");
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
		if (controllerStatus == Detector.FAULT
				|| controllerStatus == Detector.STANDBY) {
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
		NexusTreeProvider tree = readoutFrames(framesRead, framesRead)[0];
		if (controller.getNumFramesToAcquire() > 1) {
			framesRead++;
		}
		return tree;
	}

	/**
	 * Currently only looks at ROI, not the scaler windows.
	 * <p>
	 * Returns the FF (sum of ROI) in the plottable values. May want the option
	 * in the future to return the individual ROI values instead.
	 * <p>
	 * TODO add a link to the HDF5 file created by underlying controller instead
	 * of adding more data to this Nexus tree.
	 * 
	 * @param firstFrame
	 * @param lastFrame
	 * @return NexusTreeProvider array for every frame
	 * @throws DeviceException
	 */
	public NexusTreeProvider[] readoutFrames(int firstFrame, int lastFrame)
			throws DeviceException {
		int numFramesAvailable = controller.getTotalFramesAvailable();
		if (lastFrame > numFramesAvailable) {
			throw new DeviceException("Only " + numFramesAvailable
					+ " frames available, cannot return frames " + firstFrame
					+ " to " + lastFrame);
		}

		// readout ROI in format [frame][detector channel][ROIs]
		Double[][][] data = controller.readoutDTCorrectedROI(firstFrame,
				lastFrame, firstChannelToRead, numberOfChannelsToRead
						+ firstChannelToRead - 1);
		// calc FF from ROI
		int numFramesRead = lastFrame - firstFrame + 1;
		Double[][] FFs = new Double[numFramesRead][numberOfChannelsToRead]; // [frame][detector
																			// channel]
		for (int frame = 0; frame < numFramesRead; frame++) {
			for (int chan = 0; chan < numberOfChannelsToRead; chan++) {
				if (summingMethod == 1) {
					FFs[frame][chan] = data[frame][chan][0];
				} else {
					FFs[frame][chan] = sumArray(data[frame][chan]);
				}
			}
		}

		// create trees
		NexusTreeProvider[] results = new NexusTreeProvider[numFramesRead];

		for (int frame = 0; frame < numFramesRead; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(this);
			INexusTree detTree = thisFrame.getDetTree(getName());
			thisFrame.addData(detTree, sumLabel,
					new int[] { numberOfChannelsToRead }, NexusFile.NX_FLOAT64,
					FFs[frame], unitsLabel, 1);
			for (int chan = 0; chan < numberOfChannelsToRead; chan++) {
				thisFrame.setPlottableValue(getExtraNames()[chan],
						FFs[frame][chan]);
			}
			results[frame] = thisFrame;
		}
		return results;
	}

	@Override
	public String[] getExtraNames() {
		// these are the plottable values. For this detector it is the FF for
		// each channel
		String[] extraNames = new String[numberOfChannelsToRead];
		for (int i = 0; i < numberOfChannelsToRead; i++) {
			extraNames[i] = "Chan" + (firstChannelToRead + i);
		}
		return extraNames;
	}

	private Double sumArray(Double[] doubles) {
		Double sum = 0.0;
		for (Double element : doubles) {
			sum += element;
		}
		return sum;
	}

	public void setRegionsOfInterest(ROI[] regionList) throws DeviceException {
		if (regionList.length > MAX_ROI_PER_CHANNEL) {
			throw new DeviceException("Too many regions! Only "
					+ MAX_ROI_PER_CHANNEL + " allowed.");
		}
		for (int chan = firstChannelToRead; chan < numberOfChannelsToRead
				+ firstChannelToRead; chan++) {
			for (int roiNum = 0; roiNum < MAX_ROI_PER_CHANNEL; roiNum++) {
				if (roiNum < regionList.length) {
					controller.setROILimits(chan, roiNum,
							new int[] { regionList[roiNum].getStart(),
									regionList[roiNum].getEnd() });
				} else {
					controller.setROILimits(chan, roiNum, new int[] { 0, 0 });
				}
			}
		}
		controller.setNumberROIToRead(regionList.length);
	}

	/**
	 * For the moment, all ROI on all channels are the same, and assumed by this
	 * class to be the same.
	 * 
	 * @return ROI[]
	 * @throws DeviceException
	 */
	public ROI[] getRegionsOfInterest() throws DeviceException {
		ROI[] rois = new ROI[controller.getNumberROIToRead()];

		for (int roiNum = 0; roiNum < rois.length; roiNum++) {
			rois[roiNum] = new ROI();
			rois[roiNum].setName("ROI" + roiNum);
			Integer[] limits = controller.getROILimits(0, roiNum);
			rois[roiNum].setStart(limits[0]);
			rois[roiNum].setEnd(limits[1]);
		}
		return rois;
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

	public void setNumberOfFramesToCollect(int numberOfFramesToCollect)
			throws DeviceException {
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

}
