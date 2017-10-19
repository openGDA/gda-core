/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version of AreaDetectorRunnableDevice which waits for the array counter to increment before reading data.
 * <p>
 * Written as a workaround for I14 Excalibur detector, where there is a delay between the Acquire finishing and the
 * summary image becoming available.
 */
public class AreaDetectorRunnableDeviceWaitForArray extends AreaDetectorRunnableDevice {

	private static final Logger logger = LoggerFactory.getLogger(AreaDetectorRunnableDeviceWaitForArray.class);

	/**
	 * Default number of seconds the write() function will wait for the array counter to be incremented (see
	 * {@link #timeout})
	 */
	private static final long DEFAULT_TIMEOUT = 3;

	/**
	 * Maximum number of seconds the write() function will wait for the array counter to be incremented (see
	 * {@link #timeout})
	 */
	private static final long MAX_TIMEOUT = 60;

	/**
	 * Time in ms to wait between reads of array counter
	 */
	private int writeDelay = 50;

	/**
	 * Overall time in seconds to wait before timing out: see {@link #DEFAULT_TIMEOUT}<br>
	 * We have to have a timeout while waiting for the array counter to increment, as the thread that tries to write the
	 * data carries on running even if the scan has failed.
	 */
	private long timeout = DEFAULT_TIMEOUT;

	private int arrayCounter;

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		try {
			arrayCounter = getArrayCounter();
			logger.trace("run(): arrayCounter = {}", arrayCounter);
		} catch (Exception e) {
			final String message = "Error getting array counter";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}
		super.run(position);
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		try {
			final long startTime = System.currentTimeMillis();
			int currentCounter = getArrayCounter();
			logger.trace("write(): currentCounter = {}, timeout = {}", currentCounter, timeout);

			while (currentCounter <= arrayCounter) {
				final long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
				if (elapsedTime >= timeout) {
					throw new ScanningException(String.format("Wait for array counter timed out after %d s", elapsedTime));
				}
				logger.debug("Waiting {} ms for array data", writeDelay);
				Thread.sleep(writeDelay);
				currentCounter = getArrayCounter();
				logger.trace("write(): currentCounter = {}", currentCounter);
			}
		} catch (Exception e) {
			final String message = "Exception waiting for array counter to increment";
			logger.error(message, e);
			throw new ScanningException(message, e);
		}
		return super.write(pos);
	}

	private int getArrayCounter() throws Exception {
		return adDetector.getNdArray().getPluginBase().getArrayCounter_RBV();
	}

	public void setWriteDelay(int writeDelay) {
		this.writeDelay = writeDelay;
	}

	public void setTimeout(long timeout) {
		if (timeout > MAX_TIMEOUT) {
			logger.warn("Timeout too large: set to maximum of {}", MAX_TIMEOUT);
			this.timeout = MAX_TIMEOUT;
			return;
		}
		this.timeout = timeout;
	}
}
