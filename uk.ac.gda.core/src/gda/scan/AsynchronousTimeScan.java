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

import gda.device.AsynchronousDetector;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;

import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An asynchronous timeScan that can be started collecting data and read out at intervals during continuous data
 * collection
 */
public class AsynchronousTimeScan extends ScanBase implements Scan {
	
	private static final Logger logger = LoggerFactory.getLogger(AsynchronousTimeScan.class);
	
	private double totalTime;

	protected Vector<Detector> asynchronousDetectors = new Vector<Detector>();

	/**
	 * for observers to identify the relative time of the last data point from the start of the scan. This is a
	 * DummyScannable object so that data handlers will work properly.
	 */
	volatile public DummyScannable relativeTime = new DummyScannable("Time from start");

	// define a format to print out dates
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private boolean relativeTimeAdded = false;

	/**
	 * Create an asynchronous timeScan that can be started collecting data and read out at intervals during continuous
	 * data collection
	 * 
	 * @param detector
	 *            the detector to be scanned
	 * @param totalTime
	 *            total collect time in milliseconds
	 */
	public AsynchronousTimeScan(Detector detector, double totalTime) {
		this.totalTime = totalTime;

		relativeTime.setName("Time from start");

		asynchronousDetectors.add(detector);

		// make sure detector not active so that counting not started before AsynchronousTimeScan, to allow monitoring
		// during counting the detector must set active true in its countAsync method

		setUp();
	}

	/**
	 * Collect the data asynchronously i.e. let the detector do all point iterations. In this case readout() will behave
	 * in an unusual way and will be detector specific, real data read out being handled by the detector itself
	 * 
	 * @see gda.scan.Scan#doCollection()
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	@Override
	public void doCollection() throws InterruptedException, DeviceException {
		try {
			if (!relativeTimeAdded) {
				relativeTimeAdded = true;
				allScannables.add(relativeTime);
			}

			Date startTime = new Date();
			// long maxTime = (long) (startTime.getTime() + (totalTime *
			// 1000));
			logger.debug("AsynchronousTimeScan: Starting scan at " + df.format(startTime) + "\n");

			checkForInterrupts();

			Date rightNow = new Date();
			logger.debug("Collecting data at " + df.format(rightNow) + "\n");

			// start data Collection
			for (Detector detector : asynchronousDetectors) {
				checkForInterrupts();

				// start the counting
				((AsynchronousDetector) detector).countAsync(totalTime);
			}
			checkForInterrupts();
		} catch (Exception ex1) {
			interrupted = true;
			if (ex1 instanceof InterruptedException) {
				throw (InterruptedException) ex1;
			}
		}
		// at end of scan remove all the observers of the detectors which were
		// registered through this scan
		finally {
			endScan();
		}
	}

	@Override
	public void endScan() throws DeviceException {
		for (Detector det : asynchronousDetectors)
			det.endCollection();
		super.endScan();

	}

	/**
	 * readout whatever pseudo-data the detector has to offer whilst it is continually collecting (might not be the raw
	 * data!)
	 * 
	 * @throws Exception
	 */
	protected void readout() throws Exception {
		// collate the data by creating a DataPoint, asynchronous
		// detectors will give the last detector file name as a String
		ScanDataPoint point = null;
		point = new ScanDataPoint();
		point.setUniqueName(name);
		point.setCurrentFilename(getDataWriter().getCurrentFileName());
		for (Scannable scannable : allScannables) {
			point.addScannable(scannable);
		}
		for (Detector scannable : allDetectors) {
			point.addDetector(scannable);
		}
		scanDataPointPipeline.put(point);

	}

	/**
	 * Given a Java time in seconds, wait until that point and then return. If that time has passed, then return
	 * immediately;
	 * 
	 * @param targetTime
	 *            time in milli-seconds
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
	 * Create and run a scan
	 * 
	 * @param detector
	 *            the detector object to be scanned
	 * @param totalTime
	 *            total collect time in milli-seconds
	 * @param readoutinterval
	 *            time after which pseudo-data is to be read out during continuous data collection in milli-seconds
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static void runScan(Detector detector, double totalTime, @SuppressWarnings("unused") double readoutinterval)
			throws InterruptedException, Exception {
		AsynchronousTimeScan thisScan = new AsynchronousTimeScan(detector, totalTime);
		thisScan.runScan();
	}
}
