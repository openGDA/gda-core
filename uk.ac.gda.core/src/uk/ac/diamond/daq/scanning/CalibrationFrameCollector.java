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

package uk.ac.diamond.daq.scanning;

import java.util.Map;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.acquisition.parameters.DetectorDocument;

/**
 * A temporary solution to dark/flat field collection within the Solstice framework.
 * <p>
 * Collects a frame with the configured detector for the main acquisition.
 */
public class CalibrationFrameCollector extends FrameCollectingScannable {

	private static final Logger logger = LoggerFactory.getLogger(CalibrationFrameCollector.class);

	/**
	 * Maps the name of the Malcolm detector used for the snapshot to the key (under /entry/instrument/) under which
	 * Malcolm will write the snapshot.
	 */
	private Map<String, String> malcolmDetectorNames;

	/**
	 * Mapping of (the name of) the first detector in the "main" scan to the model of the detector to be used for the
	 * snapshot.<br>
	 * This is principally intended for Malcolm scans, for example to allow us to use a Malcolm device for dark field
	 * collection that does not open the shutter.<br>
	 * If this is not configured, the snapshot will be taken using the first detector in the "main" scan.
	 */
	private Map<String, IRunnableDevice<? extends IDetectorModel>> snapshotDetectors;


	@Override
	void configureCollection(ScanModel model) {
		// If no detector is set for the "main" scan, we can't do anything here
		if (model.getDetectors().isEmpty()) {
			return;
		}
		// At this point in the scan, the detector models directly in the ScanModel are not up-to-date, especially as
		// regards their exposure time. However, the the models in the ScanRequest are correct, so copy the exposure
		// time from there.
		final IRunnableDevice<? extends IDetectorModel> mainScanDetector = model.getDetectors().get(0);
		final String mainScanDetectorName = mainScanDetector.getName();
		final IDetectorModel modelFromRequest = model.getBean().getScanRequest().getDetectors().get(mainScanDetectorName);
		if (modelFromRequest == null) {
			logger.error("Cannot find detector model for {}", mainScanDetectorName);
			return;
		}
		final double exposureTime = modelFromRequest.getExposureTime();

		// If no detector has been explicitly configured, use the "main scan" detector
		final IRunnableDevice<? extends IDetectorModel> acquisitionDetector = getSnapshotDetector(mainScanDetector);
		logger.debug("Setting exposure time on {} to {}", acquisitionDetector.getName(), exposureTime);

		DetectorDocument detector = new DetectorDocument.Builder()
			.withExposure(exposureTime)
			.withName(acquisitionDetector.getName())
			.withMalcolmDetectorName(getMalcolmDetectorName(acquisitionDetector))
			.build();

		setDetectorDocument(detector);
	}

	private String getMalcolmDetectorName(IRunnableDevice<? extends IDetectorModel> det) {
		return det instanceof MalcolmDevice ? malcolmDetectorNames.get(det.getName()) : null;
	}

	private IRunnableDevice<? extends IDetectorModel> getSnapshotDetector(IRunnableDevice<? extends IDetectorModel> mainScanDetector) {
		final String mainDetectorName = mainScanDetector.getName();
		if (snapshotDetectors == null || !snapshotDetectors.containsKey(mainDetectorName)) {
			return mainScanDetector;
		}
		return snapshotDetectors.get(mainDetectorName);
	}

	public void setMalcolmDetectorNames(Map<String, String> malcolmDetectorNames) {
		this.malcolmDetectorNames = malcolmDetectorNames;
	}

	public void setSnapshotDetectors(Map<String, IRunnableDevice<? extends IDetectorModel>> snapshotDetectors) {
		this.snapshotDetectors = snapshotDetectors;
	}
}
