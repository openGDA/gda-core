/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.xspress;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorDeadTimeElement;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Represents a set of Xspress1 boards and detectors. Actually communicates with an DAServer object.
 */
public class Xspress1System extends XspressSystem implements XspressDetector, FluorescenceDetector {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Xspress1System.class);
	public static final int READOUT_FILE = 0;
	public static final int READOUT_WINDOWED = 1;

	private XspressDetectorImpl xspressDetectorImpl;
	private Timer timer = null;
	private int framesRead = 0;
	private XspressDeadTimeParameters xspressDeadTimeParameters;
	private int numberOfDetectors;
	public static int MCA_SIZE = 65536;
	private int maxNumberOfRois = 1; // The code currently uses one ROI to represent the detector window


	public Xspress1System() {
		this.inputNames = new String[] {};
	}

	@Override
	public void configure() throws FactoryException {
		if (xspressDetectorImpl == null) {
			logger.error("Xspress1System.configure(): xspressDetectorImpl not found");
			throw new FactoryException("Xspress1System.configure(): xspressDetectorImpl not found");
		}
		if (timer == null) {
			logger.error("Xspress1System.configure(): Time Frame Generator not found");
			throw new FactoryException("Xspress1System.configure(): Time Frame Generator not found");
		}

		try {
			numberOfDetectors= xspressDetectorImpl.getNumberOfDetectors();
			loadAndInitializeDetectors(configFileName,dtcConfigFileName);
		} catch (Exception e) {
			logger.error(
					"Error loading template XML. Will use a detector with a few default elements instead. "
							+ e.getMessage(), e);
			ArrayList<DetectorROI> regions = new ArrayList<DetectorROI>();
			xspressParameters = new XspressParameters();
			xspressParameters.setReadoutMode(READOUT_SCALERONLY);
			xspressParameters.setResGrade(ResGrades.NONE);
			xspressDeadTimeParameters = new XspressDeadTimeParameters();
			xspressParameters.addDetectorElement(new DetectorElement("Element0", 0, 0, 4000,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element1", 1, 85, 2047,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element2", 2, 34, 2439,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element3", 3, 31, 2126,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element4", 4, 0, 4000, true,
					regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element5", 5, 85, 2047,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element6", 6, 34, 2439,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element7", 7, 31, 2126,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element8", 8, 31, 2126,
					false, regions));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen0", 0,3.4E-7, 0.0, 3.4E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen1", 1,3.8E-7, 0.0, 3.8E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen2", 2,3.7E-7, 0.0, 3.7E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen3", 3,3.0E-7, 0.0, 3.0E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen4", 4,3.4E-7, 0.0, 3.4E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen5", 5,3.5E-7, 0.0, 3.5E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen6", 6,3.3E-7, 0.0, 3.3E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen7", 7,3.0E-7, 0.0, 3.0E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen8", 8,3.3E-7, 0.0, 3.3E-7));
			numberOfDetectors = xspressParameters.getDetectorList().size();
		}
		// If everything has been found send the format, region of interest, windows & open commands.
		if (timer != null && xspressDetectorImpl != null) {
			try {
				xspressDetectorImpl.configure();
				configureDetectorFromParameters();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(), e);
			}
		}
		configured = true;
	}

	public XspressDetectorImpl getXspressDetectorImpl() {
		return xspressDetectorImpl;
	}

	public void setXspressDetectorImpl(XspressDetectorImpl xspressDetectorImpl) {
		this.xspressDetectorImpl = xspressDetectorImpl;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	@Override
	public int getNumberOfDetectors() throws DeviceException {
		return xspressDetectorImpl.getNumberOfDetectors();
	}

	private void configureDetectorFromParameters() throws DeviceException {
		for (DetectorElement detector : xspressParameters.getDetectorList()) {
			DetectorROI detectorRoi = detector.getRegionList().get(0);
			xspressDetectorImpl.setWindows(detector.getNumber(), detectorRoi.getRoiStart(), detectorRoi.getRoiEnd());
		}
	}

	/**
	 * Reads the detector windows, gains etc from file.
	 *
	 * @param filename
	 *            detector window setup filename
	 * @throws Exception
	 */
	@Override
	public void loadAndInitializeDetectors(String filename) throws Exception {
		xspressParameters = (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL,
				XspressParameters.class, XspressParameters.schemaURL, filename);
		if (xspressParameters != null) {
			logger.debug("loaded parameters for " + xspressParameters.getDetectorName());
			numberOfDetectors = xspressParameters.getDetectorList().size();
		}
	}

	public void loadAndInitializeDetectors(String filename, String dtcConfigFileName) throws Exception {
		loadAndInitializeDetectors(filename);
		xspressDeadTimeParameters = (XspressDeadTimeParameters)XMLHelpers.createFromXML(XspressDeadTimeParameters.mappingURL,
				XspressDeadTimeParameters.class, XspressDeadTimeParameters.schemaURL, dtcConfigFileName);
	}

	@Override
	public void collectData() throws DeviceException {
		// if timer not running with frames then clear and start the xspress memory
		if (timer.getAttribute("TotalFrames").equals(0)) {
			clear();
			start();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return timer.getStatus();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Xspress system version 1";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Version 1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Xspress1System";
	}

	@Override
	public void stop() throws DeviceException {
		xspressDetectorImpl.stop();
	}

	@Override
	public String[] getExtraNames() {
		return getChannelLabels().toArray(new String[0]);
	}

	@Override
	public void atScanStart() throws DeviceException {
		stop();
		clear();
		start();
		framesRead = 0;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		// if this class was used to define framesets, then memeory is only cleared at the start of the scan
		if (!timer.getAttribute("TotalFrames").equals(0)) {
			stop();
			clear();
			start();
		}
		framesRead = 0;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if (attributeName.equals(ONLY_DISPLAY_FF_ATTR)) {
			Boolean ffonly = Boolean.parseBoolean(value.toString());
			this.onlyDisplayFF = ffonly;
		} else {
			super.setAttribute(attributeName, value);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals("liveStats")) {
			return calculateLiveStats();
		} else if (attributeName.equals(ONLY_DISPLAY_FF_ATTR)) {
			return onlyDisplayFF;
		}
		return null;
	}

	@Override
	public void close() throws DeviceException {
		xspressDetectorImpl.close();
	}

	@Override
	public void reconfigure() throws FactoryException {
		try {
			xspressDetectorImpl.reconfigure();
		} catch(DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void start() throws DeviceException {
		xspressDetectorImpl.start();
	}

	@Override
	public void clear() throws DeviceException {
		xspressDetectorImpl.clear();
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack properly
	 */
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NexusTreeProvider out = readout(framesRead, framesRead)[0];
		if (!timer.getAttribute("TotalFrames").equals(0)) {
			framesRead++;
		}
		return out;
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack properly
	 */
	public NexusTreeProvider[] readout(int startFrame, int finalFrame) throws DeviceException {

		int numberOfFrames = finalFrame - startFrame + 1;
		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];

		int[] rawscalerData = xspressDetectorImpl.readoutHardwareScalers(startFrame, numberOfFrames);
		long[][] unpackedScalerData = unpackRawScalerDataToFrames(rawscalerData, numberOfFrames);


		// scaler only mode
		if (xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_SCALERONLY)) {
			double[][] scalerData = readoutScalerData(numberOfFrames, unpackedScalerData, true);
			for (int frame = 0; frame < numberOfFrames; frame++) {
				NXDetectorData thisFrame = new NXDetectorData(this);
				INexusTree detTree = thisFrame.getDetTree(getName());
				// do not use numberOfDetectors here so all information in the array is added to Nexus (i.e. FF)
				NXDetectorData.addData(detTree, "scalers", new NexusGroupData(ArrayUtils.subarray(scalerData[frame], 0, numberOfDetectors)), "counts", 1);
				thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
				results[frame] = thisFrame;

			}
		}
		return results;
	}

	protected NXDetectorData addExtraInformationToNexusTree(long[][] unpackedScalerData, double[][] scalerData,
			int frame, NXDetectorData thisFrame, INexusTree detTree) {
		thisFrame = addFFIfPossible(detTree, thisFrame, scalerData[frame]);
		thisFrame = fillNXDetectorDataWithScalerData(thisFrame, scalerData[frame], unpackedScalerData[frame]);
		thisFrame = addDTValuesToNXDetectorData(thisFrame, unpackedScalerData[frame]);
		return thisFrame;
	}

	private NXDetectorData addDTValuesToNXDetectorData(NXDetectorData thisFrame, long[] unpackedScalerData) {
		// always add raw scaler values to nexus data
		thisFrame.addData(getName(), "raw scaler values",
				new NexusGroupData(unpackedScalerData).asInt(), "counts", 1);

		return thisFrame;
	}

	/*
	 * Adds FF to the Nexus data where defined in the ascii data
	 */
	private NXDetectorData addFFIfPossible(INexusTree detTree, NXDetectorData thisFrame, double[] ds) {
		ArrayList<String> elementNames = new ArrayList<String>();
		getUnFilteredChannelLabels(elementNames);
		int ffColumn = elementNames.indexOf("FF");

		if (elementNames.size() == ds.length && ffColumn > -1) {
			NXDetectorData.addData(detTree, "FF", new NexusGroupData(ds[ffColumn]), "counts", 1);
		}
		return thisFrame;
	}

	/*
	 * Adds to the output the 'ascii' data which is the values which will be displayed in the Jython Terminal, plotting
	 * and ascii file.
	 */
	private NXDetectorData fillNXDetectorDataWithScalerData(NXDetectorData thisFrame, double[] scalerData,
			long[] rawScalervalues) {
		// only add FF
		if (onlyDisplayFF) {
			ArrayList<String> elementNames = new ArrayList<String>();
			getUnFilteredChannelLabels(elementNames);
			int ffColumn = elementNames.indexOf("FF");
			scalerData = new double[] { scalerData[ffColumn] };
			// names = new String[] { "FF" };
		}

		// add the raw scaler values used to calculate the deadtime to ascii
		if (addDTScalerValuesToAscii) {
			double[] dblRawScalerValues = new double[rawScalervalues.length];
			for (int i = 0; i < rawScalervalues.length; i++) {
				dblRawScalerValues[i] = rawScalervalues[i];
			}
			scalerData = ArrayUtils.addAll(scalerData, dblRawScalerValues);
		}

		String[] names = getExtraNames();
		for (int i = 0; i < names.length; i++) {
			thisFrame.setPlottableValue(names[i], scalerData[i]);
		}

		return thisFrame;
	}

	/**
	 * Gets the multi-channel data for all elements. Includes setup of the detector etc. Reads from one frame and tfg
	 * counts for time passed in (suggest 1000ms). Reads 1 time frame.
	 *
	 * @param time
	 *            the time to count for (milliseconds)
	 * @return array[numberOfDetectors][mcaChannels] of int values representing the counts in each channel.
	 * @throws DeviceException
	 */
	@Override
	public int[][][] getMCData(int time) throws DeviceException {
		// Note We dont have resgrades so this is set to 1
		// for compatibility with xspressDetector interface
		int[] data = null;
		int[][][] output = new int[numberOfDetectors][1][MCA_SIZE];
		for (int detector = 0; detector < numberOfDetectors; detector++) {
			logger.debug("Reading mca detector " + detector);
			xspressDetectorImpl.setCollectionTime(time);
			data = xspressDetectorImpl.readoutMca(detector, 0, 1, MCA_SIZE - 1); // NOTE 1 time frame
			if (data != null) {
				for (int energy = 0; energy < MCA_SIZE - 1; energy++) {
					output[detector][0][energy] = data[energy];
				}
			}
		}
		return output;
	}

	@Override
	public int[] getRawScalerData() throws DeviceException {
		return xspressDetectorImpl.readoutHardwareScalers(0, 1);
	}

	public double[] readoutScalerDataNoCorrection() throws DeviceException {
		if (timer.getAttribute("TotalFrames").equals(0)) {
			return readoutScalerData(0, 0, false)[0];
		}
		return readoutScalerData(framesRead, framesRead, false)[0];
	}

	@Override
	public double[] readoutScalerData() throws DeviceException {
		if (timer.getAttribute("TotalFrames").equals(0)) {
			return readoutScalerData(0, 0, true)[0];
		}
		return readoutScalerData(framesRead, framesRead, true)[0];
	}

	private double[][] readoutScalerData(int startFrame, int finalFrame, boolean performCorrections) throws DeviceException {
		int numFrames = finalFrame - startFrame + 1;
		int[] rawScalerData = xspressDetectorImpl.readoutHardwareScalers(startFrame, numFrames);
		long[][] unpackedScalerData = unpackRawScalerDataToFrames(rawScalerData, numFrames);
		return readoutScalerData(numFrames, unpackedScalerData, performCorrections);
	}

	/*
	 * Basically what goes into the Ascii file. Columns should match the values from getExtraNames() or
	 * getUnFilteredChannelLabels()
	 */
	private double[][] readoutScalerData(int numFrames, long[][] unpackedScalerData, boolean performCorrections) throws DeviceException {

		double[][] scalerData = new double[numFrames][];
		long collectionTime = (long) timer.getAttribute("TotalExptTime");

		for (int frame = 0; frame < numFrames; frame++) {
			scalerData[frame] = new double[numberOfDetectors];
			int counter = 0;
			for (int element = 0; element < numberOfDetectors; element++) {
				if (!xspressParameters.getDetector(element).isExcluded()) {
					if (performCorrections) {
						long windowed = unpackedScalerData[frame][counter];
						long total = unpackedScalerData[frame][counter + 2];
						long resets = unpackedScalerData[frame][counter + 3];
						double deadtime = xspressDeadTimeParameters.getDetectorDT(element).getProcessDeadTimeInWindow();
						scalerData[frame][element] = relinearize(total, resets, windowed, deadtime, collectionTime);
					} else {
						scalerData[frame][element] = unpackedScalerData[frame][counter + 3];
					}
				} else {
					scalerData[frame][element] = 0.0;
				}
				counter += 4;
			}

			double ff = 0;
			for (double value : scalerData[frame]) {
				ff += value;
			}
			scalerData[frame] = ArrayUtils.add(scalerData[frame], ff);

		}
		return scalerData;
	}

	private long[][] unpackRawScalerDataToFrames(int[] scalerData, int numFrames) {

		int numberDataPerFrame = 4 * numberOfDetectors;
		long[][] unpacked = new long[numFrames][numberDataPerFrame];
		long value;
		int iterator = 0;

		for (int frame = 0; frame < numFrames; frame++) {
			for (int datum = 0; datum < numberDataPerFrame; datum++) {
				value = scalerData[iterator];
				if (value < 0) {
					value = (value << 32) >>> 32;
				}
				unpacked[frame][datum] = value;
				iterator++;
			}
		}
		return unpacked;
	}

	/**
	 * Rescales the given counts to take into account dead time etc and creates a DetectorReading with the values
	 *
	 * @param total the original total counts read from the detector
	 * @param resets the number of resets counted
	 * @param windowed the original number of counts in the window
	 * @param collectionTime the counting time for the point (secs)
	 * @return the relinearized windowed counts
	 */
	public int relinearize(long total, long resets, long windowed, double deadTime, double collectionTime) {
		// A, B, D have no real meaning they are just used to split
		// the rather unwieldy expression into manageable parts.Contact
		// the Detector Group for information about the details of the
		// expression.
		double A;
		double B;
		double D;
		double factor;
		double working;
		double deadTimeSquared;
		double deadTimeCubed;
		double bigfactor;

		if (windowed <= 0 || total <= 0) {
			return (0);
		}
		A = (double)total / collectionTime;
		B = (double)resets / collectionTime;
		D = (double)windowed / collectionTime;

		factor = (1.0 / (1.0 - B * 1.0e-07));

		A = factor * A;
		D = factor * D;

		deadTimeSquared = deadTime * deadTime;
		deadTimeCubed = deadTime * deadTimeSquared;

		bigfactor = Math.sqrt(4.0 - 20.0 * deadTime * A + 27.0 * deadTimeSquared * A * A);
		bigfactor = bigfactor * Math.sqrt(3.0) / (9.0 * deadTimeCubed);
		bigfactor = bigfactor - 10.0 / (27.0 * deadTimeCubed) + A / deadTimeSquared;
		bigfactor = Math.pow(bigfactor, 1.0 / 3.0);

		working = (bigfactor - 2.0 / (9.0 * deadTimeSquared * bigfactor) + 2.0 / (3.0 * deadTime)) / A;

		working = working * D;
		working = working * collectionTime;
		return (int) working;
	}


	/**
	 * For use by the TFGXspress class. This defines what is returned by the readoutScalers method.
	 */
	@Override
	public ArrayList<String> getChannelLabels() {
		ArrayList<String> channelLabels = new ArrayList<String>();
		if (onlyDisplayFF) {
			channelLabels.add("FF");
		} else {
			getUnFilteredChannelLabels(channelLabels);
		}
		return channelLabels;
	}

	private void getUnFilteredChannelLabels(ArrayList<String> channelLabels) {
		for (DetectorElement detector : xspressParameters.getDetectorList()) {
			channelLabels.add(detector.getName());
		}
		channelLabels.add("FF");
	}

	@Override
	public int getNumberofGrades() {
		return 1;
	}

	@Override
	public String getResGrade() throws DeviceException {
		return xspressParameters.getResGrade();
	}

	@Override
	public void setResGrade(String grade) throws DeviceException {
		// not used
		xspressParameters.setResGrade("res-none");
	}

	@Override
	public String getReadoutMode() throws DeviceException {
		return xspressParameters.getReadoutMode();
	}

	@Override
	public void setReadoutMode(String readoutMode) throws DeviceException {
		xspressParameters.setReadoutMode(XspressDetector.READOUT_SCALERONLY);
	}

	/**
	 * @return double[] - for every element, return the total count rate, deadtime correction factor and in-window count
	 *         rate
	 * @throws DeviceException
	 */
	private Object calculateLiveStats() throws DeviceException {
		int[] rawScalerData = xspressDetectorImpl.readoutHardwareScalers(0, 1);
		long[][] unpackedScalerData = unpackRawScalerDataToFrames(rawScalerData, 1);
		double[] scalerData = readoutScalerData(1, unpackedScalerData, true)[0];
		Double[] results = new Double[3 * getNumberOfDetectors()];
		long collectionTime = (long) timer.getAttribute("TotalExptTime");
		final double clockRate = 1.0E-07;
		int counter = 0;
		for (int element = 0; element < this.getNumberOfDetectors(); element++) {

			long windowed = unpackedScalerData[0][counter];
			long total = unpackedScalerData[0][counter + 2];
			long resets = unpackedScalerData[0][counter + 3];
			counter += 4;

			Double dt = (collectionTime - resets) * clockRate;
			Double measuredRate = total * 1000.0 / collectionTime;
			if (measuredRate.isNaN() || measuredRate.isInfinite()) {
				results[element * 3] = 0.0;
				results[element * 3 + 1] = 0.0;
				results[element * 3 + 2] = 0.0;
			} else {
				results[element * 3] = measuredRate;
				results[element * 3 + 1] = scalerData[element] / windowed;
				results[element * 3 + 2] = windowed / dt;
			}
		}
		return results;
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {
		// Note We dont have resgrades so this is set to 1
		// for compatibility with xspressDetector interface
		int[] data = null;
		double[][] output = new double[numberOfDetectors][MCA_SIZE];
		for (int detector = 0; detector < numberOfDetectors; detector++) {
			logger.debug("Reading mca detector " + detector);
			xspressDetectorImpl.setCollectionTime((int) time);
			data = xspressDetectorImpl.readoutMca(detector, 0, 1, MCA_SIZE - 1); // NOTE 1 time frame
			if (data != null) {
				for (int energy = 0; energy < MCA_SIZE - 1; energy++) {
					output[detector][energy] = data[energy];
				}
			}
		}
		return output;
	}

	@Override
	public int getNumberOfElements() {
		return numberOfDetectors;
	}

	@Override
	public int getMCASize() {
		return MCA_SIZE;
	}

	@Override
	public int getMaxNumberOfRois() {
		return maxNumberOfRois;
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		this.xspressParameters = (XspressParameters) parameters;
		this.configureDetectorFromParameters();
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return xspressParameters;
	}
}
