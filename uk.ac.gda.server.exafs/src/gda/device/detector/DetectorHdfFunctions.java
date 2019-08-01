/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.detector;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.addetector.filewriter.FileWriterBase;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;
import uk.ac.gda.devices.detector.xspress4.Xspress4Detector;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;

/**
 * Static methods to set, get Hdf file path for Xspress3, Xspress4 and NXDetector detectors.
 * Originally designed to help simplify {@link DetectorPreparer} classes for I20, B18 and avoid code duplication.
 *
 */
public class DetectorHdfFunctions {

	private static final Logger logger = LoggerFactory.getLogger(DetectorHdfFunctions.class);

	private DetectorHdfFunctions() {
	}

	/**
	 * Set the hdf file path on a detector (Xspress3, Xspress4, NXdetector)
	 * @param detector
	 * @param hdfFileDir
	 * @return the original hdf file path
	 * @throws DeviceException
	 */
	public static String setHdfFilePath(Detector detector, String hdfFileDir) throws DeviceException {
		if (detector == null) {
			logger.warn("Detector object is null - cannot set hdf file path");
			return "";
		}

		String originalPath = "";
		if (detector instanceof Xspress3Detector) {
			Xspress3Detector det = (Xspress3Detector) detector;
			originalPath = det.getController().getFilePath();
			logger.debug("(Xspress3 : setting {} hdf file path template to {}", detector.getName(), hdfFileDir);
			det.getController().setFilePath(hdfFileDir);
			det.setFilePath(hdfFileDir);
		} else if (detector instanceof Xspress4Detector) {
			Xspress4Detector det = (Xspress4Detector) detector;
			originalPath = det.getXspress3Controller().getFilePath();
			logger.debug("Xspress4 : setting {} hdf file path template to {}", detector.getName(), hdfFileDir);
			det.getXspress3Controller().setFilePath(hdfFileDir);
			det.setFilePath(hdfFileDir);
		} else if (detector instanceof NXDetector) {
			originalPath = setHdfFilePath((NXDetector)detector, hdfFileDir);
		} else {
			logger.warn("Detector {} not of recognised type - cannot set hdf file path", detector.getName());
		}
		return originalPath;
	}

	/**
	 *
	 * @param nxDetector
	 * @return FileWriter plugin from an NXDetector's 'additionalPluginList'
	 */
	private static Optional<FileWriterBase> getFileWriterBase(NXDetector nxDetector) {
		return nxDetector.getAdditionalPluginList()
				.stream().filter(plugin -> plugin instanceof FileWriterBase)
				.map(plugin -> (FileWriterBase) plugin)
				.findFirst();
	}

	private static String setHdfFilePath(NXDetector nxDetector, String hdfFileDir) {
		Optional<FileWriterBase> fileWriterPlugin = getFileWriterBase(nxDetector);
		if (fileWriterPlugin.isPresent()) {
			logger.debug("NXDetector : setting {} hdf file path template to {}", nxDetector.getName(), hdfFileDir);
			String originalPath = fileWriterPlugin.get().getFilePathTemplate();
			fileWriterPlugin.get().setFilePathTemplate(hdfFileDir);
			return originalPath;
		} else {
			logger.warn("NXDetector : can't set hdf file path for {}. No filewriter plugin found.");
		}
		return "";
	}

	/**
	 * Get the hdf file path currently set on a detector (Xspress3, Xspress4, NXdetector)
	 * @param detector
	 * @return the current hdf file path
	 * @throws DeviceException
	 */
	public static String getHdfFilePath(Detector detector) throws DeviceException {
		if (detector == null) {
			logger.warn("Detector object is null - cannot get hdf file path");
			return "";
		}

		if (detector instanceof Xspress3Detector) {
			Xspress3Detector det = (Xspress3Detector) detector;
			return det.getController().getFilePath();
		} else if (detector instanceof Xspress4Detector) {
			Xspress4Detector det = (Xspress4Detector) detector;
			return det.getXspress3Controller().getFilePath();
		} else if (detector instanceof NXDetector) {
			 return getHdfFilePath((NXDetector)detector);
		} else {
			logger.warn("Detector {} not of recognised type - cannot set hdf file path", detector.getName());
		}
		return "";
	}

	private static String getHdfFilePath(NXDetector nxDetector) {
		Optional<FileWriterBase> fileWriterPlugin = getFileWriterBase(nxDetector);
		if (fileWriterPlugin.isPresent()) {
			logger.debug("NXDetector : getting {} hdf file path template", nxDetector.getName());
			return fileWriterPlugin.get().getFilePathTemplate();
		} else {
			logger.warn("NXDetector : can't get hdf file path for {}. No filewriter plugin found.", nxDetector.getName());
		}
		return "";
	}

}
