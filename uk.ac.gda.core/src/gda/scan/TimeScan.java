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

package gda.scan;

import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.detector.DetectorBase;
import gda.device.scannable.DummyScannable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a TimeScan experiments. In this scan, no beamline settings are changed - data is collected from a detector
 * for periods of CollectTime, with periods of PauseTime in between up to a total time of TotalTime.
 * <p>
 * Time units are seconds.
 */
public class TimeScan extends ScanBase implements Scan {
	
	private static final Logger logger = LoggerFactory.getLogger(TimeScan.class);
	
	// CounterTimer extends Detector interface. So all CounterTimer classes
	// should extend DetectorBase class.
	private Detector detector;

	private ArrayList<Detector> detectors = new ArrayList<Detector>();

	int numberOfPoints;

	double pauseTime;

	double collectTime;

	/**
	 * for observers to identify the relative time of the last data point from the start of the scan. This is a
	 * DummyScannable object so that data handlers will work properly.
	 */
	volatile public DummyScannable relativeTime = new DummyScannable();

	// define a format to print out dates
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private boolean relativeTimeAdded = false;

	/**
	 * Create a TimeScan object to scan the detector specified
	 * 
	 * @param ct
	 *            the detector (counterTimer) to scan
	 * @param numberOfPoints
	 *            the number of data points to collect
	 * @param pause
	 *            the time between successive points of data collection
	 * @param collect
	 *            the time of data collection
	 */
	public TimeScan(Object ct, int numberOfPoints, double pause, double collect) {
		this.numberOfPoints = numberOfPoints;
		pauseTime = pause;
		collectTime = collect;
		relativeTime.setName("Time from start");

		detector = (Detector) ct;

		allScannables.add(detector);

		// detector.setUsedByDefault(true);

		setUp();
	}

	/**
	 * Create a TimeScan object to scan the detector specified
	 * 
	 * @param ct
	 *            the detector (counterTimer) to scan
	 * @param numberOfPoints
	 *            the number of data points to collect
	 * @param pause
	 *            the time between successive points of data collection
	 * @param collect
	 *            the time of data collection
	 */
	public TimeScan(ArrayList<Detector> ct, int numberOfPoints, double pause, double collect) {
		this.numberOfPoints = numberOfPoints;
		pauseTime = pause;
		collectTime = collect;
		relativeTime.setName("Time from start");
		detectors = ct;
		for (int i = 0; i < ct.size(); i++) {
			allScannables.add(detectors.get(i));
		}
		// detector.setUsedByDefault(true);

		setUp();
	}

	/**
	 * Create a TimeScan object to scan the detector specified
	 * 
	 * @param ct
	 *            the detector (counterTimer) to scan
	 * @param numberOfPoints
	 *            the number of data points to collect
	 * @param pause
	 *            the time between successive points of data collection
	 * @param collect
	 *            the time of data collection
	 * @param dw
	 */
	public TimeScan(ArrayList<Detector> ct, int numberOfPoints, double pause, double collect, DataWriter dw) {
		this(ct, numberOfPoints, pause, collect);
		setDataWriter(dw);
	}

	/**
	 * Create a TimeScan object to scan all active detectors
	 * 
	 * @param numberOfPoints
	 *            the number of data points to collect
	 * @param pause
	 *            the time between successive points of data collection
	 * @param collect
	 *            the time of data collection
	 */
	public TimeScan(int numberOfPoints, double pause, double collect) {
		this.numberOfPoints = numberOfPoints;
		pauseTime = pause;
		collectTime = collect;
		relativeTime.setName("Time from start");
		detector = null;

		setUp();
	}

	/**
	 * @param numberOfPoints
	 * @param pause
	 * @param collect
	 * @param datahandler
	 */
	public TimeScan(int numberOfPoints, double pause, double collect, DataWriter datahandler) {
		this(numberOfPoints, pause, collect);
		setDataWriter(datahandler);
	}

	/**
	 * Collect the data
	 * 
	 * @see gda.scan.Scan#doCollection()
	 * @throws InterruptedException
	 */
	@Override
	public void doCollection() throws InterruptedException {
		try {
			// work out the total number of collection iterations
			// int stepTimeInSeconds = new Long(Math
			// .round(pauseTime + collectTime)).intValue();

			double stepTimeInSeconds = pauseTime + collectTime;

			// add DummyScannable relativeTime to allScannables it's list of
			// scannables on the first run through only to prevent a build
			// up of
			// relativeTime fields in the data point.
			if (!relativeTimeAdded) {
				relativeTimeAdded = true;
				allScannables.add(relativeTime);
			}

			for (Detector detector : allDetectors) {
				detector.setCollectionTime(collectTime);
			}

			// work out the time now
			Date startTime = new Date();

			// report start time to user
			logger.debug("Starting scan at " + df.format(startTime) + "\n");

			// work out the maximum time that we must not go past
			long maxTime = new Double(startTime.getTime() + (numberOfPoints * stepTimeInSeconds * 1000)).longValue();

			// loop
			for (long i = 0; i < numberOfPoints; ++i) {
				Date rightNow = new Date();

				// perform data collection
				checkForInterrupts();
				collectData();
				// wait until time for next iteration to start
				// this is based on the fixed point when the start scan, so the
				// time
				// after collection finished to write out data etc. will not
				// extend
				// the total time
				checkForInterrupts();
				if (pauseTime > 0.0) {
					long targetTime = new Double(startTime.getTime() + ((((i + 1) * stepTimeInSeconds) * 1000)))
							.longValue();
					waitUntil(targetTime);
				}
				checkForInterrupts();
				// change the scannable which is holding the relative time
				relativeTime.moveTo((Double) relativeTime.getPosition() + new Double(stepTimeInSeconds));
				// make sure we have not passed the max time
				rightNow = new Date();
				if (rightNow.getTime() > maxTime) {
					break;
				}
			}
		} catch (Exception ex1) {
			interrupted = true;
			if (ex1 instanceof InterruptedException) {
				throw (InterruptedException) ex1;
			}
		}
	}

	/**
	 * Given a Java time in miliseconds, waits until that point and then returns. If that time has passed, then returns
	 * immediately;
	 * 
	 * @param targetTime
	 *            time in seconds
	 * @throws InterruptedException
	 */
	protected void waitUntil(long targetTime) throws InterruptedException {
		// get the time right now
		Date rightNow = new Date();
		long now = rightNow.getTime();
		// loop while we have not got to that point
		while (now < targetTime) {
			checkForInterrupts();
			if ((targetTime - now) > 10000) {
				Thread.sleep((targetTime - now) - 8000);
			} else {
				Thread.sleep(100);
			}
			rightNow = new Date();
			now = rightNow.getTime();
		}
	}

	/**
	 * Creates and runs a scan
	 * 
	 * @param detector
	 *            the device for data collection
	 * @param numberOfPoints
	 *            the number of data points to collect
	 * @param pauseTime
	 *            the time between successive points of data collection
	 * @param collectTime
	 *            the time of data collection
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static void runScan(DetectorBase detector, int numberOfPoints, double pauseTime, double collectTime)
			throws InterruptedException, Exception {
		TimeScan thisScan = new TimeScan(detector, numberOfPoints, pauseTime, collectTime);
		thisScan.runScan();
	}

	/**
	 * Creates and runs a scan
	 * 
	 * @param numberOfPoints
	 *            the number of data points to collect
	 * @param pauseTime
	 *            the time between successive points of data collection
	 * @param collectTime
	 *            the time of data collection
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static void runScan(int numberOfPoints, double pauseTime, double collectTime) throws InterruptedException,
			Exception {
		TimeScan thisScan = new TimeScan(numberOfPoints, pauseTime, collectTime);
		thisScan.runScan();
	}
}
