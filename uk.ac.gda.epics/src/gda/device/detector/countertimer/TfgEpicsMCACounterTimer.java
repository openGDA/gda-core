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

package gda.device.detector.countertimer;

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.adc.EpicsADC;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.scan.Scan;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Tfg and EpicsMCA acting as a CounterTimer combination. Since the Tfg will generally also be part of a
 * TfgScaler combination there is a slave mode. In this mode methods which set things on the Tfg do nothing.
 */
public class TfgEpicsMCACounterTimer extends TFGCounterTimer implements CounterTimer {

	private static final Logger logger = LoggerFactory.getLogger(TfgEpicsMCACounterTimer.class);

	private static int MAX_FRAMES = 256;

	private static int NUMBER_OF_ROIS = 1;

	private static boolean DIFF_MODE = true;

	private EpicsADC adc = null;

	private String adcName;

	private double[][] frameCounts;

	long channelsToCount = 0;

	protected ArrayList<String> epicsMcaNameList = new ArrayList<String>();

	private ArrayList<EpicsMCASimple> epicsMcaList = new ArrayList<EpicsMCASimple>();

	@Override
	public void configure() throws FactoryException {
		for (int i = 0; i < epicsMcaNameList.size(); i++) {
			epicsMcaList.add((EpicsMCASimple) Finder.getInstance().find(epicsMcaNameList.get(i)));
		}

		logger.debug("Finding ADC: " + adcName);
		if ((adc = (EpicsADC) Finder.getInstance().find(adcName)) == null) {
			logger.error("EpicsADC " + adcName + " not found.");
		}

		super.configure();
	}

	public void countAsync(double time) throws DeviceException {

		// JythonServerFacade.getInstance().print("CountAsync");
		System.err.println("SIC ------- " + "CountAsync");

		setMcaCollectionTimes(time);
		clearAndStart();

		if (!slave) {
			timer.countAsync(time);
		}

	}

	public int getTotalChans() {
		// + 1 for the time kludge see readout()
		return epicsMcaList.size() + 1;
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] readChans() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		double[] values = new double[getTotalChans()];
		for (int i = 0; i < values.length; i++) {
			values[i] = frameCounts[i][frame];
		}
		return values;
	}

	@Override
	public void collectData() throws DeviceException {

		countAsync(collectionTime);
	}

	/**
	 * @param frame
	 * @throws DeviceException
	 */
	public void collectFrame(int frame) throws DeviceException {

		double[] values = this.readout();

		for (int i = 0; i < values.length; i++) {
			frameCounts[i][frame] = values[i];
			// JythonServerFacade.getInstance().print(
			// "####### FRAME " + frame + " : values[" + i + "]=" +
			// values[i]);
			System.err.println("SIC ------- " + "####### FRAME " + frame + " : values[" + i + "]=" + values[i]);
		}

		clearAndStart();
	}

	/**
	 * @param frame
	 * @throws DeviceException
	 */
	public void storeFrame(int frame) throws DeviceException {
		double[] values = this.readout();

		for (int i = 0; i < values.length; i++) {
			frameCounts[i][frame] = values[i];
			// JythonServerFacade.getInstance().print(
			// "####### FRAME " + frame + " : values[" + i + "]=" +
			// values[i]);
			System.err.println("SIC ------- " + "####### FRAME " + frame + " : values[" + i + "]=" + values[i]);
		}
	}

	/**
	 * @param frame
	 * @param values
	 */
	public void setFrameValue(int frame, double[] values) {
		for (int i = 0; i < values.length; i++) {
			frameCounts[i][frame] = values[i];
		}
	}

	/**
	 * @param scan
	 */
	public void prepareForCollection(@SuppressWarnings("unused") Scan scan) {
		// Allocate an array for storing the counts for each frame, to be read out at the end using readFrame.
		frameCounts = new double[getTotalChans()][MAX_FRAMES];
	}

	/**
	 * Sets the collection times for the Epics MCAs.
	 * 
	 * @param collectionTime
	 * @throws DeviceException
	 */
	public void setMcaCollectionTimes(double collectionTime) throws DeviceException {

		for (EpicsMCASimple mca : epicsMcaList) {
			// Set the dwell time to zero to avoid any delays.
			mca.setDwellTime(0.0);

			// Get the size of the channel (in time)
			double timestep = mca.getDwellTime();
			// Calculate how many channels we need to use in order to count
			// for
			// the
			// requested time.
			channelsToCount = Math.round((collectionTime / 1000.0) / timestep);
			// Set the number of channels to count for.
			mca.setNumberOfChannels(channelsToCount);

			mca.setNumberOfRegions(NUMBER_OF_ROIS);
			EpicsMCARegionOfInterest[] r = { new EpicsMCARegionOfInterest() };

			// Set the ROIS
			// This will set all ROI to be the entire range.
			for (int i = 0; i < NUMBER_OF_ROIS; i++) {
				r[i].setRegionLow(0);
				r[i].setRegionHigh(channelsToCount - 1);
				r[i].setRegionName("TOTAL");
				mca.setRegionsOfInterest(r);
			}
		}
	}

	/**
	 * Clear and start detector.
	 * 
	 * @throws DeviceException
	 */
	public void clearAndStart() throws DeviceException {

		// JythonServerFacade.getInstance().print("clear/start.");
		System.err.println("SIC ------- " + "clear/start.");

		// Disable ADC from responding to trigger.
		adc.setScanMode("Disable");

		// Loop over each MCA and press the 'Erase & Start' button.
		for (EpicsMCASimple mca : epicsMcaList) {
			mca.eraseStartAcquisition();
		}

		// Reset ADC trigger mode
		adc.setScanMode("Burst Cont");

	}


	@Override
	public double[] readout() throws DeviceException {
		// kludged like TfgScaler to put collectionTime in values[0]
		double[] values = new double[getTotalChans()];
		values[0] = getCollectionTime();
		int j = 1;
		for (EpicsMCASimple mca : epicsMcaList) {

			double[][] counts = mca.getRegionsOfInterestCount();
			values[j] = counts[0][0];

			// Remove 2^15 from each channel if we are in differential mode.
			if (DIFF_MODE) {
				values[j] = values[j] - 32768 * channelsToCount;
			}

			// JythonServerFacade.getInstance().print(
			// "####### READOUT : values[" + j + "]=" + values[j]);
			// logger.info("SIC ------- " + "####### READOUT : values[" + j
			// + "]=" + values[j]);

			j++;
		}

		return values;
	}

	/**
	 * @return Returns the adcName.
	 */
	public String getEpicsAdcName() {
		return adcName;
	}

	/**
	 * @param adcName
	 *            The adcName to set.
	 */
	public void setEpicsAdcName(String adcName) {
		this.adcName = adcName;
	}

	/**
	 * @param channel
	 * @param epicsMcaName
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	public void setEpicsMcaName(int channel, String epicsMcaName) throws DeviceException {
		if (channel >= 0 && channel < epicsMcaNameList.size()) {
			this.extraNames = (String[]) ArrayUtils.add(this.extraNames, epicsMcaName);
		}
	}

	/**
	 * Gets an arraylist of all the names of the EpicsMCAs that are used with the countertimer.
	 * 
	 * @return ArrayList of all the EpicsMCA names.
	 */
	public ArrayList<String> getEpicsMcaNameList() {
		return epicsMcaNameList;
	}

	/**
	 * Gets the EpicsMCA name for a given channel.
	 * 
	 * @param channel
	 * @return The EpicsMCA name
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	public String getEpicsMcaName(int channel) throws DeviceException {
		String epicsMcaName = null;
		if (channel >= 0 && channel < epicsMcaNameList.size())
			epicsMcaName = epicsMcaNameList.get(channel);

		return epicsMcaName;
	}

	/**
	 * @param epicsMcaName
	 * @throws DeviceException
	 */
	@SuppressWarnings("unused")
	public void addEpicsMcaName(String epicsMcaName) throws DeviceException {
		epicsMcaNameList.add(epicsMcaName);
	}

	/**
	 * Sets the EPICS MCA names.
	 * 
	 * @param epicsMcaNames
	 *            the names
	 */
	public void setEpicsMcaNames(ArrayList<String> epicsMcaNames) {
		this.epicsMcaNameList = epicsMcaNames;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "EPICS Mca based CounterTimer with Tfg trigger";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CounterTimer";
	}

}
