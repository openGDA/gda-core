package uk.ac.gda.devices.detector.xspress3;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;

import java.io.IOException;

import org.nexusformat.NexusFile;

/**
 * Passive detector which sets up and then reads out from the Xspress3 readout
 * chain via EPICS.
 * <p>
 * By passive I mean that the Xspress3 is an externally triggered system and
 * this class does not cover the triggering but sets up the time frames and
 * reads out the data.
 * 
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
	private int fileNumber = -1;
	private String numTrackerExtension = "nxs";
	private NumTracker numTracker;
	
	public Xspress3Detector(){
		
	}

	public Xspress3Detector(Xspress3Controller controller) {
		super();
		this.controller = controller;
	}

	@Override
	public void configure() throws FactoryException {
		controller.configure();
		super.configure();
		if (numTracker == null) {
			createNumTracker();
		}
	}

	private void createNumTracker() {
		try {
			numTracker = new NumTracker(numTrackerExtension);
		} catch (IOException e) {
			throw new IllegalArgumentException("NumTracker with extension '"
					+ numTrackerExtension + "' could not be created.", e);
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		framesRead = 0;
		initiateFileWriting();
		controller.doErase();
	}

	private void initiateFileWriting() throws DeviceException {
		if (writeHDF5Files) {
			// set file path name, number here if known or set
			if (filePath != null && !filePath.isEmpty()) {
				controller.setFilePath(filePath);
			} else {
				controller.setFilePath(PathConstructor
						.createFromDefaultProperty());
			}

			if (filePrefix != null && !filePrefix.isEmpty()) {
				controller.setFilePrefix(filePrefix);
			} else {
				controller.setFilePrefix("xspress3");
			}

			controller.setNextFileNumber((int) numTracker
					.getCurrentFileNumber() + 1);
		}
		controller.setSavingFiles(writeHDF5Files);
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		// clear stored file path prefix and number at end of scan
		if (writeHDF5Files) {
			controller.setFilePath("");
			controller.setFilePrefix("");
			controller.setNextFileNumber(0);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		startRunningXspress3FrameSet();
	}

	protected void startRunningXspress3FrameSet() throws DeviceException {
		controller.doStart();
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
		return controller.getStatus();
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

		if (framesRead == controller.getNumFramesToAcquire()) {
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
	 * Currently only look at ROI, not the scalers.
	 * <p>
	 * Has a link to the HDF5 file created by underlying controller instead of
	 * adding more data to this Nexus tree.
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
						+ firstChannelToRead);
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
				String label = channelLabelPrefix + chan + firstChannelToRead;
				thisFrame.setPlottableValue(label, FFs[frame][chan]);
			}
			results[frame] = thisFrame;
		}
		return results;
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
				if (roiNum < regionList.length){
				controller.setROILimits(chan, roiNum,
						new int[] { regionList[roiNum].getStart(),
								regionList[roiNum].getEnd() });
				} else {
					controller.setROILimits(chan, roiNum,
							new int[] {0,0});
				}
			}
		}
		controller.setNumberROIToRead(regionList.length + 1);
	}
	
	/**
	 * For the moment, all ROI on all channels are the same, and assumed by this class to be the same.
	 * 
	 * @return ROI[]
	 * @throws DeviceException 
	 */
	public ROI[] getRegionsOfInterest() throws DeviceException {
		ROI[] rois = new ROI[controller.getNumberROIToRead()];
		
		for (int roiNum = 0; roiNum < rois.length; roiNum++) {
			rois[roiNum] = new ROI();
			rois[roiNum].setName("ROI"+roiNum);
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

	public int getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(int fileNumber) {
		this.fileNumber = fileNumber;
	}

	public String getNumTrackerExtension() {
		return numTrackerExtension;
	}

	public void setNumTrackerExtension(String numTrackerExtension) {
		this.numTrackerExtension = numTrackerExtension;
		createNumTracker();
	}

}
