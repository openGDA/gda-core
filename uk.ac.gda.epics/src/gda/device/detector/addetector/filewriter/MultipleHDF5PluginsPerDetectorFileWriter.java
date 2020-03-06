/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.filewriter;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataHDF5FileLinkAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;

public class MultipleHDF5PluginsPerDetectorFileWriter extends MultipleImagesPerHDF5FileWriter {

	private static Logger logger = LoggerFactory.getLogger(MultipleHDF5PluginsPerDetectorFileWriter.class);

	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		logger.trace("read({}), firstReadoutInScan={}, numCaptured={}, numToBeCaptured={}", maxToRead, firstReadoutInScan, numCaptured, numToBeCaptured);
		logStackTrace(logger, "read(...)");
		NXDetectorDataAppender dataAppender;
		// wait until the NumCaptured_RBV is equal to or exceeds maxToRead.
		if (isEnabled()) {
			checkErrorStatus();
		}
		try {
			getNdFile().getPluginBase().checkDroppedFrames();
		} catch (Exception e) {
			throw new DeviceException("Error in " + getName(), e);
		}
		if (firstReadoutInScan) {
			dataAppender = new NXDetectorDataHDF5FileLinkAppender(expectedFullFileName, getxPixelSize(), getyPixelSize(), getxPixelSizeUnit(),
					getyPixelSizeUnit(), getName());
			numToBeCaptured = 1;
			numCaptured = 0;
		} else {
			dataAppender = new NXDetectorDataNullAppender();
			numToBeCaptured++;
		}
		logger.debug("firstReadoutInScan={}, numCaptured={}, numToBeCaptured={}", firstReadoutInScan, numCaptured, numToBeCaptured);

		LogLimiter logLimiter = new LogLimiter(Duration.ofSeconds(10), true);

		while (numCaptured < numToBeCaptured) {
			if (logLimiter.isLogDue()) {
				logger.info("Waiting for {} points, but only {} captured after {} seconds on {}", numToBeCaptured, numCaptured,
						logLimiter.getTimeSinceStart().getSeconds(), getName());
			}
			try {
				getNdFile().getPluginBase().checkDroppedFrames();
			} catch (Exception e) {
				throw new DeviceException("Error in " + getName(), e);
			}
			try {
				numCaptured = getNdFileHDF5().getNumCaptured_RBV();
			} catch (Exception e) {
				throw new DeviceException("Error in getCapture_RBV" + getName(), e);
			}
			Thread.sleep(50);
		}
		firstReadoutInScan = false;
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;
	}

}
