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

import gda.jython.JythonServerFacade;
import gda.device.scannable.DummyScannable;
import gda.device.Scannable;

import java.text.DateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scan which operates over one or more pseudo devices and reads out their current position at fixed time intervals.
 * It makes no attempt to move them.
 */
public class PseudoDeviceTimeScan extends ScanBase implements Scan {
	
	private static final Logger logger = LoggerFactory.getLogger(PseudoDeviceTimeScan.class);
	
	private int collectIntervalMilliSeconds = 1000;

	private double collectIntervalSeconds = 1000;

	private int numberPoints = 0;

	// keep track of elapsed time
	private DummyScannable relativeTimePD = new DummyScannable("Time");

	// define a format to print out dates
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	/**
	 * @param pseudoDevices -
	 *            array of PseudoDevices to readout in the scan
	 * @param collectInterval -
	 *            the time in seconds between each point
	 * @param numberPoints -
	 *            the number of points to collect for - if this is set to 0 then the scan will not stop until the user
	 *            tells it to.
	 */
	public PseudoDeviceTimeScan(Scannable[] pseudoDevices, double collectInterval, int numberPoints) {
		try {
			this.collectIntervalSeconds = collectInterval;
			collectInterval *= 1000;
			this.collectIntervalMilliSeconds = ((Double) collectInterval).intValue(); // convert
			// to
			// milliseconds
			if (numberPoints != 0) {
				this.numberPoints = numberPoints;
			} else {
				this.numberPoints = Integer.MAX_VALUE;
			}

			this.allScannables.add(relativeTimePD);
			for (Scannable pd : pseudoDevices) {
				this.allScannables.add(pd);
			}

			// setUp();
			createScanDataPointPipeline();
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
			String error = "Error during scan setup: " + e.getMessage();
			JythonServerFacade.getInstance().haltCurrentScan();
			JythonServerFacade.getInstance().print(error);
		}
	}

	@Override
	public void doCollection() throws Exception {
		try {
			// work out the time now
			Date startTime = new Date();

			// report start time to user
			logger.info("Starting scan at " + df.format(startTime));

			// reset relativeTimePD
			relativeTimePD.moveTo(0.0);

			// loop
			for (long i = 0; i < numberPoints; ++i) {
				checkForInterrupts();
				collectData();
				checkForInterrupts();
				// wait until time for next iteration to start
				// this is based on the fixed point when the scan started, so
				// the
				// time to readout the scannables will not extend the total time
				if (collectIntervalMilliSeconds > 0) {
					long targetTime = startTime.getTime() + ((i + 1) * collectIntervalMilliSeconds);
					waitUntil(targetTime);
				}

				relativeTimePD.moveTo((Double) relativeTimePD.getPosition() + collectIntervalSeconds);
				checkForInterrupts();
			}
		} catch (Exception ex1) {
			interrupted = true;

			// pass on any InterruptedExceptions
			if (ex1 instanceof InterruptedException) {
				throw (InterruptedException) ex1;
			}
		}
	}

	/**
	 * Given a Java time in seconds, waits until that point and then returns. If that time has passed, then returns
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
}
