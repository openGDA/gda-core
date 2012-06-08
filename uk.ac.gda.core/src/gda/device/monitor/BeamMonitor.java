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

package gda.device.monitor;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.scan.ScanBase;

import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class to monitor the beam using a Detector. The detector notifies this class with its current data, the update
 * method then checks whether the data rate has fallen below a specified threshhold. If so the scan is paused and a new
 * thread is spawned to monitor the counter timer until the data is above the threshold value for a given number of
 * consecutive reads before the scan is resumed.
 * <p>
 * Currently this class is not Findable, so should be instantiated and used after configure-time either in a Jython
 * script or in a GUI panel.
 * <p>
 * This class relies on the Detector class being observed sending its IObervers a copy of its data.
 */
public class BeamMonitor extends MonitorBase implements Runnable, Monitor, IObserver, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(BeamMonitor.class);
	private double threshold = 1.0;

	private int waitTime = 1;

	private int consecutiveCountsAboveThreshold = 6;

	private double countTime = 1000.0;

	private String detectorName;

	private Detector detector;

	private int channel = 0;

	private int nOkReads = 0;

	/**
	 * Empty constructor for Castor configuration.
	 */
	public BeamMonitor() {

	}

	/**
	 * Constructor
	 * 
	 * @param detectorName
	 *            the name of the detector instance.
	 * @param channel
	 *            the detector channel to use for beam monitoring.
	 * @param threshold
	 *            the threshold when the scan is paused/resumed.
	 * @param waitTime
	 *            the time (in secs) between successive monitors whilst the scan is paused.
	 * @param consecutiveCountsAboveThreshold
	 *            the number of successful readings above the threshold.
	 * @param countTime
	 *            the counting time.
	 */
	public BeamMonitor(String detectorName, int channel, double threshold, int waitTime,
			int consecutiveCountsAboveThreshold, double countTime) {
		this.detectorName = detectorName;
		this.channel = channel;
		this.threshold = threshold;
		this.waitTime = waitTime;
		this.consecutiveCountsAboveThreshold = consecutiveCountsAboveThreshold;
		this.countTime = countTime;

		configure();
	}

	@Override
	public void configure() {
		if (detectorName != null) {
			detector = (Detector) Finder.getInstance().find(detectorName);
			if (detector != null)
				detector.addIObserver(this);
		}

	}

	/**
	 * @return Returns the threshold.
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            The threshold to set.
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return Returns the waitTime.
	 */
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * @param waitTime
	 *            The waitTime to set.
	 */
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	/**
	 * @return Returns the consecutiveCountsAboveThreshold.
	 */
	public int getConsecutiveCountsAboveThreshold() {
		return consecutiveCountsAboveThreshold;
	}

	/**
	 * @param consecutiveCountsAboveThreshold
	 *            The consecutiveCountsAboveThreshold to set.
	 */
	public void setConsecutiveCountsAboveThreshold(int consecutiveCountsAboveThreshold) {
		this.consecutiveCountsAboveThreshold = consecutiveCountsAboveThreshold;
	}

	/**
	 * @return Returns the countTime.
	 */
	public double getCountTime() {
		return countTime;
	}

	/**
	 * @param countTime
	 *            The countTime to set.
	 */
	public void setCountTime(double countTime) {
		this.countTime = countTime;
	}

	/**
	 * @return Returns the detectorName.
	 */
	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * @param detectorName
	 *            The detectorName to set.
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	/**
	 * @return Returns the channel.
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            The channel to set.
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return nOkReads;
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 1;
	}

	@Override
	public String getUnit() throws DeviceException {
		// just a number
		return Unit.ONE.toString();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof Detector && ScanBase.scanRunning()) {

			// convert whatever the recieved object is into an array of doubles
			Double[] currentData = ScannableUtils.objectToArray(changeCode);

			// then check the level against the threshold
			if (currentData.length > channel && currentData[channel] <= threshold) {
				JythonServerFacade.getInstance().pauseCurrentScan();
				Thread thread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
				thread.start();
			}
		}
	}

	@Override
	public synchronized void run() {
		while (nOkReads < consecutiveCountsAboveThreshold) {
			logger.info("waiting between samples for " + waitTime + " secs");
			try {
				wait(waitTime * 1000);
				if (monitorCountsReached())
					nOkReads++;
				else
					nOkReads = 0;
				logger.info("no of reads above threshold now " + nOkReads + " (requires "
						+ consecutiveCountsAboveThreshold + ")");
			} catch (InterruptedException e) {
				// Deliberately do nothing
			}
		}
		logger.debug("scan stopped waiting for beam on ");
		if (LocalProperties.check("gda.device.monitor.resumeScan", true)) {
			JythonServerFacade.getInstance().resumeCurrentScan();
		} else {
			JythonServerFacade.getInstance().restartCurrentScan();
		}
	}

	private boolean monitorCountsReached() {
		boolean countsReached = false;
		if (countTime > 0) {
			try {
				detector.setCollectionTime(countTime);
				detector.collectData();
				while (detector.getStatus() == Detector.BUSY) {
					wait(100);
				}
				// fill an array with the new data
				Double[] data = ScannableUtils.objectToArray(detector.readout());
				logger.debug("read data was " + data[channel]);
				if (data[channel] > threshold) {
					countsReached = true;
				}
			} catch (DeviceException ex) {
			} catch (InterruptedException ex) {
			}
		}
		return countsReached;
	}
}
