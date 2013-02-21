package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ScanInformation;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Xspress3FFCalculatorNXPlugin implements NXPlugin{

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
	
	public Xspress3FFCalculatorNXPlugin(Xspress3Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		// return as much data is available to read out
		int numFramesAvailable = controller.getTotalFramesAvailable();

		// readout ROI in format [frame][detector channel][ROIs]
		Double[][][] data = controller.readoutDTCorrectedROI(framesRead, numFramesAvailable, firstChannelToRead,
				numberOfChannelsToRead + firstChannelToRead - 1);
		// calc FF from ROI
		int numFramesRead = numFramesAvailable - framesRead + 1;
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

		List<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		for (Double[] thisframe : FFs){
			NXDetectorDataDoubleAppender appender = new NXDetectorDataDoubleAppender(getExtraNames(),Arrays.asList(thisframe));
			appenders.add(appender);
		}
		
		return appenders;
	}
	
	public List<String> getExtraNames() {
		// these are the plottable values. For this detector it is the FF for each channel
		String[] extraNames = new String[numberOfChannelsToRead];
		for (int i = 0; i < numberOfChannelsToRead; i++){
			extraNames[i] = "Chan" + (firstChannelToRead + i);
		}
		return Arrays.asList(extraNames);
	}

	public List<String> getFormats() {
		String[] formats = new String[numberOfChannelsToRead];
		for (int i = 0; i < numberOfChannelsToRead; i++){
			formats[i] = "%5.5g";
		}
		return Arrays.asList(formats);
	}
	
	private Double sumArray(Double[] doubles) {
		Double sum = 0.0;
		for (Double element : doubles) {
			sum += element;
		}
		return sum;
	}


	@Override
	public String getName() {
		return "Xspress3FFCalculator";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}

	@Override
	public void prepareForLine() throws Exception {
		framesRead = 0;
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
		return getExtraNames();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return getFormats();
	}

	public String getChannelLabelPrefix() {
		return channelLabelPrefix;
	}

	public void setChannelLabelPrefix(String channelLabelPrefix) {
		this.channelLabelPrefix = channelLabelPrefix;
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

	public int getSummingMethod() {
		return summingMethod;
	}

	public void setSummingMethod(int summingMethod) {
		this.summingMethod = summingMethod;
	}

}
