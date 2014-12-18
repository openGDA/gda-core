package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.data.nexus.extractor.NexusExtractorException;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.factory.Finder;

import java.util.List;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

import uk.ac.gda.beans.vortex.VortexROI;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.xspress3.ROI;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.EpicsXspress3ControllerPvProvider;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class Xspress3DataOperations {

	private static final String mcaLabel = "MCAs";
	private final String sumLabel = "FF";
	private final String unitsLabel = "counts";
	private final String allElementSumLabel = "AllElementSum";
	
	private Xspress3Controller controller;
	private int firstChannelToRead;
	private int framesRead;
	// private int rowBeingCollected;
	private String configFileName;
	private ROI[] rois;
	private String detectorName;


	public Xspress3DataOperations(Xspress3Controller controller, String detectorName, int firstChannelToRead) {
		this.controller = controller;
		this.detectorName = detectorName;
		this.firstChannelToRead = firstChannelToRead;
	}

	public String[] getOutputFormat() {
		int numNames = getExtraNames().length;
		String[] outputFormat = new String[numNames];
		for (int i = 0; i < numNames; i++) {
			outputFormat[i] = "%.3f";
		}
		return outputFormat;
	}

	public void atScanStart(boolean readDataFromFile) throws DeviceException {
		// rowBeingCollected = -1;
		if (readDataFromFile) {
			// we are in a Continuous / Fly scan, so data will be readback from
			// the HDF file at the end of each scan line
			disableAllEPICSCalculations();
		} else {
			// data to be read from EPICS MCA PVs directly
			enableEpicsMcaStorage();
		}
	}

	public void atScanLineStart() throws DeviceException {
		framesRead = 0;
		// rowBeingCollected++;
	}

	public void atPointEnd() throws DeviceException {
		if (controller.getNumFramesToAcquire() > 1) {
			framesRead++;
		}
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

		Xspress3Parameters vortexParameters = (Xspress3Parameters) XMLHelpers.createFromXML(
				Xspress3Parameters.mappingURL, Xspress3Parameters.class, Xspress3Parameters.schemaURL,
				getConfigFileName());

		List<VortexROI> vortexRois = vortexParameters.getDetector(0).getRegionList();
		rois = new ROI[vortexRois.size()];
		for (int index = 0; index < vortexRois.size(); index++) {
			rois[index] = new ROI(vortexRois.get(index).getRoiName(), vortexRois.get(index).getRoiStart(), vortexRois
					.get(index).getRoiEnd());
		}
	}

	private void disableAllEPICSCalculations() throws DeviceException {
		int numChannels = controller.getNumberOfChannels();
		for (int channel = 0; channel > numChannels; channel++) {
			controller.enableChannel(channel, false);
		}
	}

	/*
	 * Enable the visualisation of all the EPICS channels, but ensure that all
	 * the ROIs are empty, so only the latest MCAs are kept and visualisation is
	 * at a minimum.
	 */
	private void enableEpicsMcaStorage() throws DeviceException {
		int numChannels = controller.getNumberOfChannels();
		for (int channel = 0; channel > numChannels; channel++) {
			controller.enableChannel(channel, true);
			controller.setWindows(channel, 1, new int[] { 0, 0 });
			controller.setWindows(channel, 2, new int[] { 0, 0 });
			for (int roi = 0; roi > EpicsXspress3ControllerPvProvider.NUMBER_ROIs; roi++) {
				controller.setROILimits(numChannels, roi, new int[] { 0, 0 });
			}
		}
	}

	public NexusTreeProvider readoutLatest() throws DeviceException {

		// this method is for step scan readout and so it assumed that the data
		// has been collected by the time this method is called

		try {
			// yuck, but unavoidable to prevent a race condition. The Xspress3
			// is driven by the TFG, but we
			// are
			// looking at the EPICS layer which will be a bit behind the gate
			// signals sent to the xspress3 electronics, so need a delay until
			// the available frames PV (and therefore all the data PVs) has been
			// updated
			Thread.sleep(150);
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException during readout.");
		}

		// sanity check
		if (framesRead == controller.getTotalFramesAvailable()) {
			// we have run out of data! This method should not have been called.
			// Problem in the logic somewhere.
			throw new DeviceException("Cannot readout - no more data in buffer");
		}
		return readoutLatestFrame();
	}

	private NexusTreeProvider readoutLatestFrame() throws DeviceException {
		double[][] data = controller.readoutDTCorrectedLatestMCA(0, controller.getNumberOfChannels());
		return createNexusTreeForFrame(data);
	}

	private NexusTreeProvider createNexusTreeForFrame(double[][] mcasFromFile) {
		int numChannels = mcasFromFile.length;
		int numRois = rois.length;

		double[][] roiValues = new double[numRois][numChannels];
		// double[] ffs = new double[numChannels];
		double theFF = 0;
		for (int chan = 0; chan < numChannels; chan++) {
			for (int roi = 0; roi < numRois; roi++) {
				double thisRoiSum = extractRoi(mcasFromFile[chan], rois[roi].getStart(), rois[roi].getEnd());
				roiValues[roi][chan] = thisRoiSum;
				// ffs[chan] += thisRoiSum;
				theFF += thisRoiSum;
			}
		}
		
		int numMcaElements = mcasFromFile[0].length;
		double [] allElementSum = new double[numMcaElements];
		for (int element = 0; element < numMcaElements; element++){
			for  (int chan = 0; chan < numChannels; chan++) {
				allElementSum[element] += mcasFromFile[chan][element];
			}
		}
		

		NXDetectorData thisFrame = new NXDetectorData(getExtraNames(), getOutputFormat(), detectorName);
		INexusTree detTree = thisFrame.getDetTree(detectorName);

		// add the FF (sum over all rois, over all channels)
		thisFrame.addData(detTree, sumLabel, new int[] { 1 }, NexusFile.NX_FLOAT64, theFF, unitsLabel, 1);

		// add rois
		for (int roi = 0; roi < numRois; roi++) {
			thisFrame.addData(detTree, rois[roi].getName(), new int[] { numChannels }, NexusFile.NX_FLOAT64,
					roiValues[roi], unitsLabel, 2);
		}

		// add MCAs
		thisFrame.addData(detTree, mcaLabel, new int[] { numChannels, numMcaElements }, NexusFile.NX_FLOAT64, mcasFromFile,
				unitsLabel, 2);
		
		// add all element sum
		thisFrame.addData(detTree, allElementSumLabel, new int[] { numMcaElements }, NexusFile.NX_FLOAT64, allElementSum,
				unitsLabel, 2);

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

	public NexusTreeProvider[] readoutFrames(int firstFrame, int lastFrame) throws DeviceException {

		int numFramesAvailable = controller.getTotalFramesAvailable();
		if (lastFrame > numFramesAvailable) {
			throw new DeviceException("Only " + numFramesAvailable + " frames available, cannot return frames "
					+ firstFrame + " to " + lastFrame);
		}

		// this trusts that the file handle is available to read from
//		NexusFile file = null;
		try {
//			file = new NexusFile(filename, NexusFile.NXACC_READ);
			double[][][] mcasFromFile = extractMCAsFromFile(controller.getFullFileName(), firstFrame, lastFrame);
			NexusTreeProvider[] frames = new NexusTreeProvider[mcasFromFile.length];
			int numFrames = lastFrame - firstFrame + 1;
			for (int frame = 0; frame < numFrames; frame++) {
				frames[frame] = createNexusTreeForFrame(mcasFromFile[frame]);
			}
			return frames;
		} catch (Exception e) {
			throw new DeviceException(e.getMessage());
		} 
	}

	private double[][][] extractMCAsFromFile(String filename, int firstFrame, int lastFrame) throws NexusException, NexusExtractorException {
		Xspress3FileReader reader = new Xspress3FileReader(filename,controller.getNumberOfChannels(), EpicsXspress3ControllerPvProvider.MCA_SIZE);
		double[][][] mcas = reader.readFrames(firstFrame,lastFrame);
		return mcas;
	}

	public Double[] readoutFF() throws DeviceException {
		// assume that this is readout before the full readout() is called!!
		Double[][][] data = controller.readoutDTCorrectedROI(framesRead, framesRead, firstChannelToRead,
				controller.getNumberOfChannels() + firstChannelToRead - 1);
		return calculateFFs(data, 1)[0];
	}

	private Double[][] calculateFFs(Double[][][] data, int numFramesRead) {
		Double[][] FFs = new Double[numFramesRead][controller.getNumberOfChannels()]; // [frame][detector
		// channel]
		for (int frame = 0; frame < numFramesRead; frame++) {
			for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
				FFs[frame][chan] = sumArray(data[frame][chan]);
			}
		}
		return FFs;
	}

	public String[] getExtraNames() {
		int numExtraNames = (rois.length * controller.getNumberOfChannels()) + 1;
		String[] extraNames = new String[numExtraNames];
		int index = 0;
		for (int chan = 0; chan < controller.getNumberOfChannels(); chan++) {
			for (int roi = 0; roi < rois.length; roi++) {
				String valueName = "Chan" + chan + "_" + rois[roi].getName();
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

	public int[][] getData() throws DeviceException {

		double[][] deadTimeCorrectedData = controller.readoutDTCorrectedLatestMCA(firstChannelToRead,
				controller.getNumberOfChannels() - 1);
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
	public double[][] getMCData(double time) throws DeviceException {
		controller.doErase();
		controller.doStart();
		((Timer) Finder.getInstance().find("tfg")).clearFrameSets(); // we only
																		// want
																		// to
																		// collect
																		// a
																		// frame
																		// at a
																		// time
		((Timer) Finder.getInstance().find("tfg")).countAsync(time); // run tfg
																		// for
																		// time
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

}
