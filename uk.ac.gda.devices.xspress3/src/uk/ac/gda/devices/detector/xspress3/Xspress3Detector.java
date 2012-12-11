package uk.ac.gda.devices.detector.xspress3;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;

import org.nexusformat.NexusFile;

/**
 * Passive detector which sets up and then reads out from the Xspress3 readout
 * chain via EPICS.
 * <p>
 * By passive I mean that the Xspress3 is an externally triggered system and
 * this class does not cover the triggering.
 * 
 * @author rjw82
 * 
 */
public class Xspress3Detector extends DetectorBase implements NexusDetector {

	public static int SUM_ALL_ROI = 0;
	public static int SUM_FIRST_ROI = 1;

	protected final Xspress3Controller controller;
	private String channelLabelPrefix = "FF channel ";
	private String sumLabel = "FF";
	private String unitsLabel = "counts";
	private int framesRead = 0;
	private int firstChannelToRead = 0;
	private int numberOfChannelsToRead = 1;
	private int summingMethod = SUM_ALL_ROI;

	public Xspress3Detector(Xspress3Controller controller) {
		this.controller = controller;
	}

	@Override
	public void configure() throws FactoryException {
		controller.configure();
		super.configure();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		framesRead = 0;
		controller.doErase();
	}

	@Override
	public void collectData() throws DeviceException {
		controller.doStart();
	}

	@Override
	public void stop() throws DeviceException {
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
			// TODO throw some sort of excpetion as we have run out of data!
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

		// get file name
		// TODO need to create the full filename and then link correctly
		String fileName = controller.getFilePath();

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

}
